package com.fn.common.dispatcher.util;

import lombok.Cleanup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩工具
 *
 * @author fomin
 * @date 2019-12-07
 */
public final class GzipUtils {

    /**
     * gzip 压缩
     */
    public static byte[] compress(byte[] data) {
        try {
            @Cleanup ByteArrayOutputStream buffer = new ByteArrayOutputStream(data.length);
            @Cleanup GZIPOutputStream out = new GZIPOutputStream(buffer);
            out.write(data);
            out.finish();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * gzip 解压
     */
    public static byte[] decompress(byte[] data) {
        try {
            @Cleanup ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            @Cleanup GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
            IOUtils.copy(in, buffer);
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 在发送数据给客户端之前，对其进行压缩处理
     */
    public static byte[] encodeData(byte[] bytes, boolean compressed) {
        byte[] data = bytes;
        if (compressed && data.length > 0) {
            data = GzipUtils.compress(data);
        }
        if (data == null) {
            throw new IllegalStateException("Error encoding data...");
        }
        return data;
    }

    /**
     * 在读取从客户端发来的数据之前，对其进行解压处理
     */
    public static byte[] decodeData(byte[] bytes, boolean compressed) {
        byte[] data = bytes;
        if (compressed && data.length > 0) {
            data = GzipUtils.decompress(data);
        }
        if (data == null) {
            throw new IllegalStateException("Error decoding data...");
        }
        return data;
    }
}
