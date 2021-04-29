package com.example.myreadproject8.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.R;
import com.example.myreadproject8.greendao.service.BookGroupService;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.string.StringHelper;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.google.android.material.textfield.TextInputLayout;



/**
 * @author fengyue
 * @date 2020/9/20 9:48
 */
public class MyAlertDialog {

    public static AlertDialog.Builder build(Context context) {
        return new AlertDialog.Builder(context);
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             Integer inputType, boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener) {
        return createInputDia(context, title, hint, initText, inputType, cancelable, maxLen, oic, posListener, null, null, null);
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             Integer inputType, boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener,
                                             DialogInterface.OnClickListener negListener, String neutralBtn,
                                             DialogInterface.OnClickListener neutralListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null);
        TextInputLayout textInputLayout = view.findViewById(R.id.text_input_lay);

        textInputLayout.setCounterMaxLength(maxLen);
        EditText editText = textInputLayout.getEditText();
        editText.setHint(hint);
        if (inputType != null) editText.setInputType(inputType);
        if (!StringHelper.isEmpty(initText)) editText.setText(initText);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        App.getHandler().postDelayed(() -> imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED), 220);
        AlertDialog inputDia;
        if (neutralBtn == null) {
            inputDia = build(context)
                    .setTitle(title)
                    .setView(view)
                    .setCancelable(cancelable)
                    .setPositiveButton("确认", (dialog, which) -> {
                        posListener.onClick(dialog, which);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        if (negListener != null) {
                            negListener.onClick(dialog, which);
                        }
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .show();
        } else {
            inputDia = build(context)
                    .setTitle(title)
                    .setView(view)
                    .setCancelable(cancelable)
                    .setNeutralButton(neutralBtn, (dialog, which) -> {
                        neutralListener.onClick(dialog, which);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .setPositiveButton("确认", (dialog, which) -> {
                        posListener.onClick(dialog, which);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        if (negListener != null) {
                            negListener.onClick(dialog, which);
                        }
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    })
                    .show();
        }
        Button posBtn = inputDia.getButton(AlertDialog.BUTTON_POSITIVE);
        posBtn.setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editText.getText().toString();
                if (editText.getText().length() > 0 && editText.getText().length() <= maxLen && !text.equals(initText)) {
                    posBtn.setEnabled(true);
                } else {
                    posBtn.setEnabled(false);
                }
                oic.onChange(text);
            }
        });
        return inputDia;
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener) {
        return createInputDia(context, title, hint, initText, InputType.TYPE_CLASS_TEXT, cancelable, maxLen, oic, posListener);
    }

    public static void showTipDialogWithLink(Context context, int msgId){
        showTipDialogWithLink(context,"提示", msgId);
    }
    public static void showTipDialogWithLink(Context context, String title, int msgId){
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.dialog_textview, null);
        view.setText(msgId);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        build(context).setTitle(title).setView(view).setPositiveButton("知道了", null).show();
    }

    public interface OnVerify {
        void success(boolean needGoTo);
    }

    public interface OnCancel {
        void cancel();
    }

    public interface onInputChangeListener {
        void onChange(String text);
    }
}
