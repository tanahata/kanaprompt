#!/usr/bin/env bash
# Build, install and launch the disposable input host. Does not change the active IME.
set -euo pipefail
cd "$(dirname "$0")/../.."

if ! command -v adb >/dev/null 2>&1; then
  echo "adbが見つかりません。Android SDK platform-toolsを確認してください。" >&2
  exit 1
fi
if ! adb get-state 2>/dev/null | grep -qx device; then
  echo "利用可能なAndroid端末が1台接続・認証されているか、adb devicesで確認してください。" >&2
  exit 1
fi

./gradlew -p checks/offset-probe --no-daemon assembleDebug
apk=$(find checks/offset-probe/build/outputs/apk/debug -name '*-debug.apk' -type f -print -quit)
if [ -z "$apk" ] || [ ! -s "$apk" ]; then
  echo "検証用APKが見つかりません。" >&2
  exit 1
fi

adb install -r --no-incremental "$apk"
adb shell am start -n jp.kanaflick.offsetprobe/.ProbeActivity

echo "検証ホストを起動しました。KanaPromptがPR #5試験版であることを確認し、カーソル101→103、103→101を試してください。"
echo "テスト後の削除: adb uninstall jp.kanaflick.offsetprobe"
