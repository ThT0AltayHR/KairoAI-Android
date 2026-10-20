#!/usr/bin/env bash
set -euo pipefail
command -v keytool >/dev/null || { echo 'JDK keytool bulunamadı. JDK 17 kur.' >&2; exit 1; }
OUT=${1:-android-release.jks}
if [[ -e "$OUT" ]]; then echo "Dosya zaten var: $OUT — üzerine yazmamak için işlem durdu." >&2; exit 1; fi
read -rsp 'Keystore parolası: ' STORE_PASS; echo
read -rsp 'Key parolası: ' KEY_PASS; echo
read -rp 'Key alias [kairo-release]: ' ALIAS
ALIAS=${ALIAS:-kairo-release}
keytool -genkeypair -v -keystore "$OUT" -storetype JKS -alias "$ALIAS" -keyalg RSA -keysize 4096 -validity 10000 -dname 'CN=Kairo AI, OU=Android, O=AltayHR & Muhammed, L=Turkey, C=TR' -storepass "$STORE_PASS" -keypass "$KEY_PASS"
chmod 600 "$OUT"
echo
printf 'ANDROID_SIGNING_KEY_BASE64: '; base64 < "$OUT" | tr -d '\n'; echo
printf 'ANDROID_KEY_ALIAS: %s\n' "$ALIAS"
echo 'GitHub Secrets: ANDROID_SIGNING_KEY_BASE64 için yukarıdaki base64 çıktısını; ANDROID_KEYSTORE_PASSWORD ve ANDROID_KEY_PASSWORD için oluştururken seçtiğin parolaları gir.'
echo 'Bu dosyayı, parolaları veya base64 anahtarını depoya commit etme. Keystore dosyasını güvenli bir yedek konumuna al; kaybedilirse aynı uygulama kimliğiyle güncelleme imzası atamazsın.'
