package org.example.qlexpress.config;

import com.ql.util.express.ExpressRunner;

@FunctionalInterface
public interface ExpressRunnerCustomizer {

    void customize(ExpressRunner expressRunner);

}
