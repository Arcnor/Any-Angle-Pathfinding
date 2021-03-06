package com.github.ohohcakester.algorithms.anya

import org.junit.Assert.assertEquals
import org.junit.Test

class FractionTest {
	@Test
	fun test() {
		assertEquals(Fraction.gcd(5, 3), 1)
		assertEquals(Fraction.gcd(5, 10), 5)
		assertEquals(Fraction.gcd(8, 2), 2)
		assertEquals(Fraction.gcd(80, 45), 5)
		assertEquals(Fraction.gcd(-5, 3), 1)
		assertEquals(Fraction.gcd(5, -3), 1)
		assertEquals(Fraction.gcd(5, 0), 5)
		assertEquals(Fraction.gcd(6, -4), 2)
		assertEquals(Fraction.gcd(-24, -18), 6)
		assertEquals(Fraction.gcd(0, 0), 0)
		assertEquals(Fraction.gcd(0, -2), 2)
		assertEquals(Fraction.gcd(-2, -2), 2)
		assertEquals(Fraction.gcd(-18, 35), 1)
		assertEquals(Fraction.gcd(1, 0), 1)
		assertEquals(Fraction.gcd(1, -1), 1)
		assertEquals(Fraction.gcd(-72, -13), 1)

		assertEquals(-2, Fraction(-6, 3).floor())
		assertEquals(-2, Fraction(-5, 3).floor())
		assertEquals(-2, Fraction(-4, 3).floor())
		assertEquals(-1, Fraction(-3, 3).floor())
		assertEquals(-1, Fraction(-2, 3).floor())
		assertEquals(-1, Fraction(-1, 3).floor())
		assertEquals(0, Fraction(0, 3).floor())
		assertEquals(0, Fraction(1, 3).floor())
		assertEquals(0, Fraction(2, 3).floor())
		assertEquals(1, Fraction(3, 3).floor())
		assertEquals(1, Fraction(4, 3).floor())
		assertEquals(1, Fraction(5, 3).floor())
		assertEquals(2, Fraction(6, 3).floor())

		assertEquals(-2, Fraction(-6, 3).ceil())
		assertEquals(-1, Fraction(-5, 3).ceil())
		assertEquals(-1, Fraction(-4, 3).ceil())
		assertEquals(-1, Fraction(-3, 3).ceil())
		assertEquals(0, Fraction(-2, 3).ceil())
		assertEquals(0, Fraction(-1, 3).ceil())
		assertEquals(0, Fraction(0, 3).ceil())
		assertEquals(1, Fraction(1, 3).ceil())
		assertEquals(1, Fraction(2, 3).ceil())
		assertEquals(1, Fraction(3, 3).ceil())
		assertEquals(2, Fraction(4, 3).ceil())
		assertEquals(2, Fraction(5, 3).ceil())
		assertEquals(2, Fraction(6, 3).ceil())
	}
}