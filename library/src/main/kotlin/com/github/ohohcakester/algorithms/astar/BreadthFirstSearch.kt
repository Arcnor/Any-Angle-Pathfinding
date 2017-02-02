package com.github.ohohcakester.algorithms.astar

import com.github.ohohcakester.grid.GridGraph

import java.util.Arrays
import java.util.LinkedList
import java.util.Queue

open class BreadthFirstSearch(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AStar(graph, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = postSmooth(graph, sx, sy, ex, ey, ::BreadthFirstSearch)
		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = repeatedPostSmooth(graph, sx, sy, ex, ey, ::BreadthFirstSearch)
		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = dijkstra(graph, sx, sy, ex, ey, ::BreadthFirstSearch)
	}

	private lateinit var queue: Queue<Int>

	override fun computePath() {
		val start = toOneDimIndex(sx, sy)
		finish = toOneDimIndex(ex, ey)

		parent.fill(0)
		visited.fill(false)
		for (i in 0..parent.size - 1) {
			parent[i] = -1
		}

		queue = LinkedList<Int>()
		queue.offer(start)
		visited[start] = true

		while (!queue.isEmpty()) {
			val current = queue.poll()
			val currX = toTwoDimX(current)
			val currY = toTwoDimY(current)

			if (canGoDown(currX, currY)) {
				val index = toOneDimIndex(currX, currY - 1)
				if (!visited[index]) {
					if (addToQueue(current, index))
						break
				}
			}
			if (canGoUp(currX, currY)) {
				val index = toOneDimIndex(currX, currY + 1)
				if (!visited[index]) {
					if (addToQueue(current, index))
						break
				}
			}
			if (canGoLeft(currX, currY)) {
				val index = toOneDimIndex(currX - 1, currY)
				if (!visited[index]) {
					if (addToQueue(current, index))
						break
				}
			}
			if (canGoRight(currX, currY)) {
				val index = toOneDimIndex(currX + 1, currY)
				if (!visited[index]) {
					if (addToQueue(current, index))
						break
				}
			}

			maybeSaveSearchSnapshot()
		}

		maybePostSmooth()
	}

	/**
	 * Returns true iff finish is found.
	 */
	private fun addToQueue(current: Int, index: Int): Boolean {
		parent[index] = current
		queue.offer(index)
		visited[index] = true
		return index == finish
	}

	private fun canGoUp(x: Int, y: Int) = !bottomRightOfBlockedTile(x, y) || !bottomLeftOfBlockedTile(x, y)

	private fun canGoDown(x: Int, y: Int) = !topRightOfBlockedTile(x, y) || !topLeftOfBlockedTile(x, y)

	private fun canGoLeft(x: Int, y: Int) = !topRightOfBlockedTile(x, y) || !bottomRightOfBlockedTile(x, y)

	private fun canGoRight(x: Int, y: Int) = !topLeftOfBlockedTile(x, y) || !bottomLeftOfBlockedTile(x, y)

	private fun topRightOfBlockedTile(x: Int, y: Int) = graph.isBlocked(x - 1, y - 1)

	private fun topLeftOfBlockedTile(x: Int, y: Int) = graph.isBlocked(x, y - 1)

	private fun bottomRightOfBlockedTile(x: Int, y: Int) = graph.isBlocked(x - 1, y)

	private fun bottomLeftOfBlockedTile(x: Int, y: Int) = graph.isBlocked(x, y)
}
