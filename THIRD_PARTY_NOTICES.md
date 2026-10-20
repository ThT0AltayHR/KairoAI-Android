# Üçüncü taraf lisans ve bildirimleri

## Android Terminal Emulator bileşenleri

Kairo AI, aşağıdaki açık kaynak modüllerini uygulamanın içine gömer:

- `terminal-emulator/` — `com.termux.terminal` paketindeki Terminal Emulator kodu ve yerel PTY/JNI katmanı.
- `terminal-view/` — `com.termux.view` paketindeki terminal görüntüleme/etkileşim bileşeni.

Kaynak: `termux/termux-app`, `v0.119.0-beta.3` kaynak arşivi; bu modüllerin lisansı üst kaynaktaki `LICENSE.md` istisna bölümünde Apache License 2.0 olarak belirtilmiştir.

Tam Apache License 2.0 metni, ilgili modül klasörlerinde `LICENSE-APACHE-2.0.txt` adıyla yer alır. Bu bildirim yalnızca belirtilen üçüncü taraf modüllerine ilişkindir. Termux uygulaması, Termux hizmetleri, `termux-shared` ve `termux-core` Kairo'ya taşınmamıştır.

Kairo uygulamasının kendi kaynak kodu için kök `LICENSE.md` dosyasına bakın. Üçüncü taraf bileşenler kendi lisans koşullarına tabi olmaya devam eder.

## Kairo değişikliği

`terminal-emulator/src/main/jni/termux.c` dosyasında `GetStringUTFChars` ile alınan `cwd` değerinin `ReleaseStringUTFChars` çağrısında yanlışlıkla `cmd` nesnesiyle serbest bırakılması düzeltildi; `cwd` artık kendi Java string referansıyla serbest bırakılır. Terminal modüllerinin Gradle tanımları Kairo'ya gömülü kitaplık olacak şekilde uyarlanmış; Android kitaplık manifestleri Termux uygulaması/servisi içermeyen manifestlerle değiştirilmiştir.
