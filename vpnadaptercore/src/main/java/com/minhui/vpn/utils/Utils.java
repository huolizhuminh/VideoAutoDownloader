package com.minhui.vpn.utils;


import androidx.annotation.Keep;

import com.minhui.vpn.log.VPNLog;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.minhui.vpn.utils.VPNDirConstants.DATA_DIR;


/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/16.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */
@Keep
public class Utils {
    private static final String TAG = "Utils";

    public static void close(Closeable close){
        if(close!=null){
            try {
                close.close();
            }catch (Exception e){
                VPNLog.d(TAG,"failed to close "+e.getMessage());
            }
        }
    }
    public static boolean ispv4(String ipv4) {

        if (ipv4 == null || ipv4.length() == 0) {

            return false;//字符串为空或者空串

        }

        String[] parts = ipv4.split("\\.");//因为java doc里已经说明, split的参数是reg, 即正则表达式, 如果用"|"分割, 则需使用"\\|"

        if (parts.length != 4) {

            return false;//分割开的数组根本就不是4个数字

        }

        for (int i = 0; i < parts.length; i++) {

            try {

                int n = Integer.parseInt(parts[i]);

                if (n < 0 || n > 255) {

                    return false;//数字不在正确范围内

                }

            } catch (NumberFormatException e) {

                return false;//转换数字不正确

            }

        }

        return true;

    }
    public static String jsonFormat(String s) {
        int level = 0;
        //存放格式化的json字符串
        StringBuffer jsonForMatStr = new StringBuffer();
        //将字符串中的字符逐个按行输出
        for(int index=0;index<s.length();index++)
        {
            //获取s中的每个字符
            char c = s.charAt(index);
//          System.out.println(s.charAt(index));

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
//                System.out.println("123"+jsonForMatStr);
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }
    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    public static boolean isJasonFormat(String showStr) {
        return isStartAndEnd(showStr,"{","}")||isStartAndEnd(showStr,"[","]");
    }
    private static boolean isStartAndEnd(String showStr,String s, String s1) {
        return showStr.startsWith(s) && showStr.endsWith(s1);
    }

    public static boolean isUnicode(String bodyStr) {
        return bodyStr.contains("\\u");
    }


    /**
     * Convert a int ip value to ipv4 string.
     *
     * @param ip The ip address.
     * @return A ipv4 string value, format is N.N.N.N
     */
    public static String convertIp(int ip) {
        return String.format("%s.%s.%s.%s", (ip >> 24) & 0x00FF,
                (ip >> 16) & 0x00FF, (ip >> 8) & 0x00FF, (ip & 0x00FF));
    }

    /**
     * Convert a string ip value to int.
     *
     * @param ip The ip address.
     * @return A int ip value.
     */
    public static int convertIp(String ip) {
        String[] arrayStrings = ip.split("\\.");
        return (Integer.parseInt(arrayStrings[0]) << 24)
                | (Integer.parseInt(arrayStrings[1]) << 16)
                | (Integer.parseInt(arrayStrings[2]) << 8)
                | (Integer.parseInt(arrayStrings[3]));
    }

    /**
     * Convert a short ip value to int.
     *
     * @param port The port.
     * @return A int port value.
     */
    public static int convertPort(short port) {
        return port & 0xFFFF;
    }


}
