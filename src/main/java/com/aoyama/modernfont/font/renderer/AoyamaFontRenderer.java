package com.aoyama.modernfont.font.renderer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft標準FontRendererとModernFontRendererを接続する互換アダプター。
 * 通常はModernFontRendererを使用し、現在のフォントで表示できない文字を含む場合は
 * Minecraft標準FontRendererへ自動的にフォールバックする。
 */
@SideOnly(Side.CLIENT)
public class AoyamaFontRenderer extends FontRenderer {

    /**
     * Vanilla FontRendererで使用するMinecraft標準フォントテクスチャ。
     */
    private static final ResourceLocation VANILLA_FONT_TEXTURE =
            new ResourceLocation(
                    "textures/font/ascii.png"
            );

    /**
     * Minecraft標準FontRendererとのY座標互換補正。
     */
    private static final float VANILLA_Y_OFFSET = -3.5F;

    /**
     * 実際のModern Font描画を担当するレンダラー。
     */
    private final ModernFontRenderer modernFontRenderer;

    /**
     * Vanilla FontRenderer内部の幅計算を強制する深度。
     * super側のメソッドからgetCharWidth()などが再度呼ばれた場合に、
     * Modern側の幅が混入することを防ぐ。
     */
    private int vanillaMetricsDepth = 0;

    /**
     * AoyamaFontRendererを生成する。
     *
     * @param gameSettings Minecraftのゲーム設定
     * @param textureManager MinecraftのTextureManager
     * @param modernFontRenderer Modern Font描画レンダラー
     */
    public AoyamaFontRenderer(
            GameSettings gameSettings,
            TextureManager textureManager,
            ModernFontRenderer modernFontRenderer
    ) {

        super(
                gameSettings,
                VANILLA_FONT_TEXTURE,
                textureManager,
                false
        );

        if (modernFontRenderer == null) {
            throw new IllegalArgumentException(
                    "ModernFontRenderer must not be null."
            );
        }

        this.modernFontRenderer =
                modernFontRenderer;

        /*
         * Minecraft 1.12.2標準と同じ基準行高。
         */
        this.FONT_HEIGHT = 9;
    }

    /**
     * Minecraft標準形式の通常文字列描画。
     *
     * @param text 文字列
     * @param x X座標
     * @param y Y座標
     * @param color 文字色
     * @return 描画終了位置
     */
    @Override
    public int drawString(
            @Nonnull String text,
            int x,
            int y,
            int color
    ) {

        if (requiresVanillaFallback(text)) {

            beginVanillaMetrics();

            try {

                return super.drawString(
                        text,
                        (float) x,
                        (float) y,
                        color,
                        false
                );

            } finally {

                endVanillaMetrics();
            }
        }

        modernFontRenderer.drawString(
                text,
                x,
                y + VANILLA_Y_OFFSET,
                color
        );

        return getModernEndPosition(
                text,
                x
        );
    }

    /**
     * Minecraft標準形式の文字列描画。
     *
     * @param text 文字列
     * @param x X座標
     * @param y Y座標
     * @param color 文字色
     * @param dropShadow 影を付けるか
     * @return 描画終了位置
     */
    @Override
    public int drawString(
            @Nonnull String text,
            float x,
            float y,
            int color,
            boolean dropShadow
    ) {

        if (requiresVanillaFallback(text)) {

            beginVanillaMetrics();

            try {

                return super.drawString(
                        text,
                        x,
                        y,
                        color,
                        dropShadow
                );

            } finally {

                endVanillaMetrics();
            }
        }

        float adjustedY =
                y + VANILLA_Y_OFFSET;

        if (dropShadow) {

            modernFontRenderer.drawStringWithShadow(
                    text,
                    x,
                    adjustedY,
                    color
            );

        } else {

            modernFontRenderer.drawString(
                    text,
                    x,
                    adjustedY,
                    color
            );
        }

        return getModernEndPosition(
                text,
                x
        );
    }

    /**
     * Minecraft標準形式の影付き文字列描画。
     *
     * @param text 文字列
     * @param x X座標
     * @param y Y座標
     * @param color 文字色
     * @return 描画終了位置
     */
    @Override
    public int drawStringWithShadow(
            @Nonnull String text,
            float x,
            float y,
            int color
    ) {

        if (requiresVanillaFallback(text)) {

            beginVanillaMetrics();

            try {

                /*
                 * super.drawStringWithShadow()は内部で
                 * drawString()を呼ぶため、直接5引数版へ渡す。
                 */
                return super.drawString(
                        text,
                        x,
                        y,
                        color,
                        true
                );

            } finally {

                endVanillaMetrics();
            }
        }

        modernFontRenderer.drawStringWithShadow(
                text,
                x,
                y + VANILLA_Y_OFFSET,
                color
        );

        return getModernEndPosition(
                text,
                x
        );
    }

