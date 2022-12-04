package org.example.seata.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("second")
public class Second implements Serializable {

    private static final long serialVersionUID = -1141369866070360293L;

    @TableId
    private Long id;

    private String secondField;

    private LocalDateTime createTime;

}
