package xl.iot.aces.flexua

import com.google.gson.GsonBuilder
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.security.cert.CertificateException
import javax.net.ssl.*

val BASE_URL      = "https://flexiot.xl.co.id/"
val SERVER_URL    = "http://52.221.141.22:8080"
val EVENT_URL     = "http://52.221.141.22:8080/api/pcs/Generic_brand_745GENERIC_DEVICEv3"
val DEVICE_ID     = "1268068829839903"
val X_SECRET      = "YUhGREQwZjNBbEdOUmZNSmNraFZ0dzJLVUlVYTpOakFyYzFBOEFNSzBoQzZTUnpxRDFBM1k1cVVh"
val USERNAME      = "ellianto@student.umn.ac.id"
val PASSWORD      = "MaRooN55"
val CONTENT_TYPE  = "application/json"


class UnsafeOkHttpClient {
    companion object {
        fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
            try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }

                return builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}

data class APIAuthorizationResponse(
        val access_token: String,
        val scope: String,
        val token_type: String,
        val expires_in: Int
)
interface APIAuthorzationService {
    @GET("/api/applicationmgt/authenticate")
    fun request(
            @Header("X-Secret") xSecret: String
    ) : Observable<APIAuthorizationResponse>

    companion object Factory {
        fun create(): APIAuthorzationService {
            val retrofit = Retrofit.Builder()
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()!!
            return retrofit.create(APIAuthorzationService::class.java)
        }
    }
}

data class IOTUserRequestBody(
        val username: String,
        val password: String
)

data class IOTUserResponseData(
        val email: String,
        val phone: String,
        val userId: Int
)

data class IOTUserResponse(
        val auth: Boolean,
        val `X-IoT-JWT`: String,
        val data: IOTUserResponseData
)

interface IOTUserService {
    @POST("/api/usermgt/v1/authenticate")
    @Headers("Content-Type: application/json;charset=utf-8")
    fun authenticate(
            @Header("Authorization") authorization: String,
            @Body body: IOTUserRequestBody
    ) : Observable<IOTUserResponse>

    companion object Factory {
        fun create(): IOTUserService {
            val retrofit = Retrofit.Builder()
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()!!
            return retrofit.create(IOTUserService::class.java)
        }
    }
}

data class DeviceResponse(
        val parameter: String,
        val state: String,
        val value: String
)

data class ExecuteActionResponse(
        val deviceResponse: List<DeviceResponse>
)

data class ExecuteActionBody(
        val operation: String,
        val deviceId: String,
        val actionName: String,
        val userId: Int,
        val actionParameters: Any
)

interface ExecuteActionService {
    @POST("/api/userdevicecontrol/v1/devices/executeaction")
    fun execute(
            @Header("Authorization") authorization: String,
            @Header("X-IoT-JWT") xIotJwt: String,
            @Query("body") body: Any
    ) : Observable<ExecuteActionResponse>
}