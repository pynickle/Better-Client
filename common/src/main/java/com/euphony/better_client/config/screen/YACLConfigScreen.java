package com.euphony.better_client.config.screen;

import com.euphony.better_client.config.screen.category.*;
import com.euphony.better_client.config.screen.widget.CategoryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class YACLConfigScreen extends Screen {
    private final Screen parent;
    @Nullable
    private Screen chatConfigScreen = null;
    @Nullable
    private Screen cheatingConfigScreen = null;
    @Nullable
    private Screen merchantConfigScreen = null;
    @Nullable
    private Screen optimizationConfigScreen = null;
    @Nullable
    private Screen screenConfigScreen = null;
    @Nullable
    private Screen tooltipConfigScreen = null;
    @Nullable
    private Screen utilityConfigScreen = null;
    @Nullable
    private Screen visualityConfigScreen = null;

    public YACLConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("yacl3.config.better_client:config"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        assert this.minecraft != null;
        context.pose().pushMatrix();
        float scale = 2.0F;
        context.pose().translate(this.width / 2, 10);
        context.pose().scale(scale, scale);
        context.pose().translate(-this.width / 2, 0);
        context.drawCenteredString(this.minecraft.font, Component.translatable("yacl3.config.better_client:config"), this.width / 2, 0, 0xFFFFFFFF);
        context.pose().popMatrix();
    }

    @Override
    protected void init() {
        super.init();

        // 使用4列布局来适应8个按钮
        int buttonWidth = (this.width - 100) / 4;
        int buttonHeight = 20;
        int spacing = 18;
        int startX = 20;
        int startY = 50;

        // 第一行按钮
        addRenderableWidget(new CategoryButton(startX, startY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.chat"), Items.WRITABLE_BOOK.getDefaultInstance(), (btn) -> {
            if(this.chatConfigScreen == null) {
                this.chatConfigScreen = ChatConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.chatConfigScreen);
        }));

        addRenderableWidget(new CategoryButton(startX + (buttonWidth + spacing), startY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.cheating"), Items.COMMAND_BLOCK.getDefaultInstance(), (btn) -> {
            if(this.cheatingConfigScreen == null) {
                this.cheatingConfigScreen = CheatingConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.cheatingConfigScreen);
        }));

        addRenderableWidget(new CategoryButton(startX + (buttonWidth + spacing) * 2, startY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.merchant"), Items.EMERALD.getDefaultInstance(), (btn) -> {
            if(this.merchantConfigScreen == null) {
                this.merchantConfigScreen = MerchantConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.merchantConfigScreen);
        }));

        addRenderableWidget(new CategoryButton(startX + (buttonWidth + spacing) * 3, startY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.optimization"), Items.BLAZE_POWDER.getDefaultInstance(), (btn) -> {
            if(this.optimizationConfigScreen == null) {
                this.optimizationConfigScreen = OptimizationConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.optimizationConfigScreen);
        }));

        // 第二行按钮
        int secondRowY = startY + buttonHeight + spacing;

        addRenderableWidget(new CategoryButton(startX, secondRowY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.screen"), Items.ITEM_FRAME.getDefaultInstance(), (btn) -> {
            if(this.screenConfigScreen == null) {
                this.screenConfigScreen = ScreenConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.screenConfigScreen);
        }));

        addRenderableWidget(new CategoryButton(startX + (buttonWidth + spacing), secondRowY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.tooltip"), Items.BOOK.getDefaultInstance(), (btn) -> {
            if(this.tooltipConfigScreen == null) {
                this.tooltipConfigScreen = TooltipConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.tooltipConfigScreen);
        }));

        addRenderableWidget(new CategoryButton(startX + (buttonWidth + spacing) * 2, secondRowY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.utility"), Items.BUNDLE.getDefaultInstance(), (btn) -> {
            if(this.utilityConfigScreen == null) {
                this.utilityConfigScreen = UtilityConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.utilityConfigScreen);
        }));

        addRenderableWidget(new CategoryButton(startX + (buttonWidth + spacing) * 3, secondRowY, buttonWidth, buttonHeight,
                Component.translatable("yacl3.config.better_client:config.category.visuality"), Items.ENDER_EYE.getDefaultInstance(), (btn) -> {
            if(this.visualityConfigScreen == null) {
                this.visualityConfigScreen = VisualityConfigScreen.generateScreen(this);
            }
            this.minecraft.setScreen(this.visualityConfigScreen);
        }));

        // Done按钮
        int doneButtonWidth = this.width - 300;
        var buttonWidget = Button.builder(CommonComponents.GUI_DONE, (btn) -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 - doneButtonWidth / 2, this.height - 30, doneButtonWidth, 20).build();

        this.addRenderableWidget(buttonWidget);
    }
}
