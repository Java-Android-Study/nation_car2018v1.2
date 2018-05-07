package car.bkrc.right.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import car.bkrc.com.car2018.R;
import android.support.v7.widget.StaggeredGridLayoutManager;

import right.fragemt.recyclerview.data.OtherAdapter;
import right.fragemt.recyclerview.data.Other_Landmark;

public class RightOtherFragment extends Fragment {

    private List<Other_Landmark> otherList = new ArrayList<Other_Landmark>();
    Context minstance =null;

  //  private RightOtherFragment(){}

    public static RightOtherFragment getInstance()
    {
        return RightZigbeeHolder.mInstance;
    }

    private static class RightZigbeeHolder
    {
        private static final RightOtherFragment mInstance =new RightOtherFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        minstance =getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_other_fragment, container, false);
        initFruits();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        OtherAdapter adapter = new OtherAdapter(otherList,getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initFruits() {
        otherList.clear();
        Other_Landmark apple = new Other_Landmark("预设位", R.mipmap.default_position);
        otherList.add(apple);
        Other_Landmark banana = new Other_Landmark("二维码", R.mipmap.qr_code);
        otherList.add(banana);
        Other_Landmark orange = new Other_Landmark("蜂鸣器", R.mipmap.buzzer);
        otherList.add(orange);
        Other_Landmark watermelon = new Other_Landmark("指示灯", R.mipmap.light);
        otherList.add(watermelon);
        Other_Landmark openmvcamera = new Other_Landmark("OpenMV摄像头", R.mipmap.openmv);
        otherList.add(openmvcamera);
    }
}

