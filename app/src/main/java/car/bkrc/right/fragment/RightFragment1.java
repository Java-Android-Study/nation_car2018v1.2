package car.bkrc.right.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.bkrc.camera.XcApplication;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import car.bkrc.com.car2018.FirstActivity;
import car.bkrc.com.car2018.R;

public class RightFragment1 extends Fragment {

    // 接受传感器
    private long psStatus = 0;// 状态
    private long UltraSonic = 0;// 超声波
    private long Light = 0;// 光照
    private long CodedDisk = 0;// 码盘值
    private int angle_data =0;  //角度值
    String Camera_show_ip = null;

    private TextView Data_show =null;
    private EditText speededit = null;
    private EditText coded_discedit =null;
    private EditText angle_dataedit =null;
    private ImageButton up_bt,blew_bt,stop_bt,left_bt,right_bt;

    private byte[] mByte = new byte[60];
    public static final String TAG = "RightFragment1";
    private View view =null;


    public static RightFragment1 getInstance(){
        return RightFragment1Holder.sInstance;
    }

    private static class RightFragment1Holder
    {
        private static final RightFragment1 sInstance = new RightFragment1();
    }

    // 接受显示小车发送的数据
    private Handler rehHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mByte = (byte[]) msg.obj;
                if (mByte[0] == 0x55) {
                    // 光敏状态
                    psStatus = mByte[3] & 0xff;
                    // 超声波数据
                    UltraSonic = mByte[5] & 0xff;
                    UltraSonic = UltraSonic << 8;
                    UltraSonic += mByte[4] & 0xff;
                    // 光照强度
                    Light = mByte[7] & 0xff;
                    Light = Light << 8;
                    Light += mByte[6] & 0xff;
                    // 码盘
                    CodedDisk = mByte[9] & 0xff;
                    CodedDisk = CodedDisk << 8;
                    CodedDisk += mByte[8] & 0xff;

                    Camera_show_ip = FirstActivity.IPCamera.substring(0, 14);
                    if (mByte[1] == (byte) 0xaa) {  //主车
                        if(FirstActivity.chief_status_flag == true)
                        {
                            //角度
                            angle_data =mByte[11] & 0xff;
                            angle_data =angle_data<<8;
                            angle_data += mByte[10] & 0xff;

                            // 显示数据
                            Data_show.setText("主车各状态信息: " + "超声波:" + UltraSonic
                                    + "mm 光照:" + Light + "lx" + " 码盘:" + CodedDisk
                                    + " 光敏状态:" + psStatus + " 状态:" + (mByte[2])+" 角度："+angle_data);
                        }
                    }
                    if(mByte[1] == (byte) 0x02) //从车
                    {
                        if(FirstActivity.chief_status_flag == false)
                        {


                            if(mByte[2] == -110)
                            {
                                byte [] newData = new byte[50];
                                Log.e("data",""+mByte[4]);
                                newData =  Arrays.copyOfRange(mByte, 5, mByte[4]+5);
                                Log.e("data",""+"长度"+newData.length);
                                try {
                                    String str= new String(newData,"ascii");//第二个参数指定编码方式
                                    Toast.makeText(getActivity(),""+str,Toast.LENGTH_LONG).show();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                // 显示数据
                                Data_show.setText("WIFI模块IP:" + FirstActivity.IPCar + "\n" + "从车各状态信息: " + "超声波:" + UltraSonic
                                        + "mm 光照:" + Light + "lx" + " 码盘:" + CodedDisk
                                        + " 光敏状态:" + psStatus + " 状态:" + (mByte[2]));
                            }
                        }
                    }
                }
            }
        }
    };

    private byte[] rbyte = new byte[40];
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }else {
            view = inflater.inflate(R.layout.right_fragment1, container, false);
      }
        FirstActivity.recvhandler =rehHandler;
        control_init();
        if(XcApplication.isserial == XcApplication.Mode.SOCKET) {
            connect_thread();                            //开启网络连接线程
        }
        else if(XcApplication.isserial == XcApplication.Mode.SERIAL){
            serial_thread();   //使用纯串口uart4
        }

        return view;
    }

    private void control_init()
    {
        Data_show =(TextView) view.findViewById(R.id.rvdata);
        speededit =(EditText)view.findViewById(R.id.speed_data);
        coded_discedit =(EditText)view.findViewById(R.id.coded_disc_data);
        angle_dataedit =(EditText)view.findViewById(R.id.angle_data);

        up_bt =(ImageButton) view.findViewById(R.id.up_button);
        blew_bt =(ImageButton) view.findViewById(R.id.below_button);
        stop_bt =(ImageButton) view.findViewById(R.id.stop_button);
        left_bt =(ImageButton) view.findViewById(R.id.left_button);
        right_bt =(ImageButton) view.findViewById(R.id.right_button);

        up_bt.setOnClickListener(new onClickListener2());
        blew_bt.setOnClickListener(new onClickListener2());
        stop_bt.setOnClickListener(new onClickListener2());
        left_bt.setOnClickListener(new onClickListener2());
        right_bt.setOnClickListener(new onClickListener2());
        up_bt.setOnLongClickListener(new onLongClickListener2());
    }

    private void connect_thread()
    {
        XcApplication.executorServicetor.execute(new Runnable() {
            @Override
            public void run() {
                FirstActivity.Connect_Transport.connect(rehHandler,FirstActivity.IPCar);
            }
        });
    }

    private  void serial_thread(){
        XcApplication.executorServicetor.execute(new Runnable() {
            @Override
            public void run() {
                FirstActivity.Connect_Transport.serial_connect(rehHandler);
            }
        });
    }

    // 速度和码盘方法
    private int getSpeed() {
        String src = speededit.getText().toString();
        int speed = 40;
        if (!src.equals("")) {
            speed = Integer.parseInt(src);
        } else {
            Toast.makeText(getActivity(), "请输入速度值", Toast.LENGTH_SHORT).show();
        }
        return speed;
    }

    private int getEncoder() {
        String src = coded_discedit.getText().toString();
        int encoder =20;
        if (!src.equals("")) {
            encoder = Integer.parseInt(src);
        } else {
            Toast.makeText(getActivity(), "请输入码盘值", Toast.LENGTH_SHORT).show();
        }
        return encoder;
    }

    private int getAngle() {
        String src = angle_dataedit.getText().toString();
        int angle = 450;
        if (!src.equals("")) {
            angle = Integer.parseInt(src);
        } else {
            Toast.makeText(getActivity(), "请输入角度值", Toast.LENGTH_SHORT).show();
        }
        return angle;
    }
    // 速度与码盘值
    private int sp_n, en_n,angle_n;

    private class onClickListener2 implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            sp_n = getSpeed();

            switch(v.getId())
            {
                case R.id.up_button:
                    en_n = getEncoder();
                    FirstActivity.Connect_Transport.go(sp_n, en_n);
                    break;
                case R.id.left_button:
                    angle_n  =getAngle();
                    FirstActivity.Connect_Transport.left(sp_n, angle_n);
                    break;
                case R.id.right_button:
                    angle_n  =getAngle();
                    FirstActivity.Connect_Transport.right(sp_n, angle_n);
                    break;
                case R.id.below_button:
                    en_n = getEncoder();
                    FirstActivity.Connect_Transport.back(sp_n, en_n);
                    break;
                case R.id.stop_button:
                    FirstActivity.Connect_Transport.stop();
                    break;
            }

        }
    }
    private class onLongClickListener2 implements View.OnLongClickListener
    {
        @Override
        public boolean onLongClick(View view) {
            if(view.getId() ==R.id.up_button)
            {
                sp_n = getSpeed();
                FirstActivity.Connect_Transport.line(sp_n);
            }
    /*如果将onLongClick返回false，那么执行完长按事件后，还有执行单击事件。
    如果返回true，只执行长按事件*/
            return true;
        }
    }


}


