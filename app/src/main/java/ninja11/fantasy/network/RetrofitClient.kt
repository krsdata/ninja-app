package ninja11.fantasy.network

import android.content.Context
import ninja11.fantasy.utils.BindingUtils.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import skoreon.fantasy.network.PlugDataService


class RetrofitClient(val  context:Context) {
    private var retrofit: Retrofit? = null
    val service: PlugDataService
        get() {
            if (retrofit == null) {
//                if(BuildConfig.DEBUG) {
//                    val logging = HttpLoggingInterceptor()
//                    logging.level = HttpLoggingInterceptor.Level.BODY
//                    var httpClient = OkHttpClient.Builder()
//                    httpClient.addInterceptor(logging);
//                    retrofit = Retrofit.Builder()
//                        .baseUrl(BASE_URL)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .client(httpClient.build())
//                        .build()
//                }else {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
//                }

            }
            return retrofit!!.create(PlugDataService::class.java)
        }
}