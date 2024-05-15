
package org.example.chat.processor;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.network.Gateway;
import org.example.chat.protocal.s.PKickoutInfo;
import org.example.chat.utils.LocalSendHelper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * netty-chat在线处理器
 *
 * @author wcx
 * @date 2024/05/10
 */
@Slf4j
public class OnlineProcessor {
    /**
     * 存储用户id
     */
    public static final AttributeKey<String> ATTRIBUTE_KEY_USER_ID = AttributeKey.newInstance("__user_id__");
    /**
     * 首次登录时间
     */
    public static final AttributeKey<Long> ATTRIBUTE_KEY_FIRST_LOGIN_TIME = AttributeKey.newInstance("__first_login_time__");
    /**
     * 被剔除编码
     */
    public static final AttributeKey<Integer> ATTRIBUTE_KEY_BE_KICKOUT_CODE = AttributeKey.newInstance("__be_keickout_code__");
    /**
     * Dcl单例模式
     */
    private static volatile OnlineProcessor instance = null;

    private final ConcurrentMap<String, Channel> onlineSessions = new ConcurrentHashMap<>();

    public static OnlineProcessor getInstance() {
        if (instance == null) {
            synchronized (OnlineProcessor.class) {
                if (instance == null) {
                    instance = new OnlineProcessor();
                }
            }
        }
        return instance;
    }

    private OnlineProcessor() {
    }

    /**
     * 用户与通道之间进行绑定，用于后续服务端主动推送消息
     *
     * @param userId         用户id
     * @param firstLoginTime 首次登录时间
     * @param newSession     新的session
     * @return
     */
    public boolean putUser(String userId, long firstLoginTime, Channel newSession) {
        boolean putOk = true;
        final Channel oldSession = onlineSessions.get(userId);
        if (oldSession != null) {

            boolean isTheSame = (oldSession.compareTo(newSession) == 0);

            log.debug("[IMCORE-{}]【注意】用户id={}已经在在线列表中了，session也是同一个吗？{}", Gateway.getGatewayFlag(newSession), userId, isTheSame);

            /************* 以下将展开同一账号重复登陆情况的处理逻辑 *************/

            if (!isTheSame) {
                if (firstLoginTime <= 0) {
                    log.debug("[IMCORE-{}]【注意】用户id={}提交过来的firstLoginTime未设置(值={}, 应该是真的首次登陆？！)，将无条件踢出前面的会话！"
                            , Gateway.getGatewayFlag(newSession), userId, firstLoginTime);
                    sendKickoutDuplicateLogin(oldSession, userId);
                    onlineSessions.put(userId, newSession);
                } else {
                    long firstLoginTimeForOld = OnlineProcessor.getFirstLoginTimeFromChannel(oldSession);
                    if (firstLoginTime > firstLoginTimeForOld) {
                        log.debug("[IMCORE-{}]【提示】用户id={}提交过来的firstLoginTime为{}、firstLoginTimeForOld为{}，新的“首次登陆时间”【晚于】列表中的“老的”、正常踢出老的即可！"
                                , Gateway.getGatewayFlag(newSession), userId, firstLoginTime, firstLoginTimeForOld);
                        sendKickoutDuplicateLogin(oldSession, userId);
                        onlineSessions.put(userId, newSession);
                    } else if (firstLoginTime == firstLoginTimeForOld) {
                        log.error("[IMCORE-{}]【注意】用户id={}提交过来的firstLoginTime为{}、firstLoginTimeForOld为{}，新的“首次登陆时间”【等于】列表中的“老的”、此时不能踢出老的！【Bug Fix 20240426, since v6.5】"
                                , Gateway.getGatewayFlag(newSession), userId, firstLoginTime, firstLoginTimeForOld);
                        onlineSessions.put(userId, newSession);
                    } else {
                        log.debug("[IMCORE-{}]【注意】用户id={}提交过来的firstLoginTime为{}、firstLoginTimeForOld为{}，新的“首次登陆时间”【早于】列表中的“老的”，表示“新”的会话应该是未被正常通知的“已踢”会话，应再次向“新”会话发出被踢通知！！"
                                , Gateway.getGatewayFlag(newSession), userId, firstLoginTime, firstLoginTimeForOld);
                        sendKickoutDuplicateLogin(newSession, userId);
                        putOk = false;
                    }
                }
            } else {
                onlineSessions.put(userId, newSession);
            }
        } else {
            onlineSessions.put(userId, newSession);
        }
        // 打印在线用户
        printOnline();
        return putOk;
    }

