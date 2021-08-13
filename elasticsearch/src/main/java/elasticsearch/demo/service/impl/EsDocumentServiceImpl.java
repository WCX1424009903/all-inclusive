package elasticsearch.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import elasticsearch.demo.domain.StandardStatuteIndex;
import elasticsearch.demo.domain.StandardStatutePagingRequest;
import elasticsearch.demo.service.BaseElasticService;
import elasticsearch.demo.service.EsDocumentService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * es操作服务 单机版
 *
 **/
@Service
public class EsDocumentServiceImpl extends BaseElasticService implements EsDocumentService {

    /**
     * 新增 StandardStatute
     *
     * @param model
     */
    @Override
    public boolean insert(StandardStatuteIndex model) {
        try{
            // 构建IndexRequest，设置索引名称，索引类型，索引id
            IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
            // 设置索引id为 标准编号
            indexRequest.id(model.getStandardNo());
            // 设置数据源
            String statuteJson = JSONObject.toJSONString(model);
            indexRequest.source(statuteJson, XContentType.JSON);
            // 操作ES
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }catch (IOException ioException){
            return false;
        }
        return true;
    }

    /**
     * 批量新增-id为标准编号
     *
     * @param list
     */
    @Override
    public void insertBatch(List<StandardStatuteIndex> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> {
            request.add(new IndexRequest(INDEX_NAME).id(item.getStandardNo())
                    .source(JSONObject.toJSONString(item), XContentType.JSON));
        });
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除 StandardStatute
     *
     * @param standardNo
     */
    @Override
    public boolean delete(String standardNo) {
        try{
            DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME);
            deleteRequest.id(standardNo);
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
            //System.out.println("delete: " + JSON.toJSONString(deleteResponse));
        }catch (IOException ioException){
            return false;
        }
        return true;
    }

    /**
     * 修改 StandardStatute
     *
     * @param standardNo
     * @param updateModel
     */
    @Override
    public boolean update(String standardNo, StandardStatuteIndex updateModel) {
        try{
            UpdateRequest request = new UpdateRequest(INDEX_NAME, standardNo);
            request.doc(JSON.toJSONString(updateModel), XContentType.JSON);
            //设置数据不存在，那么就新增一条
            request.docAsUpsert(true);
            UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
            //System.out.println("update: " + JSON.toJSONString(updateResponse));
        }catch (IOException ioException){
            return false;
        }
        return true;
    }

    /**
     * 查询by standardNo
     *
     * @param standardNo
     */
    @Override
    public StandardStatuteIndex getByStandardNo(String standardNo) {
        StandardStatuteIndex bss = null;
        try{
            GetRequest getRequest = new GetRequest(INDEX_NAME);
            getRequest.id(standardNo);
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("get: " + JSON.toJSONString(getResponse));
            Map<String, Object> map = getResponse.getSourceAsMap();
            //map to bean
            bss = BeanUtil.fillBeanWithMap(map, new StandardStatuteIndex(), false);
        }catch (IOException ioException){
            return null;
        }
        return bss;
    }

    /**
     * 条件查询--分页 to es index
     * @param request
     * @return
     * @throws IOException
     */
    public HashMap<String,Object> queryByCriteria(StandardStatutePagingRequest request) throws IOException {
        //创建search请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //用SearchSourceBuilder来构造查询请求体,构造各种查询的方法都在这
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置一个可选的超时，控制允许搜索的时间。
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //搜索条件 类似于sql中的where条件组装部分
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //------------等值搜索  MatchQueryBuilder QueryBuilder----------------
        //查询指定类别
        if(StrUtil.isNotEmpty(request.getCategory())){
            //MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("category", request.getCategory());
            //QueryBuilder 适用于单个字段查询（matchPhraseQuery是没有用分词器，matchQuery会使用分词器）
            //QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery("category", request.getCategory());
            //精确查询
            QueryBuilder queryBuilder = QueryBuilders.termQuery("category", request.getCategory());
            //must表示符合的条件
            // must 相当于 与 & = ；must not 相当于 非 ~ ！=；should 相当于 或 | or ；filter 过滤
            boolQuery.must(queryBuilder);
        }
        //查询指定状态
        if(StrUtil.isNotEmpty(request.getStatus())){
            //QueryBuilder queryBuilder2 = QueryBuilders.matchPhraseQuery("status", request.getStatus());
            //精确查询
            QueryBuilder queryBuilder2 = QueryBuilders.termQuery("status", request.getStatus());
            boolQuery.must(queryBuilder2);
        }
        //------------等值搜索 end----------------

        //------------模糊搜索  WildcardQueryBuilder ----------------
        //根据标准号或者名称 模糊匹配
        if(StrUtil.isNotEmpty(request.getQueryStr())){
            // 去 name 和 standardNo 两个字段查询，并将 name 字段权重提升 10 倍-name 中包括查找指定值的排在前面
            //boolQuery.must(QueryBuilders.multiMatchQuery(request.getQueryStr(), "standardNo","name").field("name",10));
            //模糊查询;不能用通配符, 找到相似的
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("standardNo", "*"+request.getQueryStr()+"*");
            WildcardQueryBuilder wildcardQueryBuilder2 = QueryBuilders.wildcardQuery("name", "*"+request.getQueryStr()+"*");
            //组合查询 should(相当于SQL中的or关键字) (standardNo like xx or name like xx)
            boolQuery.should(wildcardQueryBuilder).should(wildcardQueryBuilder2);
        }
        //------------模糊搜索  end----------------

        //------------范围搜索  RangeQueryBuilder----------------
        //范围搜索条件
        // RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("fields_timestamp"); //新建range条件
        // rangeQueryBuilder.gte("2019-03-21T08:24:37.873Z"); //开始时间
        // rangeQueryBuilder.lte("2019-03-21T08:24:37.873Z"); //结束时间
        //------------范围搜索----------------

        searchSourceBuilder.query(boolQuery);
        //从搜索结果中取第0条开始的xx条数据，数据量最多不要超过10000 会报错，有游标解决方案百度
        searchSourceBuilder.from(request.getPageNum()*request.getPageSize());
        searchSourceBuilder.size(request.getPageSize());

        //排序根据字段id 按照指定排列
        if(StrUtil.isEmpty(request.getOrderByColumn())){
            //没有指定字段排序 默认根据实施日期倒排
            searchSourceBuilder.sort(new FieldSortBuilder("implementDate").order(SortOrder.DESC));
        }else {
            if(StrUtil.equalsIgnoreCase("asc", request.getIsAsc())){
                searchSourceBuilder.sort(new FieldSortBuilder(request.getOrderByColumn()).order(SortOrder.ASC));
            }else {
                searchSourceBuilder.sort(new FieldSortBuilder(request.getOrderByColumn()).order(SortOrder.DESC));
            }
        }
        //将请求体加入到请求中
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("搜索语句为:"+searchRequest.source().toString());
        return getTableDataInfo(response);
    }
}
