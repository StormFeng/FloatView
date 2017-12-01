package com.nof.floatview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.PopupWindow;


/**
 * Created by Administrator on 2017/11/27.
 */

public class FloatPopup extends PopupWindow implements FloatPopupItem.OnItemClickListener {

    //设置悬浮按钮尺寸
    private int size = Util.dp2px(50);
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    //触发移动事件的最小距离
    private int touchSlop = new ViewConfiguration().getScaledTouchSlop();
    //记录当前手指实时位置
    private float curX,curY;
    //记录手指按下时的位置
    private float lastX,lastY;
    //设置当前悬浮按钮的显示位置
    private float showX,showY;
    //记录当前悬浮按钮显示状态
    private boolean showMenu = false;
    //记录当前悬浮按钮显示位置
    private boolean showLeft = true;
    private FloatPopupItem item;
    private Activity context;
    private OnClickListener onClickListener;
    private Handler handler;
    private Message message;

    private static FloatPopup floatPopup;

    public static FloatPopup getInstance(){
        if(floatPopup==null){
            floatPopup = new FloatPopup(Util.getContext());
        }
        return floatPopup;
    }

    public void show(){
        if(!floatPopup.isShowing()){
            floatPopup.showAtLocation(Util.getContext().getWindow().getDecorView(),
                    Gravity.NO_GRAVITY,0,0);
            floatPopup.setOnClickListener((OnClickListener) Util.getContext());
        }
    }

    @SuppressLint("HandlerLeak")
    public FloatPopup(Context context) {
        this.context = (Activity) context;
        item = new FloatPopupItem(context);
        item.setOnItemClickListener(this);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //接受到消息,说明用户在规定时间没有操作悬浮按钮，这个时候还要判断下子选项是否展开，
                //选项没有展开，那么就让悬浮按钮变小，靠边站
                if(!showMenu){
                    toSmallIcon(msg.arg1,msg.arg2);
                }
            }
        };
        message = handler.obtainMessage();
        message.what = 0;

        ImageView iv = new ImageView(context);
        iv.setMinimumWidth(size);
        iv.setMinimumHeight(size);
        iv.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher_round));
        setContentView(iv);
        setWidth(size);
        setHeight(size);
        setFocusable(false);
        setBackgroundDrawable(new ColorDrawable(0x00000000));
        setOutsideTouchable(false);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                curX = event.getRawX();
                curY = event.getRawY();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        float ddx = lastX - curX;
//                        float ddy = lastY - curY;
//                        if(Math.abs(ddx)<touchSlop && Math.abs(ddy)<touchSlop){
//                            return true;
//                        }
                        /*如果当前手指在Y轴上的位置小于按钮的一半时，这个时候按钮的上边沿已经最靠边了。
                        想想一下当Y刚好等于临界值size/2，按钮会在什么位置，就会理解这里为什么做判断了。*/
                        if(curY<size/2){
                           /*在MotionEvent.ACTION_MOVE里面去update按钮的位置，是因为手指每次的移动都会消费
                            move事件，你移动很长的一段路程，在move事件里就会分解成一小段一小段的位移。*/
                            update((int)event.getRawX() - size/2,0);
                        /*在M这里和上边是一样的道理，当Y=临界值screenHeight-size/2时，说明按钮已经接近下边缘了*/
                        }else if(curY>screenHeight-size/2){
                            update((int)event.getRawX() - size/2,screenHeight-size);
                        }else{
                            /*常规情况。但是这里为什么要减去size/2呢(还有上边)？
                            我们设置的位置对于按钮来说是它的左上角，这里减去size/2只是为了让我们的参考点移
                            动到按钮的中心位置，另外，滑动的时候会消除掉一顿一顿的情况，不信你试试没有减掉
                            size/2时是什么样子*/
                            update((int)event.getRawX() - size/2,(int)event.getRawY()-size/2);
                        }

                        /*当开始移动的时候要判断下子项是否展开，如果展开，关闭之后再移动*/
                        if(item.isShowing()){
                            item.dismiss();
                            showMenu = !showMenu;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        /*这里就很好理解了，当我们手指抬起时，如果抬起的位置靠近左边(curX<screenWidth/2),
                            抬起之后就让按钮滚到左边去，否则，到右边乖乖站好*/
                        if(curX<screenWidth/2){
                            showLeft = true;
                            showX = 0;
                            showY = event.getRawY()-size/2;
                        }else{
                            showLeft = false;
                            showX = screenWidth-size;
                            showY = event.getRawY()-size/2;
                        }
                        update((int) showX,(int) showY);

                        /*这里是处理点击事件的，当手指按钮和抬起之间的距离小于touchSlop时，
                        我们认为这是一次点击事件，并根据showMenu的值处理显示或者隐藏子项*/
                        float dx = lastX - curX;
                        float dy = lastY - curY;
                        if(Math.abs(dx)<touchSlop && Math.abs(dy)<touchSlop){
                            if(!showMenu){
                                showMenu();
                            }else{
                                hideMenu();
                            }
                            showMenu = !showMenu;
                        }

                        handler.removeMessages(0);

                        message = handler.obtainMessage();
                        message.what = 0;
                        message.arg1 = (int) showX;
                        message.arg2 = (int) showY;
                        /*手指抬起5s内没有操作的话，让图标变小*/
                        handler.sendMessageDelayed(message,5000);
                        break;
                }
                return true;
            }
        });
    }

    private void toSmallIcon(int curx,int cury){
        if(showLeft){
            update(curx,cury,size/2,size/2);
        }else{
            update(curx+size/2,cury,size/2,size/2);
        }
    }

    private void hideMenu() {
        if(item!=null){
            item.dismiss();
        }
    }

    private void showMenu(){
        /*这里为什么加，为什么减，自己拿尺子比着屏幕量吧，打字好累...*/
        if(showLeft){
            item.showAtLocation(context.getWindow().getDecorView(),Gravity.NO_GRAVITY,(int)(showX+size),(int)showY);
        }else{
            item.showAtLocation(context.getWindow().getDecorView(),Gravity.NO_GRAVITY,(int)(showX-item.width),(int)showY);
        }
    }

    @Override
    public void update(int x, int y) {
        this.update(x,y,size, size);
    }
 
    @Override
    public void update(int x, int y, int width, int height) {
        super.update(x, y, width, height);
    }

    /**
     * 这里用了两个接口把子项item的点击事件传到FloatPopup的onClick(int i)方法里面统一处理，
     * 因为我们只对外暴露FloatPopup
     * @param i
     */
    @Override
    public void onItemClick(int i) {
        if(onClickListener!=null){
            onClickListener.onClick(i);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener{
        void onClick(int i);
    }

    public void release(){
        handler.removeMessages(0);
        message = null;
        handler = null;
        dismiss();
        floatPopup = null;
    }
}
