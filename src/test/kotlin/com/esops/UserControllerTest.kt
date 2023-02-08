package com.esops

import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@MicronautTest
class UserControllerTest {

    @Inject
    private lateinit var embeddedServer: EmbeddedServer

    @Test
    fun testServerIsRunning() {
        assert(embeddedServer.isRunning)
    }
}
