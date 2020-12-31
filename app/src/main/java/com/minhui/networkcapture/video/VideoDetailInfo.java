package com.minhui.networkcapture.video;


import com.minhui.bdvideoplayer.bean.IVideoInfo;

public class VideoDetailInfo implements IVideoInfo {

    public String title;
    public String videoPath;

    @Override
    public String getVideoTitle() {
        return title;
    }

    @Override
    public String getVideoPath() {
        return videoPath;
    }

    public VideoDetailInfo(String title, String videoPath) {
        this.title = title;
        this.videoPath = videoPath;
    }
}
