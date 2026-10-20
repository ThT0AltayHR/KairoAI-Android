# Kairo AI — Kalite ve kabul kontrol listesi

Bu liste doğrulanması gereken kontrollerdir; kutular test gerçekten yapıldığında işaretlenmelidir. Kaynak ZIP'in bulunması başarılı bir cihaz testi anlamına gelmez.

## Statik kaynak kontrolleri
- [ ] XML dosyaları ve AndroidManifest açılır/parçalanabilir.
- [ ] Bütün `R.drawable`, `R.string`, layout ve launcher-widget kaynakları mevcut.
- [ ] Kotlin dosyalarının importları ve Compose çağrıları Android Gradle build tarafından doğrulanır.
- [ ] Gradle bağımlılık çözümlemesi başarılı; `lint` sıfır kritik hata verir.
- [ ] Unit testler ve `assembleDebug` başarılı.
- [ ] En az bir emülatör veya fiziksel Android cihazda kurulum ve ilk açılış testi yapılır.

## İlk açılış ve izinler
- [ ] Uygulama ilk açılışta izinlerin nedenini Türkçe açıklar.
- [ ] Bildirim izni kabul/red/sonradan verme senaryoları kontrol edilir.
- [ ] Overlay izni reddedildiğinde uygulama çökmez ve doğru sistem sayfasına yönlendirir.
- [ ] Mikrofon izni olmadan sesli oturum başlatılmaz.
- [ ] Pil optimizasyonu ayarına yönlendirme cihaz üreticisinin ekranında denenir; izin verildiği varsayılmaz.
- [ ] Kullanıcı izinleri reddetse bile sohbet ve temel ekranlar kullanılabilir.

## Sohbet/veri kalıcılığı
- [ ] Yeni sohbet, sabitleme, yeniden adlandırma, silme ve uygulama yeniden açma.
- [ ] Her sohbetin dosyaları diğer sohbetlerin alanlarıyla karışmaz.
- [ ] Proje ortak dosyaları sohbet dosyalarından ayrı kalır.
- [ ] Çıktı dosyası belge seçicisiyle hedef klasöre yazılır ve açılabilir.
- [ ] JSON/Preferences güncellemelerinde bozuk veri okunurken uygulama kapanmaz.
- [ ] Uygulamanın kaldırılması/veri temizlemesinin yerel veriyi sileceği kullanıcılara açıklanır.

## Gemini ve ajanlar
- [ ] En az bir genel Gemini anahtarı ekleme, göster/gizle ve silme.
- [ ] Sekiz ajan anahtar yuvasının her birini kaydetme/silme; anahtar UI dışındaki log ve prompt'larda görünmüyor.
- [ ] Geçersiz/iptal edilmiş API anahtarı için anlaşılır hata gösterilir.
- [ ] Ana ajan yanıtı, uzman rol hata verdiğinde de mümkünse devam eder.
- [ ] AI widget kartları doğru prompt'u görev hattına gönderir; görev çalışırken çakışan başlat düğmeleri devre dışıdır.
- [ ] Android widget seçicisinde 22 ayrı AI widget türü ve genel durum widget'ı görünür.
- [ ] Her launcher widget'ının `AÇ` ve `AI İLE BAŞLAT` düğmeleri doğru rotayı/prompt'u taşır.
- [ ] Ağ widget'ı Android bağlantı durumunu, Spotify widget'ı gerçek kayıtlı oynatma bilgilerini gösterir.
- [ ] “ÖNERİ” adımları gerçek shell sonucu gibi gösterilmez.
- [ ] Model/akıl yürütme düzeyi seçeneğinin kullanılan model tarafından desteklenmesi kontrol edilir.

## Dinamik kapsül ve ağ
- [ ] Overlay izinli/izinsiz durumu, kapsül açma/kapatma, sürükleme ve geometri kaydı.
- [ ] Üç saniyelik basılı tutma sayacı/altın iz doğru zamanda tamamlanır; bırakınca sıfırlanır.
- [ ] Dinamik kapsül ekranın farklı yoğunluklarında ekran dışına sürüklenemez.
- [ ] Görev, hata, başarı, Spotify ve offline/online durumları birbirinin içeriğini yanlışlıkla ezmez.
- [ ] Ağ kesilince kırmızı çevrimdışı durumu; bağlantı dönünce yeşil durum bir kez gösterilir.
- [ ] Arka plan servis/bildirim davranışı Android sürümlerinde ve en az bir üretici cihazında denenir.
- [ ] Uygulama zorla durdurulduğunda Android kısıtları nedeniyle devam etmeyebilecek görevler açıkça belirtilir.

## Spotify
- [ ] Spotify Developer Dashboard'da Client ID ve birebir Redirect URI yapılandırılır.
- [ ] PKCE/state doğrulama başarılı; iptal/bozuk callback token oluşturmaz.
- [ ] Albüm kapağı yüklenmezse yer tutucu görünür, ekran çökmez.
- [ ] Oynat/duraklat/önceki/sonraki/shuffle/repeat gerçek cihaz ve gerekli hesap koşullarıyla test edilir.
- [ ] 401/403/429 yanıtlarında açıklayıcı durum gösterilir; erişim tokenı log'a yazılmaz.
- [ ] Kapsül ve launcher widget'ındaki son bilinen parça bilgisinin güncelleme gecikmesi açıklanır.

