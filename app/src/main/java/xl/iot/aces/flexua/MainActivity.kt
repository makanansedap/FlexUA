package xl.iot.aces.flexua

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context
import android.content.Intent
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            startBtn.isEnabled = false
            startBtn.setText(R.string.starting)
            val apiService = APIAuthorzationService.create()
            var apiServiceResponse = apiService.request(X_SECRET)
            val errorBehaviour = { e : Throwable ->
                e.printStackTrace()
                startBtn.isEnabled = true
                startBtn.setText(R.string.retry)
                statusTV.setText(R.string.check_ng)
            }
            apiServiceResponse.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        val prefEd = getSharedPreferences("ACCESS", Context.MODE_PRIVATE).edit()
                        prefEd.putString("TOKEN", it.access_token)
                        prefEd.putString("SCOPE", it.scope)
                        prefEd.putString("TOKEN_TYPE", it.token_type)
                        prefEd.apply()
                        val userService = IOTUserService.create()
                        val userServiceResponse = userService.authenticate(it.token_type + " " + it.access_token, IOTUserRequestBody(USERNAME, PASSWORD))
                        userServiceResponse.observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({
                                    val prefEd = getSharedPreferences("ACCESS", Context.MODE_PRIVATE).edit()
                                    prefEd.putString("X_IOT_JWT", it.`X-IoT-JWT`)
                                    prefEd.putInt("USER_ID", it.data.userId)
                                    prefEd.apply()
                                    startBtn.isEnabled = true
                                    startBtn.setText(R.string.start)
                                    statusTV.setText(R.string.check_ok)
                                    progressBar.visibility = View.INVISIBLE
                                    startActivity(Intent(this, HomeActivity::class.java))
                                }, errorBehaviour)
                    }, errorBehaviour)
        }
    }
}
