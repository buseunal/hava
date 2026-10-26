package com.kampplus.hava.ui.lofi

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.kampplus.hava.model.WeatherCondition
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Her hava durumu için kodla üretilen (sentezlenen) basit bir lofi döngüsü.
 *
 * Hiçbir ses dosyası kullanılmaz; tüm sesler matematiksel olarak üretildiği için
 * tamamen telifsizdir. Akor yürüyüşü + yumuşak davul + plak cızırtısı +
 * hava durumuna özel katman (yağmur, rüzgâr, kuş, çan, gök gürültüsü).
 */

object LofiSynth {
    const val SAMPLE_RATE = 22050
    private const val LOOP_SECONDS = 8

    private data class Mood(
        val chords: List<IntArray>, // MIDI nota numaraları
        val drums: Boolean,
        val layer: Layer
    )

    private enum class Layer { BIRDS, CRICKETS, NONE, WIND, RAIN_SOFT, RAIN, SNOW_BELLS, STORM }

    // Fmaj7 - Em7 - Dm7 - Cmaj7 gibi klasik lofi akorları
    private val warm =
        listOf(intArrayOf(53, 57, 60, 64), intArrayOf(52, 55, 59, 62), intArrayOf(50, 53, 57, 60), intArrayOf(48, 52, 55, 59))
    private val sad = listOf(intArrayOf(45, 48, 52, 55), intArrayOf(41, 45, 48, 52), intArrayOf(43, 47, 50, 53), intArrayOf(40, 43, 47, 50))
    private val dreamy =
        listOf(intArrayOf(50, 54, 57, 61), intArrayOf(47, 50, 54, 57), intArrayOf(43, 47, 50, 54), intArrayOf(45, 49, 52, 55))

    private fun moodOf(c: WeatherCondition): Mood = when (c) {
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> Mood(warm, true, Layer.BIRDS)
        WeatherCondition.PARTLY_CLOUDY -> Mood(warm, true, Layer.NONE)
        WeatherCondition.OVERCAST, WeatherCondition.FOG -> Mood(dreamy, false, Layer.WIND)
        WeatherCondition.DRIZZLE -> Mood(sad, true, Layer.RAIN_SOFT)
        WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> Mood(sad, true, Layer.RAIN)
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> Mood(dreamy, false, Layer.SNOW_BELLS)
        WeatherCondition.THUNDERSTORM -> Mood(sad, true, Layer.STORM)
        WeatherCondition.UNKNOWN -> Mood(warm, false, Layer.NONE)
    }

    // Gece: daha geniş aralıklı, yumuşak ve yavaş akorlar (Am7 - Fmaj7 - Cmaj9 - Gmaj9)
    private val night = listOf(
        intArrayOf(45, 52, 55, 60, 64),
        intArrayOf(41, 48, 52, 57, 60),
        intArrayOf(48, 55, 59, 62, 64),
        intArrayOf(43, 50, 54, 57, 62)
    )

    /** Geceyi sakinleştirir: davul yok, kuş yerine cırcır böceği, yağmurlu/fırtınalı havada hüzünlü akorlar kalır. */
    private fun nightMoodOf(m: Mood): Mood = Mood(
        chords = if (m.chords === sad) sad else night,
        drums = false,
        layer = if (m.layer == Layer.BIRDS) Layer.CRICKETS else m.layer
    )

    private fun midiToHz(n: Int) = 440.0 * 2.0.pow((n - 69) / 12.0)

