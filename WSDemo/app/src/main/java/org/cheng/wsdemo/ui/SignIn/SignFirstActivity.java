package org.cheng.wsdemo.ui.SignIn;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.ui.BaseActivity;

public class SignFirstActivity extends BaseActivity {

    private EditText email;

    private EditText id;

    private Button sendId;

    private Button next;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_first);

        email=(EditText)findViewById(R.id.email);
        id=(EditText)findViewById(R.id.id);
        sendId=(Button)findViewById(R.id.sendEmail);
        next=(Button)findViewById(R.id.step1_next);

        sendId.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO
            }
        });

        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO
            }
        });


    }
}
