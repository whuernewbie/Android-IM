package org.cheng.wsdemo.ui.SignIn;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.ui.BaseActivity;

public class SignSecondActivity extends BaseActivity {
    private EditText password;

    private EditText rpassword;

    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_second);

        password=(EditText)findViewById(R.id.password);
        rpassword=(EditText)findViewById(R.id.rpassword);
        login=(Button)findViewById(R.id.finish);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }
}
