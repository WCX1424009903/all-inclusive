package org.example.sharding.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 分片键策略
 * @author wcx
 * @date 2021/8/18 20:52
 */
@Slf4j
@Component
public class HintShardingAlgorithmTables implements PreciseShardingAlgorithm<String> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        log.info("数据源分片集合:"+availableTargetNames);
        log.info("数据源分片值:"+shardingValue.getValue());
        for (String each : availableTargetNames) {
            String endValue = each.substring(each.lastIndexOf("_")+1);
            if (endValue.equalsIgnoreCase(String.valueOf(Long.parseLong(shardingValue.getValue())%2))) {
                return each;
            }
        }
        return "t_order";
    }
}
