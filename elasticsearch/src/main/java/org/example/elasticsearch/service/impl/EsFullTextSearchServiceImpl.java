package org.example.elasticsearch.service.impl;

import cn.hutool.core.util.StrUtil;
import org.example.elasticsearch.domain.StandardStatutePagingRequest;
import org.example.elasticsearch.service.BaseElasticService;
import org.example.elasticsearch.service.EsFullTextSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @program: inner-standard
 * @description:
 **/
@Service
public class EsFullTextSearchServiceImpl extends BaseElasticService implements EsFullTextSearchService {

    /**
     * 全文搜索字段
     */
    public static final String FULL_TEXT_SEARCH_FIELD = "fullContext";

    /**
     * 验证是否只包含字母
     *
     * @return 验证成功返回true，验证失败返回false
     */
    private boolean checkLetter(String param) {
        String regex = "^[A-Za-z]+$";
        return Pattern.matches(regex, param);
    }

    /**
     * 验证是否中文
     *
     * @param param 中文字符
     * @return 验证成功返回true，验证失败返回false
     */
    private boolean checkChinese(String param) {
        String regex = "^[\u4E00-\u9FA5]+$";
        return Pattern.matches(regex, param);
    }

    /**
     * 根据组装好的queryBuilder和highlightBuilder以及入参request
     * 填充SearchRequest并执行查询出
     *
     * @param queryBuilder
     * @param highlightBuilder
     * @param request
     * @return
     * @throws IOException
     */
    private SearchResponse exeSearch(QueryBuilder queryBuilder,
                                       HighlightBuilder highlightBuilder,
                                       StandardStatutePagingRequest request) throws IOException {
        if(queryBuilder == null){
            throw new RuntimeException("es QueryBuilder param is not null !!!");
        }
        // 1、创建search请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        // 2.用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置一个可选的超时，控制允许搜索的时间。
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 3.添加组装的queryBuilder 入参对象
        searchSourceBuilder.query(queryBuilder);

        // 4.添加高亮
        if(highlightBuilder != null){
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 5.从搜索结果中取第0条开始的xx条数据，数据量最多不要超过10000 会报错，有游标解决方案百度
        searchSourceBuilder.from(request.getPageNum()*request.getPageSize());
        searchSourceBuilder.size(request.getPageSize());
        // 6.根据排序字段按照指定排列
        if(StrUtil.isNotEmpty(request.getOrderByColumn())){
            if(StrUtil.equalsIgnoreCase("asc", request.getIsAsc())){
                searchSourceBuilder.sort(new FieldSortBuilder(request.getOrderByColumn()).order(SortOrder.ASC));
            }else {
                searchSourceBuilder.sort(new FieldSortBuilder(request.getOrderByColumn()).order(SortOrder.DESC));
            }
        }
        // 7.将请求体加入到请求中
        searchRequest.source(searchSourceBuilder);
        // 8.发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("搜索语句为:"+searchRequest.source().toString());
        return  searchResponse;
    }

    /**
     * match query
     * ik分词和拼音分词+全文检索并高亮 分页查询显示
     * 分页采用from从0开始 size条数为3000 然后前端分页-一般用户使用最多前10页数据。
     * 暂不采用Scroll游标方式
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    public HashMap<String,Object> fullTextSearch(StandardStatutePagingRequest request) throws IOException {
        //默认根据实施日期倒排
        if(StrUtil.isEmpty(request.getOrderByColumn())){
            request.setOrderByColumn("implementDate");
            request.setIsAsc("desc");
        }
        //size条数为3000 然后前端分页-一般用户使用最多前10页数据。数据量最多不要超过10000 会报错；否则采用scroll游标方式
        request.setPageNum(0);
        request.setPageSize(3000);

        SearchResponse searchResponse = null;
        if(StrUtil.isEmpty(request.getQueryStr())){
            //查询所有
            searchResponse = exeSearch(QueryBuilders.matchAllQuery(), null, request);
        }else {
            /**
             * matchQuery 会分词再匹配
             */
            QueryBuilder matchQueryBuilder = null;
            // 高亮设置
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            HighlightBuilder.Field highlightName = new HighlightBuilder.Field(FULL_TEXT_SEARCH_FIELD);
            //高亮拼接的前缀
            highlightName.preTags("<font color=\"red\">");
            //高亮拼接的后缀
            highlightName.postTags("</font>");
            highlightName.fragmentSize(100);
            //设置高亮字段
            highlightBuilder.field(highlightName);

            //如果只有字母则认为是拼音-做拼音分词匹配
            if(checkLetter(request.getQueryStr())){
                matchQueryBuilder = QueryBuilders.matchQuery(FULL_TEXT_SEARCH_FIELD, request.getQueryStr()).analyzer("pinyin_analyzer");
                searchResponse = exeSearch(matchQueryBuilder, highlightBuilder, request);
                if(RestStatus.OK.equals(searchResponse.status())
                    &&searchResponse.getHits().getTotalHits().value<=0){
                    searchResponse = null;
                }
            }
            //如果不是拼音分词匹配或者拼音分词匹配不到 再做一次ik_smart分词匹配
            if(searchResponse == null){
                matchQueryBuilder = QueryBuilders.matchQuery(FULL_TEXT_SEARCH_FIELD, request.getQueryStr()).analyzer("ik_smart");
                searchResponse = exeSearch(matchQueryBuilder, highlightBuilder, request);
                if(RestStatus.OK.equals(searchResponse.status())
                        &&searchResponse.getHits().getTotalHits().value<=0){
                    searchResponse = null;
                }
            }
            //如果前面都没匹配到数据 最后再做一次ik_max_word分词匹配
            if(searchResponse == null){
                matchQueryBuilder = QueryBuilders.matchQuery(FULL_TEXT_SEARCH_FIELD, request.getQueryStr()).analyzer("ik_max_word");
                searchResponse = exeSearch(matchQueryBuilder, highlightBuilder, request);
            }
        }
        return getHighlightTableDataInfo(searchResponse, FULL_TEXT_SEARCH_FIELD);
    }
}