    /**
     * 踢出重复登录的会话
     *
     * @param sessionBeKick 被踢出的session会话
     * @param toUserId      目标用户
     */
    private void sendKickoutDuplicateLogin(final Channel sessionBeKick, String toUserId) {
        try {
            LocalSendHelper.sendKickout(sessionBeKick, toUserId, PKickoutInfo.KICKOUT_FOR_DUPLICATE_LOGIN, null);
            log.debug("[IMCORE-{}]【提示】服务端正在向用户id={}发送被踢指令！", Gateway.getGatewayFlag(sessionBeKick), toUserId);
        } catch (Exception e) {
            log.warn("[IMCORE-" + Gateway.getGatewayFlag(sessionBeKick) + "] sendKickoutDuplicate的过程中发生了异常：", e);
        }
    }

    public void printOnline() {
        log.debug("【@】当前在线用户共(" + onlineSessions.size() + ")人------------------->");
        for (String key : onlineSessions.keySet()) {
            log.debug("      > user_id=" + key + ",session=" + onlineSessions.get(key).remoteAddress());
        }
    }

    /**
     * 移除该用户会话
     *
     * @param userId 用户id
     * @return
     */
    public boolean removeUser(String userId) {
        synchronized (onlineSessions) {
            if (!onlineSessions.containsKey(userId)) {
                log.warn("[IMCORE]！用户id={}不存在在线列表中，本次removeUser没有继续.", userId);
                printOnline();
                return false;
            } else {
                return (onlineSessions.remove(userId) != null);
            }
        }
    }

    /**
     * 根据用户id获取该用户会话session，用于发送消息
     *
     * @param userId
     * @return
     */
    public Channel getOnlineSession(String userId) {
        if (userId == null) {
            log.warn("[IMCORE][CAUTION] getOnlineSession时，作为key的user_id== null.");
            return null;
        }

        return onlineSessions.get(userId);
    }

    /**
     * 获取在线session会话集合
     *
     * @return
     */
    public ConcurrentMap<String, Channel> getOnlineSessions() {
        return onlineSessions;
    }


    public static boolean isLogined(Channel session) {
        return session != null && getUserIdFromChannel(session) != null;
    }

    public static boolean isOnline(String userId) {
        return OnlineProcessor.getInstance().getOnlineSession(userId) != null;
    }

    public static void setUserIdForChannel(Channel session, String userId) {
        session.attr(OnlineProcessor.ATTRIBUTE_KEY_USER_ID).set(userId);
    }

    public static void setFirstLoginTimeForChannel(Channel session, long firstLoginTime) {
        session.attr(OnlineProcessor.ATTRIBUTE_KEY_FIRST_LOGIN_TIME).set(firstLoginTime);
    }

    public static void setBeKickoutCodeForChannel(Channel session, int beKickoutCode) {
        session.attr(OnlineProcessor.ATTRIBUTE_KEY_BE_KICKOUT_CODE).set(beKickoutCode);
    }

    /**
     * 获取用户id
     *
     * @param session 会话
     * @return
     */
    public static String getUserIdFromChannel(Channel session) {
        return (session != null ? session.attr(ATTRIBUTE_KEY_USER_ID).get() : null);
    }

    /**
     * 获取通道首次登录时间
     *
     * @param session 会话
     * @return
     */
    public static long getFirstLoginTimeFromChannel(Channel session) {
        if (session != null) {
            Long attr = session.attr(ATTRIBUTE_KEY_FIRST_LOGIN_TIME).get();
            return attr != null ? attr : -1;
        }
        return -1;
    }

    public static int getBeKickoutCodeFromChannel(Channel session) {
        if (session != null) {
            Integer attr = session.attr(ATTRIBUTE_KEY_BE_KICKOUT_CODE).get();
            return attr != null ? attr : -1;
        }
        return -1;
    }

    /**
     * 移除此会话的所有属性
     *
     * @param session
     */
    public static void removeAttributesForChannel(Channel session) {
        session.attr(OnlineProcessor.ATTRIBUTE_KEY_USER_ID).set(null);
        session.attr(OnlineProcessor.ATTRIBUTE_KEY_FIRST_LOGIN_TIME).set(null);
        session.attr(OnlineProcessor.ATTRIBUTE_KEY_BE_KICKOUT_CODE).set(null);
    }
}
