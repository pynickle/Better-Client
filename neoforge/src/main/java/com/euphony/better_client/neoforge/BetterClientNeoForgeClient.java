package com.euphony.better_client.neoforge;

import com.euphony.better_client.BetterClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = BetterClient.MOD_ID, dist = Dist.CLIENT)
public class BetterClientNeoForgeClient {
    public BetterClientNeoForgeClient(IEventBus bus) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (client, screen) -> BetterClient.config.makeScreen(screen));
    }
}
