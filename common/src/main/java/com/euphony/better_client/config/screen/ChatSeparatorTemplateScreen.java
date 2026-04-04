package com.euphony.better_client.config.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.config.Config.DEFAULTS;

public class ChatSeparatorTemplateScreen extends Screen {
    private final Screen parent;
    private MultiLineEditBox editor;

    public ChatSeparatorTemplateScreen(Screen parent) {
        super(Component.translatable("text.better_client.chat_separator.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int editorWidth = this.width - 40;
        int editorHeight = this.height - 120;
        this.editor = MultiLineEditBox.builder()
                .setX(20)
                .setY(50)
                .setPlaceholder(Component.translatable("text.better_client.chat_separator.placeholder"))
                .setShowBackground(true)
                .setShowDecorations(true)
                .build(
                        this.font,
                        editorWidth,
                        editorHeight,
                        Component.translatable("text.better_client.chat_separator.title"));
        this.editor.setCharacterLimit(1024);
        this.editor.setLineLimit(12);
        this.editor.setValue(config.chatHistorySeparatorTemplate);
        this.editor.setFocused(true);
        this.addRenderableWidget(this.editor);
        this.setInitialFocus(this.editor);

        int buttonY = this.height - 30;
        this.addRenderableWidget(
                Button.builder(Component.translatable("text.better_client.chat_separator.reset"), button -> {
                            this.editor.setValue(DEFAULTS.chatHistorySeparatorTemplate);
                        })
                        .bounds(20, buttonY, 100, 20)
                        .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(this.width / 2 - 102, buttonY, 100, 20)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    config.chatHistorySeparatorTemplate = this.editor.getValue();
                    this.minecraft.setScreen(this.parent);
                })
                .bounds(this.width / 2 + 2, buttonY, 100, 20)
                .build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(this.font, this.title, this.width / 2, 18, 0xFFFFFFFF);
    }
}
