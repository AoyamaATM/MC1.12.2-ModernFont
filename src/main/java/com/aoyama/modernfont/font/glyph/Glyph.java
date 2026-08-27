package com.aoyama.modernfont.font.glyph;

/**
 * 1文字分の描画情報を保持するクラス。
 * 例:
 * 'A'
 * 'あ'
 * '漢'
 * といった1文字について、
 * ・文字そのもの
 * ・テクスチャ上の位置
 * ・文字画像のサイズ
 * ・描画位置の補正
 * ・次の文字まで進む距離
 * を保持する。
 */
public class Glyph {

    /**
     * このGlyphが表す文字。
     * Unicodeを考慮してcharではなくintで保持する。
     */
    private final int codePoint;

    /**
     * Glyph Atlas上のX座標。
     */
    private final int textureX;

    /**
     * Glyph Atlas上のY座標。
     */
    private final int textureY;

    /**
     * Glyph画像の横幅。
     */
    private final int width;

    /**
     * Glyph画像の高さ。
     */
    private final int height;

    /**
     * 実際に画面へ描画するときのX方向補正。
     */
    private final float offsetX;

    /**
     * 実際に画面へ描画するときのY方向補正。
     */
    private final float offsetY;

    /**
     * この文字を描画したあと、
     * 次の文字へ何px進むか。
     * 文字そのものの画像幅とは必ずしも一致しない。
     */
    private final float advance;

    /**
     * Glyphを生成する。
     */
    public Glyph(
            int codePoint,
            int textureX,
            int textureY,
            int width,
            int height,
            float offsetX,
            float offsetY,
            float advance
    ) {

        this.codePoint = codePoint;
        this.textureX = textureX;
        this.textureY = textureY;
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.advance = advance;
    }

    public int getTextureX() {
        return textureX;
    }

    public int getTextureY() {
        return textureY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getAdvance() {
        return advance;
    }

    /**
     * Glyph Atlas上の位置を設定した新しいGlyphを返す.
     * 元のGlyphは変更せず、
     * textureX / textureYだけ変更したコピーを生成する。
     *
     * @param textureX Atlas上のX座標
     * @param textureY Atlas上のY座標
     * @return Atlas座標を持った新しいGlyph
     */
    public Glyph withTexturePosition(
            int textureX,
            int textureY
    ) {

        return new Glyph(
                codePoint,
                textureX,
                textureY,
                width,
                height,
                offsetX,
                offsetY,
                advance
        );
    }
}