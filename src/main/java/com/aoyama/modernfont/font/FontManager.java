package com.aoyama.modernfont.font;

import com.aoyama.modernfont.AoyamasModernFont;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Aoyama's Modern Fontで使用するフォントの検索、読み込み、
 * および内蔵フォントへのフォールバックを管理する。
 */
public class FontManager {

    /**
     * ロガー。
     */
    private static final Logger LOGGER =
            LogManager.getLogger(
                    AoyamasModernFont.MODID
            );

    /**
     * MODに内蔵しているデフォルトフォント。
     */
    private static final String DEFAULT_FONT_RESOURCE =
            "/assets/aoyamasmodernfont/fonts/SourceHanSansJP-Medium.otf";

    /**
     * ユーザーがカスタムフォントを配置するフォルダ。
     */
    private final File fontDirectory;

    /**
     * 実際に読み込まれたフォント。
     */
    private Font loadedFont;

    /**
     * カスタムフォントを使用しているか。
     */
    private boolean usingCustomFont = false;

    /**
     * 現在発生しているカスタムフォントエラー。
     */
    private CustomFontErrorType customFontErrorType =
            CustomFontErrorType.NONE;

    /**
     * FontManagerを生成する。
     *
     * @param event ForgeのPreInitializationイベント
     */
    public FontManager(
            FMLPreInitializationEvent event
    ) {

        File modConfigDirectory =
                new File(
                        event.getModConfigurationDirectory(),
                        "aoyamasmodernfont"
                );

        this.fontDirectory =
                new File(
                        modConfigDirectory,
                        "fonts"
                );
    }

    /**
     * フォント管理機能を初期化する。
     * カスタムフォントが存在すれば優先して読み込み、
     * 使用できない場合は内蔵フォントへフォールバックする。
     */
    public void initialize() {

        usingCustomFont = false;
        customFontErrorType =
                CustomFontErrorType.NONE;

        if (!createFontDirectory()) {

            loadDefaultFont();
            return;
        }

        File customFont =
                findCustomFont();

        if (customFont != null) {

            if (loadCustomFont(customFont)) {
                return;
            }

            LOGGER.warn(
                    "Falling back to default font."
            );
        }

        loadDefaultFont();
    }

    /**
     * config/aoyamasmodernfont/fonts/ を作成する。
     *
     * @return 使用可能な状態ならtrue
     */
    private boolean createFontDirectory() {

        if (fontDirectory.exists()) {

            if (fontDirectory.isDirectory()) {
                return true;
            }

            customFontErrorType =
                    CustomFontErrorType.DIRECTORY_ERROR;

            LOGGER.warn(
                    "Font path exists but is not a directory: {}",
                    fontDirectory.getAbsolutePath()
            );

            return false;
        }

        if (fontDirectory.mkdirs()) {

            LOGGER.info(
                    "Created font directory: {}",
                    fontDirectory.getAbsolutePath()
            );

            return true;
        }

        customFontErrorType =
                CustomFontErrorType.DIRECTORY_ERROR;

        LOGGER.warn(
                "Could not create font directory: {}",
                fontDirectory.getAbsolutePath()
        );

        return false;
    }

    /**
     * カスタムフォント関連のエラー種別。
     */
    public enum CustomFontErrorType {

        /**
         * エラーなし。
         */
        NONE,

        /**
         * カスタムフォントの読み込み失敗。
         */
        LOAD_FAILED,

        /**
         * 複数のカスタムフォントを検出。
         */
        MULTIPLE_FONTS,

        /**
         * フォントフォルダの作成または読み取り失敗。
         */
        DIRECTORY_ERROR
    }

