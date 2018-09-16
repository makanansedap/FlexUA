package xl.iot.aces.flexua

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
            val executeService = ExecuteActionService.create()
            val pref = getSharedPreferences("ACCESS", Context.MODE_PRIVATE)
            if (!onOffState) {
                val executeServiceResponse = executeService.execute(pref.getString("AUTHORIZATION", ""), pref.getString("X_IOT_JWT", ""), ExecuteActionBody("deviceControl", DEVICE_ID, "turnOn", pref.getInt("USER_ID", 0), ActionParameter(SERIAL)))
                executeServiceResponse.observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            changeState(!onOffState)
                            onOffBtn.setText(R.string.on)
                            onOffBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, android.R.drawable.button_onoff_indicator_on)
                        }, { Toast.makeText(this@HomeActivity, "Unable to turn the device on", Toast.LENGTH_SHORT).show(); it.printStackTrace() })
            } else {
                val executeServiceResponse = executeService.execute(pref.getString("AUTHORIZATION", ""), pref.getString("X_IOT_JWT", ""), ExecuteActionBody("deviceControl", DEVICE_ID, "turnOff", pref.getInt("USER_ID", 0), ActionParameter(SERIAL)))
                executeServiceResponse.observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            changeState(!onOffState)
                            onOffBtn.setText(R.string.off)
                            onOffBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, android.R.drawable.button_onoff_indicator_off)
                        }, { Toast.makeText(this@HomeActivity, "Unable to turn the device off", Toast.LENGTH_SHORT).show(); it.printStackTrace() })
            }
        }

        runBtn.setOnClickListener {
            val executeService = ExecuteActionService.create()
            val pref = getSharedPreferences("ACCESS", Context.MODE_PRIVATE)
            val executeServiceResponse = executeService.execute(pref.getString("AUTHORIZATION", ""), pref.getString("X_IOT_JWT", ""), ExecuteActionBody("deviceControl", DEVICE_ID, "startTest", pref.getInt("USER_ID", 0), ActionParameter(SERIAL)))
            executeServiceResponse.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        Toast.makeText(this@HomeActivity, "Running the test...", Toast.LENGTH_SHORT).show()
                        runBtn.isEnabled = false
                    }, { Toast.makeText(this@HomeActivity, "Unable to run the test on the device", Toast.LENGTH_SHORT).show(); it.printStackTrace() })
        }
    }
}
