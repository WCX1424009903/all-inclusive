package elasticsearch.demo.service;

import java.io.IOException;
import java.util.List;

/**
 * 索引操作类
 *
 * @program: inner-standard
 * @description: es索引操作
 **/
public interface EsIndexService {

    /**
     * 创建索引-默认
     * @param indexName
     * @return
     */
    boolean createIndex(String indexName);

    /**
     * 创建索引-指定字段类型配置(ik,pinyin分词;date类型......)
     *
     * @param indexName
     * @param textFields 定义的text类型字段集合
     * @param dateFields 定义的date类型字段集合
     * @return
     */
    boolean createIndex(String indexName, List<String> textFields, List<String> dateFields);

    /**
     * 删除索引
     * @param indexName
     * @return
     */
    boolean deleteIndex(String indexName);

    /**
     * 判断某个index是否存在
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    boolean isExistsIndex(String indexName) throws IOException;

}


