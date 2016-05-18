package com.xuxh.slideselectview;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends Activity {
    private int defalutValue = 75;
    private SeekBar light_seekBar;
    private SlideSelectView slideSelectView;
    private String[] textStrings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        light_seekBar = (SeekBar)findViewById(R.id.light_seekBar);
        slideSelectView = (SlideSelectView)findViewById(R.id.slideSelectView);

        light_seekBar.setOnSeekBarChangeListener(seekBarChange);

        textStrings = new String[]{"小", "中", "大", "特大","超大"};
        slideSelectView.setString(textStrings);
        slideSelectView.setOnSelectListener(onSelect);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setScreenLight(progress);
        }
    };

    //设置屏幕亮度
    public void setScreenLight(int progress) {
        if (progress < 1) {
            progress = 1;
        } else if (progress > 255) {
            progress = 255;
        }
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.screenBrightness = progress / 255f;
        getWindow().setAttributes(attrs);
        defalutValue = progress;
    }


    private SlideSelectView.onSelectListener onSelect = new SlideSelectView.onSelectListener() {
        @Override
        public void onSelect(int index) {
            Toast.makeText(MainActivity.this,"当前滑动到位置:"+textStrings[index],Toast.LENGTH_SHORT).show();
        }
    };
}
