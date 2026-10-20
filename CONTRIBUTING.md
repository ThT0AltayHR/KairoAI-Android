# Katkı rehberi

1. Önce kapsam, kullanım durumu ve kabul ölçütü içeren bir issue açın.
2. Değişiklikleri odaklı bir branch ve pull request olarak gönderin.
3. API key, OAuth tokenı, keystore, parola, kullanıcı dosyası veya temizlenmemiş log commit etmeyin.
4. Arayüz değişikliklerinde Türkçe erişilebilir etiketler ve küçük ekran davranışı ekleyin; özel asset'leri SVG/vektör olarak tutun.
5. Android kontrolünü JDK 17 + Android SDK 35 ve Gradle 8.10.2 ile çalıştırın: `gradle clean testDebugUnitTest lint assembleDebug`.
6. Güvenlik/izin/OAuth/arka plan eylemlerinde reddedilen izin ve bağlantı hatası durumlarını da test edin.
7. Gerçek terminal entegrasyonu eklenirse komutları shell string'ine körlemesine birleştirmeyin; argüman bazlı süreç başlatma, kullanıcı onayı, iptal, timeout, çıktı sınırı ve çalışma dizini izolasyonu gereklidir.
8. Dış eylemler (e-posta, GitHub dispatch, Notion sayfası) gerçek sistem etkisi yaratır; UI'da hedefi ve kullanıcı onayını koruyun.
9. Lisans `LICENSE` içindeki GNU GPL-3.0'dır. GPL kodun değiştirilmesine izin verir; dağıtılan türevlerin lisans koşullarını yerine getirme yükümlülüğü vardır.
