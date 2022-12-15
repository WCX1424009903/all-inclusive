package org.example.minio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.example.minio.config.StarterMinioConstants.MINIO_PREFIX;

/**
*minio属性配置
* @author wcx
* @date 2022/12/14
*/
@ConfigurationProperties(prefix = MINIO_PREFIX)
public class MinioProperty {
    /**
    * 是否开启
    */
    private boolean enabled = true;
    /**
    *对象存储服务的URL
    */
    private String url;
    /**
    *accessKey
    */
    private String accessKey;
    /**
    *secretKey
    */
    private String secretKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
