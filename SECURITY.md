# Güvenlik politikası

Güvenlik sorunlarını kamuya açık bir issue yerine özel iletişim kanalından bildirin: GitHub profili [THT0AltayHR](https://github.com/THT0AltayHR) veya Telegram [@AltayHR](https://t.me/AltayHR).

Rapor; etkilenen sürümü, etkisini, tekrarlama adımlarını ve secret içermeyen kanıtı açıklamalıdır. Gerçek API anahtarı, OAuth tokenı, keystore/parola, webhook imza anahtarı veya kişisel verileri paylaşmayın.

Kapsam: secret/log sızıntısı, OAuth state/PKCE doğrulama atlatma, yetkisiz API eylemi, güvensiz dış URL, dosya yolu aşımı, komut enjeksiyonu, kullanıcı onayı olmadan yan etki ve arka plan izinlerinin kötüye kullanımı.

Kairo AI kaynak kodu `LICENSE` içindeki GNU GPL-3.0 altında dağıtılır. Gerçek kullanıcıların veya üçüncü taraf hesapların verilerine karşı saldırı denemesi yapmayın.

## Kasa ve yedekleme

Gizli değerler Android Keystore tarafından korunan AES-GCM anahtarıyla şifrelenir. Android Keystore cihaz/uygulama kurulumuna bağlıdır; cihaz sıfırlanır veya uygulama verisi silinirse sırlar geri alınamayabilir. Kritik sırların ayrı bir parola yöneticisinde yedeklenmesi gerekir. Sırlar AI prompt'larına, olay günlüğüne veya kaynak koda eklenmemelidir.
