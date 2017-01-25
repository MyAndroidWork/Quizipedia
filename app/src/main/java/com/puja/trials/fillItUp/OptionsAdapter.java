package com.puja.trials.fillItUp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by puja on 25/01/17.
 */

public class OptionsAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<String> options;

    OptionsAdapter(Context context, ArrayList<String> options)
    {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.options = options;
    }

    @Override
    public int getCount() {
        return this.options.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null){
            view = inflater.inflate(R.layout.blanks_bottomlayout, null);
        }

        TextView optionTextView = (TextView) view.findViewById(R.id.optionWord);
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/Bariol_Regular.otf");
        optionTextView.setTypeface(custom_font);

        optionTextView.setText(options.get(position));

        return view;
    }
}
