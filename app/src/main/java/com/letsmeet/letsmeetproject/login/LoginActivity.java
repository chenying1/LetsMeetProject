package com.letsmeet.letsmeetproject.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.letsmeet.letsmeetproject.MainActivity;
import com.letsmeet.letsmeetproject.R;
import com.letsmeet.letsmeetproject.http.HttpUtil;
import com.letsmeet.letsmeetproject.register.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements HttpUtil.HttpResponse {

    private Context context = this;
    private AutoCompleteTextView userView;
    private EditText passwordView;

    private final static String TAG = "LoginActivity";

    private HttpUtil httpUtil;
    private String url = "login";
    private String user;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestLocationPermission();

        // Set up the login form.
        userView = (AutoCompleteTextView) findViewById(R.id.user);
        passwordView = (EditText) findViewById(R.id.password);

        MyOnclickListener onclickListener = new MyOnclickListener();

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(onclickListener);

        TextView new_user_register = (TextView) findViewById(R.id.new_user_register);
        new_user_register.setOnClickListener(onclickListener);
        httpUtil = new HttpUtil(url,this);
    }

    private class MyOnclickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.sign_in_button:
//                    attemptLogin();
                    loginIgnore();
                    break;
                case R.id.new_user_register:
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    break;
                case R.id.forget_password:
                    break;
            }
        }
    }

    @Override
    public void httpResponseCallback(String responseResult) {
        Log.e(TAG,"responseResult:"+responseResult);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user",user);
        context.startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid user, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Store values at the time of the login attempt.
        //用户名和密码
        user = userView.getText().toString();
        password = passwordView.getText().toString();
        md5(password);
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user)) {
            userView.setError(getString(R.string.error_field_required));
        } else if (!isEmailValid(user)) {
            userView.setError(getString(R.string.error_invalid_email));
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user",user);
            jsonObject.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpUtil.sendMsg(jsonObject.toString());


    }

    //测试其他功能时可以使用此代码，将登录功能的验证屏蔽掉，直接跳转到主界面。
    private void loginIgnore(){
        user = userView.getText().toString();
        password = passwordView.getText().toString();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user",user);
        context.startActivity(intent);
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
//        return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * 获取wifi列表必须获得位置权限
     */
    public void requestLocationPermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//如果 API level 是小于等于 23(Android 6.0) 时 不需要显式申请权限
            return;
        }
        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            //判断是否需要向用户解释为什么需要申请该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);
            }
            //请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            Log.e(TAG,"md5:"+result);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}



