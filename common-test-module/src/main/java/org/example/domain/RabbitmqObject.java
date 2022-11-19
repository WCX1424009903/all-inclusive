package org.example.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
*rabbitmq对象接收
*@author wcx
*@date 2022/11/19 17:23
*/
@Getter
@Setter
public class RabbitmqObject implements Serializable {
    private static final long serialVersionUID = 5664991294682495011L;

    private String userName;

    private String telPhone;

    private String account;

}
