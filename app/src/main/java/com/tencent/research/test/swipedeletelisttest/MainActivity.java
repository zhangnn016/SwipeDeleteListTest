package com.tencent.research.test.swipedeletelisttest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {

    private SwipeDeleteListView listView;
    private ArrayAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (SwipeDeleteListView)findViewById(R.id.swipe_list);
        simpleAdapter = new SimpleAdapterWeakRef(this, 0, 0, Images.imageUrls);
        listView.setAdapter(simpleAdapter);
    }
}
