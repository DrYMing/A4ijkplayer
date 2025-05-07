package com.alanwang4523.a4ijkplayerdemo.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alanwang4523.a4ijkplayerdemo.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mVideoUrlEditText;
    private static final int REQUEST_CODE_SELECT_VIDEO = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置为横屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_home);

        mVideoUrlEditText = findViewById(R.id.et_video_url);

        ImageButton selectLocalButton = findViewById(R.id.btn_select_local);
        ImageButton playButton = findViewById(R.id.btn_play);

        selectLocalButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_select_local) {
            // Open file explorer to select a video
            Intent intent = new Intent(this, FileExplorerActivity.class);
            startActivity(intent);
        } else if (id == R.id.btn_play) {
            // Play the video from URL
            String videoUrl = mVideoUrlEditText.getText().toString().trim();
            if (TextUtils.isEmpty(videoUrl)) {
                Toast.makeText(this, "Please enter a video URL", Toast.LENGTH_SHORT).show();
                return;
            }
            videoUrl = "";
            // Use the existing VideoActivity to play the URL
            VideoActivity.intentTo(this, videoUrl, "Video from URL");
        }
    }
}
