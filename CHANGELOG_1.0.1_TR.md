# Kairo AI 1.0.1 — Gömülü terminal sürümü

## Öne çıkanlar

- Kairo'nun mevcut `applicationId` ve `namespace` değeri `com.altayhr.kairo` olarak korunur.
- Termux uygulamasını başlatan intent, Termux servisi, ayrı Termux bildirimi ve Termux uygulamasına ait manifest `Application` sınıfı kullanılmaz.
- Termux kaynak arşivinin Apache-2.0 lisanslı `terminal-emulator` ve `terminal-view` modülleri Kairo içinde yer alır; yerel PTY üzerinden gerçek `/system/bin/sh` kabuğu açılır.
- Sohbet için ayrı çalışma dizini; proje sohbetleri için paylaşılan proje kökü; her oturum için ayrı HOME/TMPDIR bulunur.
- AI, Gemini function calling üzerinden tek tek terminal komutu önerebilir. Her komut açık kullanıcı onayı ister; gerçek STDOUT/STDERR, çıkış kodu ve durum kaydedilir.
- Komut günlüğü Android Keystore anahtarıyla AES-GCM şifrelemesi altında saklanır. Ekran görüntüsü ve kapsül güncellemesinde yaygın API token biçimleri maskelenir.
- Komut widget'ı artık öneri metinleri yerine gerçek terminal olaylarını ve çıktı özetini gösterir; kapsüle canlı, maskelenmiş durum satırları gönderilir.
- Komut sonrasında çalışma alanının dosya envanteri yenilenir. Başarısız / engellenen komutlar görünür kalır.
- Shell yetkisi Kairo'nun normal uygulama UID'sidir; root veya ayrıcalık yükseltme yoktur.
- Komut politika testleri 25 senaryo üzerinden bağımsız JVM smoke testinde geçti.
- Apache-2.0 üçüncü taraf bildirimleri ve lisans metinleri eklendi.
- Son kaynak kalite raporu ve terminal kabul listesi güncellendi.

## Bilinen sınırlamalar

- Bu paket kaynak ZIP'idir; bu ortamda Android SDK/Gradle bulunmadığı için APK derlenmedi ve gerçek cihaz testi yapılmadı.
- Node.js, npm, Git, Gradle, JDK, Expo/EAS ve `pkg`/`apt` gibi araçların Linux kullanıcı alanı ikilileri bu Termux kaynak arşivinde yoktur; PATH'te bulunmayan araçlar gerçek `command not found` hatası verebilir. Terminal emülatörü modülünü gömmek tek başına bu derleyici/çalıştırıcıları paketlemez.
- Gömülü kabuk Kairo'nun Android uygulama kimliği ve normal uygulama izinleriyle çalışır; bu bir OS/container sandbox değildir. Komutlar inceleme/onaydan sonra çalıştırılmalıdır.
- İnteraktif PTY oturumu Android'in süreç yaşam döngüsü ve bellek kısıtlarına tabidir.
