package demo.song.com.refresh;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements RefreshLayout.OnRefreshListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private List<String> mList;
    private RefreshLayout refreshlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        mList = new ArrayList<>();
        mList.add("北京");
        mList.add("上海");
        mList.add("天津");
        mList.add("重庆");
        mList.add("杭州");
        mList.add("深圳");

        mList.add("武汉");
        mList.add("长沙");
        mList.add("台湾");
        mList.add("香港");

        mList.add("济南");
        mList.add("青岛");
        mList.add("大连");
        mList.add("沈阳");
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        refreshlayout = findViewById(R.id.refreshlayout);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new CtiyAdapter());
        refreshlayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh: ");
    }

    @Override
    public void onRefreshComplete() {
        Log.i(TAG, "onRefreshComplete: ");
    }

    private class  CityHolder extends RecyclerView.ViewHolder{
        private TextView cityname;
        private ImageView imageView;

        public CityHolder(View itemView) {
            super(itemView);
            cityname = itemView.findViewById(R.id.cityname);
            imageView = itemView.findViewById(R.id.city_icon);
        }
    }
    private class CtiyAdapter extends RecyclerView.Adapter<CityHolder>{

        @Override
        public CityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=LinearLayout.inflate(MainActivity.this,R.layout.layout_item,null);
            CityHolder holder=new CityHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(CityHolder holder, int position) {
            holder.cityname.setText(mList.get(position));
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
