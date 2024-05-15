
package org.example.chat.protocal;

import com.alibaba.fastjson.JSON;
import org.example.chat.protocal.c.PLoginInfo;
import org.example.chat.protocal.s.PErrorResponse;
import org.example.chat.protocal.s.PKeepAliveResponse;
import org.example.chat.protocal.s.PLoginInfoResponse;

/**
 * 自定义协议体构造工厂
 *
 * @author wcx
 * @date 2024/05/10
 */
public class ProtocalFactory {
    private static String create(Object c) {
        return JSON.toJSONString(c);
    }

    public static <T> T parse(byte[] fullProtocalJSONBytes, int len, Class<T> clazz) {
        return parse(CharsetHelper.getString(fullProtocalJSONBytes, len), clazz);
    }

    public static <T> T parse(String dataContentOfProtocal, Class<T> clazz) {
        return JSON.parseObject(dataContentOfProtocal, clazz);
    }

    public static Protocal parse(byte[] fullProtocalJSONBytes, int len) {
        return parse(fullProtocalJSONBytes, len, Protocal.class);
    }

    public static Protocal createCommonData(String dataContent, String from_user_id, String to_user_id, boolean QoS, String fingerPrint) {
        return createCommonData(dataContent, from_user_id, to_user_id, QoS, fingerPrint, -1);
    }

    public static Protocal createCommonData(String dataContent, String from_user_id, String to_user_id, boolean QoS, String fingerPrint, int typeu) {
        return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA, dataContent, from_user_id, to_user_id, QoS, fingerPrint, typeu);
    }

    public static Protocal createPKeepAliveResponse(String to_user_id) {
        return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE, create(new PKeepAliveResponse()), "0", to_user_id);
    }

    public static PLoginInfo parsePLoginInfo(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, PLoginInfo.class);
    }

    public static Protocal createPLoginInfoResponse(int code, long firstLoginTime, String user_id) {
        return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN, create(new PLoginInfoResponse(code, firstLoginTime)), "0", user_id, false, null);
    }

    public static Protocal createRecivedBack(String from_user_id, String to_user_id, String recievedMessageFingerPrint) {
        return createRecivedBack(from_user_id, to_user_id, recievedMessageFingerPrint, false);
    }

    public static Protocal createRecivedBack(String from_user_id, String to_user_id, String recievedMessageFingerPrint, boolean bridge) {
        Protocal p = new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED, recievedMessageFingerPrint, from_user_id, to_user_id);
        p.setBridge(bridge);
        return p;
    }

    public static Protocal createPErrorResponse(int errorCode, String errorMsg, String user_id) {
        return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR, create(new PErrorResponse(errorCode, errorMsg)), "0", user_id);
    }

}
