package org.example.mapper.second;

import org.apache.ibatis.annotations.Param;
import org.example.aop.ShardingAop;
import org.example.entity.TOrder;

import java.util.List;
import java.util.Map;

public interface SecondMapper {
    List<Map> listMenus();

    List<Map> listToder(TOrder tOrder);

    @ShardingAop(value = "#a0.orderId")
    int inserTorder(TOrder tOrder);
}
