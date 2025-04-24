package com.euphony.better_client.neoforge;

import com.euphony.better_client.BetterClient;
import net.neoforged.fml.common.Mod;

@Mod(BetterClient.MOD_ID)
public final class BetterClientNeoForge {
    public BetterClientNeoForge() {
        // Run our common setup.
        BetterClient.init();
    }
}
