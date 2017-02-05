package com.github.ohohcakester.algorithms.astar.visibilitygraph

import com.github.ohohcakester.algorithms.astar.AStar
import com.github.ohohcakester.datatypes.SnapshotItem
import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.FloatIndirectHeap
import java.awt.Color
import java.util.ArrayList

open class VisibilityGraphAlgorithm<out P>(graph: GridGraph,
                                           sx: Int, sy: Int, ex: Int, ey: Int,
                                           pointConstructor: (x: Int, y: Int) -> P) : AStar<P>(graph, sx, sy, ex, ey, pointConstructor) {
	companion object {
		fun <P> noHeuristic(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P): VisibilityGraphAlgorithm<P> {
			val algo = VisibilityGraphAlgorithm(graph, sx, sy, ex, ey, pointConstructor)
			algo.heuristicWeight = 0f
			return algo
		}

		fun <P> graphReuseNoHeuristic(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P): VisibilityGraphAlgorithm<P> {
			val algo = VisibilityGraphAlgorithm(graph, sx, sy, ex, ey, pointConstructor)
			algo.reuseGraph = true
			algo.heuristicWeight = 0f
			return algo
		}

		fun <P> graphReuse(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P): VisibilityGraphAlgorithm<P> {
			val algo = VisibilityGraphAlgorithm(graph, sx, sy, ex, ey, pointConstructor)
			algo.reuseGraph = true
			return algo
		}

		fun <P> graphReuseSlowDijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P): VisibilityGraphAlgorithm<P> {
			val algo = VisibilityGraphAlgorithm(graph, sx, sy, ex, ey, pointConstructor)
			algo.reuseGraph = true
			algo.slowDijkstra = true
			return algo
		}
	}

	lateinit var visibilityGraph: VisibilityGraph
		protected set
	protected var reuseGraph = false
	private var slowDijkstra = false

	override fun getSize(): Int {
		// FIXME: This is broken (we should not setup this here...)
		setupVisibilityGraph()
		return visibilityGraph.size()
	}

	override fun computePath() {
		setupVisibilityGraph()
		//		setDistance(new Float[visibilityGraph.size()]);
		//		setParent(new int[visibilityGraph.size()]);

		initialise(visibilityGraph.startNode())
		//		setVisited(new boolean[visibilityGraph.size()]);

		if (slowDijkstra) {
			slowDijkstra()
		} else {
			pqDijkstra()
		}
	}

	protected fun setupVisibilityGraph() {
		if (reuseGraph) {
			visibilityGraph = VisibilityGraph.getStoredGraph(graph, sx, sy, ex, ey)
		} else {
			visibilityGraph = VisibilityGraph(graph, sx, sy, ex, ey)
		}

		if (recorder?.isRecording ?: false) {
			visibilityGraph.setSaveSnapshotFunction { saveVisibilityGraphSnapshot() }
			visibilityGraph.initialise()
			saveVisibilityGraphSnapshot()
		} else {
			visibilityGraph.initialise()
		}
	}

	private fun slowDijkstra() {
		val finish = visibilityGraph.endNode()
		while (true) {
			val current = findMinDistance()
			if (current == -1) {
				break
			}
			visited[current] = true

			if (current == finish) {
				break
			}

			val itr = visibilityGraph.edgeIterator(current)
			while (itr.hasNext()) {
				val edge = itr.next()
				if (!visited[edge.dest]) {
					relax(edge)
				}
			}

			maybeSaveSearchSnapshot()
		}
	}

	private fun findMinDistance(): Int {
		var minDistance = java.lang.Float.POSITIVE_INFINITY
		var minIndex = -1
		for (i in 0..distance.size - 1) {
			if (!visited[i] && distance[i] < minDistance) {
				minDistance = distance[i]
				minIndex = i
			}
		}
		return minIndex
	}

	private fun pqDijkstra() {
		val pq = FloatIndirectHeap(distance, true)
		pq.heapify()

		val finish = visibilityGraph.endNode()
		while (!pq.isEmpty) {
			val current = pq.popMinIndex()
			visited[current] = true

			if (current == finish) {
				break
			}

			val itr = visibilityGraph.edgeIterator(current)
			while (itr.hasNext()) {
				val edge = itr.next()
				if (!visited[edge.dest] && relax(edge)) {
					// If relaxation is done.
					val dest = visibilityGraph.coordinateOf(edge.dest)
					pq.decreaseKey(edge.dest, distance[edge.dest] + heuristic(dest.x, dest.y))
				}
			}

			maybeSaveSearchSnapshot()
		}
	}

	protected fun relax(edge: Edge): Boolean {
		// return true iff relaxation is done.
		return relax(edge.source, edge.dest, edge.weight)
	}

	override fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		// return true iff relaxation is done.
		val newWeight = distance[u] + weightUV
		if (newWeight < distance[v]) {
			distance[v] = newWeight
			parent[v] = u
			return true
		}
		return false
	}


	private fun pathLength(): Int {
		var length = 0
		var current = visibilityGraph.endNode()
		while (current != -1) {
			current = parent[current]
			length++
		}
		return length
	}

	override val path: List<P>
		get() {
			val length = pathLength()
			var current = visibilityGraph.endNode()
			return List(length) {
				val point = visibilityGraph.coordinateOf(current)
				val x = point.x
				val y = point.y

				current = parent[current]

				makePoint(x, y)
			}.reversed()
		}

	override fun goalParentIndex(): Int {
		return visibilityGraph.endNode()
	}

	override fun snapshotEdge(endIndex: Int): IntArray {
		val startIndex = parent[endIndex]
		val startPoint = visibilityGraph.coordinateOf(startIndex)
		val endPoint = visibilityGraph.coordinateOf(endIndex)
		return intArrayOf(
				startPoint.x,
				startPoint.y,
				endPoint.x,
				endPoint.y
		)
	}

	override fun snapshotVertex(index: Int) = if (selected(index)) {
		val point = visibilityGraph.coordinateOf(index)
		intArrayOf(point.x, point.y)
	} else {
		null
	}

	private fun saveVisibilityGraphSnapshot() {
		/*if (!isRecording()) {
            return;
        }*/
		val size = visibilityGraph.size()

		val snapshotItemList = ArrayList<SnapshotItem>(size)

		for (i in 0..size - 1) {
			val iterator = visibilityGraph.edgeIterator(i)
			while (iterator.hasNext()) {
				val edge = iterator.next()
				if (edge.source < edge.dest) {
					val start = visibilityGraph.coordinateOf(edge.source)
					val end = visibilityGraph.coordinateOf(edge.dest)

					val path = intArrayOf(
							start.x,
							start.y,
							end.x,
							end.y
					)

					val snapshotItem = SnapshotItem.generate(path, Color.GREEN)
					snapshotItemList.add(snapshotItem)
				}
			}
		}
		addSnapshot(snapshotItemList)
	}
}
