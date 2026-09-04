package com.aoyama.modernfont.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * MinecraftのGUI描画時にAoyamaFontRendererをインストールする。
 * VanillaのGuiMainMenuだけでなく、Custom Main Menuなどの独自GUIにも対応する。
 */
public class ClientGuiEventHandler {

    /**
     * 最初のGUI描画前にFontRendererを一度だけ差し替える。
     *
     * @param event GUI描画イベント
     */
    @SubscribeEvent
    public void onDrawScreen(
            GuiScreenEvent.DrawScreenEvent.Pre event
    ) {

        /*
         * すでにインストール済みなら何もしない。
         */
        if (FontRendererInstaller.isInstalled()) {
            return;
        }

        /*
         * Minecraft.fontRendererを
         * AoyamaFontRendererへ差し替える。
         */
        boolean installed =
                FontRendererInstaller.install();

        if (!installed) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getMinecraft();

        GuiScreen currentScreen =
                event.getGui();

        /*
         * 現在のGUIは、生成時点で古いFontRendererを
         * 保持している可能性がある。
         *
         * 描画中に直接再初期化すると危険なので、
         * 次のMinecraftタスクとしてGUIを再初期化する。
         */
        minecraft.addScheduledTask(
                () -> {

                    /*
                     * タスク実行までに別画面へ移動していた場合は
                     * 何もしない。
                     */
                    if (minecraft.currentScreen
                            != currentScreen) {

                        return;
                    }

                    /*
                     * 現在の画面を再初期化する。
                     *
                     * GuiScreen側のfontRendererも
                     * 新しいminecraft.fontRendererへ更新される。
                     */
                    currentScreen.setWorldAndResolution(
                            minecraft,
                            currentScreen.width,
                            currentScreen.height
                    );
                }
        );
    }
}