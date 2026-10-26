package com.kampplus.hava.data

import com.kampplus.hava.model.City
import java.text.Collator
import java.util.Locale

// Türkiye'nin 81 ili Türkçe alfabeye göre sıralı
object TurkishProvinces {
    private val raw = listOf(
        "Adana" to (37.0000 to 35.3213), "Adıyaman" to (37.7648 to 38.2786), "Afyonkarahisar" to (38.7507 to 30.5567),
        "Ağrı" to (39.7191 to 43.0503), "Aksaray" to (38.3687 to 34.0370), "Amasya" to (40.6499 to 35.8353),
        "Ankara" to (39.9334 to 32.8597), "Antalya" to (36.8969 to 30.7133), "Ardahan" to (41.1105 to 42.7022),
        "Artvin" to (41.1828 to 41.8183), "Aydın" to (37.8560 to 27.8416), "Balıkesir" to (39.6484 to 27.8826),
        "Bartın" to (41.6344 to 32.3375), "Batman" to (37.8812 to 41.1351), "Bayburt" to (40.2552 to 40.2249),
        "Bilecik" to (40.1506 to 29.9792), "Bingöl" to (38.8854 to 40.4980), "Bitlis" to (38.4006 to 42.1095),
        "Bolu" to (40.7395 to 31.6116), "Burdur" to (37.7203 to 30.2908), "Bursa" to (40.1826 to 29.0665),
        "Çanakkale" to (40.1553 to 26.4142), "Çankırı" to (40.6013 to 33.6134), "Çorum" to (40.5506 to 34.9556),
        "Denizli" to (37.7765 to 29.0864), "Diyarbakır" to (37.9144 to 40.2306), "Düzce" to (40.8438 to 31.1565),
        "Edirne" to (41.6818 to 26.5623), "Elazığ" to (38.6810 to 39.2264), "Erzincan" to (39.7500 to 39.5000),
        "Erzurum" to (39.9000 to 41.2700), "Eskişehir" to (39.7767 to 30.5206), "Gaziantep" to (37.0662 to 37.3833),
        "Giresun" to (40.9128 to 38.3895), "Gümüşhane" to (40.4386 to 39.5086), "Hakkari" to (37.5833 to 43.7333),
        "Hatay" to (36.2021 to 36.1600), "Iğdır" to (39.9237 to 44.0450), "Isparta" to (37.7648 to 30.5566),
        "İstanbul" to (41.0082 to 28.9784), "İzmir" to (38.4237 to 27.1428), "Kahramanmaraş" to (37.5858 to 36.9371),
        "Karabük" to (41.2061 to 32.6204), "Karaman" to (37.1759 to 33.2287), "Kars" to (40.6167 to 43.1000),
        "Kastamonu" to (41.3887 to 33.7827), "Kayseri" to (38.7312 to 35.4787), "Kilis" to (36.7184 to 37.1212),
        "Kırıkkale" to (39.8468 to 33.5153), "Kırklareli" to (41.7333 to 27.2167), "Kırşehir" to (39.1425 to 34.1709),
        "Kocaeli" to (40.8533 to 29.8815), "Konya" to (37.8667 to 32.4833), "Kütahya" to (39.4167 to 29.9833),
        "Malatya" to (38.3552 to 38.3095), "Manisa" to (38.6191 to 27.4289), "Mardin" to (37.3212 to 40.7245),
        "Mersin" to (36.8000 to 34.6333), "Muğla" to (37.2153 to 28.3636), "Muş" to (38.9462 to 41.7539),
        "Nevşehir" to (38.6939 to 34.6857), "Niğde" to (37.9667 to 34.6833), "Ordu" to (40.9839 to 37.8764),
        "Osmaniye" to (37.0742 to 36.2478), "Rize" to (41.0201 to 40.5234), "Sakarya" to (40.6940 to 30.4358),
        "Samsun" to (41.2928 to 36.3313), "Şanlıurfa" to (37.1591 to 38.7969), "Siirt" to (37.9333 to 41.9500),
        "Sinop" to (42.0231 to 35.1531), "Şırnak" to (37.4187 to 42.4918), "Sivas" to (39.7477 to 37.0179),
        "Tekirdağ" to (40.9833 to 27.5167), "Tokat" to (40.3167 to 36.5500), "Trabzon" to (41.0015 to 39.7178),
        "Tunceli" to (39.1079 to 39.5401), "Uşak" to (38.6823 to 29.4082), "Van" to (38.4891 to 43.4089),
        "Yalova" to (40.6500 to 29.2667), "Yozgat" to (39.8181 to 34.8147), "Zonguldak" to (41.4564 to 31.7987)
    )

    val TR: Locale = Locale.forLanguageTag("tr-TR")
    private val collator: Collator = Collator.getInstance(TR)

    // İller plaka sırasıyla: indeks + 1 = plaka kodu
    private val platesInOrder = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın",
        "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı",
        "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir",
        "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir",
        "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya",
        "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya",
        "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak",
        "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak",
        "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )

    // "Istanbul" / "İstanbul" / "istanbul" hepsi aynı anahtara düşsün diye harfleri sadeleştirdik
    private fun fold(s: String): String = s.lowercase(TR)
        .replace('ı', 'i').replace('ş', 's').replace('ğ', 'g')
        .replace('ü', 'u').replace('ö', 'o').replace('ç', 'c')

    private val plates: Map<String, String> =
        platesInOrder.mapIndexed { i, name -> fold(name) to "%02d".format(i + 1) }.toMap()

    fun plateOf(city: City): String? {
        if (!city.countryCode.equals("TR", ignoreCase = true)) return null
        return plates[fold(city.admin1 ?: city.name)] ?: plates[fold(city.name)]
    }

    val all: List<City> = raw.mapIndexed { i, (name, coord) ->
        City(-(i + 1L), name, coord.first, coord.second, "TR", "Türkiye", name)
    }.sortedWith { a, b -> collator.compare(a.name, b.name) }

    // Türkçe büyük/küçük harf duyarsız filtre ("izm" -> İzmir, "IGD" -> Iğdır)
    fun filter(query: String): List<City> {
        val q = query.trim().lowercase(TR)
        if (q.isEmpty()) return all
        return all.filter { it.name.lowercase(TR).contains(q) }
    }
}
