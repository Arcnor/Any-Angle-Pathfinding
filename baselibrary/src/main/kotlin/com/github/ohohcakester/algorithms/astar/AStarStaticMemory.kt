package com.github.ohohcakester.algorithms.astar

import com.github.ohohcakester.algorithms.PathFindingAlgorithm
import com.github.ohohcakester.datatypes.Memory
import com.github.ohohcakester.priorityqueue.ReusableIndirectHeap
import com.github.ohohcakester.grid.GridGraph


open class AStarStaticMemory(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : PathFindingAlgorithm(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): AStarStaticMemory {
			val aStar = AStarStaticMemory(graph, sx, sy, ex, ey)
			aStar.postSmoothingOn = true
			aStar.repeatedPostSmooth = false
			return aStar
		}

		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): AStarStaticMemory {
			val aStar = AStarStaticMemory(graph, sx, sy, ex, ey)
			aStar.postSmoothingOn = true
			aStar.repeatedPostSmooth = true
			return aStar
		}

		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): AStarStaticMemory {
			val aStar = AStarStaticMemory(graph, sx, sy, ex, ey)
			aStar.heuristicWeight = 0f
			return aStar
		}
	}

	protected var postSmoothingOn = false
	protected var repeatedPostSmooth = false
	protected var heuristicWeight = 1f

	protected var pq: ReusableIndirectHeap

	protected var finish: Int = 0

	init {
		val totalSize = (graph.sizeX + 1) * (graph.sizeY + 1)
		pq = ReusableIndirectHeap(totalSize)
	}

	override fun computePath() {
		val totalSize = (graph.sizeX + 1) * (graph.sizeY + 1)

		val start = toOneDimIndex(sx, sy)
		finish = toOneDimIndex(ex, ey)

		pq = ReusableIndirectHeap(totalSize)
		this.initialiseMemory(totalSize, java.lang.Float.POSITIVE_INFINITY, -1, false)

		initialise(start)

		var lastDist = -1f
		while (!pq.isEmpty) {
			val dist = pq.minValue

			val current = pq.popMinIndex()

			if (Math.abs(dist - lastDist) > 0.01f) {
				maybeSaveSearchSnapshot()
				lastDist = dist
			}

			if (current == finish || distance(current) == java.lang.Float.POSITIVE_INFINITY) {
				//maybeSaveSearchSnapshot();
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

			//maybeSaveSearchSnapshot();
		}

		maybePostSmooth()
	}

	protected open fun tryRelaxNeighbour(current: Int, currentX: Int, currentY: Int, x: Int, y: Int) {
		if (!graph.isValidCoordinate(x, y))
			return

		val destination = toOneDimIndex(x, y)
		if (visited(destination))
			return
		if (!graph.neighbourLineOfSight(currentX, currentY, x, y))
			return

		if (relax(current, destination, weight(currentX, currentY, x, y))) {
			// If relaxation is done.
			pq.decreaseKey(destination, distance(destination) + heuristic(x, y))
		}
	}

	protected open fun heuristic(x: Int, y: Int): Float {
		//return 0;
		return heuristicWeight * graph.distance(x, y, ex, ey)
	}


	protected fun weight(x1: Int, y1: Int, x2: Int, y2: Int): Float {
		return graph.distance(x1, y1, x2, y2)
	}

	protected open fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		// return true iff relaxation is done.

		val newWeight = distance(u) + weightUV
		if (newWeight < distance(v)) {
			setDistance(v, newWeight)
			setParent(v, u)
			//maybeSaveSearchSnapshot();
			return true
		}
		return false
	}


	protected fun initialise(s: Int) {
		pq.decreaseKey(s, 0f)
		Memory.setDistance(s, 0f)
	}


	private fun pathLength(): Int {
		var length = 0
		var current = finish
		while (current != -1) {
			current = parent(current)
			length++
		}
		return length
	}

	protected fun lineOfSight(node1: Int, node2: Int): Boolean {
		val x1 = toTwoDimX(node1)
		val y1 = toTwoDimY(node1)
		val x2 = toTwoDimX(node2)
		val y2 = toTwoDimY(node2)
		return graph.lineOfSight(x1, y1, x2, y2)
	}

	protected fun physicalDistance(node1: Int, node2: Int): Float {
		val x1 = toTwoDimX(node1)
		val y1 = toTwoDimY(node1)
		val x2 = toTwoDimX(node2)
		val y2 = toTwoDimY(node2)
		return graph.distance(x1, y1, x2, y2)
	}

	protected fun physicalDistance(x1: Int, y1: Int, node2: Int): Float {
		val x2 = toTwoDimX(node2)
		val y2 = toTwoDimY(node2)
		return graph.distance(x1, y1, x2, y2)
	}

	protected fun maybePostSmooth() {
		if (postSmoothingOn) {
			if (repeatedPostSmooth) {
				while (postSmooth()) {}
			} else {
				postSmooth()
			}
		}
	}

	private fun postSmooth(): Boolean {
		var didSomething = false

		var current = finish
		while (current != -1) {
			var next = parent(current) // we can skip checking this one as it always has LoS to current.
			if (next != -1) {
				next = parent(next)
				while (next != -1) {
					if (lineOfSight(current, next)) {
						setParent(current, next)
						next = parent(next)
						didSomething = true
						maybeSaveSearchSnapshot()
					} else {
						next = -1
					}
				}
			}

			current = parent(current)
		}

		return didSomething
	}


	override val path: Array<IntArray>
		get() {
			var current = finish

			return Array(pathLength()) {
				val result = intArrayOf(toTwoDimX(current), toTwoDimY(current))
				current = parent(current)
				result
			}.reversedArray()
		}

	override fun selected(index: Int) = visited(index)

	protected fun parent(index: Int) = Memory.parent(index)

	protected fun setParent(index: Int, value: Int) = Memory.setParent(index, value)

	protected fun distance(index: Int) = Memory.distance(index)

	protected fun setDistance(index: Int, value: Float) = Memory.setDistance(index, value)

	protected fun visited(index: Int) = Memory.visited(index)

	protected fun setVisited(index: Int, value: Boolean) = Memory.setVisited(index, value)
}