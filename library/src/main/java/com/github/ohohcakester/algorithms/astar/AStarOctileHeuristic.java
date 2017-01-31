package com.github.ohohcakester.algorithms.astar;

import com.github.ohohcakester.grid.GridGraph;

public class AStarOctileHeuristic extends AStarStaticMemory {
	public AStarOctileHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, sx, sy, ex, ey);
	}

	public static AStarOctileHeuristic postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
		AStarOctileHeuristic algo = new AStarOctileHeuristic(graph, sx, sy, ex, ey);
		algo.setPostSmoothingOn(true);
		return algo;
	}

	protected float heuristic(int x, int y) {
		return getGraph().octileDistance(x, y, getEx(), getEy());
	}
}
