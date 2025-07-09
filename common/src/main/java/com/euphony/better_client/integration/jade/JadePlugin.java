package com.euphony.better_client.integration.jade;

import net.minecraft.world.entity.npc.Villager;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(VillagerComponentProvider.INSTANCE, Villager.class);
        registration.markAsClientFeature(JadeConstants.VILLAGER_REPUTATION);
    }
}
