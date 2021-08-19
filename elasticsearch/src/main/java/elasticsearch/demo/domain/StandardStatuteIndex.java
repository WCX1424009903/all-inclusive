package elasticsearch.demo.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: 搜索库文档对象
 * @description: es文档索引对象性
 * @author: liy
 * @create: 2020-11-23 13:36
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class StandardStatuteIndex extends StandardStatuteDocument {

    private static final long serialVersionUID = 797054228911659755L;

    /**
     * 文档文本内容
     */
    private String fullContext;

}
