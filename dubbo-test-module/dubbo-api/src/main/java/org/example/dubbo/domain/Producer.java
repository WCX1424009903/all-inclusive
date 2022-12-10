package org.example.dubbo.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
*实体类
* @author wcx
* @date 2022/12/10
*/
@Getter
@Setter
public class Producer implements Serializable {
    private static final long serialVersionUID = 6457982954688369501L;

    private Long userId;

    private String userName;

    private LocalDateTime createTime;

}