    /**
     * カスタムフォントを検索する。
     *
     * @return 使用可能なフォントファイル。存在しない場合はnull
     */
    private File findCustomFont() {

        File[] fontFiles =
                fontDirectory.listFiles(
                        file ->
                                file.isFile()
                                        && isSupportedFont(file)
                );

        if (fontFiles == null) {

            customFontErrorType =
                    CustomFontErrorType.DIRECTORY_ERROR;

            LOGGER.warn(
                    "Could not read custom font directory: {}",
                    fontDirectory.getAbsolutePath()
            );

            return null;
        }

        if (fontFiles.length == 0) {

            LOGGER.info(
                    "No custom font found."
            );

            return null;
        }

        if (fontFiles.length > 1) {

            customFontErrorType =
                    CustomFontErrorType.MULTIPLE_FONTS;

            LOGGER.warn(
                    "Multiple custom fonts found."
            );

            LOGGER.warn(
                    "Please place only one font file in: {}",
                    fontDirectory.getAbsolutePath()
            );

            LOGGER.warn(
                    "The built-in default font will be used."
            );

            return null;
        }

        return fontFiles[0];
    }

    /**
     * カスタムフォントを読み込む。
     *
     * @param fontFile 読み込むフォントファイル
     * @return 読み込みに成功した場合true
     */
    private boolean loadCustomFont(
            File fontFile
    ) {

        try (
                InputStream inputStream =
                        Files.newInputStream(
                                fontFile.toPath()
                        )
        ) {

            loadedFont =
                    Font.createFont(
                            Font.TRUETYPE_FONT,
                            inputStream
                    );

            usingCustomFont = true;

            customFontErrorType =
                    CustomFontErrorType.NONE;

            LOGGER.info(
                    "Custom font loaded successfully."
            );

            LOGGER.info(
                    "File: {}",
                    fontFile.getName()
            );

            LOGGER.info(
                    "Custom font name: {}",
                    loadedFont.getFontName()
            );

            return true;

        } catch (FontFormatException | IOException e) {

            customFontErrorType =
                    CustomFontErrorType.LOAD_FAILED;

            usingCustomFont = false;
            loadedFont = null;

            LOGGER.error(
                    "Failed to load custom font: {}",
                    fontFile.getName(),
                    e
            );

            return false;
        }
    }

    /**
     * MOD内蔵フォントを読み込む。
     */
    private void loadDefaultFont() {

        try (
                InputStream inputStream =
                        FontManager.class.getResourceAsStream(
                                DEFAULT_FONT_RESOURCE
                        )
        ) {

            if (inputStream == null) {

                throw new IOException(
                        "Default font resource not found: "
                                + DEFAULT_FONT_RESOURCE
                );
            }

            loadedFont =
                    Font.createFont(
                            Font.TRUETYPE_FONT,
                            inputStream
                    );

            usingCustomFont = false;

            LOGGER.info(
                    "Default font loaded successfully."
            );

            LOGGER.info(
                    "Default font name: {}",
                    loadedFont.getFontName()
            );

        } catch (FontFormatException | IOException e) {

            usingCustomFont = false;
            loadedFont = null;

            LOGGER.error(
                    "Failed to load default font.",
                    e
            );
        }
    }

    /**
     * 指定ファイルが対応フォント形式か確認する。
     *
     * @param file 判定するファイル
     * @return .ttfまたは.otfの場合true
     */
    private boolean isSupportedFont(
            File file
    ) {

        String name =
                file.getName()
                        .toLowerCase();

        return name.endsWith(".ttf")
                || name.endsWith(".otf");
    }

    /**
     * 読み込まれているフォントを取得する。
     *
     * @return 読み込まれたフォント。読み込み失敗時はnull
     */
    public Font getLoadedFont() {

        return loadedFont;
    }

    /**
     * 現在カスタムフォントを使用しているか確認する。
     *
     * @return カスタムフォント使用中ならtrue
     */
    @SuppressWarnings("unused")
    public boolean isUsingCustomFont() {

        return usingCustomFont;
    }

    /**
     * カスタムフォント関連のエラーがあるか確認する。
     *
     * @return エラーがある場合true
     */
    public boolean hasCustomFontError() {

        return customFontErrorType
                != CustomFontErrorType.NONE;
    }

    /**
     * カスタムフォントエラーの種別を取得する。
     *
     * @return エラー種別
     */
    public CustomFontErrorType getCustomFontErrorType() {

        return customFontErrorType;
    }
}