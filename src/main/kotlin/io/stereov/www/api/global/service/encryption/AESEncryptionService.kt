package io.stereov.www.api.global.service.encryption

import io.stereov.www.api.properties.EncryptionProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Service
class AESEncryptionService(
    private val properties: EncryptionProperties,
) {

    private val logger = LoggerFactory.getLogger(AESEncryptionService::class.java)

    fun encrypt(strToEncrypt: String): String {
        logger.debug("Encrypting...")

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(properties.secretKey.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encrypted = cipher.doFinal(strToEncrypt.toByteArray())
        return Base64.getUrlEncoder().encodeToString(encrypted)
    }

    fun decrypt(strToDecrypt: String): String {
        logger.debug("Decrypting...")

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(properties.secretKey.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decrypted = cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt))
        return String(decrypted)
    }

}