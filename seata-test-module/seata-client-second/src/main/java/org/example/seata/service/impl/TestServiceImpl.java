package org.example.seata.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.core.context.RootContext;
import org.example.seata.domain.Second;
import org.example.seata.mapper.SecondMapper;
import org.example.seata.service.TestService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service
public class TestServiceImpl extends ServiceImpl<SecondMapper, Second> implements TestService {


    @Override
    public Long add(Second second) {
        this.baseMapper.insert(second);
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String rpcXid = request.getHeader(RootContext.KEY_XID);
        System.out.println("xid为:"+rpcXid);
        System.out.println(1/0);
        return second.getId();
    }
}
