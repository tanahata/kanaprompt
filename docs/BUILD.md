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

PR #5のコード上の識別子は`jp.kanaprompt.ime`、IMEサービスは`KanaPromptImeService`、版番号は1.0.5（22）へ更新済み。ただし、**実機で24件を検証したローカルAPKは旧識別子の別ビルド**です。現PRをそのままマージ・公開しないでください。

1. Macで`git status --short`を確認し、未コミット作業と`releases/v1.0.5/`の旧候補APK・ソースZIP・SHA256SUMSを保護する。
2. ローカルの試験済み候補版が持つ文頭左・文末右の境界修正を`app/src/main/java/jp/kanaprompt/ime/CursorMovement.kt`へ反映し、試験済みソースとGitHubを照合する。
3. 新パッケージでローカルビルド・両lint・該当回帰チェックを実行する。APKのpackageが`jp.kanaprompt.ime`、版番号が1.0.5／22、IMEサービスが`jp.kanaprompt.ime.KanaPromptImeService`であることを確認する。
4. 新APKを旧版とは別アプリとして実機に導入。設定から新KanaPromptを有効化・選択し、かな入力・削除・カーソル・IME切替を確認する。旧`jp.kanaflick.ime`は新IMEの動作を確認するまでは削除しない。旧版と新規版は同名で並ぶ可能性がある。
5. 差分を確認してPR #5をマージ。診断専用PR #6は製品にマージしない。新パッケージのAPKを既存証明書で署名し、マージしたソースに対応するZIPとSHA256SUMSを生成・照合してv1.0.5 GitHub Releaseへ公開。旧識別子で署名したローカルAPKは流用しない。
6. 公開後にReleaseのAPK・ソースZIP・SHA256SUMSと`releases/latest`を確認する。Actionsは手動専用のまま。

IME画面のバージョン番号表示は追加しない。Androidのアプリ情報でバージョンを確認できるためです。

公開操作を行っていない段階ではv1.0.5を公開済みと記載せず、署名鍵や個人的な入力内容をコミットしない。

## 署名

公開APKはreleaseビルドを旧版と同じ証明書で署名しています。旧開発版との更新互換性を優先しているため、証明書の名義はAndroid Debugです。APK自体はdebuggableではありません。これはGoogle Play向けの新規署名設計ではありません。

公開APKの証明書SHA-256:
`5a6955d60f416ca2ad93a8fbda52cbf33b6837912a26f7e24f3d1c326ffd8bb7`

署名鍵は公開しません。自分でビルド・署名したAPKは公開版へ上書きできない場合があります。鍵、パスワード、ローカル設定、入力文章、端末IDをリポジトリに含めないでください。
