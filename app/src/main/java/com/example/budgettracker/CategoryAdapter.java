package com.example.budgettracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryAdapter extends BaseAdapter {
    private Context context;
    private String[] names;
    private int[] icons;

    public CategoryAdapter(Context context, String[] names, int[] icons) {
        this.context = context;
        this.names = names;
        this.icons = icons;
    }

    @Override
    public int getCount() { return names.length; }

    @Override
    public Object getItem(int position) { return names[position]; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the custom layout we created for each grid item
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        }

        ImageView iconView = convertView.findViewById(R.id.ivCategoryIcon);
        TextView textView = convertView.findViewById(R.id.tvCategoryName);

        // Set the catchy icon and name
        iconView.setImageResource(icons[position]);
        textView.setText(names[position]);

        return convertView;
    }
}