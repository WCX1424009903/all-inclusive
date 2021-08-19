package org.example.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * 分片键策略
 * @author wcx
 * @date 2021/8/18 20:52
 */
public abstract class HintShardingAlgorithmImpl implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(shardingValue.getValue() % 2 +"")) {
                return each;
            }
        }
        throw new IllegalArgumentException("未找到匹配的数据表");
    }
}
