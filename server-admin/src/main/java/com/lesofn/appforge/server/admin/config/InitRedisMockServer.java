package com.lesofn.appforge.server.admin.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 使用 Testcontainers 启动 Redis 容器，替代 jedis-mock，支持所有平台（包括 Apple Silicon Mac）。
 *
 * @author sofn
 * @version 2.0 Created at: 2025-08-25 23:33
 */
@Slf4j
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "app-forge.embedded.redis", havingValue = "true")
public class InitRedisMockServer {

    private static final int REDIS_PORT = 6379;
    private static final String REDIS_IMAGE = "redis:7-alpine";

    private GenericContainer<?> redisContainer;

    public InitRedisMockServer(DataRedisProperties redisProperties) {
        try {
            log.info("Starting Redis container via Testcontainers...");
            redisContainer =
                    new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                            .withExposedPorts(REDIS_PORT);
            redisContainer.start();

            Integer mappedPort = redisContainer.getMappedPort(REDIS_PORT);
            String host = redisContainer.getHost();
            log.info("Redis container started at {}:{}", host, mappedPort);

            // 动态覆盖 Spring Data Redis 配置
            redisProperties.setHost(host);
            redisProperties.setPort(mappedPort);
        } catch (Exception e) {
            log.error("Failed to start Redis container", e);
            throw new RuntimeException("Failed to start Redis container", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (redisContainer != null && redisContainer.isRunning()) {
            log.info("Stopping Redis container");
            redisContainer.stop();
            log.info("Redis container stopped");
        }
    }
}
