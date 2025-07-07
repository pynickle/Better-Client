package com.euphony.better_client.utils.records;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record BundleCandidate(Slot slot, int index, ItemStack itemStack,
                              double efficiency) implements Comparable<BundleCandidate> {

    @Override
    public int compareTo(BundleCandidate other) {
        return Double.compare(other.efficiency, this.efficiency);
    }
}
