package com.bigggfish.horizontalunitview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private HorizontalUnitView mHorizontalUnitView;
    private int itemCount = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHorizontalUnitView = (HorizontalUnitView) findViewById(R.id.horizontal_unit_view);
        mHorizontalUnitView.setOnItemClickListener(new HorizontalUnitView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mHorizontalUnitView.setCheckedPos(position);
            }
        });
        //loadData();

        findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHorizontalUnitView.setAdapter(new HorizontalUnitView.Adapter<String>(getStringList()) {
                    @Override
                    public String getText(String item) {
                        return item;
                    }
                });
                mHorizontalUnitView.setVisibleNum(itemCount - 2);
            }
        });
    }

    //模拟加载数据
    private void loadData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHorizontalUnitView.setAdapter(new HorizontalUnitView.Adapter<String>(getStringList()) {
                    @Override
                    public String getText(String item) {
                        return item;
                    }
                });
                mHorizontalUnitView.setVisibleNum(itemCount - 2);
            }
        }, 2000);
    }

    private List<String> getStringList() {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            stringList.add("Item " + i);
        }
        return stringList;
    }
}
