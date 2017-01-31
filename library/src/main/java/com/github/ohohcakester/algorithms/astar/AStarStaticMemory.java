package com.github.ohohcakester.algorithms.astar;

import com.github.ohohcakester.algorithms.PathFindingAlgorithm;
import com.github.ohohcakester.algorithms.datatypes.Memory;
import com.github.ohohcakester.algorithms.priorityqueue.ReusableIndirectHeap;
import com.github.ohohcakester.grid.GridGraph;


public class AStarStaticMemory extends PathFindingAlgorithm {

	protected boolean postSmoothingOn = false;
	protected boolean repeatedPostSmooth = false;
	protected float heuristicWeight = 1f;

	protected ReusableIndirectHeap pq;

	protected int finish;


	public AStarStaticMemory(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, graph.getSizeX(), graph.getSizeY(), sx, sy, ex, ey);
	}

	public static AStarStaticMemory postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
		AStarStaticMemory aStar = new AStarStaticMemory(graph, sx, sy, ex, ey);
		aStar.postSmoothingOn = true;
		aStar.repeatedPostSmooth = false;
		return aStar;
	}

	public static AStarStaticMemory repeatedPostSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
		AStarStaticMemory aStar = new AStarStaticMemory(graph, sx, sy, ex, ey);
		aStar.postSmoothingOn = true;
		aStar.repeatedPostSmooth = true;
		return aStar;
	}

	public static AStarStaticMemory dijkstra(GridGraph graph, int sx, int sy, int ex, int ey) {
		AStarStaticMemory aStar = new AStarStaticMemory(graph, sx, sy, ex, ey);
		aStar.heuristicWeight = 0;
		return aStar;
	}

	@Override
	public void computePath() {
		int totalSize = (getGraph().getSizeX() + 1) * (getGraph().getSizeY() + 1);

		int start = toOneDimIndex(getSx(), getSy());
		finish = toOneDimIndex(getEx(), getEy());

		pq = new ReusableIndirectHeap(totalSize);
		this.initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);

		initialise(start);

		float lastDist = -1;
		while (!pq.isEmpty()) {
			float dist = pq.getMinValue();

			int current = pq.popMinIndex();

			if (Math.abs(dist - lastDist) > 0.01f) {
				maybeSaveSearchSnapshot();
				lastDist = dist;
			}

			if (current == finish || distance(current) == Float.POSITIVE_INFINITY) {
				//maybeSaveSearchSnapshot();
				break;
			}
			setVisited(current, true);

			int x = toTwoDimX(current);
			int y = toTwoDimY(current);

			tryRelaxNeighbour(current, x, y, x - 1, y - 1);
			tryRelaxNeighbour(current, x, y, x, y - 1);
			tryRelaxNeighbour(current, x, y, x + 1, y - 1);

			tryRelaxNeighbour(current, x, y, x - 1, y);
			tryRelaxNeighbour(current, x, y, x + 1, y);

			tryRelaxNeighbour(current, x, y, x - 1, y + 1);
			tryRelaxNeighbour(current, x, y, x, y + 1);
			tryRelaxNeighbour(current, x, y, x + 1, y + 1);

			//maybeSaveSearchSnapshot();
		}

		maybePostSmooth();
	}

	protected void tryRelaxNeighbour(int current, int currentX, int currentY, int x, int y) {
		if (!getGraph().isValidCoordinate(x, y))
			return;

		int destination = toOneDimIndex(x, y);
		if (visited(destination))
			return;
		if (!getGraph().neighbourLineOfSight(currentX, currentY, x, y))
			return;

		if (relax(current, destination, weight(currentX, currentY, x, y))) {
			// If relaxation is done.
			pq.decreaseKey(destination, distance(destination) + heuristic(x, y));
		}
	}

	protected float heuristic(int x, int y) {
		//return 0;
		return heuristicWeight * getGraph().distance(x, y, getEx(), getEy());
	}


	protected float weight(int x1, int y1, int x2, int y2) {
		return getGraph().distance(x1, y1, x2, y2);
	}

	protected boolean relax(int u, int v, float weightUV) {
		// return true iff relaxation is done.

		float newWeight = distance(u) + weightUV;
		if (newWeight < distance(v)) {
			setDistance(v, newWeight);
			setParent(v, u);
			//maybeSaveSearchSnapshot();
			return true;
		}
		return false;
	}


	protected final void initialise(int s) {
		pq.decreaseKey(s, 0f);
		Memory.INSTANCE.setDistance(s, 0f);
	}


	private int pathLength() {
		int length = 0;
		int current = finish;
		while (current != -1) {
			current = parent(current);
			length++;
		}
		return length;
	}

	protected boolean lineOfSight(int node1, int node2) {
		int x1 = toTwoDimX(node1);
		int y1 = toTwoDimY(node1);
		int x2 = toTwoDimX(node2);
		int y2 = toTwoDimY(node2);
		return getGraph().lineOfSight(x1, y1, x2, y2);
	}

	protected float physicalDistance(int node1, int node2) {
		int x1 = toTwoDimX(node1);
		int y1 = toTwoDimY(node1);
		int x2 = toTwoDimX(node2);
		int y2 = toTwoDimY(node2);
		return getGraph().distance(x1, y1, x2, y2);
	}

	protected float physicalDistance(int x1, int y1, int node2) {
		int x2 = toTwoDimX(node2);
		int y2 = toTwoDimY(node2);
		return getGraph().distance(x1, y1, x2, y2);
	}

	protected void maybePostSmooth() {
		if (postSmoothingOn) {
			if (repeatedPostSmooth) {
				while (postSmooth()) ;
			} else {
				postSmooth();
			}
		}
	}

	private boolean postSmooth() {
		boolean didSomething = false;

		int current = finish;
		while (current != -1) {
			int next = parent(current); // we can skip checking this one as it always has LoS to current.
			if (next != -1) {
				next = parent(next);
				while (next != -1) {
					if (lineOfSight(current, next)) {
						setParent(current, next);
						next = parent(next);
						didSomething = true;
						maybeSaveSearchSnapshot();
					} else {
						next = -1;
					}
				}
			}

			current = parent(current);
		}

		return didSomething;
	}


	public int[][] getPath() {
		int length = pathLength();
		int[][] path = new int[length][];
		int current = finish;

		int index = length - 1;
		while (current != -1) {
			int x = toTwoDimX(current);
			int y = toTwoDimY(current);

			path[index] = new int[2];
			path[index][0] = x;
			path[index][1] = y;

			index--;
			current = parent(current);
		}

		return path;
	}

	@Override
	protected boolean selected(int index) {
		return visited(index);
	}


	protected int parent(int index) {
		return Memory.INSTANCE.parent(index);
	}

	protected void setParent(int index, int value) {
		Memory.INSTANCE.setParent(index, value);
	}

	protected float distance(int index) {
		return Memory.INSTANCE.distance(index);
	}

	protected void setDistance(int index, float value) {
		Memory.INSTANCE.setDistance(index, value);
	}

	protected boolean visited(int index) {
		return Memory.INSTANCE.visited(index);
	}

	protected void setVisited(int index, boolean value) {
		Memory.INSTANCE.setVisited(index, value);
	}
}