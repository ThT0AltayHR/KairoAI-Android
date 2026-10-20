<div align="center">

<img src="design/svg/kairo-orbit.svg" alt="Kairo AI orbit mark" width="140" />

# KAIRO AI

### Your AI workspace. Your projects. Your command timeline.

**Türkçe arayüz · Android · Gemini · Spotify · 22 AI widget kartı · 22 ayrı ana ekran widget'ı · güvenli kasa · yerel otomasyon**

[![Android CI workflow included](https://img.shields.io/badge/Android%20CI-workflow%20included-3DAD70)](.github/workflows/android-ci.yml)
[![Signed Release workflow included](https://img.shields.io/badge/Signed%20Release-GitHub%20Actions-8a7bff)](.github/workflows/release.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Built with Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-min%20SDK%2026-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)

**Geliştiriciler:** AltayHR & Muhammed  
**Resmî GitHub:** [THT0AltayHR](https://github.com/THT0AltayHR)  
**İletişim:** Telegram [@AltayHR](https://t.me/AltayHR)

**Kairo'yu faydalı buldun mu?** Resmî hesabın depolarını burada bulabilir ve yayınlandıktan sonra Kairo AI reposuna yıldız verebilirsin: **[★ THT0AltayHR GitHub depoları](https://github.com/THT0AltayHR?tab=repositories)**

</div>

---

> [!NOTE]
> Kairo AI, Android'e özgü yeniden yazılmış bir uygulamadır; macOS pencerelerinin piksel piksel kopyası değildir. Android'de izinler, bildirimler, ön plan servisleri ve launcher widget'ları ile eşdeğer iş akışları kurar. Kairo içinde Termux kaynaklarından lisans-uyumlu terminal emülatörü/PTY ve Android sistem kabuğu bulunur. AI komutları gerçek shell üzerinde çalıştırılmadan önce kullanıcı onayına sunulur; komut günlüğü gerçek çıkış kodu ve stdout/stderr bilgisini kaydeder. Node/npm/Git/Gradle/Expo gibi Linux geliştirme araçları bu kaynak arşivinde henüz paketlenmiş değildir.

> [!IMPORTANT]
> Bu paket, `THT0AltayHR` hesabına henüz yüklenmemiş bir kaynak arşividir; CI rozetleri yerel workflow dosyalarını işaret eder. Repoyu GitHub’a yükleyip `KairoAI` adını verdiğinde, GitHub Actions durumunu gerçek rozetlerle değiştirebilirsin. Bu kaynak paketi ve GitHub Actions release akışını içerir; bu ZIP'in içinde imzalı bir APK olduğu iddia edilmez. Gerçek sürüm etiketi ile imzalı APK/AAB üretmek için GitHub Secrets içinde kişisel Android imza anahtarını tanımlamak gerekir. Bu çalışma ortamında tam Android SDK/Gradle derlemesi çalıştırılamadı; CI gerçek derleme, test ve imza kontrolü için dağıtım kapısıdır.

## İçindekiler

- [Proje nedir?](#proje-nedir)
- [Öne çıkan özellikler](#öne-çıkan-özellikler)
- [22 AI widget kartı](#22-ai-widget-kartı)
- [Komut önizleme widget'ı](#komut-önizleme-widgetı)
- [Gömülü terminal ve AI komutları](#gömülü-terminal-ve-ai-komutları)
- [Görev yaşam döngüsü](#görev-yaşam-döngüsü)
- [Ajan filosu](#ajan-filosu)
- [Sohbet ve proje alanları](#sohbet-ve-proje-alanları)
- [Dinamik etkinlik kapsülü](#dinamik-etkinlik-kapsülü)
- [Spotify](#spotify)
- [Güvenli kasa](#güvenli-kasa)
- [Sunucusuz yerel otomasyon](#sunucusuz-yerel-otomasyon)
- [Çevrimdışı davranışı](#çevrimdışı-davranışı)
- [Android ana ekran widget'ı](#android-ana-ekran-widgetı)
- [Gemini ve canlı ses](#gemini-ve-canlı-ses)
- [Dosya kartları ve dışa aktarma](#dosya-kartları-ve-dışa-aktarma)
- [Ayar kategorileri](#ayar-kategorileri)
- [Güvenlik modeli](#güvenlik-modeli)
- [Teknik mimari](#teknik-mimari)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [GitHub Actions CI ve imzalı Release](#github-actions-ci-ve-imzalı-release)
- [İzinler](#izinler)
- [Veri saklama ve yedekleme](#veri-saklama-ve-yedekleme)
- [Kaynak dosya haritası](#kaynak-dosya-haritası)
- [Kalite ve test yaklaşımı](#kalite-ve-test-yaklaşımı)
- [Bilinen sınırlar](#bilinen-sınırlar)
- [Sık sorulan sorular](#sık-sorulan-sorular)
- [Katkı, yıldız ve iletişim](#katkı-yıldız-ve-iletişim)
- [Lisans](#lisans)
- [Üçüncü taraf lisansları](#üçüncü-taraf-lisansları)

## Proje nedir?

Kairo AI, sohbeti tek başına bir metin alanı olarak ele almaz. Her sohbetin kendi mesaj geçmişi ve dosya alanı vardır; proje alanında birden fazla sohbetin paylaşabileceği dosyalar saklanabilir. Görev merkezi, sekiz ajan rolü, kalıcı görev kayıtları, dinamik etkinlik kapsülü ve 22 AI destekli kontrol kartı aynı Gemini görev hattına bağlanır.

Uygulamanın görsel yönü grafit/siyah yüzeyler, yumuşak sınırlı paneller, özel SVG işaretler, Mochi durum görselleri, dikkat dağıtmayan mikro animasyonlar ve okunabilir tipografi etrafında tasarlanır. Uygulama arayüzü Türkçedir. Kod bloklarında dilin kendi söz dizimi korunur.

| Kimlik | Değer |
|---|---|
| Ürün | Kairo AI |
| Geliştirici kredisi | AltayHR & Muhammed |
| Android `applicationId` | `com.altayhr.kairo` |
| Varsayılan sürüm | `1.0.2` |
| Android `minSdk` | 26 |
| `targetSdk` / `compileSdk` | 35 |
| JDK hedefi | 17 |
| UI | Kotlin + Jetpack Compose + Material 3 |
| AI | Gemini API; API anahtarı kullanıcı tarafından sağlanır |
| Yerel arka plan işleri | Android WorkManager ve açık kullanıcı eylemiyle başlatılan ön plan servisleri |
| Web/backend zorunluluğu | **Yok** |
| Terminal yürütmesi | Kairo içi Termux Terminal Emulator/PTY + `/system/bin/sh`; AI komut kuyruğu, onay, gerçek çıktı ve şifreli geçmiş |

## Öne çıkan özellikler

- Türkçe koyu arayüz ve özel SVG/vektör varlıkları.
- Animasyonlu Kairo açılış ekranı; splash görünümünde uygulama launcher ikonunu dev bir logo olarak göstermek yerine marka işareti ve hareketli görsel kullanılır.
- Gemini sohbeti, model tercihi, akıl yürütme seçeneği ve web kaynaklarını destekleyen yanıt akışı.
- Kalıcı sohbetler, sabitleme, yeniden adlandırma, silme ve arama.
- Projeler, her sohbet için ayrı dosya alanı ve projeye ait ortak dosya alanı.
- Kod ve metin çıktılarında dosya kartları; Android belge sağlayıcısı ile hedef klasöre kaydetme.
- Sekiz rol için API anahtarı yuvası olan ajan filosu.
- Görev aşamalarının yerel günlüğü; gerçek çalıştırılmış eylem ile öneri/plan ayrı gösterilir.
- 22 AI widget kartı; her kart kendi bağlamına uygun prompt'u Gemini sohbet hattına yollar.
- Kairo içine gömülü terminal emülatörü ve gerçek PTY/kabuk; her sohbet için bağımsız kabuk oturumu ve proje/sohbet dosyalarına bağlı çalışma dizini.
- AI terminal aracı tek komut önerir; komut politika denetimi ve kullanıcı onayıyla gerçek shell yürütmesi yapılır; stdout/stderr ve çıkış kodu kaydedilir.
- Komut akışı widget'ı, gerçek terminal kayıtlarını `|_`, `|__`, `|___` ağaç görünümünde gösterir; öneri ile tamamlanmış sonuç birbirinden ayrıdır.
- Üç saniye basılı tutma akışına sahip yüzen dinamik kapsül; altın ilerleme izi ve Mochi durumları.
- Spotify OAuth PKCE, parça bilgisi, albüm kapağı, önceki/sonraki, oynat/duraklat, shuffle ve tekrar kontrolleri.
- Ortak güvenli kasa; şifreli API anahtarları ve token referansları.
- Cihaz içinde çalışan yerel otomasyon: manuel, yerel olay ve zamanlanmış işler.
- HTTP/n8n, GitHub Actions dispatch, Resend ve Notion için doğrudan API eylemleri.
- Bağlantı kaybı ve geri gelmesi durumunda çevrimiçi/çevrimdışı UI durumu.
- Android ana ekran widget'ı ve hızlı rota butonları.
- Notlar, e-posta taslakları, WhatsApp mesaj taslağı, cihaz durumu, kripto fiyatı/yerel portföy ve Flow yardımcı araçları.
- GitHub Actions ile birim testleri, lint, debug APK ve tag tabanlı imzalı release artefaktı akışı.

## 22 AI widget kartı

Widget merkezi ekranındaki 22 kartın her biri AI bağlantılıdır. Bir kartın **AI ile çalıştır** düğmesi kartın amacını ve varsa son görev bağlamını bir göreve dönüştürüp ana Gemini görev kuyruğuna gönderir. Bir görev devam ederken kart düğmeleri devre dışı kalır; kullanıcıya ajanın meşgul olduğu açıklanır. Kartın içerdiği komutlar/iş adımları “öneri” olabilir; AI tarafından metin olarak önerilmesi komutun cihazda yürütüldüğü anlamına gelmez.

| No. | Widget | Amaç | AI çıktısı |
|---:|---|---|---|
| 1 | Komut akışı | Komutları ağaç görünümünde sıralama | Komut önizlemesi, ön koşullar ve risk |
| 2 | Proje ilerlemesi | Kilometre taşlarını izleme | Kabul ölçütleri ve bağımlılıklar |
| 3 | Ajan filosu | Alt görevlere ayırma | Rol/girdi/teslim listesi |
| 4 | Gradle derlemesi | Android build hazırlığı | SDK, JDK, test, lint ve imza planı |
| 5 | NPM iş akışı | Node tabanlı projeler | Script'lere bağlı npm komut önerileri |
| 6 | GitHub Release | Release pipeline | Tag, imza, SHA-256 ve asset kontrol listesi |
| 7 | Expo build | Expo/EAS planı | Profil, credentials ve çıktı kontrolü |
| 8 | İndirme takibi | İndirme güvenliği | Byte/ yüzde ölçümü, checksum ve hedef |
| 9 | Dosya denetimi | Referans/manifest kontrolü | Eksik dosya, import ve kaynak raporu |
| 10 | Hata ayıklama | Kök neden arama | Hipotez, kanıt ve regresyon testi |
| 11 | Test laboratuvarı | Test matrisi | Birim, entegrasyon, izin ve regresyon adımları |
| 12 | Güvenlik nöbetçisi | Secret ve ağ güvenliği | Risk listesi ve azaltım önerileri |
| 13 | Kod incelemesi | Mimari ve kalite | Öncelikli kusur ve patch önerileri |
| 14 | Entegrasyon durumu | Servis API eşlemesi | Kimlik doğrulama, kapsam ve sınırlar |
| 15 | Spotify canlı durumu | Medya bağlantısı | OAuth/cihaz/oynatma hata teşhisi |
| 16 | Ağ ve çevrimdışı | İnternet kesintisi | Kuyruk, tekrar deneme ve geri bağlanma planı |
| 17 | Bağlam penceresi | Model bağlamı | Gerekli dosya, eksik bilgi ve tahmin |
| 18 | İstem atölyesi | İsteği görev planına dönüştürme | Hedef, kapsam, kabul ölçütü ve risk |
| 19 | Onay kuyruğu | Riskli işlerin onayı | Etki, geri alma ve açık onay kapıları |
| 20 | Teslim kartları | Dosya/artefakt hazırlığı | Ad, uzantı, hedef ve doğrulama alanları |
| 21 | Etkinlik günlüğü | Ajan durumunun sıralanması | Son olaylar ve durum özeti |
| 22 | Proje bütünlüğü | Dosya/proje alanlarının ayrımı | Yol, uzantı ve bağlam denetimi |

### Widget durumlarının anlamı

- **HAZIR:** Widget bir görevi başlatabilecek durumda.
- **ÇALIŞIYOR:** Uygulamanın görev hattında bir çalışma sürüyor; yeni widget başlatma düğmeleri kilitlenir.
- **SON: OK:** Son kaydedilmiş ajan görevi başarılı yanıt durumuna ulaştı. Bu, önerilen Gradle/terminal komutlarının yürütüldüğü anlamına gelmez.
- **SON: HATA:** Son kaydedilmiş ajan görevinde hata kaydedildi.
- **ÖNERİ:** AI tarafından önerilmiş adım; henüz bir komut yürütücüsüne bağlanmamış olabilir.

Bu ayrım, kullanıcıya sahte “npm success”, “Gradle passed” veya “Git push complete” durumu göstermemek için kasıtlıdır.

## Komut önizleme widget'ı

Komut akışı kartı kullanıcı örneğine benzeyen bir ağaç görünümü sunar:

```text
|_ Görev / istek
|__ Planlama ajanı
|___ npm run build
|____ ./gradlew assembleRelease
|_____ git status --short
```

Her gerçek komut kaydı komut kimliği, sohbet/oturum, amaç, durum, çıkış kodu, sınırlı stdout/stderr ve zaman bilgisi tutar. Öneri ve gerçek sonuç ayrı durumlarla gösterilir. Kayıtlar Android Keystore tabanlı AES-GCM ile şifrelenir ve metin içinde bilinen token desenleri maskelenir.

Örnek durum ayrımı:

| Satır | Gösterim | Neden |
|---|---|---|
| Görev alındı | Gerçek kayıt durumu | Uygulama görevi kuyruğa koymuştur |
| Planlama ajanı | Çalışıyor/planlandı | Ajan orkestrasyonu bu rolü gerçekten çağırdıysa güncellenir |
| `npm run build` | ÖNERİ ya da gerçek hata/başarı | Bu Android shell ortamında npm mevcut değilse komut “bulunamadı” ile başarısız olur |
| `./gradlew assembleRelease` | ÖNERİ ya da gerçek hata/başarı | Proje wrapper, JDK ve gerekli Android araçlarını içermiyorsa komut başarısız olur |
| `git status --short` | ÖNERİ ya da gerçek hata/başarı | Git ikilisi bulunmalı ve klasör Git deposu olmalıdır |
| Gemini yanıtı | Başarılı/hata | Gerçek API yanıtına göre görev günlüğü güncellenir |

Gerçek komutlar Kairo terminal motorunda en fazla 900 saniye çalışır; her stdout/stderr akışı 6.000 karakterde sınırlandırılır ve boruların kilitlenmesini önlemek için fazla çıktı okunup atılır. Bu güvenlik kontrolleri bir OS container/ayrı UID sandbox'ının yerini almaz; shell Kairo uygulamasının Android kullanıcı kimliğiyle çalışır.

## Gömülü terminal ve AI komutları

Kairo, `termux-app` kaynak ağacından yalnızca Apache-2.0 lisanslı `terminal-emulator` ve `terminal-view` modüllerini uygulamanın içine alır. Ayrı Termux uygulaması başlatılmaz; Termux bildirim servisi veya Termux manifest `Application` sınıfı paket içinde kullanılmaz. Terminal görünümü Kairo'nun kendi ekranıdır; native PTY kitaplığı `/system/bin/sh` ile etkileşimli kabuk başlatır.

### Oturumlar ve dosyalar

- Her sohbet için ayrı PTY oturumu, ayrı shell geçmişi kaydı ve ayrı `HOME`/`TMPDIR` alanı vardır.
- Projeye bağlı sohbetler projenin gerçek dosya klasörünü, kişisel sohbetler o sohbete ait dosya klasörünü çalışma dizini olarak kullanır.
- Terminal komutundan sonra uygulama ilgili dosya envanterini güvenli sınırlar içinde yeniler; yeni veya değiştirilmiş dosyalar sohbet/proje dosya listesine yansıtılır.
- Herhangi bir `node_modules`, `.git`, `build`, `.gradle`, `dist`, `target`, `.expo` veya önbellek klasörü dosya kartı listesi hazırlanırken tarama kapsamından çıkarılır; klasörün kendisi çalışma dizininde durmaya devam eder.
- En fazla 8 canlı PTY oturumu bellekte tutulur. Eski oturum bellekten çıkarıldığında süreç kapatılır; proje/sohbet dosyaları silinmez.

### AI işleyişi

1. Kullanıcı geliştirme veya terminal gerektiren bir istek gönderir.
2. Gemini, varsa terminal fonksiyonunu kullanarak tek bir komut ve kısa amaç üretir.
3. Komut `TerminalCommandPolicy` denetiminden geçer; root, sistem/uygulama özel verisini hedefleme, bilinen token'ı komuta koyma ve tehlikeli desenler engellenir.
4. Komut terminal kuyruğunda görünür; kullanıcı komutu okuyup **Onayla ve çalıştır** veya **Reddet** seçer.
5. Kairo shell'i komutu gerçek çalışma dizininde yürütür. Çıkış kodu, sınırlı stdout/stderr, süre aşımı ve durum kaydı tutulur.
6. Sonuç komut widget'ında/kapsülde gösterilir. Kullanıcı **AI ile devam** dediğinde gerçek komut sonucu Gemini'ye bağlam olarak gönderilir ve sonraki adım önerilebilir.

Örnek zaman çizelgesi:

```text
|_ İstek: projenin derlemesini kontrol et
|__ Planlama: depo yolunu ve gereken araçları belirle
|___ pwd                      · gerçek komut / çıkış kodu
|____ git status --short      · araç varsa gerçek sonuç
|_____ ./gradlew test         · kullanıcı onayı sonrası
|______ Hata / başarılı       · stdout/stderr ve exit code
```

### Çalıştırma sınırları ve güvenlik

- Terminal `/system/bin/sh` ve Android sistem yardımcılarını kullanır. Bu sürüm Termux'un Linux paket yöneticisini (`pkg`/`apt`) veya Node.js, npm, Git, Gradle, Java JDK, Expo/EAS ikililerini paketlemez. Sağlanan `.tar.gz`, kaynak koddur; bu araçları hazır kullanıcı alanı ikilileri olarak içermez.
- Komut denetimi koruyucu bir kara listedir, tam işletim sistemi sandbox'ı değildir. Onaylanan shell, Kairo'nun normal Android uygulama kimliğiyle ve onun erişebildiği uygulama dosyalarıyla çalışır; root yoktur. Kaynağı bilinmeyen komutları onaylamadan önce incele.
- Shell komutları için azami süre 900 saniye; stdout ve stderr için her biri 6.000 karakter yakalama sınırı vardır. Limitten fazla çıktı boru kilidini önlemek amacıyla okunup atılır.
- Komut geçmişi AES-GCM ile şifrelenir; bilinen token desenleri loglardan maskelenir. Redaksiyon bütün olası sırları bulacağını garanti etmez; secret'ı komuta yazma.
- `npm` veya `git` gibi olmayan bir ikili komut çalıştırılırsa sistem gerçek hata çıktısını kaydeder. Widget bu hatayı başarıya dönüştürmez.
- Build araçları ve Linux kullanıcı alanı paketlerini Kairo'nun uygulama kimliği için derlemek veya dağıtmak ayrı bir çalışma gerektirir. Resmî Termux paketlerinin `/data/data/com.termux/files/usr` önekini varsaydığı hazır ikilileri yalnızca farklı klasöre açmak yeterli değildir.

## Görev yaşam döngüsü

1. Kullanıcı mesajı veya widget görevi gönderilir.
2. Secret algılama katmanı bilinen token desenlerini kontrol eder; olası secret bulunursa kullanıcıdan kasaya kaydetme veya iptal etme kararı ister.
3. Sohbet mesajı yerel depoya kaydedilir.
4. Görev kaydı başlatılır ve kapsül/bildirim durumu yayınlanır.
5. Arka plan görev servisi sohbet bağlamını ve ek dosyaları yükler.
6. Yapılandırılmış ajan rollerinden uygun olanlar kısa görev çıktıları hazırlar. Bir alt ajanın hatası ana sohbet yanıtını iptal etmez.
7. Ana Gemini isteği yürütülür.
8. Varsa inceleme ajanı yanıtı kontrol eder ve anlamlı düzeltme notları ekler.
9. Yanıt, dosya kartları ve durumlar yerel olarak kaydedilir.
10. Bildirim, kapsül ve launcher widget güncellenir.
11. Başarı/hata sonucu görev kaydına yazılır.

### Görev kuyruğu ve kapasite

Uygulama şu sürümde kullanıcı görevlerini Android servisindeki sırayla işler; aynı anda sınırsız bağımsız shell ajanı çalıştırdığını iddia etmez. Ajan filosundaki anahtar yuvaları ve rol ekranı, ayrı API anahtarlarıyla uzman çağrısı yapmaya yarar. Kotalar/erişim farklı olduğunda bazı uzman çağrıları atlanabilir. Arayüz, yeni iş kabul edemiyorsa “ajanlar çalışıyor” mesajını gösterir.

### Süre tahmini

Widget, gerçek başlangıç ve bitiş zamanlarını kullanabilir. Ölçülmüş tarihçe yoksa kesin yüzde veya kalan dakika gösterilmemelidir. Bu nedenle henüz komut yürütülmeyen işlemler için “%80 tamamlandı” gibi sabit bir ilerleme üretilmez.

## Ajan filosu

Kairo ekranında aşağıdaki rollere ayrılmış sekiz anahtar yuvası bulunur:

| Yuva | Rol | Sorumluluk |
|---:|---|---|
| 1 | Sohbet ajanı | Kullanıcıyla ana diyalog ve yanıt |
| 2 | Planlama ajanı | Alt görevler, bağımlılıklar ve kabul ölçütleri |
| 3 | Kodlama ajanı | Teknik yaklaşım, kod ve dosya çıktıları |
| 4 | Terminal yöneticisi | Kairo içi shell komutu önerme, güvenlik politikası, onay ve terminal çıktısı üzerinden ana ajana devam |
| 5 | Hata ayıklama ajanı | Log/hata analizi ve düzeltme kontrolleri |
| 6 | Entegrasyon ajanı | API/servis bağlama ve güvenli kimlik bilgisi sınırları |
| 7 | Ana dağıtıcı ajan | Görev yönlendirme ve özet birleştirme |
| 8 | Kapsül/widget ajanı | Görünür durum özeti ve ölçülebilir sonraki adım |

Anahtarlar `SecureApiKeyStore` ile şifrelenmiş depoda tutulur. Her yuvaya anahtar kaydetmek, otomatik olarak sekiz paralel arka plan süreç oluştuğu anlamına gelmez. Ana ajan, mevcut görev karmaşıklığına göre uygun rolleri seçer; erişilemeyen/anahtarsız rol atlanır. Modelin gizli düşünce zinciri gösterilmez; kullanıcıya özet, uygulanabilir adım, kanıt ve sonuç sunulur.

## Sohbet ve proje alanları

- Her sohbetin bir kimliği, başlığı, mesaj dizisi, pin durumu ve kendi dosya alanı bulunur.
- Her proje ortak dosya alanına sahiptir; projeye bağlanan sohbetler bu ortak dosyalara erişebilir.
- Sohbet başlığı ilk mesajdan üretilebilir, kullanıcı tarafından yeniden adlandırılabilir ve sabitlenebilir.
- Sohbet silme kullanıcı eylemidir; ilişkili dosyaların nasıl davranacağı silme akışında korunmalıdır.
- Dosya ekleri sohbet bağlamına eklenir; arşivler diske açılmadan sınırlı giriş/boyut koşullarıyla okunabilir.
- Bir dosya başka klasöre kopyalanırken Android Storage Access Framework kullanıcı tarafından seçilen URI'yi kullanır.
- Sohbet ve proje alanları birbirinden ayrıdır; proje dosyaları sohbet dosyalarıyla aynı koleksiyon sayılmaz.

### Veri bütünlüğü prensipleri

1. Her dosya kaydının kimliği ve içerik adı ayrı tutulur.
2. Bir dosya dışa aktarılmadan önce kullanıcı hedef klasörü seçer.
3. Dosya dışa aktarımında uygulama belgesini kopyaladığını bildirir; taşınma işlemi yapıyorsa orijinali silmeden önce onay almalıdır.
4. Yapılandırma değişiklikleri kalıcı depoya yazılır.
5. Gizli değerler sohbet JSON'una eklenmez; yalnızca kasa etiketinin adı referans olarak kullanılır.
6. Uygulama kaldırılırsa uygulama içi depodaki veriler Android tarafından silinebilir. Kendi yedekleme politikanı kurmadan cihaz verisini temizleme.

## Dinamik etkinlik kapsülü

Kapsül Android tarafında kullanıcı izniyle çalışan `TYPE_APPLICATION_OVERLAY` katmanıdır; iOS Dynamic Island'ın sistem bileşeni değildir. Android cihazlarda gerçek kamera deliğinin çevresine yerleşmek garanti edilemez. Uygulama kapsülü yüzen yuvarlatılmış bir panel olarak çizilir.

### Görünür durumlar

- Boşta / hazır.
- İstek kabul edildi.
- Bağlam hazırlanıyor.
- Planlama ajanı çalışıyor.
- Ana Gemini yanıtı hazırlanıyor.
- Sesli oturum dinliyor/yanıt veriyor.
- Spotify bağlantısı veya parça durumu.
- Dosya çıktıları hazırlanıyor.
- Başarılı tamamlandı.
- Hata/yeniden bağlanma.
- Çevrimdışı ve tekrar çevrimiçi durumu.

### Konum ve görünüm

Ayarlar bölümünde X/Y yüzde veya dp karşılıkları, genişlik, yükseklik, opaklık, köşe yarıçapı ve otomatik daralma seçenekleri saklanır. Ön izlemedeki dairesel tutamaç sürüklenebilir; gerçek yüzen kapsül de izin verildiğinde taşınabilir. Kapsül geometri değerleri ekran sınırlarına göre normalize edilmelidir.

### Üç saniye basılı tutma

1. Kullanıcı kapsüle basıp tutar.
2. Kapsül çevresindeki altın animasyon izi süreyi görsel olarak doldurur.
3. Üç saniye tamamlanınca uygulama mikrofon/API izinlerini kontrol eder.
4. Koşullar tamam ise Gemini Live ses servisi başlatılır; değilse ilgili ayara yönlendirme gösterilir.

Android OEM arka plan sınırları ve overlay davranışları cihazdan cihaza farklılık gösterebilir. Bu nedenle bu özellik gerçek cihazda doğrulanmadan “her Android'de kesintisiz çalışır” olarak kabul edilmemelidir.

## Spotify

Spotify entegrasyonu API erişimi olduğunda şu alanları okur veya kontrol eder:

- Aktif parça adı, sanatçı, albüm ve albüm kapak adresi.
- Oynatım süresi ve mevcut ilerleme.
- Önceki parça, oynat/duraklat, sonraki parça.
- Karıştırma durumu ve tekrar modu.
- Kapsül veya launcher widget'ından medya ekranına geçiş.
- Uygulama bağlantısının durumu ve hesap yetkisi hataları.

### Bağlantı gereksinimi

1. [Spotify Developer Dashboard](https://developer.spotify.com/dashboard) içinde uygulama oluştur.
2. Spotify Client ID'yi Kairo içindeki Spotify ekranına gir.
3. Dashboard'da `com.altayhr.kairo:/oauth2redirect` redirect URI'sini tam olarak ekle. Spotify politikası, uygulama türü veya hesap kuralı bu özel URI'yi reddederse Spotify'ın HTTPS/App Links yönergelerine göre redirect mimarisini ayrıca ayarlamak gerekir.
4. Authorization Code + PKCE kullanılır; **Client Secret APK'ya gömülmez**.
5. İstenen kapsamlar: `user-read-private`, `user-read-email`, `user-read-playback-state`, `user-read-currently-playing`, `user-modify-playback-state`.
6. Spotify cihazında müzik çalarken Kairo içinden durum yenile.

Spotify API davranışı hesap türü, erişim kotası, aktif cihaz, uygulama erişim durumu ve provider politikalarına bağlıdır. API 403/404 hatası döndürürse Kairo bağlantının var olduğunu gösterebilir ancak oynatma eyleminin başarıyla tamamlandığını iddia etmez.

### Yapay zekâ ile müzik komutları

Sohbet metni Spotify niyeti olarak tanınırsa `SpotifyCommandService` temel komutları Spotify API eylemlerine bağlayabilir. Doğal dildeki her istek için arama/URI çözümleme veya playlist seçimi tüm durumlarıyla kapsanmıyorsa kullanıcıdan daha açık bir şarkı/artist adı iste. Spotify bağlantısının varlığı tüm doğal dil komutlarının yürütülebileceği anlamına gelmez.

## Güvenli kasa

Kasa sohbet başına değildir; tüm sohbetler, projeler ve ajan akışları için tek bir ortak kullanıcı kasasıdır. Bir anahtar eklerken kullanıcı bir kayıt adı ve değer girer. Değer varsayılan olarak gizlenir; göz düğmesi geçici olarak görünür kılar. Kaydet veya iptal işlemi açık bir kullanıcı eylemidir.

### Şifreleme ilkeleri

- Değerler `SecureApiKeyStore` aracılığıyla AES-GCM ile şifrelenir.
- Şifreleme anahtarı Android Keystore'da tutulur.
- API key/token'ın kendisi normal SharedPreferences JSON'una yazılmaz.
- İş akışı yalnızca `secretName` gibi bir etiket saklar; eylem çalışırken değer şifreli depodan okunur.
- Gizli değerler Gemini prompt'una eklenmemelidir.
- Token log'a, snackbar metnine, analytics olayına veya exception string'ine yazılmamalıdır.
- Kasa kayıtlarını yedekleme/export etmeyi eklerken plaintext secret export etme; parola şifreli yedek ve açık kullanıcı onayı olmadan sunulmaz.

### Token algılama

Sohbet mesajında bilinen bazı secret desenleri algılanırsa gönderim askıya alınır ve kullanıcıya güvenli kasa modalı gösterilir. Algılama kusursuz değildir; token'ı elle paylaşırsan mesaj göndermeden önce kontrol et. Bir sağlayıcı token'ını kullandığında yalnızca gereken kapsamı ver ve artık kullanılmıyorsa sağlayıcı panelinden iptal et.

### Güvenli kasa sınırları

- Uygulamayı kaldırmak veya uygulama verisini temizlemek yerel kasa kayıtlarını siler.
- Keystore anahtarı kaybolursa mevcut şifreli kayıt çözülemeyebilir.
- Kasa değerlerini başka kullanıcılarla paylaşan senkronizasyon bu kaynak sürümünde yoktur.
- Gerçek terminal bağlantısı gelene kadar bir secret'ı shell `ENV` değişkenine güvenli aktarma işlemi uygulanmış değildir.

## Sunucusuz yerel otomasyon

Otomasyon ekranı Android cihazında çalışır. İş akışları `AutomationStore` içinde saklanır; manuel çalıştırma ve olay tetikleri `AutomationWorker` üzerinden kuyruğa alınır; zamanlama `WorkManager` ile yürütülür. İnternet yoksa ağ gerektiren iş ağ geri gelene kadar bekleyebilir veya retry politikasıyla yeniden denenebilir.

### Desteklenen eylemler

| Eylem | API çağrısı | Gereken bilgi |
|---|---|---|
| HTTP | Kullanıcı tarafından verilen HTTPS endpoint | URL, yöntem, JSON başlıkları/gövdesi, isteğe bağlı kasa etiketi |
| n8n | Kullanıcı tarafından verilen HTTPS webhook endpoint | n8n URL'si, varsa auth token'ı kasada |
| GitHub Actions | `POST /repos/{owner}/{repo}/actions/workflows/{workflow}/dispatches` | Owner, repo, workflow adı/ID, ref ve yetkili token kasa etiketi |
| Resend | `POST /emails` | Doğrulanmış sender, alıcı, konu, içerik ve API key kasa etiketi |
| Notion | `POST /v1/pages` | Database ID, title property, sayfa başlığı/içeriği ve integration token kasa etiketi |

Provider eylemlerinde secret değeri forma gömülmez; yalnızca kasadaki ismi kaydedilir. E-posta göndermek veya GitHub workflow tetiklemek dış sistemde gerçek etki yaratır; iş akışını kaydetmeden önce hedefi ve token kapsamını kontrol et.

### Tetikleyiciler

- **Manuel:** Kullanıcı “Çalıştır” düğmesine basar.
- **Yerel olay:** `chat.message.sent`, `chat.response.completed`, `voice.started`, `oauth.connected` gibi uygulama olayları cihaz içinden oluşturulur ve eşleşen akışları sıraya alabilir.
- **Zamanlanmış:** Android WorkManager aralıklı iş zamanlar; minimum periyodik aralık platform tarafından belirlenir ve tam saniye garantisi vermez.

### Sunucu olmadan mümkün olmayanlar

Android telefonun kendisi, genel internetten sürekli dinleyen herkese açık HTTPS webhook sunucusu değildir. Bu nedenle cihaz kapalıyken gelen GitHub/Stripe olaylarını almak, genel webhook URL'si oluşturmak veya telefon kapalıyken cron çalıştırmak bu sürümde yer almaz. Bunun yerine uygulama açıkken/Android izin verdiğinde yerel tetikler ve dış API'lere giden istekler çalışır. Kullanıcı sunucu istemediği için bu yetenekler gizlenmemiş/yalancı bir “aktif” durum olarak sunulmaz.

## Çevrimdışı davranışı

- Android `ConnectivityManager` üzerinden bağlantı kaybı ve geri gelme durumları izlenir.
- Bağlantı kaybolduğunda kırmızı çevrimdışı işareti/banner ve kapsül durumu güncellenir.
- Bağlantı geri geldiğinde yeşil çevrimiçi durumu gösterilir; yeniden bağlantı bildirimi yayımlanabilir.
- Yerel notlar ve daha önce kaydedilmiş sohbetler ağ olmadan görüntülenebilir.
- Gemini, Spotify ve dış API eylemleri internet gerektirir.
- Retry davranışı endpoint/eşiklere göre uygulanır; sınırsız hızlı tekrar yapılmamalıdır.
- Çevrimdışı durum, bir isteğin arka planda kesin tamamlanacağını garanti etmez. WorkManager ve Android üretici güç ayarları çalıştırma zamanını geciktirebilir.

## Android ana ekran widget'ı

Android launcher tarafında genel durum/Spotify kartı (`KairoWidgetProvider`) korunur. Buna ek olarak **22 ayrı AI widget sağlayıcısı** vardır; böylece Android widget seçicisinde toplam **23 Kairo widget türü** görünür. Her AI widget'ın kendi Kotlin sağlayıcı sınıfı (`app/src/main/java/com/altayhr/kairo/widgets/*WidgetProvider.kt`) ve kendi `appwidget-provider` XML kaydı (`app/src/main/res/xml/kairo_widget_*.xml`) vardır. Görsel düzen ortak olsa da widget seçicisinde ayrı öğeler olarak listelenir.

- Siyah, ince kenarlıklı, mor vurgulu premium kart tasarımı.
- **AÇ** düğmesi ilgili Kairo alanına gider.
- **AI İLE BAŞLAT** düğmesi o widget'ın anahtarını uygulamaya taşır ve widget'a özgü istemi mevcut Gemini görev kuyruğuna gönderir.
- Durum satırı yerel AI görev kaydından okunur. Ağ widget'ı Android `ConnectivityManager` ile geçerli bağlantıyı ölçer; Spotify widget'ı kaydedilmiş oynatma/track bilgisini kullanır.
- Derleme, npm, GitHub Release ve Expo widget'ları gerçek bir build çalışmadıysa başarı yüzdesi uydurmaz; ilgili AI analizini veya kontrol planını başlatır.
- Android `RemoteViews`, Compose animasyonlarını veya tam zamanlı müzik ilerleme çubuğunu çalıştıramaz; yenilemeler kayıtlı görev, ağ ve medya olaylarıyla yapılır.

Ana ekrana eklemek için launcher'ın widget menüsünde **Kairo AI** başlığı altındaki istenen widget'ı seç. Bazı üretici arayüzlerinde liste uygulama adı altında gruplanır; uygulama güncellemesinden sonra widget listesi yenilenene kadar kısa süre önbellekte kalabilir.
## Gemini ve canlı ses

- Ana Gemini API anahtarı Ayarlar üzerinden kaydedilir.
- Sekiz ajan anahtar yuvası isteğe bağlıdır; her uzman API anahtarı bulunmadığında o rol atlanabilir.
- Model adı ve düşünme seviyesi kullanıcının hesabında kullanılabilir olmalıdır.
- Web arama/grounding özellikleri yalnızca seçili API modelinin desteklemesi ve yanıtın kaynak döndürmesi durumunda çalışır.
- Canlı ses oturumu mikrofon izni ve kullanıcı eylemi gerektirir.
- Servis, devam eden ses oturumunun durumunu bildirim/kapsüle aktarır; mikrofon gizlice başlatılmaz.
- Android canlı ses servisi durdurulursa aktif oturum da durabilir.

API sürümleri, model adları, quota ve tool desteği sağlayıcı tarafından değiştirilebilir. Uygulama ayarında modelin seçilebilir olması Google hesabının o modele erişimi bulunduğuna tek başına kanıt değildir.

## Dosya kartları ve dışa aktarma

Dosya kartı şu bilgileri göstermelidir: dosya adı, uzantı, boyut veya tahmin edilen içerik tipi, üretim/eklenme kaynağı ve mevcut eylemler. Kart tıklandığında Android belge seçici ile kullanıcı dosyayı bir klasöre kopyalayabilir.

- Kaynak kod ve metin içerikleri bağlam olarak okunabilir.
- Büyük arşivlerde toplam boyut ve giriş sayısı sınırları korunmalıdır.
- ZIP dosyası diske güvenli olmayan şekilde açılmaz; path traversal (`../`) yolları kullanılmamalıdır.
- Bilinmeyen ikili dosyalar metin olarak modele gönderilmemelidir.
- Dışa aktarma başarı bildirimini, belge yazımı kapandıktan sonra göstermelidir.
- Kod dosyasının öneri olması, cihazda derlendiği anlamına gelmez.

## Ayar kategorileri

Ayarları konu bazlı kategorilere ayıran yapı şu alanları kapsar:

1. **Hesap ve AI:** Gemini API anahtarı, model, düşünme seviyesi ve web arama seçeneği.
2. **Uzman ajanlar:** Sekiz anahtar yuvası ve roller.
3. **Canlı ses:** Live model, ses profili, mikrofon izni ve servis durumu.
4. **Dinamik kapsül:** Aç/kapat, X/Y konumu, genişlik, yükseklik, opaklık, köşe yarıçapı, otomatik daralma.
5. **Bildirim ve ses:** Bildirim izni, durum sesleri ve arka plan görev bilgileri.
6. **Spotify ve medya:** Client ID, bağlantı durumu ve oynatma izinleri.
7. **Güvenlik ve kasa:** Ortak gizli değer kaydı, göster/gizle ve silme.
8. **Otomasyon:** Eylem, trigger, zaman aralığı, endpoint ve kasa etiketi.
9. **Veri ve gizlilik:** Yerel veri alanları, geçmiş temizleme ve uygulama verisinin silinmesine ilişkin uyarılar.
10. **İzinler:** Bildirim, mikrofon, overlay ve pil optimizasyonu yönlendirmeleri.

Her ayarın kullanıcı seçimi yerel kalıcı depoya yazılır. Android izinlerini uygulama kendi kendine veremez; kullanıcının sistem ekranında onaylaması gerekir.

## Güvenlik modeli

### Ağ

- Dış API istekleri HTTPS kullanır.
- Genel HTTP otomasyon URL'leri kabul edilmez.
- URL içine username/password veya token gömmek önerilmez.
- Secret header değerleri normal JSON workflow formunda saklanmamalıdır; `secretName` ile kasaya referans verilmelidir.
- API yanıtının tamamı loglanmamalıdır; yalnızca HTTP kodu ve redakte edilmiş durum gösterilmelidir.

### Yetki

- Mikrofon ve overlay yalnızca ilgili akışta ve açık izinle kullanılır.
- E-posta gönderme, dış API tetikleme ve GitHub Actions çalıştırma kullanıcı tarafından ayarlanan akışlar üzerinden gerçekleşir.
- GitHub token'ına minimum `Actions: write` ve gerektiği kadar depo erişimi verilir.
- Notion integration token'ı yalnızca gerekli workspace/database erişimiyle sınırlanır.
- Resend API key'in gönderen domain yetkisi yalnızca gereken kapsama indirilir.

### Günlükler

- API key/token değerleri task status ve command timeline satırlarına yazılmaz.
- Hata mesajları uzunlukla sınırlanır.
- Gizli düşünce zinciri kullanıcıya dökülmez; plan ve eylem özeti gösterilir.
- Komut çıktılarına token/PII redaksiyonu terminal entegrasyonu eklenirken zorunlu olmalıdır.

### Threat model

| Risk | Azaltım |
|---|---|
| Yanlışlıkla prompt'a token gönderme | Secret deseni algılama ve kasa onayı |
| API anahtarının kaynak koda girmesi | Git ignore, CI secret ve yerel kasa |
| Yanlış/zararlı hedef URL | HTTPS ve URL/host doğrulaması |
| Dış eylemin kullanıcıdan habersiz tetiklenmesi | Yerel iş akışı kurulumu, açık çalıştırma/onay ve olay kaydı |
| Çevrimdışı durumda görev kaybı | Yerel kalıcı kayıt, WorkManager ve başarısız durum bilgisi |
| Sahte başarı göstergesi | Gerçek API sonuç kodu ile öneri adımını farklı etiketleme |
| Kaydedilmemiş ayarlar | Uygulama ayarlarının kalıcı depoya yazılması |
| Cihaz kaybı/veri temizleme | Kasa kurtarma/şifreli yedek bu sürümde garanti edilmez; kullanıcıyı uyar |

## Teknik mimari

```text
app/src/main/java/com/altayhr/kairo/
├── MainActivity.kt                    # Türkçe ana gezinme ve sohbet akışı
├── data/
│   ├── Models.kt                      # Sohbet/proje/dosya veri modelleri
│   ├── WorkspaceStore.kt              # Sohbet/proje yerel depolama
│   ├── SecureApiKeyStore.kt            # Android Keystore + AES-GCM kasa
│   ├── AgentRunStore.kt                # Kalıcı görev aşaması/sonuç günlüğü
│   ├── FlowToolkitStore.kt              # Yerel yardımcı araç verileri
│   ├── NotesRepository.kt               # Yerel notlar
│   └── UnifiedDiff.kt                   # Yerel metin/kod diff
├── service/
│   ├── GeminiTaskService.kt             # Kullanıcı başlatmalı görev kuyruğu
│   ├── AgentOrchestrator.kt              # Uzman roller ve yanıt inceleme
│   ├── GeminiLiveVoiceService.kt         # Canlı ses foreground service
│   ├── IslandOverlayService.kt           # Dinamik etkinlik kapsülü
│   └── CapsuleGeometry.kt                # Kapsül geometri depolama
├── network/GeminiClient.kt               # Gemini istemcisi
├── oauth/OAuthFlowManager.kt              # Doğrudan PKCE OAuth istemcisi
├── automation/
│   ├── AutomationModels.kt                # Yerel otomasyon modelleri
│   ├── AutomationCenterScreen.kt          # Otomasyon UI
│   ├── AutomationExecutor.kt              # Doğrudan HTTPS/API eylemleri
│   └── AutomationWorker.kt                # Yerel WorkManager görevleri
├── features/
│   ├── AgentWidgetsScreen.kt              # 22 AI destekli widget kartı
│   ├── AgentStudioScreen.kt                # Görev merkezi
│   ├── AgentPoolScreen.kt                 # 8 rol/anahtar yuvası
│   ├── SpotifyPlayerScreen.kt             # Spotify API kontrolü
│   ├── SecureVaultScreen.kt                # Ortak kasa UI
│   ├── CapsuleSettingsSection.kt            # Kapsül ayarları
│   └── ...                                 # Notlar, medya, Flow ve ek ekranlar
├── ui/KairoSplashScreen.kt                  # Animasyonlu açılış görünümü
└── widgets/
    ├── KairoWidgetProvider.kt               # Genel durum/Spotify widget'ı
    ├── KairoAgentWidgetProvider.kt           # Ortak canlı durum + AI başlatma altyapısı
    └── *WidgetProvider.kt                   # 22 ayrı launcher widget sağlayıcısı
```

### Kullanılan temel teknoloji

| Paket / platform | Kullanım |
|---|---|
| Kotlin | Uygulama dili |
| Jetpack Compose | Modern, state-driven UI |
| Material 3 | Temel bileşenler/tema |
| AndroidX WorkManager | Ağ bağlı otomasyon ve zamanlama |
| Android foreground service | Uzun süren görev/ses/overlay durumu |
| OkHttp | HTTPS / WebSocket / REST |
| Android Keystore + AES-GCM | Yerel secret şifreleme |
| SharedPreferences / JSON | Bu kaynak sürümündeki bazı yerel veri kayıtları |
| GitHub Actions | CI, test, lint ve sürüm dağıtımı |

## Hızlı başlangıç

### Gereksinimler

- Android Studio güncel kararlı sürüm veya eşdeğer Android Gradle ortamı.
- JDK 17.
- Android SDK Platform 35 ve Build Tools 35.0.0.
- Gradle 8.10.2 (CI bunu kurar; yerel IDE kendi Gradle ortamını kullanabilir).
- Gemini API key; Live ses için ilgili model/hesap erişimi.
- Spotify için Spotify Developer Client ID ve gerekli API izinleri.

### Android Studio

1. ZIP'i bir klasöre çıkar.
2. Android Studio'da proje kökünü aç.
3. `local.properties` dosyasını kendi Android SDK yoluna göre oluştur (bu dosyayı Git'e ekleme).
4. Gradle sync yap.
5. `app` konfigürasyonunu seç.
6. Önce `testDebugUnitTest`, `lint`, ardından `assembleDebug` çalıştır.
7. Uygulamayı bir emülatörde veya Android cihazda aç.
8. İlk kurulum ekranında gerekli izinleri isteğe bağlı biçimde yönet.
9. Ayarlar'da Gemini API anahtarını ekle.
10. Widget merkezi üzerinden komut planını test et; terminal komutlarının gerçek yürütülmediğini unutma.

### Yerel komutlar

```bash
gradle --version
gradle --no-daemon clean testDebugUnitTest
gradle --no-daemon lint
gradle --no-daemon assembleDebug
gradle --no-daemon assembleRelease
```

`assembleRelease`, imza parametreleri verilmemişse yerel test amacıyla imzasız veya dağıtıma uygun olmayan çıktı üretebilir. Play Store/GitHub release için imzalama yapılandırmasını kullan.

## GitHub Actions CI ve imzalı Release

`.github/workflows/android-ci.yml` değişikliklerde test, lint ve debug derlemesi yapar. `.github/workflows/release.yml` `vMAJOR.MINOR.PATCH` tag'i üzerine release APK/AAB üretir, APK imzasını `apksigner` ile doğrular, SHA-256 dosyası oluşturur ve GitHub Release'a ekler.

### Tek seferlik Android imza anahtarı

Release signing key kişisel dağıtım kimliğidir. Bu özel anahtarı repo içine veya ZIP dosyasına koyma.

1. Güvenli bilgisayarında bir `.jks` keystore oluştur.
2. Keystore dosyasını base64'e çevir.
3. GitHub repository → **Settings → Secrets and variables → Actions** bölümüne bu değerleri ekle:
   - `ANDROID_SIGNING_KEY_BASE64`
   - `ANDROID_KEYSTORE_PASSWORD`
   - `ANDROID_KEY_ALIAS`
   - `ANDROID_KEY_PASSWORD`
4. Keystore dosyasını ve şifrelerini yalnızca güvenli yedekte sakla.
5. Repo'ya değişiklik gönder; Android CI'nin başarılı olduğunu doğrula.
6. İstenen sürüm numarasıyla annotated tag oluştur ve gönder:

```bash
git tag -a v1.0.2 -m "Kairo AI 1.0.2"
git push origin v1.0.2
```

7. GitHub Actions **Signed Android Release** işini aç.
8. Job başarıyla tamamlanırsa Releases bölümünde imzalı APK, AAB ve `SHA256SUMS.txt` bulunur.

### İmza anahtarı devamlılığı

Aynı uygulamanın sonraki sürümlerini Android'in güncelleme olarak kabul etmesi için aynı private keystore kullanılmalıdır. Keystore kaybolur veya değiştirilirse mevcut kullanıcıların uygulamayı normal APK update yöntemiyle güncellemesi mümkün olmayabilir. Bu yüzden GitHub Secret yalnızca tek kopya olarak tutulmamalı; şifreli güvenli yedeği olmalıdır.

### Release dosyaları

- `KairoAI-vX.Y.Z-signed.apk`: cihaza yüklenebilir Android paketi.
- `KairoAI-vX.Y.Z.aab`: Android App Bundle; desteklenen mağaza yayın süreci için.
- `SHA256SUMS.txt`: indirilen dosyaların bütünlük doğrulaması.

Bir CI koşusu başarılı olmadıkça release üretilmiş gibi davranma. Bu kaynak ZIP'inde CI'nin gelecekte üreteceği bir APK yoktur.

## İzinler

| İzin / ayar | Kullanım | Zorunlu mu? |
|---|---|---|
| `INTERNET` | Gemini, Spotify ve dış API istekleri | AI/online iş için |
| `ACCESS_NETWORK_STATE` | Çevrim içi/çevrim dışı sinyali | Durum bilgisi için |
| `POST_NOTIFICATIONS` | Uzun görev ve otomasyon bildirimi | Android 13+ kullanıcı onayı |
| `SYSTEM_ALERT_WINDOW` | Yüzen kapsül | Kapsül özelliği için |
| `RECORD_AUDIO` | Gemini Live konuşma | Sesli mod için |
| `FOREGROUND_SERVICE` | Uzun süren kullanıcı başlatmalı servis | İlgili servis için |
| `FOREGROUND_SERVICE_MICROPHONE` | Arka plan mikrofon servisi | Sesli oturum için |
| `FOREGROUND_SERVICE_DATA_SYNC` | Görev servisi | Veri/AI görevleri için |
| Pil optimizasyonu yönlendirmesi | Daha güvenilir arka plan işi | İsteğe bağlı; cihaz izin verirse |

İzin sihirbazı kullanıcıya ayar sayfasına gitme butonu verir. Hiçbir buton, kullanıcının sistem onayı yerine geçmez. İzinler verilmezse yalnızca bu izinlere dayanan özellikler kısıtlanır; kullanıcıya uygulamanın geri kalanını kullanmaya devam etme seçeneği sunulmalıdır.

## Veri saklama ve yedekleme

| Veri | Depolama yaklaşımı | Hassasiyet |
|---|---|---|
| Sohbetler ve projeler | Yerel uygulama verisi | Özel kullanıcı içeriği |
| Sohbet/proje dosyaları | Uygulama alanı ve seçilmiş belge URI'leri | Dosya içeriğine bağlı |
| API anahtarları/tokens | Android Keystore anahtarıyla AES-GCM kasası | Yüksek |
| Ajan anahtar yuvaları | Aynı ortak kasada farklı kayıt adları | Yüksek |
| OAuth PKCE geçici işlem | Şifreli kasa kaydı, süreli state/verifier | Yüksek |
| Otomasyon tanımları | Yerel workflow kaydı | Orta; token değerleri burada bulunmamalı |
| Otomasyon olayları | Son kayıtlarla sınırlandırılmış yerel günlük | Orta; secret eklenmemeli |
| Görev aşamaları | Yerel `AgentRunStore` | Orta; yalnızca durum metadata'sı |
| Widget durumu | Uygulama durumundan türetilmiş cache | Düşük/orta |
| Spotify son parça bilgisi | Son bilinen medya özeti | Kullanıcı tercihi |

### Veri kaybını azaltma

- Değişen sohbet/proje kayıtlarını mümkün olduğunca çabuk kalıcı depoya yaz.
- Büyük dosyalar için güvenli hedef klasöre dışa aktarım sağla.
- Kasanın şifre çözme anahtarıyla birlikte düz metin export'undan kaçın.
- Uygulama güncellemesinde `applicationId` ve keystore devamlılığını koru.
- Uygulama verisini temizlemeden önce sohbet ve dosyalarını dışa aktar.
- Bu sürüm otomatik çoklu cihaz bulut eşitlemesi sağlamaz; yerel verinin tek kopyası olan cihazda sıfırlama veri kaybettirebilir.

## Kaynak dosya haritası

Ayrıntılı kaynak/port eşlemesi `docs/PORTING_MAP.md`, test adımları `docs/QA_CHECKLIST.md`, release rehberi `docs/RELEASE_1.0.1_TR.md` içindedir. Bu belgeler çalışma alanındaki kaynak yapısını ve kalan sınırlamaları anlatır.

### Önemli varlıklar

| Dosya | İşlev |
|---|---|
| `design/icons/spotify.svg` | Spotify için özel marka vektörü |
| `design/icons/github.svg` | GitHub durum/derleme görünümü |
| `design/icons/expo.svg` | Expo build görünümü |
| `design/icons/kairo-mark.svg` | Kairo işareti |
| `design/svg/kairo-orbit.svg` | README/splash orbit görseli |
| `design/svg/voice-wave.svg` | Ses dalga görseli |
| `app/src/main/res/drawable/ic_mochi*.xml` | Mochi durumlarının Android vektörleri |
| `app/src/main/res/raw/sound_*.wav` | Kairo durum/mikro animasyon sesleri |
| `app/src/main/res/layout/widget_kairo.xml` | Android ana ekran widget düzeni |

## Kalite ve test yaklaşımı

Release kabulü yalnızca kaynak klasörünün varlığına veya ZIP'in açılmasına göre verilmez.

### CI kapıları

- [ ] Gradle sync ve bağımlılık çözümlemesi.
- [ ] JVM unit testleri.
- [ ] Android lint.
- [ ] Debug APK derlemesi.
- [ ] Release APK/AAB derlemesi.
- [ ] Keystore secret'larının GitHub Actions'a doğru aktarılması.
- [ ] APK sertifika doğrulaması.
- [ ] SHA-256 checksum dosyası.
- [ ] GitHub Release asset'lerinin tamlığı.

### Gerçek cihaz kapıları

- [ ] İlk açılış ve atlanabilir izinler.
- [ ] Overlay izin ver/ret ve kapsül konum ayarları.
- [ ] Kapsül basılı tutma 3 saniye geri bildirimi.
- [ ] Mikrofon izni verildi/verilmedi akışları.
- [ ] Ekran kapalıyken kullanıcı başlatmalı görev bildirimleri.
- [ ] Ağ kaybı ve geri gelme banner/kapsül/widget davranışı.
- [ ] Spotify bağlama, token yenileme, aktif cihaz yok ve API 403/404.
- [ ] Ortak güvenli kasa kaydet/göster/gizle/sil.
- [ ] Sohbet ve proje dosyalarını dışa aktarma.
- [ ] Yerel HTTP iş akışı success/4xx/5xx/network retry.
- [ ] GitHub Actions dispatch için minimum token yetkisi.
- [ ] Resend gerçek test adresi ile güvenli e-posta teslimi.
- [ ] Notion database/title property mapping.
- [ ] Launcher widget'ı ekle/kaldır ve cihaz yeniden başlatma.
- [ ] En az 22 AI widget kartının her biri için prompt rotası.
- [ ] Komut önizlemesinin terminal çalıştırıldı iddiası üretmediği kontrolü.

### Kod denetim notları

- Kaynak kodun tamamının Android cihazda derlendiğini söylemek için gerçek `gradle assembleDebug`/`assembleRelease` çıktısı gerekir.
- Gerçek Spotify/Gemini hesabı olmadan uçtan uca API yetkileri doğrulanamaz.
- Cihaz üreticisinin pil yönetimi davranışı emülatörden farklı olabilir.
- `AgentWidgetsScreen` durumları kayıtlı görevleri gösterir, gizli model düşünce zinciri göstermez.
- Önerilen komutlar komut gerçekten yürütülene kadar yalnızca öneridir.

## Bilinen sınırlar

1. **Linux geliştirme paketleri yok:** terminal/PTY ve Android `/system/bin/sh` çalışır; ancak bu kaynak arşivi Node/npm/Git/Gradle/Expo kullanıcı alanı paketlerini içermiyor. Bu araçlar bulunmadığında bu komutların gerçek sonucu “bulunamadı” olacaktır.
2. **Sunucusuz webhooks yok:** Android cihaz genel HTTPS listener olarak hizmet vermez.
3. **Cihaz kapalıyken cron yok:** WorkManager cihazın güç/ağ koşullarına bağlıdır.
4. **Her OAuth sağlayıcısı public client değildir:** istemci secret'ı gerektiren sağlayıcılar backend olmadan güvenli OAuth exchange sunmayabilir. Bu tür sağlayıcılar için token'ı sağlayıcı panelinden oluşturup yerel kasada saklama veya kendi kontrolündeki bir broker mimarisi gerekir; mevcut pakette harici broker yoktur.
5. **Widget performansı:** Android launcher güncelleme sıklığı, RemoteViews sınırlamaları ve servis yaşam döngüsü dikkate alınır.
6. **Model kapasitesi:** sekiz API anahtarı yuvası model/limit/istek kotası sınırını ortadan kaldırmaz.
7. **Canlı ses:** model erişimi, bağlantı ve mikrofon izinleri gerektirir.
8. **Otomasyon etkileri:** GitHub Actions dispatch, e-posta ve Notion sayfası oluşturma dış dünyada gerçek işlemlerdir.
9. **Yerel veriler:** cihaz temizleme veya uygulamayı kaldırma sohbet/kasa verisini kaybettirebilir.
10. **Piksel eşdeğerliği:** macOS UI davranışı Android ölçülerinde ve Android kontrol desenleriyle yeniden kurulmuştur.

## Sık sorulan sorular

### 1. Bu ZIP içinde APK var mı?

Hayır; teslim edilen bu paket kaynak kod ve GitHub Actions dosyalarını içerir. İmzalı APK'yı tag tabanlı Release workflow'u oluşturur.

### 2. GitHub Release otomatik imzalar mı?

Evet, workflow imza secret'ları tanımlıysa APK'yı keystore ile imzalar ve sertifika doğrulaması yapar. Secret'lar olmadan Release bilerek hata verir.

### 3. Cloud backend gerekir mi?

Kairo'nun sohbeti, yerel dosyaları, yerel görev ve WorkManager otomasyonları için harici backend gerekmez. Ancak herkese açık inbound webhook, gizli istemci OAuth broker'ı veya telefon kapalıyken çalışma isteyen bazı otomasyonlar bir sunucu gerektirir ve burada yer almaz.

### 4. Komut widget'ı npm ve Gradle komutlarını gerçekten çalıştırır mı?

Komut widget’ı gerçek terminal kayıtlarını gösterir; komut çalıştırma motoru Kairo içine gömülü PTY ve `/system/bin/sh` kullanır. AI komutları güvenlik kontrolünden geçer ve Terminal ekranında kullanıcı onayı bekler; onay verilmeden komut çalışmaz. Ancak `npm`, `node`, `git`, `gradle` ve `expo` gibi geliştirme araçları bu arşivde henüz paketlenmedi; bunları gerektiren işlerde eksik ikili açıkça hata verir, sahte başarı gösterilmez.

### 5. 22 widget'ın tümü AI'ya bağlı mı?

Her kartın AI ile çalıştırma eylemi vardır ve kart amacına göre bir prompt ana Gemini görev hattına gönderilir. Widget başlığında “AI destekli” yazması komut yürütme veya gerçek provider API erişimi garantisi değildir.

### 6. Sohbetlerim uygulama yeniden başlatılınca durur mu?

Sohbet/proje verileri yerel store'a kaydedilir. Buna rağmen uygulama verisini temizlemek, uygulamayı kaldırmak veya cihazı sıfırlamak veriyi silebilir; önemli dosyaları dışa aktar.

### 7. Token'lar her sohbet için ayrı mı?

Hayır. Kasa tüm sohbetler/projeler arasında ortaktır. Otomasyon bir secret değerin kendisini değil kasadaki etiketini referans alabilir.

### 8. Spotify Client Secret neden istenmiyor?

Android uygulaması public client olarak Authorization Code + PKCE akışını kullanır. İstemci secret'ını APK içine gömmek güvenli değildir. Spotify hesabı ve Developer Dashboard koşulları ayrıca karşılanmalıdır.

### 9. Çevrimdışı olduğumda Gemini yanıtlar mı?

Hayır, API için internet gerekir. Yerel notlar/önceden kaydedilmiş sohbetler okunabilir; AI ve uzak API eylemleri bağlantı gelene kadar çalışamaz.

### 10. Kapsül her telefonda ekran deliğinin çevresine sabitlenir mi?

Hayır. Android ekran üstü overlay olarak çalışır. Yerleşim, izinler ve OEM davranışı telefona bağlıdır.

### 11. Özel bir bug bulursam ne göndermeliyim?

Android sürümü, model/telefon bilgisi, hata adımları ve token/kişisel veri içermeyen log bölümünü paylaş. Secret veya erişim token'ını issue içine ekleme.

### 12. İmzalı Release kurulumu nerede?

[`docs/RELEASE_1.0.1_TR.md`](docs/RELEASE_1.0.1_TR.md) ve [`docs/AUTOMATION_AND_RELEASE_SETUP_TR.md`](docs/AUTOMATION_AND_RELEASE_SETUP_TR.md) belgelerinde.

## Katkı, yıldız ve iletişim

Kairo'yu beğendiysen projeyi GitHub'a yükledikten sonra depoya **★ yıldız** bırakabilirsin. Resmî hesap: [THT0AltayHR depoları](https://github.com/THT0AltayHR?tab=repositories). Repo henüz yayımlanmadığı için bu kaynak paketinde canlı Issues adresi varsayılmamıştır. Rapor gönderirken API key, Spotify token'ı, e-posta adresleri ve özel kaynak kodu gibi gizli verileri kaldır.

- **GitHub:** [THT0AltayHR](https://github.com/THT0AltayHR)
- **Telegram:** [@AltayHR](https://t.me/AltayHR)
- **Issue şablonları:** `.github/ISSUE_TEMPLATE/`
- **Katkı yönergesi:** [`CONTRIBUTING.md`](CONTRIBUTING.md)
- **Güvenlik yönergesi:** [`SECURITY.md`](SECURITY.md)

### İyi bir bug raporu nasıl hazırlanır?

1. Android sürümü ve cihaz modelini yaz.
2. Sorunu baştan sona tekrarlayan net adımlar ekle.
3. Beklenen ve gözlenen davranışı ayır.
4. Loglardan token, cookie, e-posta, kişisel dosya yolu ve diğer özel verileri sil.
5. Sorunun tekrarlandığı kaynak değişikliğini/commit'i belirt.

### İyi bir özellik isteği nasıl hazırlanır?

- Kullanıcı amacını anlat.
- Hangi ekranda olacağını tarif et.
- Gerçek veri/izin/API gereksinimini belirt.
- Başarı koşulunu tanımla.
- Yanlış başarı bildirimi üretebilecek durumları da not et.

## Üçüncü taraf lisansları

Gömülü `terminal-emulator` ve `terminal-view` modülleri Apache License 2.0 koşullarına tabidir. Atıf, kapsam ve tam metin için [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) ile modül klasörlerindeki `LICENSE-APACHE-2.0.txt` dosyalarına bakın.

## Lisans

Bu proje [`LICENSE`](LICENSE) içinde bulunan GNU General Public License v3.0 koşullarıyla dağıtılır. GPLv3, kaynak kodun incelenmesini ve lisans şartları çerçevesinde değiştirilip dağıtılmasını sağlar; bu nedenle README'de kod değişikliğini mutlak biçimde yasaklayan bir ifade bulunmaz. Üçüncü taraf ikon, logo, API ve servisler kendi marka/servis koşullarına tabidir.

**Garanti verilmez.** Yazılım “olduğu gibi” sunulur. Üretim dağıtımından önce kendi cihazlarında, gerçek hesaplarda, gerekli izinlerde, release imzasında ve güvenlik kontrollerinde doğrulama yap.
