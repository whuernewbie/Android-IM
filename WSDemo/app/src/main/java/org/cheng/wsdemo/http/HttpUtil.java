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

    public static void postDataWithIdAndPsd(final String address,String userId,String password,final okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("uid",userId);//传递键值对参数
        formBody.add("password",password);
        Request request = new Request.Builder()//创建Request 对象。
                .url(address)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void postEmail(final String address,String email,final okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("email",email);//传递键值对参数
        Request request = new Request.Builder()//创建Request 对象。
                .url(address)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static  void postFindFrinds(final String address,String uid,final okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("uid",uid);//传递键值对参数
        Request request = new Request.Builder()//创建Request 对象。
                .url(address)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void postVcodeAndPsd(final String address,String email,String password,String vcode,final okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("auth",vcode);//传递键值对参数
        formBody.add("email",email);
        formBody.add("uname","xiaohu");
        formBody.add("password",password);
        Request request = new Request.Builder()//创建Request 对象。
                .url(address)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(callback);
    }
}
