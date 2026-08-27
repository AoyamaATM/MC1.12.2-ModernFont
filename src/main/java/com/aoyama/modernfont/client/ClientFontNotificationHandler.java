package com.aoyama.modernfont.client;

import com.aoyama.modernfont.AoyamasModernFont;
import com.aoyama.modernfont.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * カスタムフォントに問題があった場合、
 * 最初のワールドログイン時にプレイヤーへ通知する。
 */
public class ClientFontNotificationHandler {

    /**
     * このMinecraft起動中に通知済みか。
     */
    private boolean notificationSent = false;

    /**
     * クライアントプレイヤーがワールドへ参加した際に
     * カスタムフォントエラーを通知する。
     *
     * @param event EntityJoinWorldEvent
     */
    @SubscribeEvent
    public void onEntityJoinWorld(
            EntityJoinWorldEvent event
    ) {

        /*
         * サーバー側では処理しない。
         */
        if (!event.getWorld().isRemote) {
            return;
        }

        /*
         * クライアント自身のプレイヤー以外では処理しない。
         */
        if (!(event.getEntity() instanceof EntityPlayerSP)) {
            return;
        }

        /*
         * 1回のMinecraft起動につき通知は1回だけ。
         */
        if (notificationSent) {
            return;
        }

        FontManager fontManager =
                AoyamasModernFont.getFontManager();

        if (fontManager == null) {
            return;
        }

        /*
         * カスタムフォントに問題がなければ通知不要。
         */
        if (!fontManager.hasCustomFontError()) {

            notificationSent = true;

            return;
        }

        notificationSent = true;

        EntityPlayerSP player =
                (EntityPlayerSP) event.getEntity();

        /*
         * ワールド参加処理中に直接チャットへ追加せず、
         * Minecraftの次のタスクとして安全に実行する。
         */
        Minecraft.getMinecraft().addScheduledTask(
                () -> sendNotification(
                        player
                )
        );
    }

    /**
     * カスタムフォントエラーをチャットへ表示する。
     *
     * @param player 通知先プレイヤー
     */
    private void sendNotification(
            EntityPlayerSP player
    ) {

        if (player == null) {
            return;
        }

        FontManager fontManager =
                AoyamasModernFont.getFontManager();

        if (fontManager == null) {
            return;
        }

        FontManager.CustomFontErrorType errorType =
                fontManager.getCustomFontErrorType();

        switch (errorType) {

            case LOAD_FAILED:

                player.sendMessage(
                        createMessage(
                                "aoyamasmodernfont.chat.custom_font_load_failed",
                                TextFormatting.RED
                        )
                );

                sendFallbackMessage(player);

                break;

            case MULTIPLE_FONTS:

                player.sendMessage(
                        createMessage(
                                "aoyamasmodernfont.chat.multiple_fonts",
                                TextFormatting.RED
                        )
                );

                player.sendMessage(
                        createMessage(
                                "aoyamasmodernfont.chat.multiple_fonts_instruction",
                                TextFormatting.YELLOW
                        )
                );

                sendFallbackMessage(player);

                break;

            case DIRECTORY_ERROR:

                player.sendMessage(
                        createMessage(
                                "aoyamasmodernfont.chat.font_directory_error",
                                TextFormatting.RED
                        )
                );

                sendFallbackMessage(player);

                break;

            case NONE:
            default:
                break;
        }
    }

    /**
     * 内蔵フォントへのフォールバックを通知する。
     *
     * @param player 通知先プレイヤー
     */
    private void sendFallbackMessage(
            EntityPlayerSP player
    ) {

        player.sendMessage(
                createMessage(
                        "aoyamasmodernfont.chat.custom_font_fallback",
                        TextFormatting.YELLOW
                )
        );
    }

    /**
     * MOD名付きチャットメッセージを生成する。
     *
     * @param translationKey 翻訳キー
     * @param messageColor 本文色
     * @return チャットコンポーネント
     */
    private ITextComponent createMessage(
            String translationKey,
            TextFormatting messageColor
    ) {

        TextComponentString prefix =
                new TextComponentString(
                        "[Aoyama's Modern Font] "
                );

        prefix.getStyle().setColor(
                TextFormatting.GOLD
        );

        TextComponentString message =
                new TextComponentString(
                        I18n.format(
                                translationKey
                        )
                );

        message.getStyle().setColor(
                messageColor
        );

        prefix.appendSibling(
                message
        );

        return prefix;
    }
}