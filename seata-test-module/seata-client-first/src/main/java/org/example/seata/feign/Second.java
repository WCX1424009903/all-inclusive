package org.example.seata.feign;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Second implements Serializable {
    private static final long serialVersionUID = -1141369866070360293L;

    private Long id;

    private String secondField;

    private LocalDateTime createTime;

}
