package org.example.elasticsearch.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import org.example.elasticsearch.domain.StandardStatuteIndex;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * es操作服务 单机版 基类
 * 默认索引[standard-statute]在bean:EsIndexService初始化时已创建好
 *
 * <span>基于es7.x版本的 HighLevelRestClient操作</span>
 * <span>7.x后 已废弃type的多类型指定，默认只有_doc类型</span>
 * <span>从7.x开始，一个Mapping只属于一个索引的type 默认type 为：_doc</span>
 * <span>注意：Request cannot be executed; I/O reactor status: STOPPED 问题；client不能自调关闭方法及影响到的IOReactor异常
 *             https://github.com/elastic/elasticsearch/issues/45115
 * </span>
 *
 **/
public class BaseElasticService {
    /**
     * 标准文档属性在es中的索引名称 必须是小写
     */
    public static final String INDEX_NAME = "standard-statute";
    /**
     * 标准文档属性在es中的索引类型；7.x后只有_doc类型
     */
    //public static final String INDEX_TYPE = "_doc";

    @Autowired
    public RestHighLevelClient restHighLevelClient;

    /**
     * 获得查询返回的searchResponse中的数据
     * @param searchResponse
     * @return
     */
    protected HashMap<String,Object> getTableDataInfo(SearchResponse searchResponse){
        HashMap<String,Object> map = new HashMap<>(2);
        if(searchResponse != null && RestStatus.OK.equals(searchResponse.status())) {
            SearchHits hits = searchResponse.getHits();
            int cnt = (int)hits.getTotalHits().value;
            List<StandardStatuteIndex> list = new ArrayList<>();
            for (SearchHit hit : hits) {
                Map<String, Object> map2 = hit.getSourceAsMap();
                list.add(BeanUtil.fillBeanWithMap(map2, new StandardStatuteIndex(), false));
            }
            map.put("cnt",cnt);
            map.put("standardStatutes", list);
        }
        return map;
    }

    /**
     * 获得高亮查询返回的searchResponse中的数据
     * @param searchResponse
     * @return
     */
    protected HashMap<String,Object> getHighlightTableDataInfo(SearchResponse searchResponse, String fieldName){
        HashMap<String,Object> map = new HashMap<>(2);
        //搜索结果状态信息
        if(searchResponse != null && RestStatus.OK.equals(searchResponse.status())) {
            SearchHits hits = searchResponse.getHits();
            int cnt = (int)hits.getTotalHits().value;
            List<StandardStatuteIndex> list = new ArrayList<>(cnt);
            StandardStatuteIndex standardStatuteIndex = null;
            for (SearchHit hit : hits) {
                Map<String, Object> map2 = hit.getSourceAsMap();
                standardStatuteIndex = BeanUtil.fillBeanWithMap(map2, new StandardStatuteIndex(), false);
                //高亮字段map
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                if(CollUtil.isNotEmpty(highlightFieldMap)){
                    HighlightField highlightField = highlightFieldMap.get(fieldName);
                    if(highlightField != null){
                        standardStatuteIndex.setFullContext(highlightField.getFragments()[0].toString());
                    }
                }
                list.add(standardStatuteIndex);
            }
            map.put("cnt",cnt);
            map.put("standardStatutes", list);
        }
        return map;
    }

}
