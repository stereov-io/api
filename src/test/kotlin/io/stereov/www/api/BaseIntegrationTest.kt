package io.stereov.www.api

import io.stereov.www.api.config.Constants
import io.stereov.www.api.global.service.jwt.JwtService
import io.stereov.www.api.user.dto.DeviceInfoRequestDto
import io.stereov.www.api.user.dto.RegisterUserDto
import io.stereov.www.api.user.model.UserDocument
import io.stereov.www.api.user.service.UserService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BaseIntegrationTest {


    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @AfterEach
    fun clearDatabase() = runBlocking {
        userService.deleteAll()
    }

    companion object {
        private val mongoDBContainer = MongoDBContainer("mongo:latest").apply {
            start()
        }

        private val redisContainer = GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .apply {
                start()
            }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { "${mongoDBContainer.connectionString}/test" }
            registry.add("jwt.expires-in") { 900 }
            registry.add("jwt.secret-key") { "64f09a172d31b6253d0af2e7dccce6bc9e4e55f8043df07c3ebda72c262758662c2c076e9f11965f43959186b9903fa122da44699b38e40ec21b4bd2fc0ad8c93be946d3dcd0208a1a3ae9d39d4482674d56f6e6dddfe8a6321ad31a824b26e3d528943b0943ad3560d23a79da1fefde0ee2a20709437cedee9def79d5b4c0cf96ee36c02b67ab5fd28638606a5c19ffe8b76d40077549f6db6920a97da0089f5cd2d28665e1d4fb6d9a68fe7b78516a8fc8c33d6a6dac53a77ab670e3449cb237a49104478b717e20e1d22e270f7cf06f9b412b55255c150cb079365eadaddd319385d6221e4b40ed89cdcde540538ce88e66ae2f783c3c48859a14ec6eff83" }
            registry.add("encryption.secret-key") { "3eJAiq7XBjMc5AXkCwsjbA==" }
            registry.add("mail.enable-email-verification") { false }
            registry.add("mail.host") { "host.com" }
            registry.add("mail.port") { "587" }
            registry.add("mail.email") { "mail@host.com" }
            registry.add("mail.username") { "mail@host.com" }
            registry.add("mail.password") { "mailpassword"}
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
            registry.add("spring.data.redis.password") { "" }
        }
    }

    open class TestRegisterResponse(
        open val info: UserDocument,
        open val accessToken: String,
        open val refreshToken: String,
    )

    suspend fun registerUser(
        email: String = "test@email.com",
        password: String = "password",
        deviceId: String = "device",
        name: String = "Test",
    ): TestRegisterResponse {
        val device = DeviceInfoRequestDto(id = deviceId)

        val responseCookies = webTestClient.post()
            .uri("/user/register")
            .bodyValue(RegisterUserDto(email, password, name, device, null))
            .exchange()
            .expectStatus().isOk
            .returnResult<Void>()
            .responseCookies

        val user = userService.findByEmailOrNull(email)
        val accessToken = responseCookies[Constants.ACCESS_TOKEN_COOKIE]?.firstOrNull()?.value
        val refreshToken = responseCookies[Constants.REFRESH_TOKEN_COOKIE]?.firstOrNull()?.value

        requireNotNull(user) { "User associated to $email not saved" }
        requireNotNull(accessToken) { "No access token contained in response" }
        requireNotNull(refreshToken) { "No refresh token contained in response" }

        return TestRegisterResponse(user, accessToken, refreshToken)
    }

    suspend fun deleteUser(response: TestRegisterResponse) {
        webTestClient.delete()
            .uri("/account/me")
            .header(HttpHeaders.COOKIE, "${Constants.ACCESS_TOKEN_COOKIE}=${response.accessToken}")
            .exchange()
            .expectStatus().isOk
    }
}