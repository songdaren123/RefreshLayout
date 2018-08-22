package demo.song.com.refresh;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private RecyclerView mRecyclerView;
    private List<String> mList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        mList=new ArrayList<>();
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
    }

    private void initView() {
        mRecyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new CtiyAdapter());
    }
    private class  CityHolder extends RecyclerView.ViewHolder{
        private TextView cityname;
        private ImageView imageView;
        public CityHolder(View itemView) {
            super(itemView);
            cityname=itemView.findViewById(R.id.cityname);
            imageView=itemView.findViewById(R.id.city_icon);
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
