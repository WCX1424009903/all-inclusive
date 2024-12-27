package org.example.qlexpress.config;


import com.ql.util.express.ExpressRunner;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

public class ExpressRunnerFactoryBean implements FactoryBean<ExpressRunner>, InitializingBean {

    private ExpressRunner expressRunner;

    @Setter
    private List<ExpressRunnerCustomizer> configurationCustomizers;

    @Override
    public ExpressRunner getObject() throws Exception {
        if (this.expressRunner == null) {
            afterPropertiesSet();
        }
        return this.expressRunner;
    }

    @Override
    public Class<?> getObjectType() {
        return this.expressRunner == null ? ExpressRunner.class : this.expressRunner.getClass();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.expressRunner = buildExpressRunner();
    }

    protected ExpressRunner buildExpressRunner() throws Exception {
        ExpressRunner runner = new ExpressRunner(true, false);
        // 注入自定义内容
        if (configurationCustomizers != null) {
            for (ExpressRunnerCustomizer customizer : configurationCustomizers) {
                customizer.customize(runner);
            }
        }
        return runner;
    }

}
