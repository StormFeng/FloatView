package com.nof.floatview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements FloatPopup.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button layout = new Button(this);
        layout.setBackgroundColor(0xFFFF8080);
        layout.setText("点击屏幕开始游戏");
        layout.setTextColor(Color.WHITE);
        setContentView(layout);
        Util.setContext(this);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatPopup.getInstance().show();
            }
        });
    }

    @Override
    public void onClick(int i) {
        Toast.makeText(this,"The "+i+" item is clicked",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FloatPopup.getInstance().release();
    }
}
