package com.example.ayazshah.faceassistantglassapp;

import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * Created by QiFeng on 11/21/15.
 */

public class API {

    public static String TAG = API.class.getSimpleName();


    // API ENDPOINT URL
    private static final String SCHEME = "https";
    private static final String HOST_LIVE = "faceassist.us";
    private static final String SEGMENT = "api";
    private static final String VERSION_LIVE = "v1";


    private static String CONTENT_TYPE = "application/json";

    //JSON TYPE
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //PlainText Type
    public static final MediaType EMPTY = MediaType.parse("text/plain; charset=utf-8");

    // API get method
    public static Call get(String[] path,
                           Map<String, String> headers,
                           Map<String, Object> parameters,
                           Callback callback) {

        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).build();
        Headers requestHeaders = Headers.of(headers); //add headers
        HttpUrl.Builder url = new HttpUrl.Builder()   //build url
                .scheme(SCHEME)
                .host(HOST_LIVE)
                .addPathSegment(SEGMENT)
                .addPathSegment(VERSION_LIVE);

        if (path != null) {
            for (String p : path) {
                if (p != null) {
                    url.addPathSegment(p);
                }
            }
        }

        if (parameters != null) {
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) { //add parameters
                url.addQueryParameter(parameter.getKey(), parameter.getValue().toString());
            }
        }

        HttpUrl request = url.build();

        //Log.d(TAG, "get: "+request.toString());
        Call call = client.newCall(new Request.Builder().url(request).headers(requestHeaders).build());
        call.enqueue(callback);
        return call;
    }

    //API POST
    public static Call post(String[] path,
                            Map<String, String> headers,
                            Map<String, Object> parameters,
                            Callback callback) {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //get requestbody
        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).build();
        Headers requestHeaders = Headers.of(headers);   //add headers
        String url = getURL(path);
        //Log.d(TAG, "post: "+url);
        Call call = client.newCall(new Request.Builder().url(url).headers(requestHeaders).post(body).build());
        call.enqueue(callback);
        return call;
    }

    //API POST
    // NOTE: This is a synchronise call
    public static Response postWithLongTimeout(String[] path, Map<String, String> headers, Map<String, Object> parameters)
            throws IOException {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //get requestbody

        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES))
                .build();

        Headers requestHeaders = Headers.of(headers);   //add headers
        String url = getURL(path);

        return client.newCall(
                new Request.Builder()
                        .url(url)
                        .headers(requestHeaders)
                        .post(body)
                        .build())
                .execute();
    }

    //API PUT
    public static Call put(String[] path,
                           Map<String, String> headers,
                           Map<String, Object> parameters,
                           Callback callback) {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //create json
        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).build();
        Headers requestHeaders = Headers.of(headers); //add headers
        String url = getURL(path);
        Call call = client.newCall(new Request.Builder().url(url).put(body).headers(requestHeaders).build());
        call.enqueue(callback);

        return call;
    }

    //API DELETE
    public static Call delete(String[] path,
                              Map<String, String> headers,
                              Map<String, Object> params,
                              Callback callback) {

        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).build();
        Headers requestHeaders = Headers.of(headers); //add headers
        JSONObject json = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, json.toString()); //create json
        HttpUrl.Builder url = new HttpUrl.Builder()   //build url
                .scheme(SCHEME)
                .host(HOST_LIVE)
                .addPathSegment(SEGMENT)
                .addPathSegment(VERSION_LIVE);

        if (path != null) {
            for (String p : path) {
                if (p != null) {
                    url.addPathSegment(p);
                }
            }
        }

        if (params != null) {
            for (Map.Entry<String, Object> parameter : params.entrySet()) { //add parameters
                url.addQueryParameter(parameter.getKey(), parameter.getValue().toString());
            }
        }

        Call call = client.newCall(new Request.Builder().url(url.toString()).delete(body).headers(requestHeaders).build());
        call.enqueue(callback);

        return call;
    }

    public static String getURL(String[] path) {
        StringBuilder url = new StringBuilder();
        url.append(SCHEME)
                .append("://")
                .append(HOST_LIVE)
                .append("/")
                .append(SEGMENT)
                .append("/")
                .append(VERSION_LIVE);

        for (String p : path)
            url.append("/").append(p);


        return url.toString();
    }

    public static HashMap<String, String> getMainHeader(String token) {
        HashMap<String, String> header = new HashMap<String,String>();
        header.put("Authorization", token);
        header.put("Content-Type", CONTENT_TYPE);
        return header;
    }

    public static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<ConnectionSpec>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }
}
