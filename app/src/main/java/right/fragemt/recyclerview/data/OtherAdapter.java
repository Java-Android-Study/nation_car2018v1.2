package right.fragemt.recyclerview.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bkrc.camera.XcApplication;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import car.bkrc.com.car2018.FirstActivity;
import car.bkrc.com.car2018.R;
import car.bkrc.right.fragment.LeftFragment;
import car2017_demo.RGBLuminanceSource;

public class OtherAdapter extends RecyclerView.Adapter<OtherAdapter.ViewHolder>{

    private List<Other_Landmark> mOtherLandmarkList;
    Context context =null;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View InfrareView;
        ImageView OtherImage;
        TextView OtherName;

        public ViewHolder(View view) {
            super(view);
            InfrareView = view;
            OtherImage = (ImageView) view.findViewById(R.id.landmark_image);
            OtherName = (TextView) view.findViewById(R.id.landmark_name);
        }
    }

    public OtherAdapter(List<Other_Landmark> InfrareLandmarkList, Context context) {
        mOtherLandmarkList = InfrareLandmarkList;
        this.context =context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.OtherName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Other_Landmark otherLandmark = mOtherLandmarkList.get(position);
                Other_select(otherLandmark);
                Toast.makeText(v.getContext(), "you clicked view " + otherLandmark.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.OtherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = holder.getAdapterPosition();
                Other_Landmark otherLandmark = mOtherLandmarkList.get(position);
                Other_select(otherLandmark);
                Toast.makeText(v.getContext(), "you clicked image " + otherLandmark.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Other_Landmark InfrareLandmark = mOtherLandmarkList.get(position);
        holder.OtherImage.setImageResource(InfrareLandmark.getImageId());
        holder.OtherName.setText(InfrareLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mOtherLandmarkList.size();
    }


    private void Other_select(Other_Landmark InfrareLandmark)
    {
        switch (InfrareLandmark.getName())
        {
            case "预设位":
                position_Dialog();
                break;
            case "二维码":
                qrHandler.sendEmptyMessage(10);
                break;
            case "蜂鸣器":
                buzzerController();
                break;
            case "指示灯":
                lightController();
                break;
            case "OpenMV摄像头":
                openmv_camera();
                break;
            default:
                break;
        }

    }


    private Timer timer;
    private String result_qr;
    // 二维码、车牌处理
    Handler qrHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Result result = null;
                            RGBLuminanceSource rSource = new RGBLuminanceSource(
                                   LeftFragment.bitmap);
                            try {
                                BinaryBitmap binaryBitmap = new BinaryBitmap(
                                        new HybridBinarizer(rSource));
                                Map<DecodeHintType, String> hint = new HashMap<DecodeHintType, String>();
                                hint.put(DecodeHintType.CHARACTER_SET, "utf-8");
                                QRCodeReader reader = new QRCodeReader();
                                result = reader.decode(binaryBitmap, hint);
                                if (result.toString() != null) {
                                    result_qr = result.toString();
                                    timer.cancel();
                                    qrHandler.sendEmptyMessage(20);
                                }
                                System.out.println("正在识别");
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            } catch (ChecksumException e) {
                                e.printStackTrace();
                            } catch (FormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, 200);
                    break;
                case 20:
                    Toast.makeText(context, result_qr, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        };
    };



    private int state_camera =0;
    private void  position_Dialog()  //预设位对话框
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("预设位设置");
        String[] set_item = { "set1", "set2" ,"set3","call1","call2","call3"};
        builder.setSingleChoiceItems(set_item, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 自动生成的方法存根
                state_camera = which+5;
                camerastate_control();
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean flag_camera;
    int k =3;
    // 开启线程接受摄像头当前图片
    private void camerastate_control() {
        XcApplication.executorServicetor.execute(new Runnable() {
            public void run() {
                while (true) {
                    switch (state_camera) {
                        //上下左右转动
                        case 1:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 0, 1);  //向上
                            break;
                        case 2:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 2, 1);  //向下
                            break;
                        case 3:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 4, 1);  //向左
                            break;
                        case 4:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 6, 1);  //向右
                            break;
                        // / 5-7   设置预设位1到3
                        case 5:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 30, 0);
                            break;
                        case 6:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 32, 0);
                            break;
                        case 7:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 34, 0);
                            break;
                        //调用预设位1-3
                        case 8:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 31, 0);
                            break;
                        case 9:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 33, 0);
                            break;
                        case 10:
                            LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 35, 0);
                            break;
                        default:
                            break;
                    }
                    state_camera = 0;
                }

            }
        });
    }

    // OpenMV 摄像头控制
    private  void openmv_camera()
    {
        AlertDialog.Builder openmv_builder = new AlertDialog.Builder(context);
        openmv_builder.setTitle("OpenMV摄像头");
        String[] openmv_str = { "保留", "开启识别二维码","关闭识别二维码" };
        openmv_builder.setSingleChoiceItems(openmv_str, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                                break;
                            case 1:  //开启识别二维码
                                FirstActivity.Connect_Transport.opencv_control(0x92 , 0x01);
                                break;
                            case 2:  //关闭识别
                                FirstActivity.Connect_Transport.opencv_control(0x92 , 0x02);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        openmv_builder.create().show();
    }

    // 蜂鸣器
    private void buzzerController() {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle("蜂鸣器");
        String[] im = { "开", "关" };
        build.setSingleChoiceItems(im, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (which == 0) {
                            // 打开蜂鸣器
                            FirstActivity.Connect_Transport.buzzer(1);
                        } else if (which == 1) {
                            // 关闭蜂鸣器
                            FirstActivity.Connect_Transport.buzzer(0);
                        }
                        dialog.dismiss();
                    }
                });
        build.create().show();
    }
    // 指示灯遥控器
    private void lightController() {
        AlertDialog.Builder lt_builder = new AlertDialog.Builder(context);
        lt_builder.setTitle("指示灯");
        String[] item = { "左亮", "全亮", "右亮", "全灭" };
        lt_builder.setSingleChoiceItems(item, -1,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (which == 0) {
                            FirstActivity.Connect_Transport.light(1, 0);
                        } else if (which == 1) {
                            FirstActivity.Connect_Transport.light(1, 1);
                        } else if (which == 2) {
                            FirstActivity.Connect_Transport.light(0, 1);
                        } else if (which == 3) {
                            FirstActivity.Connect_Transport.light(0, 0);
                        }
                        dialog.dismiss();
                    }
                });
        lt_builder.create().show();
    }



}