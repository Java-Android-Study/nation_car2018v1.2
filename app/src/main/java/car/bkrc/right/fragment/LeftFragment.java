package car.bkrc.right.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bkrc.camera.XcApplication;
import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;

import car.bkrc.com.car2018.FirstActivity;
import car.bkrc.com.car2018.R;

public class LeftFragment extends Fragment implements View.OnClickListener{

    // 图片区域滑屏监听点击和弹起坐标位置
    private final int MINLEN = 30;
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;
    private  ImageView image_show =null;
    // 摄像头工具
    public static CameraCommandUtil cameraCommandUtil;
    private static TextView showip =null;
    private Button state,control,angle_control,clear_coded_disc;
    public static Handler btchange_handler;

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.left_fragment,container,false);
        image_show =(ImageView)view.findViewById(R.id.img);
        image_show.setOnTouchListener(new ontouchlistener1());
        showip =(TextView) view.findViewById(R.id.showip);
        state =(Button) view.findViewById(R.id.state);
        control =(Button) view.findViewById(R.id.control);
        angle_control =(Button) view.findViewById(R.id.angle_control);
        clear_coded_disc =(Button) view.findViewById(R.id.clear_coded_disc);

       // phThread.start();
        state.setOnClickListener(this);
        control.setOnClickListener(this);
        angle_control.setOnClickListener(this);
        clear_coded_disc.setOnClickListener(this);

        cameraCommandUtil = new CameraCommandUtil();

        XcApplication.executorServicetor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    getBitmap();
                }
            }
        });




        btchange_handler =bt_handler;
        if(XcApplication.isserial == XcApplication.Mode.SOCKET)
         {
            showip.setText("WIFIIP:" + FirstActivity.IPCar + "\n" + "CameraIP:" + FirstActivity.purecameraip);
        }
        return view;
    }

    public static Handler showidHandler =new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what ==22)
            {
                showip.setText(msg.obj + "\n" + "CameraIP:" + FirstActivity.purecameraip);
            }
        }
    };
    // 图片
    public static Bitmap bitmap;
    // 得到当前摄像头的图片信息
    public void getBitmap() {
        bitmap = cameraCommandUtil.httpForImage(FirstActivity.IPCamera);
        phHandler.sendEmptyMessage(10);
    }

    // 开启线程接受摄像头当前图片
    private Thread phThread = new Thread(new Runnable() {
        public void run() {
            while (true) {
                getBitmap();
            }
        }
    });

    // 显示图片
    private Handler phHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                image_show.setImageBitmap(bitmap);
            }
        }
    };

    private class ontouchlistener1 implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO 自动生成的方法存根
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 点击位置坐标
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                // 弹起坐标
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    y2 = event.getY();
                    float xx = x1 > x2 ? x1 - x2 : x2 - x1;
                    float yy = y1 > y2 ? y1 - y2 : y2 - y1;
                    // 判断滑屏趋势
                    if (xx > yy) {
                        if ((x1 > x2) && (xx > MINLEN)) {        // left
                            Toast.makeText(getActivity(),"左边",Toast.LENGTH_SHORT).show();
                            XcApplication.executorServicetor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    cameraCommandUtil.postHttp(FirstActivity.IPCamera, 4, 1);  //左
                                }
                            });

                        } else if ((x1 < x2) && (xx > MINLEN)) { // right
                            Toast.makeText(getActivity(),"右边",Toast.LENGTH_SHORT).show();
                            XcApplication.executorServicetor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    cameraCommandUtil.postHttp(FirstActivity.IPCamera, 6, 1);  //右
                                }
                            });
                        }
                    } else {
                        if ((y1 > y2) && (yy > MINLEN)) {        // up
                            Toast.makeText(getActivity(),"上边",Toast.LENGTH_SHORT).show();
                            XcApplication.executorServicetor.execute(new Runnable() {
                                @Override
                                public void run() {
                                        cameraCommandUtil.postHttp(FirstActivity.IPCamera, 0, 1);  //上
                                }
                            });
                        } else if ((y1 < y2) && (yy > MINLEN)) { // down
                            Toast.makeText(getActivity(),"下边",Toast.LENGTH_SHORT).show();
                            XcApplication.executorServicetor.execute(new Runnable() {
                                @Override
                                public void run() {
                                       cameraCommandUtil.postHttp(FirstActivity.IPCamera, 2, 1);  //下
                                }
                            });
                        }
                    }
                    x1 = 0;
                    x2 = 0;
                    y1 = 0;
                    y2 = 0;
                    break;
            }
            return true;
        }
    }

    @Override
    public void onClick(View view) {
        Button bt =(Button)view;
        String content=(String)bt.getText();

        switch (view.getId())
        {
            case R.id.state:
                if(content.equals("主车状态"))
                {
                    FirstActivity.chief_status_flag =true;
                    bt.setText(getResources().getText(R.string.follow_status));
                    FirstActivity.Connect_Transport.vice(2);
                    FirstActivity.but_handler.obtainMessage(11).sendToTarget();
                }
                else if(content.equals("从车状态"))
                {
                    FirstActivity.chief_status_flag =false;
                    bt.setText(getResources().getText(R.string.main_status));
                    FirstActivity.Connect_Transport.vice(1);
                    FirstActivity.but_handler.obtainMessage(22).sendToTarget();
                }
                break;
            case R.id.control:
                if(content.equals("主车控制"))
                {
                    FirstActivity.chief_control_flag =true;
                    bt.setText(getResources().getText(R.string.follow_control));
                    FirstActivity.Connect_Transport.TYPE = 0xAA;
                    FirstActivity.but_handler.obtainMessage(33).sendToTarget();
                }
                else if(content.equals("从车控制"))
                {
                    FirstActivity.chief_control_flag =false;
                    bt.setText(getResources().getText(R.string.main_control));
                    FirstActivity.Connect_Transport.TYPE =  0x02;
                    FirstActivity.but_handler.obtainMessage(44).sendToTarget();
                }
                break;
            case R.id.angle_control:
                if(content.equals("码盘控制"))
                {
                    FirstActivity.coded_control_flag =true;
                    bt.setText(getResources().getText(R.string.angle_control));
                    FirstActivity.but_handler.obtainMessage(55).sendToTarget();
                }
                else if(content.equals("角度控制"))
                {
                    FirstActivity.coded_control_flag =false;
                    bt.setText(getResources().getText(R.string.coded_control));
                    FirstActivity.but_handler.obtainMessage(66).sendToTarget();
                }
                break;
            case R.id.clear_coded_disc:
                FirstActivity.Connect_Transport.clear();
                break;
            default:
                break;
        }
    }

    private Handler bt_handler =new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 21:
                    state.setText(getResources().getText(R.string.follow_status));
                    break;
                case 22:
                    state.setText(getResources().getText(R.string.main_status));
                    break;
                case 23:
                    control.setText(getResources().getText(R.string.follow_control));
                    break;
                case 24:
                    control.setText(getResources().getText(R.string.main_control));
                    break;
                case 25:
                    angle_control.setText(getResources().getText(R.string.angle_control));
                    break;
                case 26:
                    angle_control.setText(getResources().getText(R.string.coded_control));
                    break;
                default:
                    break;
            }
        }
    };

}
