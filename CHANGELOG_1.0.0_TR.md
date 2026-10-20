# Kairo AI 1.0.0 — Değişiklik günlüğü

## Eklenenler

- Spotify OAuth PKCE bağlantı ekranı ve Web API oynatma durumu.
- Parça/artist/album bilgisi, albüm kapak görseli, oynat/duraklat, önceki/sonraki, shuffle ve repeat kontrolleri.
- Spotify'ın son bilinen parçasını ana ekran widget'ında gösterme ve kapsüle müzik durumunu aktarma.
- Sohbetlerden bağımsız, tüm projeler için ortak güvenli kasa ekranı.
- AES-GCM/Android Keystore üzerinden saklanan sekiz Gemini API key yuvası ve ajan rol ekranı.
- Ağ bağlantısı değişiminde çevrimiçi/çevrimdışı banner; kapsül ve widget güncelleme sinyalleri.
- GitHub, Spotify ve Expo için özel SVG kaynakları.
- Türkçe kapsamlı README ve tam GPL-3.0 lisans metni.
- GitHub Actions'ta Gradle Wrapper JAR bağımlılığı kaldırılıp sabit Gradle 8.10.2 kurulumu eklendi.
- 22 kartlık AI widget merkezi ve ağaç biçiminde komut önizleme/timeline widget'ı. Gerçek terminal çıktısı ile önerilen komutlar görsel olarak ayrılır.
- Cloudflare Worker/D1 ve barındırılan otomasyon katmanı kaynak ağaçtan kaldırıldı; otomasyonlar Android cihazda WorkManager ile çalışır.
- Spotify kapsül sayfasına özel yeşil vektör simgesi, bağlantı durumu, bağlan/kontrolleri aç eylemi ve müzik durumu yayımlama akışı.
- Çevrimiçi/çevrimdışı olaylarında kapsül/widget durumu yenileme.
- Entegrasyon otomasyonlarında gizli Authorization/API key başlıklarının düz metin JSON'a yazılmasını engelleme.
- Gradle varsayılan sürümü 1.0.0 / versionCode 1000000 olarak yükseltildi.

## Sınırlar

- Bu kaynak paketi gerçek Spotify/Gemini hesaplarıyla cihazda test edilmiş APK değildir.
- Spotify doğal dil komut adaptörü yaygın Türkçe oynatma/atlama/karıştırma/tekrar niyetlerini işler; Spotify API izinleri ve aktif cihaz koşullarına bağlıdır.
- Sekiz anahtar yuvası ve roller tanımlıdır; bağımsız, paralel alt ajan yürütücüsü terminal entegrasyonu ile tamamlanacaktır.
- Gerçek PTY/shell/komut yürütme ve build loglarından canlı ilerleme terminal entegrasyonu aşamasına bırakılmıştır.
- GNU GPL-3.0 kodun lisans koşulları içinde değiştirilmesine izin verir; değiştirmeyi yasaklama şartı GPL ile bağdaşmaz.
