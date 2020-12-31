package com.minhui.vpn.log;


import androidx.core.util.Pools;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Log message to file.
 *
 * @author minhui
 * @since 17/9/22 21:49
 */

/* package */ final class FileLog implements ILog {

    private final LogWriter mWriter;

    private final List<String> mTagsToFile;

    private final boolean vToLog;
    private final boolean dToLog;
    private final boolean iToLog;
    private final boolean wToLog;
    private final boolean eToLog;
    private final boolean wtfToLog;

    /* package */ FileLog (VLog.FileConfig config) {
        if (config.logFile == null) {
            throw new RuntimeException("Log file must not be empty!");
        }

        this.mTagsToFile = Arrays.asList(config.tagsToFile);
        this.mWriter = new LogWriter(config.logFile, config.headers, config.limitSize);

        List<Integer> levelsToFile = Arrays.asList(config.levelsToFile);
        this.vToLog = levelsToFile.isEmpty() || levelsToFile.contains(VLog.VERBOSE);
        this.dToLog = levelsToFile.isEmpty() || levelsToFile.contains(VLog.DEBUG);
        this.iToLog = levelsToFile.isEmpty() || levelsToFile.contains(VLog.INFO);
        this.wToLog = levelsToFile.isEmpty() || levelsToFile.contains(VLog.WARN);
        this.eToLog = levelsToFile.isEmpty() || levelsToFile.contains(VLog.ERROR);
        this.wtfToLog = levelsToFile.isEmpty() || levelsToFile.contains(VLog.WTF);
    }

    @Override
    public void v(String tag, String msg) {
        if (!vToLog || (!mTagsToFile.isEmpty() && !mTagsToFile.contains(tag))) {
            return;
        }
        mWriter.write("[V]", tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        if (!dToLog || (!mTagsToFile.isEmpty() && !mTagsToFile.contains(tag))) {
            return;
        }
        mWriter.write("[D]", tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        if (!iToLog || (!mTagsToFile.isEmpty() && !mTagsToFile.contains(tag))) {
            return;
        }
        mWriter.write("[I]", tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        if (!wToLog || (!mTagsToFile.isEmpty() && !mTagsToFile.contains(tag))) {
            return;
        }
        mWriter.write("[W]", tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        if (!eToLog) {
            return;
        }
        mWriter.write("[E]", tag, msg);
    }

    @Override
    public void wtf(String tag, Throwable throwable) {
        if (!wtfToLog || (!mTagsToFile.isEmpty() && !mTagsToFile.contains(tag))) {
            return;
        }
        String msg = throwable.getMessage();
        StringBuilder sb = new StringBuilder(msg == null ? "" : msg);
        StackTraceElement[] elements = throwable.getStackTrace();
        if (elements != null) {
            for (StackTraceElement element : elements) {
                sb.append("\n");
                for (int i = 0; i < 22 + tag.length(); i++) {
                    sb.append(" ");
                }
                sb.append(element.toString());
            }
        }
        mWriter.write("[WTF]", tag, sb.toString());
    }

    @Override
    public void close() throws IOException {
        if (mWriter != null) {
            mWriter.close();
        }
    }

    private static class Log {

        private long time;
        private String level;
        private String tag;
        private String content;

        private Log(String level, String tag, String content) {
            this(System.currentTimeMillis(), level, tag, content);
        }

        private Log(long time, String level, String tag, String content) {
            this.time = time;
            this.level = level;
            this.tag = tag;
            this.content = content;
        }

        private void update(String level, String tag, String content) {
            this.time = System.currentTimeMillis();
            this.level = level;
            this.tag = tag;
            this.content = content;
        }

    }

    private static class LogWriter implements Closeable {

        private static final int MAX_LOG_SIZE_IN_POOL = 8;

        private final long mLimitSize;
        private final Pools.Pool<Log> mLogPool;
        private final File mOriginLogFile;

        private ExecutorService mExecutor = Executors.newFixedThreadPool(1);

        private File mLogFile;
        private BufferedWriter mBufferedWriter;
        private int mIndex = 1;

        private String[] mHeaders;

        private boolean closed;

        private LogWriter(File logFile, String[] headers, long limitSize) {
            // validate log file
            if (logFile.isDirectory()) {
                throw new IllegalArgumentException("Can not log to a directory!");
            }
            if (logFile.exists()) {
                logFile.delete();
            } else {
                File parent = logFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
            }

            this.mOriginLogFile = logFile;
            this.mLimitSize = limitSize;
            this.mLogPool = new Pools.SimplePool<>(MAX_LOG_SIZE_IN_POOL);

            this.mLogFile = logFile;

            this.mHeaders = headers;
        }

        private void write(final String level, final String tag, final String msg) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    writeAsync(newLog(level, tag, msg));
                }
            });
        }

        @Override
        public void close() throws IOException {
            closed = true;
            mExecutor.shutdown();
            if (mBufferedWriter != null) {
                mBufferedWriter.close();
            }
        }

        private Log newLog(String level, String tag, String msg) {
            Log log = mLogPool.acquire();
            if (log != null) {
                log.update(level, tag, msg);
            } else {
                log = new Log(level, tag, msg);
            }
            return log;
        }

        private void writeAsync(Log log) {
            if (closed) {
                return;
            }
            // check log file limit size
            if (mLimitSize > 0 && mLogFile.length() >= mLimitSize) {
                newLogFile();
            }

            // build content
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
            writeToFile(mLogFile, format.format(new Date(log.time)) + " " + log.level + " " +
                    log.tag +  " : " + log.content);

            // recycle log instance
            mLogPool.release(log);
        }

        private void newLogFile() {
            mIndex ++;
            String fileName = mOriginLogFile.getName();
            int extendIndex = fileName.lastIndexOf(".");
            if (extendIndex > 0) {
                mLogFile = new File(mOriginLogFile.getParent(),
                        new StringBuilder(fileName).insert(extendIndex, "-" + mIndex).toString());
            } else {
                mLogFile = new File(mOriginLogFile.getParent(), fileName + "-" + mIndex);
            }
            mBufferedWriter = null;
        }

        private void writeToFile(File file, String content) {
            try {
                // check file status
                if (!file.exists()) {
                    File parent = file.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        // may be no sdcard permissions
                        return;
                    }
                    // we must reset writer
                    mBufferedWriter = null;
                }
                if (mBufferedWriter == null) {
                    mBufferedWriter = new BufferedWriter(new FileWriter(file));
                }

                // write headers first
                if (mHeaders != null && mHeaders.length != 0) {
                    for (String header : mHeaders) {
                        if (header != null) {
                            mBufferedWriter.write(header);
                            mBufferedWriter.newLine();
                        }
                    }
                    mHeaders = null;
                }

                mBufferedWriter.write(content);
                mBufferedWriter.newLine();
                mBufferedWriter.flush();
            } catch (IOException e) {
                // may be closed
            }
        }

    }

}
