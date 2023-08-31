package org.example.elasticsearch.service.impl;

import cn.hutool.core.collection.CollUtil;
import org.example.elasticsearch.service.BaseElasticService;
import org.example.elasticsearch.service.EsIndexService;
import org.apache.commons.collections4.map.HashedMap;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: inner-standard
 * @description:
 * @author: liy
 * @create: 2020-11-24 11:22
 **/
@Service
public class EsIndexServiceImpl extends BaseElasticService implements EsIndexService {

    /**
     * 类加载初始化时 判断是否存在索引 若不存在则新建
     * 查看索引的mapping： http://localhost:9200/standard-statute/_mapping 或者kebana查看
     */
    @PostConstruct
    public void init() {
        try {
            if(isExistsIndex(INDEX_NAME)){
                return;
            }
            //指定索引里mapping中定义的类型text和date的字段
            List<String> textFields = new ArrayList<>(2);
            textFields.add("name");
            textFields.add("nameEN");
            textFields.add("fullContext");
            List<String> dateFields = new ArrayList<>(5);
            dateFields.add("permissionDate");
            dateFields.add("implementDate");
            dateFields.add("filingDate");
            dateFields.add("abolishDate");

            createIndex(INDEX_NAME, textFields, dateFields);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * 创建索引-默认
     *
     * @param indexName
     * @return
     */
    @Override
    public boolean createIndex(String indexName) {
        try {
            if (isExistsIndex(indexName)) {
                return false;
            }
            //创建索引 7.x版本
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            /*
            //索引type 可以不设置，默认就是'_doc'[7.x后 已废弃type的多类型指定，默认只有_doc类型]
            indexRequest.type(INDEX_TYPE);
            //设置索引index的 settings 分片等等 默认即可
            indexRequest.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));
            //设置索引index的 mapping 字段类型
            indexRequest.mapping(CREATE_INDEX, XContentType.JSON);
            */
            CreateIndexResponse res = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return true;
    }

    /**
     * 创建索引-指定字段类型配置(ik,pinyin分词;date类型......)
     *
     * @param indexName
     * @param textFields 定义的text类型字段集合
     * @param dateFields 定义的date类型字段集合
     * @return
     */
    @Override
    public boolean createIndex(String indexName, List<String> textFields, List<String> dateFields) {
        try{
            if(isExistsIndex(indexName)){
                return false;
            }
            //创建索引 7.x版本
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

            //-----------------------------索引 setting-----------------------------
            //自定义拼音分词器-输入拼音就能查找到相关的汉字词汇
            //使用mapping的analysis属性设置 其包含：analyzer和tokenizer
            Map<String, Map<String, Map<String, Object>>> analysisMap = new HashMap<>(2);
            //analysis-analyzer
            Map<String, Map<String, Object>> analyzerMap = new HashMap<>(1);
            Map<String, Object> pinyinAnalyzerMap = new HashMap<>(2);
            pinyinAnalyzerMap.put("tokenizer", "my_pinyin");
            pinyinAnalyzerMap.put("filter", "word_delimiter");
            analyzerMap.put("pinyin_analyzer", pinyinAnalyzerMap);
            analysisMap.put("analyzer",analyzerMap);
            //analysis-tokenizer
            Map<String, Map<String, Object>> tokenizerMap = new HashMap<>(1);
            Map<String, Object> tokenizerPinyinMap = new HashedMap(7);
            tokenizerPinyinMap.put("type", "pinyin");
            tokenizerPinyinMap.put("keep_separate_first_letter", false);
            tokenizerPinyinMap.put("keep_full_pinyin", true);
            tokenizerPinyinMap.put("keep_original", true);
            tokenizerPinyinMap.put("limit_first_letter_length", 16);
            tokenizerPinyinMap.put("lowercase", true);
            tokenizerPinyinMap.put("remove_duplicated_term", true);
            tokenizerMap.put("my_pinyin", tokenizerPinyinMap);
            analysisMap.put("tokenizer", tokenizerMap);

            //索引setting根对象定义
            Map<String, Object> settings = new HashMap<>(1);
            settings.put("analysis", analysisMap);
            createIndexRequest.settings(settings);
            //-----------------------------索引 setting  end------------------------

            //-----------------------------索引 mapping-----------------------------
            //注意：mapping生成后是不允许修改（包括删除）的。所以需要提前合理的的定义mapping数据类型
            //设置text类型字典
            Map<String, Object> textField = new HashMap<>(2);
            textField.put("type", "text");
            //内置分词器-搜索内容进行细粒度分词
            textField.put("analyzer", "ik_max_word");
            //内置搜索时的分词器-提高搜索精确性
            textField.put("search_analyzer", "ik_smart");
            //设置拼音分词 "ields":{"pinyin":{"type": "text","analyzer": "pinyin_analyzer"......}}
            Map<String, Map<String, Object>> pinyinMap = new HashMap<>(1);
            Map<String, Object> pinyingPropertiesMap = new HashMap<>(5);
            pinyingPropertiesMap.put("type", "text");
            pinyingPropertiesMap.put("store", false);
            pinyingPropertiesMap.put("term_vector", "with_offsets");
            pinyingPropertiesMap.put("analyzer", "pinyin_analyzer");
            pinyingPropertiesMap.put("boost", 10);
            pinyinMap.put("pinyin", pinyingPropertiesMap);
            textField.put("fields", pinyinMap);
            //设置date类型字段
            Map<String, Object> dateField = new HashMap<>(2);
            dateField.put("type", "date");
            //表示该日期支持 年月日时分秒 、年月日和时间戳三种格式
            dateField.put("format", "yyyy‐MM‐dd HH:mm:ss||yyyy‐MM‐dd||epoch_millis");

            //properties属性
            Map<String, Object> properties = new HashMap<>(20);
            //设置text类型字段
            if(CollUtil.isNotEmpty(textFields)){
                for(int t = 0; t<textFields.size(); t++){
                    properties.put(textFields.get(t), textField);
                }
            }
            //设置date类型字段
            if(CollUtil.isNotEmpty(dateFields)){
                for(int d = 0; d<dateFields.size(); d++){
                    properties.put(dateFields.get(d), dateField);
                }
            }

            //索引mapping定义
            Map<String, Object> mapping = new HashMap<>(1);
            mapping.put("properties", properties);
            //设置mapping参数
            createIndexRequest.mapping(mapping);
            //-----------------------------索引 mapping  end-----------------------

            //设置别名
//            indexRequest.alias(new Alias("pancm_alias"));
            //创建索引(默认分片数为5和副本数为1)
            CreateIndexResponse res = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            // 指示是否所有节点都已确认请求
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    /**
     * 判断某个index是否存在
     *
     * @param indexName
     * @return
     * @throws Exception
     */
    @Override
    public boolean isExistsIndex(String indexName) throws IOException {
        //判断集群中{indexName}是否存在
        GetIndexRequest request = new GetIndexRequest(indexName);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除索引
     *
     * @param indexName
     * @return
     */
    @Override
    public boolean deleteIndex(String indexName) {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        try {
            AcknowledgedResponse deleteResponse = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //是否所有节点都已删除该索引
            boolean acknowledged = deleteResponse.isAcknowledged();
            return acknowledged;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return false;
    }

}
