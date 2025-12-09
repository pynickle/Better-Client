package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IMultiLineEditBox;
import com.euphony.better_client.utils.mc.KeyUtils;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    @Shadow
    private MultiLineEditBox page;

    @Shadow
    protected abstract void pageBack();

    @Shadow
    protected abstract void pageForward();

    @Shadow
    private int currentPage;

    @Shadow
    protected abstract int getNumPages();

    @Unique
    private boolean better_client$isModified;

    @Unique
    double better_client$progress = 0;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(
            method = "appendPageToBook",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getNumPages()I",
                            shift = At.Shift.AFTER))
    private void appendPageToBookInject(CallbackInfo ci) {
        this.better_client$isModified = true;
    }

    @Override
    public void onClose() {
        if (!config.enableBookSaveConfirmation) {
            super.onClose();
            return;
        }

        if (this.better_client$isModified || ((IMultiLineEditBox) this.page).better_client$getIsModified()) {
            this.minecraft.setScreen(new ConfirmScreen(
                    (response) -> {
                        if (response) {
                            this.better_client$isModified = false;
                            ((IMultiLineEditBox) this.page).better_client$setIsModified(false);
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
        if (!config.enableBookScroll) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        double scrollDelta = verticalAmount + horizontalAmount;

        double better_client$speedFactor = 1.0;
        if (KeyUtils.hasControlDown()) better_client$speedFactor *= config.ctrlSpeedMultiplier;

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

        if (pageTurned && config.enablePageTurnSound) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
        return true;
    }
}