    /**
     * Minecraft標準形式で文字列の横幅を返す。
     *
     * @param text 対象文字列
     * @return 横幅
     */
    @Override
    public int getStringWidth(
            @Nonnull String text
    ) {

        if (requiresVanillaFallback(text)) {

            beginVanillaMetrics();

            try {

                return super.getStringWidth(
                        text
                );

            } finally {

                endVanillaMetrics();
            }
        }

        return Math.round(
                modernFontRenderer.getStringWidth(
                        text
                )
        );
    }

    /**
     * Minecraft標準形式で1文字の横幅を返す。
     *
     * @param character 対象文字
     * @return 表示幅
     */
    @Override
    public int getCharWidth(
            char character
    ) {

        /*
         * Vanilla側の内部処理中は、
         * 必ずVanillaの文字幅を返す。
         */
        if (vanillaMetricsDepth > 0) {

            return super.getCharWidth(
                    character
            );
        }

        /*
         * Minecraftの書式開始記号。
         * Vanilla FontRendererも-1を返す。
         */
        if (character == '§') {
            return -1;
        }

        /*
         * 単独のUTF-16サロゲートは
         * char単位では正しくCode Point化できない。
         */
        if (Character.isSurrogate(character)) {

            return super.getCharWidth(
                    character
            );
        }


        if (!modernFontRenderer.canDisplay(
                character
        )) {

            return super.getCharWidth(
                    character
            );
        }

        return Math.round(
                modernFontRenderer.getCodePointWidth(
                        character,
                        false
                )
        );
    }

    /**
     * 指定幅に収まるよう文字列を前方から切り詰める。
     *
     * @param text 対象文字列
     * @param width 最大幅
     * @return 切り詰めた文字列
     */
    @Override
    @Nonnull
    public String trimStringToWidth(
            @Nonnull String text,
            int width
    ) {

        return trimStringToWidth(
                text,
                width,
                false
        );
    }

    /**
     * 指定幅に収まるよう文字列を切り詰める。
     *
     * @param text 対象文字列
     * @param width 最大幅
     * @param reverse trueなら末尾側を残す
     * @return 切り詰めた文字列
     */
    @Override
    @Nonnull
    public String trimStringToWidth(
            @Nonnull String text,
            int width,
            boolean reverse
    ) {

        if (requiresVanillaFallback(text)) {

            beginVanillaMetrics();

            try {

                return super.trimStringToWidth(
                        text,
                        width,
                        reverse
                );

            } finally {

                endVanillaMetrics();
            }
        }

        if (width <= 0 || text.isEmpty()) {
            return "";
        }

        if (reverse) {

            return trimStringToWidthReverse(
                    text,
                    width
            );
        }

        return trimStringToWidthForward(
                text,
                width
        );
    }

    /**
     * 指定幅で文字列を折り返して描画する。
     *
     * @param str 対象文字列
     * @param x X座標
     * @param y Y座標
     * @param wrapWidth 折り返し幅
     * @param textColor 文字色
     */
    @Override
    public void drawSplitString(
            @Nonnull String str,
            int x,
            int y,
            int wrapWidth,
            int textColor
    ) {

        if (requiresVanillaFallback(str)) {

            beginVanillaMetrics();

            try {

                super.drawSplitString(
                        str,
                        x,
                        y,
                        wrapWidth,
                        textColor
                );

            } finally {

                endVanillaMetrics();
            }

            return;
        }

        String trimmed =
                trimTrailingNewlines(
                        str
                );

        List<String> lines =
                listFormattedStringToWidth(
                        trimmed,
                        wrapWidth
                );

        int currentY = y;

        for (String line : lines) {

            drawString(
                    line,
                    x,
                    currentY,
                    textColor
            );

            currentY +=
                    this.FONT_HEIGHT;
        }
    }

    /**
     * 指定幅で折り返した場合の高さを返す。
     *
     * @param str 対象文字列
     * @param maxLength 最大幅
     * @return 必要な高さ
     */
    @Override
    public int getWordWrappedHeight(
            @Nonnull String str,
            int maxLength
    ) {

        if (requiresVanillaFallback(str)) {

            beginVanillaMetrics();

            try {

                return super.getWordWrappedHeight(
                        str,
                        maxLength
                );

            } finally {

                endVanillaMetrics();
            }
        }

        return FONT_HEIGHT
                * listFormattedStringToWidth(
                str,
                maxLength
        ).size();
    }

