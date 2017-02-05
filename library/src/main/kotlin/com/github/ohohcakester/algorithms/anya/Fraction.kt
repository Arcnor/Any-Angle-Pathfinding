package com.github.ohohcakester.algorithms.anya


class Fraction : Comparable<Fraction> {

	companion object {

		@JvmStatic
		fun gcd(x: Int, y: Int): Int {
			val result = gcdRecurse(x, y)
			return if (result < 0) -result else result
		}

		private fun gcdRecurse(x: Int, y: Int): Int {
			return if (x == 0) y else gcdRecurse(y % x, x)
		}

		@JvmStatic
		fun length(width: Fraction, height: Int): Float {
			val fWidth = width.toFloat()
			return Math.sqrt((fWidth * fWidth + height * height).toDouble()).toFloat()
		}
	}

	val n: Int // numerator. Can be negative
	val d: Int // denominator. Cannot be negative.

	/**
	 * @param n Integer.
	 */
	constructor(n: Int) {
		this.n = n
		this.d = 1
	}

	constructor(n: Int, d: Int) {
		var n = n
		var d = d
		if (d == 0) throw ArithmeticException("Invalid denominator")

		if (d < 0) {
			n = -n
			d = -d
		}
		val gcd = gcd(n, d)

		this.n = n / gcd
		this.d = d / gcd
	}

	val isWholeNumber: Boolean
		get() {
			assert(gcd(n, d) == 1)
			return d == 1
		}

	fun isLessThanOrEqual(o: Fraction): Boolean {
		return this <= o
	}

	fun isLessThan(o: Fraction): Boolean {
		return this < o
	}

	fun isLessThanOrEqual(x: Int): Boolean {
		return n <= d * x
	}

	fun isLessThan(x: Int): Boolean {
		return n < d * x
	}

	fun multiplyDivide(multiply: Int, divide: Int): Fraction {
		return Fraction(n * multiply, d * divide)
	}

	fun multiply(o: Fraction): Fraction {
		return Fraction(n * o.n, d * o.d)
	}

	fun divide(o: Fraction): Fraction {
		return Fraction(n * o.d, d * o.n)
	}

	operator fun minus(o: Fraction): Fraction {
		return Fraction(n * o.d - o.n * d, d * o.d)
	}

	operator fun plus(o: Fraction): Fraction {
		return Fraction(n * o.d + o.n * d, d * o.d)
	}

	operator fun minus(value: Int): Fraction {
		return Fraction(n - value * d, d)
	}

	operator fun plus(value: Int): Fraction {
		return Fraction(n + value * d, d)
	}

	/**
	 * @return largest integer leq to this.
	 */
	fun floor(): Int {
		if (d == 1) return n
		if (n > 0) {
			return n / d
		} else {
			return (n + 1) / d - 1
		}
	}

	/**
	 * @return smallest integer geq to this.
	 */
	fun ceil(): Int {
		if (d == 1) return n
		if (n > 0) {
			return (n - 1) / d + 1
		} else {
			return n / d
		}
	}

	fun toFloat(): Float {
		return n.toFloat() / d
	}

	override fun compareTo(o: Fraction): Int {
		// this - that.
		// n1/d1 < n2/d2 iff n1d2 < n2d1 as d1,d2 are positive.
		return n * o.d - o.n * d // n1d2 - n2d1
	}

	override fun hashCode(): Int {
		val prime = 31
		var result = 1
		result = prime * result + 60 * n / d
		return result
	}

	override fun equals(obj: Any?): Boolean {
		if (obj !is Fraction) return false
		val o = obj
		return n * o.d == o.n * d
	}

	override fun toString(): String {
		return n.toString() + "/" + d
	}
}
