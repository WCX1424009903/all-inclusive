package elasticsearch.demo.service;


import elasticsearch.demo.domain.StandardStatuteIndex;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * es索引库聚合统计查询
 *
 * @program: inner-standard
 * @description: es聚合统计服务
 **/
public interface EsStatisticsService {

    /**
     * 统计数据-多字段聚合取聚合结果
     * @return
     */
    HashMap<String, HashMap<String, Long>> statistics() throws IOException;

    /**
     * 查询最近的数据
     * @param latestVal 最近值个月数 默认1个月
     * @return
     */
    HashMap<String, HashMap<String, List<StandardStatuteIndex>>> queryLatestData(int latestVal)  throws IOException ;
}
