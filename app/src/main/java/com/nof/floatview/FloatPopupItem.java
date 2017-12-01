package com.nof.floatview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Created by Administrator on 2017/11/27.
 */

public class FloatPopupItem extends PopupWindow implements View.OnClickListener {

    public int width;
    private int height = Util.dp2px(50);
    private OnItemClickListener onItemClickListener;

    /**
     * 尼玛，这里这么简单就别写备注了，免得被人以为瞧不起拿刀砍
     * @param context
     */
    public FloatPopupItem(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < 3; i++) {
            ImageView iv = new ImageView(context);
            iv.setMinimumWidth(height);
            iv.setMinimumHeight(height);
            iv.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher_round));
            iv.setTag(i);
            layout.addView(iv);
            width+=height;
            iv.setOnClickListener(this);
        }
        setContentView(layout);
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(0x00000000));
        setOutsideTouchable(false);
    }

    interface OnItemClickListener{
        void onItemClick(int i);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        if(onItemClickListener!=null){
            onItemClickListener.onItemClick(tag);
        }
    }
}
