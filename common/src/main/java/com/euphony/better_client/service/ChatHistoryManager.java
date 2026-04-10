package com.euphony.better_client.service;

import com.euphony.better_client.mixin.accessor.ChatComponentStateAccessor;
import com.euphony.better_client.utils.FormatUtils;
import com.euphony.better_client.utils.JsonUtils;
import com.euphony.better_client.utils.mc.DataUtils;
import com.euphony.better_client.utils.mc.LevelUtils;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.euphony.better_client.BetterClient.LOGGER;
import static com.euphony.better_client.BetterClient.config;

public final class ChatHistoryManager {
    private static final String SEPARATOR_MARKER = "\uE000";
    private static final Path CHAT_HISTORY_PATH = DataUtils.getDataDir().resolve("chat_history.json");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final Map<String, RuntimeChatState> RUNTIME_STATES = new HashMap<>();
    private static final Object PERSISTENT_STORE_LOCK = new Object();

    private static boolean allowVanillaClear;
    private static boolean restoringChat;
    private static PersistentChatStore persistentChatStore;
    private static boolean persistentStoreSaveScheduled;
    private static boolean persistentStoreSaveDirty;

    private ChatHistoryManager() {}

    public static boolean shouldCancelVanillaClear(boolean history) {
        return history && config.enableChatHistoryRetention && !allowVanillaClear;
    }

