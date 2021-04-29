package com.example.myreadproject8.widget.edit_text;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.example.myreadproject8.R;


public class EditText_Clear extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {

    //删除按钮引用
    private Drawable mClearDrawable;

    //控件是否有焦点
    private boolean hasFoucs;

    public EditText_Clear(Context context) {
        super(context);
        init();
    }

    public EditText_Clear(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditText_Clear(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){

        mClearDrawable = getResources().getDrawable(R.drawable.ic_clear_default);

        mClearDrawable.setBounds(0,0,mClearDrawable.getIntrinsicWidth(),mClearDrawable.getIntrinsicHeight());

        //设置默认隐藏图标
        setClearIconVisible(false);
        //设置焦点改变的监听
        setOnFocusChangeListener(this);
        //设置输入框里面内容发生改变的监听
        addTextChangedListener(this);
    }

    //设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
    protected void setClearIconVisible(boolean visible){
        boolean wasVisible = (getCompoundDrawables()[2] != null);
        if(visible != wasVisible) {
            Drawable x = visible ? mClearDrawable : null;
            setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
        }
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    //当输入框内容发生变化回调方法
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(hasFoucs){
            setClearIconVisible(s.length() > 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    //edittext失去焦点则设置不可见
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFoucs = hasFocus;
        if(hasFocus){
            setClearIconVisible(getText().length() > 0);
        }else{
            setClearIconVisible(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(getCompoundDrawables()[2]!=null){
                //当按下的位置在EditText的宽度-图标到控件右边的间距-图标的宽度和EditText的宽度-图标到控件右边的间距之间则看作点击了图标，由于只有一行所以不考虑竖直方向
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight()) && event.getX() < (getWidth() - getPaddingRight());
                if(touchable){
                    this.setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
