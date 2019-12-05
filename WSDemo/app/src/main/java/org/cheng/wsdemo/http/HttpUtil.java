package org.cheng.wsdemo.http;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void getOkHttpRequest(final String address, final okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void postDataWithParame(final String address,String userId,String password,final okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("uname",userId);//传递键值对参数
        formBody.add("password",password);
        Request request = new Request.Builder()//创建Request 对象。
                .url(address)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(callback);
    }
}
