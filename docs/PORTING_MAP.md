# Kairo AI — Kaynak ve özellik eşleme haritası

Bu belge kaynak paketin gerçek yapısını, macOS kaynaklarından taşınan karşılıkları ve bilinçli olarak kapsam dışında kalan davranışları özetler. Android uygulamasının tamamının macOS ile piksel veya API düzeyinde birebir aynı olduğu iddia edilmez.

## Port ilkeleri

1. macOS API'leri Android'in eşdeğer izin/servis/Compose yapılarına çevrilir; AppKit/SwiftUI kodu Android'de doğrudan çalıştırılmaz.
2. Durum ve iş geçmişi cihazda tutulur. Kişisel API anahtarları Android Keystore korumalı AES-GCM depolamasına yazılır.
3. UI'daki önerilen terminal komutu, gerçekten terminal entegrasyonu çalıştırmadıkça **öneri** olarak gösterilir. `npm`, Gradle, Git, Expo veya shell komutlarının çalıştığını uydurmak yasaktır.
4. İnternet gerektiren üçüncü taraf eylemleri çevrimiçi cihazdan doğrudan ilgili HTTPS API'lerine gider. Genel gelen webhook, cihaz kapalıyken çalışan zamanlayıcı ve sürekli sunucu görevi bu sunucusuz sürümde yoktur.
5. Ağ hatası, API izin reddi veya doğrulama başarısızlığında uygulama açık hata durumu gösterir; başarılı durum üretmez.

## Ana kaynak alanları

| Alan | Kaynak | Sorumluluk |
|---|---|---|
| Android giriş / gezinme | `app/src/main/java/com/altayhr/kairo/MainActivity.kt` | Sohbet, proje, menü, ekran yönlendirme |
| Sohbet/veri modelleri | `data/Models.kt`, `data/WorkspaceStore.kt` | Sohbetler, mesajlar, proje/çalışma alanı verileri |
| Gemini istemcisi | `network/GeminiClient.kt` | API istekleri, yanıt/kaynak işleme |
| Görev servisi | `service/GeminiTaskService.kt` | Uzun süren AI görevi, bildirim ve durum yayını |
| Ajan dağıtımı | `service/AgentOrchestrator.kt` | Sekiz rolün görev bağlamları ve inceleme özeti |
| Görev günlüğü | `data/AgentRunStore.kt` | Ajan aşamaları ve son durum |
| Dinamik kapsül | `service/IslandOverlayService.kt` | Ekran üstü kapsül, dokunma, tutma ilerlemesi, sayfalar |
| Geometri / tutma çizgisi | `service/CapsuleGeometry.kt`, `service/CapsuleHoldProgressDrawable.kt` | Sürükleme sınırları, 3 saniyelik altın ilerleme animasyonu |
| İlk açılış izinleri | `features/PermissionSetupScreen.kt` | Bildirim, mikrofon, overlay ve arka plan ayarına yönlendirme |
| Kalıcı gizli kasa | `data/SecureApiKeyStore.kt`, `features/SecureVaultScreen.kt` | API anahtarı ve sağlayıcı tokenlarının Keystore tabanlı şifrelenmesi |
| Sekiz AI rolü | `features/AgentPoolScreen.kt`, `service/AgentOrchestrator.kt` | Sekiz ayrı Gemini anahtar yuvası ve isteğe bağlı rol çağrıları |
| Spotify bağlantısı | `oauth/OAuthFlowManager.kt`, `features/SpotifyPlayerScreen.kt`, `features/SpotifyCommandService.kt` | PKCE, oynatma bilgisi/kontrolü ve yerel token depolama |
| Widget kataloğu | `features/AgentWidgetsScreen.kt` | 22 AI destekli uygulama içi kontrol kartı ve komut önizleme |
| Android ana ekran widget'ları | `widgets/KairoWidgetProvider.kt`, `widgets/KairoAgentWidgetProvider.kt`, `widgets/*WidgetProvider.kt`, `res/xml/kairo_widget_*.xml` | Genel Spotify/durum widget'ı + 22 ayrı AI launcher widget'ı; her birinde gerçek görev durumu ve widget'a özel AI başlatma rotası |
| Yerel otomasyon modeli/çalıştırıcısı | `automation/AutomationModels.kt`, `AutomationExecutor.kt`, `AutomationWorker.kt` | Cihazda WorkManager ile manuel, yerel olay ve yaklaşık zamanlı işler |
| Dosya kartları | `saveCodeArtifacts` ve çalışma alanı depoları | Kod/metin çıktısını dosya kartlarına ve dışa aktarmaya dönüştürme |
| Splash ve varlıklar | `ui/KairoSplashScreen.kt`, `design/` | Özel orbit/voice/Spotify/GitHub/Expo SVG varlıkları ve açılış animasyonu |

