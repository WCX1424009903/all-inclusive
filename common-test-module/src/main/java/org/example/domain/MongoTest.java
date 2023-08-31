package org.example.domain;

import lombok.Data;
import org.example.mongodb.annotation.QueryField;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
*MongoDB测试类
*@author wcx
*@date 2022/10/23 19:06
*/
@Data
@Document("mongo_test")
public class MongoTest implements Serializable {

    @Id
    @QueryField
    private String id;

    private String userName;

    private String password;

}