## Yerel otomasyon ve gizli kasa
- [ ] Gizli kasa öğesi kaydet/göster/gizle/sil ve uygulamayı yeniden başlatma.
- [ ] HTTP/n8n başlıkları geçerli JSON olmalı; `Authorization`, `api-key`, token/secret değerleri düz metin başlıklara kaydedilemez.
- [ ] HTTPS dış hedef doğrulaması ve hatalı URL reddi.
- [ ] GitHub Actions dispatch tokenı ve repository/workflow/ref hedefi doğrulanır.
- [ ] GitHub workflow inputs JSON doğru `inputs` alanı olarak gönderilir.
- [ ] Resend ve Notion hedefleri test hesaplarında kontrollü eylemle doğrulanır.
- [ ] Başarılı/başarısız HTTP sonucu ve retry davranışı doğru yazılır; response body/token loglanmaz.
- [ ] WorkManager zamanlaması yaklaşık aralıkla çalışır; cihaz çevrimdışı/Doze/kapanmış durumları denenir.
- [ ] Uygulama kapalıyken dışarıdan inbound webhook alamadığı kullanıcıya açıkça anlatılır.

## Release / imza
- [ ] `.github/workflows/android-ci.yml` gerçek GitHub Actions koşusunda başarılı.
- [ ] İmza anahtarı GitHub Actions Secrets içinde; kaynakta veya ZIP'te JKS/parola yok.
- [ ] Tag sürümü Gradle versionName/versionCode ile uyumlu.
- [ ] Release iş akışı test, lint, APK/AAB derlemesi ve apksigner doğrulamasını geçirir.
- [ ] Release artefakt SHA-256 checksum'ları doğrulanır.
- [ ] APK'yı bir test cihazına kurma, güncelleme üstüne kurma ve açılış testi tamamlanır.

## Bu paketteki ortam sınırı
Tam Gradle/Android uygulama derlemesi ve fiziksel cihaz testi bu paket hazırlanırken yapılamadı. Bu listeyi otomatik olarak tamamlanmış saymayın; CI gerçek derleme/cihaz testi sonuçlarını doğrulamak için kullanılmalıdır.

## Gömülü terminal ve AI komutları
- [ ] Kairo `applicationId` ve `namespace` değerlerinin `com.altayhr.kairo` kaldığı APK manifestinden doğrulanır.
- [ ] Uygulama manifestinde Termux `Application`, Termux servisi, Termux bildirim kimliği veya harici uygulama intent'i bulunmadığı doğrulanır.
- [ ] Terminal ekranında etkileşimli gerçek `/system/bin/sh` PTY açılır; `pwd`, `echo`, `ls -la` çıktıları doğru görünür.
- [ ] Normal sohbet oturumlarının çalışma dizinleri ayrıdır; proje sohbetleri aynı proje dizinini kullanır.
- [ ] AI'nin önerdiği komut `approval_required` durumunda kalır ve kullanıcı onayından önce yürütülmez.
- [ ] Komutu reddetmek hiçbir alt süreç başlatmaz.
- [ ] Onaydan sonra gerçek stdout, stderr, çıkış kodu ve başarılı/hatalı/zaman aşımı durumu kaydedilir.
- [ ] Aynı onay kaydına çift dokunma aynı komutu iki kez çalıştırmaz.
- [ ] Kasıtlı başarısız komut (ör. `command_that_does_not_exist`) başarı gibi gösterilmez.
- [ ] 900 saniyeyi geçen süreçler timeout olarak raporlanır; uygulama yanıt vermeyi sürdürür.
- [ ] Aşırı uzun stdout/stderr bellek sınırlamasına tabi tutulur; süreç boruları çıktı tavanına ulaştıktan sonra da boşaltılır.
- [ ] GitHub, Google ve genel API key biçimleri kapsül/komut çıktısı günlüğünde maskelenir.
- [ ] Komut geçmişi `SecureApiKeyStore`/Android Keystore üzerinden AES-GCM ile şifreli saklanır; düz metin preferences alanı kullanılmaz.
- [ ] `sudo`, `su`, cihaz/yönetim işlemleri, Kairo private veri yolları, Termux sabit yolları, `../` gezinmesi, `rm -rf /`, tırnaklı `$HOME`/`${HOME}` ve curl/wget pipe-to-shell denemeleri engellenir.
- [ ] Komut widget'ı sadece gerçek kayıt durumlarını gösterir; komut çalışmadıysa başarılı/çıktı alınmış gibi göstermez.
- [ ] Terminal güncellemesi kapsüle iletilir; hassas satırlar maskelenir ve hata/başarı durumu ayırt edilir.
- [ ] Komut sonrası dosya envanteri yenilenir, proje/sohbet dosya listesi doğru kalır.
- [ ] Terminal uygulama UID'siyle çalıştığı için bunun OS sandbox olmadığı kullanıcıya açıklanır.
- [ ] Node/npm/Git/Gradle/Expo gibi ikililer bulunmadığında gerçek eksik-komut hatası görülür; hazır paket oldukları iddia edilmez.
- [ ] İnteraktif shell, uygulamayı arka plana alma, ekranı döndürme, oturum kapatma/açma ve bellek baskısı gerçek cihazda denenir.

## Üçüncü taraf lisanslar
- [ ] Gömülü `terminal-emulator` ve `terminal-view` kaynaklarının Apache 2.0 metinleri paket içinde bulunur.
- [ ] Root Kairo lisansının ayrı tutulduğu ve yalnızca belirtilen modül istisnalarının Apache 2.0 olduğu belgelenir.

