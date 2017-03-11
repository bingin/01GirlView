package com.bingin.girlview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";
    @BindView(R.id.lv_girl)
    ListView mLvGirl;
    private List<GankApiBean.ResultsBean> mList;
    private GirlViewAdapter mAdapter;
    private Gson mGson;
    private OkHttpClient mClient;
    private Request mRequest;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();
    }

    private void initView() {
//        mLvGirl = (ListView) findViewById(R.id.lv_girl); //方法一
        ButterKnife.bind(this);
    }

    private void initData() {
        mGson = new Gson();
//        List<GankApiBean> mList =  new ArrayList<GankApiBean>();
        mList = new ArrayList<GankApiBean.ResultsBean>();

        mClient = new OkHttpClient();
        mRequest = new Request.Builder().url("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/3/1").build();

        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String url = response.body().string();
                GankApiBean bean = mGson.fromJson(url, GankApiBean.class);
                mList.addAll(bean.getResults());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLvGirl.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }


    private void initListener() {
        mLvGirl.setOnScrollListener(MainActivity.this);
        mAdapter = new GirlViewAdapter();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                if (view.getLastVisiblePosition() == mList.size() - 1) {
                    if (!isLoading) {
                        loadMoreData();
                    }
                }
                break;
            case SCROLL_STATE_FLING:
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
                break;

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private void loadMoreData() {
        isLoading = true;
        mRequest = new Request.Builder().url("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/3/"+ (mList.size() / 3) + 1).build();

        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String url = response.body().string();
                GankApiBean bean = mGson.fromJson(url, GankApiBean.class);
                mList.addAll(bean.getResults());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isLoading = false;
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private class GirlViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_girlview, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTime.setText(mList.get(position).getPublishedAt());
//            Glide.with(MainActivity.this).load(mList.get(position).getUrl()).into(holder.ivGirl);
            Glide.with(MainActivity.this)
                    .load(mList.get(position).getUrl())
                    .centerCrop()
                    .bitmapTransform(new BlurTransformation(MainActivity.this))
                    .into(holder.ivGirl);

            return convertView;
        }

        class ViewHolder {
            TextView tvTime;
            ImageView ivGirl;

            public ViewHolder(View root) {
                tvTime = (TextView) root.findViewById(R.id.tv_time);
                ivGirl = (ImageView) root.findViewById(R.id.iv_girl);
            }
        }
    }
}
