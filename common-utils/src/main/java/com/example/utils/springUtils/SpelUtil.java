package com.example.utils.springUtils;


import lombok.experimental.UtilityClass;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
* spel表达式解析工具类
*@author wcx
*@date 2022/6/5 20:40
*/
@UtilityClass
public class SpelUtil {

    private static final LocalVariableTableParameterNameDiscoverer  localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private static final ExpressionParser expressionParser = new SpelExpressionParser();
    private static final StandardEvaluationContext context = new StandardEvaluationContext();

   public <T> T parseSpel(String spel, Method method,Object[] args,Class<T> tClass) {
       // 获取被拦截方法参数名列表(使用spring支持类库)
       String[] paraNameArr = localVariableTableParameterNameDiscoverer.getParameterNames(method);
       // 把方法参数放入spel上下文中
       for (int i=0;i<paraNameArr.length;i++) {
           context.setVariable(paraNameArr[i],args[i]);
       }
       return expressionParser.parseExpression(spel).getValue(context,tClass);
   }


}
