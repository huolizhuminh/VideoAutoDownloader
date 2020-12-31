package com.minhui.networkcapture.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minhui.networkcapture.R;
import com.minhui.vpn.log.VPNLog;
import com.minhui.networkcapture.video.VideoDetailActivity;
import com.minhui.networkcapture.video.VideoDetailInfo;
import com.minhui.vpn.utils.ImageLoader;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.utils.ThreadProxy;
import com.minhui.vpn.utils.TimeFormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoCaptureView extends RecyclerView {
    private static final String LOG_TAG = "VideoCaptureView";
    private List<BaseItemInfo> mCaptureShowItem;
    private Handler mHandler;
    private VideoAdapter mAdapter;
    private static final int TIME_TYPE = 0;
    private static final int VIDEO_TYPE = 1;

    public VideoCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler();
        mAdapter = new VideoAdapter();
        setAdapter(mAdapter);
        LinearLayoutManager linerLayoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(linerLayoutManager);
    }


    public void refreshDataAndRefreshView(List<String> fileList) {
        VPNLog.d(LOG_TAG, "refreshDataAndRefreshView size = " + fileList.size());
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<File> newFileList = new ArrayList<>();
                long startTime = System.currentTimeMillis();
                for (String file : fileList) {
                    File videoFile = new File(file);
                    if (videoFile.isDirectory()) {
                        continue;
                    }
                    boolean isThumbnailExist = ImageLoader.getInstance().isThumbnailExist(getContext(), videoFile);
                    if (isThumbnailExist) {
                        newFileList.add(videoFile);
                        VPNLog.d(LOG_TAG, "refreshDataAndRefreshView  has thumbnail path = " + videoFile.getAbsolutePath());
                    } else {
                        VPNLog.d(LOG_TAG, "refreshDataAndRefreshView not has thumbnail path = " + videoFile.getAbsolutePath());
                    }
                    if ((newFileList.size() % 3 == 0)
                            && (System.currentTimeMillis() - startTime > 2000)
                            && newFileList.size() > 0) {
                        startTime = System.currentTimeMillis();
                        List<File> copyFile = new ArrayList<>();
                        copyFile.addAll(newFileList);
                        mCaptureShowItem = getShowItemFromCaptureFile(copyFile);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                VPNLog.d(LOG_TAG, "refreshDataAndRefreshView newFileList = " + newFileList.size());
                mCaptureShowItem = getShowItemFromCaptureFile(newFileList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    private List<BaseItemInfo> getShowItemFromCaptureFile(List<File> copyFile) {
        List<File> timeSortedFileList = MyFileUtils.getTimeSortedFileList(copyFile);
        List<BaseItemInfo> baseItemInfoList = new ArrayList<>();
        int day = 0;
        String currentCaptureTime = null;
        VideoItemInfo videoItemInfo = null;
        for (File file : timeSortedFileList) {
            String captureTime = TimeFormatUtil.formatYYMMDD(file.lastModified());
            if (!captureTime.equals(currentCaptureTime)) {
                day++;
                baseItemInfoList.add(new TimeSectorInfo(captureTime));
                currentCaptureTime = captureTime;
                videoItemInfo = new VideoItemInfo();
                baseItemInfoList.add(videoItemInfo);
            }
            if (videoItemInfo.filePath_1 == null) {
                videoItemInfo.filePath_1 = file;
            } else if (videoItemInfo.filePath_2 == null) {
                videoItemInfo.filePath_2 = file;
            } else if (videoItemInfo.filePath_3 == null) {
                videoItemInfo.filePath_3 = file;
            } else {
                videoItemInfo = new VideoItemInfo();
                videoItemInfo.filePath_1 = file;
                baseItemInfoList.add(videoItemInfo);
            }
        }
        if (day == 1 && baseItemInfoList.size() > 0) {
            baseItemInfoList.remove(0);
        }
        return baseItemInfoList;
    }

    private class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIDEO_TYPE) {
                return new VideoHolder(View.inflate(parent.getContext(), R.layout.layout_video_list, null));
            } else {
                return new TimeSectorHolder(View.inflate(parent.getContext(), R.layout.layout_textview, null));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BaseItemInfo itemInfo = mCaptureShowItem.get(position);
            if (VIDEO_TYPE == itemInfo.itemType) {
                VideoItemInfo videoItemInfo = (VideoItemInfo) itemInfo;
                VideoHolder videoHolder = (VideoHolder) holder;
                videoHolder.bindData(videoItemInfo);
            } else {
                TimeSectorHolder timeSectorHolder = (TimeSectorHolder) holder;
                timeSectorHolder.textView.setText(((TimeSectorInfo) itemInfo).date);
            }

        }

        @Override
        public int getItemCount() {
            if (mCaptureShowItem == null) {
                return 0;
            } else {
                return mCaptureShowItem.size();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mCaptureShowItem.get(position).itemType;
        }
    }

    class TimeSectorHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TimeSectorHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_item);
        }
    }

    class VideoHolder extends RecyclerView.ViewHolder {
        VideoItemView videoItemView_1;
        VideoItemView videoItemView_2;
        VideoItemView videoItemView_3;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            videoItemView_1 = itemView.findViewById(R.id.item_1);
            videoItemView_2 = itemView.findViewById(R.id.item_2);
            videoItemView_3 = itemView.findViewById(R.id.item_3);
        }

        public void bindData(VideoItemInfo videoItemInfo) {
            videoItemView_1.bindData(videoItemInfo.filePath_1);
            videoItemView_2.bindData(videoItemInfo.filePath_2);
            videoItemView_3.bindData(videoItemInfo.filePath_3);
        }
    }

    class BaseItemInfo {
        int itemType;
    }

    class VideoItemInfo extends BaseItemInfo {
        File filePath_1;
        File filePath_2;
        File filePath_3;

        public VideoItemInfo() {
            itemType = VIDEO_TYPE;
        }
    }

    class TimeSectorInfo extends BaseItemInfo {
        String date;

        public TimeSectorInfo(String date) {
            this.date = date;
            itemType = TIME_TYPE;
        }
    }

}
