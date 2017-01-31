package com.github.ohohcakester.algorithms.astar;

import com.github.ohohcakester.algorithms.astar.visibilitygraph.Edge;
import com.github.ohohcakester.algorithms.astar.visibilitygraph.VisibilityGraph;
import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.datatypes.SnapshotItem;
import com.github.ohohcakester.priorityqueue.IndirectHeap;
import com.github.ohohcakester.grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VisibilityGraphAlgorithm extends AStar {
	protected VisibilityGraph visibilityGraph;
	protected boolean reuseGraph = false;
	private boolean slowDijkstra = false;

	public VisibilityGraphAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, sx, sy, ex, ey);
	}

	public static VisibilityGraphAlgorithm noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
		VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
		algo.heuristicWeight = 0;
		return algo;
	}

	public static VisibilityGraphAlgorithm graphReuseNoHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
		VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
		algo.reuseGraph = true;
		algo.heuristicWeight = 0;
		return algo;
	}

	public static VisibilityGraphAlgorithm graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
		VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
		algo.reuseGraph = true;
		return algo;
	}

	public static VisibilityGraphAlgorithm graphReuseSlowDijkstra(GridGraph graph, int sx, int sy, int ex, int ey) {
		VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
		algo.reuseGraph = true;
		algo.slowDijkstra = true;
		return algo;
	}

	public VisibilityGraph getVisibilityGraph() {
		return visibilityGraph;
	}

	@Override
	public void computePath() {
		setupVisibilityGraph();

		distance = new Float[visibilityGraph.size()];
		setParent(new int[visibilityGraph.size()]);

		initialise(visibilityGraph.startNode());
		visited = new boolean[visibilityGraph.size()];

		if (slowDijkstra) {
			slowDijkstra();
		} else {
			pqDijkstra();
		}
	}

	protected void setupVisibilityGraph() {
		if (reuseGraph) {
			visibilityGraph = VisibilityGraph.getStoredGraph(getGraph(), getSx(), getSy(), getEx(), getEy());
		} else {
			visibilityGraph = new VisibilityGraph(getGraph(), getSx(), getSy(), getEx(), getEy());
		}

		if (isRecording()) {
			visibilityGraph.setSaveSnapshotFunction(() -> saveVisibilityGraphSnapshot());
			visibilityGraph.initialise();
			saveVisibilityGraphSnapshot();
		} else {
			visibilityGraph.initialise();
		}
	}

	private void slowDijkstra() {
		int finish = visibilityGraph.endNode();
		while (true) {
			int current = findMinDistance();
			if (current == -1) {
				break;
			}
			visited[current] = true;

			if (current == finish) {
				break;
			}

			Iterator<Edge> itr = visibilityGraph.edgeIterator(current);
			while (itr.hasNext()) {
				Edge edge = itr.next();
				if (!visited[edge.dest]) {
					relax(edge);
				}
			}

			maybeSaveSearchSnapshot();
		}
	}

	private int findMinDistance() {
		float minDistance = Float.POSITIVE_INFINITY;
		int minIndex = -1;
		for (int i = 0; i < distance.length; i++) {
			if (!visited[i] && distance[i] < minDistance) {
				minDistance = distance[i];
				minIndex = i;
			}
		}
		return minIndex;
	}

	private void pqDijkstra() {
		pq = new IndirectHeap<Float>(distance, true);
		pq.heapify();

		int finish = visibilityGraph.endNode();
		while (!pq.isEmpty()) {
			int current = pq.popMinIndex();
			visited[current] = true;

			if (current == finish) {
				break;
			}

			Iterator<Edge> itr = visibilityGraph.edgeIterator(current);
			while (itr.hasNext()) {
				Edge edge = itr.next();
				if (!visited[edge.dest] && relax(edge)) {
					// If relaxation is done.
					Point dest = visibilityGraph.coordinateOf(edge.dest);
					pq.decreaseKey(edge.dest, distance[edge.dest] + heuristic(dest.getX(), dest.getY()));
				}
			}

			maybeSaveSearchSnapshot();
		}
	}

	protected final boolean relax(Edge edge) {
		// return true iff relaxation is done.
		return relax(edge.source, edge.dest, edge.weight);
	}

	protected final boolean relax(int u, int v, float weightUV) {
		// return true iff relaxation is done.
		float newWeight = distance[u] + weightUV;
		if (newWeight < distance[v]) {
			distance[v] = newWeight;
			getParent()[v] = u;
			return true;
		}
		return false;
	}


	private int pathLength() {
		int length = 0;
		int current = visibilityGraph.endNode();
		while (current != -1) {
			current = getParent()[current];
			length++;
		}
		return length;
	}

	@Override
	public int[][] getPath() {
		int length = pathLength();
		int[][] path = new int[length][];
		int current = visibilityGraph.endNode();

		int index = length - 1;
		while (current != -1) {
			Point point = visibilityGraph.coordinateOf(current);
			int x = point.getX();
			int y = point.getY();

			path[index] = new int[2];
			path[index][0] = x;
			path[index][1] = y;

			index--;
			current = getParent()[current];
		}

		return path;
	}

	@Override
	protected int goalParentIndex() {
		return visibilityGraph.endNode();
	}

	@Override
	protected Integer[] snapshotEdge(int endIndex) {
		Integer[] edge = new Integer[4];
		int startIndex = getParent()[endIndex];
		Point startPoint = visibilityGraph.coordinateOf(startIndex);
		Point endPoint = visibilityGraph.coordinateOf(endIndex);
		edge[0] = startPoint.getX();
		edge[1] = startPoint.getY();
		edge[2] = endPoint.getX();
		edge[3] = endPoint.getY();
		return edge;
	}

	@Override
	protected Integer[] snapshotVertex(int index) {
		if (selected(index)) {
			Point point = visibilityGraph.coordinateOf(index);
			Integer[] edge = new Integer[2];
			edge[0] = point.getX();
			edge[1] = point.getY();
			return edge;
		}
		return null;
	}

	private void saveVisibilityGraphSnapshot() {
	    /*if (!isRecording()) {
            return;
        }*/
		int size = visibilityGraph.size();

		List<SnapshotItem> snapshotItemList = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			Iterator<Edge> iterator = visibilityGraph.edgeIterator(i);
			while (iterator.hasNext()) {
				Edge edge = iterator.next();
				if (edge.source < edge.dest) {
					Point start = visibilityGraph.coordinateOf(edge.source);
					Point end = visibilityGraph.coordinateOf(edge.dest);

					Integer[] path = new Integer[4];
					path[0] = start.getX();
					path[1] = start.getY();
					path[2] = end.getX();
					path[3] = end.getY();

					SnapshotItem snapshotItem = SnapshotItem.Companion.generate(path, Color.GREEN);
					snapshotItemList.add(snapshotItem);
				}
			}
		}
		addSnapshot(snapshotItemList);
	}
}
