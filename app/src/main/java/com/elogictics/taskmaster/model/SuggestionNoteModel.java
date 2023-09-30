package com.elogictics.taskmaster.model;

import androidx.annotation.NonNull;

import com.elogictics.taskmaster.common.widgets.tagview.interfaces.TagInterface;

import java.util.Locale;

public class SuggestionNoteModel implements TagInterface {
    private final int id;

    private final int empId;
    private final String name;

    private final String address;

    public SuggestionNoteModel(int id,int empId, @NonNull String name, String address) {
        this.id = id;
        this.empId = empId;
        this.name = name;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public int getEmpId() {
        return empId;
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
