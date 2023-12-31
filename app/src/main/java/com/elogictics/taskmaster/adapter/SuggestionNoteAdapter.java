package com.elogictics.taskmaster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elogictics.taskmaster.R;
import com.elogictics.taskmaster.model.SuggestionNoteModel;

import java.util.ArrayList;
import java.util.List;

public class SuggestionNoteAdapter extends ArrayAdapter<SuggestionNoteModel> {

    private final Context mContext;

    private final int mResourceId;
    private final List<SuggestionNoteModel> mList, mTempList, mSuggestionList;

    public SuggestionNoteAdapter(@NonNull Context context, int resourceId,
                             List<SuggestionNoteModel> items) {
        super(context, resourceId, items);
        mContext = context;
        mResourceId = resourceId;
        mList = items;
        mTempList = new ArrayList<>(items);
        mSuggestionList = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(mResourceId, parent, false);
        }
        SuggestionNoteModel model = getItem(position);
        TextView nameTextView = view.findViewById(R.id.textView_name);
        TextView usernameTextView = view.findViewById(R.id.textView_username);
        nameTextView.setText(model.getName());
        usernameTextView.setText(model.getAddress());
        return view;
    }

    @NonNull
    @Override
    public SuggestionNoteModel getItem(int position) {

        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            SuggestionNoteModel model = (SuggestionNoteModel) resultValue;
            return model.getTag();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {
                String query = charSequence.toString().toLowerCase();
                mSuggestionList.clear();
                for (SuggestionNoteModel model : mTempList) {
                    if (model.getLabel().toLowerCase().contains(query)
                            || ("@" + model.getAddress()).toLowerCase().contains(query)) {
                        mSuggestionList.add(model);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mSuggestionList;
                filterResults.count = mSuggestionList.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            try{
                clear();
                if (filterResults != null && filterResults.count > 0) {
                    SuggestionNoteAdapter.this.addAll((ArrayList<SuggestionNoteModel>) filterResults.values);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }catch (UnsupportedOperationException e){
                e.printStackTrace();
            }
        }
    };
}
