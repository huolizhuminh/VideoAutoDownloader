package com.minhui.networkcapture.video;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.minhui.bdvideoplayer.BDVideoView;
import com.minhui.bdvideoplayer.listener.SimpleOnVideoControlListener;
import com.minhui.bdvideoplayer.utils.DisplayUtils;
import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;
import com.minhui.networkcapture.utils.ContextUtil;
import com.minhui.networkcapture.utils.PermissionUtil;
import com.minhui.vpn.greenDao.SessionHelper;
import com.minhui.vpn.greenDao.VideoItemDao;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.utils.ThreadProxy;
import com.minhui.vpn.video.VideoItem;

import java.io.File;
import java.util.List;

import static com.minhui.networkcapture.utils.ContextUtil.PROVIDER_NAME;


public class VideoDetailActivity extends BaseActivity {

    private static final String TAG = "VideoDetailActivity";
    private static final int REQUIRE_READ_WRITE_PERMISSION = 1001;
    private BDVideoView videoView;
    private long startTime;
    private VideoDetailInfo info;

    public static void start(Context context, VideoDetailInfo info) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra("info", info);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   setContentView(R.layout.activity_video_detail);

        info = (VideoDetailInfo) getIntent().getSerializableExtra("info");

        videoView = (BDVideoView) findViewById(R.id.vv);
        videoView.setOnVideoControlListener(new SimpleOnVideoControlListener() {

            @Override
            public void onRetry(int errorStatus) {
                // TODO: 2017/6/20 调用业务接口重新获取数据
                // get info and call method "videoView.startPlayVideo(info);"
            }

            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onFullScreen() {
                DisplayUtils.toggleScreenOrientation(VideoDetailActivity.this);
            }
        });
        videoView.startPlayVideo(info);
        startTime = System.currentTimeMillis();
     //   InterstitialManager.getInstance().showSplashAds(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.share) {
            share();
            return true;
        } else if (itemId == R.id.save_video) {
            saveVideo();
        } else if (itemId == R.id.add_favorite) {
            addFavorite();
        } else if (itemId == R.id.copy_url) {
            copyUrl();
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyUrl() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                VideoItem videoItem = getVideoItemFormAll();
                if (videoItem == null) {
                    return;
                }
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm == null) {
                    return;
                }
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("requestUrl", videoItem.getUrl());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VideoDetailActivity.this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private VideoItem getVideoItemFormAll() {
        VideoItemDao videoItemDao = SessionHelper
                .getDaoSession(getApplicationContext(), SessionHelper.VIDEO_TABLE)
                .getVideoItemDao();
        List<VideoItem> videoItems = videoItemDao
                .queryBuilder()
                .where(VideoItemDao.Properties.Path.eq(info.videoPath))
                .list();
        if (videoItems == null || videoItems.isEmpty()) {
            return null;
        }
        return videoItems.get(0);
    }
    private VideoItem getVideoItemFormFavorite() {
        VideoItemDao videoItemDao = SessionHelper
                .getDaoSession(getApplicationContext(), SessionHelper.VIDEO_FAVORITE_TABLE)
                .getVideoItemDao();
        List<VideoItem> videoItems = videoItemDao
                .queryBuilder()
                .where(VideoItemDao.Properties.Path.eq(info.videoPath))
                .list();
        if (videoItems == null || videoItems.isEmpty()) {
            return null;
        }
        return videoItems.get(0);
    }

    private void addFavorite() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                VideoItem videoItemFormFavorite = getVideoItemFormFavorite();
                if(videoItemFormFavorite!=null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoDetailActivity.this, getString(R.string.has_add_favorite_success), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                VideoItem videoItemFormAll = getVideoItemFormAll();
                if(videoItemFormAll==null){
                    return;
                }
                VideoItemDao videoItemDao = SessionHelper
                        .getDaoSession(getApplicationContext(), SessionHelper.VIDEO_FAVORITE_TABLE)
                        .getVideoItemDao();
                videoItemDao.insert(videoItemFormAll);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VideoDetailActivity.this, getString(R.string.has_add_favorite_success), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void saveVideo() {
        String[] requirePermissions = PermissionUtil.getRequirePermissions(this, PermissionUtil.READ_WRITE_STORAGE);
        if (requirePermissions == null || requirePermissions.length == 0) {
            realSaveVideo();
        } else {
            ActivityCompat.requestPermissions(this, requirePermissions, REQUIRE_READ_WRITE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        VPNLog.d(TAG, "onRequestPermissionsResult requestCode = " + requestCode
                + " permissions = " + permissions + " grantResults = " + grantResults);
        if (requestCode == REQUIRE_READ_WRITE_PERMISSION) {
            boolean isGranted = true;
            for (int result : grantResults) {
                VPNLog.d(TAG, "onRequestPermissionsResult result = " + result);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                }
            }
            if (isGranted) {
                realSaveVideo();
            }
        }
    }

    private void realSaveVideo() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                MyFileUtils.saveFileToAlbum(getApplicationContext(), info.videoPath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.success_save_video) + MyFileUtils.PICTURE_SHOW_PATH
                                , Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
    }

    private void share() {
        try {
            if (info.videoPath == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_file), Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(info.videoPath);
            if (!file.exists()) {
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_file), Toast.LENGTH_SHORT).show();
                return;
            }
            String providerName = ContextUtil.getAppMetaData(getApplicationContext(), PROVIDER_NAME);
            Uri videoUri = FileProvider.getUriForFile(getApplicationContext(), providerName, file);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
            shareIntent.setType("video/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
        } catch (Exception e) {
            VPNLog.e(TAG, "failed to share video " + e.getMessage());

        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_video_detail;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        videoView.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        videoView.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        videoView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!DisplayUtils.isPortrait(this)) {
            if (!videoView.isLock()) {
                DisplayUtils.toggleScreenOrientation(this);
            }
        } else {
            super.onBackPressed();
        }
    }
}
