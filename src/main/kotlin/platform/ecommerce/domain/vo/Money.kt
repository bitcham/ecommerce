package platform.ecommerce.domain.vo

import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Embeddable
data class Money(
    val amount: BigDecimal
) {
    init {
        require(amount >= BigDecimal.ZERO) {
            "Amount must be greater than or equal to zero."
        }
    }

    companion object {
        val ZERO = Money(BigDecimal.ZERO)

        fun of(amount: Double): Money {
            return Money(BigDecimal(amount))
        }
    }

    fun plus(other: Money): Money {
        return Money(this.amount + other.amount)
    }

    fun minus(other: Money): Money {
        val result = this.amount - other.amount
        require(result >= BigDecimal.ZERO) {
            "Resulting amount must be greater than or equal to zero."
        }
        return Money(result)
    }

    fun times(factor: Int): Money {
        require(factor >= 0) {
            "Factor must be greater than or equal to zero."
        }
        return Money(this.amount * BigDecimal.valueOf(factor.toLong()))
    }
}
