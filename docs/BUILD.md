# ビルド

Android SDK 35、Android Gradle Plugin 8.7.3、Kotlin 2.0.21、Gradle Wrapper 9.6.0、JDK 21を使用します。
Android Studioでフォルダを開き、SDKの場所を環境に合わせて設定してください。local.propertiesはコミットしません。

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease
```

debug APKはapp/build/outputs/apk/debug/、未署名release APKはapp/build/outputs/apk/release/に生成されます。
IMEには通常のActivityがありません。インストール後、Androidの設定で有効化して選択します。

## 回帰チェック

ビルド後、Javaのソース起動で配列・モード切替用の `checks/KeyboardLayoutCheck.java` と、カーソル移動用の `checks/CursorMovementCheck.java` を実行します。クラスパスに `app/build/tmp/kotlin-classes/debug` とKotlin stdlib 2.0.21のJARを指定してください。

```sh
java -cp "app/build/tmp/kotlin-classes/debug:/path/to/kotlin-stdlib-2.0.21.jar" checks/KeyboardLayoutCheck.java
java -cp "app/build/tmp/kotlin-classes/debug:/path/to/kotlin-stdlib-2.0.21.jar" checks/CursorMovementCheck.java
```

## GitHubでの任意検査

GitHub Actionsはpush・PR更新では自動実行しません。必要性が明確なときだけActions画面から手動で `Android checks` を実行します。通常はMacのローカル環境でGradleビルド・lint・回帰チェックを行い、実施できなかった項目は未検証と記録します。手動実行時もArtifactはアップロードせず、Gradleキャッシュを保存しません。CIはAPKの公開署名・配布や実機操作を行いません。実機確認の範囲は[検証記録](TESTING.md)を参照してください。

## v1.0.5 公開・移行の確認記録（2026-11-08）

- [v1.0.5 Release](https://github.com/tanahata/kanaprompt/releases/tag/v1.0.5)は公開済み。PR #5を`main`へマージし、新しい`applicationId=jp.kanaprompt.ime`、IMEサービス`jp.kanaprompt.ime.KanaPromptImeService`、版番号`versionName=1.0.5`／`versionCode=22`を採用。
- 公開APKはタグに対応するソースからビルドし、既存の署名証明書で署名。Macでdebug/releaseビルド、両lint、KeyboardLayoutCheck・CursorMovementCheckに成功。AQUOS R9 Pro（Android 16）で新IMEの導入・有効化・選択・かな入力・削除・カーソルを確認しました。
- APK、対応するソースZIPとSHA256SUMSをReleaseへ登録。公開時のGitHub Actionsは不使用。
- v1.0.3以前の`jp.kanaflick.ime`とは別アプリとして共存可能で、上書き更新はできません。新IMEを有効化・選択し、入力確認後に旧版を必要に応じて削除してください。ユーザーは新KanaPromptでの通常入力を確認し、重複する旧KanaFlickを手動削除済みです。
- 旧識別子のローカルv1.0.5候補版については[検証記録](TESTING.md)に24件の局所検証履歴を保持しています。新識別子版で24件すべてを再実行したという意味ではありません。

以降の更新時も`git status --short`で未コミット差分を保護し、実際の署名APK・タグ付きソース・ZIP・SHA256SUMSの整合性を確認してから配布してください。診断用PR #6のコードは製品にマージしません。IME画面へバージョン番号は表示せず、Androidのアプリ情報で確認します。

## v1.0.5の署名（履歴）と次期公開版

**v1.0.6からの専用公開鍵への切替計画・Macでの手順：[公開署名鍵の管理と切替](SIGNING.md)／[Issue #7](https://github.com/tanahata/kanaprompt/issues/7)**。新鍵は生成・バックアップ・署名・実機確認が完了するまで公開版に適用しません。

v1.0.5の公開APKはreleaseビルドを旧版と同じ証明書で署名しています。旧開発版との更新互換性を優先しているため、証明書の名義はAndroid Debugです。APK自体はdebuggableではありません。これはGoogle Play向けの新規署名設計ではありません。

公開APKの証明書SHA-256:
`5a6955d60f416ca2ad93a8fbda52cbf33b6837912a26f7e24f3d1c326ffd8bb7`

署名鍵は公開しません。自分でビルド・署名したAPKは公開版へ上書きできない場合があります。鍵、パスワード、ローカル設定、入力文章、端末IDをリポジトリに含めないでください。
