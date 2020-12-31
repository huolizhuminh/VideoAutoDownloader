package com.minhui.vpn.parser;

import com.minhui.vpn.log.VPNLog;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Pattern;

import static com.minhui.vpn.utils.VPNConstants.MAX_SHOW_LENGTH;


/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/8/5.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class MyStrUtil {
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String TAG = "MyStrUtil";
    private static final Pattern NORMAL_PATTERN = Pattern.compile("[\\x00-\\x7F]+");
    private static final int CHECK_CLEAR_LENGTH = 20;

    /**
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte bytes) {

        char[] buf = new char[2];
        int a = 0;
        if (bytes < 0) {
            a = 256 + bytes;
        } else {
            a = bytes;
        }

        buf[0] = HEX_CHAR[a / 16];
        buf[1] = HEX_CHAR[a % 16];

        return new String(buf);
    }

    public static boolean isClearStr(String readStr) {
        if (readStr == null) {
            return false;
        }
        int length = readStr.length() < CHECK_CLEAR_LENGTH ? readStr.length() : CHECK_CLEAR_LENGTH;
        CharsetEncoder charsetEncoder = Charset.forName("GBK").newEncoder();
        for (int i = 0; i < length; i++) {
            String substring = readStr.substring(i, i + 1);
            if (!charsetEncoder.canEncode(substring)) {
                return false;
            }
        }
        return true;
    }

    public static String getClearStr(String readStr) {
        VPNLog.d(TAG, "getClearStr " + readStr.length());
        String oneStr;
        StringBuilder builder = new StringBuilder();
        long startTime = System.currentTimeMillis();
        CharsetEncoder charsetEncoder = Charset.forName("GBK").newEncoder();
        for (int z = 0; z < readStr.length(); z++) {
            oneStr = readStr.substring(z, z + 1);
            if (charsetEncoder.canEncode(oneStr)) {
                builder.append(oneStr);
            } else {
                builder.append(".");
            }
        }
        VPNLog.d(TAG, "getClearStr " + readStr.length() + " constTime" + (System.currentTimeMillis() - startTime));
        return builder.toString();
    }

    public static String getRawHexStr(byte[] bodyBytes) {
        if (bodyBytes == null) {
            return null;
        }
        int consumedByte = 0;
        int rowSize = 16;
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[rowSize];
        while ((consumedByte < bodyBytes.length)) {
            byte[] showByte = null;
            int leftByte = bodyBytes.length - consumedByte;
            if (leftByte < rowSize) {
                showByte = new byte[leftByte];
                copyData(bodyBytes, showByte, consumedByte, leftByte);
            } else {
                copyData(bodyBytes, buffer, consumedByte, rowSize);
                showByte = buffer;
            }

            consumedByte = consumedByte + showByte.length;
            for (int i = 0; i < rowSize; i++) {
                String byteToStr = null;
                if (i > showByte.length - 1) {
                    byteToStr = "";
                } else {
                    byteToStr = MyStrUtil.bytesToHex(showByte[i]);
                }
                builder.append(byteToStr);
            }


        }
        return builder.toString();
    }

    public static void copyData(byte[] res, byte[] des, int consumedByte, int consumeNum) {
        for (int i = 0; i < des.length; i++) {
            des[i] = 0;
        }
        for (int i = 0; i < consumeNum; i++) {
            des[i] = res[consumedByte + i];
        }

    }

    public static String getShowHexStr(byte[] bodyBytes, int rowSize) {
        return getShowHexStr(bodyBytes, rowSize, ShowData.DEFAULT_CHARSET);
    }

    public static String getShowHexStr(byte[] bodyBytes, int rowSize, String charSet) {
        int consumedByte = 0;
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[rowSize];
        int needConsumeData = bodyBytes.length > MAX_SHOW_LENGTH ? MAX_SHOW_LENGTH : bodyBytes.length;
        while ((consumedByte < needConsumeData)) {
            byte[] showByte = null;
            int leftByte = needConsumeData - consumedByte;
            if (leftByte < rowSize) {
                showByte = new byte[leftByte];
                copyData(bodyBytes, showByte, consumedByte, leftByte);
            } else {
                copyData(bodyBytes, buffer, consumedByte, rowSize);
                showByte = buffer;
            }
            String contentStr = null;
            try {
                contentStr = MyStrUtil.getClearStr(new String(showByte, charSet));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (contentStr == null) {
                contentStr = new String(showByte);
            }
            contentStr = contentStr.replace("\n", " ");
            consumedByte = consumedByte + showByte.length;
            for (int i = 0; i < rowSize; i++) {
                String byteToStr = null;
                if (i > showByte.length - 1) {
                    byteToStr = "   ";
                } else {
                    byteToStr = MyStrUtil.bytesToHex(showByte[i]) + " ";
                }
                builder.append(byteToStr);
                if (i == rowSize / 2 - 1) {
                    builder.append(" ");
                }
            }
            builder.append("   ").append(contentStr)
                    .append("\r\n");


        }
        return builder.toString();
    }

    /**
     * 从 Unicode 形式的字符串转换成对应的编码的特殊字符串。 如 "\u9EC4" to "黄".
     * Converts encoded \\uxxxx to unicode chars
     * and changes special saved chars to their original forms
     *
     * @param in       Unicode编码的字符数组。
     * @param off      转换的起始偏移量。
     * @param len      转换的字符长度。
     * @param convtBuf 转换的缓存字符数组。
     * @return 完成转换，返回编码前的特殊字符串。
     */
    public static String fromEncodedUnicode(char[] in, int off, int len) {
        char aChar;
        // 只短不长
        char[] out = new char[len];
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                break;
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = (char) aChar;
            }
        }
        return new String(out, 0, outLen);
    }

}
