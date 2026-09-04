package com.aoyama.modernfont.client;

import com.aoyama.modernfont.AoyamasModernFont;
import com.aoyama.modernfont.font.FontManager;
import com.aoyama.modernfont.font.renderer.AoyamaFontRenderer;
import com.aoyama.modernfont.font.renderer.ModernFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Minecraft標準FontRendererをAoyamaFontRendererへ差し替える。
 */
public final class FontRendererInstaller {

    /**
     * ロガー。
     */
    private static final Logger LOGGER =
            LogManager.getLogger(
                    AoyamasModernFont.MODID
            );

    /**
     * 二重インストールを防止する。
     */
    private static boolean installed = false;

    /**
     * インスタンス化を禁止する。
     */
    private FontRendererInstaller() {
    }

    /**
     * AoyamaFontRendererをMinecraftへインストールする。
     *
     * @return インストールした場合true
     */
    public static boolean install() {

        if (installed) {
            return false;
        }

        Minecraft minecraft =
                Minecraft.getMinecraft();

        FontManager fontManager =
                AoyamasModernFont.getFontManager();

        if (fontManager == null
                || fontManager.getLoadedFont() == null) {

            LOGGER.error(
                    "Cannot install FontRenderer: font is not loaded."
            );

            return false;
        }

        ModernFontRenderer modernFontRenderer =
                new ModernFontRenderer(
                        fontManager.getLoadedFont(),
                        minecraft.gameSettings.anaglyph
                );

        /*
         * Minecraft 1.12.2ではfontRendererが
         * publicかつ書き換え可能なので直接差し替える。
         *
         * 差し替え前のFontRendererから
         * Unicode/Bidi状態を新しいレンダラーへ引き継ぐ。
         */
        FontRenderer previousFontRenderer =
                minecraft.fontRenderer;

        AoyamaFontRenderer aoyamaFontRenderer =
                new AoyamaFontRenderer(
                        minecraft.gameSettings,
                        minecraft.getTextureManager(),
                        modernFontRenderer
                );

        aoyamaFontRenderer.setUnicodeFlag(
                previousFontRenderer.getUnicodeFlag()
        );

        aoyamaFontRenderer.setBidiFlag(
                previousFontRenderer.getBidiFlag()
        );

        minecraft.fontRenderer =
                aoyamaFontRenderer;

        IResourceManager resourceManager =
                minecraft.getResourceManager();

        if (resourceManager instanceof IReloadableResourceManager) {

            ((IReloadableResourceManager) resourceManager)
                    .registerReloadListener(
                            aoyamaFontRenderer
                    );
        }

        installed = true;

        LOGGER.info(
                "Minecraft FontRenderer replaced successfully."
        );

        return true;
    }

    /**
     * インストール済みか確認する。
     *
     * @return インストール済みならtrue
     */
    public static boolean isInstalled() {
        return installed;
    }
}