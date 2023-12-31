package com.elogictics.taskmaster.common.widgets.tagview.models;


import com.elogictics.taskmaster.common.widgets.tagview.interfaces.TagInterface;

public class TagIndex implements TagInterface {

    private final int start;
    private final int count;
    private String tag;
    private String label;

    public TagIndex(int start, int count) {
        this.start = start;
        this.count = count;
    }

    public TagIndex(int start, TagInterface tag) {
        this.start = start;
        this.tag = tag.getTag();
        this.count = this.tag.length();
        this.label = tag.getLabel();
    }

    public int getStart() {
        return start;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
