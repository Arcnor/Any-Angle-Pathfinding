package com.github.ohohcakester.algorithms.astarstatic

import com.github.ohohcakester.grid.GridGraph

class AStarOctileHeuristic(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AStarStaticMemory(graph, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): AStarOctileHeuristic {
			val algo = AStarOctileHeuristic(graph, sx, sy, ex, ey)
			algo.postSmoothingOn = true
			algo.repeatedPostSmooth = false
			return algo
		}

		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): AStarOctileHeuristic {
			val algo = AStarOctileHeuristic(graph, sx, sy, ex, ey)
			algo.postSmoothingOn = true
			algo.repeatedPostSmooth = true
			return algo
		}
	}

	override fun heuristic(x: Int, y: Int) = graph.octileDistance(x, y, ex, ey)
}
