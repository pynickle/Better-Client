package com.euphony.better_client.client.resource;

import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.ramixin.mixson.inline.Mixson;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;

// Copy From https://github.com/pajicadvance/misctweaks/blob/multicutter/src/main/java/me/pajic/misctweaks/mixson/ClientResourceModifications.java
public class ResourceModification {
    public static void init() {
        if (config.enableLowerShield) List.of(Items.SHIELD).forEach(item -> {
            Identifier id = item.arch$registryName();
            if (id != null) {
                String namespace = id.getNamespace();
                String path = id.getPath();
                Mixson.registerEvent(
                        Mixson.DEFAULT_PRIORITY,
                        rl -> rl.getNamespace().equals(namespace) && rl.getPath().equals("models/item/" + path),
                        "misctweaks:modify_" + namespace + "_" + path + "_model",
                        context -> {
                            if (
                                    context.getFile().getAsJsonObject().has("display") &&
                                            context.getFile().getAsJsonObject().getAsJsonObject("display")
                                                    .has("firstperson_lefthand")
                            ) {
                                context.getFile().getAsJsonObject()
                                        .getAsJsonObject("display")
                                        .getAsJsonObject("firstperson_lefthand")
                                        .getAsJsonArray("translation")
                                        .set(1, new JsonPrimitive(config.shieldOffset));
                            }
                        },
                        true
                );
            }
        });
    }
}
