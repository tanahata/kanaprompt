# ExtractedText.startOffset 実機切り分け（使い捨てテストホスト）

PR #5 の製品コードには含めず、別ブランチで検証します。入力欄は合成文字だけを使います。インターネット権限・入力本文の保存・送信はありません。IMEのデバッグログも製品へ追加しません。

## Macでの事前確認

- MacにJDK 21、Android SDK 35、`adb`が利用できる状態で、USB接続したAndroidが`adb devices`に`device`と表示されること。認証待ちの`unauthorized`では試験できません。
- 事前にPR #5のソースを使って、**従来と同じ署名鍵で署名した試験用KanaPrompt APK**をMac側で準備してください。公開版v1.0.3への再インストール記録があるため、端末上のIMEがPR #5の試験ビルドであることを確認してから実行します。公開版とPR #5はversionCodeが同じなので、設定画面の版番号のみでは区別できません。
- 検証用ホストは製品APKとは別パッケージです。専用の署名鍵やユーザー情報をGitHubへ登録する必要はありません。

```sh
git fetch origin
git switch test/offset-extraction-probe-20261107
git pull --ff-only
adb devices
java -version
```

未コミットの作業がMac側にある場合は、`git switch`を無理に実行せず、その作業を先に保護してください。

## ビルドと起動（Mac + Android）

```sh
bash checks/offset-probe/start-on-device.sh
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
