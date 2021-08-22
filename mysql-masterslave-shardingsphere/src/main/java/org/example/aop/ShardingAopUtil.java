package org.example.aop;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * shardingaop实现，用aop拦截，将分片值作为spring-el表达式进行计算得到对应的分片键值，在按照算法得到物理表
 *
 * @author wcx
 * @date 2021/8/18 21:55
 */
@Aspect
@Component
@Slf4j
public class ShardingAopUtil {

    @Around(value = "@annotation(shardingAop)")
    public Object aroudShardingPointCut(ProceedingJoinPoint point,ShardingAop shardingAop) throws Throwable {
        log.info("aop分片注解生效");
        try (HintManager hintManager = HintManager.getInstance()) {
            // 分片值
            String sharingValue = shardingAop.value();
            if (StrUtil.isNotBlank(sharingValue)) {
                sharingValue = AspectSupportUtils.getKeyValue(point,sharingValue).toString();
                // 设置分片策略
                hintManager.addTableShardingValue("t_order", sharingValue);
            }
        }
        return point.proceed();
    }

}
