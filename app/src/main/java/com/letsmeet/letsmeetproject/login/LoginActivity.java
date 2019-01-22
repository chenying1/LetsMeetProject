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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.letsmeet.letsmeetproject.MainActivity;
import com.letsmeet.letsmeetproject.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private Context context = this;
    private AutoCompleteTextView userView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestLocationPermission();

        // Set up the login form.
        userView = (AutoCompleteTextView) findViewById(R.id.user);
        passwordView = (EditText) findViewById(R.id.password);

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        userView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        //用户名和密码
        final String email = userView.getText().toString();
        final String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            userView.setError(getString(R.string.error_field_required));
            focusView = userView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            userView.setError(getString(R.string.error_invalid_email));
            focusView = userView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user",email);
            context.startActivity(intent);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        String urlstring = "http://222.20.73.120:8080/login";
//                        URL url = new URL(urlstring);
//                        //得到connection对象。
//                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                        //设置请求方式
//                        connection.setRequestMethod("POST");
//                        connection.setDoOutput(true);
//
//                        // 设置文件类型:
//                        connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
//
//                        //连接
//                        connection.connect();
//                        //发送数据
//                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("user",email);
//                        jsonObject.put("password",password);
//                        wr.writeBytes(jsonObject.toString());
//                        wr.flush();
//                        wr.close();
//                        //得到响应码
//                        int responseCode = connection.getResponseCode();
//                        if(responseCode == HttpURLConnection.HTTP_OK){  //200
//                            //得到响应流
//                            InputStream inputStream = connection.getInputStream();
//                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
//                            String result = reader.readLine();
//                            //将响应流转换成字符串
////                            String result = inputStream.toString();//将流转换为字符串。
//                            Log.e("kwwl","result============="+result);
//                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                            context.startActivity(intent);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
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

}

