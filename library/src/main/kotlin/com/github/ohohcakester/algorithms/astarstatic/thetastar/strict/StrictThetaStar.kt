package com.github.ohohcakester.algorithms.astarstatic.thetastar.strict

import com.github.ohohcakester.grid.GridGraph

/**
 * An modification of Theta* that I am experimenting with. -Oh

 * @author Oh
 * *
 *
 *
 * *         Ideas:
 * *         Heuristic trap:
 * *         - The heuristic value of the final node is 1.1f instead of 0.
 * *         - A lot of inoptimality comes because the algorithm is too eager to relax
 * *         the final vertex. The slightly higher heuristic encourages the algorithm
 * *         to explore a little more first.
 */
class StrictThetaStar(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AbstractStrictThetaStar(graph, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = postSmooth(graph, sx, sy, ex, ey, ::StrictThetaStar)
		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = repeatedPostSmooth(graph, sx, sy, ex, ey, ::StrictThetaStar)
		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = dijkstra(graph, sx, sy, ex, ey, ::StrictThetaStar)
	}

	private val BUFFER_VALUE = 0.42f

	override fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		// return true iff relaxation is done.
		val par = getParent(u)
		if (lineOfSight(getParent(u), v)) {
			val newWeight = distance(par) + physicalDistance(par, v)
			return relaxTarget(v, par, newWeight)
		} else {
			val newWeight = distance(u) + physicalDistance(u, v)
			return relaxTarget(v, u, newWeight)
		}
	}

	private fun relaxTarget(v: Int, par: Int, newWeight: Float): Boolean {
		var par = par
		var newWeight = newWeight
		if (newWeight < distance(v)) {
			if (!isTaut(v, par)) {
				newWeight += BUFFER_VALUE
				par += Integer.MIN_VALUE
			}
			setDistance(v, newWeight)
			setParent(v, par)
			return true
		}
		return false
	}
}
