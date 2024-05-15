
package org.example.chat.protocal;

import java.io.UnsupportedEncodingException;

/**
 * 字符集工具类
 *
 * @author wcx
 * @date 2024/05/10
 */
public class CharsetHelper {
    public final static String ENCODE_CHARSET = "UTF-8";
    public final static String DECODE_CHARSET = "UTF-8";

    public static String getString(byte[] b, int len) {
        try {
            return new String(b, 0, len, DECODE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return new String(b, 0, len);
        }
    }

    public static String getString(byte[] b, int start, int len) {
        try {
            return new String(b, start, len, DECODE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return new String(b, start, len);
        }
    }

    public static byte[] getBytes(String str) {
        if (str != null) {
            try {
                return str.getBytes(ENCODE_CHARSET);
            } catch (UnsupportedEncodingException e) {
                return str.getBytes();
            }
        }
        return new byte[0];
    }
}
