package name.kingbright.android.brilliant.net;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import name.kingbright.android.brilliant.extensions.retrofit2.RxJavaCallAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Jin Liang
 * @since 16/3/8
 */
public class BNet {
    private static final String TAG = "BNet";

    private BNet() {
        // not allowed to create instance
    }

    /**
     * Create a restful service builder.
     *
     * @param config
     * @return
     */
    public static <T> T createService(Config config, final Class<T> service) {
        if (config == null) {
            throw new NullPointerException("Config should not be null.");
        }

        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        okBuilder.connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Log.d(TAG, "request : " + request.toString());

                        Response response = chain.proceed(request);
                        Log.d(TAG, "response : " + response.toString());
                        return response;
                    }
                });

        Retrofit.Builder retroBuilder = new Retrofit.Builder();
        retroBuilder.baseUrl(config.baseUrl);
        retroBuilder.client(okBuilder.build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create(Schedulers.io(), AndroidSchedulers.mainThread()))
                .addConverterFactory(config.factory);

        return new RetrofitServiceBuilder(retroBuilder.build()).create(service);
    }

    public static class Config {
        public String baseUrl;
        public Converter.Factory factory;

        public Config() {
            this(null, null);
        }

        public Config(String url) {
            this(url, null);
        }

        public Config(String url, Converter.Factory factory) {
            this.baseUrl = url;
            this.factory = factory;
        }

    }

    static class RetrofitServiceBuilder implements ServiceBuilder {
        private Retrofit retrofit;

        public RetrofitServiceBuilder(Retrofit retrofit) {
            this.retrofit = retrofit;
        }

        public <T> T create(final Class<T> service) {
            return retrofit.create(service);
        }
    }

    static class VolleyServiceBuilder implements ServiceBuilder {

        @Override
        public <T> T create(Class<T> service) {
            return null;
        }
    }

    interface ServiceBuilder {
        <T> T create(final Class<T> service);
    }

}
