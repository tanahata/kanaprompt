# ビルド

Android SDK 35、Android Gradle Plugin 8.7.3、Kotlin 2.0.21、Gradle Wrapper 9.6.0、JDK 21を使用します。
Android Studioでフォルダを開き、SDKの場所を環境に合わせて設定してください。local.propertiesはコミットしません。

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease
```

debug APKはapp/build/outputs/apk/debug/、未署名release APKはapp/build/outputs/apk/release/に生成されます。
IMEには通常のActivityがありません。インストール後、Androidの設定で有効化して選択します。

## 回帰チェック

ビルド後、Javaのソース起動でchecks/KeyboardLayoutCheck.javaを実行します。クラスパスにapp/build/tmp/kotlin-classes/debugとKotlin stdlib 2.0.21のJARを指定してください。

```sh
java -cp "app/build/tmp/kotlin-classes/debug:/path/to/kotlin-stdlib-2.0.21.jar" checks/KeyboardLayoutCheck.java
```

## 署名

公開APKはreleaseビルドを旧版と同じ証明書で署名しています。旧開発版との更新互換性を優先しているため、証明書の名義はAndroid Debugです。APK自体はdebuggableではありません。これはGoogle Play向けの新規署名設計ではありません。

公開APKの証明書SHA-256:
`5a6955d60f416ca2ad93a8fbda52cbf33b6837912a26f7e24f3d1c326ffd8bb7`

署名鍵は公開しません。自分でビルド・署名したAPKは公開版へ上書きできない場合があります。鍵、パスワード、ローカル設定、入力文章、端末IDをリポジトリに含めないでください。
