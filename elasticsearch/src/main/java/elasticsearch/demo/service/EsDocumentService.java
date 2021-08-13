package elasticsearch.demo.service;

import elasticsearch.demo.domain.StandardStatuteIndex;
import java.util.List;

/**
 * 索引库index旗下的文档-每个标准法规文档属性对象的crud
 *
 * @program: inner-standard
 * @description: elasticsearch 索引文档(表)相关操作服务
 **/
public interface EsDocumentService {
    /**
     * 新增 StandardStatute to es index
     * @param model
     * @return
     */
    boolean insert(StandardStatuteIndex model);

    /**
     * 批量新增
     * @param list
     */
    public void insertBatch(List<StandardStatuteIndex> list);

    /**
     * 删除 StandardStatute to es index
     * @param standardNo
     * @return
     */
    boolean delete(String standardNo);

    /**
     * 修改 StandardStatute to es index
     * @param standardNo
     * @param updateModel
     * @return
     */
    boolean update(String standardNo, StandardStatuteIndex updateModel);

    /**
     * 查询 StandardStatute to es index by standardNo
     * @param standardNo
     * @return
     */
    StandardStatuteIndex getByStandardNo(String standardNo);

}