    /**
     * 指定幅に収まる複数行へ文字列を分割する。
     *
     * @param str 対象文字列
     * @param wrapWidth 最大幅
     * @return 分割された文字列一覧
     */
    @Override
    @Nonnull
    public List<String> listFormattedStringToWidth(
            @Nonnull String str,
            int wrapWidth
    ) {

        if (requiresVanillaFallback(str)) {

            beginVanillaMetrics();

            try {

                return super.listFormattedStringToWidth(
                        str,
                        wrapWidth
                );

            } finally {

                endVanillaMetrics();
            }
        }

        return wrapModernString(
                str,
                wrapWidth
        );
    }

    /**
     * 前方から指定幅まで切り詰める。
     *
     * @param text 対象文字列
     * @param width 最大幅
     * @return 切り詰めた文字列
     */
    private String trimStringToWidthForward(
            String text,
            int width
    ) {

        StringBuilder builder =
                new StringBuilder();

        float currentWidth = 0.0F;

        boolean bold = false;

        int offset = 0;

        while (offset < text.length()) {

            char first =
                    text.charAt(
                            offset
                    );

            if (first == '§'
                    && offset + 1 < text.length()) {

                char formatCode =
                        Character.toLowerCase(
                                text.charAt(
                                        offset + 1
                                )
                        );

                builder.append(
                        first
                );

                builder.append(
                        text.charAt(
                                offset + 1
                        )
                );

                bold =
                        updateBoldState(
                                bold,
                                formatCode
                        );

                offset += 2;

                continue;
            }

            int codePoint =
                    text.codePointAt(
                            offset
                    );

            float characterWidth =
                    modernFontRenderer.getCodePointWidth(
                            codePoint,
                            bold
                    );

            if (currentWidth + characterWidth
                    > width) {

                break;
            }

            builder.appendCodePoint(
                    codePoint
            );

            currentWidth +=
                    characterWidth;

            offset +=
                    Character.charCount(
                            codePoint
                    );
        }

        return builder.toString();
    }

    /**
     * 末尾側を残す形で指定幅まで切り詰める。
     * テキストフィールドなどの横スクロール用途を想定する。
     *
     * @param text 対象文字列
     * @param width 最大幅
     * @return 切り詰めた文字列
     */
    private String trimStringToWidthReverse(
            String text,
            int width
    ) {

        if (getStringWidth(text) <= width) {
            return text;
        }

        int start = 0;

        while (start < text.length()) {

            int nextStart =
                    getNextTokenEnd(
                            text,
                            start
                    );

            String formatPrefix =
                    FontRenderer.getFormatFromString(
                            text.substring(
                                    0,
                                    nextStart
                            )
                    );

            String candidate =
                    formatPrefix
                            + text.substring(
                            nextStart
                    );

            if (getStringWidth(candidate)
                    <= width) {

                return candidate;
            }

            start =
                    nextStart;
        }

        return "";
    }

    /**
     * Modern Fontの文字幅を使って複数行へ分割する。
     *
     * @param text 対象文字列
     * @param wrapWidth 最大幅
     * @return 分割結果
     */
    private List<String> wrapModernString(
            String text,
            int wrapWidth
    ) {

        List<String> lines =
                new ArrayList<>();

        if (text.isEmpty()) {

            lines.add("");

            return lines;
        }

        String remaining =
                text;

        while (!remaining.isEmpty()) {

            int breakIndex =
                    sizeStringToWidthModern(
                            remaining,
                            wrapWidth
                    );

            /*
             * 全体が幅内に収まった。
             */
            if (breakIndex >= remaining.length()) {

                lines.add(
                        remaining
                );

                break;
            }

            /*
             * 先頭が改行の場合。
             */
            if (breakIndex == 0
                    && remaining.charAt(0) == '\n') {

                lines.add("");

                remaining =
                        remaining.substring(1);

                continue;
            }

            /*
             * 極端に狭い幅でも無限ループしないよう、
             * 最低1トークンは進める。
             */
            if (breakIndex <= 0) {

                breakIndex =
                        getNextTokenEnd(
                                remaining,
                                0
                        );
            }

            String line =
                    remaining.substring(
                            0,
                            breakIndex
                    );

            lines.add(
                    line
            );

            char breakCharacter =
                    remaining.charAt(
                            breakIndex
                    );

            boolean skipBreakCharacter =
                    breakCharacter == ' '
                            || breakCharacter == '\n';

            int nextIndex =
                    breakIndex
                            + (skipBreakCharacter
                            ? 1
                            : 0);

            String formatPrefix =
                    FontRenderer.getFormatFromString(
                            line
                    );

            remaining =
                    formatPrefix
                            + remaining.substring(
                            nextIndex
                    );
        }

        return lines;
    }

