package com.aoyama.modernfont.font.glyph;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成した文字画像を1枚の大きなテクスチャへまとめるクラス。
 * 必要になったGlyphだけを順番にAtlasへ追加する。
 */
public class GlyphAtlas {

    /**
     * Atlasの横幅。
     */
    private static final int ATLAS_WIDTH = 1024;

    /**
     * Atlasの高さ。
     */
    private static final int ATLAS_HEIGHT = 1024;

    /**
     * Glyph同士が隣接しすぎないように入れる余白。
     */
    private static final int PADDING = 2;

    /**
     * Glyph情報を生成するキャッシュ。
     */
    private final GlyphCache glyphCache;

    /**
     * Glyph描画に使うフォント。
     */
    private final Font font;

    /**
     * Atlas本体。
     * 背景透明のARGB画像。
     */
    private final BufferedImage atlasImage;

    /**
     * すでにAtlasへ追加済みのGlyph。
     */
    private final Map<Integer, Glyph> atlasGlyphs =
            new HashMap<>();

    /**
     * 次のGlyphを配置するX座標。
     */
    private int cursorX = PADDING;

    /**
     * 次のGlyphを配置するY座標。
     */
    private int cursorY = PADDING;

    /**
     * 現在の行で一番高いGlyphの高さ。
     * 次の行へ移動するときに使用する。
     */
    private int currentRowHeight = 0;

    /**
     * Atlasが更新されたか。
     * 後でMinecraft側のDynamicTextureを
     * 更新するために使用する予定。
     */
    private boolean dirty = false;

    /**
     * GlyphAtlasを生成する。
     *
     * @param glyphCache 使用するGlyphCache
     */
    public GlyphAtlas(GlyphCache glyphCache) {

        if (glyphCache == null) {
            throw new IllegalArgumentException(
                    "GlyphCache must not be null."
            );
        }

        this.glyphCache = glyphCache;
        this.font = glyphCache.getFont();

        this.atlasImage =
                new BufferedImage(
                        ATLAS_WIDTH,
                        ATLAS_HEIGHT,
                        BufferedImage.TYPE_INT_ARGB
                );

        clearAtlasImage();
    }

    /**
     * 指定された文字のGlyphを取得する。
     * すでにAtlasへ追加済みならそれを返し、
     * 未追加なら新しくAtlasへ描画する。
     *
     * @param codePoint Unicode Code Point
     * @return Atlas上の位置を持ったGlyph
     */
    public Glyph getGlyph(int codePoint) {

        Glyph cachedGlyph =
                atlasGlyphs.get(codePoint);

        if (cachedGlyph != null) {
            return cachedGlyph;
        }

        return addGlyph(codePoint);
    }

