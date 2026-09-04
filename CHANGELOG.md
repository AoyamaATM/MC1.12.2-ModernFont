# Changelog

## v1.0.1 - 2026-09-04

Compatibility and stability update.

### Fixed

- `FontRenderer#getStringWidth(null)` など、Vanilla互換のnull処理を追加
- AppleSkinなど他MODからのFontRenderer呼び出しで発生するクラッシュを修正
- Bidi描画時にVanilla FontRendererへフォールバックするよう修正
- Minecraft書式コードの互換性を改善
- 未対応の`§k`および未知の書式コードをVanillaへフォールバック
- 末尾の単独`§`のVanilla互換処理を追加
- NBSP（U+00A0）および制御文字のVanilla互換処理を追加
- 太字を含む文字幅・切り詰め・折り返し処理の互換性を改善
- 影付き文字列の終了位置計算を修正
- Anaglyph設定時の文字色に対応
- OpenGLのBlend / Texture状態を適切に保持するよう改善
- GUI切り替え時のFontRenderer導入タイミングを改善
- リソースリロード時のFontRenderer互換性を改善
- OptiFine環境のDynamicTextureバッファ構造に対応
- Glyph Atlas満杯時にクラッシュせずVanilla FontRendererへフォールバックするよう修正

### Changed

- Glyph Atlasを1024×1024から2048×2048へ拡張
- Glyph Atlas更新時の不要な一時メモリ確保を削減
- Glyph Atlas満杯後はVanilla FontRendererへ切り替え、例外の連続発生を防止
- MODバージョンを1.0.1へ更新

### Tested

- Minecraft Forge 1.12.2
- Nomifactory CEu 1.7.7
- JEI
- Better Questing
- GregTech系GUI
- AppleSkin
- Minecraft標準GUI

## v1.0.0 - 2026-08-27

Initial Release.

### Added

- Minecraft 1.12.2向けTrueType / OpenTypeフォントレンダラー
- Source Han Sans JP Mediumを標準フォントとして搭載
- `.ttf` / `.otf` カスタムフォント対応
- Minecraft標準カラーコード対応
- 太字対応
- 斜体対応
- 下線対応
- 取り消し線対応
- 文字影対応
- 文字幅計算対応
- 中央揃え対応
- 文字列の折り返し・切り詰め対応
- 未対応GlyphのVanillaフォントフォールバック
- 下付き数字など特殊文字のフォールバック表示
- カスタムフォント読み込み失敗時の自動フォールバック
- 複数カスタムフォント検出
- カスタムフォントエラーのゲーム内チャット通知
- 日本語・英語通知対応

### Tested

- Minecraft Forge 1.12.2
- Nomifactory CEu
- JEI / Had Enough Items
- Better Questing
- GregTech
- 各種Minecraft標準GUI