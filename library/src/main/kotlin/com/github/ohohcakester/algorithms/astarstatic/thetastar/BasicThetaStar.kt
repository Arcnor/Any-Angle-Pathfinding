package com.github.ohohcakester.algorithms.astarstatic.thetastar

import com.github.ohohcakester.algorithms.astarstatic.AStarStaticMemory
import com.github.ohohcakester.grid.GridGraph

open class BasicThetaStar<out P>(graph: GridGraph,
                                 sx: Int, sy: Int, ex: Int, ey: Int,
                                 pointConstructor: (x: Int, y: Int) -> P) : AStarStaticMemory<P>(graph, sx, sy, ex, ey, pointConstructor) {
	companion object {
		fun <P> postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = postSmooth(graph, sx, sy, ex, ey, pointConstructor, ::BasicThetaStar)
		fun <P> repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = repeatedPostSmooth(graph, sx, sy, ex, ey, pointConstructor, ::BasicThetaStar)
		fun <P> dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = dijkstra(graph, sx, sy, ex, ey, pointConstructor, ::BasicThetaStar)
	}

	override fun tryRelaxNeighbour(current: Int, currentX: Int, currentY: Int, x: Int, y: Int) {
		if (!graph.isValidCoordinate(x, y))
			return

		val destination = toOneDimIndex(x, y)
		if (visited(destination))
			return
		if (getParent(current) != -1 && getParent(current) == getParent(destination))
		// OPTIMISATION: [TI]
			return  // Idea: don't bother trying to relax if parents are equal. using triangle inequality.
		if (!graph.neighbourLineOfSight(currentX, currentY, x, y))
			return

		if (relax(current, destination, 0f)) {
			// If relaxation is done.
			pq.decreaseKey(destination, distance(destination) + heuristic(x, y))
		}
	}

	override fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		var u = u
		// return true iff relaxation is done.

		if (lineOfSight(getParent(u), v)) {
			u = getParent(u)
		}

		val newWeight = distance(u) + physicalDistance(u, v)
		return when {
			newWeight < distance(v) -> {
				setDistance(v, newWeight)
				setParent(v, u)
				true
			}
			else -> false
		}
	}

}