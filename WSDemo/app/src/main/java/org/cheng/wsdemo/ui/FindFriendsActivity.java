package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.View.ClearEditText;
import org.cheng.wsdemo.bean.AddFriendsBean;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class FindFriendsActivity extends AppCompatActivity {

    private Button find;

    private TextView findResult;

    private ClearEditText text;

    private String Id="1000000";

    private String name=Id;

    public static final String senderId="senderId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends);

        find=(Button)findViewById(R.id.FindFriends);
        text=(ClearEditText)findViewById(R.id.EditFindId);
        findResult=(TextView)findViewById(R.id.findResult);

        final Intent intent=getIntent();
        Id=intent.getStringExtra(senderId);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 发送查找好友请求
                HttpUtil.postFindFrinds(FakeDataUtil.FindFriendsAddress,text.getText().toString(),new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        try
                        {
                            final JSONObject jsonObject=new JSONObject(responseData);
                            System.out.println("成功成功成功"+jsonObject.toString());
                            if(jsonObject.get("status").toString().equals("ok"))
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findResult.setText(text.getText());
                                        try
                                        {
                                            name=jsonObject.get("uname").toString();
                                        }catch (JSONException e)
                                        {
                                            //TODO jse
                                        }
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
            }
        });

        findResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(FindFriendsActivity.this,UserInfoActivity.class);
                intent1.putExtra(UserInfoActivity.ID,text.getText().toString());
                intent1.putExtra(UserInfoActivity.NAME,name);
                startActivity(intent1);

            }
        });









    }
}
