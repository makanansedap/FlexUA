package xl.iot.aces.flexua

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var onOffState = false
        runBtn.isEnabled = onOffState

        onOffBtn.setOnClickListener {
            val changeState = { aBool : Boolean ->
                onOffState = aBool
                runBtn.isEnabled = aBool
            }
            if (onOffState) {
                (it as Button).setText(R.string.on)
                it.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, android.R.drawable.button_onoff_indicator_on)
            } else {
                (it as Button).setText(R.string.off)
                it.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, android.R.drawable.button_onoff_indicator_off)
            }
        }

        runBtn.setOnClickListener {

        }
    }
}
