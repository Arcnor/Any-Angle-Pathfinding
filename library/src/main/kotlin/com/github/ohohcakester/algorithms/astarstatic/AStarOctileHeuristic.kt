package com.github.ohohcakester.algorithms.astarstatic

import com.github.ohohcakester.grid.GridGraph

class AStarOctileHeuristic(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AStarStaticMemory(graph, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = postSmooth(graph, sx, sy, ex, ey, ::AStarOctileHeuristic)
		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = repeatedPostSmooth(graph, sx, sy, ex, ey, ::AStarOctileHeuristic)
		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = dijkstra(graph, sx, sy, ex, ey, ::AStarOctileHeuristic)
	}

	override fun heuristic(x: Int, y: Int) = graph.octileDistance(x, y, ex, ey)
}
