package com.github.ohohcakester.algorithms.astarstatic.thetastar

import com.github.ohohcakester.grid.GridGraph

class RecursiveThetaStar<out P>(graph: GridGraph,
                                sx: Int, sy: Int, ex: Int, ey: Int,
                                pointConstructor: (x: Int, y: Int) -> P) : BasicThetaStar<P>(graph, sx, sy, ex, ey, pointConstructor) {
	companion object {
		private tailrec fun gcd(a: Int, b: Int): Int {
			return if (a == 0) b else gcd(b % a, a)
		}
	}

	override tailrec fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		var u = u
		// return true iff relaxation is done.

		if (lineOfSight(getParent(u), v)) {
			u = getParent(u)
			return relax(u, v, weightUV)
		} else {
			val newWeight = distance(u) + physicalDistance(u, v)
			if (newWeight < distance(v)) {
				setDistance(v, newWeight)
				setParent(v, u)
				//                setParentGranular(v, u);
				return true
			}
			return false
		}
	}

	private fun setParentGranular(v: Int, u: Int) {
		var x1 = toTwoDimX(u)
		var y1 = toTwoDimY(u)
		val x2 = toTwoDimX(v)
		val y2 = toTwoDimY(v)

		var dx = x2 - x1
		var dy = y2 - y1
		var gcd = gcd(dx, dy)
		if (gcd < 0) gcd = -gcd
		dx /= gcd
		dy /= gcd
		var par = u
		for (i in 1..gcd) {
			x1 += dx
			y1 += dy
			val curr = toOneDimIndex(x1, y1)

			setParent(curr, par)
			par = curr
		}
	}
}
