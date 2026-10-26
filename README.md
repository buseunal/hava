# 🌤️ Hava

Kotlin ve Jetpack Compose ile yazılmış, **lofi havası** taşıyan bir hava durumu uygulaması.
81 ili plaka koduyla listeler, istediğin şehrin anlık, saatlik ve 7 günlük tahminini gösterir.
Gece olunca tema, arka plan ve müzik sakinleşir.

> Bilişim Kampı Android eğitimi kapsamında geliştirildi. Hazırlayan: [@buseunal](https://github.com/buseunal)

## ✨ Neler yapıyor?

**Şehir listesi**
- 81 il alfabetik sırayla listelenir, **plaka koduyla** (01, 06, 34...) gösterilir.
- Yazdıkça liste anında süzülür (Türkçe karakter duyarlı: `izm` → İzmir, `IGD` → Iğdır).
- 500 ms bekledikten sonra internette de aranır; ilçeler ve yurt dışı şehirler de bulunur.
- Her şehir plakasına göre sabit bir **pastel renk** ve rozetinde ince bir **doku** alır.
- Saate göre selamlayan **karşılama kartı** (Günaydın, İyi akşamlar, İyi geceler...).
- Sonuç yoksa "Şehir bulunamadı", internet yoksa "Tekrar dene" düğmeli hata ekranı.

**Hava tahmini**
- Anlık durum (sıcaklık, hissedilen, nem, rüzgar), saatlik ve 7 günlük tahmin.
- **Gece/gündüz** şehrin kendi saatine göre belirlenir: emoji, tema, arka plan ve müzik değişir.
- Uygulama ilk açılışta konum izni ister ve bulunduğun şehrin tahminini gösterir.

**Lofi atmosfer**
- Pencereden hava durumuna göre değişen **lofi sahne** (güneş, bulut, yağmur, kar, yıldızlı gece).
- **Hiç ses dosyası olmadan**, kodla üretilen lofi müzik. Akor, davul ve ortam sesi hava durumuna göre değişir,
  geceleri davulsuz ve daha sakin olur.
- Arka plan resim değil, **Canvas ile çizilmiştir**.

## 🛠️ Kullanılan teknolojiler

| Alan | Araç | Neden |
|---|---|---|
| Dil | Kotlin | Android'in resmi, güvenli ve kısa dili |
| Arayüz | Jetpack Compose + Material 3 | Ekranı XML yerine kodla çizer, state değişince kendini yeniler |
| Durum yönetimi | ViewModel + StateFlow | Ekran dönünce veri kaybolmaz, mantık ekrandan ayrılır |
| Asenkron iş | Kotlin Coroutines | İnternet beklerken ekranı dondurmaz |
| Ağ | Retrofit + Gson | `@GET` arayüzünden istek üretir, JSON'u nesneye çevirir |
| Gezinme | Navigation Compose | Liste ve tahmin ekranı arası geçiş |
| Ses | `AudioTrack` + özel sentezleyici | Müziği telifsiz ve hafif şekilde kodla üretir |
| Çizim | Compose `Canvas` | Arka plan, sahne ve dokular dosyasız çizilir |
| Yazı tipi | Nunito (SIL OFL) | Yuvarlak, tatlı ve okunaklı |
| Test | JUnit + MockWebServer + Coroutines Test | İnternetsiz, hızlı ve güvenilir testler |
| Kod biçimi | ktlint (Spotless) | Tutarlı kod, CI'da otomatik denetim |
| CI | GitHub Actions | Her push'ta derleme, biçim kontrolü ve testler |

## 🏗️ Mimari: üç katman

*UI çizer, ViewModel durumu yönetir, Repository veriye erişir.*

```
UI (Composable)  →  ViewModel (StateFlow)  →  Repository (suspend)  →  Retrofit → Open-Meteo
```

Ekran her an dört durumdan **tam olarak birindedir** (`sealed interface LoadState`):
`Loading` · `Success` · `Empty` · `Error`.

```
com.kampplus.hava
├── MainActivity.kt            Uygulamanın kapısı
├── model/                     Veri neye benzer?
│   ├── City.kt                City + CitySearchResponse
│   ├── Forecast.kt            Forecast + anlık / saatlik / günlük
│   └── WeatherCondition.kt    WMO kodu → emoji + Türkçe etiket (gece sürümüyle)
├── data/                      Veri nereden gelir?
│   ├── ApiClient.kt           Retrofit kurulumu
│   ├── CityApi.kt · ForecastApi.kt
│   ├── ForecastResponse.kt    JSON → model çevrimi
│   ├── CityRepository.kt · WeatherRepository.kt
│   └── TurkishProvinces.kt    81 il, plaka kodları, Türkçe süzme
└── ui/                        Ne çizilir, ne zaman değişir?
    ├── LoadState.kt · HavaNavHost.kt
    ├── city/                  CityScreen · CityViewModel · CityCard · HeroCard
    ├── forecast/              ForecastScreen · ForecastViewModel
    ├── location/              FirstLaunchLocation (konum izni + şehir bulma)
    ├── lofi/                  LofiBackground · LofiScene · LofiSynth (müzik)
    ├── component/             ShimmerList · EmptyView · ErrorView · SoftAppear
    └── theme/                 Color · Theme · Type
```

Ayrıntılar için:
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): mimari kararlar ve veri akışı
- [docs/MEKANIKLER.md](docs/MEKANIKLER.md): her mekanik hangi araçla, neden yapıldı

