package com.example.myreadproject8.ui.adapter.bookcase;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;


import com.example.myreadproject8.R;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;

import java.util.List;



/**
 * @author fengyue
 * @date 2021/2/17 12:55
 */
public class BookTagAdapter extends TagAdapter<String> {
    private TextView tvTagName;
    private final Context context;
    private final int textSize;

    public BookTagAdapter(Context context, List<String> datas, int textSize) {
        super(datas);
        this.context = context;
        this.textSize = textSize;
    }

    @Override
    public View getView(FlowLayout parent, int position, String tagName) {
        try {
            tvTagName = (TextView) View.inflate(context, R.layout.item_book_tag, null);
            tvTagName.setTextSize(textSize);
            //默认为分类
            if (tagName.startsWith("1:")) { //字数
                tvTagName.setBackground(ContextCompat.getDrawable(context, R.drawable.tag_green_shape));
            } else if (tagName.startsWith("2:")) {//连载状态
                tvTagName.setBackground(ContextCompat.getDrawable(context, R.drawable.tag_red_shape));
            }
            tvTagName.setText(tagName.substring(2));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showError("" + e.getLocalizedMessage());
        }
        return tvTagName;
    }
}
