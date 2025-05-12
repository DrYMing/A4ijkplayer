package com.alanwang4523.a4ijkplayerdemo.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alanwang4523.a4ijkplayerdemo.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mVideoUrlEditText;
    private static final int REQUEST_CODE_SELECT_VIDEO = 1001;

    // 用于在 onResume 中恢复横屏
    private boolean mNeedRestoreOrientation = false;

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
            // 打开系统文件选择器选择视频
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // 使用现代的文件选择器 (Android 4.4+)
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");

                // 请求持久性权限
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                // 使用传统的文件选择器
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // 尝试强制文件选择器使用横屏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra("android.intent.extra.SCREEN_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            // 标记需要在 onResume 中恢复横屏
            mNeedRestoreOrientation = true;

            try {
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
            } catch (Exception e) {
                Toast.makeText(this, "无法打开文件选择器: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.btn_play) {
            // 播放URL视频
            String videoUrl = mVideoUrlEditText.getText().toString().trim();
            if (TextUtils.isEmpty(videoUrl)) {
                Toast.makeText(this, "请输入视频URL", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用VideoActivity播放URL
            VideoActivity.intentTo(this, videoUrl, "在线视频");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 如果需要恢复横屏模式
        if (mNeedRestoreOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mNeedRestoreOrientation = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                // 对于内容URI，需要授予持久性权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(selectedVideoUri, takeFlags);
                    } catch (SecurityException e) {
                        // 如果无法获取持久性权限，记录错误但继续尝试播放
                        Log.e("HomeActivity", "无法获取视频的持久性权限: " + e.getMessage());
                    }
                }

                // 获取视频文件的名称
                String videoName = "本地视频";
                try {
                    Cursor cursor = getContentResolver().query(selectedVideoUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (displayNameIndex != -1) {
                            videoName = cursor.getString(displayNameIndex);
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e("HomeActivity", "获取视频名称失败: " + e.getMessage());
                }

                // 直接使用VideoActivity播放视频
                String videoPath = selectedVideoUri.toString();
                Log.d("HomeActivity", "选择的视频URI: " + videoPath);
                VideoActivity.intentTo(this, videoPath, videoName);
            } else {
                Toast.makeText(this, "无法获取所选视频", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
