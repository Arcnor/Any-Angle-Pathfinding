package com.github.ohohcakester.algorithms.astar.visibilitygraph;

import com.github.ohohcakester.algorithms.astar.VisibilityGraphAlgorithm;
import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.grid.GridGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class BFSVisibilityGraph extends VisibilityGraphAlgorithm {

	public LinkedList<Point> path;

	public BFSVisibilityGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, sx, sy, ex, ey);
	}

	public static BFSVisibilityGraph graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
		BFSVisibilityGraph algo = new BFSVisibilityGraph(graph, sx, sy, ex, ey);
		algo.setReuseGraph(true);
		return algo;
	}

	@Override
	public void computePath() {
		setupVisibilityGraph();

		int start = getVisibilityGraph().startNode();
		int finish = getVisibilityGraph().endNode();
		Queue<Integer> queue = new LinkedList<>();
//		setParent(new int[visibilityGraph.size()]);
//		setVisited(new boolean[visibilityGraph.size()]);
		for (int i = 0; i < getParent().length; i++) {
			getParent()[i] = -1;
		}

		queue.offer(start);
		getVisited()[start] = true;

		while (queue != null && !queue.isEmpty()) {
			int current = queue.poll();

			Iterator<Edge> itr = getVisibilityGraph().edgeIterator(current);
			while (itr.hasNext()) {
				Edge edge = itr.next();
				if (!getVisited()[edge.dest]) {
					getVisited()[edge.dest] = true;
					getParent()[edge.dest] = current;
					if (edge.dest == finish) {
						queue = null;
						break;
					}
					queue.offer(edge.dest);
				}
			}
			maybeSaveSearchSnapshot();
		}
	}
}
