# lint警告の整理（v1.0.3）

2026-11-06、debug/releaseのlintはともにエラー0・警告4でした。v1.0.2の7件から3件を修正しています。警告の一括抑制や、動作しない実装を追加して件数だけ減らす対応はしていません。

## 修正した3件

| 警告 | 対応 |
|---|---|
| DataExtractionRules | allowBackup=falseを維持し、古いAndroid向けのfullBackupContent=falseと、Android 12以降のクラウドバックアップ・端末間転送の除外ルールを追加。対象の全9ドメインを除外。 |
| ObsoleteSdkInt | minSdk 29で不要なmipmap-anydpi-v26をmipmap-anydpiへ移動。 |
| MonochromeLauncherIcon | 既存の前景ベクターをmonochromeレイヤーにも使用。通常の配色や形状は変更しない。 |

## 残る4件と判断

| 警告 | 現状・理由 | 今後の対応 |
|---|---|---|
| OldTargetApi | targetSdk 35を維持。今回のバックアップ・アイコン整理と、対象SDK変更による動作変更を分けるため。 | 対象SDK更新時に関連するIME動作や表示を検証する。 |
| ViewConstructor | IMEが入力欄参照・入力処理のコールバックを渡してViewを直接生成。XMLからの生成は行わない。空のコンストラクタを加えると入力処理のないViewを作れてしまう。 | XML生成やプレビューが必要になった時に設計を見直す。 |
| ClickableViewAccessibility | 各キーの読み上げ・操作を表すアクセシビリティ階層は未実装。空のperformClickだけ追加しても解決しない。 | 仮想キーのノードと操作方法を設計し、TalkBackで検証する。 |
| RtlHardcoded | ガイドはgetLocationOnScreenで得た絶対座標をGravity.LEFTで指定。STARTに替えるとRTL時に座標の意味が変わる。 | RTL環境で絶対位置とガイド表示を検証して判断する。 |

## 確認の限界

バックアップ・端末移行の実行試験、テーマ付きアイコンの各ランチャーでの見た目、Android 10〜15でのアイコン読込は未実施です。XMLの構成、APKビルド、lint、Android 16実機への導入を確認しています。入力動作と互換性の確認範囲は[実機検証記録](TESTING.md)を参照してください。

## 参照資料

- [Androidのバックアップ設定](https://developer.android.com/identity/data/autobackup?hl=en)：data-extraction-rulesの構文と除外ドメイン。
- [Adaptive icons](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive)：同一の前景ベクターをmonochromeに使う公式例。
- [カスタムViewのアクセシビリティ](https://developer.android.com/guide/topics/ui/accessibility/views/custom-views)：単なるクリック通知に加え、各操作の情報が必要。
