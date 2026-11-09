# KanaPrompt 公開用署名鍵の管理と切替

**適用対象：次の公開版（v1.0.6を想定、未公開）**  
**進捗・検証チェックリスト：[Issue #7](https://github.com/tanahata/kanaprompt/issues/7)**

## 確定方針

- v1.0.5は[公開済み](https://github.com/tanahata/kanaprompt/releases/tag/v1.0.5)。既存APKは`Android Debug`名義の証明書で署名済み。**公開済みのAPK・タグ・アセットを差し替えない**。
- 次の公開から**KanaPromptだけで使う専用のrelease署名鍵**を長期使用する。EXIF Marginの鍵、従来のデバッグ鍵とは分ける。署名鍵はMacのローカルに生成し、秘密鍵とパスワードをGitHub、GitHub Actions、チャット、ログへ送信しない。
- `applicationId=jp.kanaprompt.ime`とIMEサービス`KanaPromptImeService`は変更しない。最初の新鍵版は`versionName=1.0.6`、`versionCode=23`を予定する。**v1.0.6公開準備ブランチでは版番号を1.0.6／23に変更済み**。v1.0.5の公開タグ・既存APKはそのまま保つ。実機・署名・公開完了までは次期版扱いとする。
- 署名証明書が変わるため、**v1.0.5からの通常の上書き更新はできない**。旧版のアンインストールが必要。旧版を使っている間に別の入力方式（Gboard等）を有効にしてから切り替え、新版を有効化・選択する。APK署名v3の鍵ローテーションと履歴の維持は今回採用しない（複雑性を増やさないため）。鍵の切替は今回限りとし、その後は同じ新鍵で継続更新する。
- 同じv1.0.6でキーボードの配色・アイコン背景をEXIF Margin共通の山吹色`#D99A22`に変更する。**入力機能、キー配列・寸法は変更しない**。昼夜切替と押下／フリック表示の確認を行い、Macローカルでビルド・検証する。Actionsは自動実行しない。

## Macでの秘密鍵生成（1回のみ）

1. 最初に`git status --short`で未コミット変更を確認し保護する。現在の配布版とソースの対応を確認する。
2. 署名鍵の保存場所を選ぶ。以下は**Gitリポジトリ外**の例。既存のキーストアは決して上書きしない。
3. JDKの`keytool`を使用する。パスワードは**コマンド引数やシェル履歴へ記載しない**。対話で十分な長さの異なるアプリ用パスワードを設定し、パスワードマネージャー等に保存する。

```sh
set -eu
umask 077
KEY_DIR="$HOME/Library/Application Support/KanaPrompt/Signing"
KEY_FILE="$KEY_DIR/KanaPrompt-release.p12"
mkdir -p "$KEY_DIR"
chmod 700 "$KEY_DIR"
if [ -e "$KEY_FILE" ]; then
  echo "署名鍵が既に存在します。上書きせず中止します。"
  exit 1
fi
keytool -genkeypair -v \
  -keystore "$KEY_FILE" -storetype PKCS12 \
  -alias kanaprompt-release \
  -keyalg RSA -keysize 3072 -validity 12000 \
  -dname "CN=KanaPrompt, O=tanahata, C=JP"
chmod 600 "$KEY_FILE"
keytool -list -v -keystore "$KEY_FILE" -alias kanaprompt-release
```

- **既存の鍵がある場合**：新しい鍵を作り直さず、証明書の有効期限・SHA-256・バックアップの有無を確認する。
- 秘密鍵本体は2箇所以上に保管し、少なくとも1箇所はMacと独立した暗号化バックアップにする。複製・復元した鍵が同一であることを、安全なローカル環境でSHA-256または証明書fingerprintで確認する。パスワードは鍵と同一の無保護フォルダに置かない。復元できる状態の確認が終わるまでは公開しない。
- **記録・公開してよいもの**：証明書SHA-256、期限、鍵の管理方式（秘密の保存場所・パスワードは不要）。この情報だけをIssue/Notionに記録する。

## 次のAPKの署名と検証

1. Macでソース・変更内容・未コミット差分を確認する。**公開準備ブランチ**の版番号は`1.0.6 (23)`へ更新済みなので再変更せず、従来の`applicationId`とIMEコンポーネントを維持できているか確認する。
2. `./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease`と、[ビルド手順](BUILD.md)の`KeyboardLayoutCheck`・`CursorMovementCheck`・`KeyboardThemeCheck`を実行。失敗があれば配布を中断。
3. Android SDK Build-Tools付属の`zipalign`、`apksigner`を使用する。下記は**実行例**。Codexはインストール済みのSDK/Build-Toolsの絶対パスと未署名APKの実在をまず確認する。パスワードを引数、環境変数、ログへ出さず、`apksigner`の対話入力を利用する。

```sh
# Macのインストール先に合わせて変更する（例）
BUILD_TOOLS="$HOME/Library/Android/sdk/build-tools/35.0.0"
UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
KEY_FILE="$HOME/Library/Application Support/KanaPrompt/Signing/KanaPrompt-release.p12"
mkdir -p releases/v1.0.6
"$BUILD_TOOLS/zipalign" -v 4 "$UNSIGNED_APK" "releases/v1.0.6/KanaPrompt-v1.0.6-aligned.apk"
"$BUILD_TOOLS/apksigner" sign \
  --ks "$KEY_FILE" --ks-key-alias kanaprompt-release \
  --out "releases/v1.0.6/KanaPrompt-v1.0.6.apk" \
  "releases/v1.0.6/KanaPrompt-v1.0.6-aligned.apk"
"$BUILD_TOOLS/apksigner" verify --verbose --print-certs \
  "releases/v1.0.6/KanaPrompt-v1.0.6.apk"
```

4. 署名済みAPKの証明書SHA-256と新キーストアの証明書を比較。**旧公開版のfingerprint `5a6955d60f416ca2ad93a8fbda52cbf33b6837912a26f7e24f3d1c326ffd8bb7`とは異なる**ことも確認する。APKのアプリID・版番号・`debuggable=false`・IMEサービス名を`aapt`や`apkanalyzer`で確認。未署名APKや中間のaligned APKを公開しない。
5. AQUOS R9 Proで**別のIMEを先に有効化**してから、旧署名KanaPromptをアンインストール。新署名のAPKをインストールしてKanaPromptを有効化・選択する。かなフリック、濁点、削除、カーソル、IME切替を実機で確認。**ライト／ダークの山吹色UI、押下文字と選択面の視認性、フリックガイド、山吹色アプリアイコンも確認する。**テスト用文字列に個人情報を使わない。
6. 全検証後、実際に公開するソースコミットを確定し、対応するソースZIPとSHA256SUMSを作る。APKとソースの整合性を検査する。README・CHANGELOG・Releaseには**署名変更のため再インストールが必要・山吹色UIへの変更**と明記。旧v1.0.5 Releaseは編集・上書きせず、独立したv1.0.6 Releaseを作成する。

## 公開を止める条件

- 新しい鍵の暗号化バックアップと復元可能性が確認できない。
- 秘密鍵・パスワードがGit、ZIP、ログ、Issueに含まれる疑いがある。
- 署名検証、ID・バージョン、実機IME入力のいずれかが未完了または不合格。
- 公開APKとソースの対応、SHA256SUMSの整合を確認できない。

参考：Android公式[アプリ署名](https://developer.android.com/studio/publish/app-signing)、[APKの署名と検証](https://developer.android.com/tools/apksigner)、[コマンドラインでのビルド](https://developer.android.com/build/building-cmdline)。