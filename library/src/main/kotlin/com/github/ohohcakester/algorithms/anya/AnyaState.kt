package com.github.ohohcakester.algorithms.anya

import com.github.ohohcakester.datatypes.Point

internal class AnyaState private constructor(val xL: Fraction, val xR: Fraction, val y: Int, val basePoint: Point, var gValue: Float, var parent: AnyaState?) {
	companion object {
		fun createStartState(xL: Fraction, xR: Fraction, y: Int, start: Point): AnyaState {
			return AnyaState(xL, xR, y,
					start,
					0f,
					null)
		}

		fun createObservableSuccessor(xL: Fraction, xR: Fraction, y: Int, sourceInterval: AnyaState): AnyaState {
			return AnyaState(xL, xR, y,
					sourceInterval.basePoint,
					sourceInterval.gValue,
					sourceInterval.parent)
		}

		fun createUnobservableSuccessor(xL: Fraction, xR: Fraction, y: Int, basePoint: Point, sourceInterval: AnyaState): AnyaState {
			val dx = basePoint.x - sourceInterval.basePoint.x
			val dy = basePoint.y - sourceInterval.basePoint.y
			return AnyaState(xL, xR, y,
					basePoint,
					sourceInterval.gValue + Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat(),
					sourceInterval)
		}
	}

	var hValue: Float = 0.toFloat()
	val fValue: Float
		get() = gValue + hValue

	var visited: Boolean = false

	override fun hashCode(): Int {
		val prime = 31
		var result = 1
		// Removed null checks.
		result = prime * result + basePoint.hashCode()
		result = prime * result + xL.hashCode()
		result = prime * result + xR.hashCode()
		result = prime * result + y
		return result
	}

	override fun equals(other: Any?): Boolean {
		// Removed type checks. Removed null checks.
		other as AnyaState
		if (xL != other.xL) return false
		if (xR != other.xR) return false
		if (y != other.y) return false
		if (basePoint != other.basePoint) return false
		return true
	}

	override fun toString() = "($xL $xR) - $y"
}