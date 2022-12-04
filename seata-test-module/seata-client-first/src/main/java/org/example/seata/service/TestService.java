package org.example.seata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.seata.domain.First;

public interface TestService extends IService<First> {

    void add(First first);
}
