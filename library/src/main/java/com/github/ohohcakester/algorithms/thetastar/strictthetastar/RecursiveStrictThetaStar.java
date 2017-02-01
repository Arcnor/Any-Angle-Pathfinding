package com.github.ohohcakester.algorithms.thetastar.strictthetastar;

import com.github.ohohcakester.priorityqueue.ReusableIndirectHeap;
import com.github.ohohcakester.algorithms.thetastar.BasicThetaStar;
import com.github.ohohcakester.grid.GridGraph;

/**
 * An modification of Theta* that I am experimenting with. -Oh
 * <p>
 * ||| Experimental Versions: These versions can be found in source control |||
 * V1: Add buffer value to basic Theta*. that's all. [CURRENT - Strict Theta*]
 * V1b: An attempt to improve V1 using tryLocateTautParent. Not useful.
 * <p>
 * V2: With recursive taut-parent finding.
 * V2b: With collinear point merging to reduce depth of taut-parent searches.
 * [V2c NOT INCLUDED] <-- change the way distance comparison worked during relaxation to not include the buffer value.
 * '-> Did not perform well. Change was reversed.
 * V2d: Identify when buffer value has been added, and removes the buffer value on dequeue from PQ.
 * V2e: Add heuristic and change buffer value. [CURRENT - Recursive Strict Theta*]
 * '-> Amend: Remove Heuristic Trap (described below)
 * <p>
 * V3: Lazy ver <-- did not perform well. Discarded.
 * <p>
 * Ideas:
 * Heuristic trap (No longer used):
 * - The heuristic value of the final node is 1.1f instead of 0.
 * - A lot of inoptimality comes because the algorithm is too eager to relax
 * the final vertex. The slightly higher heuristic encourages the algorithm
 * to explore a little more first.
 */
public class RecursiveStrictThetaStar extends BasicThetaStar {
	private int DEPTH_LIMIT = -1;
	private float BUFFER_VALUE = 0.42f;

