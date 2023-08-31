package org.example.seata.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.example.core.result.R;
import org.example.seata.domain.First;
import org.example.seata.feign.Second;
import org.example.seata.feign.SecondFeignInterface;
import org.example.seata.mapper.FirstMapper;
import org.example.seata.service.TestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class TestServiceImpl extends ServiceImpl<FirstMapper,First> implements TestService {

    @Resource
    private SecondFeignInterface secondFeignInterface;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(First first) {
        LocalDateTime localDateTime = LocalDateTime.now();
        first.setCreateTime(localDateTime);
        this.baseMapper.insert(first);
        Second second = new Second();
        second.setSecondField("第二属性");
        second.setCreateTime(localDateTime);
        R<Long> result = secondFeignInterface.add(second);
        result.checkAndGet();
    }
}
