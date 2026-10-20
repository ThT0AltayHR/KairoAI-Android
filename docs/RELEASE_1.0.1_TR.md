# Kairo AI 1.0.1 — Yayın kontrol planı

## Kaynak

- Ürün: Kairo AI
- Paket: `com.altayhr.kairo`
- Varsayılan sürüm: `1.0.1`
- SDK: min 26 / compile-target 35
- Arayüz: Türkçe, Kotlin + Jetpack Compose
- Geliştiriciler: AltayHR & Muhammed

## Release öncesi

1. GitHub CI'da `testDebugUnitTest`, `lint`, `assembleDebug` başarılı olmalı.
2. Gerçek test cihazında ilk açılış izinleri, Gemini sohbeti, sesli sohbet, kapsül, çevrimiçi/çevrimdışı banner ve launcher widget denenmeli.
3. Spotify Client ID ve Redirect URI hesapta yapılandırılıp OAuth/çalma kontrolleri doğrulanmalı.
4. Sekiz ajan anahtar yuvası ve ortak güvenli kasa kaydetme/silme/yükleme senaryolarından geçirilmeli.
5. Yerel HTTP/n8n, GitHub Actions, Resend ve Notion eylemleri test hedeflerinde kontrollü denenmeli.
6. Gömülü terminal ekranında PTY ve `/system/bin/sh` gerçek cihazda denenmeli; `pwd`, başarısız komut, onay/reddetme, uzun çıktı ve kapsül akışı doğrulanmalı. Node/npm/Git/Gradle/Expo kullanıcı alanı ikililerinin bu pakette bulunmadığı unutulmamalı.

## İmzalı GitHub Release

`.github/workflows/release.yml` sürüm tag'iyle çalışır. Dört Actions Secret gerekir: `ANDROID_SIGNING_KEY_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Keystore kaynak kodda bulunmamalıdır. İş akışı APK ve AAB üretmeli, `apksigner verify` ve SHA-256 kontrolünden geçirmeli ve GitHub Release'e artefakt eklemelidir.

## Sunucusuz sınır

Cloudflare Worker/D1 veya kullanıcı sunucusu yoktur. Bu sürüm gelen genel webhook'ları internetten dinleyemez, cihaz kapalıyken remote cron çalıştıramaz ve cihaz kapalıyken CLI komutlarını yürütemez. Kairo içinde `/system/bin/sh` kabuğu vardır; Termux paket yöneticisi ile Node/npm/Git/Gradle/Expo/JDK kullanıcı alanı ikilileri bu pakete gömülmüş değildir. Yerel WorkManager zamanlaması Android işletim sistemi kısıtlarına tabidir.

Bu kontrol planı CI run'ı veya gerçek cihaz testinin yerine geçmez.
