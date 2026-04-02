package com.euphony.better_client.client.resource;

import com.google.gson.JsonPrimitive;
import net.ramixin.mixson.Mixson;
import net.ramixin.mixson.MixsonCodecs;
import net.ramixin.mixson.enums.ErrorPolicy;
import net.ramixin.mixson.enums.Lifetime;

import static com.euphony.better_client.BetterClient.config;

// Copy From
// https://github.com/pajicadvance/misctweaks/blob/multicutter/src/main/java/me/pajic/misctweaks/mixson/ClientResourceModifications.java
public class ResourceModification {
    public static void init() {
        if (config.enableLowerShield) {
            Mixson.registerEvent(
                    MixsonCodecs.JSON_ELEMENT,
                    Mixson.DEFAULT_PRIORITY,
                    Lifetime.PERSISTENT,
                    ErrorPolicy.IGNORE,
                    "better_client:lower_shield",
                    index -> index.id().getNamespace().equals("minecraft")
                            && index.id().getPath().equals("models/item/shield"),
                    context -> {
                        if (context.getFile().getAsJsonObject().has("display")
                                && context.getFile()
                                .getAsJsonObject()
                                .getAsJsonObject("display")
                                .has("firstperson_lefthand")) {
                            context.getFile()
                                    .getAsJsonObject()
                                    .getAsJsonObject("display")
                                    .getAsJsonObject("firstperson_lefthand")
                                    .getAsJsonArray("translation")
                                    .set(1, new JsonPrimitive(config.shieldOffset));
                        }
                    }
            );
        }
    }
}
