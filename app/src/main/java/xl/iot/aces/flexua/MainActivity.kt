package xl.iot.aces.flexua

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import kotlinx.android.synthetic.main.activity_main.*
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.app.ProgressDialog
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Handler
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var progressStatus = 0
        val handler = Handler()

        startBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            startBtn.isEnabled = false
            startBtn.text = "Now Analyzing..."
            Thread(Runnable {
                while (progressStatus < 100) {
                    progressStatus += 1
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(Runnable {
                        progressBar.progress = progressStatus
                        analyzeTV.text = progressStatus.toString() + "/" + progressBar.max
                    })
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(200)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                progressBar.visibility = View.INVISIBLE
            }).start()
        }
    }
}
