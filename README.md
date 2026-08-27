# Aoyama's Modern Font for 1.12.2

Minecraft 1.12.2 の標準フォント描画を置き換え、
TrueType / OpenType フォントによる滑らかな文字描画を行う
クライアント専用MODです。

## 主な機能

- TrueType / OpenType フォントによる滑らかな文字描画
- 日本語・英数字の描画
- Minecraftの装飾コードに対応
  - 色
  - 太字
  - 斜体
  - 下線
  - 取り消し線
  - 影
- Vanilla FontRenderer互換の文字幅計算
- 文字列の折り返し・切り詰めに対応
- 使用フォントに存在しない文字はVanillaフォントへフォールバック
- 一部の下付き数字を代替表示
- 任意の .ttf / .otf フォントへ差し替え可能
- カスタムフォントに問題がある場合は内蔵フォントへ自動復帰
- カスタムフォントエラーをゲーム内チャットへ通知
- 通知は日本語・英語に対応

## 動作環境

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.x
- クライアント専用

サーバー側へ導入する必要はありません。

## 標準フォント

標準では以下のフォントを使用します。

**Source Han Sans JP Medium**

Source Han Sans はAdobeによって公開されているフォントで、
SIL Open Font License 1.1 のもとで配布されています。

## カスタムフォント

任意の TrueType / OpenType フォントを使用できます。

以下のフォルダへ `.ttf` または `.otf` ファイルを
1つだけ配置してください。

```text
config/aoyamasmodernfont/fonts/
```

例：

config/
└─ aoyamasmodernfont/
   └─ fonts/
      └─ MyFont.otf

フォントを変更した場合はMinecraftを再起動してください。

### カスタムフォントがない場合

内蔵されている Source Han Sans JP Medium を使用します。

### カスタムフォントを読み込めない場合

フォントファイルが壊れているなどの理由で読み込めない場合は、
内蔵フォントへ自動的にフォールバックします。

ワールド参加時にチャットへエラー内容を表示します。

### 複数のフォントがある場合

fonts フォルダに配置できるカスタムフォントは1つだけです。

複数の .ttf / .otf ファイルを検出した場合は、
カスタムフォントを使用せず、内蔵フォントを使用します。

## 互換性

Aoyama's Modern Font はMinecraftのクライアント側 FontRenderer を
置き換えながら、一般的なFontRendererの処理との互換性を維持するよう
設計しています。

Nomifactory CEuを含む、大規模なMinecraft 1.12.2 MOD環境でも
動作確認を行っています。

ただし、すべてのMODとの完全な互換性を保証するものではありません。

表示上の問題を発見した場合はGitHub Issuesへ報告してください。

## 導入方法

1. Minecraft 1.12.2 とForgeを導入します。
2. 本MODのJARファイルを mods フォルダへ配置します。
3. Minecraftを起動します。

通常利用では追加設定は必要ありません。

## ライセンス

Aoyama's Modern Font for 1.12.2 のソースコードは
MIT License のもとで公開されています。

同梱されている Source Han Sans JP は、
SIL Open Font License 1.1 のもとで別途ライセンスされています。

詳細は同梱のライセンスファイルを参照してください。

## リンク

Modrinth:
