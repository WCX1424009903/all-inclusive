
package org.example.chat.protocal;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 自定义协议体
 *
 * @author wcx
 * @date 2024/05/10
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Protocal {
    protected boolean bridge = false;

    protected int type = 0;

    protected String dataContent = null;

    protected String from = "-1";

    protected String to = "-1";
    /**
     * 发送消息指纹，qos为true时，需要服务端应答消息已达服务端
     */
    protected String fp = null;

    protected boolean QoS = false;

    protected int typeu = -1;

    protected transient int retryCount = 0;

    protected long sm = -1;

    public Protocal(int type, String dataContent, String from, String to) {
        this(type, dataContent, from, to, -1);
    }

    public Protocal(int type, String dataContent, String from, String to, int typeu) {
        this(type, dataContent, from, to, false, null, typeu);
    }

    public Protocal(int type, String dataContent, String from, String to
            , boolean QoS, String fingerPrint) {
        this(type, dataContent, from, to, QoS, fingerPrint, -1);
    }

    public Protocal(int type, String dataContent, String from, String to
            , boolean QoS, String fingerPrint, int typeu) {
        this.type = type;
        this.dataContent = dataContent;
        this.from = from;
        this.to = to;
        this.QoS = QoS;
        this.typeu = typeu;

        if (QoS && fingerPrint == null) {
            fp = Protocal.genFingerPrint();
        } else {
            fp = fingerPrint;
        }
    }

    public String toGsonString() {
        return JSON.toJSONString(this);
    }

    public byte[] toBytes() {
        return CharsetHelper.getBytes(toGsonString());
    }

    @Override
    public Object clone() {
        Protocal cloneP = new Protocal(this.getType()
                , this.getDataContent(), this.getFrom(), this.getTo(), this.isQoS(), this.getFp());
        cloneP.setBridge(this.bridge);
        cloneP.setTypeu(this.typeu);
        cloneP.setSm(this.sm);
        return cloneP;
    }

    public static long genServerTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 消息指纹，服务端发起qos心跳检测时需要用到。
     *
     * @return
     */
    public static String genFingerPrint() {
        return UUID.randomUUID().toString();
    }
}
