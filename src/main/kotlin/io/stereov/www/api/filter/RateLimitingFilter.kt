package io.stereov.www.api.filter

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.stereov.www.api.properties.BackendProperties
import kotlinx.coroutines.reactor.mono
import io.stereov.www.api.auth.service.AuthenticationService
import io.stereov.www.api.global.service.exception.StereovIoException
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration

@Component
@Order(2)
class RateLimitingFilter(
    private val authenticationService: AuthenticationService,
    private val proxyManager: LettuceBasedProxyManager<String>,
    private val backendProperties: BackendProperties,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {

        return Mono.defer {
            val clientIp = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"

            val ipBucket = resolveBucket("rate_limit_ip:$clientIp", backendProperties.ipRateLimitMinute, 1)

            ipBucket
                .flatMap { checkBucket(it) }
                .then(
                    mono { authenticationService.getCurrentAccountId() }
                        .onErrorResume { Mono.empty() }
                        .flatMap { accountId ->
                            val userBucket = resolveBucket("rate_limit_user:$accountId", backendProperties.accountRateLimitMinute, 1)
                            userBucket.flatMap { checkBucket(it) }
                        }
                )
                .then(chain.filter(exchange))
                .onErrorResume { e ->
                    if (e is TooManyRequestsException) {
                        exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                        exchange.response.setComplete()
                    } else {
                        Mono.error(e)
                    }
                }
        }
    }

    private fun resolveBucket(key: String, capacity: Long, periodMinutes: Long = 1): Mono<Bucket> {
        val configuration = BucketConfiguration.builder()
            .addLimit { limit -> limit.capacity(capacity).refillGreedy(capacity, Duration.ofMinutes(periodMinutes))}
            .build()

        return Mono.defer {
            val bucket = proxyManager.getProxy(key) { configuration }
            Mono.just(bucket)
        }
    }

    private fun checkBucket(bucket: Bucket): Mono<Void> {
        return Mono.defer {
            if (bucket.tryConsume(1)) {
                Mono.empty()
            } else {
                Mono.error(TooManyRequestsException("Rate limit exceeded"))
            }
        }
    }

    class TooManyRequestsException(message: String, cause: Throwable? = null) : StereovIoException(message, cause)
}