package com.minhui.vpn.nat;


import android.content.Context;

import com.minhui.vpn.processparse.AppInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/8/12.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class ConversationManager {
    public static int MAX_SHOW_CONVERSATION = 100;
    List<Conversation> cache = new LinkedList<>();
    private Context context;
    private String packageName;

    private ConversationManager() {

    }

    public synchronized void clear() {
        cache.clear();
    }

    static class InnerClass {
        static ConversationManager instance = new ConversationManager();
    }

    public void init(Context context) {
        this.context = context;
        this.packageName = context.getPackageName();
    }

    public static ConversationManager getInstance() {
        return InnerClass.instance;
    }

    public synchronized void addConversation(Conversation conversation) {
        //排除自己
        NatSession session = conversation.getSession();
        if (packageName.equals(session.pgName)) {
            return;
        }
        if (packageName.equals(session.defaultAPP)) {
            return;
        }
        if (cache.size() > MAX_SHOW_CONVERSATION) {
            cache.remove(cache.size() - 1);
        }

        cache.add(0, conversation);
    }

    public synchronized List<Conversation> getConversations() {
        ArrayList<Conversation> conversations = new ArrayList<>();

        for (Conversation conversation : cache) {
            conversation.refreshSize();
            NatSession session = conversation.getSession();
            if (packageName.equals(session.pgName)) {
                continue;
            }
            if ( packageName.equals(session.defaultAPP)) {
                continue;
            }
            conversations.add(conversation);
        }
        return conversations;
    }
}