    /**
     * Modern Fontの文字幅を使い、
     * 1行に含められる文字列位置を求める。
     *
     * @param text 対象文字列
     * @param wrapWidth 最大幅
     * @return 改行位置
     */
    private int sizeStringToWidthModern(
            String text,
            int wrapWidth
    ) {

        float currentWidth =
                0.0F;

        int lastSpace =
                -1;

        int offset =
                0;

        boolean bold =
                false;

        boolean hasVisibleCharacter =
                false;

        while (offset < text.length()) {

            char first =
                    text.charAt(
                            offset
                    );

            if (first == '\n') {

                return offset;
            }

            if (first == ' ') {

                lastSpace =
                        offset;
            }

            if (first == '§'
                    && offset + 1 < text.length()) {

                char formatCode =
                        Character.toLowerCase(
                                text.charAt(
                                        offset + 1
                                )
                        );

                bold =
                        updateBoldState(
                                bold,
                                formatCode
                        );

                offset += 2;

                continue;
            }

            int codePoint =
                    text.codePointAt(
                            offset
                    );

            int codePointLength =
                    Character.charCount(
                            codePoint
                    );

            float characterWidth =
                    modernFontRenderer.getCodePointWidth(
                            codePoint,
                            bold
                    );

            if (currentWidth + characterWidth
                    > wrapWidth) {

                if (lastSpace >= 0) {

                    return lastSpace;
                }

                /*
                 * 最初の1文字だけで幅を超える場合も、
                 * その文字は1行目へ含める。
                 */
                if (!hasVisibleCharacter) {

                    return offset
                            + codePointLength;
                }

                return offset;
            }

            currentWidth +=
                    characterWidth;

            hasVisibleCharacter =
                    true;

            offset +=
                    codePointLength;
        }

        return text.length();
    }

    /**
     * 書式コードによって太字状態を更新する。
     *
     * @param currentBold 現在の太字状態
     * @param formatCode Minecraft書式コード
     * @return 更新後の太字状態
     */
    private boolean updateBoldState(
            boolean currentBold,
            char formatCode
    ) {

        if (isColorCode(formatCode)) {
            return false;
        }

        if (formatCode == 'l') {
            return true;
        }

        if (formatCode == 'r') {
            return false;
        }

        return currentBold;
    }

    /**
     * Minecraftのカラーコードか確認する。
     *
     * @param formatCode 書式コード
     * @return 0～9またはa～fならtrue
     */
    private boolean isColorCode(
            char formatCode
    ) {

        char normalized =
                Character.toLowerCase(
                        formatCode
                );

        return (normalized >= '0'
                && normalized <= '9')
                || (normalized >= 'a'
                && normalized <= 'f');
    }

    /**
     * 1文字または1書式コード分だけ次へ進む。
     *
     * @param text 対象文字列
     * @param index 現在位置
     * @return 次の安全な文字位置
     */
    private int getNextTokenEnd(
            String text,
            int index
    ) {

        if (index >= text.length()) {
            return text.length();
        }

        if (text.charAt(index) == '§'
                && index + 1 < text.length()) {

            return index + 2;
        }

        int codePoint =
                text.codePointAt(
                        index
                );

        return index
                + Character.charCount(
                codePoint
        );
    }

    /**
     * 末尾の改行を削除する。
     *
     * @param text 対象文字列
     * @return 改行削除後の文字列
     */
    private String trimTrailingNewlines(
            String text
    ) {

        String result =
                text;

        while (result.endsWith("\n")) {

            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        return result;
    }

    /**
     * 文字列に現在のModern Fontでは表示できない文字が含まれるか確認する。
     * Minecraftの§書式コードは表示文字ではないため判定対象から除外する。
     *
     * @param text 判定する文字列
     * @return Vanilla FontRendererへのフォールバックが必要ならtrue
     */
    private boolean requiresVanillaFallback(
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

            if (Character.isISOControl(
                    codePoint
            )) {

                continue;
            }

            if (!modernFontRenderer.canDisplay(
                    codePoint
            )) {

                return true;
            }
        }

        return false;
    }

    /**
     * Modern Fontで描画した場合の終了X座標を取得する。
     *
     * @param text 描画した文字列
     * @param startX 描画開始X座標
     * @return 描画終了位置
     */
    private int getModernEndPosition(
            String text,
            float startX
    ) {

        return Math.round(
                startX
                        + modernFontRenderer.getStringWidth(
                        text
                )
        );
    }

    /**
     * Vanilla側内部計測モードへ入る。
     */
    private void beginVanillaMetrics() {

        vanillaMetricsDepth++;
    }

    /**
     * Vanilla側内部計測モードから抜ける。
     */
    private void endVanillaMetrics() {

        if (vanillaMetricsDepth > 0) {
            vanillaMetricsDepth--;
        }
    }
}