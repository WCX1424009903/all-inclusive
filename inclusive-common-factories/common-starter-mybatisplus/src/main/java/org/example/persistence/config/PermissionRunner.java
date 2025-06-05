package org.example.persistence.config;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.example.persistence.annotation.DataScope;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PermissionRunner implements ApplicationRunner {

    @Resource
    private MybatisPlusInterceptor mybatisPlusInterceptor;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<InnerInterceptor> innerInterceptors = new ArrayList<>(mybatisPlusInterceptor.getInterceptors());
        innerInterceptors.add(0,  new DataPermissionInterceptor(new InnerDataPermissionHandler()));
        mybatisPlusInterceptor.setInterceptors(innerInterceptors);
    }

    @RequiredArgsConstructor
    public static class InnerDataPermissionHandler implements MultiDataPermissionHandler {

        @Override
        public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
            try {
                Class<?> mapperClazz = Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(".")));
                String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(".") + 1);
                // 获取自身类中的所有方法，不包括继承。与访问权限无关
                Method[] methods = mapperClazz.getDeclaredMethods();
                if (methods.length == 0) {
                    return null;
                }
                Method targetMethods = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).findFirst().orElse(null);
                if (targetMethods == null) {
                    return null;
                }
                DataScope dataScopeAnnotationMethod = targetMethods.getAnnotation(DataScope.class);
                if (ObjectUtils.isEmpty(dataScopeAnnotationMethod) || !dataScopeAnnotationMethod.enabled()) {
                    return null;
                } else {
                    // 跳过join中的on条件表达式拼装
                    // 此为on中有多个and条件情况
                    if (where != null && where.getASTNode() == null) {
                        if (where instanceof AndExpression) {
                            Expression leftExpression = ((AndExpression) where).getLeftExpression();
                            if (leftExpression.getASTNode() != null) {
                                if ("RegularCondition".equals(leftExpression.getASTNode().toString())) {
                                    if ("JoinerExpression".equals(leftExpression.getASTNode().jjtGetParent().jjtGetParent().toString())) {
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                    if (where != null && where.getASTNode() != null) {
                        if ("JoinerExpression".equals(where.getASTNode().jjtGetParent().jjtGetParent().toString())) {
                            return null;
                        }
                    }
                    return buildDataScopeByAnnotation(dataScopeAnnotationMethod);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 根据注解构建express语句
         */
        private Expression buildDataScopeByAnnotation(DataScope dataScopeAnnotationMethod) {
            // 构建权限语句
            // 构造用户in表达式。
            InExpression userIdsInExpression = new InExpression();
            List<String> userIds = new ArrayList<>();
            // 权限适用范围为全部
            if (userIds == null) {
                return null;
            }
            if (userIds.isEmpty()) {
                throw new RuntimeException("当前权限适用范围内暂无用户数据筛选");
            }
            ExpressionList expressionList = new ExpressionList();
            userIds.forEach(a -> expressionList.addExpressions(new StringValue(a)));
            // 设置左边的字段表达式，右边设置值。
            userIdsInExpression.setLeftExpression(buildColumn(dataScopeAnnotationMethod.tableAlias(), dataScopeAnnotationMethod.tableField()));
            userIdsInExpression.setRightItemsList(expressionList);
            return userIdsInExpression;
        }

        /**
         * 构建Column
         *
         * @param tableAlias 表别名
         * @param columnName 字段名称
         * @return 带表别名字段
         */
        private Column buildColumn(String tableAlias, String columnName) {
            if (StringUtils.isNotEmpty(tableAlias)) {
                columnName = tableAlias + "." + columnName;
            }
            return new Column(columnName);
        }

    }

}
