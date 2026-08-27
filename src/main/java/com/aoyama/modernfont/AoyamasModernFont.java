package com.aoyama.modernfont;

import com.aoyama.modernfont.client.ClientFontNotificationHandler;
import com.aoyama.modernfont.client.ClientGuiEventHandler;
import com.aoyama.modernfont.font.FontManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

/**
 * Aoyama's Modern Font for 1.12.2 のメインMODクラス。
 */
@Mod(
        modid = AoyamasModernFont.MODID,
        name = AoyamasModernFont.NAME,
        version = AoyamasModernFont.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        clientSideOnly = true
)
public class AoyamasModernFont {

    /**
     * MOD ID。
     */
    public static final String MODID =
            "aoyamasmodernfont";

    /**
     * MOD表示名。
     */
    public static final String NAME =
            "Aoyama's Modern Font for 1.12.2";

    /**
     * MODバージョン。
     */
    public static final String VERSION =
            "1.0.0";

    /**
     * フォントの検索と読み込みを管理するFontManager。
     */
    private static FontManager fontManager;

    /**
     * Forge PreInitialization処理。
     *
     * @param event ForgeのPreInitializationイベント
     */
    @Mod.EventHandler
    public void preInit(
            FMLPreInitializationEvent event
    ) {

        Logger logger =
                event.getModLog();

        logger.info(
                "{} loaded.",
                NAME
        );

        /*
         * カスタムフォントまたは
         * 内蔵デフォルトフォントを読み込む。
         */
        fontManager =
                new FontManager(
                        event
                );

        fontManager.initialize();

        /*
         * AoyamaFontRenderer導入処理を登録する。
         */
        MinecraftForge.EVENT_BUS.register(
                new ClientGuiEventHandler()
        );

        /*
         * カスタムフォントエラーの
         * チャット通知処理を登録する。
         */
        MinecraftForge.EVENT_BUS.register(
                new ClientFontNotificationHandler()
        );
    }

    /**
     * FontManagerを取得する。
     *
     * @return FontManager
     */
    public static FontManager getFontManager() {

        return fontManager;
    }
}