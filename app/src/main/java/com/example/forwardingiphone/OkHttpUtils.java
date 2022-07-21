package com.example.forwardingiphone;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtils {
    public static Headers SetHeaders(Map<String, Object> headersParams) {
        Headers headers;
        Headers.Builder headersbuilder = new Headers.Builder();
        if (headersParams!=null) {
            Iterator<String> iterator = headersParams.keySet().iterator();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next();
                headersbuilder.add(key, (String) headersParams.get(key));
            }
        }
        headers = headersbuilder.build();
        return headers;
    }
    public static String getParams(Map<String, Object> params) {
        int i=0;
        StringBuffer sb = new StringBuffer("?");
        if (params!=null) {
            for (Map.Entry<String, Object> item : params.entrySet()) {
                Object value = item.getValue();
                if (value!=null&&value!="") {
                    if(i>0)
                        sb.append("&");
                    sb.append(item.getKey());
                    sb.append("=");
                    sb.append(value);
                    i++;
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    public static String get(String url, Map<String, Object> params, Map<String, Object> headers) throws Exception {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(128);
        dispatcher.setMaxRequests(128);
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(64,10,TimeUnit.MINUTES))
                .connectTimeout(60, TimeUnit.SECONDS)
                .dns(Dns.SYSTEM)
                .writeTimeout(60,TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS)
                .build();
        Headers setHeaders = SetHeaders(headers);
        if(params.size() > 0) {
            url += getParams(params);
        }
        Request request = (new Request.Builder()).url(url).headers(setHeaders).build();

        Response response = okHttpClient.newCall(request).execute();

        ResponseBody resp = response.body();
        return resp == null ? null : resp.string();
    }
    static MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    synchronized
    public static String postFile(String url, Map<String, Object> params, Map<String, Object> headers, File file) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS)
                .build();
        Headers setHeaders = SetHeaders(headers);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"),
                                file)).build();
        Request request = (new Request.Builder())
                .url(url)
                .headers(setHeaders)
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        ResponseBody resp = response.body();
        return resp == null ? null : resp.string();
    }

    public static String sendPost(String url, String json, Map<String, Object> params, Map<String, Object> headers) throws Exception {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(128);
        dispatcher.setMaxRequests(128);
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectionPool(new ConnectionPool(64,10,TimeUnit.MINUTES))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20,TimeUnit.SECONDS)
                .writeTimeout(20,TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .dns(Dns.SYSTEM)
                .build();
        FormBody.Builder builder = new FormBody.Builder();
        MediaType mediaType = MediaType.parse("application/json");

        RequestBody requestBody;
        url += getParams(params);
        Request.Builder url1 = (new Request.Builder()).url(url);
        if(headers!=null) {
            for (String key : headers.keySet()) {
                url1.addHeader(key, headers.get(key) + "");
            }
        }


        requestBody = RequestBody.create(mediaType,json);

        Request request = url1.post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return response.body() == null ? null : response.body().string();
    }
}
