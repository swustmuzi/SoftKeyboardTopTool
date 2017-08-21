package com.muzi.keyboard.tool;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    private static final String KEYBOARD_TOP_VIEW_FIRST_TIP_NULL = "www.";
    private static final String KEYBOARD_TOP_VIEW_SECOND_TIP_NULL = "m.";
    private static final String KEYBOARD_TOP_VIEW_THIRD_TIP_NULL = "wap.";
    private static final String KEYBOARD_TOP_VIEW_FOURTH_TIP_NULL = ".cn";
    private static final String KEYBOARD_TOP_VIEW_FIRST_TIP = ".";
    private static final String KEYBOARD_TOP_VIEW_SECOND_TIP = "/";
    private static final String KEYBOARD_TOP_VIEW_THIRD_TIP = ".com";
    private static final String KEYBOARD_TOP_VIEW_FOURTH_TIP = ".cn";

    private View mContainer;
    private EditText mEditText;
    private TextView mKeyboardTopViewFirstTxt;
    private TextView mKeyboardTopViewSecondTxt;
    private TextView mKeyboardTopViewThirdTxt;
    private TextView mKeyboardTopViewFourthTxt;
    private View mKeyboardTopViewTipContainer;
    private boolean mInputViewIsNull = true;

    private SeekBar mKeyboardTopViewSeekBar;
    private ValueAnimator mExtendSeekBarAnimator;
    private ValueAnimator mShrinkSeekBarAnimator;
    private boolean mIsCanMoveCursor = false;
    private int mLastSeekBarProgress = 25;

    private PopupWindow mSoftKeyboardTopPopupWindow;
    private boolean mIsSoftKeyBoardShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContainer = findViewById(R.id.container);
        mEditText = (EditText) findViewById(R.id.edit_view);
        mEditText.addTextChangedListener(this);

        //监听视图树的布局改变(弹出/隐藏软键盘会触发)
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardOnGlobalChangeListener());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        updateKeyboardTopViewTips(TextUtils.isEmpty(text));
    }

    @Override
    public void onClick(View v) {
        String txt = "";
        switch (v.getId()) {
            case R.id.keyboard_top_view_first_txt:
                txt = mKeyboardTopViewFirstTxt.getText().toString();
                break;
            case R.id.keyboard_top_view_second_txt:
                txt = mKeyboardTopViewSecondTxt.getText().toString();
                break;
            case R.id.keyboard_top_view_third_txt:
                txt = mKeyboardTopViewThirdTxt.getText().toString();
                break;
            case R.id.keyboard_top_view_fourth_txt:
                txt = mKeyboardTopViewFourthTxt.getText().toString();
                break;
        }
        insertTextToEditText(txt);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mIsCanMoveCursor) {
            moveEditViewCursor(mLastSeekBarProgress > progress);
        }
        mLastSeekBarProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsCanMoveCursor = false;
        extendSeekBarAnimator();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsCanMoveCursor = false;
        shrinkSeekBarAnimator();
    }

    private void extendSeekBarAnimator() {
        if (mShrinkSeekBarAnimator != null && mShrinkSeekBarAnimator.isRunning()) {
            mShrinkSeekBarAnimator.cancel();
            mShrinkSeekBarAnimator = null;
        }

        int screenWidth = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        final int seekBarWidth = mKeyboardTopViewSeekBar.getWidth();
        mExtendSeekBarAnimator = ValueAnimator.ofInt(screenWidth - seekBarWidth);
        mExtendSeekBarAnimator.setDuration(300);
        mExtendSeekBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                if (mKeyboardTopViewSeekBar == null) return;
                Integer value = (Integer) animation.getAnimatedValue();
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mKeyboardTopViewSeekBar.getLayoutParams();
                layoutParams.width = value + seekBarWidth;
                mKeyboardTopViewSeekBar.setLayoutParams(layoutParams);
            }
        });
        mExtendSeekBarAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mKeyboardTopViewTipContainer != null) {
                    mKeyboardTopViewTipContainer.setVisibility(View.INVISIBLE);
                    mIsCanMoveCursor = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mExtendSeekBarAnimator.start();
    }

    private void shrinkSeekBarAnimator() {
        if (mExtendSeekBarAnimator != null && mExtendSeekBarAnimator.isRunning()) {
            mExtendSeekBarAnimator.cancel();
            mExtendSeekBarAnimator = null;
        }

        final int seekBarWidth = mKeyboardTopViewSeekBar.getWidth();
        final int minWidth = getResources().getDimensionPixelOffset(R.dimen.keyboard_top_view_seek_bar_min_width);
        mShrinkSeekBarAnimator = ValueAnimator.ofInt(seekBarWidth - minWidth);
        mShrinkSeekBarAnimator.setDuration(300);
        mShrinkSeekBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                if (mKeyboardTopViewSeekBar == null) return;
                Integer value = (Integer) animation.getAnimatedValue();
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mKeyboardTopViewSeekBar.getLayoutParams();
                layoutParams.width = seekBarWidth - value;
                mKeyboardTopViewSeekBar.setLayoutParams(layoutParams);
                int normalProgress = mKeyboardTopViewSeekBar.getMax() / 2;
                int progress = (mKeyboardTopViewSeekBar.getProgress() - normalProgress) * (value / (seekBarWidth - minWidth)) + normalProgress;
                mKeyboardTopViewSeekBar.setProgress(progress);
            }
        });
        mShrinkSeekBarAnimator.start();

        if (mKeyboardTopViewTipContainer != null) {
            mKeyboardTopViewTipContainer.setVisibility(View.VISIBLE);
        }
    }

    private void insertTextToEditText(String txt) {
        if (TextUtils.isEmpty(txt)) return;
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();
        Editable edit = mEditText.getEditableText();//获取EditText的文字
        if (start < 0 || start >= edit.length()) {
            edit.append(txt);
        } else {
            edit.replace(start, end, txt);//光标所在位置插入文字
        }
    }

    private void moveEditViewCursor(boolean isMoveLeft) {
        int index = mEditText.getSelectionStart();
        if (isMoveLeft) {
            if (index <= 0) return;
            mEditText.setSelection(index - 1);
        } else {
            Editable edit = mEditText.getEditableText();//获取EditText的文字
            if (index >= edit.length()) return;
            mEditText.setSelection(index + 1);
        }
    }

    private void showKeyboardTopPopupWindow(int x, int y) {
        if (mSoftKeyboardTopPopupWindow != null && mSoftKeyboardTopPopupWindow.isShowing()) {
            updateKeyboardTopPopupWindow(x, y); //可能是输入法切换了输入模式，高度会变化（比如切换为语音输入）
            return;
        }

        View popupView = getLayoutInflater().inflate(R.layout.soft_keyboard_top_tool_view, null);

        mKeyboardTopViewFirstTxt = (TextView) popupView.findViewById(R.id.keyboard_top_view_first_txt);
        mKeyboardTopViewSecondTxt = (TextView) popupView.findViewById(R.id.keyboard_top_view_second_txt);
        mKeyboardTopViewThirdTxt = (TextView) popupView.findViewById(R.id.keyboard_top_view_third_txt);
        mKeyboardTopViewFourthTxt = (TextView) popupView.findViewById(R.id.keyboard_top_view_fourth_txt);
        mKeyboardTopViewSeekBar = (SeekBar) popupView.findViewById(R.id.keyboard_top_view_seek_bar);
        mKeyboardTopViewTipContainer = popupView.findViewById(R.id.keyboard_top_view_tip_container);

        mKeyboardTopViewFirstTxt.setOnClickListener(this);
        mKeyboardTopViewSecondTxt.setOnClickListener(this);
        mKeyboardTopViewThirdTxt.setOnClickListener(this);
        mKeyboardTopViewFourthTxt.setOnClickListener(this);
        mKeyboardTopViewSeekBar.setOnSeekBarChangeListener(this);

        mSoftKeyboardTopPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mSoftKeyboardTopPopupWindow.setTouchable(true);
        mSoftKeyboardTopPopupWindow.setOutsideTouchable(false);
        mSoftKeyboardTopPopupWindow.setFocusable(false);
        mSoftKeyboardTopPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED); //解决遮盖输入法
        mSoftKeyboardTopPopupWindow.showAtLocation(mContainer, Gravity.BOTTOM, x, y);
        updateKeyboardTopViewTips(TextUtils.isEmpty(mEditText.getText()));
    }

    private void updateKeyboardTopPopupWindow(int x, int y) {
        if (mSoftKeyboardTopPopupWindow != null && mSoftKeyboardTopPopupWindow.isShowing()) {
            mSoftKeyboardTopPopupWindow.update(x, y, mSoftKeyboardTopPopupWindow.getWidth(), mSoftKeyboardTopPopupWindow.getHeight());
        }
    }

    private void closePopupWindow() {
        if (mSoftKeyboardTopPopupWindow != null && mSoftKeyboardTopPopupWindow.isShowing()) {
            mSoftKeyboardTopPopupWindow.dismiss();
            mSoftKeyboardTopPopupWindow = null;
            mKeyboardTopViewFirstTxt = null;
            mKeyboardTopViewSecondTxt = null;
            mKeyboardTopViewThirdTxt = null;
            mKeyboardTopViewFourthTxt = null;
            mKeyboardTopViewSeekBar = null;
            mInputViewIsNull = true;
        }
    }

    private void updateKeyboardTopViewTips(boolean isNull) {
        if (mInputViewIsNull == isNull) {
            return;
        }

        if (isNull) {
            if (mKeyboardTopViewFirstTxt != null) {
                mKeyboardTopViewFirstTxt.setText(KEYBOARD_TOP_VIEW_FIRST_TIP_NULL);
            }
            if (mKeyboardTopViewSecondTxt != null) {
                mKeyboardTopViewSecondTxt.setText(KEYBOARD_TOP_VIEW_SECOND_TIP_NULL);
            }
            if (mKeyboardTopViewThirdTxt != null) {
                mKeyboardTopViewThirdTxt.setText(KEYBOARD_TOP_VIEW_THIRD_TIP_NULL);
            }
            if (mKeyboardTopViewFourthTxt != null) {
                mKeyboardTopViewFourthTxt.setText(KEYBOARD_TOP_VIEW_FOURTH_TIP_NULL);
            }
            mInputViewIsNull = true;
        } else {
            if (mKeyboardTopViewFirstTxt != null) {
                mKeyboardTopViewFirstTxt.setText(KEYBOARD_TOP_VIEW_FIRST_TIP);
            }
            if (mKeyboardTopViewSecondTxt != null) {
                mKeyboardTopViewSecondTxt.setText(KEYBOARD_TOP_VIEW_SECOND_TIP);
            }
            if (mKeyboardTopViewThirdTxt != null) {
                mKeyboardTopViewThirdTxt.setText(KEYBOARD_TOP_VIEW_THIRD_TIP);
            }
            if (mKeyboardTopViewFourthTxt != null) {
                mKeyboardTopViewFourthTxt.setText(KEYBOARD_TOP_VIEW_FOURTH_TIP);
            }
            mInputViewIsNull = false;
        }
    }

    private class KeyboardOnGlobalChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {

        private int getScreenHeight() {
            return  ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getHeight();
        }

        private int getScreenWidth() {
            return ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getWidth();
        }

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            // 获取当前页面窗口的显示范围
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = getScreenHeight();
            int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
            boolean preShowing = mIsSoftKeyBoardShowing;
            if (Math.abs(keyboardHeight) > screenHeight / 5) {
                mIsSoftKeyBoardShowing = true; // 超过屏幕五分之一则表示弹出了输入法
                showKeyboardTopPopupWindow(getScreenWidth() / 2, keyboardHeight);
            } else {
                if (preShowing) {
                    closePopupWindow();
                }
                mIsSoftKeyBoardShowing = false;
            }
        }
    }
}
