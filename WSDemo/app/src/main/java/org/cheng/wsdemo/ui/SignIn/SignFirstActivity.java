package org.cheng.wsdemo.ui.SignIn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.ui.BaseActivity;
import org.cheng.wsdemo.ui.LoginActivity;
import org.cheng.wsdemo.ui.MessagesActivity;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class SignFirstActivity extends BaseActivity {

    private EditText email;

    private EditText id;

    private Button sendId;


    private EditText password;

    private EditText rpassword;

    private Button login;

    private boolean IsvCodeTrue=false;

    private boolean IsAllTrue=false;

    private String uid="";

    private Context mContext;

    public Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    IsvCodeTrue=true;//发送验证码通过
                    break;

                case 1://验证通过
                    IsAllTrue=true;
                    uid=msg.obj.toString();
                    Intent intent=new Intent(mContext,LoginActivity.class);
                    intent.putExtra(LoginActivity.userId,uid);
                    intent.putExtra(LoginActivity.userPsd,password.getText().toString());
                    startActivity(intent);
                    finish();
                    break;
                default: break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_first);

        mContext=SignFirstActivity.this;

        email=(EditText)findViewById(R.id.email);
        id=(EditText)findViewById(R.id.id);
        sendId=(Button)findViewById(R.id.sendEmail);
        password=(EditText)findViewById(R.id.password);
        rpassword=(EditText)findViewById(R.id.rpassword);
        login=(Button)findViewById(R.id.finish);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsvCodeTrue){
                    if(password.getText().toString().equals(rpassword.getText().toString()))
                    {
                        HttpUtil.postVcodeAndPsd(FakeDataUtil.AuthHttpAddress,email.getText().toString(),password.getText().toString(),id.getText().toString(),new okhttp3.Callback(){
                            @Override
                            public void onResponse(Call call, Response response) throws IOException{
                                String responseData=response.body().string();
                                System.out.println(responseData);
                                try
                                {
                                    JSONObject jsonObject=new JSONObject(responseData);
                                    if(jsonObject.get("status").toString().equals("ok"))
                                    {
                                        Message message=new Message();
                                        message.obj=jsonObject.get("uid").toString();
                                        message.what=1;
                                        mhandler.sendMessage(message);
                                    }
                                }catch (JSONException e)
                                {
                                    //TODO 子线程JSON转换错误
                                }
                            }
                            @Override
                            public void onFailure(Call call,IOException e){

                            }

                        });
                    }
                    else
                    {
                        //TODO 提示密码不一致
                    }
                }
                else
                {
                    //TODO 提示先填验证码
                }
                if(IsAllTrue){
                }
            }
        });

        sendId.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                HttpUtil.postEmail(FakeDataUtil.EmailHttpAddress,email.getText().toString(),new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException{
                        String responseData=response.body().string();
                        try{
                            JSONObject jsonObject=new JSONObject(responseData);
                            if(jsonObject.get("status").toString().equals("ok"))
                            {
                                Message message=new Message();
                                message.obj=responseData;
                                message.what=0;
                                mhandler.sendMessage(message);
                            }
                        }catch (JSONException e)
                        {
                            //TODO 子线程JSON转换错误
                        }
                    }
                    @Override
                    public void onFailure(Call call,IOException e){
                        //TODO 子线程http错误
                    }
                });
            }
        });

    }
}
