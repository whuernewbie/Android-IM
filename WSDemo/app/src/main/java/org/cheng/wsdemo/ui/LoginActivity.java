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
import org.cheng.wsdemo.ui.SignIn.SignFirstActivity;
import org.cheng.wsdemo.util.FakeDataUtil;
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

    public static String uid="";

    public static String psd="";

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

        accountEdit.setText(uid);
        accountEdit.setText(psd);

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
                String password = passwordEdit.getText().toString();

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
                                        Intent intent=new Intent(LoginActivity.this,MessagesActivity.class);
                                        startActivity(intent);
                                        FakeDataUtil.SenderUid=account;
                                        finish();
                                    }
                                });
                            }
                        }catch (JSONException e)
                        {
                            //TODO JSON转换错误
                        }
                    }
                    @Override
                    public void onFailure(Call call,IOException e){

                    }

                });
                // 如果账号是admin且密码是123456，就认为登录成功
                if (account.equals("admin") && password.equals("123456")) {
                    editor = pref.edit();
                    if (rememberPass.isChecked()) { // 检查复选框是否被选中
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", account);
                        editor.putString("password", password);
                    } else {
                        editor.clear();
                    }
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MessagesActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "account or password is invalid",
                            Toast.LENGTH_SHORT).show();
                }
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
