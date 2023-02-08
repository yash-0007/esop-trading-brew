package com.esops.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ConfigurationProperties("fees")
class PlatformFeesConfiguration(@field:Min(0) @field:Max(100) var normal: Float = 0.0F, @field:Min(0) @field:Max(100) var performance: Float = 0.0F) {
}