    public static void saveCurrentSession() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!config.enableChatHistoryRetention || minecraft.level == null) {
            return;
        }

        saveChatState(
                LevelUtils.getCurrentSessionKey(minecraft, minecraft.level),
                LevelUtils.getCurrentSessionName(minecraft, minecraft.level));
    }

    public static void handleLevelTransition(ClientLevel previousLevel, ClientLevel currentLevel) {
        if (!config.enableChatHistoryRetention) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        String previousSessionKey =
                previousLevel == null ? null : LevelUtils.getCurrentSessionKey(minecraft, previousLevel);
        String currentSessionKey =
                currentLevel == null ? null : LevelUtils.getCurrentSessionKey(minecraft, currentLevel);

        if (Objects.equals(previousSessionKey, currentSessionKey)) {
            return;
        }

        ChatComponent chat = minecraft.gui.getChat();
        clearChat(chat);

        if (currentLevel == null) {
            return;
        }

        String sessionKey = currentSessionKey;
        RuntimeChatState runtimeState = RUNTIME_STATES.get(sessionKey);
        if (runtimeState != null) {
            restoreRuntimeState(chat, runtimeState);
            return;
        }

        if (config.enablePersistentChatStorage) {
            PersistentChatState persistentState = getPersistentStore().sessions.get(sessionKey);
            if (persistentState != null) {
                restorePersistentState(chat, persistentState);
            }
        }
    }

    private static void displaySeparator(ChatComponent chat, String separator) {
        if (restoringChat) {
            return;
        }

        if (separator.isBlank()) {
            return;
        }

        separator = separator.replace('&', '§');

        restoringChat = true;
        try {
            for (String line : separator.split("\n", -1)) {
                chat.addClientSystemMessage(Component.literal(SEPARATOR_MARKER + line));
            }
        } finally {
            restoringChat = false;
        }
    }

    private static void saveChatState(String sessionKey, String sessionName) {
        ChatComponent chat = Minecraft.getInstance().gui.getChat();
        ChatComponent.State state = sanitizeState(chat.storeState());
        Instant disconnectTime = Instant.now();

        RUNTIME_STATES.put(sessionKey, new RuntimeChatState(state, sessionName, disconnectTime));

        if (!config.enablePersistentChatStorage) {
            return;
        }

        PersistentChatStore store = getPersistentStore();
        synchronized (PERSISTENT_STORE_LOCK) {
            store.sessions.put(sessionKey, PersistentChatState.from(state, sessionName, disconnectTime));
        }
        savePersistentStoreAsync();
    }

    private static void restoreRuntimeState(ChatComponent chat, RuntimeChatState runtimeState) {
        if (isStateEmpty(runtimeState.state())) {
            return;
        }

        restoringChat = true;
        try {
            chat.restoreState(runtimeState.state());
        } finally {
            restoringChat = false;
        }

        displaySeparator(chat, formatSeparator(runtimeState.sessionName(), runtimeState.disconnectTime()));
    }

    private static void restorePersistentState(ChatComponent chat, PersistentChatState persistentState) {
        if ((persistentState.messages == null || persistentState.messages.isEmpty())
                && (persistentState.recentChat == null || persistentState.recentChat.isEmpty())) {
            return;
        }

        restoringChat = true;
        try {
            List<GuiMessage> restoredMessages =
                    rebasePersistedMessageTimes(deserializeMessages(persistentState.messages));
            ChatComponent.State restoredState = new ChatComponent.State(
                    restoredMessages,
                    persistentState.recentChat == null ? List.of() : List.copyOf(persistentState.recentChat),
                    List.of());
            chat.restoreState(restoredState);
        } finally {
            restoringChat = false;
        }

        displaySeparator(
                chat,
                formatSeparator(
                        persistentState.sessionName, Instant.ofEpochMilli(persistentState.lastDisconnectEpochMillis)));
    }

    private static String formatSeparator(String sessionName, Instant disconnectTime) {
        String template = config.chatHistorySeparatorTemplate == null ? "" : config.chatHistorySeparatorTemplate;
        String normalizedTemplate = template.replace("\\n", "\n");
        return FormatUtils.format(
                normalizedTemplate,
                Map.of(
                        "session_name", sessionName,
                        "session_key", LevelUtils.getCurrentSessionKey(),
                        "disconnect_time", TIME_FORMATTER.format(disconnectTime.atZone(ZoneId.systemDefault())),
                        "disconnect_date", DATE_FORMATTER.format(disconnectTime.atZone(ZoneId.systemDefault())),
                        "disconnect_datetime",
                                DATE_TIME_FORMATTER.format(disconnectTime.atZone(ZoneId.systemDefault())),
                        "last_disconnect_time", TIME_FORMATTER.format(disconnectTime.atZone(ZoneId.systemDefault())),
                        "last_disconnect_date", DATE_FORMATTER.format(disconnectTime.atZone(ZoneId.systemDefault())),
                        "last_disconnect_datetime",
                                DATE_TIME_FORMATTER.format(disconnectTime.atZone(ZoneId.systemDefault()))));
    }

    private static boolean isStateEmpty(ChatComponent.State state) {
        ChatComponentStateAccessor accessor = (ChatComponentStateAccessor) state;
        return accessor.better_client$getMessages().isEmpty()
                && accessor.better_client$getHistory().isEmpty();
    }

    private static ChatComponent.State sanitizeState(ChatComponent.State state) {
        if (!config.cleanRestoredChatSeparatorsOnSave) {
            return state;
        }

        ChatComponentStateAccessor accessor = (ChatComponentStateAccessor) state;
        List<GuiMessage> filteredMessages = accessor.better_client$getMessages().stream()
                .filter(message -> !isSeparatorMessage(message))
                .toList();
        return new ChatComponent.State(filteredMessages, List.copyOf(accessor.better_client$getHistory()), List.of());
    }

    private static List<GuiMessage> rebasePersistedMessageTimes(List<GuiMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }

        int currentGuiTicks = Minecraft.getInstance().gui.getGuiTicks();
        int newestAddedTime = messages.stream().mapToInt(GuiMessage::addedTime).max().orElse(currentGuiTicks);
        int tickOffset = currentGuiTicks - newestAddedTime;

        List<GuiMessage> rebasedMessages = new ArrayList<>(messages.size());
        for (GuiMessage message : messages) {
            int rebasedAddedTime = Math.max(0, message.addedTime() + tickOffset);
            rebasedMessages.add(new GuiMessage(
                    rebasedAddedTime,
                    message.content(),
                    message.signature(),
                    message.source(),
                    message.tag()));
        }

        return rebasedMessages;
    }

    private static boolean isSeparatorMessage(GuiMessage message) {
        if (message == null || message.content() == null) {
            return false;
        }

        return message.content().getString().startsWith(SEPARATOR_MARKER);
    }

    private static void clearChat(ChatComponent chat) {
        allowVanillaClear = true;
        try {
            chat.clearMessages(true);
        } finally {
            allowVanillaClear = false;
        }
    }

    private static PersistentChatStore getPersistentStore() {
        synchronized (PERSISTENT_STORE_LOCK) {
            if (persistentChatStore != null) {
                return persistentChatStore;
            }

            persistentChatStore = loadPersistentStore();
            return persistentChatStore;
        }
    }

    private static PersistentChatStore loadPersistentStore() {
        if (Files.notExists(CHAT_HISTORY_PATH)) {
            return new PersistentChatStore();
        }

        try (Reader reader = Files.newBufferedReader(CHAT_HISTORY_PATH, StandardCharsets.UTF_8)) {
            JsonObject root = JsonUtils.GSON.fromJson(reader, JsonObject.class);
            return deserializeStore(root);
        } catch (IOException | JsonParseException e) {
            LOGGER.error("[BetterClient] Failed to load chat history data", e);
            return new PersistentChatStore();
        }
    }

    private static void savePersistentStoreAsync() {
        synchronized (PERSISTENT_STORE_LOCK) {
            persistentStoreSaveDirty = true;
            if (persistentStoreSaveScheduled) {
                return;
            }
            persistentStoreSaveScheduled = true;
        }

        Util.ioPool().execute(() -> {
            while (true) {
                PersistentChatStore snapshot;
                synchronized (PERSISTENT_STORE_LOCK) {
                    if (!persistentStoreSaveDirty) {
                        persistentStoreSaveScheduled = false;
                        return;
                    }

                    persistentStoreSaveDirty = false;
                    snapshot = copyStore(persistentChatStore);
                }

                writePersistentStore(snapshot);
            }
        });
    }

    private static void writePersistentStore(PersistentChatStore store) {
        try {
            Path basePath = CHAT_HISTORY_PATH.getParent();
            if (basePath != null && Files.notExists(basePath)) {
                Files.createDirectories(basePath);
            }

            Files.writeString(CHAT_HISTORY_PATH, JsonUtils.GSON.toJson(serializeStore(store)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[BetterClient] Failed to save chat history data", e);
        }
    }

    private static PersistentChatStore copyStore(PersistentChatStore store) {
        PersistentChatStore copy = new PersistentChatStore();
        if (store == null) {
            return copy;
        }

        for (Map.Entry<String, PersistentChatState> entry : store.sessions.entrySet()) {
            copy.sessions.put(entry.getKey(), copyState(entry.getValue()));
        }
        return copy;
    }

    private static PersistentChatState copyState(PersistentChatState state) {
        PersistentChatState copy = new PersistentChatState();
        copy.sessionName = state.sessionName;
        copy.lastDisconnectEpochMillis = state.lastDisconnectEpochMillis;
        copy.messages = copyMessages(state.messages);
        copy.recentChat = state.recentChat == null ? null : new ArrayList<>(state.recentChat);
        return copy;
    }

    private static List<PersistentChatMessage> copyMessages(List<PersistentChatMessage> messages) {
        if (messages == null) {
            return null;
        }

        List<PersistentChatMessage> copy = new ArrayList<>(messages.size());
        for (PersistentChatMessage message : messages) {
            PersistentChatMessage copiedMessage = new PersistentChatMessage();
            copiedMessage.addedTime = message.addedTime;
            copiedMessage.content = message.content == null ? null : message.content.deepCopy();
            copiedMessage.signature = message.signature == null ? null : message.signature.deepCopy();
            copiedMessage.source = message.source;
            copiedMessage.tag = copyTag(message.tag);
            copy.add(copiedMessage);
        }
        return copy;
    }

    private static PersistentGuiMessageTag copyTag(PersistentGuiMessageTag tag) {
        if (tag == null) {
            return null;
        }

        PersistentGuiMessageTag copy = new PersistentGuiMessageTag();
        copy.indicatorColor = tag.indicatorColor;
        copy.icon = tag.icon;
        copy.text = tag.text == null ? null : tag.text.deepCopy();
        copy.logTag = tag.logTag;
        return copy;
    }

    private record RuntimeChatState(ChatComponent.State state, String sessionName, Instant disconnectTime) {}

    private static final class PersistentChatStore {
        private final Map<String, PersistentChatState> sessions = new LinkedHashMap<>();
    }

    private static final class PersistentChatState {
        private String sessionName;
        private long lastDisconnectEpochMillis;
        private List<PersistentChatMessage> messages;
        private List<String> recentChat;

        private static PersistentChatState from(ChatComponent.State state, String sessionName, Instant disconnectTime) {
            ChatComponentStateAccessor accessor = (ChatComponentStateAccessor) state;
            PersistentChatState persistentState = new PersistentChatState();
            persistentState.sessionName = sessionName;
            persistentState.lastDisconnectEpochMillis = disconnectTime.toEpochMilli();
            persistentState.messages =
                    new ArrayList<>(accessor.better_client$getMessages().size());
            for (GuiMessage guiMessage : accessor.better_client$getMessages()) {
                PersistentChatMessage serialized = serializeMessage(guiMessage);
                if (serialized != null) {
                    persistentState.messages.add(serialized);
                }
            }
            persistentState.recentChat = new ArrayList<>(accessor.better_client$getHistory());
            return persistentState;
        }
    }

    private static final class PersistentChatMessage {
        private int addedTime;
        private JsonElement content;
        private JsonElement signature;
        private String source;
        private PersistentGuiMessageTag tag;
    }

    private static final class PersistentGuiMessageTag {
        private int indicatorColor;
        private String icon;
        private JsonElement text;
        private String logTag;
    }

    private static PersistentChatStore deserializeStore(JsonObject root) {
        PersistentChatStore store = new PersistentChatStore();
        if (root == null || !root.has("sessions") || !root.get("sessions").isJsonObject()) {
            return store;
        }

        JsonObject sessions = root.getAsJsonObject("sessions");
        for (Map.Entry<String, JsonElement> entry : sessions.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            PersistentChatState state = deserializeState(entry.getValue().getAsJsonObject());
            store.sessions.put(entry.getKey(), state);
        }

        return store;
    }

    private static PersistentChatState deserializeState(JsonObject json) {
        PersistentChatState state = new PersistentChatState();
        state.sessionName = getAsString(json, "sessionName", "Unknown");
        state.lastDisconnectEpochMillis = getAsLong(json, "lastDisconnectEpochMillis", 0L);
        state.messages = deserializeMessages(json.get("messages"));
        state.recentChat = deserializeStringList(json.get("recentChat"));
        return state;
    }

    private static JsonObject serializeStore(PersistentChatStore store) {
        JsonObject root = new JsonObject();
        JsonObject sessions = new JsonObject();
        for (Map.Entry<String, PersistentChatState> entry : store.sessions.entrySet()) {
            sessions.add(entry.getKey(), serializeState(entry.getValue()));
        }
        root.add("sessions", sessions);
        return root;
    }

    private static JsonObject serializeState(PersistentChatState state) {
        JsonObject json = new JsonObject();
        json.addProperty("sessionName", state.sessionName);
        json.addProperty("lastDisconnectEpochMillis", state.lastDisconnectEpochMillis);
        json.add("messages", serializeMessages(state.messages));
        json.add("recentChat", serializeStringList(state.recentChat));
        return json;
    }

    private static JsonArray serializeMessages(List<PersistentChatMessage> messages) {
        JsonArray array = new JsonArray();
        if (messages == null) {
            return array;
        }

        for (PersistentChatMessage message : messages) {
            JsonObject json = new JsonObject();
            json.addProperty("addedTime", message.addedTime);
            json.add("content", message.content == null ? JsonNull.INSTANCE : message.content);
            json.add("signature", message.signature == null ? JsonNull.INSTANCE : message.signature);
            if (message.source != null) {
                json.addProperty("source", message.source);
            }
            json.add("tag", serializePersistentTag(message.tag));
            array.add(json);
        }

        return array;
    }

    private static List<PersistentChatMessage> deserializeMessages(JsonElement element) {
        List<PersistentChatMessage> messages = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return messages;
        }

        for (JsonElement entry : element.getAsJsonArray()) {
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                PersistentChatMessage migrated = new PersistentChatMessage();
                migrated.content = encodeComponent(Component.literal(entry.getAsString()));
                migrated.signature = JsonNull.INSTANCE;
                migrated.tag = null;
                messages.add(migrated);
                continue;
            }

            if (!entry.isJsonObject()) {
                continue;
            }

            JsonObject json = entry.getAsJsonObject();
            PersistentChatMessage message = new PersistentChatMessage();
            message.addedTime = getAsInt(json, "addedTime", 0);
            message.content = json.has("content") ? json.get("content") : JsonNull.INSTANCE;
            message.signature = json.has("signature") ? json.get("signature") : JsonNull.INSTANCE;
            message.source = getAsNullableString(json, "source");
            message.tag = deserializeTag(json.get("tag"));
            messages.add(message);
        }

        return messages;
    }

    private static JsonElement serializeTag(PersistentGuiMessageTag tag) {
        if (tag == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject json = new JsonObject();
        json.addProperty("indicatorColor", tag.indicatorColor);
        if (tag.icon != null) {
            json.addProperty("icon", tag.icon);
        }
        json.add("text", tag.text == null ? JsonNull.INSTANCE : tag.text);
        if (tag.logTag != null) {
            json.addProperty("logTag", tag.logTag);
        }
        return json;
    }

    private static PersistentGuiMessageTag deserializeTag(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }

        JsonObject json = element.getAsJsonObject();
        PersistentGuiMessageTag tag = new PersistentGuiMessageTag();
        tag.indicatorColor = getAsInt(json, "indicatorColor", 0);
        tag.icon = getAsNullableString(json, "icon");
        tag.text = json.has("text") ? json.get("text") : JsonNull.INSTANCE;
        tag.logTag = getAsNullableString(json, "logTag");
        return tag;
    }

    private static JsonArray serializeStringList(List<String> values) {
        JsonArray array = new JsonArray();
        if (values == null) {
            return array;
        }

        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private static List<String> deserializeStringList(JsonElement element) {
        List<String> values = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return values;
        }

        for (JsonElement entry : element.getAsJsonArray()) {
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                values.add(entry.getAsString());
            }
        }

        return values;
    }

    private static PersistentChatMessage serializeMessage(GuiMessage guiMessage) {
        if (guiMessage == null) {
            return null;
        }

        if (config.cleanRestoredChatSeparatorsOnSave && isSeparatorMessage(guiMessage)) {
            return null;
        }

        PersistentChatMessage message = new PersistentChatMessage();
        message.addedTime = guiMessage.addedTime();
        Component content = guiMessage.content();
        if (content == null) {
            return null;
        }

        message.content = encodeComponent(content);
        message.signature = encodeSignature(guiMessage.signature());
        message.source = guiMessage.source().name();
        message.tag = serializeTag(guiMessage.tag());
        return message;
    }

    private static PersistentGuiMessageTag serializeTag(GuiMessageTag tag) {
        if (tag == null) {
            return null;
        }

        PersistentGuiMessageTag serialized = new PersistentGuiMessageTag();
        serialized.indicatorColor = tag.indicatorColor();
        GuiMessageTag.Icon icon = tag.icon();
        serialized.icon = icon == null ? null : icon.name();
        Component text = tag.text();
        serialized.text = text == null ? JsonNull.INSTANCE : encodeComponent(text);
        serialized.logTag = tag.logTag();
        return serialized;
    }

    private static GuiMessageTag deserializeTag(PersistentGuiMessageTag tag) {
        if (tag == null) {
            return null;
        }

        try {
            GuiMessageTag.Icon icon = deserializeIcon(tag.icon);
            Component text = decodeComponent(tag.text);
            return new GuiMessageTag(tag.indicatorColor, icon, text, tag.logTag);
        } catch (RuntimeException e) {
            LOGGER.error("[BetterClient] Failed to deserialize chat tag", e);
        }

        return null;
    }

    private static GuiMessageTag.Icon deserializeIcon(String iconName) {
        if (iconName == null || iconName.isBlank()) {
            return null;
        }

        try {
            return GuiMessageTag.Icon.valueOf(iconName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static List<GuiMessage> deserializeMessages(List<PersistentChatMessage> storedMessages) {
        List<GuiMessage> messages = new ArrayList<>();
        if (storedMessages == null) {
            return messages;
        }

        for (PersistentChatMessage storedMessage : storedMessages) {
            GuiMessage message = deserializeMessage(storedMessage);
            if (message != null) {
                messages.add(message);
            }
        }

        return messages;
    }

    private static GuiMessage deserializeMessage(PersistentChatMessage storedMessage) {
        Component content = decodeComponent(storedMessage.content);
        if (content == null) {
            return null;
        }

        return new GuiMessage(
                storedMessage.addedTime,
                content,
                decodeSignature(storedMessage.signature),
                deserializeSource(storedMessage),
                deserializeTag(storedMessage.tag));
    }

    private static GuiMessageSource deserializeSource(PersistentChatMessage storedMessage) {
        String sourceName = storedMessage.source;
        if (sourceName != null && !sourceName.isBlank()) {
            try {
                return GuiMessageSource.valueOf(sourceName);
            } catch (IllegalArgumentException ignored) {
                return fallbackSource(storedMessage.signature);
            }
        }

        return fallbackSource(storedMessage.signature);
    }

    private static GuiMessageSource fallbackSource(JsonElement signature) {
        return signature != null && !signature.isJsonNull() ? GuiMessageSource.PLAYER : GuiMessageSource.SYSTEM_CLIENT;
    }

    private static JsonElement encodeComponent(Component component) {
        if (component == null) {
            return JsonNull.INSTANCE;
        }

        return ComponentSerialization.CODEC
                .encodeStart(JsonOps.INSTANCE, component)
                .resultOrPartial(error -> LOGGER.error("[BetterClient] Failed to encode chat component: {}", error))
                .orElse(JsonNull.INSTANCE);
    }

    private static Component decodeComponent(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        return ComponentSerialization.CODEC
                .parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> LOGGER.error("[BetterClient] Failed to decode chat component: {}", error))
                .orElse(null);
    }

    private static JsonElement encodeSignature(MessageSignature signature) {
        if (signature == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject json = new JsonObject();
        json.addProperty("bytes", Base64.getEncoder().encodeToString(signature.bytes()));
        return json;
    }

    private static MessageSignature decodeSignature(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }

        JsonObject json = element.getAsJsonObject();
        String bytes = getAsNullableString(json, "bytes");
        if (bytes == null || bytes.isBlank()) {
            return null;
        }

        try {
            return new MessageSignature(Base64.getDecoder().decode(bytes));
        } catch (IllegalArgumentException e) {
            LOGGER.error("[BetterClient] Failed to decode chat signature", e);
            return null;
        }
    }

    private static JsonElement serializePersistentTag(PersistentGuiMessageTag tag) {
        if (tag == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject json = new JsonObject();
        json.addProperty("indicatorColor", tag.indicatorColor);
        if (tag.icon != null) {
            json.addProperty("icon", tag.icon);
        }
        json.add("text", tag.text == null ? JsonNull.INSTANCE : tag.text);
        if (tag.logTag != null) {
            json.addProperty("logTag", tag.logTag);
        }
        return json;
    }

    private static String getAsString(JsonObject json, String key, String fallback) {
        String value = getAsNullableString(json, key);
        return value == null ? fallback : value;
    }

    private static String getAsNullableString(JsonObject json, String key) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }

        JsonElement value = json.get(key);
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : null;
    }

    private static long getAsLong(JsonObject json, String key, long fallback) {
        if (json == null || !json.has(key) || !json.get(key).isJsonPrimitive()) {
            return fallback;
        }

        try {
            return json.get(key).getAsLong();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int getAsInt(JsonObject json, String key, int fallback) {
        if (json == null || !json.has(key) || !json.get(key).isJsonPrimitive()) {
            return fallback;
        }

        try {
            return json.get(key).getAsInt();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