	public RecursiveStrictThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, sx, sy, ex, ey);
	}

	public static RecursiveStrictThetaStar setBuffer(GridGraph graph, int sx, int sy, int ex, int ey, float bufferValue) {
		RecursiveStrictThetaStar algo = new RecursiveStrictThetaStar(graph, sx, sy, ex, ey);
		algo.BUFFER_VALUE = bufferValue;
		return algo;
	}

	public static RecursiveStrictThetaStar noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
		RecursiveStrictThetaStar algo = new RecursiveStrictThetaStar(graph, sx, sy, ex, ey);
		algo.setHeuristicWeight(0f);
		return algo;
	}

	public static RecursiveStrictThetaStar depthLimit(GridGraph graph, int sx, int sy, int ex, int ey, int depthLimit) {
		RecursiveStrictThetaStar algo = new RecursiveStrictThetaStar(graph, sx, sy, ex, ey);
		algo.DEPTH_LIMIT = depthLimit;
		return algo;
	}

	public static RecursiveStrictThetaStar postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
		RecursiveStrictThetaStar algo = new RecursiveStrictThetaStar(graph, sx, sy, ex, ey);
		algo.setPostSmoothingOn(true);
		return algo;
	}

	@Override
	public void computePath() {
		int totalSize = (getGraph().getSizeX() + 1) * (getGraph().getSizeY() + 1);

		int start = toOneDimIndex(getSx(), getSy());
		setFinish(toOneDimIndex(getEx(), getEy()));

		setPq(new ReusableIndirectHeap(totalSize));
		this.initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);

		initialise(start);

		while (!getPq().isEmpty()) {
			int current = getPq().popMinIndex();
			tryFixBufferValue(current);

			if (current == getFinish() || distance(current) == Float.POSITIVE_INFINITY) {
				maybeSaveSearchSnapshot();
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

			maybeSaveSearchSnapshot();
		}

		maybePostSmooth();
	}

	protected float heuristic(int x, int y) {
		return getHeuristicWeight() * getGraph().distance(x, y, getEx(), getEy());

		// MOD 2 :: Increased Goal Heuristic - Not needed when a Penalty value of 0.42 is used.
	    /*if (x == ex && y == ey) {
            return 0.18f; // 0.18f
        } else { 
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }*/
	}

	private void tryFixBufferValue(int current) {
		if (getParent(current) < 0 && getParent(current) != -1) {
			setParent(current, getParent(current) - Integer.MIN_VALUE);
			setDistance(current, distance(getParent(current)) + physicalDistance(current, getParent(current)));
		}
	}


	protected void tryRelaxNeighbour(int current, int currentX, int currentY, int x, int y) {
		if (!getGraph().isValidCoordinate(x, y))
			return;

		int destination = toOneDimIndex(x, y);
		if (visited(destination))
			return;
		if (getParent(current) != -1 && getParent(current) == getParent(destination)) // OPTIMISATION: [TI]
			return; // Idea: don't bother trying to relax if parents are equal. using triangle inequality.
		if (!getGraph().neighbourLineOfSight(currentX, currentY, x, y))
			return;

		if (relax(current, destination, weight(currentX, currentY, x, y))) {
			// If relaxation is done.
			getPq().decreaseKey(destination, distance(destination) + heuristic(x, y));
		}
	}

	@Override
	protected boolean relax(int u, int v, float weightUV) {
		// return true iff relaxation is done.
		return tautRelax(u, v, DEPTH_LIMIT);
	}

	// This gives very good paths... but the recursion level is too deep.
	private boolean tautRelax(int u, int v, int depth) {
		if (isTaut(v, u)) {
			return tryRelaxVertex(u, v, false);
		} else {
			int par = getParent(u);
			if (lineOfSight(par, v)) {
				if (depth == 0) {
					return tryRelaxVertex(par, v, !isTaut(v, par));
				}
				return tautRelax(par, v, depth - 1);
			} else {
				return tryRelaxVertex(u, v, true);
			}
		}
	}

	private boolean tryRelaxVertex(int u, int v, boolean addBuffer) {
		int newParent = u;
		float newWeight = distance(u) + physicalDistance(u, v);
		if (addBuffer) {
			newWeight += BUFFER_VALUE;
			newParent += Integer.MIN_VALUE;
		}
		if (newWeight < distance(v)) {
			if (isMergeableWithParent(u, v)) {
				newParent = getParent(u);
			}
			setDistance(v, newWeight);
			setParent(v, newParent);
			return true;
		}
		return false;
	}

	// If getParent(u),u,v collinear, remove u from path, except when u is at an outer corner.
	private boolean isMergeableWithParent(int u, int v) {
		if (u == -1) return false;
		int p = getParent(u);
		if (p == -1) return false; // u is start point.
		int ux = toTwoDimX(u);
		int uy = toTwoDimY(u);
		if (isOuterCorner(ux, uy)) return false; // u is outer corner

		int vx = toTwoDimX(v);
		int vy = toTwoDimY(v);
		int px = toTwoDimX(p);
		int py = toTwoDimY(p);

		return isCollinear(px, py, ux, uy, vx, vy);
	}

	protected final boolean isOuterCorner(int x, int y) {
		boolean a = getGraph().isBlocked(x - 1, y - 1);
		boolean b = getGraph().isBlocked(x, y - 1);
		boolean c = getGraph().isBlocked(x, y);
		boolean d = getGraph().isBlocked(x - 1, y);

		return ((!a && !c) || (!d && !b)) && (a || b || c || d);
        
        /* NOTE:
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *  
         *  The definition below excludes the above case.
         */
	}

	protected final boolean isCollinear(int x1, int y1, int x2, int y2, int x3, int y3) {
		// (y2-y1)/(x2-x1) == (y3-y2)/(x3-x2)
		// <=>
		return (y2 - y1) * (x3 - x2) == (y3 - y2) * (x2 - x1);
	}


	/**
	 * Checks whether the path v, u, p=getParent(u) is taut.
	 */
	private boolean isTaut(int v, int u) {
		int p = getParent(u); // assert u != -1
		if (p == -1) return true;
		int x1 = toTwoDimX(v);
		int y1 = toTwoDimY(v);
		int x2 = toTwoDimX(u);
		int y2 = toTwoDimY(u);
		int x3 = toTwoDimX(p);
		int y3 = toTwoDimY(p);
		return getGraph().isTaut(x1, y1, x2, y2, x3, y3);
	}
}
