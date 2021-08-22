package org.example.controller;

import org.example.entity.TOrder;
import org.example.mapper.first.FirstMapper;
import org.example.mapper.second.SecondMapper;
import org.example.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 多数据源及分表测试
 * @author wcx
 * @date 2021/6/3 22:02
 */
@RestController
@RequestMapping("/multiDataSource")
public class TestController {

    @Autowired
    private FirstMapper firstMapper;

    @Autowired
    private SecondMapper secondMapper;

    @RequestMapping("/test")
    public R test() {
       TOrder tOrder = new TOrder();
       tOrder.setOrderNumber("123");
       List<Map> fist = firstMapper.listUsers();
       List<Map> second = secondMapper.listMenus();
       List<Map> orders = secondMapper.listToder(tOrder);
       fist.addAll(second);
       fist.addAll(orders);
       return R.ok(fist);
    }

    @RequestMapping("/insert")
    public R insert() {
        TOrder tOrder = new TOrder();
        tOrder.setOrderId(123L);
        tOrder.setOrderNumber("456");
        tOrder.setOtherthings("其它事项");
        int orders = secondMapper.inserTorder(tOrder);
        return R.ok(orders);
    }


}
