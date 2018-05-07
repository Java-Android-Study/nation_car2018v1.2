package car.bkrc.right.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import car.bkrc.com.car2018.R;
import right.fragemt.recyclerview.data.InfrareAdapter;
import right.fragemt.recyclerview.data.Infrared_Landmark;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class RightInfraredFragment extends Fragment {

    public static final String TAG = "RightInfraredFragment";
    private Infrared_Landmark[]  infrareds = {new Infrared_Landmark("报警器", R.mipmap.alarm), new Infrared_Landmark("档位器", R.mipmap.gear_position),
             new Infrared_Landmark("立体显示", R.mipmap.stereo_display),new Infrared_Landmark("数码相框", R.mipmap.digital_photo)}; //new Infrared_Landmark("风扇", R.mipmap.air_fan),

    private List<Infrared_Landmark> InfraredList = new ArrayList<>();
    private InfrareAdapter adapter;
    private static RightInfraredFragment mInstance =null;

 //   private RightInfraredFragment(){}
    public  static RightInfraredFragment getInstance() {
        if(mInstance ==null) {
            synchronized (RightInfraredFragment.class) {
                if(mInstance ==null) {
                    mInstance =new RightInfraredFragment();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_infrared, container, false);
        initInfrared();
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        ImageView fruitImageView = (ImageView) view.findViewById(R.id.fruit_image_view);
        collapsingToolbar.setTitle("红外控制");
        Glide.with(this).load(R.mipmap.bkrc_logo).into(fruitImageView);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        adapter = new InfrareAdapter(InfraredList,getActivity());
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void initInfrared() {
        InfraredList.clear();
        for (int i = 0; i < infrareds.length; i++) {
            InfraredList.add(infrareds[i]);
        }
    }

}
