package com.aoyama.modernfont.font.glyph;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成済みのGlyph情報を保存・再利用するクラス。
 * 同じ文字を何度も描画するたびに
 * フォント情報を再計算するのを防ぐ。
 */
public class GlyphCache {

    /**
     * Glyph生成に使用するフォント。
     */
    private final Font font;

    /**
     * Java2Dでフォントのサイズや位置を計算するための設定。
     * AntiAlias = true
     * Fractional Metrics = true
     */
    private final FontRenderContext fontRenderContext;

    /**
     * codePointごとのGlyphキャッシュ。
     */
    private final Map<Integer, Glyph> glyphCache =
            new HashMap<>();

    /**
     * GlyphCacheを生成する。
     *
     * @param font 使用するフォント
     */
    public GlyphCache(Font font) {

        if (font == null) {
            throw new IllegalArgumentException(
                    "Font must not be null."
            );
        }

        this.font = font;

        this.fontRenderContext =
                new FontRenderContext(
                        null,
                        true,
                        true
                );
    }

    /**
     * 指定されたUnicode Code PointのGlyphを取得する。
     * すでに生成済みならキャッシュから返し、
     * 未生成なら新しく生成する。
     *
     * @param codePoint Unicode Code Point
     * @return Glyph
     */
    public Glyph getGlyph(int codePoint) {

        Glyph cachedGlyph =
                glyphCache.get(codePoint);

        if (cachedGlyph != null) {
            return cachedGlyph;
        }

        Glyph newGlyph =
                createGlyph(codePoint);

        glyphCache.put(
                codePoint,
                newGlyph
        );

        return newGlyph;
    }

    /**
     * 実際にフォントからGlyph情報を生成する。
     * 現段階ではまだGlyph Atlasへ配置しないため、
     * textureX / textureY は0としている。
     */
    private Glyph createGlyph(int codePoint) {

        String text =
                new String(
                        Character.toChars(codePoint)
                );

        /*
         * Fontから1文字分のGlyph情報を生成
         */
        GlyphVector glyphVector =
                font.createGlyphVector(
                        fontRenderContext,
                        text
                );

        /*
         * 通常は1文字なのでGlyph index 0を使用
         */
        java.awt.font.GlyphMetrics awtMetrics =
                glyphVector.getGlyphMetrics(0);

        /*
         * 実際に文字が描画される範囲
         */
        Rectangle pixelBounds =
                glyphVector.getGlyphPixelBounds(
                        0,
                        fontRenderContext,
                        0.0F,
                        0.0F
                );

        int width =
                Math.max(
                        0,
                        pixelBounds.width
                );

        int height =
                Math.max(
                        0,
                        pixelBounds.height
                );

        float offsetX =
                pixelBounds.x;

        float offsetY =
                pixelBounds.y;
        /*
         * 次の文字まで進む距離。
         *
         * widthではなくフォント本来のAdvanceを使う。
         */
        float advance =
                awtMetrics.getAdvance();

        /*
         * Atlasへの配置は後で行うため
         * textureX / textureY は現段階では0。
         */
        return new Glyph(
                codePoint,
                0,
                0,
                width,
                height,
                offsetX,
                offsetY,
                advance
        );
    }

    /**
     * 使用しているフォントを取得する。
     */
    public Font getFont() {
        return font;
    }
}