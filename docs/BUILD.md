# ビルド

Android SDK 35、Android Gradle Plugin 8.7.3、Kotlin 2.0.21、Gradle Wrapper 9.6.0、JDK 21を使用します。
Android Studioでフォルダを開き、SDKの場所を環境に合わせて設定してください。local.propertiesはコミットしません。

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease
```

debug APKはapp/build/outputs/apk/debug/、未署名release APKはapp/build/outputs/apk/release/に生成されます。
IMEには通常のActivityがありません。インストール後、Androidの設定で有効化して選択します。

## 回帰チェック

ビルド後、Javaのソース起動で配列・モード切替用の `checks/KeyboardLayoutCheck.java` と、カーソル移動用の `checks/CursorMovementCheck.java`、v1.0.6の配色検査 `checks/KeyboardThemeCheck.java` を実行します。クラスパスに `app/build/tmp/kotlin-classes/debug` とKotlin stdlib 2.0.21のJARを指定してください。

```sh
java -cp "app/build/tmp/kotlin-classes/debug:/path/to/kotlin-stdlib-2.0.21.jar" checks/KeyboardLayoutCheck.java
java -cp "app/build/tmp/kotlin-classes/debug:/path/to/kotlin-stdlib-2.0.21.jar" checks/CursorMovementCheck.java
java -cp "app/build/tmp/kotlin-classes/debug:/path/to/kotlin-stdlib-2.0.21.jar" checks/KeyboardThemeCheck.java
```

## GitHubでの任意検査

GitHub Actionsはpush・PR更新では自動実行しません。必要性が明確なときだけActions画面から手動で `Android checks` を実行します。通常はMacのローカル環境でGradleビルド・lint・回帰チェックを行い、実施できなかった項目は未検証と記録します。手動実行時もArtifactはアップロードせず、Gradleキャッシュを保存しません。CIはAPKの公開署名・配布や実機操作を行いません。実機確認の範囲は[検証記録](TESTING.md)を参照してください。

## 今後の更新・配布手順

1. `git status --short`でローカル変更を確認・保護し、作業対象のコミットと`main`の差分を把握する。公開前に版番号を更新し、`applicationId=jp.kanaprompt.ime`と`KanaPromptImeService`を維持する。
2. 上記のGradleビルド・lintと3種類の回帰チェックを実行する。入力処理を変更した場合は影響箇所を実機で再確認する。検査の合否と対象コミットを[検証記録](TESTING.md)に残す。
3. **v1.0.6以降と同じKanaPrompt専用鍵**でrelease APKを署名し、証明書・アプリID・版番号・`debuggable=false`を検証する。新しい鍵を作らない。コマンドと保管上の注意は[署名手順](SIGNING.md)を参照する。
4. Android実機でIMEのインストール／更新、有効化・選択、かな入力・削除・カーソル・IME切替を確認する。UI変更がある場合はライト／ダークと押下表示も確認する。通常の同一署名更新では、旧版を毎回アンインストールする必要はない。
5. 検証済みソースを`main`へ反映し、公開するソースとタグ・APK・ソースZIPの整合を確認する。必要なら確定ソースから再ビルド・再署名する。`SHA256SUMS`を生成し、GitHub ReleaseへAPK・対応するソースZIP・チェックサムを登録してダウンロード照合する。公開済みのタグやアセットは差し替えない。

GitHub Actionsは通常不要です。秘密鍵、パスワード、個人的な入力内容、端末ID、ローカル設定をリポジトリや配布ZIPに含めないでください。IME上には版番号を表示せず、Androidのアプリ情報で確認します。

## 過去の公開と移行（履歴）

- **v1.0.5（2026-11-08）**：旧`jp.kanaflick.ime`から`jp.kanaprompt.ime`へアプリIDを変更。旧版とは別アプリとしてインストール。[Release](https://github.com/tanahata/kanaprompt/releases/tag/v1.0.5)・[実機検証](TESTING.md)に履歴を保持。
- **v1.0.6（2026-11-10）**：山吹色UIの採用とKanaPrompt専用署名鍵への一度限りの移行。旧署名版からは再インストールが必要。以後は同じ鍵を継続使用する。[Release](https://github.com/tanahata/kanaprompt/releases/tag/v1.0.6)・[署名記録](SIGNING.md)・[実機検証](TESTING.md)を参照。

