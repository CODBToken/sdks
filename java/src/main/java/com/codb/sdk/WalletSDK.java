package com.codb.sdk;

import com.codb.sdk.model.Platform;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.SSLSocketFactory;

public final class WalletSDK {

    public static Api create(String no, String accessKey, String secretKey) {
        Platform platform = new Platform(no, accessKey, secretKey);
        Logger logger = LoggerFactory.getLogger("OkHttp");
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger::info);
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(System.out::println);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        SSLSocketFactory factory = SSLFactory.create();
        if (factory != null) {
            builder.sslSocketFactory(factory, SSLFactory.getX509TrustManager());
        }
        builder.hostnameVerifier((s, sslSession) -> s.equals("wallet.codbtoken.com"));
        AuthorizationExpiredInterceptor authorizationExpiredInterceptor = new AuthorizationExpiredInterceptor();
        builder.addInterceptor(authorizationExpiredInterceptor);
        CodecInterceptor codecInterceptor = new CodecInterceptor();
        builder.addInterceptor(codecInterceptor);
        builder.addInterceptor(loggingInterceptor);
        ApiService service = new Retrofit.Builder()
                .baseUrl("https://wallet.codbtoken.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build()
                .create(ApiService.class);
        authorizationExpiredInterceptor.setPlatform(platform);
        authorizationExpiredInterceptor.setApiService(service);
        codecInterceptor.setPlatform(platform);
        return new Api(service);
    }
}
