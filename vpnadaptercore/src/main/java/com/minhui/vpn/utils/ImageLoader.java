package com.minhui.vpn.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.minhui.vpn.log.VPNLog;

import java.io.File;

import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;

public class ImageLoader {
    private static final String LOG_TAG = "ImageLoader";
    LruCache<String, Bitmap> mBitmapCache = new LruCache<>(20);

    public void saveResultToCache(File videoThumbnailFile, Bitmap bitmap) {
        if (videoThumbnailFile != null && bitmap != null) {
            mBitmapCache.put(videoThumbnailFile.getAbsolutePath(), bitmap);
        }
    }

    public Bitmap getThumbnailBitmap(Context context, File file) {
        Bitmap bitmap;
        File videoThumbnailFile = MyFileUtils.getVideoThumbnailFile(file);
        if (videoThumbnailFile == null) {
            return null;
        }
        if (videoThumbnailFile.exists()) {
            bitmap = ImageLoader.getInstance().loadBitmap(videoThumbnailFile);
        } else {
            bitmap = MyFileUtils.getVideoThumbnail(file.getAbsolutePath()
                    , FULL_SCREEN_KIND
                    , DensityUtil.dip2px(context, 100f)
                    , DensityUtil.dip2px(context, 100f));
            if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                MyFileUtils.saveBitmapToThumbnail(bitmap, videoThumbnailFile);
                ImageLoader.getInstance().saveResultToCache(videoThumbnailFile, bitmap);
            } else {
                VPNLog.d(LOG_TAG, "getThumbnailBitmap invalid bitmap ");
            }

        }
        return bitmap;
    }

    public boolean isThumbnailExist(Context context, File videoFile) {
        if (videoFile == null) {
            return false;
        }
        if (videoFile.isDirectory()) {
            return false;
        }
        String absolutePath = videoFile.getAbsolutePath();
        if (mBitmapCache.get(absolutePath) != null) {
            return true;
        }
        File videoThumbnailFile = MyFileUtils.getVideoThumbnailFile(videoFile);
        if (videoThumbnailFile == null) {
            return false;
        }
        if (videoThumbnailFile.exists() && videoThumbnailFile.length() > 0) {
            return true;
        } else {
            Bitmap bitmap = MyFileUtils.getVideoThumbnail(videoFile.getAbsolutePath()
                    , FULL_SCREEN_KIND
                    , DensityUtil.dip2px(context, 100f)
                    , DensityUtil.dip2px(context, 100f));
            if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                MyFileUtils.saveBitmapToThumbnail(bitmap, videoThumbnailFile);
                ImageLoader.getInstance().saveResultToCache(videoThumbnailFile, bitmap);
                return true;
            } else {
                VPNLog.d(LOG_TAG, "getThumbnailBitmap invalid bitmap ");
            }

        }
        return false;
    }

    private static class Inner {
        private static ImageLoader imageLoader = new ImageLoader();
    }

    public static ImageLoader getInstance() {
        return Inner.imageLoader;
    }

    public Bitmap loadBitmap(File file) {
        String absolutePath = file.getAbsolutePath();
        Bitmap bitmap = mBitmapCache.get(absolutePath);
        if (bitmap != null) {
            return bitmap;
        }
        Bitmap newBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (newBitmap != null) {
            mBitmapCache.put(file.getAbsolutePath(), newBitmap);
        }
        return newBitmap;
    }
}
