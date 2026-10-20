# Kairo AI 1.0.2 — Widget kaynak denetimi

**Paket türü:** Kaynak kod ZIP'i; doğrudan gömülü terminal kaynakları dahil. APK değildir.  
**Sürüm:** 1.0.2  
**Geliştiriciler:** AltayHR & Muhammed

## Denetim kapsamı

- Kairo Android `applicationId` ve `namespace` değerleri.
- Sohbet/proje çalışma alanları, dosya envanteri yenileme ve terminal oturumu klasörleri.
- Termux kaynak arşivinden alınan Apache-2.0 lisanslı terminal-emulator ve terminal-view modülleri.
- Kairo içine yerleştirilmiş gerçek PTY/TerminalView ve `/system/bin/sh` oturumu.
- Gemini function-call tabanlı terminal komutu önerisi, açık kullanıcı onayı, tek kullanımlık sonuç tüketimi ve terminal günlüğü.
- AES-GCM/Android Keystore ile terminal komut geçmişi şifrelemesi; kapsül akışında hassas çıktıyı maskeleme.
- Komut politikası testleri, XML, SVG, YAML ve belge bağlantıları.
- 22 ayrı Android AI launcher widget sağlayıcısı, ayrı picker metadata XML kayıtları ve ortak siyah RemoteViews kartı.
- Ana ekran widget'ından uygulama içi widget kataloğuna, ardından widget'a özgü Gemini prompt görevine geçiş.

## Sonuçlar

| Kontrol | Sonuç |
|---|---|
| Android `applicationId` | `com.altayhr.kairo` olarak korundu |
| Android `namespace` | `com.altayhr.kairo` olarak korundu |
| App manifest'te Termux `Application`/servis | Bulunmadı |
| `termux-core` / `termux-shared` dahil edilmesi | Hayır |
| Gömülü PTY sınıfları | `TerminalSession`, `TerminalView` kaynakları mevcut |
| Shell yürütme | Kairo UID altında `/system/bin/sh`; kullanıcı onaylı komut çalıştırıcısı |
| Terminal komut politikası bağımsız JVM smoke testi (`scripts/test_terminal_policy.sh`) | **25/25 geçti** |
| Android XML kaynakları | **45/45 ayrıştırıldı** |
| SVG kaynakları | **7/7 ayrıştırıldı** |
| GitHub Actions YAML | **2/2 ayrıştırıldı** |
| Uygulama Kotlin kaynakları | 62 dosya |
| Gömülü terminal kitaplığı | 17 emulator ve 12 view modül kaynağı |
| Uygulama içi AI widget kartı | 22 benzersiz kart |
| Android launcher widget türü | 22 AI sağlayıcısı + 1 genel widget = 23 tür |
| Widget manifest / metadata / katalog tutarlılık testi (`scripts/verify_widget_catalog.py`) | **Başarılı** — 22 benzersiz sağlayıcı, metadata, etiket ve AI rota bağlantısı |
| Release anahtarı script'i (`bash -n`) | Başarılı |
| Termux sabit yolu ve Kairo private-path reddi | Politika senaryolarında engellenir |
| Tırnaklı `$HOME` / `${HOME}` recursive delete denemesi | Politika senaryolarında engellenir |
| API/GitHub token biçimleri ve hassas environment assignment | Politika senaryolarında engellenir |
| Apache 2.0 lisans dosyaları | İki modüle ve üçüncü taraf bildirimine eklendi |
| İmza anahtarı / kullanıcı API token'ı pakette | Yok |

## Paket kimliği ve bildirim davranışı

Kairo `com.altayhr.kairo` kimliğiyle kalır. Termux uygulaması, Termux servisleri veya Termux bildirimleri başlatılmaz. `terminal-emulator` ve `terminal-view`, Android uygulaması içinde kullanılan kitaplık modülleridir; yalnızca bu modüllerin PTY terminal emülasyonu ve görünüm katmanı alınmıştır.

## Ne gerçekten çalıştırılır?

Entegre edilmiş görünüm, yerel PTY üzerinden Android'in `/system/bin/sh` kabuğunu açar. AI bir komut önerebilir, kullanıcı onaylayabilir ve ayrı komut çalıştırıcısı gerçek stdout/stderr ile çıkış kodunu kaydeder. Komutların geçmişi cihazda Android Keystore anahtarıyla AES-GCM şifrelemesi altında saklanır. Çalışma dizini seçili sohbetin çalışma alanı veya bağlı proje klasörüdür; çıktıdan sonra dosya envanteri yenilenir.

**Bu henüz tam Termux kullanıcı alanı değildir.** Yüklenen `.tar.gz`, Termux uygulamasının kaynak arşividir; Node.js, npm, Git, Gradle, JDK, Expo/EAS veya `pkg`/`apt` ikililerini hazır uygulama içi paketler olarak sağlamaz. Bu araçların bulunmaması durumunda terminal başarılıymış gibi davranmaz. Bunları `com.altayhr.kairo` önekine uygun şekilde derlemek ve paketlemek ayrı bir işçilik gerektirir.

Komut politikası için paket içi smoke testi `scripts/test_terminal_policy.sh` tarafından yürütülür; son çalıştırmada 25/25 örnek geçti. Komut politikası savunma amaçlı bir filtredir; Android UID'li shell için OS seviyesinde container/sandbox değildir. Kullanıcı tüm komutları kontrol edip açıkça onaylamalıdır. Kabuk Kairo'nun normal uygulama yetkileri ile çalışır; root istemez.

## Widget güncellemesi ve Android derlemesi: doğrulanmamış

Bu widget güncellemesinde XML/manifest/katalog tutarlılığı statik olarak doğrulanabilir; fakat bu çalışma ortamında Android SDK/platform `android.jar` ve kullanılabilir Gradle kurulumu bulunmadığından `assembleDebug`, `assembleRelease`, `lint`, ADB/emülatör veya gerçek cihaz testi yapılmadı. Bu nedenle **APK derlendi/cihazda çalışıyor/hatasız** iddiası yapılmıyor. Kaynak derleme doğrulaması, CI workflow'ları ile GitHub üzerinde gerçekleştirilmelidir.

`.github/workflows/android-ci.yml` testi, lint'i ve debug paketini; `.github/workflows/release.yml` sürüm etiketinden imzalı APK/AAB ve checksum oluşturmayı hedefler. Sürüm iş akışı için imza secret'ları gereklidir. Başarılı CI çalışması görülmeden bu kaynak paketi derlenmiş APK yerine geçmez.

## Özellikle atlanan iddialar

- Telefon kapalıyken çalışan sunucu/harici webhook hizmeti yoktur.
- Gerçek npm, Git, Gradle veya Expo ikililerinin bu ZIP içinde bulunduğu iddia edilmez.
- Android SDK/Gradle derlemesi veya cihaz testi yapılmış gibi gösterilmez.
- Kaynak denetimi tüm olası Android cihazlarında hatasızlığı garanti etmez.
