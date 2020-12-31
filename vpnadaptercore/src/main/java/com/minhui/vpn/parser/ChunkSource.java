package com.minhui.vpn.parser;

import com.minhui.vpn.parser.AbstractSource;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

import okio.Buffer;
import okio.BufferedSource;
import okio.Source;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/16.
 *         从okhttp源码中拷贝
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class ChunkSource extends AbstractSource {
    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    private static final long NO_CHUNK_YET = -1L;
    private long bytesRemainingInChunk = NO_CHUNK_YET;
    private boolean hasMoreChunks = true;
    public ChunkSource(BufferedSource source) {
        super(source);
    }

    @Override public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed) throw new IllegalStateException("closed");
        if (!hasMoreChunks) return -1;

        if (bytesRemainingInChunk == 0 || bytesRemainingInChunk == NO_CHUNK_YET) {
            readChunkSize();
            if (!hasMoreChunks) return -1;
        }

        long read = super.read(sink, Math.min(byteCount, bytesRemainingInChunk));
        if (read == -1) {
            ProtocolException e = new ProtocolException("unexpected end of stream");
            detachTimeout(timeout);
            throw e;
        }
        bytesRemainingInChunk -= read;
        return read;
    }

    private void readChunkSize() throws IOException {
        // Read the suffix of the previous chunk.
        if (bytesRemainingInChunk != NO_CHUNK_YET) {
            source.readUtf8LineStrict();
        }
        try {
            bytesRemainingInChunk = source.readHexadecimalUnsignedLong();
            String extensions = source.readUtf8LineStrict().trim();
            if (bytesRemainingInChunk < 0 || (!extensions.isEmpty() && !extensions.startsWith(";"))) {
                throw new ProtocolException("expected chunk size and optional extensions but was \""
                        + bytesRemainingInChunk + extensions + "\"");
            }
        } catch (NumberFormatException e) {
            throw new ProtocolException(e.getMessage());
        }
        if (bytesRemainingInChunk == 0L) {
            hasMoreChunks = false;
            detachTimeout(timeout);
        }
    }

    @Override public void close() throws IOException {
        if (closed) return;
        if (hasMoreChunks && !discard(this, DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
            detachTimeout(timeout);
        }
        closed = true;
    }
    /**
     * Attempts to exhaust {@code source}, returning true if successful. This is useful when reading a
     * complete source is helpful, such as when doing so completes a cache body or frees a socket
     * connection for reuse.
     */
    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads until {@code in} is exhausted or the deadline has been reached. This is careful to not
     * extend the deadline if one exists already.
     */
    public static boolean skipAll(Source source, int duration, TimeUnit timeUnit) throws IOException {
        long now = System.nanoTime();
        long originalDuration = source.timeout().hasDeadline()
                ? source.timeout().deadlineNanoTime() - now
                : Long.MAX_VALUE;
        source.timeout().deadlineNanoTime(now + Math.min(originalDuration, timeUnit.toNanos(duration)));
        try {
            Buffer skipBuffer = new Buffer();
            while (source.read(skipBuffer, 8192) != -1) {
                skipBuffer.clear();
            }
            return true; // Success! The source has been exhausted.
        } catch (InterruptedIOException e) {
            return false; // We ran out of time before exhausting the source.
        } finally {
            if (originalDuration == Long.MAX_VALUE) {
                source.timeout().clearDeadline();
            } else {
                source.timeout().deadlineNanoTime(now + originalDuration);
            }
        }
    }
}
