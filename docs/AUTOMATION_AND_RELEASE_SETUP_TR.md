# Kairo AI 1.0.1 — Yerel otomasyon ve imzalı Android Release

Kairo AI bu sürümde Cloudflare Worker, D1 veritabanı veya kullanıcı tarafından yönetilecek bir sunucu içermez. İş akışları Android cihazda saklanır ve WorkManager ile yürütülür. Uzak servis eylemleri cihaz çevrimiçiyken doğrudan sağlayıcı HTTPS API'lerine gönderilir.

## 1. Yerel otomasyon modeli

Desteklenen eylem türleri:

- **HTTP isteği**: harici HTTPS uç noktasına `GET`, `POST`, `PUT`, `PATCH` veya `DELETE`.
- **n8n**: n8n webhook URL'sine giden dış istek. n8n kurulumu/çalışan endpoint sana aittir; Kairo kendi n8n sunucusunu barındırmaz.
- **GitHub Actions dispatch**: depo ve workflow üzerinde Actions dispatch yetkisi olan token ile workflow tetikleme.
- **Resend e-posta**: Resend API key ve doğrulanmış gönderen domain ile e-posta gönderme.
- **Notion sayfası**: Notion integration tokenı ve paylaşılan database ID ile sayfa oluşturma.

Tetikleyiciler cihazda `manual`, uygulama olayları ve yaklaşık WorkManager `schedule` biçimindedir. Zamanlanmış iş en az 15 dakikalık aralığı kabul eder; işletim sistemi pil/Doze/üretici kısıtları sebebiyle tam saat garantisi vermez. Cihaz kapanmışken veya zorla durdurma durumunda sürekli sunucu cron'u gibi çalışmaz. Genel HTTPS webhook listener **yoktur**; bunun için internete açık ve sürekli çalışan bir endpoint gerekir.

## 2. Güvenli Kasa

1. Uygulamayı açıp **Güvenli Kasa** bölümüne gir.
2. Bir anahtar etiketi oluştur (örn. `github-actions-readwrite`, `resend-main`, `notion-projects`).
3. Gizli tokenı değer alanına yaz; görünürlük düğmesiyle kontrol ettikten sonra kaydet.
4. Otomasyon akışında yalnızca kasa etiketini seç/yaz; tokenı prompt'a veya JSON başlığına kopyalama.
5. HTTP/n8n için `Authorization`, `api-key`, `token` ve benzeri gizli başlık değerleri düz metin yapılandırmasına kaydedilemez. Varsa tokenı Kasa etiketi alanına koy.

Kasa öğeleri Android Keystore'da tutulan AES-GCM anahtarı ile cihazda şifreli saklanır. Şifrelenmiş verinin geri alınabilmesi aynı uygulama kurulumuna/Keystore anahtarına bağlıdır. Telefonu sıfırlamadan veya uygulama verisini temizlemeden önce kritik kimlik bilgilerini güvenli parola yöneticisinde yedekle. Tokenı hiçbir koşulda Git commit'ine, issue'ya, log'a veya AI prompt'una koyma.

## 3. Spotify OAuth PKCE

1. Spotify Developer Dashboard'da bir uygulama oluştur ve Client ID'yi al.
2. Dashboard'da redirect olarak uygulama URI'sini **birebir** kaydet: `com.altayhr.kairo:/oauth2redirect`.
3. Kairo → Spotify ekranında Client ID'yi gir ve bağlan.
4. İstenen Spotify kapsamlarını onayla.
5. Bağlantıdan sonra müzik bilgisi/çalma kontrollerini dene. API'nin bazı oynatma denetimleri uygun bir aktif cihaz veya Premium hesabı gerektirebilir. Sağlayıcı hesabı erişim vermiyorsa uygulama hata durumunu gösterecektir.

