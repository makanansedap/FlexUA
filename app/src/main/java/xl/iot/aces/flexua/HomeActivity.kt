package xl.iot.aces.flexua

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.entries
import android.graphics.Color
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry




class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var onOffState = false
        runBtn.isEnabled = onOffState



        val yAxis = barChart.axisLeft
        yAxis.setDrawLabels(false)
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(false)
        yAxis.setDrawZeroLine(true)
        yAxis.labelCount = 20
        yAxis.setLabelCount(3, true)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.textColor = Color.RED
        xAxis.granularity = 1f
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)

        val entry1 = ArrayList<BarEntry>()
        val entry2 = ArrayList<BarEntry>()
        val entry3 = ArrayList<BarEntry>()
//        entry1.add(BarEntry(0f, 40f))
//        entry2.add(BarEntry(1f, 80f))
//        entry3.add(BarEntry(2f, 60f))
        val set1 = BarDataSet(entry1, "Biuret")
        set1.setColors(Color.BLUE)
        val set2 = BarDataSet(entry2, "Benedict")
        set2.setColors(Color.RED)
        val set3 = BarDataSet(entry3, "Dehydration")
        set3.setColors(Color.YELLOW)
        val data = BarData(set1, set2, set3)
        barChart.data = data
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.invalidate()

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
                        var testReturned = false
                        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        Log.d("TIMESTAMP", timestamp)
                        Thread {
                            while (!testReturned) {
                                val dataService = IOTDataService.create()
                                val dataServiceResponse = dataService.data(pref.getString("AUTHORIZATION", ""), pref.getString("X_IOT_JWT", ""), "submitResult", DEVICE_ID.toString(), timestamp, "1", "Asia/Jakarta", "biuret,benedict,dehydration")
                                dataServiceResponse.observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({
                                            testReturned = true
                                            Log.d("TEST RESULT", it.toString())
                                            entry1.add(BarEntry(0f, it.device[0].biuret.toFloat()))
                                            entry2.add(BarEntry(1f, it.device[0].benedict.toFloat()))
                                            entry3.add(BarEntry(2f, it.device[0].dehydration.toFloat()))
                                            barChart.invalidate()
                                        }, { it.printStackTrace() })
                                Thread.sleep(15000)
                            }
                        }.start()
                    }, { Toast.makeText(this@HomeActivity, "Unable to run the test on the device", Toast.LENGTH_SHORT).show(); it.printStackTrace() })
        }
    }
}
