
package org.example.chat.protocal.s;

import lombok.Data;

/**
 * server端主动踢出信息体
 *
 * @author wcx
 * @date 2024/05/10
 */
@Data
public class PKickoutInfo {
    /**
     * 重复登录编码
     */
    public final static int KICKOUT_FOR_DUPLICATE_LOGIN = 1;
    /**
     * 管理员踢出编码
     */
    public final static int KICKOUT_FOR_ADMIN = 2;

    protected int code = -1;
    /**
     * 踢出原因
     */
    protected String reason = null;


}
