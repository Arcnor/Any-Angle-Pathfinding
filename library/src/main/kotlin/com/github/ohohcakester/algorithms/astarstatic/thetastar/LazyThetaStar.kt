package com.github.ohohcakester.algorithms.astarstatic.thetastar

import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.ReusableIndirectHeap

class LazyThetaStar<out P>(graph: GridGraph,
                           sx: Int, sy: Int, ex: Int, ey: Int,
                           pointConstructor: (x: Int, y: Int) -> P) : BasicThetaStar<P>(graph, sx, sy, ex, ey, pointConstructor) {
	companion object {
		fun <P> postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = postSmooth(graph, sx, sy, ex, ey, pointConstructor, ::LazyThetaStar)
		fun <P> repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = repeatedPostSmooth(graph, sx, sy, ex, ey, pointConstructor, ::LazyThetaStar)
		fun <P> dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = dijkstra(graph, sx, sy, ex, ey, pointConstructor, ::LazyThetaStar)
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
			val x = toTwoDimX(current)
			val y = toTwoDimY(current)

			val parentIndex = getParent(current)
			if (parentIndex != -1) {
				val parX = toTwoDimX(parentIndex)
				val parY = toTwoDimY(parentIndex)

				if (!graph.lineOfSight(x, y, parX, parY)) {
					findPath1Parent(current, x, y)
				}
			}

			if (current == finish || distance(current) == java.lang.Float.POSITIVE_INFINITY) {
				maybeSaveSearchSnapshot()
				break
			}
			setVisited(current, true)

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

	private fun findPath1Parent(current: Int, x: Int, y: Int) {
		setDistance(current, java.lang.Float.POSITIVE_INFINITY)
		for (i in -1..1) {
			for (j in -1..1) {
				if (i == 0 && j == 0) continue
				val px = x + i
				val py = y + j
				if (!graph.isValidBlock(px, py)) continue
				val index = graph.toOneDimIndex(px, py)
				if (!visited(index)) continue
				if (!graph.neighbourLineOfSight(x, y, px, py)) continue

				val gValue = distance(index) + graph.distance(x, y, px, py)
				if (gValue < distance(current)) {
					setDistance(current, gValue)
					setParent(current, index)
				}
			}
		}
	}

	override fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		var u = u
		// return true iff relaxation is done.
		if (getParent(u) != -1) {
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