Kairo herkese açık OAuth istemcisi olarak PKCE akışını kullanır; Spotify client secret'ı uygulamaya gömülmez. Başka sağlayıcılarda client secret zorunlu OAuth akışları yalnızca bir mobil client ile uygulanamayabilir. Böyle bir servis sunucu gerektiriyorsa bunu yerel OAuth ekranının çalışıyor gibi göstermesi yerine ayrıca bir backend gereksinimi olarak değerlendirmek gerekir.

## 4. GitHub Actions'ta derleme

Depoyu GitHub repository'sine yükleyin. `.github/workflows/android-ci.yml`, Java 17 ve Android SDK 35 kurup şu kontrolü yürütür:

```bash
gradle --no-daemon clean testDebugUnitTest lint assembleDebug
```

Android SDK / bağımlılık erişimi GitHub runner'ında sağlanmalıdır. Yerel Android Studio için Android SDK 35, JDK 17 ve Gradle 8.10.2 kullanın. Depoda Gradle Wrapper olmadığı için burada kullanılan `gradle` komutu CI tarafından sabit sürümle kurulmaktadır.

## 5. İmzalama anahtarı oluşturma

Güvenilir bir makinede JDK 17 ve `keytool` bulunmalı. Depo kökünde:

```bash
bash scripts/create-signing-key.sh kairo-release.jks
base64 < kairo-release.jks | tr -d '
'
```

Komut bir release keystore'u oluşturur ve Base64 metni verir. Dosyayı güvenli yerde yedekle; `.jks` dosyasını, parolaları veya Base64 değerini repository'ye commit etme. Aynı uygulamanın güncellemeleri aynı imza sertifikasıyla yayınlanmalıdır.

GitHub → **Settings → Secrets and variables → Actions** bölümünde dört Repository Secret oluştur:

- `ANDROID_SIGNING_KEY_BASE64` — keystore dosyasının Base64 içeriği
- `ANDROID_KEYSTORE_PASSWORD` — store parolası
- `ANDROID_KEY_ALIAS` — key alias adı
- `ANDROID_KEY_PASSWORD` — private key parolası

Ardından sürüm tag'i gönder:

```bash
git tag v1.0.1
git push origin v1.0.1
```

`.github/workflows/release.yml` tag formatını denetler, test/lint ve `assembleRelease bundleRelease` çalıştırır, APK imzasını `apksigner` ile doğrular, SHA-256 dosyası oluşturur ve APK/AAB/checksum'ı GitHub Release'e yükler. Bu iş akışının kaynakta bulunması başarılı bir release oluştuğu anlamına gelmez; Actions run'ının yeşil ve artefaktın indirilebilir olması gerekir.

## 6. Eylem ve güvenlik notları

- GitHub tokenını yalnızca hedef repository/workflow için gerekli en dar izinle oluştur.
- Resend gönderen adres/domain doğrulanmış olmalı. E-posta eylemi gerçek e-posta gönderir.
- Notion database önceden ilgili integration ile paylaşılmış olmalı.
- HTTP/n8n hedefi genel erişimli HTTPS olmalı; özel ağ/localhost hedefleri güvenlik için reddedilir.
- Başarı/hata mesajları HTTP durum kodunu gösterir; yanıt gövdesi veya gizli başlık loglanmaz.
- Dış API 401/403/429 veya schema hatası döndürürse ilgili token/kapsam/sürüm/plan koşullarını sağlayıcı hesabında düzelt.
- Telefon çevrimdışıyken dış HTTP eylemleri bekleyebilir veya hata verebilir. Android WorkManager yeniden deneme garantisi gerçek sağlayıcı işleminin exactly-once olduğu anlamına gelmez; e-posta ve dispatch gibi eylemlerde tekrarın etkisini düşün.

## 7. Dağıtımda ne doğrulanmalıdır?

Release öncesi Android CI'nın başarılı olduğundan emin olun. İlk gerçek imzalı artefaktı indirip checksum ve APK imzasını doğrulayın, test cihazına kurun, temel sohbet/izin/overlay/mikrofon/Spotify/WorkManager/yerel kasa kontrollerini gerçekleştirin. Bu kaynak paketi içinde keystore ve imzalı APK bulunmaz.
