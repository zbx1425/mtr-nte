package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.block.BlockFeedbackBox;
import cn.zbx1425.mtrsteamloco.network.PacketFeedback;
import cn.zbx1425.mtrsteamloco.network.RequestFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BookEditScreen.class)
public class BookEditScreenMixin {

    @Shadow @Final private ItemStack book;

    @Shadow @Final private List<String> pages;
    @Shadow private String title;

    @Shadow private Button doneButton;

    private boolean isDummyBook() {
        return book.getTag() != null && book.getTag().contains(BlockFeedbackBox.DUMMY_BOOK_IDENTIFY_TAG);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        if (isDummyBook()) {
            doneButton.setMessage(CommonComponents.GUI_CANCEL);
        }
    }

    @Inject(method = "saveChanges", at = @At("HEAD"), cancellable = true)
    public void saveChanges(boolean bl, CallbackInfo ci) {
        if (isDummyBook()) {
            if (!bl) {
                ci.cancel();
                return;
            }
            final String pageDelimiter = Strings.repeat('-', 32);
            boolean empty = true;
            StringBuilder sb = new StringBuilder();
            if (!StringUtils.isEmpty(title)) {
                sb.append(title).append('\n').append(pageDelimiter).append('\n');
            }
            for (int i = 0; i < pages.size(); ++i) {
                if (i > 0) sb.append('\n').append(pageDelimiter).append('\n');
                String page = pages.get(i);
                sb.append(page);
                if (!StringUtils.isEmpty(page.trim())) empty = false;
            }
            if (!empty) {
                PacketFeedback.sendFeedbackC2S(book.getTag().getString("counterName"), sb.toString());
            }
            ci.cancel();
        }
    }

    private static final ResourceLocation BOOK_LOCATION = new ResourceLocation("mtrsteamloco:textures/gui/feedback_note.png");

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    public void redirectRenderSetTexture(int i, ResourceLocation resourceLocation) {
        if (isDummyBook()) {
            RenderSystem.setShaderTexture(i, BOOK_LOCATION);
        } else {
            RenderSystem.setShaderTexture(i, resourceLocation);
        }
    }

}
