package io.stereov.www.api.user.repository

import io.stereov.www.api.user.model.UserDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<UserDocument, String> {

    suspend fun existsByEmail(email: String): Boolean

    suspend fun findByEmail(email: String): UserDocument?
}
