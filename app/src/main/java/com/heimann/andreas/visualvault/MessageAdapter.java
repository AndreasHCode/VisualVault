package com.heimann.andreas.visualvault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Andreas on 07.04.2017.
 */

public class MessageAdapter extends BaseAdapter implements ListAdapter {

    private List<Message> messages;
    private Context context;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_message_item, null);

        Message message = messages.get(position);
        String title = message.getTitle();
        String titleFull = "";
        if (title.length() > 1) {
            String titleShortStart = title.substring(0, 1);
            String titleShortEnd = title.substring(title.length() - 1, title.length());
            titleFull = titleShortStart + titleShortEnd;
        }

        TextView titleView = (TextView) view.findViewById(R.id.list_message_title);

        titleView.setText(titleFull);

        return view;
    }
}
