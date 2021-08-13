package elasticsearch.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.utils.dateUtils.DateUtils;
import elasticsearch.demo.domain.StandardStatuteIndex;
import elasticsearch.demo.service.BaseElasticService;
import elasticsearch.demo.service.EsStatisticsService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * es库聚合查询可能存在误差情况[分词等等原因，做聚合group的字段最好是数字类型]
 **/
@Service
public class EsStatisticsServiceImpl extends BaseElasticService implements EsStatisticsService {

    /**
     * 根据法规类型和法规状态 聚合统计条数
     *
     * @return
     */
    @Override
    public HashMap<String, HashMap<String, Long>> statistics() throws IOException {
        //创建search请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //初始化条件构造器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //聚合统计不需要返回参与查询的文档
        sourceBuilder.size(0);
        // 定义聚合查询  按类型和状态 分组统计总条数
        //字段的mappting类型为text不能做聚合group 这里设置keyword才行 参考:https://blog.csdn.net/qq_39390545/article/details/102895666
        //分组统计category数据
        TermsAggregationBuilder categoryBuilder = AggregationBuilders.terms("group_category").field("category.keyword");
        //根据category的聚合分组统计status数据
        TermsAggregationBuilder categoryStatusBuilder = AggregationBuilders.terms("group_category_status").field("status.keyword");

        sourceBuilder.aggregation(categoryBuilder.subAggregation(categoryStatusBuilder));
        //es7.x版本 默认查询获得最高10000的总数 这里设置获取准确的结果数
        sourceBuilder.trackTotalHits(true);
        searchRequest.source(sourceBuilder);
        // 发起请求并获取返回值
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("搜索语句为:"+searchRequest.source().toString());
        System.out.println("搜索结果为:"+JSONObject.toJSONString(response));
        //得到聚合
        Aggregations aggregations = response.getAggregations();
        System.out.println("聚合结果为:"+JSONObject.toJSONString(aggregations));

        //分组category的 map记录
        HashMap<String, HashMap<String, Long>> categoryMap = new HashMap<>(3);

        //得到设置的group_category分组
        Terms  categoryTerms = aggregations.get("group_category");
        //得到这个聚合的List对象
        List<? extends Terms.Bucket> buckets = categoryTerms.getBuckets();
        //遍历这个List对象 取docCount
        for (Terms.Bucket categoryBucket : buckets) {
            long categoryDocCount = categoryBucket.getDocCount();
            String categoryKey = categoryBucket.getKeyAsString();
//            System.out.println("category  key======="+categoryKey+"     doc_count===:"+categoryDocCount);

            //分组category_status的count map记录
            HashMap<String, Long> statusCountMap = new HashMap<>(3);

            Terms categoryStatusTerms = categoryBucket.getAggregations().get("group_category_status");
            for(Terms.Bucket categoryStatusBucket : categoryStatusTerms.getBuckets()){
                Long categoryStatusDocCount = categoryStatusBucket.getDocCount();
                String categoryStatusKey = categoryStatusBucket.getKeyAsString();
//                System.out.println("category_status  key======="+categoryStatusKey+"     doc_count===:"+categoryStatusDocCount);

                statusCountMap.put(categoryStatusKey, categoryStatusDocCount);
            }
            categoryMap.put(categoryKey, statusCountMap);
        }
        return categoryMap;
    }


    @Override
    public HashMap<String, HashMap<String, List<StandardStatuteIndex>>> queryLatestData(int latestVal) throws IOException {
        //查询类型数据
//        List<SysDictData> dictList = dictDataService.selectDictDataByType("sys_standard_type");
        HashMap<String, HashMap<String, List<StandardStatuteIndex>>> hashMap = new HashMap<>();
//        for(SysDictData sysDictData : dictList){
//            hashMap.put(sysDictData.getDictValue(), queryLatestData(sysDictData.getDictValue(), latestVal));
//        }
        return hashMap;
    }

    /**
     * 根据类型和修改时间范围 取数据
     * @param category 类型
     * @param latestVal 时间范围
     * @return
     */
    private HashMap<String, List<StandardStatuteIndex>> queryLatestData(String category, int latestVal)  throws IOException {
        if(latestVal == 0){
            latestVal = -1;
        }else if(latestVal >= 1){
            latestVal = -latestVal;
        }
        //创建search请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //初始化条件构造器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //精确查询
        QueryBuilder queryBuilder = QueryBuilders.termQuery("category", category);
        boolQuery.must(queryBuilder);

        //一个月的时间区间
        long curDate = System.currentTimeMillis();
        long lateDate = DateUtils.subMonth(new Date(),latestVal).getTime();

        //时间范围的设定 updateTime的mapping 字段为long类型
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("updateTime").from(lateDate).to(curDate);
        boolQuery.must(rangeQueryBuilder);
        //查询条件
        sourceBuilder.query(boolQuery);
        //es7.x版本 默认查询获得最高10000的总数 这里设置获取准确的结果数
        sourceBuilder.trackTotalHits(true);

        searchRequest.source(sourceBuilder);
        // 发起请求并获取返回值
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("搜索语句为:"+searchRequest.source().toString());
        System.out.println("搜索结果为:"+JSONObject.toJSONString(response));

        HashMap<String, List<StandardStatuteIndex>> map = new HashMap<>(100);
        //搜索结果状态信息
        if(response != null && RestStatus.OK.equals(response.status())) {
            SearchHits hits = response.getHits();
            int cnt = (int)hits.getTotalHits().value;
            List<StandardStatuteIndex> list = new ArrayList<>(cnt);
            StandardStatuteIndex standardStatuteIndex = null;
            for (SearchHit hit : hits) {
                standardStatuteIndex = BeanUtil.fillBeanWithMap(hit.getSourceAsMap(), new StandardStatuteIndex(), false);
                if(map.containsKey(standardStatuteIndex.getStatus())){
                    list = map.get(standardStatuteIndex.getStatus());
                }else {
                    list = new ArrayList<>(list.size());
                }
                list.add(standardStatuteIndex);
                map.put(standardStatuteIndex.getStatus(), list);
            }
        }
        return map;
    }
}
