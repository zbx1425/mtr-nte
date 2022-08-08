package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.block.BlockFeedbackBox;
import cn.zbx1425.mtrsteamloco.network.RequestFactory;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BookEditScreen.class)
public class BookEditScreenMixin {

    @Shadow @Final private ItemStack book;

    @Shadow @Final private Player owner;

    @Shadow @Final private List<String> pages;
    @Shadow private String title;

    @Shadow private Button doneButton;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        if (book.getTag() != null && book.getTag().contains(BlockFeedbackBox.DUMMY_BOOK_IDENTIFY_TAG)) {
            doneButton.setMessage(CommonComponents.GUI_CANCEL);
        }
    }

    @Inject(method = "saveChanges", at = @At("HEAD"), cancellable = true)
    public void saveChanges(boolean bl, CallbackInfo ci) {
        if (book.getTag() != null && book.getTag().contains(BlockFeedbackBox.DUMMY_BOOK_IDENTIFY_TAG)) {
            if (!bl) {
                ci.cancel();
                return;
            }
            final String pageDelimiter = Strings.repeat('-', 32);
            boolean empty = true;
            StringBuilder sb = new StringBuilder();
            if (!StringUtils.isEmpty(title)) {
                sb.append(title).append('\n').append(pageDelimiter);
            }
            for (int i = 0; i < pages.size(); ++i) {
                if (i > 0) sb.append('\n').append(pageDelimiter);
                String page = pages.get(i);
                sb.append(page);
                if (!StringUtils.isEmpty(page.trim())) empty = false;
            }
            if (!empty) {
                RequestFactory.buildFeedback(book.getTag().getString("counterName"), owner, sb.toString(), url -> {
                    if (Minecraft.getInstance().isRunning()) {
                        TranslatableComponent chatComponent = new TranslatableComponent("gui.mtrsteamloco.feedback_success");
                        TextComponent urlComponent = new TextComponent(url);
                        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
                        urlComponent.setStyle(urlComponent.getStyle().withClickEvent(click).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
                        chatComponent.append(urlComponent);
                        Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(chatComponent));
                    }
                }).sendAsync();
            }
            ci.cancel();
        }
    }

}
