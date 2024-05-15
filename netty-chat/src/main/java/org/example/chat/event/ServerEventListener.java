
package org.example.chat.event;

import io.netty.channel.Channel;
import org.example.chat.protocal.Protocal;
import org.example.chat.protocal.s.PKickoutInfo;

/**
 * 服务器事件侦听器
 *
 * @author wcx
 * @date 2024/05/11
 */
public interface ServerEventListener {

    /**
     * 用户身份验证回调方法定义（即验证客户端连接的合法性，合法就允许正常能信，否则断开）.
     * <p>
     * 服务端的应用层可在本方法中实现用户登陆验证。
     * <br>
     * 注意：本回调在一种特殊情况下——即用户实际未退出登陆但再次发起来登陆包时，本回调是不会被调用的！
     * <p>
     * 本方法中用户验证通过（即方法返回值=0时）后
     * ，将立即调用回调方法 {@link #onUserLoginSucess(String, String, Channel)}。
     * 否则会将验证结果通知客户端。
     *
     * @param userId  传递过来的准一id，保证唯一就可以通信，可能是登陆用户名、也可能是任意不重复的id等，具体意义由业务层决定
     * @param token   用于身份鉴别和合法性检查的token，它可能是登陆密码，也可能是通过前置单点登陆接口拿到的token等，具体意义由业务层决定
     * @param extra   额外信息字符串。本字段目前为保留字段，供上层应用自行放置需要的内容
     * @param session 此客户端连接对应的 netty “会话”
     * @return 0 表示登陆验证通过，否则可以返回用户自已定义的错误码，错误码值应为：>=1025的整数
     */
    int onUserLoginVerify(String userId, String token, String extra, Channel session);

    /**
     * 用户登录验证成功后的回调方法定义（在业务上可理解为该用户的上线通知）.
     * <p>
     * 服务端的应用层通常可在本方法中实现用户上线通知等。
     * <br>
     * 注意：本回调在一种特殊情况下——即用户实际未退出登陆但再次发起来登陆包时，回调也是一定会被调用。
     *
     * @param userId  传递过来的准一id，保证唯一就可以通信，可能是登陆用户名、也可能是任意不重复的id等，具体意义由业务层决定
     * @param extra   额外信息字符串。本字段目前为保留字段，供上层应用自行放置需要的内容。为了丰富应用层处理的手段，在本回调中也把此字段传进来了
     * @param session 此客户端连接对应的 netty “会话”
     */
    void onUserLoginSucess(String userId, String extra, Channel session);

    /**
     * 用户退出登录回调方法定义（可理解为下线通知回调）。
     * <p>
     * 服务端的应用层通常可在本方法中实现用户下线通知等。
     *
     * @param userId        下线的用户user_id
     * @param session       此客户端连接对应的 netty “会话”
     * @param beKickoutCode 被踢原因编码，本参数当为-1时表示本次logout事件不是源自“被踢”，否则被踢原因编码请见 {@link PKickoutInfo}类中的常量定义
     */
    void onUserLogout(String userId, Channel session, int beKickoutCode);

    /**
     * 收到客户端发送给“服务端”的数据回调通知（即：消息路径为“C2S”的消息）前的处理逻辑。
     * <p>
     * <b>本方法的默认实现</b>：<font color="green">当开发者不需要本方法进行额外逻辑处理时，请直接返回true即可！</font>
     * <p>
     * <b>本方法的典型用途</b>：开发者可在本方法中实现如：用户聊天内容的鉴黄、过滤、篡改等等，把内容审读权限交给开发者，就看怎么用了。
     *
     * @param p       消息/指令的完整协议包对象
     * @param session 消息发送者的“会话”引用（也就是客户端的网络连接对象）
     * @return true表示经过本方法后将正常进入 {@link #onTransferMessage4C2S(Protocal, Channel)}继续正常逻辑  ，false表示该条指令将不会继续处理（直接被丢弃）
     * @see #onTransferMessage4C2S(Protocal, Channel)
     */
    boolean onTransferMessage4C2CBefore(Protocal p, Channel session);

    /**
     * 收到客户端发送给“服务端”的数据回调通知（即：消息路径为“C2S”的消息）.
     * <b>本方法的典型用途</b>：投递消息到MQ中，做全量消息存储等用途
     *
     * @param p       消息/指令的完整协议包对象
     * @param session 此客户端连接对应的 netty “会话”
     * @return true表示本方法已成功处理完成，否则表示未处理成功。此返回值目前框架中并没有特殊意义，仅作保留吧
     * @see Protocal
     */
    boolean onTransferMessage4C2S(Protocal p, Channel session);

    /**
     * 收到客户端发送给“其它客户端”的数据回调通知（即：消息路径为“C2C”的消息）.
     * <p>
     * <b>注意：</b>本方法当且仅当在数据被服务端成功实时发送（“实时”即意味着对方在线的情况下）出去后被回调调用.
     * <p>
     * <b>本方法的典型用途</b>：开发者可在本方法中可以实现用户聊天信息的收集，以便后期监控分析用户的行为等^_^。
     * 开发者可以对本方法不作任何代码实现，也不会影响整体运行，因为本回调并非关键逻辑，只是个普通消息传输结果的回调而已。
     * <p>
     * 提示：如果开启消息QoS保证，因重传机制，本回调中的消息理论上有重复的可能，请以参数 #fingerPrint
     * 作为消息的唯一标识ID进行去重处理。
     *
     * @param p 消息/指令的完整协议包对象
     * @see Protocal
     */
    void onTransferMessage4C2C(Protocal p);

    /**
     * 服务端在进行消息发送时，当对方在线但实时发送失败、以及其它各种问题导致消息并没能正常发出时
     * ，将无条件走本回调通知。
     *
     * <p>
     * <b>注意：</b>本方法当且仅当在数据被服务端<u>在线发送</u>失败后被回调调用.
     * <b>本方法的典型用途</b>：<br>
     * 开发者可在本方法中实现离线消息的持久化存储（反正进到本回调通知的消息，就是应该被离线存储起来的）。
     *
     * <p>
     * <b>此方法存的意义何在？</b><br>
     * 发生此种情况的场景可能是：对方确实不在线（那么此方法里就可以作为离线消息处理了）、或者在发送时判断对方是在线的
     * 但服务端在发送时却没有成功（这种情况就可能是通信错误或对方非正常通出但尚未到达会话超时时限）。<br><u>应用层在
     * 此方法里实现离线消息的处理即可！</u>
     *
     * @param p 消息/指令的完整协议包对象
     * @return true表示应用层已经处理了离线消息（如果该消息有QoS机制，则服务端将代为发送一条伪应答包
     * （伪应答仅意味着不是接收方的实时应答，而只是存储到离线DB中，但在发送方看来也算是被对方收到，只是延
     * 迟收到而已（离线消息嘛））），否则表示应用层没有处理（如果此消息有QoS机制，则发送方在QoS重传机制超时
     * 后报出消息发送失败的提示）
     * @see Protocal
     * @see #onTransferMessage4C2C(Protocal)
     * @since 4.0
     */
    boolean onTransferMessage_RealTimeSendFaild(Protocal p);
}
