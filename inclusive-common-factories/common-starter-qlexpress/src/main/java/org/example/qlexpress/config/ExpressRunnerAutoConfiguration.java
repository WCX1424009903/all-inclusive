package org.example.qlexpress.config;

import com.ql.util.express.ExpressRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

public class ExpressRunnerAutoConfiguration {

    private final List<ExpressRunnerCustomizer> configurationCustomizers;

    public ExpressRunnerAutoConfiguration(ObjectProvider<List<ExpressRunnerCustomizer>> configurationCustomizersProvider) {
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }


    @Bean
    @ConditionalOnMissingBean
    public ExpressRunner expressRunner() throws Exception {
        ExpressRunnerFactoryBean exprRunnerFactoryBean = new ExpressRunnerFactoryBean();
        exprRunnerFactoryBean.setConfigurationCustomizers(configurationCustomizers);
        return exprRunnerFactoryBean.getObject();
    }


}
