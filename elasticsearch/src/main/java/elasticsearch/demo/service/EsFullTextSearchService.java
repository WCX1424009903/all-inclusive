package elasticsearch.demo.service;

import elasticsearch.demo.domain.StandardStatutePagingRequest;

import java.io.IOException;
import java.util.HashMap;

/**
 * @program: inner-standard
 * @description: es全文检索 服务类
 **/
public interface EsFullTextSearchService {

    /**
     * ik分词和拼音分词+全文检索并高亮 分页查询显示
     * 分页采用from从0开始 size条数为3000 然后前端分页-一般用户使用最多前10页数据。
     * 暂不采用Scroll游标方式
     *
     * @param request
     * @return
     * @throws IOException
     */
    HashMap<String,Object> fullTextSearch(StandardStatutePagingRequest request) throws IOException;
}
