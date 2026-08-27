package com.aoyama.modernfont.font.renderer;

import com.aoyama.modernfont.font.glyph.Glyph;
import com.aoyama.modernfont.font.glyph.GlyphAtlas;
import com.aoyama.modernfont.font.glyph.GlyphCache;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * Aoyama's Modern Font の文字描画クラス。
 * Minecraft 1.12.2のカラー、太字、斜体、下線、取り消し線、
 * 影、下付き数字、リセットに対応する。
 */
public class ModernFontRenderer {

    /**
     * Glyph生成時のフォントサイズ。
     */
    private static final float GLYPH_SOURCE_SIZE = 36.0F;

    /**
     * Minecraft上での基準表示サイズ。
     */
    private static final float DISPLAY_FONT_SIZE = 9.0F;

    /**
     * 高解像度GlyphをMinecraft上へ縮小する倍率。
     */
    private static final float DISPLAY_SCALE =
            DISPLAY_FONT_SIZE / GLYPH_SOURCE_SIZE;

    /**
     * 擬似Boldの横方向ずらし量。
     */
    private static final float BOLD_OFFSET = 0.5F;

    /**
     * 擬似Italicの上辺ずらし量。
     */
    private static final float ITALIC_OFFSET = 1.5F;

    /**
     * 影の描画位置をずらす量。
     */
    private static final float SHADOW_OFFSET = 1.0F;

    /**
     * 装飾線の最低表示太さ。
     */
    private static final float MIN_DECORATION_THICKNESS = 0.75F;

    /**
     * 擬似下付き数字の表示倍率。
     */
    private static final float SUBSCRIPT_SCALE = 0.70F;

    /**
     * 擬似下付き数字をベースライン方向へ下げる量。
     */
    private static final float SUBSCRIPT_Y_OFFSET = 2.0F;

    /**
     * Unicode下付き数字0のCode Point。
     */
    private static final int SUBSCRIPT_ZERO = 0x2080;

    /**
     * Unicode下付き数字9のCode Point。
     */
    private static final int SUBSCRIPT_NINE = 0x2089;

    /**
     * Minecraft標準の16色。
     */
    private static final int[] COLOR_CODES = {
            0x000000,
            0x0000AA,
            0x00AA00,
            0x00AAAA,
            0xAA0000,
            0xAA00AA,
            0xFFAA00,
            0xAAAAAA,
            0x555555,
            0x5555FF,
            0x55FF55,
            0x55FFFF,
            0xFF5555,
            0xFF55FF,
            0xFFFF55,
            0xFFFFFF
    };

    /**
     * Glyph情報のキャッシュ。
     */
    private final GlyphCache glyphCache;

    /**
     * Glyphを格納するAtlas。
     */
    private final GlyphAtlas glyphAtlas;

    /**
     * Minecraft側のAtlasテクスチャ。
     */
    private final GlyphAtlasTexture atlasTexture;

    /**
     * 行上端からベースラインまでの距離。
     */
    private final float lineAscent;

    /**
     * ベースラインから下線までの距離。
     */
    private final float underlineOffset;

    /**
     * 下線の太さ。
     */
    private final float underlineThickness;

    /**
     * ベースラインから取り消し線までの距離。
     */
    private final float strikethroughOffset;

    /**
     * 取り消し線の太さ。
     */
    private final float strikethroughThickness;

    /**
     * ModernFontRendererを生成する。
     *
     * @param loadedFont FontManagerが読み込んだフォント
     */
    public ModernFontRenderer(
            Font loadedFont
    ) {

        if (loadedFont == null) {
            throw new IllegalArgumentException(
                    "Font must not be null."
            );
        }

        Font glyphFont =
                loadedFont.deriveFont(
                        Font.PLAIN,
                        GLYPH_SOURCE_SIZE
                );

        FontRenderContext fontRenderContext =
                new FontRenderContext(
                        null,
                        true,
                        true
                );

        LineMetrics lineMetrics =
                glyphFont.getLineMetrics(
                        "Ag日本語",
                        fontRenderContext
                );

        this.lineAscent =
                lineMetrics.getAscent();

        this.underlineOffset =
                lineMetrics.getUnderlineOffset();

        this.underlineThickness =
                lineMetrics.getUnderlineThickness();

        this.strikethroughOffset =
                lineMetrics.getStrikethroughOffset();

        this.strikethroughThickness =
                lineMetrics.getStrikethroughThickness();

        this.glyphCache =
                new GlyphCache(
                        glyphFont
                );

        this.glyphAtlas =
                new GlyphAtlas(
                        glyphCache
                );

        this.atlasTexture =
                new GlyphAtlasTexture(
                        glyphAtlas
                );
    }

