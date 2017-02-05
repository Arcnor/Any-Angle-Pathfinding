package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.Memory
import com.github.ohohcakester.grid.GridGraph

abstract class BaseAStar<out P>(graph: GridGraph, sizeX: Int, sizeY: Int,
                            sx: Int, sy: Int, ex: Int, ey: Int,
                            pointConstructor: (x: Int, y: Int) -> P) : PathFindingAlgorithm<P>(graph, sizeX, sizeY, sx, sy, ex, ey, pointConstructor) {
	protected companion object {
		@JvmStatic
		fun <P, T : BaseAStar<P>> postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P,
		                                     constructor: (graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) -> T): T {
			val r = constructor(graph, sx, sy, ex, ey, pointConstructor)
			r.postSmoothingOn = true
			r.repeatedPostSmooth = false
			return r
		}

		@JvmStatic
		fun <P, T : BaseAStar<P>> repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P,
		                                       constructor: (graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) -> T): T {
			val r = constructor(graph, sx, sy, ex, ey, pointConstructor)
			r.postSmoothingOn = true
			r.repeatedPostSmooth = true
			return r
		}

		@JvmStatic
		fun <P, T : BaseAStar<P>> dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P,
		                             constructor: (graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) -> T): T {
			val r = constructor(graph, sx, sy, ex, ey, pointConstructor)
			r.heuristicWeight = 0f
			return r
		}
	}

	protected var postSmoothingOn = false
	protected var repeatedPostSmooth = false
	protected var heuristicWeight = 1f

	protected fun initialiseMemory(size: Int, defaultDistance: Float, defaultParent: Int, defaultVisited: Boolean) {
		val ticketNumber = Memory.initialise(size, defaultDistance, defaultParent, defaultVisited)
		recorder?.initializeStaticMemory(ticketNumber)
	}

	internal abstract fun getParent(index: Int): Int

	protected abstract fun setParent(index: Int, value: Int)

	internal abstract val parentSize: Int

	protected fun toOneDimIndex(x: Int, y: Int) = graph.toOneDimIndex(x, y)

	protected fun toTwoDimX(index: Int) = graph.toTwoDimX(index)

	protected fun toTwoDimY(index: Int) = graph.toTwoDimY(index)

	protected open fun selected(index: Int) = false

	internal open fun goalParentIndex(): Int {
		return toOneDimIndex(ex, ey)
	}

	internal open fun snapshotVertex(index: Int) = when {
		selected(index) -> intArrayOf(toTwoDimX(index), toTwoDimY(index))
		else -> null
	}

	internal open fun snapshotEdge(endIndex: Int): IntArray {
		val edge = intArrayOf(0, 0, toTwoDimX(endIndex), toTwoDimY(endIndex))
		val startIndex = getParent(endIndex)
		if (startIndex < 0) {
			edge[0] = edge[2]
			edge[1] = edge[3]
		} else {
			edge[0] = toTwoDimX(startIndex)
			edge[1] = toTwoDimY(startIndex)
		}

		return edge
	}
}