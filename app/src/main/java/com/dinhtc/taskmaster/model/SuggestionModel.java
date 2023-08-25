package com.dinhtc.taskmaster.model;

import androidx.annotation.NonNull;

import com.dinhtc.taskmaster.common.widgets.tagview.interfaces.TagInterface;

import java.util.Locale;

public class SuggestionModel implements TagInterface {
    private final int id;
    private final String name;

    private final String address;

    public SuggestionModel(int id, @NonNull String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    @Override
    public String getTag() {
        return String.format(Locale.getDefault(), "@{{%d}}", id);
    }

    @Override
    public String getLabel() {
        return String.format(Locale.getDefault(), "@%s", name);
    }

}
