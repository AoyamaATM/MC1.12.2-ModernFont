package com.aoyama.modernfont.font.renderer;

import com.aoyama.modernfont.font.glyph.GlyphAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;

/**
 * GlyphAtlasのBufferedImageを
 * Minecraftで使用できるDynamicTextureへ変換・同期するクラス。
 */
public class GlyphAtlasTexture {

    /**
     * ロガー。
     */
    private static final Logger LOGGER =
            LogManager.getLogger(
                    "aoyamasmodernfont"
            );

    /**
     * 元になるGlyphAtlas。
     */
    private final GlyphAtlas glyphAtlas;

    /**
     * Minecraft側の動的テクスチャ。
     */
    private DynamicTexture dynamicTexture;

    /**
     * MinecraftのTextureManagerへ登録した場所。
     */
    private ResourceLocation textureLocation;

    /**
     * GlyphAtlasTextureを生成する。
     * この時点ではまだGPUテクスチャは生成しない。
     *
     * @param glyphAtlas 使用するGlyphAtlas
     */
    public GlyphAtlasTexture(
            GlyphAtlas glyphAtlas
    ) {

        if (glyphAtlas == null) {
            throw new IllegalArgumentException(
                    "GlyphAtlas must not be null."
            );
        }

        this.glyphAtlas = glyphAtlas;
    }

    /**
     * GlyphAtlasの内容をMinecraft側の
     * DynamicTextureへ同期する。
     * 初回：
     * DynamicTextureを新規作成
     * 2回目以降：
     * Atlasが更新されている場合のみ
     * GPUテクスチャを更新
     */
    public void updateTexture() {

        /*
         * 初回の場合
         */
        if (dynamicTexture == null) {

            createTexture();

            return;
        }

        /*
         * Atlasに変更がなければ何もしない。
         */
        if (!glyphAtlas.isDirty()) {
            return;
        }

        /*
         * BufferedImageの内容を
         * DynamicTextureへコピーする。
         */
        copyAtlasToTexture();

        /*
         * GPUへ反映する。
         */
        dynamicTexture.updateDynamicTexture();

        /*
         * 同期済みなのでDirtyを解除。
         */
        glyphAtlas.clearDirty();
    }

    /**
     * 初回のDynamicTexture生成。
     */
    private void createTexture() {

        BufferedImage atlasImage =
                glyphAtlas.getAtlasImage();

        /*
         * GlyphAtlas画像から
         * DynamicTextureを生成する。
         */
        dynamicTexture =
                new DynamicTexture(
                        atlasImage
                );

        /*
         * フォントを滑らかに縮小表示するため、
         * Linear Filteringを使用する。
         */
        dynamicTexture.setBlurMipmap(
                true,
                false
        );

        Minecraft minecraft =
                Minecraft.getMinecraft();

        /*
         * MinecraftのTextureManagerへ登録。
         */
        textureLocation =
                minecraft
                        .getTextureManager()
                        .getDynamicTextureLocation(
                                "aoyamasmodernfont_glyph_atlas",
                                dynamicTexture
                        );

        /*
         * DynamicTexture生成時点で
         * Atlasの現在内容は同期済み。
         */
        glyphAtlas.clearDirty();

        LOGGER.debug(
                "Glyph Atlas texture created successfully."
        );

        LOGGER.debug(
                "Atlas texture size: {}x{}",
                glyphAtlas.getWidth(),
                glyphAtlas.getHeight()
        );
    }

    /**
     * GlyphAtlasのBufferedImageを
     * 既存DynamicTextureへコピーする。
     */
    private void copyAtlasToTexture() {

        BufferedImage atlasImage =
                glyphAtlas.getAtlasImage();

        int width =
                glyphAtlas.getWidth();

        int height =
                glyphAtlas.getHeight();

        /*
         * BufferedImageからARGBピクセルを取得。
         */
        int[] imagePixels =
                atlasImage.getRGB(
                        0,
                        0,
                        width,
                        height,
                        null,
                        0,
                        width
                );

        /*
         * DynamicTexture内部のピクセル配列。
         */
        int[] texturePixels =
                dynamicTexture.getTextureData();

        /*
         * 念のためサイズ確認。
         */
        if (imagePixels.length
                != texturePixels.length) {

            throw new IllegalStateException(
                    "Glyph Atlas texture size mismatch."
            );
        }

        /*
         * Atlas画像
         * ↓
         * DynamicTexture
         */
        System.arraycopy(
                imagePixels,
                0,
                texturePixels,
                0,
                imagePixels.length
        );
    }

    /**
     * このAtlasテクスチャを
     * OpenGLの現在のテクスチャとして設定する。
     */
    public void bindTexture() {

        updateTexture();

        if (textureLocation == null) {
            return;
        }

        Minecraft
                .getMinecraft()
                .getTextureManager()
                .bindTexture(
                        textureLocation
                );
    }
}