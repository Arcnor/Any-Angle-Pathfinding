package com.github.ohohcakester.algorithms.astarstatic.thetastar.strict

import com.github.ohohcakester.algorithms.astarstatic.thetastar.BasicThetaStar
import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.ReusableIndirectHeap

abstract class AbstractStrictThetaStar(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : BasicThetaStar(graph, sx, sy, ex, ey) {
	override fun heuristic(x: Int, y: Int): Float {
		return heuristicWeight * graph.distance(x, y, ex, ey)

		// MOD 2 :: Increased Goal Heuristic - Not needed when a Penalty value of 0.42 is used.
		/*if (x == ex && y == ey) {
            return 0.18f; // 0.18f
        } else {
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }*/
	}

	override fun computePath() {
		val totalSize = (graph.sizeX + 1) * (graph.sizeY + 1)

		val start = toOneDimIndex(sx, sy)
		finish = toOneDimIndex(ex, ey)

		pq = ReusableIndirectHeap(totalSize)
		this.initialiseMemory(totalSize, java.lang.Float.POSITIVE_INFINITY, -1, false)

		initialise(start)

		while (!pq.isEmpty) {
			val current = pq.popMinIndex()
			tryFixBufferValue(current)

			if (current == finish || distance(current) == Float.POSITIVE_INFINITY) {
				maybeSaveSearchSnapshot()
				break
			}
			setVisited(current, true)

			val x = toTwoDimX(current)
			val y = toTwoDimY(current)


			tryRelaxNeighbour(current, x, y, x - 1, y - 1)
			tryRelaxNeighbour(current, x, y, x, y - 1)
			tryRelaxNeighbour(current, x, y, x + 1, y - 1)

			tryRelaxNeighbour(current, x, y, x - 1, y)
			tryRelaxNeighbour(current, x, y, x + 1, y)

			tryRelaxNeighbour(current, x, y, x - 1, y + 1)
			tryRelaxNeighbour(current, x, y, x, y + 1)
			tryRelaxNeighbour(current, x, y, x + 1, y + 1)

			maybeSaveSearchSnapshot()
		}

		maybePostSmooth()
	}

	private fun tryFixBufferValue(current: Int) {
		if (getParent(current) < 0 && getParent(current) != -1) {
			setParent(current, getParent(current) - Integer.MIN_VALUE)
			setDistance(current, distance(getParent(current)) + physicalDistance(current, getParent(current)))
		}
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

		if (relax(current, destination, weight(currentX, currentY, x, y))) {
			// If relaxation is done.
			pq.decreaseKey(destination, distance(destination) + heuristic(x, y))
		}
	}

	/**
	 * Checks whether the path v, u, p=getParent(u) is taut.
	 */
	protected fun isTaut(v: Int, u: Int): Boolean {
		val p = getParent(u) // assert u != -1
		if (p == -1) return true
		val x1 = toTwoDimX(v)
		val y1 = toTwoDimY(v)
		val x2 = toTwoDimX(u)
		val y2 = toTwoDimY(u)
		val x3 = toTwoDimX(p)
		val y3 = toTwoDimY(p)
		return graph.isTaut(x1, y1, x2, y2, x3, y3)
	}
}