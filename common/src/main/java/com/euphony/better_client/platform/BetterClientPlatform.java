package com.euphony.better_client.platform;

import java.nio.file.Path;
import java.util.Set;

public interface BetterClientPlatform {
    Path getConfigDirectory();

    boolean isModLoaded(String modId);

    Set<String> getLoadedModIds();

    String getModDisplayName(String modId);

    PlatformType getPlatformType();

    /**
     * 声明本模组在 HUD 右侧状态栏区域额外占用的高度
     * <p>
     * NeoForge 用一个累加器来排布右侧状态栏，其它元素（如手持物品名）会据此让位；
     * 原版与 Fabric 没有这套机制，实现为空操作。
     *
     * @param height 占用的像素高度
     */
    default void addRightStatusBarHeight(int height) {
    }
}
