package com.github.ohohcakester.grid;

import com.github.ohohcakester.algorithms.astar.BreadthFirstSearch;
import com.github.ohohcakester.datatypes.Point;
import kotlin.jvm.functions.Function2;

import java.util.ArrayList;

public class ReachableNodes<P> extends BreadthFirstSearch<P> {

	private ReachableNodes(GridGraph graph, int sx, int sy, int ex,
	                       int ey, Function2<Integer, Integer, P> pointConstructor) {
		super(graph, sx, sy, ex, ey, pointConstructor);
	}

	/**
	 * Computes the set of all nodes reachable from (sx,sy) by an unblocked path.
	 *
	 * @param graph the grid to use.
	 * @param sx    x-coordinate of root node
	 * @param sy    y-coordinate of root node
	 * @return An ArrayList of Point objects (nodes reachable from (sx,sy) via an unblocked path).
	 */
	public static ArrayList<Point> computeReachable(GridGraph graph, int sx, int sy) {
		ReachableNodes nodes = new ReachableNodes<>(graph, sx, sy, -10, -10, Point::new);
		ArrayList<Point> list = new ArrayList<>();

		nodes.computePath();
		for (int i = 0; i < nodes.getVisited().length; i++) {
			if (nodes.getVisited()[i]) {
				int x = nodes.toTwoDimX(i);
				int y = nodes.toTwoDimY(i);
				list.add(new Point(x, y));
			}
		}

		return list;
	}

}