    /**
     * GlyphをAtlasへ追加する。
     */
    private Glyph addGlyph(int codePoint) {

        /*
         * まずGlyphCacheから文字情報を取得
         */
        Glyph sourceGlyph =
                glyphCache.getGlyph(codePoint);

        int glyphWidth =
                sourceGlyph.getWidth();

        int glyphHeight =
                sourceGlyph.getHeight();

        /*
         * スペースなど、
         * 実際に描画する形を持たない文字。
         *
         * Advance情報だけあればよいので、
         * Atlasには描画しない。
         */
        if (glyphWidth <= 0 || glyphHeight <= 0) {

            Glyph emptyGlyph =
                    sourceGlyph.withTexturePosition(
                            0,
                            0
                    );

            atlasGlyphs.put(
                    codePoint,
                    emptyGlyph
            );

            return emptyGlyph;
        }

        /*
         * Glyphの左右にPaddingを入れた状態で
         * Atlasへ収まるか確認する。
         *
         * 横幅を超える場合は次の行へ移動。
         */
        if (cursorX
                + glyphWidth
                + PADDING
                > ATLAS_WIDTH) {

            cursorX = PADDING;

            cursorY +=
                    currentRowHeight
                            + PADDING;

            currentRowHeight = 0;
        }

        /*
         * 縦方向にも収まらない場合。
         *
         * 現段階ではAtlas拡張をまだ実装しないので
         * 例外として停止させる。
         */
        if (cursorY
                + glyphHeight
                + PADDING
                > ATLAS_HEIGHT) {

            throw new IllegalStateException(
                    "Glyph Atlas is full."
            );
        }

        /*
         * このGlyphを配置する座標。
         */
        int textureX = cursorX;
        int textureY = cursorY;

        /*
         * 実際に文字をAtlasへ描画。
         */
        drawGlyph(
                codePoint,
                sourceGlyph,
                textureX,
                textureY
        );

        /*
         * Atlas座標を持ったGlyphを作る。
         */
        Glyph atlasGlyph =
                sourceGlyph.withTexturePosition(
                        textureX,
                        textureY
                );

        /*
         * Atlas側のキャッシュへ保存。
         */
        atlasGlyphs.put(
                codePoint,
                atlasGlyph
        );

        /*
         * 次の文字の配置位置へ進める。
         */
        cursorX +=
                glyphWidth
                        + PADDING;

        /*
         * この行で最も高いGlyphを記録。
         */
        currentRowHeight =
                Math.max(
                        currentRowHeight,
                        glyphHeight
                );

        /*
         * Atlas画像が変更された。
         */
        dirty = true;

        return atlasGlyph;
    }

    /**
     * 指定された文字をAtlas画像へ描画する。
     */
    private void drawGlyph(
            int codePoint,
            Glyph glyph,
            int textureX,
            int textureY
    ) {

        Graphics2D graphics =
                atlasImage.createGraphics();

        try {

            applyRenderingHints(graphics);

            graphics.setFont(font);

            /*
             * 白文字として描画する。
             *
             * 実際の表示色は後でMinecraft側で
             * OpenGLのcolorを使って変更できる。
             */
            graphics.setColor(Color.WHITE);

            String text =
                    new String(
                            Character.toChars(codePoint)
                    );

            GlyphVector glyphVector =
                    font.createGlyphVector(
                            graphics.getFontRenderContext(),
                            text
                    );

            /*
             * Glyph.javaのoffsetX / offsetYは
             * ベースラインから見たVisual Boundsの位置。
             *
             * textureX / textureYを
             * 実際の画像左上に合わせるため、
             * offsetの分だけ逆方向へ補正する。
             */
            float baselineX =
                    textureX
                            - glyph.getOffsetX();

            float baselineY =
                    textureY
                            - glyph.getOffsetY();

            graphics.drawGlyphVector(
                    glyphVector,
                    baselineX,
                    baselineY
            );

        } finally {

            graphics.dispose();
        }
    }

    /**
     * Java2Dの文字描画設定。
     */
    private void applyRenderingHints(
            Graphics2D graphics
    ) {

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
    }

    /**
     * Atlas画像を完全透明にする。
     */
    private void clearAtlasImage() {

        Graphics2D graphics =
                atlasImage.createGraphics();

        try {

            graphics.setComposite(
                    AlphaComposite.Clear
            );

            graphics.fillRect(
                    0,
                    0,
                    ATLAS_WIDTH,
                    ATLAS_HEIGHT
            );

        } finally {

            graphics.dispose();
        }
    }

    /**
     * Atlas画像を取得する。
     * 後でDynamicTextureを作成するときに使う。
     */
    public BufferedImage getAtlasImage() {
        return atlasImage;
    }

    /**
     * Atlasの横幅。
     */
    public int getWidth() {
        return ATLAS_WIDTH;
    }

    /**
     * Atlasの高さ。
     */
    public int getHeight() {
        return ATLAS_HEIGHT;
    }

    /**
     * Atlas画像が更新されたか。
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Atlasの更新済みフラグを解除する。
     * 後でDynamicTextureへアップロードした後に呼ぶ。
     */
    public void clearDirty() {
        dirty = false;
    }
}