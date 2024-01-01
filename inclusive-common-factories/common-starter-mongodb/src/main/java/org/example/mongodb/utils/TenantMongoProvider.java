package org.example.mongodb.utils;

import org.springframework.stereotype.Component;

/**
 * 基于Spel表达式mongo动态集合存储
 *
 * @author wcx
 * @date 2024/1/1
 */
@Component("tenantMongoProvider")
public class TenantMongoProvider {

    private final ThreadLocal<String> holder = new ThreadLocal<>();

    public String get() {
        return holder.get();
    }

    public void set(String tenantId) {
        if (tenantId == null) {
            throw new RuntimeException("tenantId not null.");
        }
        holder.set(tenantId);
    }

    public void clear() {
        holder.remove();
    }

}
