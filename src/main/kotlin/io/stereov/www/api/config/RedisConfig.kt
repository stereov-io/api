package io.stereov.www.api.config

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {

    @Bean
    fun statefulRedisConnection(): StatefulRedisConnection<String, ByteArray> {
        val redisUri = RedisURI.builder()
            .withHost(redisProperties.host)
            .withPort(redisProperties.port)
            .withSsl(redisProperties.ssl.isEnabled)
            .apply {
                if (!redisProperties.password.isNullOrEmpty()) {
                    withPassword(redisProperties.password?.toCharArray())
                }
                withDatabase(redisProperties.database)
            }
            .build()

        val redisClient = RedisClient.create(redisUri)
        return redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec()))
    }

    @Bean
    fun bucketProxyManager(connection: StatefulRedisConnection<String, ByteArray>): LettuceBasedProxyManager<String> {
        return Bucket4jLettuce.casBasedBuilder(connection)
            .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
            .build()
    }
}