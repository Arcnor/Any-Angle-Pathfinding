package com.github.ohohcakester.algorithms.astar

import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.FloatIndirectHeap

import java.util.ArrayList
import java.util.Arrays

class AcceleratedAStar(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AStar(graph, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = postSmooth(graph, sx, sy, ex, ey, ::AcceleratedAStar)
		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = repeatedPostSmooth(graph, sx, sy, ex, ey, ::AcceleratedAStar)
		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = dijkstra(graph, sx, sy, ex, ey, ::AcceleratedAStar)
	}

	private var closed: MutableList<Int>? = null
	private var maxRanges: Array<IntArray>? = null

	init {
		postSmoothingOn = false
	}

	override fun computePath() {
		val start = toOneDimIndex(sx, sy)
		finish = toOneDimIndex(ex, ey)

		Arrays.fill(distance, 0f)
		Arrays.fill(parent, 0)
		Arrays.fill(visited, false)
		maxRanges = graph.computeMaxDownLeftRanges() // O(size of gridGraph) computation. See actual method.

		initialise(start)

		closed = ArrayList<Int>()

		pq = FloatIndirectHeap(distance, true)
		pq.heapify()

		while (!pq.isEmpty) {
			val current = pq.popMinIndex()
			if (current == finish || distance[current] == Float.POSITIVE_INFINITY) {
				maybeSaveSearchSnapshot()
				break
			}
			visited[current] = true
			closed!!.add(current)

			val x = toTwoDimX(current)
			val y = toTwoDimY(current)

			val maxSquare = detectMaxSquare(x, y)

			if (maxSquare == 0) {
				relaxSuccessorsSizeZero(x, y)
			} else {
				relaxSuccessors(x, y, maxSquare)
			}

			maybeSaveSearchSnapshot()
		}

		maybePostSmooth()
	}

	private fun relaxSuccessorsSizeZero(x: Int, y: Int) {
		val udlr = BooleanArray(4)
		if (!graph.isBlocked(x - 1, y - 1)) { // bottom left
			udlr[2] = true
			udlr[1] = true
		}
		if (!graph.isBlocked(x, y - 1)) { // bottom right
			udlr[3] = true
			udlr[1] = true
		}
		if (!graph.isBlocked(x - 1, y)) { // top left
			udlr[2] = true
			udlr[0] = true
		}
		if (!graph.isBlocked(x, y)) { // top right
			udlr[3] = true
			udlr[0] = true
		}
		if (udlr[0])
			generateVertex(x, y + 1)
		if (udlr[1])
			generateVertex(x, y - 1)
		if (udlr[2])
			generateVertex(x - 1, y)
		if (udlr[3])
			generateVertex(x + 1, y)
	}

	private fun relaxSuccessors(x: Int, y: Int, squareSize: Int) {
		generateVertex(x, y + squareSize)
		generateVertex(x, y - squareSize)
		generateVertex(x + squareSize, y)
		generateVertex(x - squareSize, y)

	}

	private fun generateVertex(x: Int, y: Int) {
		val destination = toOneDimIndex(x, y)
		if (visited[destination])
			return

		var fValueUpdated = false

		if (processNode(destination, x, y)) {
			fValueUpdated = true
		}

		if (fValueUpdated) {
			pq.decreaseKey(destination, distance[destination] + heuristic(x, y))
		}
	}

	private fun processNode(destination: Int, destX: Int, destY: Int): Boolean {
		var changed = false
		for (fromNode in closed!!) {
			val fromX = toTwoDimX(fromNode)
			val fromY = toTwoDimY(fromNode)
			val newFValue = distance[fromNode] + weight(fromX, fromY, destX, destY)
			if (newFValue < distance[destination]) {
				if (graph.lineOfSight(fromX, fromY, destX, destY)) {
					distance[destination] = newFValue
					parent[destination] = fromNode
					changed = true
				}
			}
		}
		return changed
	}

	/**
	 * <pre>
	 * returns the size of the max square at (x,y). can possibly return 0.
	 * 1: XX
	 * XX

	 * 2: XXX
	 * XXX
	 * XXX
	</pre> *
	 */
	private fun detectMaxSquare(x: Int, y: Int): Int {
		// This is the newer, O(n) method.
		var lower = 0
		var upper = getMaxSize(x, y)
		var newUpper: Int
		val i = x - y + sizeY
		val j = Math.min(x, y)
		if (upper <= lower) return 0

		while (true) {
			newUpper = checkUpperBoundNew(i, j, lower)
			if (newUpper < upper) upper = newUpper
			if (upper <= lower) break

			newUpper = checkUpperBoundNew(i, j, -1 - lower)
			if (newUpper < upper) upper = newUpper
			if (upper <= lower) break

			lower++
			if (upper <= lower) break
		}
		return lower
	}

	/**
	 * <pre>
	 * _______  This function returns the upper bound detected by
	 * |   |k=1| the a leftward and downward search.
	 * |___|___| k is the number of steps moved in the up-right direction.
	 * |k=0|   | k = 0 the the square directly top-right of grid point (x,y).
	 * _______.___|___|
	 * |   |-1 |(x,y)
	 * |___|___|  point of concern
	 * |-2 |   |
	 * |___|___|
	</pre> *
	 */
	private fun checkUpperBoundNew(i: Int, j: Int, k: Int): Int {
		return maxRanges!![i][j + k] - k
	}

	/**
	 * Compares the tile with the end point to set an upper bound on the size.
	 */
	private fun getMaxSize(x: Int, y: Int): Int {
		return Math.max(Math.abs(x - ex), Math.abs(y - ey))
	}

}
