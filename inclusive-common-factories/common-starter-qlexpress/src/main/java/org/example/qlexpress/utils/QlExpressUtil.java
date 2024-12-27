package org.example.qlexpress.utils;

import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import lombok.RequiredArgsConstructor;
import org.example.qlexpress.config.QLExpressContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

@RequiredArgsConstructor
public class QlExpressUtil implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final ExpressRunner expressRunner;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 表达式计算
     *
     * @param statement 执行语句
     * @param context   上下文
     * @throws Exception
     */
    public Object execute(String statement, Map<String, Object> context) {
        IExpressContext<String, Object> expressContext = new QLExpressContext(context, applicationContext);
        try {
            return expressRunner.execute(statement, expressContext, null, true, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 表达式计算
     *
     * @param statement 执行语句
     * @throws Exception
     */
    public Object execute(String statement) {
        IExpressContext<String, Object> expressContext = new QLExpressContext(applicationContext);
        try {
            return expressRunner.execute(statement, expressContext, null, true, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}