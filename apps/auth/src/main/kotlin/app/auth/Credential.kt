package app.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

interface Encoder {
    fun encode(credential: Credential): String
}

interface Decoder {
    fun decode(value: String): Credential?
}

class Converter(private val secret: String, private val issuer: String): Encoder, Decoder {

    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm).withIssuer(issuer).build()

    override fun encode(credential: Credential): String {
        val withClaim = JWT.create()
            .withIssuer(issuer)
            .withClaim("consumer", credential.consumer)
        credential.user?.let { withClaim.withClaim("user", it) }
        return withClaim.sign(algorithm)
    }

    override fun decode(value: String): Credential? {
        return try {
            verifier.verify(value).claims
                .let { Credential(it["consumer"]!!.asString(), it["user"]?.asString()) }
        } catch (e: Exception) {
            null
        }
    }
}