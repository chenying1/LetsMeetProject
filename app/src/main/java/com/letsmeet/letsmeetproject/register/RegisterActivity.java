package com.letsmeet.letsmeetproject.register;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.letsmeet.letsmeetproject.R;
import com.letsmeet.letsmeetproject.http.HttpUtil;
import com.letsmeet.letsmeetproject.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements HttpUtil.HttpResponse {

    private HttpUtil httpUtil;
    private String url = "register";
    private AutoCompleteTextView userView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private Button signUpBtn;

    private final String TAG = "RegisterActivity";

    private Context context = this;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
//                    注册失败 提示用户已存在
                    Toast.makeText(context, getString(R.string.username_exist), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar();
        setTitle(this.getString(R.string.register));
        httpUtil = new HttpUtil(url,this);

        userView = (AutoCompleteTextView) findViewById(R.id.user);
        passwordView = (EditText) findViewById(R.id.password);
        confirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        signUpBtn = (Button) findViewById(R.id.sign_up_button);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempRegister();
            }
        });
    }

    private void attempRegister(){

        String user = userView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        if (TextUtils.isEmpty(user)) {
            userView.setError(getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
        }
        if (!password.equals(confirmPassword)){
            confirmPasswordView.setError(getString(R.string.erro_incorrect_confirm_password));
        }

        JSONObject registerJson = new JSONObject();
        try {
            registerJson.put("user",user);
            registerJson.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpUtil.sendMsg(registerJson.toString());
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * http请求回调函数
     * @param responseResult 服务器返回的响应结果
     */
    @Override
    public void httpResponseCallback(String responseResult) {
        JSONObject read ;
        try {
            read = new JSONObject(responseResult);
            int status = read.getInt("status");
            switch (status){
                case 0:
//                    注册成功
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                case 1:
//                    注册失败 用户名已存在
                    handler.obtainMessage(0,null).sendToTarget();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
