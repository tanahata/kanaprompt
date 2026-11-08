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

## v1.0.5 公開前の整合確認（2026-11-08）

実機検証済みの候補版とGitHubのソースに差があるため、**現状のPR #5をそのままマージ・タグ付けしてはいけません**。

1. Macの`git status --short`を先に確認し、未コミットの作業とローカル`releases/v1.0.5/`を保護する。試験済みAPK・対応ソースZIP・SHA256SUMSがあることを確認する。
2. ローカルv1.0.5に入っている「文頭から左／文末から右は無操作」の境界修正を、PR #5の`CursorMovement.kt`へ反映する。変更内容を試験済みソースと照合する。`app/build.gradle.kts`を`versionName = "1.0.5"`、`versionCode = 22`に合わせる（現GitHubブランチではまだ1.0.3／20）。
3. ソースと版番号、署名に関する差分を確認し、`docs/TESTING.md`の24件を検証したローカル候補版と一致するか判定する。一致しなければ差分に必要なチェックを追加する。過去の合格を未確認の別ビルドへ流用しない。
4. PR #5のレビューとマージを行い、`main`上の公開対象コミットを確定する。診断専用PR #6の使い捨てテストホストは製品にマージしない。
5. 確定したソースに対して`v1.0.5`タグを作成し、既存の署名証明書で署名したAPK、対応するソースZIP、SHA256SUMSをGitHub Releaseへ登録する。APKの署名証明書・版番号・ハッシュと、ZIPの対応ソースを照合する。Release本文には[変更履歴](../CHANGELOG.md)と[検証記録](TESTING.md)を参照先として記載する。
6. 公開後に`releases/latest`がv1.0.5を指すこと、添付ファイル3点と文書リンクが開けることを確認する。GitHub Actionsは必須とせず、自動起動もしない。

公開操作を行っていない段階では、v1.0.5を「公開済み」と記載しない。署名鍵・パスワード・実機入力内容はコミットしない。

## 署名

公開APKはreleaseビルドを旧版と同じ証明書で署名しています。旧開発版との更新互換性を優先しているため、証明書の名義はAndroid Debugです。APK自体はdebuggableではありません。これはGoogle Play向けの新規署名設計ではありません。

公開APKの証明書SHA-256:
`5a6955d60f416ca2ad93a8fbda52cbf33b6837912a26f7e24f3d1c326ffd8bb7`

署名鍵は公開しません。自分でビルド・署名したAPKは公開版へ上書きできない場合があります。鍵、パスワード、ローカル設定、入力文章、端末IDをリポジトリに含めないでください。
