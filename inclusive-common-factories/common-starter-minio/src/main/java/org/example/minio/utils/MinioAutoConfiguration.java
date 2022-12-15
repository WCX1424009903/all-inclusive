package org.example.minio.utils;

import io.minio.MinioClient;
import org.example.minio.config.MinioProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.example.minio.config.StarterMinioConstants.MINIO_CLIENT;
import static org.example.minio.config.StarterMinioConstants.MINIO_PREFIX;

/**
*minio自动装配
* @author wcx
* @date 2022/12/14
*/
@Configuration
@ConditionalOnProperty(prefix = MINIO_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MinioProperty.class)
public class MinioAutoConfiguration {

    @Bean(MINIO_CLIENT)
    @ConditionalOnMissingBean(MinioClient.class)
    public MinioClient minioClient(MinioProperty minioProperty) {
        MinioClient minioClient =  MinioClient.builder().endpoint(minioProperty.getUrl())
                .credentials(minioProperty.getAccessKey(),minioProperty.getSecretKey()).build();
        MinioUtils.minioClient = minioClient;
        return minioClient;
    }

}
