package elasticsearch.demo.domain;

import lombok.*;

import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StandardStatuteDocument {

    private static final long serialVersionUID = -4044264250158980764L;

    /** 名称 */
    private String name;

    /** 类别 */
    private String category;

    /**
     * 编号
     */
    private String standardNo;

    /**
     * 国家地区
     */
    private String region;

    /**
     * 关键词
     */
    private String keywords;
    /**
     * 状态
     */
    private String status;
    /**
     * 认证
     */
    private String authentication;

    /**
     * 英文名称
     */
    private String nameEN;
    /**
     * 单位
     */
    private String draftOrg;
    /**
     * 发布日期
     */
    private Date permissionDate;
    /**
     * 责任单位
     */
    private String dutyOrg;
    /**
     * 内容摘要
     */
    private String content;
    /**
     * 所属专业领域
     */
    private String domainProperty;
    /**
     * 标准性质
     */
    private String feature;
    /**
     * 适用车型
     */
    private String vehicleType;
    /**
     * 能源种类
     */
    private String impetusType;
    /**
     * 起草人
     */
    private String draftsman;
    /**
     * 实施日期
     */
    private Date implementDate;
    /**
     * 备案日期
     */
//    @NotNull(message = "备案日期不能为空")
    private Date filingDate;

    /** 废止日期 */
    private Date abolishDate;

}
