
package org.example.chat.protocal.c;

import lombok.Data;

/**
 * 登录事件
 *
 * @author wcx
 * @date 2024/05/12
 */
@Data
public class PLoginInfo {
    protected String loginUserId;
    protected String loginToken;
    protected String extra;
    protected long firstLoginTime = 0;

    public static boolean isFirstLogin(long firstLoginTime) {
        return firstLoginTime <= 0;
    }
}
