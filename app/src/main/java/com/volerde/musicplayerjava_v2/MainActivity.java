package com.volerde.musicplayerjava_v2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ContentResolver mContentResolver;
    private MediaCursorAdapter mCursorAdapter;

    private final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " = ? " + " AND " +
            MediaStore.Audio.Media.MIME_TYPE + " LIKE ? ";
    private final String[] SELLECTION_ARGS = {
            Integer.toString(1),
            "audio/mpeg"
    };

    private BottomNavigationView navigation;
    private TextView bottomTitle;
    private TextView bottomArtist;
    private ImageView albumThumbnail;
    private ImageView isPlay;

    private MediaPlayer mMediaPlayer = null;

    public static final String DATA_URI = "com.volerde.musicplayerjava_v2.DATA_URI";
    public static final String TITLE = "com.volerde.musicplayerjava_v2.TITLE";
    public static final String ARTIST = "com.volerde.musicplayerjava_v2.ARTIST";

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    @Override
    protected void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            Log.d("TAG", "onStop invoked");
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView mPlaylist = findViewById(R.id.playList);

        mContentResolver = getContentResolver();
        mCursorAdapter = new MediaCursorAdapter(MainActivity.this);
        mPlaylist.setAdapter(mCursorAdapter);


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } else {
            initPlaylist();
        }

        navigation = findViewById(R.id.navigation);
        LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.bottom_media_toolbar, navigation, true);
        isPlay = navigation.findViewById(R.id.play);
        bottomTitle = navigation.findViewById(R.id.bottom_title);
        bottomArtist = navigation.findViewById(R.id.bottom_artist);
        albumThumbnail = navigation.findViewById(R.id.thumbnail);

/*        if (isPlay != null) {
            isPlay.setOnClickListener(temp);
        }*/

        mPlaylist.setOnItemClickListener(itemClickListener);

//        navigation.setVisibility(View.GONE);
    }

/*    private ImageView.OnClickListener temp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                isPlay.setImageResource(R.drawable.ic_baseline_arrow_right_24);
            }else {
                mMediaPlayer.start();
                isPlay.setImageResource(R.drawable.ic_baseline_pause_24);
            }
        }
    };*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPlaylist();
            }
        }
    }

    private final ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Cursor cursor = mCursorAdapter.getCursor();
            if (cursor != null && cursor.moveToPosition(i)) {
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                String title = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                long albumId = cursor.getLong(albumIdIndex);
                String data = cursor.getString(dataIndex);

                Uri dataUri = Uri.parse(data);
                if (mMediaPlayer != null) {
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(getApplicationContext(), dataUri);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
                serviceIntent.putExtra(MainActivity.DATA_URI, data);
                serviceIntent.putExtra(MainActivity.TITLE,title);
                serviceIntent.putExtra(MainActivity.ARTIST,artist);
                startService(serviceIntent);

                navigation.setVisibility(View.VISIBLE);

                if (bottomTitle != null) {
                    bottomTitle.setText(title);
                }
                if (bottomArtist != null) {
                    bottomArtist.setText(artist);
                }

                Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                Cursor albumCursor = mContentResolver.query(albumUri, null, null, null, null);

                if (albumCursor != null && albumCursor.getCount() > 0) {
                    albumCursor.moveToFirst();
                    int albumArtIndex = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                    String albumArt = albumCursor.getString(albumArtIndex);
                    Glide.with(MainActivity.this).load(albumArt).into(albumThumbnail);
                    albumCursor.close();
                }
            }
        }
    };

    private void initPlaylist() {
        Cursor mCursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                SELECTION,
                SELLECTION_ARGS,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        mCursorAdapter.swapCursor(mCursor);
        mCursorAdapter.notifyDataSetChanged();
    }
}