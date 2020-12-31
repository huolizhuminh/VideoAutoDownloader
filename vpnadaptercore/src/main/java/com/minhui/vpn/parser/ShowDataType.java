package com.minhui.vpn.parser;

public class ShowDataType {
    public static final int ALL=0;
    public static final int TEXT=1;
    public static final int IMAGE=2;
    public static final int AUDIO=3;
    public static final int VIDEO =4;
    public static final int UDP =5;
    public static final int OTHER =6;
    public static final String HTML_STR = "html";
    public static final String JSON_STR = "json";
    public static final String TXT_STR = "txt";
    public static final String TEXT_STR = "text";
    public static final String JAVA_STR = "java";
    public static final String MULTI_FORM_STR = "multipart";
    public static final String ZIP_STR = "zip";
    public static final String IMAGE_STR = "image";
    public static final String URLENCODED_STR = "urlencoded";
    public static final String AUDIO_STR = "audio";
    public static final String VIDEO_STR = "video";
    public static final String BYTES_STR = "BYTES";
    public static final String CONTENT_FILE_STR = "contentFile";

    public static int getContentType(String contentType) {
        if (contentType != null) {
            String toLowerCase = contentType.toLowerCase();
            if (contentType.contains(HTML_STR)
                    || contentType.contains(JSON_STR)
                    || contentType.contains(TXT_STR)
                    || contentType.contains(JAVA_STR)
                    || contentType.contains(TEXT_STR)) {
                return ShowDataType.TEXT;
            }
            if (toLowerCase.contains(IMAGE_STR)) {
                return ShowDataType.IMAGE;
            } else if (toLowerCase.contains(VIDEO_STR)) {
                return ShowDataType.VIDEO;
            } else if (toLowerCase.contains(AUDIO_STR)) {
                return ShowDataType.AUDIO;
            } else {
                return ShowDataType.OTHER;
            }
        } else {
            return ShowDataType.TEXT;
        }
    }


}
