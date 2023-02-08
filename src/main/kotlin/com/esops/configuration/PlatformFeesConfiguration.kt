package com.esops.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ConfigurationProperties("fees")
class PlatformFeesConfiguration {
    @Min(0)
    @Max(100)
    var normal: Float = 0.0F
    @Min(0)
    @Max(100)
    var performance: Float = 0.0F
}
