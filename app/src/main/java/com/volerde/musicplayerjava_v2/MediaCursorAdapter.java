package com.volerde.musicplayerjava_v2;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

class MediaCursorAdapter extends CursorAdapter {

    private Context mContext;
    private final LayoutInflater mLayoutInflater;

    public MediaCursorAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View itemView = mLayoutInflater.inflate(R.layout.list_item, viewGroup, false);

        if (itemView != null) {
            ViewHolder vh = new ViewHolder();
            vh.title = itemView.findViewById(R.id.title);
            vh.artist = itemView.findViewById(R.id.artist);
            vh.order = itemView.findViewById(R.id.order);
            vh.divider = itemView.findViewById(R.id.divider);
            itemView.setTag(vh);
            return itemView;
        }
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vh = (ViewHolder) view.getTag();
        Object tag = view.getTag();

        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String title = cursor.getString(titleIndex);
        String artist = cursor.getString(artistIndex);

        int position = cursor.getPosition();

        if (vh != null) {
            vh.title.setText(title);
            vh.artist.setText(artist);
            vh.order.setText(Integer.toString(position + 1));
        }
    }

    public static class ViewHolder {
        TextView title;
        TextView artist;
        TextView order;
        View divider;
    }
}


