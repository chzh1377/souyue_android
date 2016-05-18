package me.maxwin;

import java.util.ArrayList;

import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

public class XListViewActivity extends Activity implements IXListViewListener {
    
    /**
     * 打印LOG的标签
     */
    private static final String TAG = XListViewActivity.class.getSimpleName();
    
    private XListView mListView;

    private ArrayAdapter<String> mAdapter;

    private ArrayList<String> items = new ArrayList<String>();

    private Handler mHandler;

    private int start = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHandler = new Handler();
        
        geneItems();

        mListView = (XListView) findViewById(R.id.xListView);
        mListView.setPullLoadEnable(true);
        mAdapter = new ArrayAdapter<String>(this, R.layout.list_item, items);
        mListView.setAdapter(mAdapter);

        //		mListView.setPullLoadEnable(false);
        //		mListView.setPullRefreshEnable(false);

        mListView.setXListViewListener(this);
        
    }

    private void geneItems() {
        
        for (int i = 0; i < 20; ++i) {
            items.add("refresh cnt " + (++start));
        }
        
        Log.i(TAG, "-------------->start = " + start);
        
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                geneItems();
                mAdapter.notifyDataSetInvalidated();
                
                onLoad();
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                geneItems();

                mAdapter.notifyDataSetChanged();
                onLoad();
            }
        }, 2000);
    }
    
    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("刚刚");
    }
    
}