## macOS özellikleri → Android karşılıkları

| Kaynak özelliği | Android karşılığı | Durum / not |
|---|---|---|
| SwiftUI/macOS ekranları | Kotlin + Jetpack Compose ekranları | Tasarım karşılığı; birebir piksel kopyası değil |
| Notch/Mochi durum alanı | Overlay kapsül ve bildirim | Android'in izin/pil/üretici sınırlarına bağlı |
| Sohbetler | Yerel sohbet deposu ve görev servisi | Temel iş akışı taşındı; macOS'a özgü bütün kenar durumları için eşdeğerlik iddiası yok |
| Proje ve dosya alanları | Proje/sohbet dosya kartları | Android belge sağlayıcısı ile dışa aktarma |
| Gemini API ve canlı ses | Gemini istemcisi + Live ses servisi | Gerçek anahtar/model/izin gerektirir |
| Ses efekti ve özel ifadeler | Android ses kaynakları/vektör durumu | Kaynak asset'leri paket içinde |
| Flow/düzeltme/metin araçları | Akıllı araçlar ekranları ve yerel depo | Bazı macOS yardımcı servisleri birebir taşınmamış olabilir |
| Spotify | OAuth PKCE + Spotify Web API | Geçerli geliştirici Client ID, kapsam ve uygun aktif cihaz/Premium gerektirebilir |
| GitHub/Resend/Notion/n8n/HTTP | Yerel otomasyonun doğrudan API isteği | Tokenları yerel kasaya koy; kapsam ve sağlayıcı limitleri geçerli |
| Genel webhook listener / uzak cron | Yok | Dış URL'den uygulamaya ulaşılabilmesi için bir sunucu gerekirdi; kullanıcı isteğiyle sunucu katmanı kaldırıldı |
| Gömülü terminal emülatörü / PTY / Android shell | `terminal-emulator`, `terminal-view`, `EmbeddedTerminalSession`, `EmbeddedTerminalEngine`, `TerminalCommandPolicy` | Gerçek `/system/bin/sh` komutları, kullanıcı onayı, gerçek stdout/stderr ve çıkış kodu; Linux geliştirme ikilileri henüz pakete dahil değil |

## Widget katalog listesi

`AgentWidgetsScreen.kt` içinde 22 uygulama içi kart vardır; bunlara karşılık gelen 22 bağımsız Android launcher sağlayıcısı da vardır: Komut akışı, Proje ilerlemesi, Ajan filosu, Gradle derlemesi, NPM iş akışı, GitHub Release, Expo build, İndirme takibi, Dosya denetimi, Hata ayıklama, Test laboratuvarı, Güvenlik nöbetçisi, Kod incelemesi, Entegrasyon durumu, Spotify canlı durumu, Ağ ve çevrimdışı, Bağlam penceresi, İstem atölyesi, Onay kuyruğu, Teslim kartları, Etkinlik günlüğü ve Proje bütünlüğü.

Bütün kartların **AI ile çalıştır** aksiyonu ana Gemini görev hattına bağlanır. Bu, karta özel canlı sensör/terminal süreci var demek değildir: yalnızca gerçekten mevcut yerel görev kaydı varsa o durum gösterilir. Komut kartları gerçek terminal sonuçları varsa bunları, yoksa yalnızca açıkça etiketlenmiş önerileri gösterir. Gömülü PTY `/system/bin/sh` çalıştırır; Linux geliştirme ikilileri mevcut değilse komut gerçek hata verir.

## Veri ve güvenlik

- OAuth `state` ve PKCE verifier zaman sınırlı tutulur ve Android Keystore anahtarı ile AES-GCM şifreli depolanır.
- Kasa sırları AI prompt'una eklenmez. Ajan istemlerine secret değeri yerine yalnızca görev metni girer.
- Dış eylemlerde etkisi açıklanır; HTTP cevap gövdesi ve kimlik doğrulama başlıkları olay günlüğüne eklenmez.
- Şifreli yerel verinin silinmesini/uygulamanın kaldırılmasını önleyebileceği iddia edilmez. Kritik kasa değerlerini parola yöneticisinde güvenli yedekle.

## Doğrulama sınırları

Tam Android Gradle derlemesi ve cihaz/Spotify/Gemini hesabı testleri yalnızca gerçek Android SDK/CI ve geliştirici hesaplarında sonuçlandırılabilir. CI yapılandırması depoda bulunur; başarılı koşu/Release yapılmadan imzalı APK veya kararlı sürüm üretildiği iddia edilmez.
