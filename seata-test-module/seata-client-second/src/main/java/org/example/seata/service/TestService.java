package org.example.seata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.seata.domain.Second;

public interface TestService extends IService<Second> {

    Long add(Second second);

}
