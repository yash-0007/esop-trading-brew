package  com.esops.configuration

import com.esops.validation.configuration.BreakupArrayValid
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.Min

@ConfigurationProperties("vesting")
class VestingConfiguration(@BreakupArrayValid var breakup: Array<Double> = arrayOf(0.3, 0.2, 0.1, 0.4), @field:Min(0) var duration: Int = 0) {
}
