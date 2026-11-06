# ExtractedText.startOffset 実機切り分け（使い捨てテストホスト）

PR #5 の製品コードには含めず、別ブランチで検証します。入力欄は合成文字だけを使います。インターネット権限・入力本文の保存・送信はありません。IMEのデバッグログも製品へ追加しません。

## ビルドと起動（Mac + Android）

```sh
./gradlew -p checks/offset-probe --no-daemon assembleDebug
apk=$(find checks/offset-probe/build/outputs/apk/debug -name '*-debug.apk' -print -quit)
adb install -r "$apk"
adb shell am start -n jp.kanaflick.offsetprobe/.ProbeActivity
```

IMEをPR #5の試験版KanaPromptに切り替えてください。公開v1.0.3を試しただけでは補正コードの検証になりません。

1. 画面に `connections=1` 以上が表示されるか確認します。ゼロならテスト欄とIMEの接続が未成立です。
2. 「Set cursor to 101」を押し、KanaPromptの右カーソルキーを1回押します。`last get` に `returned startOffset=100, relativeStart=1`、`last set` に `request=103..103`、実カーソル位置103が表示される想定です。
3. 「Set cursor to 103」を押し、左カーソルキーを1回押します。`relativeStart=3`、`request=101..101`、実カーソル位置101が想定です。
4. `getExtractedText fallback` の場合、`rawOffset`・`rawSelection`・`rawLength` の数値で接続の前提条件を見直します。特に`startOffset`と`partialStartOffset`を混同しないでください。
5. `getExtractedText` が一度も呼ばれない場合、対象入力欄へのフォーカス、選択中のIME、`onCreateInputConnection`の呼出しを確認します。

実機画面上の表示と、必要であれば次のコマンドで数値のイベントだけを確認できます。

```sh
adb logcat -d -s CursorOffsetProbe:I '*:S'
```

このホストはオフセット非0を人工的に返す統合試験です。一般の実アプリが同じ値を返す保証ではありません。別の入力欄との互換性を証明するものでもありません。

検証後は、元のIMEへ戻してテストホストを削除します。PR #5をマージする前にテスト用コードを製品の変更差分へ混入させないでください。

```sh
adb uninstall jp.kanaflick.offsetprobe
```
