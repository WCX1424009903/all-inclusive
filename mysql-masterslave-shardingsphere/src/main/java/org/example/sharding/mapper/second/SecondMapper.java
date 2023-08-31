package org.example.sharding.mapper.second;

import org.example.sharding.aop.ShardingAop;
import org.example.sharding.entity.TOrder;

import java.util.List;
import java.util.Map;

public interface SecondMapper {
    List<Map> listMenus();
    @ShardingAop(value = "#a0.orderNumber")
    List<Map> listToder(TOrder tOrder);

    @ShardingAop(value = "#a0.orderNumber")
    int inserTorder(TOrder tOrder);
}
