package com.elogictics.taskmaster.common.widgets.tagview;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.elogictics.taskmaster.common.widgets.tagview.interfaces.TagInterface;
import com.elogictics.taskmaster.common.widgets.tagview.utils.TagUtils;

import java.util.ArrayList;
import java.util.List;

public class TagTextView extends AppCompatTextView implements TextWatcher {

    private List<TagInterface> mTags = new ArrayList<>();

    public TagTextView(Context context) {
        super(context);
        init();
    }

    public TagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        removeTextChangedListener(this);
        addTextChangedListener(this);
    }

    public void setTags(List<? extends TagInterface> tags) {
        mTags = new ArrayList<>(tags);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mTags.size() > 0) {
            TagUtils.parseText(s, mTags);
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

}
