package com.okunev.waifusim.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.okunev.waifusim.MessageItem;
import com.okunev.waifusim.R;

import java.util.ArrayList;

/**
 * Created by gwa on 6/13/16.
 */

public class MessageAdapter extends BaseAdapter {
    ArrayList<MessageItem> items = new ArrayList<>();
    Context context;

    public MessageAdapter(Context context, ArrayList<MessageItem> arr) {
        if (arr != null) {
            items = arr;
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.message_item, parent, false);
        }
        TextView msg = (TextView) convertView.findViewById(R.id.msg);
        TextView time = (TextView) convertView.findViewById(R.id.timeView);
        msg.setText(items.get(position).getMsg());
        time.setText(items.get(position).getTime());
        return convertView;
    }
}
