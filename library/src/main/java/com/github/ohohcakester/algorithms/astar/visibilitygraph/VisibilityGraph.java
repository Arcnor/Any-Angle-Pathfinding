package com.github.ohohcakester.algorithms.astar.visibilitygraph;

import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.grid.GridGraph;

import java.util.ArrayList;
import java.util.Iterator;

public class VisibilityGraph {
	private static VisibilityGraph storedVisibilityGraph;
	private static GridGraph storedGridGraph;

	private final GridGraph graph;
	private final int sx, sy, ex, ey;
	ArrayList<Point> nodeList;
	ArrayList<ArrayList<Edge>> outgoingEdgeList;
	private int[][] nodeIndex;
	private int startIndex;
	private boolean startIsNewNode;
	private int endIndex;
	private boolean endIsNewNode;
	private Runnable saveSnapshot;

	public VisibilityGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
		this.graph = graph;
		this.sx = sx;
		this.sy = sy;
		this.ex = ex;
		this.ey = ey;
	}

	public static VisibilityGraph repurpose(VisibilityGraph oldGraph, int sx, int sy, int ex, int ey) {
		oldGraph.removeStartAndEnd();
		VisibilityGraph newGraph = new VisibilityGraph(oldGraph.graph, sx, sy, ex, ey);
		newGraph.nodeIndex = oldGraph.nodeIndex;
		newGraph.startIndex = oldGraph.startIndex;
		newGraph.startIsNewNode = oldGraph.startIsNewNode;
		newGraph.endIndex = oldGraph.endIndex;
		newGraph.endIsNewNode = oldGraph.endIsNewNode;
		newGraph.nodeList = oldGraph.nodeList;
		newGraph.outgoingEdgeList = oldGraph.outgoingEdgeList;

		newGraph.addStartAndEnd(sx, sy, ex, ey);

		return newGraph;
	}

	public static VisibilityGraph getStoredGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
		VisibilityGraph visibilityGraph = null;
		if (storedGridGraph != graph || storedVisibilityGraph == null) {
			//("Get new graph");
			visibilityGraph = new VisibilityGraph(graph, sx, sy, ex, ey);
			storedVisibilityGraph = visibilityGraph;
			storedGridGraph = graph;
		} else {
			//("Reuse graph");
			visibilityGraph = repurpose(storedVisibilityGraph, sx, sy, ex, ey);
			storedVisibilityGraph = visibilityGraph;
		}
		return visibilityGraph;
	}

	public void setSaveSnapshotFunction(Runnable saveSnapshot) {
		this.saveSnapshot = saveSnapshot;
	}

	public void initialise() {
		if (nodeList != null) {
			//("already initialised.");
			return;
		}

		nodeList = new ArrayList<>();
		outgoingEdgeList = new ArrayList<>();

		addNodes();
		addAllEdges();
		addStartAndEnd(sx, sy, ex, ey);
	}

	private void addNodes() {
		nodeIndex = new int[graph.getSizeY() + 1][];
		for (int y = 0; y < nodeIndex.length; y++) {
			nodeIndex[y] = new int[graph.getSizeX() + 1];
			for (int x = 0; x < nodeIndex[y].length; x++) {
				if (isCorner(x, y)) {
					nodeIndex[y][x] = assignNode(x, y);
				} else {
					nodeIndex[y][x] = -1;
				}
			}
		}
	}

	private int assignNode(int x, int y) {
		int index = nodeList.size();
		nodeList.add(new Point(x, y));
		outgoingEdgeList.add(new ArrayList<Edge>());
		return index;
	}

	private int assignNodeAndConnect(int x, int y) {
		int index = nodeList.size();
		nodeList.add(new Point(x, y));
		outgoingEdgeList.add(new ArrayList<Edge>());

		for (int i = 0; i < nodeList.size() - 1; i++) {
			Point toPoint = coordinateOf(i);
			if (graph.lineOfSight(x, y, toPoint.getX(), toPoint.getY())) {
				float weight = computeWeight(x, y, toPoint.getX(), toPoint.getY());
				addEdge(i, index, weight);
				addEdge(index, i, weight);
			}
		}

		return index;
	}

	/**
	 * Assumption: start and end are the last two nodes, if they exist.
	 */
	private void removeStartAndEnd() {
		if (startIsNewNode) {
			int index = nodeList.size() - 1;
			nodeIndex[sy][sx] = -1;
			nodeList.remove(index);
			outgoingEdgeList.remove(index);
			removeInstancesOf(index);

			startIsNewNode = false;
		}
		if (endIsNewNode) {
			int index = nodeList.size() - 1;
			nodeIndex[ey][ex] = -1;
			nodeList.remove(index);
			outgoingEdgeList.remove(index);
			removeInstancesOf(index);

			endIsNewNode = false;
		}
	}

	protected final void removeInstancesOf(int index) {
		for (ArrayList<Edge> edgeList : outgoingEdgeList) {
			Edge edge = new Edge(0, index, 0);
			edgeList.remove(edge);
		}
	}

	private void addStartAndEnd(int sx, int sy, int ex, int ey) {
		if (isNode(sx, sy)) {
			startIndex = indexOf(sx, sy);
			startIsNewNode = false;
		} else {
			startIndex = nodeIndex[sy][sx] = assignNodeAndConnect(sx, sy);
			startIsNewNode = true;
		}

		if (isNode(ex, ey)) {
			endIndex = indexOf(ex, ey);
			endIsNewNode = false;
		} else {
			endIndex = nodeIndex[ey][ex] = assignNodeAndConnect(ex, ey);
			endIsNewNode = true;
		}
	}

	private void addAllEdges() {
		int saveFactor = nodeList.size() / 10;
		if (saveFactor == 0) saveFactor = 1;

		for (int i = 0; i < nodeList.size(); i++) {
			Point fromPoint = coordinateOf(i);
			for (int j = i + 1; j < nodeList.size(); j++) {
				Point toPoint = coordinateOf(j);
				if (graph.lineOfSight(fromPoint.getX(), fromPoint.getY(), toPoint.getX(), toPoint.getY())) {
					float weight = computeWeight(fromPoint.getX(), fromPoint.getY(), toPoint.getX(), toPoint.getY());
					addEdge(i, j, weight);
					addEdge(j, i, weight);
				}
			}

			if (i % saveFactor == 0)
				maybeSaveSnapshot();
		}
	}

	private float computeWeight(int x1, int y1, int x2, int y2) {
		int dx = x2 - x1;
		int dy = y2 - y1;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	protected final void addEdge(int fromI, int toI, float weight) {
		ArrayList<Edge> edgeList = outgoingEdgeList.get(fromI);
		edgeList.add(new Edge(fromI, toI, weight));
	}

	private int indexOf(int x, int y) {
		return nodeIndex[y][x];
	}

	private boolean isNode(int x, int y) {
		return nodeIndex[y][x] != -1;
	}

	private boolean isCorner(int x, int y) {
		boolean a = graph.isBlocked(x - 1, y - 1);
		boolean b = graph.isBlocked(x, y - 1);
		boolean c = graph.isBlocked(x, y);
		boolean d = graph.isBlocked(x - 1, y);

		return ((!a && !c) || (!d && !b)) && (a || b || c || d);

        /* NOTE
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *
         *  The definition below excludes the above case.
         */

        /*int results = 0;
        if(a)results++;
        if(b)results++;
        if(c)results++;
        if(d)results++;
        return (results == 1);*/
	}

	public Point coordinateOf(int index) {
		return nodeList.get(index);
	}

	public int size() {
		return nodeList.size();
	}

	public int computeSumDegrees() {
		int sum = 0;
		for (ArrayList<Edge> list : outgoingEdgeList) {
			sum += list.size();
		}
		return sum;
	}

	public Iterator<Edge> edgeIterator(int source) {
		return outgoingEdgeList.get(source).iterator();
	}

	public Edge getEdge(int source, int dest) {

		ArrayList<Edge> edges = outgoingEdgeList.get(source);
		for (Edge edge : edges) {
			if (edge.dest == dest) {
				return edge;
			}
		}
		return new Edge(source, dest, Float.POSITIVE_INFINITY);
	}

	public int startNode() {
		return startIndex;
	}

	public int endNode() {
		return endIndex;
	}

	private void maybeSaveSnapshot() {
		if (saveSnapshot != null)
			saveSnapshot.run();
	}


}