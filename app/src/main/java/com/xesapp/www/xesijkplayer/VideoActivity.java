package com.xesapp.www.xesijkplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.xesapp.www.xesijkplayer.ijkplayer.IjkMediaController;
import com.xesapp.www.xesijkplayer.ijkplayer.IjkVideoView;
import com.xesapp.www.xesijkplayer.utils.LocalVideoUtils;

public class VideoActivity extends AppCompatActivity {

    private IjkVideoView ijkVideoView;
    private IjkMediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ijkVideoView = findViewById(R.id.videoView);
        mediaController = findViewById(R.id.video_controller);
        ijkVideoView.setMediaController(mediaController);

    }

    public void openLocalVideo(View v) {
        if (ijkVideoView != null) {
            ijkVideoView.stopPlayback();
            ijkVideoView.release(true);
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    public void openNetVideo(View v) {
        String url = "http://118.212.138.72/vod.cntv.lxdns.com/flash/mp4video1/qgds/2009/12/27/qgds_h264418000nero_aac32_20091227_1261891653006.mp4?wshc_tag=0&wsts_tag=5b8d1442&wsid_tag=d20c78f1&wsiphost=ipdbm";
        loadVideo(url);
    }

    public void loadVideo(String Url) {
        ijkVideoView.setVideoPath(Url);
        ijkVideoView.resume();
        ijkVideoView.start();
    }

    private String path;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
//                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = LocalVideoUtils.getPath(this, uri);
                Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                loadVideo(path);
            } else {//4.4以下下系统调用方法
                path = LocalVideoUtils.getRealPathFromURI(this, uri);
                Toast.makeText(VideoActivity.this, path + " ", Toast.LENGTH_SHORT).show();
                loadVideo(path);
            }
        }
    }


    @Override
    protected void onResume() {
        if (ijkVideoView != null) {
            ijkVideoView.start();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (ijkVideoView != null && ijkVideoView.isPlaying()) {
            ijkVideoView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (ijkVideoView != null) {
            ijkVideoView.stopPlayback();
            ijkVideoView.release(true);
        }
        super.onDestroy();
    }
}
