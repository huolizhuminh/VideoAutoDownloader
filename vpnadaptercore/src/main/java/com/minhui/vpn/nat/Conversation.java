package com.minhui.vpn.nat;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.minhui.vpn.greenDao.DaoSession;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/8/1.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
@Keep
@Entity
public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id(autoincrement = true)
    Long id =0L;
    @Transient
    NatSession session;
    String sessionTag;
    int index;
    String requestURL;
    long size;
    long time;
    int type;
    @Transient
    transient DaoSession daoSession;

    public void refreshDb() {
        if (daoSession == null) {
            return;
        }
        if (id == 0L) {
            return;
        }
        daoSession.getConversationDao().update(this);
    }

    public void deleteDb() {
        if (daoSession == null) {
            return;
        }
        if (id == 0L) {
            return;
        }
        daoSession.getConversationDao().delete(this);
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getSessionTag() {
        return sessionTag;
    }

    private Conversation(Builder builder) {
        session = builder.session;
        index = builder.index;
        requestURL = builder.requestURL;
        size = builder.size;
        time = builder.time;
        sessionTag = builder.sessionTag;
        daoSession = builder.daoSession;
    }

    @Generated(hash = 1536498274)
    public Conversation(Long id, String sessionTag, int index, String requestURL,
                        long size, long time, int type) {
        this.id = id;
        this.sessionTag = sessionTag;
        this.index = index;
        this.requestURL = requestURL;
        this.size = size;
        this.time = time;
        this.type = type;
    }

    @Generated(hash = 1893991898)
    public Conversation() {
    }

    public NatSession getSession() {
        return session;
    }

    public int getIndex() {
        return index;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public long getSize() {
        return size;
    }

    public long getTime() {
        return time;
    }

    public List<File> getShowDataFile() {
        File requestFile = session.getReqSaveDataFile(index);
        File responseFile = session.getRespSaveDataFile(index);
        ArrayList<File> files = new ArrayList<>();
        files.add(requestFile);
        files.add(responseFile);
        return files;
    }

    public String getTAG() {
        return getSession().getIpAndPort() + index;
    }

    public void refreshSize() {
        size = session.getConversationSize(index);
    }

    public void delete() {
        getSession().deleteConversation(index);
    }

    public void setSize(Long conversationSize) {
        size = conversationSize;

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSessionTag(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setNatSession(NatSession natSessions) {
        this.session=natSessions;
    }

    public void setDatSession(DaoSession daoSession) {
        this.daoSession=daoSession;
    }

    public static final class Builder {
        private NatSession session;
        private int index;
        private String requestURL;
        private long size;
        private long time;
        private String sessionTag;
        private DaoSession daoSession;

        public Builder() {
        }

        public Builder session(NatSession val) {
            session = val;
            return this;
        }

        public Builder index(int val) {
            index = val;
            return this;
        }

        public Builder requestURL(String val) {
            requestURL = val;
            return this;
        }

        public Builder size(long val) {
            size = val;
            return this;
        }

        public Builder time(long val) {
            time = val;
            return this;
        }

        public Builder sessionTag(String val) {
            sessionTag = val;
            return this;
        }

        public Builder daoSession(DaoSession val) {
            daoSession = val;
            return this;
        }

        public Conversation build() {
            return new Conversation(this);
        }
    }
}
