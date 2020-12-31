package com.minhui.vpn.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.text.TextUtils;

import com.minhui.vpn.log.VPNLog;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;
import static com.minhui.vpn.utils.VPNDirConstants.DATA_DIR;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/6.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class MyFileUtils {
    public static final String PICTURE_SHOW_PATH = "sdcard/Pictures/VideoCapture";
    private static final String TAG = "MyFileUtils";
    private static Map<String, String> captureTime = new ConcurrentHashMap<>();
    //在picture目录下新建一个自己文件夹
    public static final String rootPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/VideoCapture";

    /**
     * 这个方法用来把已经存在的一个文件存储到相册
     *
     * @param context 用来发送广播
     * @param srcString 需要拷贝的文件的地址
     */
    public static void saveFileToAlbum(Context context, String srcString) {
        if (TextUtils.isEmpty(srcString)) {
            return;
        }
        File srcFile = new File(srcString);
        if (!srcFile.exists()) {
            return;
        }
        //如果root文件夹没有需要新建一个
        createDirIfNotExist();

        //拷贝文件到picture目录下
        File destFile = new File(rootPath + "/" + srcFile.getName());
        copyFile(srcFile, destFile);

        //将该文件扫描到相册
        MediaScannerConnection.scanFile(context, new String[] { destFile.getPath() }, null, null);
    }

    public static void createDirIfNotExist() {
        File file = new File(rootPath);
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 删除文件
     *
     * @param 需要删除的文件或文件夹
     * @param 判断文件是否需要删除，为空时所有文件都需要删除
     */
    public static void deleteFile(File file, FileFilter fileFilter) {
        if (file == null) {
            return;
        }
        if (fileFilter != null && !fileFilter.accept(file)) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        if (files == null) {
            file.delete();
            return;
        }
        for (File childFile : files) {
            deleteFile(childFile, fileFilter);
        }
        file.delete();
    }


    public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
    public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
    public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
    public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VPNLog.d(TAG, "获取文件大小失败!");
        }
        return FormatFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VPNLog.d(TAG, "获取文件大小失败!");
        }
        return FormatFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    private static double FormatFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }


    public static byte[] getByteFromFile(String fileStr) {
        FileInputStream in = null;
        byte[] bytes;
        try {
            File file = new File(fileStr);
            bytes = new byte[(int) file.length()];
            in = new FileInputStream(file);
            in.read(bytes);
        } catch (Exception e) {

            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

                }

            }
        }
        return bytes;

    }


    /**
     * 压缩文件和文件夹
     *
     * @param srcFileString 要压缩的文件或文件夹
     * @param zipFileString 压缩完成的Zip路径
     * @throws Exception
     */
    public static void zipFolder(String srcFileString, String zipFileString) throws Exception {
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
        File file = new File(srcFileString);
        zipFiles(file.getParent() + File.separator, file.getName(), outZip);
        //完成和关闭
        outZip.finish();
        outZip.close();
    }

    /**
     * 压缩文件
     *
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    private static void zipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
        if (zipOutputSteam == null)
            return;
        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
            inputStream.close();
        } else {
            //文件夹
            String fileList[] = file.list();
            //没有子文件和压缩
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //子文件和递归
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderString + fileString + File.separator, fileList[i], zipOutputSteam);
            }
        }
    }

    public static int getSumFileSize(File file) {
        int sumFileSize = 0;
        File[] list = file.listFiles();
        if (list == null || list.length == 0) {
            return 0;
        }
        for (File childFile : list) {
            if (childFile.isDirectory()) {
                sumFileSize = sumFileSize + getSumFileSize(childFile);
            } else {
                sumFileSize = sumFileSize + 1;
            }
        }
        return sumFileSize;
    }

    public static void saveStrToFile(String publicKey, File pubKey) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(pubKey);
            byte[] bytes = publicKey.getBytes();
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            Utils.close(fileOutputStream);
        }
    }

    public static Bitmap getVideoThumbnail(String videoPath, int kind, int width, int height) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        if (width > 0 && height > 0) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }

        return bitmap;
    }

    public static void saveVideoThumbnail(Context context, File videoFile) {
        if (videoFile == null || !videoFile.exists() || videoFile.isDirectory()) {
            return;
        }
        Bitmap bitmap = MyFileUtils.getVideoThumbnail(videoFile.getAbsolutePath()
                , FULL_SCREEN_KIND
                , DensityUtil.dip2px(context, 100f)
                , DensityUtil.dip2px(context, 100f));
        if (bitmap.getHeight() > 0 && bitmap.getHeight() > 0) {
            File thumbnail = getVideoThumbnailFile(videoFile);
            saveBitmapToThumbnail(bitmap, thumbnail);
        }
    }

    public static void saveBitmapToThumbnail(Bitmap bitmap, File thumbnail) {
        if (bitmap.getHeight() <= 0 || bitmap.getHeight() <= 0) {
            return;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(thumbnail);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            try {
                out.close();
            } catch (Exception err) {

            }
        }
    }

    public static File getVideoThumbnailFile(File videoFile) {
        String parent = videoFile.getParent();
        File thumbnailDir = new File(parent, "thumbnail");
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs();
        }
        String name = videoFile.getName();
        if (!name.contains(".")) {
            return null;
        }
        String firstName = name.substring(0, name.indexOf("."));
        String thumbName = firstName + ".png";
        return new File(thumbnailDir, thumbName);
    }

    public static List<File> getTimeSortedChildFile(File file) {
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, files);
        Iterator<File> iterator = fileList.iterator();
        //过滤掉空文件夹
        while (iterator.hasNext()) {
            File next = iterator.next();
            if (next.isDirectory()) {
                iterator.remove();
            }
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (int) (o2.lastModified() - o1.lastModified());
            }
        });
        return fileList;
    }

    public static List<File> getTimeSortedFileList(List<File> fileList) {
        Iterator<File> iterator = fileList.iterator();
        //过滤掉空文件夹
        while (iterator.hasNext()) {
            File next = iterator.next();
            if (next.isDirectory()) {
                iterator.remove();
            }
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (int) (o2.lastModified() - o1.lastModified());
            }
        });
        return fileList;
    }

    public static void copyFile(File childFile, File desFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(childFile);
            out = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Utils.close(in);
            Utils.close(out);
        }
    }

    public static String getVideoTotalTime(File videoFile) {
        if (!videoFile.exists()) {
            return null;
        }
        String key = videoFile.getAbsolutePath() + videoFile.lastModified();
        String saveCaptureTime = captureTime.get(key);
        if (saveCaptureTime != null) {
            return saveCaptureTime;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.getAbsolutePath());
        String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {
            int secSum = Integer.parseInt(timeString) / 1000;
            int hour = secSum / 3600;
            int min = (secSum - hour * 3600) / 60;
            int sec = secSum - hour * 3600 - min * 60;
            String showStr = "";
            if (hour > 0) {
                showStr = showStr + hour + ":";
            }
            if (min >= 10) {
                showStr = showStr + min + ":";
            } else {
                showStr = showStr + "0" + min + ":";
            }
            if (sec >= 10) {
                showStr = showStr + sec;
            } else {
                showStr = showStr + "0" + sec;
            }
            captureTime.put(key, showStr);
            return showStr;
        } catch (Exception e) {

        }
        return null;
    }
}