    /**
     * 指定色で文字列を描画する。
     *
     * @param text 文字列
     * @param x X座標
     * @param y Y座標
     * @param color 基本色
     */
    public void drawString(
            String text,
            float x,
            float y,
            int color
    ) {

        drawStringInternal(
                text,
                x,
                y,
                color,
                false
        );
    }

    /**
     * 指定色の影付き文字列を描画する。
     *
     * @param text 文字列
     * @param x X座標
     * @param y Y座標
     * @param color 基本色
     */
    public void drawStringWithShadow(
            String text,
            float x,
            float y,
            int color
    ) {

        drawStringInternal(
                text,
                x + SHADOW_OFFSET,
                y + SHADOW_OFFSET,
                color,
                true
        );

        drawStringInternal(
                text,
                x,
                y,
                color,
                false
        );
    }

    /**
     * 実際の文字列描画処理を行う。
     *
     * @param text 文字列
     * @param x X座標
     * @param y Y座標
     * @param color 基本色
     * @param shadow 影として描画するか
     */
    private void drawStringInternal(
            String text,
            float x,
            float y,
            int color,
            boolean shadow
    ) {

        if (text == null || text.isEmpty()) {
            return;
        }

        registerRequiredGlyphs(
                text
        );

        atlasTexture.bindTexture();

        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        int baseColor =
                normalizeColor(
                        color
                );

        int currentColor =
                shadow
                        ? darkenForShadow(baseColor)
                        : baseColor;

        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;

        applyColor(
                currentColor
        );

        float baselineY =
                y
                        + lineAscent
                        * DISPLAY_SCALE;

        float cursorX = x;

        int[] codePoints =
                text.codePoints().toArray();

        for (int i = 0; i < codePoints.length; i++) {

            int codePoint =
                    codePoints[i];

            if (codePoint == '§'
                    && i + 1 < codePoints.length) {

                char formatCode =
                        Character.toLowerCase(
                                (char) codePoints[++i]
                        );

                int colorIndex =
                        getColorIndex(
                                formatCode
                        );

                if (colorIndex >= 0) {

                    int alpha =
                            (baseColor >>> 24)
                                    & 0xFF;

                    int formattedColor =
                            (alpha << 24)
                                    | COLOR_CODES[colorIndex];

                    currentColor =
                            shadow
                                    ? darkenForShadow(
                                    formattedColor
                            )
                                    : formattedColor;

                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;

                    applyColor(
                            currentColor
                    );

                    continue;
                }

                if (formatCode == 'l') {
                    bold = true;
                    continue;
                }

                if (formatCode == 'o') {
                    italic = true;
                    continue;
                }

                if (formatCode == 'n') {
                    underline = true;
                    continue;
                }

                if (formatCode == 'm') {
                    strikethrough = true;
                    continue;
                }

                if (formatCode == 'r') {

                    currentColor =
                            shadow
                                    ? darkenForShadow(
                                    baseColor
                            )
                                    : baseColor;

                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;

                    applyColor(
                            currentColor
                    );

                    continue;
                }

                /*
                 * §kおよび未対応の書式コードは
                 * 現時点では読み飛ばす。
                 */
                continue;
            }

            boolean syntheticSubscript =
                    shouldUseSyntheticSubscript(
                            codePoint
                    );

            int glyphCodePoint =
                    syntheticSubscript
                            ? toNormalDigit(codePoint)
                            : codePoint;

            float glyphScale =
                    syntheticSubscript
                            ? SUBSCRIPT_SCALE
                            : 1.0F;

            Glyph glyph =
                    glyphAtlas.getGlyph(
                            glyphCodePoint
                    );

            float glyphAdvance =
                    glyph.getAdvance()
                            * DISPLAY_SCALE
                            * glyphScale;

            float boldOffset =
                    bold
                            ? BOLD_OFFSET * glyphScale
                            : 0.0F;

            if (bold) {
                glyphAdvance += boldOffset;
            }

            if (glyph.getWidth() > 0
                    && glyph.getHeight() > 0) {

                float drawX =
                        cursorX
                                + glyph.getOffsetX()
                                * DISPLAY_SCALE
                                * glyphScale;

                float drawY =
                        baselineY
                                + glyph.getOffsetY()
                                * DISPLAY_SCALE
                                * glyphScale;

                if (syntheticSubscript) {
                    drawY += SUBSCRIPT_Y_OFFSET;
                }

                float drawWidth =
                        glyph.getWidth()
                                * DISPLAY_SCALE
                                * glyphScale;

                float drawHeight =
                        glyph.getHeight()
                                * DISPLAY_SCALE
                                * glyphScale;

                float italicOffset =
                        italic
                                ? ITALIC_OFFSET * glyphScale
                                : 0.0F;

                drawGlyph(
                        glyph,
                        drawX,
                        drawY,
                        drawWidth,
                        drawHeight,
                        italicOffset
                );

                if (bold) {

                    drawGlyph(
                            glyph,
                            drawX + boldOffset,
                            drawY,
                            drawWidth,
                            drawHeight,
                            italicOffset
                    );
                }
            }

            if (strikethrough) {

                float lineY =
                        baselineY
                                + strikethroughOffset
                                * DISPLAY_SCALE;

                float thickness =
                        Math.max(
                                MIN_DECORATION_THICKNESS,
                                strikethroughThickness
                                        * DISPLAY_SCALE
                        );

                drawDecorationLine(
                        cursorX,
                        cursorX + glyphAdvance,
                        lineY,
                        thickness
                );
            }

            if (underline) {

                float lineY =
                        baselineY
                                + underlineOffset
                                * DISPLAY_SCALE;

                float thickness =
                        Math.max(
                                MIN_DECORATION_THICKNESS,
                                underlineThickness
                                        * DISPLAY_SCALE
                        );

                drawDecorationLine(
                        cursorX,
                        cursorX + glyphAdvance,
                        lineY,
                        thickness
                );
            }

            cursorX += glyphAdvance;
        }

        GlStateManager.disableBlend();

        GlStateManager.color(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

    /**
     * 影用にRGBを4分の1程度まで暗くする。
     *
     * @param color ARGB
     * @return 影用ARGB
     */
    private int darkenForShadow(
            int color
    ) {

        int alpha =
                (color >>> 24)
                        & 0xFF;

        int red =
                ((color >>> 16)
                        & 0xFF) / 4;

        int green =
                ((color >>> 8)
                        & 0xFF) / 4;

        int blue =
                (color
                        & 0xFF) / 4;

        return (alpha << 24)
                | (red << 16)
                | (green << 8)
                | blue;
    }

    /**
     * 描画に必要なGlyphをAtlasへ登録する。
     *
     * @param text 対象文字列
     */
    private void registerRequiredGlyphs(
            String text
    ) {

        int[] codePoints =
                text.codePoints().toArray();

        for (int i = 0; i < codePoints.length; i++) {

            int codePoint =
                    codePoints[i];

            if (codePoint == '§'
                    && i + 1 < codePoints.length) {

                i++;

                continue;
            }

            int glyphCodePoint =
                    shouldUseSyntheticSubscript(
                            codePoint
                    )
                            ? toNormalDigit(codePoint)
                            : codePoint;

            glyphAtlas.getGlyph(
                    glyphCodePoint
            );
        }
    }

    /**
     * Minecraftのカラーコードを色配列のIndexへ変換する。
     *
     * @param formatCode 書式コード
     * @return 0～15、カラーコードでなければ-1
     */
    private int getColorIndex(
            char formatCode
    ) {

        if (formatCode >= '0'
                && formatCode <= '9') {

            return formatCode - '0';
        }

        if (formatCode >= 'a'
                && formatCode <= 'f') {

            return formatCode - 'a' + 10;
        }

        return -1;
    }

    /**
     * RGB形式の色へ完全不透明Alphaを補う。
     *
     * @param color RGBまたはARGB
     * @return ARGB
     */
    private int normalizeColor(
            int color
    ) {

        if ((color & 0xFF000000) == 0) {

            return 0xFF000000
                    | color;
        }

        return color;
    }

    /**
     * ARGB色をOpenGLへ設定する。
     *
     * @param color ARGB
     */
    private void applyColor(
            int color
    ) {

        int alpha =
                (color >>> 24)
                        & 0xFF;

        int red =
                (color >>> 16)
                        & 0xFF;

        int green =
                (color >>> 8)
                        & 0xFF;

        int blue =
                color
                        & 0xFF;

        GlStateManager.color(
                red / 255.0F,
                green / 255.0F,
                blue / 255.0F,
                alpha / 255.0F
        );
    }

    /**
     * Glyph Atlas上の1文字を描画する。
     *
     * @param glyph Glyph
     * @param x X座標
     * @param y Y座標
     * @param width 表示幅
     * @param height 表示高さ
     * @param italicOffset 斜体用の上辺X補正
     */
    private void drawGlyph(
            Glyph glyph,
            float x,
            float y,
            float width,
            float height,
            float italicOffset
    ) {

        float u0 =
                glyph.getTextureX()
                        / (float) glyphAtlas.getWidth();

        float v0 =
                glyph.getTextureY()
                        / (float) glyphAtlas.getHeight();

        float u1 =
                (glyph.getTextureX()
                        + glyph.getWidth())
                        / (float) glyphAtlas.getWidth();

        float v1 =
                (glyph.getTextureY()
                        + glyph.getHeight())
                        / (float) glyphAtlas.getHeight();

        float x1 =
                x + width;

        float y1 =
                y + height;

        Tessellator tessellator =
                Tessellator.getInstance();

        BufferBuilder buffer =
                tessellator.getBuffer();

        buffer.begin(
                GL11.GL_QUADS,
                DefaultVertexFormats.POSITION_TEX
        );

        buffer.pos(
                x,
                y1,
                0.0D
        ).tex(
                u0,
                v1
        ).endVertex();

        buffer.pos(
                x1,
                y1,
                0.0D
        ).tex(
                u1,
                v1
        ).endVertex();

        buffer.pos(
                x1 + italicOffset,
                y,
                0.0D
        ).tex(
                u1,
                v0
        ).endVertex();

        buffer.pos(
                x + italicOffset,
                y,
                0.0D
        ).tex(
                u0,
                v0
        ).endVertex();

        tessellator.draw();
    }

    /**
     * 下線または取り消し線として細い矩形を描画する。
     *
     * @param startX 開始X座標
     * @param endX 終了X座標
     * @param y Y座標
     * @param thickness 線の太さ
     */
    private void drawDecorationLine(
            float startX,
            float endX,
            float y,
            float thickness
    ) {

        GlStateManager.disableTexture2D();

        Tessellator tessellator =
                Tessellator.getInstance();

        BufferBuilder buffer =
                tessellator.getBuffer();

        buffer.begin(
                GL11.GL_QUADS,
                DefaultVertexFormats.POSITION
        );

        buffer.pos(
                startX,
                y + thickness,
                0.0D
        ).endVertex();

        buffer.pos(
                endX,
                y + thickness,
                0.0D
        ).endVertex();

        buffer.pos(
                endX,
                y,
                0.0D
        ).endVertex();

        buffer.pos(
                startX,
                y,
                0.0D
        ).endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
    }

    /**
     * 1つのUnicode Code Pointの表示幅を取得する。
     * 擬似下付き数字および太字による追加幅も反映する。
     *
     * @param codePoint Unicode Code Point
     * @param bold 太字として計測するか
     * @return 表示幅
     */
    public float getCodePointWidth(
            int codePoint,
            boolean bold
    ) {

        boolean syntheticSubscript =
                shouldUseSyntheticSubscript(
                        codePoint
                );

        int glyphCodePoint =
                syntheticSubscript
                        ? toNormalDigit(codePoint)
                        : codePoint;

        float glyphScale =
                syntheticSubscript
                        ? SUBSCRIPT_SCALE
                        : 1.0F;

        Glyph glyph =
                glyphCache.getGlyph(
                        glyphCodePoint
                );

        float width =
                glyph.getAdvance()
                        * DISPLAY_SCALE
                        * glyphScale;

        if (bold && width > 0.0F) {

            width +=
                    BOLD_OFFSET
                            * glyphScale;
        }

        return width;
    }

    /**
     * 文字列の横幅を取得する。
     *
     * @param text 対象文字列
     * @return 表示幅
     */
    public float getStringWidth(
            String text
    ) {

        if (text == null || text.isEmpty()) {
            return 0.0F;
        }

        float width = 0.0F;

        boolean bold = false;

        int[] codePoints =
                text.codePoints().toArray();

        for (int i = 0; i < codePoints.length; i++) {

            int codePoint =
                    codePoints[i];

            if (codePoint == '§'
                    && i + 1 < codePoints.length) {

                char formatCode =
                        Character.toLowerCase(
                                (char) codePoints[++i]
                        );

                /*
                 * 色コードはMinecraftと同じく
                 * 書式状態をリセットする。
                 */
                if (getColorIndex(formatCode) >= 0) {

                    bold = false;

                    continue;
                }

                if (formatCode == 'l') {

                    bold = true;

                    continue;
                }

                if (formatCode == 'r') {

                    bold = false;

                    continue;
                }

                continue;
            }

            width +=
                    getCodePointWidth(
                            codePoint,
                            bold
                    );
        }

        return width;
    }

    /**
     * 現在のフォントで指定されたUnicode Code Pointを表示できるか確認する。
     * 下付き数字を直接表示できない場合は通常数字による擬似表示も判定する。
     *
     * @param codePoint Unicode Code Point
     * @return 表示可能な場合true
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canDisplay(
            int codePoint
    ) {

        if (!Character.isValidCodePoint(
                codePoint
        )) {

            return false;
        }

        Font font =
                glyphCache.getFont();

        /*
         * フォント自体が対象文字を持っている場合。
         */
        if (font.canDisplay(
                codePoint
        )) {

            return true;
        }

        /*
         * 下付き数字そのものが無い場合でも、
         * 通常数字を使った擬似下付き表示が可能なら
         * 表示可能として扱う。
         */
        if (isSubscriptDigit(
                codePoint
        )) {

            return font.canDisplay(
                    toNormalDigit(
                            codePoint
                    )
            );
        }

        return false;
    }

    /**
     * Unicode下付き数字か確認する。
     *
     * @param codePoint Unicode Code Point
     * @return U+2080～U+2089ならtrue
     */
    private boolean isSubscriptDigit(
            int codePoint
    ) {

        return codePoint >= SUBSCRIPT_ZERO
                && codePoint <= SUBSCRIPT_NINE;
    }

    /**
     * 下付き数字を通常のASCII数字へ変換する。
     *
     * @param codePoint 下付き数字のCode Point
     * @return ASCII数字のCode Point
     */
    private int toNormalDigit(
            int codePoint
    ) {

        return '0'
                + (codePoint - SUBSCRIPT_ZERO);
    }

    /**
     * 指定された下付き数字を擬似描画する必要があるか確認する。
     *
     * @param codePoint Unicode Code Point
     * @return 通常数字を縮小して代用する場合true
     */
    private boolean shouldUseSyntheticSubscript(
            int codePoint
    ) {

        if (!isSubscriptDigit(
                codePoint
        )) {

            return false;
        }

        Font font =
                glyphCache.getFont();

        if (font.canDisplay(
                codePoint
        )) {

            return false;
        }

        return font.canDisplay(
                toNormalDigit(
                        codePoint
                )
        );
    }
}