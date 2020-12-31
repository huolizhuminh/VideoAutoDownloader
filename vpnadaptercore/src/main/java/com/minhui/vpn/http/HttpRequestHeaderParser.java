package com.minhui.vpn.http;

import android.text.TextUtils;


import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.parser.CommonMethods;

import java.util.Locale;

/**
 * Created by minhui.zhu on 15/12/30.
 */
public class HttpRequestHeaderParser {

    private static final String TAG = "HttpRequestHeaderParser";

    public static void parseHttpRequestHeader(NatSession session, byte[] buffer, int offset, int count) {
        try {
            switch (buffer[offset]) {
                //GET
                case 'G':
                    //HEAD
                case 'H':
                    //POST, PUT
                case 'P':
                    //DELETE
                case 'D':
                    //OPTIONS
                case 'O':
                    //TRACE
                case 'T':
                    //CONNECT
                case 'C':
                    getHttpHostAndRequestUrl(session, buffer, offset, count);
                    break;
                //SSL
                case 0x16:
                    if (session.isHttpsSession) {
                        break;
                    }
                    session.remoteHost = getSNI(session, buffer, offset, count);
                    session.isHttpsSession = true;
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            if (VPNLog.debug) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public static void getHttpHostAndRequestUrl(NatSession session, byte[] buffer, int offset, int count) {
        String headerString = new String(buffer, offset, count);
        String[] headerLines = headerString.split("\\r\\n");
        if (!headerLines[0].toLowerCase().contains("http")) {
            return;
        }
        session.isHttp = true;
        String host = getHttpHost(headerLines);
        if (!TextUtils.isEmpty(host)) {
            session.remoteHost = host;
        }
        RequestLineParseData requestLineParseData = RequestLineParseData.parseData(headerLines[0]);
        if (requestLineParseData != null) {
            session.refreshRequestData(requestLineParseData);
        }

        //   paresRequestLine(session, headerLines[0]);
    }

    public static String getRemoteHost(byte[] buffer, int offset, int count) {
        String headerString = new String(buffer, offset, count);
        String[] headerLines = headerString.split("\\r\\n");
        return getHttpHost(headerLines);
    }

    public static String getHttpHost(String[] headerLines) {
        for (int i = 1; i < headerLines.length; i++) {
            String[] nameValueStrings = headerLines[i].split(":");
            if (nameValueStrings.length == 2 || nameValueStrings.length == 3) {
                String name = nameValueStrings[0].toLowerCase(Locale.ENGLISH).trim();
                String value = nameValueStrings[1].trim();
                if ("host".equals(name)) {
                    return value;
                }
            }
        }
        return null;
    }


    public static String getSNI(NatSession session, byte[] buffer, int offset, int count) {
        int limit = offset + count;
        //TLS Client Hello
        if (count > 43 && buffer[offset] == 0x16) {
            //Skip 43 byte header
            offset += 43;

            //read sessionID
            if (offset + 1 > limit) {
                return null;
            }
            int sessionIDLength = buffer[offset++] & 0xFF;
            offset += sessionIDLength;

            //read cipher suites
            if (offset + 2 > limit) {
                return null;
            }

            int cipherSuitesLength = CommonMethods.readShort(buffer, offset) & 0xFFFF;
            offset += 2;
            offset += cipherSuitesLength;

            //read Compression method.
            if (offset + 1 > limit) {
                return null;
            }
            int compressionMethodLength = buffer[offset++] & 0xFF;
            offset += compressionMethodLength;
            if (offset == limit) {
                VPNLog.w(TAG, "TLS Client Hello packet doesn't contains SNI info.(offset == limit)");
                return null;
            }

            //read Extensions
            if (offset + 2 > limit) {
                return null;
            }
            int extensionsLength = CommonMethods.readShort(buffer, offset) & 0xFFFF;
            offset += 2;

            if (offset + extensionsLength > limit) {
                VPNLog.w(TAG, "TLS Client Hello packet is incomplete.");
                return null;
            }

            while (offset + 4 <= limit) {
                int type0 = buffer[offset++] & 0xFF;
                int type1 = buffer[offset++] & 0xFF;
                int length = CommonMethods.readShort(buffer, offset) & 0xFFFF;
                offset += 2;
                //have SNI
                if (type0 == 0x00 && type1 == 0x00 && length > 5) {
                    offset += 5;
                    length -= 5;
                    if (offset + length > limit) {
                        return null;
                    }
                    String serverName = new String(buffer, offset, length);
                    VPNLog.d(TAG, "SNI: " + serverName);
                    return serverName;
                } else {
                    offset += length;
                }

            }
            VPNLog.i(TAG, "TLS Client Hello packet doesn't contains Host field info.");
            return null;
        } else {
            VPNLog.e(TAG, "Bad TLS Client Hello packet.");
            return null;
        }
    }


}
