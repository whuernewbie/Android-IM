package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.ui.SignIn.SignFirstActivity;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.WebSocketUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private EditText accountEdit;

    private EditText passwordEdit;

    private Button login;

    private Button Signin;

    private TextView forgetPsd;

    private CheckBox rememberPass;

    private String uid="";
    private String psd="";

    public static String userId="";

    public static String userPsd="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        login = (Button) findViewById(R.id.login);
        Signin =(Button)findViewById(R.id.signin);
        forgetPsd= (TextView)findViewById(R.id.forget_psd);

        Intent intent=getIntent();
        uid=intent.getStringExtra(userId);
        psd=intent.getStringExtra(userPsd);

        accountEdit.setText(uid);
        passwordEdit.setText(psd);

        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            // 将账号和密码都设置到文本框中
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String account = accountEdit.getText().toString();
                final String password = passwordEdit.getText().toString();

                HttpUtil.postDataWithIdAndPsd(FakeDataUtil.LoginHttpAddress,account,password,new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        try
                        {
                            JSONObject jsonObject=new JSONObject(responseData);
                            System.out.println("成功成功成功"+jsonObject.get("status").toString());
                            if(jsonObject.get("status").toString().equals("ok"))
                            {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        editor = pref.edit();
                                        if (rememberPass.isChecked()) { // 检查复选框是否被选中
                                            editor.putBoolean("remember_password", true);
                                            editor.putString("account", account);
                                            editor.putString("password", password);
                                        } else {
                                            editor.clear();
                                        }
                                        editor.apply();
                                        FakeDataUtil.SenderUid=account;
                                        WebSocketUtil.ROOT_URL=WebSocketUtil.ROOT_URL+account;
                                        Intent intent=new Intent(LoginActivity.this,MessagesActivity.class);
                                        startActivity(intent);
                                        //建立Websocket连接
                                        finish();
                                    }
                                });
                            }
                        }catch (JSONException e)
                        {
                            //TODO 子线程JSON格式转换错误
                        }
                    }
                    @Override
                    public void onFailure(Call call,IOException e){
                        //TODO 子线程http传输错误
                    }

                });
            }
        });

        Signin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, SignFirstActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgetPsd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO 处理忘记密码
            }
        });
    }

}
