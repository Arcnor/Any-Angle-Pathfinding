package com.github.ohohcakester.algorithms.astar

import com.github.ohohcakester.algorithms.BaseAStar
import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.FloatIndirectHeap

open class AStar<out P>(graph: GridGraph,
                    sx: Int, sy: Int, ex: Int, ey: Int,
                    pointConstructor: (x: Int, y: Int) -> P) : BaseAStar<P>(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey, pointConstructor) {
	companion object {
		fun <P> postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = postSmooth(graph, sx, sy, ex, ey, pointConstructor, ::AStar)
		fun <P> repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = repeatedPostSmooth(graph, sx, sy, ex, ey, pointConstructor, ::AStar)
		fun <P> dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = dijkstra(graph, sx, sy, ex, ey, pointConstructor, ::AStar)
	}

	val distance: FloatArray
	protected val visited: BooleanArray

	private lateinit var pq: FloatIndirectHeap

	protected var finish: Int = 0

	protected val parent: IntArray

	init {
		val totalSize = getSize()

		visited = BooleanArray(totalSize)
		distance = FloatArray(totalSize)
		parent = IntArray(totalSize)
	}

	open fun getSize() = (graph.sizeX + 1) * (graph.sizeY + 1)

	override fun computePath() {
		val start = toOneDimIndex(sx, sy)
		finish = toOneDimIndex(ex, ey)

		distance.fill(0f)
		parent.fill(0)
		visited.fill(false)

		initialise(start)

		pq = FloatIndirectHeap(distance, true)
		pq.heapify()

		//float lastDist = -1;
		while (!pq.isEmpty) {
			//float dist = pq.getMinValue();

			val current = pq.popMinIndex()

			maybeSaveSearchSnapshot()
			//if (Math.abs(dist - lastDist) > 0.01f) { maybeSaveSearchSnapshot(); lastDist = dist;}

			if (current == finish || distance[current] == java.lang.Float.POSITIVE_INFINITY) {
				//maybeSaveSearchSnapshot();
				break
			}
			visited[current] = true

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

	private fun tryRelaxNeighbour(current: Int, currentX: Int, currentY: Int, x: Int, y: Int) {
		if (!graph.isValidCoordinate(x, y))
			return

		val destination = toOneDimIndex(x, y)
		if (visited[destination])
			return
		if (!graph.neighbourLineOfSight(currentX, currentY, x, y))
			return

		if (relax(current, destination, weight(currentX, currentY, x, y))) {
			// If relaxation is done.
			pq.decreaseKey(destination, distance[destination] + heuristic(x, y))
		}
	}

	protected fun heuristic(x: Int, y: Int): Float {
		//return 0;
		return heuristicWeight * graph.distance(x, y, ex, ey)
	}


	protected fun weight(x1: Int, y1: Int, x2: Int, y2: Int): Float {
		return graph.distance(x1, y1, x2, y2)
	}

	protected open fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		// return true iff relaxation is done.

		val newWeight = distance[u] + weightUV
		if (newWeight < distance[v]) {
			distance[v] = newWeight
			parent[v] = u
			//maybeSaveSearchSnapshot();
			return true
		}
		return false
	}


	protected fun initialise(s: Int) {
		for (i in distance.indices) {
			distance[i] = java.lang.Float.POSITIVE_INFINITY
			parent[i] = -1
		}
		distance[s] = 0f
	}

	private fun pathLength(): Int {
		var length = 0
		var current = finish
		while (current != -1) {
			current = parent[current]
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
				while (postSmooth()) {
				}
			} else {
				postSmooth()
			}
		}
	}

	private fun postSmooth(): Boolean {
		var didSomething = false

		var current = finish
		while (current != -1) {
			var next = parent[current] // we can skip checking this one as it always has LoS to current.
			if (next != -1) {
				next = parent[next]
				while (next != -1) {
					if (lineOfSight(current, next)) {
						parent[current] = next
						next = parent[next]
						didSomething = true
						maybeSaveSearchSnapshot()
					} else {
						next = -1
					}
				}
			}

			current = parent[current]
		}

		return didSomething
	}


	override val path: List<P>
		get() {
			var current = finish

			return List(pathLength()) {
				val result = makePoint(toTwoDimX(current), toTwoDimY(current))
				current = getParent(current)
				result
			}.reversed()
		}

	override fun selected(index: Int) = visited[index]

	override fun getParent(index: Int) = parent[index]

	override fun setParent(index: Int, value: Int) {
		parent[index] = value
	}

	override val parentSize = parent.size
}