    /** 16-bit mono PCM döngüsü üretir. */
    fun render(condition: WeatherCondition, isDay: Boolean = true): ShortArray {
        val night = !isDay
        val mood = if (night) nightMoodOf(moodOf(condition)) else moodOf(condition)
        val n = SAMPLE_RATE * LOOP_SECONDS
        val buf = DoubleArray(n)
        val rnd = Random(condition.ordinal + 7 + if (night) 100 else 0)
        val chordLen = n / mood.chords.size
        val beat = chordLen / 4

        // 1) Elektrikli piyano akorları (sinüs + hafif detune + üstel sönüm)
        mood.chords.forEachIndexed { ci, chord ->
            val start = ci * chordLen
            chord.forEachIndexed { ni, note ->
                val f = midiToHz(note)
                val offset = ni * 300 // hafif "strum"
                for (i in 0 until chordLen) {
                    val t = i.toDouble() / SAMPLE_RATE
                    // Gece: daha yavaş sönüm, daha yumuşak vuruş, daha kısık ses
                    val env = exp(-t * (if (night) 0.45 else 0.9)) * (1 - exp(-t * (if (night) 25 else 60))) *
                        (if (night) 0.8 else 1.0)
                    val s = sin(2 * PI * f * t) + 0.4 * sin(2 * PI * f * 1.003 * t) + 0.15 * sin(4 * PI * f * t)
                    val idx = (start + offset + i) % n
                    buf[idx] += s * env * 0.09
                }
            }
            // bas notası
            val bass = midiToHz(chord[0] - 12)
            for (i in 0 until chordLen) {
                val t = i.toDouble() / SAMPLE_RATE
                buf[start + i] += sin(2 * PI * bass * t) * exp(-t * 0.6) * (if (night) 0.12 else 0.18)
            }
            // Gece: akor boyunca nefes alıp veren yumuşak pad (başı ve sonu sessiz, döngü kesilmez)
            if (night) {
                chord.forEach { note ->
                    val f = midiToHz(note)
                    for (i in 0 until chordLen) {
                        val t = i.toDouble() / SAMPLE_RATE
                        val swell = sin(PI * i / chordLen).let { it * it }
                        buf[start + i] += sin(2 * PI * f * t) * swell * 0.02
                    }
                }
            }
        }

        // 2) Yumuşak davul: kick + "tss" hi-hat
        if (mood.drums) {
            for (b in 0 until n / beat) {
                val s = b * beat
                if (b % 4 == 0 || b % 4 == 2 && b % 8 == 6) addKick(buf, s)
                if (b % 2 == 1) addNoiseHit(buf, s, 0.12, 25.0, rnd)
                addNoiseHit(buf, s + beat / 2, 0.03, 80.0, rnd)
            }
        }

        // 3) Hava durumu katmanı
        when (mood.layer) {
            Layer.BIRDS -> repeat(6) { addChirp(buf, rnd.nextInt(n - SAMPLE_RATE), rnd) }
            Layer.CRICKETS -> addCrickets(buf, rnd)
            Layer.WIND -> addFilteredNoise(buf, rnd, 0.10, 0.01, slowMod = true)
            Layer.RAIN_SOFT -> addFilteredNoise(buf, rnd, 0.06, 0.25, slowMod = false)
            Layer.RAIN -> addFilteredNoise(buf, rnd, 0.12, 0.3, slowMod = false)
            Layer.SNOW_BELLS -> repeat(10) { addBell(buf, rnd.nextInt(n - SAMPLE_RATE), rnd) }
            Layer.STORM -> {
                addFilteredNoise(buf, rnd, 0.14, 0.3, slowMod = false)
                addThunder(buf, SAMPLE_RATE * 3, rnd)
            }
            Layer.NONE -> Unit
        }

        // 4) Plak cızırtısı
        for (i in 0 until n) {
            if (rnd.nextDouble() < 0.0006) buf[i] += (rnd.nextDouble() - 0.5) * 0.25
            buf[i] += (rnd.nextDouble() - 0.5) * 0.006
        }

        // 5) Lofi tonu: tek kutuplu alçak geçiren filtre + yumuşak kırpma
        var y = 0.0
        val out = ShortArray(n)
        for (i in 0 until n) {
            y += (if (night) 0.22 else 0.35) * (buf[i] - y)
            val v = kotlin.math.tanh(y * 1.4) * 0.8
            out[i] = (v * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun addKick(buf: DoubleArray, start: Int) {
        val len = SAMPLE_RATE / 4
        var phase = 0.0
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            phase += 2 * PI * (50 + 70 * exp(-t * 30)) / SAMPLE_RATE
            buf[(start + i) % buf.size] += sin(phase) * exp(-t * 14) * 0.45
        }
    }

    private fun addNoiseHit(buf: DoubleArray, start: Int, amp: Double, decay: Double, rnd: Random) {
        val len = SAMPLE_RATE / 5
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            buf[(start + i) % buf.size] += (rnd.nextDouble() - 0.5) * amp * exp(-t * decay)
        }
    }

    private fun addFilteredNoise(buf: DoubleArray, rnd: Random, amp: Double, alpha: Double, slowMod: Boolean) {
        var y = 0.0
        for (i in buf.indices) {
            y += alpha * ((rnd.nextDouble() - 0.5) - y)
            val mod = if (slowMod) 0.6 + 0.4 * sin(2 * PI * i / buf.size * 2) else 1.0
            buf[i] += y * amp * mod * (if (slowMod) 8 else 2)
            // ara sıra iri damla
            if (!slowMod && rnd.nextDouble() < 0.0004) addNoiseHit(buf, i, amp * 0.8, 120.0, rnd)
        }
    }

    private fun addChirp(buf: DoubleArray, start: Int, rnd: Random) {
        val base = 2500 + rnd.nextDouble() * 1500
        repeat(3) { k ->
            val s = start + k * SAMPLE_RATE / 9
            val len = SAMPLE_RATE / 14
            var phase = 0.0
            for (i in 0 until len) {
                val p = i.toDouble() / len
                phase += 2 * PI * (base + 900 * p) / SAMPLE_RATE
                buf[(s + i) % buf.size] += sin(phase) * sin(PI * p) * 0.035
            }
        }
    }

    /** Gece sakinliği: yaklaşık saniyede bir, üçlü kısa cırcır böceği darbeleri. */
    private fun addCrickets(buf: DoubleArray, rnd: Random) {
        val f = 4200.0
        var start = 0
        while (start < buf.size - SAMPLE_RATE / 2) {
            repeat(3) { k ->
                val s = start + k * SAMPLE_RATE / 14
                val len = SAMPLE_RATE / 22
                for (i in 0 until len) {
                    val p = i.toDouble() / len
                    buf[(s + i) % buf.size] += sin(2 * PI * f * i / SAMPLE_RATE) * sin(PI * p) * 0.03
                }
            }
            start += SAMPLE_RATE + rnd.nextInt(SAMPLE_RATE / 4)
        }
    }

    private fun addBell(buf: DoubleArray, start: Int, rnd: Random) {
        val notes = intArrayOf(74, 78, 81, 86, 90)
        val f = midiToHz(notes[rnd.nextInt(notes.size)])
        for (i in 0 until SAMPLE_RATE) {
            val t = i.toDouble() / SAMPLE_RATE
            buf[(start + i) % buf.size] += (sin(2 * PI * f * t) + 0.3 * sin(2 * PI * f * 2.76 * t)) * exp(-t * 3) * 0.05
        }
    }

    private fun addThunder(buf: DoubleArray, start: Int, rnd: Random) {
        var y = 0.0
        val len = SAMPLE_RATE * 3
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            y += 0.02 * ((rnd.nextDouble() - 0.5) - y)
            val env = (1 - exp(-t * 8)) * exp(-t * 1.2)
            buf[(start + i) % buf.size] += y * env * 6.0
        }
    }
}

// Üretilen döngüyü sonsuz tekrar ile çalan küçük oynatıcı. */
class LofiPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var track: AudioTrack? = null

    @Volatile private var generation = 0

    fun play(condition: WeatherCondition, isDay: Boolean = true) {
        stop()
        val gen = generation
        scope.launch {
            val pcm = LofiSynth.render(condition, isDay)
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(LofiSynth.SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * 2)
                .build()
            t.write(pcm, 0, pcm.size)
            t.setLoopPoints(0, pcm.size, -1)
            synchronized(this@LofiPlayer) {
                if (gen != generation) {
                    t.release()
                    return@launch
                }
                track?.release()
                track = t
            }
            t.play()
        }
    }

    @Synchronized
    fun stop() {
        generation++
        track?.let {
            runCatching { it.stop() }
            it.release()
        }
        track = null
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
