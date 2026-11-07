package com.my31bandeq

import android.app.Activity
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private var eq: Equalizer? = null
    private lateinit var status: TextView
    private val centers = intArrayOf(
        20,25,31,40,50,63,80,100,125,160,200,250,315,400,500,630,
        800,1000,1250,1600,2000,2500,3150,4000,5000,6300,8000,10000,
        12500,16000,20000
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(0xFF101820.toInt())
        }
        val title = TextView(this).apply {
            text = "My31BandEQ"
            textSize = 26f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            text = "กำลังตรวจสอบระบบเสียง..."
            textSize = 14f
            setTextColor(0xFFFFD166.toInt())
            setPadding(0, 12, 0, 12)
        }
        val power = Switch(this).apply {
            text = "เปิด / ปิด EQ"
            setTextColor(0xFFFFFFFF.toInt())
            setOnCheckedChangeListener { _, on -> eq?.enabled = on }
        }
        val presets = Spinner(this)
        val names = arrayOf("กำหนดเอง", "Rock", "Pop", "Jazz", "Bass")
        presets.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        presets.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                applyPreset(position)
            }
        }

        val scroll = ScrollView(this)
        val bands = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        centers.forEachIndexed { index, hz ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 7, 0, 7)
            }
            val label = TextView(this).apply {
                text = "$hz Hz"
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
            }
            val seek = SeekBar(this).apply {
                max = 120
                progress = 60
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar?, value: Int, fromUser: Boolean) {
                        if (fromUser) setBand(index, value - 60)
                    }
                    override fun onStartTrackingTouch(s: SeekBar?) {}
                    override fun onStopTrackingTouch(s: SeekBar?) {}
                })
            }
            row.addView(label)
            row.addView(seek)
            bands.addView(row)
        }
        scroll.addView(bands)
        page.addView(title)
        page.addView(status)
        page.addView(power)
        page.addView(TextView(this).apply {
            text = "พรีเซ็ต"; setTextColor(0xFFFFFFFF.toInt())
        })
        page.addView(presets)
        page.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(page)
        openEqualizer()
    }

    private fun openEqualizer() {
        try {
            // Session 0 requests a device/global effect. Many Android devices block or ignore this.
            eq = Equalizer(0, 0)
            val e = eq!!
            e.enabled = true
            status.text = "พบระบบ EQ: ${e.numberOfBands} แบนด์จากเครื่อง"
            if (e.numberOfBands.toInt() != 31) {
                status.append("\nหมายเหตุ: เครื่องมี ${e.numberOfBands} แบนด์จริง ไม่ใช่ 31 แบนด์เต็ม")
            }
        } catch (ex: Exception) {
            status.text = "เครื่องไม่อนุญาต EQ ระดับระบบ: ${ex.javaClass.simpleName}\nอาจใช้กับ YouTube ไม่ได้"
        }
    }

    private fun setBand(index: Int, db: Int) {
        try {
            val e = eq ?: return
            if (index >= e.numberOfBands) return
            val range = e.bandLevelRange
            val level = (db * 100).coerceIn(range[0].toInt(), range[1].toInt())
            e.setBandLevel(index.toShort(), level.toShort())
        } catch (_: Exception) {}
    }

    private fun applyPreset(which: Int) {
        val e = eq ?: return
        try {
            for (i in 0 until e.numberOfBands) {
                val value = when (which) {
                    1 -> if (i < 5) 350 else if (i > 20) 250 else 0
                    2 -> if (i in 8..18) 250 else 0
                    3 -> if (i in 4..8 || i in 17..22) 200 else 0
                    4 -> if (i < 8) 500 else 0
                    else -> 0
                }
                val r = e.bandLevelRange
                e.setBandLevel(i.toShort(), value.coerceIn(r[0].toInt(), r[1].toInt()).toShort())
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        eq?.release()
        eq = null
        super.onDestroy()
    }
}
