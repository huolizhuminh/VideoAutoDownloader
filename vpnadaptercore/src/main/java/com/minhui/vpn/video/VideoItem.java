package com.minhui.vpn.video;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Keep;

import java.io.Serializable;

@Entity
@Keep
public class VideoItem implements Serializable {
    private static final long serialVersionUID = 1L;
    String path;
    String url;

    public VideoItem(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public void setPath(String s) {
        this.path = s;
    }

    public void setUrl(String s) {
        this.url = s;
    }
}