## 🌐 API: Open-Meteo (anahtar gerekmez)

| Amaç | Çağrı |
|---|---|
| Şehir arama | `https://geocoding-api.open-meteo.com/v1/search?name=Ankara&count=10&language=tr` |
| Tahmin | `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=..&hourly=..&daily=..&forecast_days=7&timezone=auto` |

- Sonuç yoksa arama yanıtında `results` alanı **hiç gelmez**, bu Boş (Empty) durumudur.
- `is_day` alanı, şehrin kendi saatine göre gündüz mü gece mi olduğunu söyler.
- `weather_code` WMO standardındadır (0 açık, 61 yağmur, 95 fırtına...).

Hava verisi [Open-Meteo.com](https://open-meteo.com/) tarafından sağlanır
([CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)). Ücretsiz kullanım ticari olmayan projeler içindir.

## ▶️ Çalıştırma

Android Studio'da projeyi açıp Gradle Sync yapın, ardından ▶ ile emülatörde veya telefonda çalıştırın.

```bash
./gradlew assembleDebug        # debug APK  → app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # unit testler
./gradlew spotlessCheck        # kod biçimi denetimi (düzeltmek için spotlessApply)
./gradlew assembleRelease      # release APK (R8 açık)
```

Android SDK yolunu `local.properties` içinde `sdk.dir=...` ile veya `ANDROID_HOME` ortam değişkeniyle verin.
Gereksinim: Android 8.0 (API 26) ve üzeri.

## 🧪 Testler

| Test | Neyi korur |
|---|---|
| `WeatherConditionTest` | WMO kod → görünüm eşlemesi |
| `CityApiTest` | JSON → `City` eşlemesi (`@SerializedName`) |
| `ForecastApiTest` | Sütun → satır çevrimi |
| `CityViewModelTest` | Dört durum, arama ve "Tekrar dene" |
| `ForecastViewModelTest` | Rota parametreleri ve durumlar |

Testler gerçek internete çıkmaz: `MockWebServer` ve sahte repository'ler kullanılır.

## 🔐 Release imzası

`keystore.properties.example` dosyasını `keystore.properties` olarak kopyalayıp doldurun (repoya girmez).
Dosya yoksa release APK debug anahtarıyla imzalanır.

## 📄 Lisanslar ve kaynaklar

- Hava verisi: [Open-Meteo](https://open-meteo.com/) (CC BY 4.0)
- Yazı tipi: [Nunito](https://fonts.google.com/specimen/Nunito) (SIL Open Font License)
- Müzik ve görsellerin tamamı kodla üretilmiştir, harici ses veya resim dosyası kullanılmamıştır.
