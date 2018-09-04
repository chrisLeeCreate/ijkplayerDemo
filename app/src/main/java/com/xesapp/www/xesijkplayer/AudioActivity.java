package com.xesapp.www.xesijkplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xesapp.www.xesijkplayer.ijkplayer.IjkAudioController;
import com.xesapp.www.xesijkplayer.ijkplayer.IjkAudioView;
import com.xesapp.www.xesijkplayer.utils.LocalVideoUtils;

public class AudioActivity extends AppCompatActivity {

    private IjkAudioController audioController;
    private IjkAudioView ijkAudioViewUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        Button button = findViewById(R.id.button3);
        audioController = findViewById(R.id.ijkaAudio);
        ijkAudioViewUtils = new IjkAudioView();
        ijkAudioViewUtils.setAudioController(audioController, false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }

    public void loadAudio(String Url) {
        if (ijkAudioViewUtils != null) {
            ijkAudioViewUtils.setAudioPath(this, Url);
            ijkAudioViewUtils.start();
        }
        if (audioController != null) {
            audioController.setClickAble();
        }
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
                loadAudio(path);
            } else {//4.4以下下系统调用方法
                path = LocalVideoUtils.getRealPathFromURI(this, uri);
                Toast.makeText(AudioActivity.this, path + " ", Toast.LENGTH_SHORT).show();
                loadAudio(path);
            }
        }
    }

    @Override
    protected void onResume() {
        if (ijkAudioViewUtils != null) {
            ijkAudioViewUtils.start();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (ijkAudioViewUtils != null && ijkAudioViewUtils.isPlaying()) {
            ijkAudioViewUtils.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (ijkAudioViewUtils != null) {
            ijkAudioViewUtils.stopPlayback(this);
            ijkAudioViewUtils.releaseMedia(this);
        }
        super.onDestroy();
    }

}
