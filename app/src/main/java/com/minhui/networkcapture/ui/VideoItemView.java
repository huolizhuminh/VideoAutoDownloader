package com.minhui.networkcapture.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.video.VideoDetailActivity;
import com.minhui.networkcapture.video.VideoDetailInfo;
import com.minhui.vpn.utils.ImageLoader;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.utils.ThreadProxy;

import java.io.File;

public class VideoItemView extends RelativeLayout {
    ImageView thumbnail;
    TextView playTime;

    public VideoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(getContext(), R.layout.layout_videocontent, this);
        initView();
    }

    private void initView() {
        thumbnail = findViewById(R.id.image);
        playTime = findViewById(R.id.play_time);
    }

    public void bindData(File file) {
        if (file == null || !file.exists()) {
            setVisibility(INVISIBLE);
            return;
        } else {
            setVisibility(VISIBLE);
        }
        thumbnail.setTag(file.getAbsolutePath());
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Bitmap finalBitmap = ImageLoader.getInstance().getThumbnailBitmap(getContext()
                        , file);
                String videoTotalTime = MyFileUtils.getVideoTotalTime(file);

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (file.getAbsolutePath().equals(thumbnail.getTag())) {
                            thumbnail.setImageBitmap(finalBitmap);
                            playTime.setText(videoTotalTime);
                        }
                    }
                });

            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoDetailInfo info = new VideoDetailInfo(file.getName(), file.getAbsolutePath());
                VideoDetailActivity.start(getContext(), info);
            }
        });
    }
}
