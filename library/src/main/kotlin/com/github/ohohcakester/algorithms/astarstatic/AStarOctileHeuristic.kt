package com.github.ohohcakester.algorithms.astarstatic

import com.github.ohohcakester.grid.GridGraph

class AStarOctileHeuristic<out P>(graph: GridGraph,
                                  sx: Int, sy: Int, ex: Int, ey: Int,
                                  pointConstructor: (x: Int, y: Int) -> P) : AStarStaticMemory<P>(graph, sx, sy, ex, ey, pointConstructor) {
	companion object {
		fun <P> postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = postSmooth(graph, sx, sy, ex, ey, pointConstructor, ::AStarOctileHeuristic)
		fun <P> repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = repeatedPostSmooth(graph, sx, sy, ex, ey, pointConstructor, ::AStarOctileHeuristic)
		fun <P> dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, pointConstructor: (x: Int, y: Int) -> P) = dijkstra(graph, sx, sy, ex, ey, pointConstructor, ::AStarOctileHeuristic)
	}

	override fun heuristic(x: Int, y: Int) = graph.octileDistance(x, y, ex, ey)
}
