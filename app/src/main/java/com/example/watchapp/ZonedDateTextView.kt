package com.example.watchapp

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Un TextView care afiseaza data/ziua intr-un fus orar SI intr-o limba alese din XML
// (TextClock foloseste mereu limba telefonului)
class ZonedDateTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatTextView(context, attrs) {

    private val zone: ZoneId
    private val locale: Locale
    private val formatter: DateTimeFormatter

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ZonedDateTextView)
        zone = ZoneId.of(a.getString(R.styleable.ZonedDateTextView_timeZone) ?: "UTC")
        locale = Locale.forLanguageTag(a.getString(R.styleable.ZonedDateTextView_language) ?: "en")
        formatter = DateTimeFormatter.ofPattern(
            a.getString(R.styleable.ZonedDateTextView_pattern) ?: "EEEE", locale
        )
        a.recycle()
    }

    // Ziua se schimba doar la fix de minut, deci e suficient sa actualizam o data pe minut
    private val ticker = object : Runnable {
        override fun run() {
            update()
            postDelayed(this, 60_000 - System.currentTimeMillis() % 60_000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    private fun update() {
        // In romana zilele se scriu cu litera mica ("marți"); o facem mare pentru afisare
        text = ZonedDateTime.now(zone).format(formatter)
            .replaceFirstChar { it.titlecase(locale) }
    }
}
