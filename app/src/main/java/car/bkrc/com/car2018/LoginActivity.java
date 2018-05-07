package car.bkrc.com.car2018;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bkrc.camera.XcApplication;
import java.util.Timer;
import java.util.TimerTask;
import dialog.bkrc.com.waitprogressdialog.ProgressDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText device_edit =null;
    private EditText login_edit = null;
    private EditText passwd_edit = null;

    private Button bt_reset = null;
    private Button bt_connect = null;
    private CheckBox rememberbox = null;
    private Switch transmitswitch = null;
    private TextView usbserialdata = null;
    private String str_deviceid = null;
    private String str_loginname = null;
    private String str_passwd = null;

//使用SharedPreferences来存储设备id、用户名、密码，在程序在没有用到，作为程序的扩展
    private SharedPreferences preferences = null;//用于访问
    private SharedPreferences.Editor editor = null;//用于写入
    private WifiManager wifiManager;
    // 服务器管理器
    private DhcpInfo dhcpInfo;

    private boolean isstarted = false;
    private ProgressDialog dialog =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        widget_init();  //控件初始化
       // read_sharedproferences();  //获取haredproferences中保存的数据
        Camer_Init();//摄像头初始化
    }

    @Override
    protected void onResume() {
        super.onResume();
        dialog =ProgressDialog.getInstance(LoginActivity.this);
        dialog.showWaitPrompt();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                search();						//开启Service 搜索摄像头IP
            }
        },1200);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(FirstActivity.IPCamera ==null) {
                    exitHandler.sendEmptyMessage(10);

                }
            }
        },5000);
    }

    private Handler  exitHandler =new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what ==10) {
                Toast.makeText(LoginActivity.this, "摄像头连接不上", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };

    private void widget_init() {
        device_edit =(EditText) findViewById(R.id.deviceid);
        login_edit = (EditText) findViewById(R.id.loginname);
        passwd_edit = (EditText) findViewById(R.id.loginpasswd);
        bt_reset = (Button) findViewById(R.id.reset);
        bt_connect = (Button) findViewById(R.id.connect);
        rememberbox = (CheckBox) findViewById(R.id.remember);
        rememberbox.setChecked(false);
        transmitswitch = (Switch) findViewById(R.id.transmit_way);

        bt_reset.setOnClickListener(this);
        bt_connect.setOnClickListener(this);
        transmitswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton Button, boolean isChecked) {
                if (isChecked) {
                    XcApplication.isserial = XcApplication.Mode.USB_SERIAL;
                    Button.setText("使用usb转uart");
                    Toast.makeText(LoginActivity.this, "用usb转uart通信", Toast.LENGTH_SHORT).show();
                } else {
                    XcApplication.isserial = XcApplication.Mode.SOCKET;
                    Button.setText("使用socket");
                    Toast.makeText(LoginActivity.this, "用socket通信", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void read_sharedproferences() {
        preferences = getSharedPreferences("login_data", MODE_PRIVATE);
        editor = preferences.edit();
        Boolean ischecked = preferences.getBoolean("ischecked", false);
        if (ischecked) {
            str_deviceid = preferences.getString("deviceid", null);
            str_loginname = preferences.getString("loginname", null);
            str_passwd = preferences.getString("passwd", null);
              device_edit.setText(str_deviceid);
            login_edit.setText(str_loginname);
            passwd_edit.setText(str_passwd);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.reset) {
            device_edit.setText("");
            login_edit.setText("");
            passwd_edit.setText("");
            rememberbox.setChecked(false);
        } else if (view.equals(bt_connect)) {
            str_deviceid =device_edit.getText().toString();
            str_loginname = login_edit.getText().toString();
            str_passwd = passwd_edit.getText().toString();

            if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
                use_network();
            }

            if (XcApplication.isserial != XcApplication.Mode.SOCKET) {
                Intent ipintent = new Intent();
                //ComponentName的参数1:目标app的包名,参数2:目标app的Service完整类名
                ipintent.setComponent(new ComponentName("com.android.settings", "com.android.settings.ethernet.CameraInitService"));
                //设置要传送的数据
                ipintent.putExtra("purecameraip", FirstActivity.purecameraip);
                startService(ipintent);   //摄像头设为静态192.168.16.20时，可以不用发送
            }

            if (FirstActivity.IPCamera != null) {
                startActivity(new Intent(LoginActivity.this, FirstActivity.class));
            }
        }
    }

    private void use_network() {
        wifi_Init(); //WiFi初始化
    }

    private void wifi_Init() {
        // 得到服务器的IP地址
        wifiManager = (WifiManager) getApplicationContext(). getSystemService(Context.WIFI_SERVICE);
        dhcpInfo = wifiManager.getDhcpInfo();
        FirstActivity.IPCar = Formatter.formatIpAddress(dhcpInfo.gateway);
    }

    // 广播名称
    public static final String A_S = "com.a_s";
    // 广播接收器接受SearchService搜索的摄像头IP地址加端口
    private int time = 2; //连续扫描的次数
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            FirstActivity.IPCamera = arg1.getStringExtra("IP");
            FirstActivity.purecameraip = arg1.getStringExtra("pureip");
            Log.e("camera ip::", "  " + FirstActivity.IPCamera.toString());

            dialog.disMiss();
            dialog =null;
        }
    };

        private void Camer_Init() {
            //广播接收器注册
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(A_S);
            registerReceiver(myBroadcastReceiver, intentFilter);
            // 搜索摄像头图片工具
        }

        // 搜索摄像cameraIP进度条
        private void search() {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, SearchService.class);
            startService(intent);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(myBroadcastReceiver);
            if(dialog != null) {
                dialog.disMiss();
                dialog =null;
            }
        }

    }
