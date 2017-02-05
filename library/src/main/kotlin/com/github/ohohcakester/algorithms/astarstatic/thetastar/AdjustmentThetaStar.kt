package com.github.ohohcakester.algorithms.astarstatic.thetastar

import com.github.ohohcakester.grid.GridGraph

/**
 * An modification of Theta* that I am experimenting with. -Oh

 * @author Oh
 */
class AdjustmentThetaStar<out P>(graph: GridGraph,
                                 sx: Int, sy: Int, ex: Int, ey: Int,
                                 pointConstructor: (x: Int, y: Int) -> P) : BasicThetaStar<P>(graph, sx, sy, ex, ey, pointConstructor) {
	companion object {
		fun <P> postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = postSmooth(graph, sx, sy, ex, ey, pointConstructor, ::AdjustmentThetaStar)
		fun <P> repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = repeatedPostSmooth(graph, sx, sy, ex, ey, pointConstructor, ::AdjustmentThetaStar)
		fun <P> dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = dijkstra(graph, sx, sy, ex, ey, pointConstructor, ::AdjustmentThetaStar)
	}

	override fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		var u = u
		// return true iff relaxation is done.
		var updated = false
		if (lineOfSight(getParent(u), v)) {
			u = getParent(u)

			val newWeight = distance(u) + physicalDistance(u, v)
			if (newWeight < distance(v)) {
				setDistance(v, newWeight)
				setParent(v, u)
				updated = true
			}
		} else {
			val newWeight = distance(u) + weightUV
			if (newWeight < distance(v)) {
				setDistance(v, newWeight)
				setParent(v, u)
				updated = true
			}
		}

		if (tryUpdateWithNeighbouringNodes(getParent(u), v)) {
			updated = true
		}
		return updated
	}

	private fun tryUpdateWithNeighbouringNodes(u: Int, v: Int): Boolean {
		if (u == -1) return false

		val ux = toTwoDimX(u)
		val uy = toTwoDimY(u)
		var updated = false

		if (tryUpdateWithNode(ux + 1, uy, v)) updated = true
		if (tryUpdateWithNode(ux - 1, uy, v)) updated = true
		if (tryUpdateWithNode(ux, uy + 1, v)) updated = true
		if (tryUpdateWithNode(ux, uy - 1, v)) updated = true

		return updated
	}

	private fun tryUpdateWithNode(ux: Int, uy: Int, v: Int): Boolean {
		val u = toOneDimIndex(ux, uy)
		val newWeight = distance(u) + physicalDistance(ux, uy, v)
		if (newWeight < distance(v)) {
			if (lineOfSight(u, v)) {
				setDistance(v, newWeight)
				setParent(v, u)
				return true
			}
		}
		return false
	}

}
