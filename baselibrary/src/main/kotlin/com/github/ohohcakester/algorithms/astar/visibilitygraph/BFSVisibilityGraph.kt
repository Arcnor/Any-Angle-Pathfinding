package com.github.ohohcakester.algorithms.astar.visibilitygraph

import com.github.ohohcakester.algorithms.astar.VisibilityGraphAlgorithm
import com.github.ohohcakester.datatypes.Point
import com.github.ohohcakester.grid.GridGraph
import java.util.LinkedList
import java.util.Queue

class BFSVisibilityGraph(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : VisibilityGraphAlgorithm(graph, sx, sy, ex, ey) {
	companion object {
		fun graphReuse(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): BFSVisibilityGraph {
			val algo = BFSVisibilityGraph(graph, sx, sy, ex, ey)
			algo.reuseGraph = true
			return algo
		}
	}

	override fun computePath() {
		setupVisibilityGraph()

		val start = visibilityGraph.startNode()
		val finish = visibilityGraph.endNode()
		var queue: Queue<Int>? = LinkedList()
		parent.fill(-1)
		visited.fill(false)

		queue!!.offer(start)
		visited[start] = true

		while (queue != null && !queue.isEmpty()) {
			val current = queue.poll()

			val itr = visibilityGraph.edgeIterator(current)
			while (itr.hasNext()) {
				val edge = itr.next()
				if (!visited[edge.dest]) {
					visited[edge.dest] = true
					parent[edge.dest] = current
					if (edge.dest == finish) {
						queue = null
						break
					}
					queue!!.offer(edge.dest)
				}
			}
			maybeSaveSearchSnapshot()
		}
	}
}
