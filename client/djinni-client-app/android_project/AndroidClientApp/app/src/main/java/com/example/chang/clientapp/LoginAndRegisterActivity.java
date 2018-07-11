package com.example.chang.clientapp;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginAndRegisterActivity extends AppCompatActivity {
	/**
	 * 请求成功
	 */
	private static final int REPLY_SUCCESS = 0;
	/**
	 * 请求失败
	 */
	private static final int REPLY_FAILURE = -1;
	/**
	 * native api
	 */
	private Clent mCppApi;
	/**
	 * 返回实体
	 */
	private Reply mReply;
	/**
	 * 登录/注册按钮
	 */
	private Button mGeneralButton;
	/**
	 * 页面标题
	 */
	private TextView mTitleText;

	static {
		System.loadLibrary("client-lib");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCppApi = Cient.create();
		mTitleText = findViewById(R.id.title_text);
		mGeneralButton = findViewById(R.id.login_btn);

		mTitleText.setText("登录");
		mGeneralButton.setText("登录");
		final EditText userName = findViewById(R.id.user_name);
		final EditText password = findViewById(R.id.user_pwd);
		mGeneralButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReply = mCppApi.check_auth(userName.getText().toString(), getAuth(userName.getText().toString()));
				//auth失效（比如被其他设备挤掉或者过期）-> 重新登录
				if (mReply.resultCode != REPLY_SUCCESS) {
					mReply = mCppApi.login(userName.getText().toString(), password.getText().toString(), getDeviceId());
					if (mReply.resultCode == REPLY_SUCCESS) {
						Toast.makeText(LoginAndRegisterActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
						redirectToHomePage();
						//可以在此处保存auth，也可以在djinni的中间层保存
					} else {
						Toast.makeText(LoginAndRegisterActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
					}
				} else {
					//auth仍然有效 -> 无需登录
					redirectToHomePage();
				}

			}
		});

		final View registerText = findViewById(R.id.register_text);
		registerText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//跳转注册页(此处仅在本页面做控件重用)
				registerText.setVisibility(View.GONE);
				mTitleText.setText("注册");
				mGeneralButton.setText("注册");
				mGeneralButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mReply = mCppApi.register(userName.getText().toString(), password.getText().toString(), getDeviceId());
						if (mReply.resultCode == REPLY_SUCCESS) {
							Toast.makeText(LoginAndRegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
							redirectToHomePage();
							//可以在此处保存auth，也可以在djinni的中间层保存
						} else {
							Toast.makeText(LoginAndRegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
						}
					}
				});

			}
		});
	}

	/**
	 * 获取设备id
	 *
	 * @return 设备IMEI
	 */
	@SuppressLint("MissingPermission")
	private String getDeviceId() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		return telephonyManager != null ? telephonyManager.getDeviceId() : "";
	}

	/**
	 * 获取用户名对应的auth
	 *
	 * @param userName 用户名
	 * @return auth
	 */
	private String getAuth(String userName) {
		//TODO 待实现
		return "";
	}

	/**
	 * 跳转到业务页面
	 */
	private void redirectToHomePage() {
		//TODO 跳转到业务页面
	}
}
