package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    @Shadow
    private boolean isModified;

    @Shadow
    protected abstract void pageBack();

    @Shadow
    protected abstract void pageForward();

    @Shadow
    private int currentPage;

    @Shadow
    protected abstract int getNumPages();

    @Unique
    double better_client$progress = 0;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Override
    public void onClose() {
        if (!BetterClientConfig.HANDLER.instance().enableBookSaveConfirmation) {
            super.onClose();
            return;
        }

        if (this.isModified) {
            this.minecraft.setScreen(new ConfirmScreen(
                    (response) -> {
                        if (response) {
                            this.minecraft.setScreen(null);
                        } else {
                            this.minecraft.setScreen(this);
                        }
                    },
                    Component.translatable("message.better_client.book_save.title"),
                    Component.translatable("message.better_client.book_save.question")));
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!BetterClientConfig.HANDLER.instance().enableBookScroll)
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        double scrollDelta = verticalAmount + horizontalAmount;

        double better_client$speedFactor = 1.0;
        if (hasControlDown()) better_client$speedFactor *= BetterClientConfig.HANDLER.instance().ctrlSpeedMultiplier;

        better_client$progress += scrollDelta * better_client$speedFactor;

        boolean pageTurned = false;
        if (better_client$progress >= 1.0) {
            while (better_client$progress >= 1.0) {
                better_client$progress -= 1.0;
                this.pageBack();
                pageTurned = true;
            }
        } else if (better_client$progress < 0.0) {
            while (better_client$progress < 0.0) {
                better_client$progress += 1.0;
                if (this.currentPage < this.getNumPages() - 1) {
                    this.pageForward();
                }
                pageTurned = true;
            }
        }

        if (pageTurned && BetterClientConfig.HANDLER.instance().enablePageTurnSound) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
        return true;
    }
}
