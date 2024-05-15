
package org.example.chat.protocal.s;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Server端错误响应体
 *
 * @author wcx
 * @date 2024/05/10
 */
@Data
@AllArgsConstructor
public class PErrorResponse {
    protected int errorCode = -1;

    protected String errorMsg = null;

}
