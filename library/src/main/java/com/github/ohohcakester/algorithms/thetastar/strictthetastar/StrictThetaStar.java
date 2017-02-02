package com.github.ohohcakester.algorithms.thetastar.strictthetastar;

import com.github.ohohcakester.algorithms.astarstatic.thetastar.strict.AbstractStrictThetaStar;
import com.github.ohohcakester.grid.GridGraph;

/**
 * An modification of Theta* that I am experimenting with. -Oh
 *
 * @author Oh
 *         <p>
 *         Ideas:
 *         Heuristic trap:
 *         - The heuristic value of the final node is 1.1f instead of 0.
 *         - A lot of inoptimality comes because the algorithm is too eager to relax
 *         the final vertex. The slightly higher heuristic encourages the algorithm
 *         to explore a little more first.
 */
public class StrictThetaStar extends AbstractStrictThetaStar {
	private float BUFFER_VALUE = 0.42f;

	public StrictThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, sx, sy, ex, ey);
	}

	public static StrictThetaStar setBuffer(GridGraph graph, int sx, int sy, int ex, int ey, float bufferValue) {
		StrictThetaStar algo = new StrictThetaStar(graph, sx, sy, ex, ey);
		algo.BUFFER_VALUE = bufferValue;
		return algo;
	}

	public static StrictThetaStar noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
		StrictThetaStar algo = new StrictThetaStar(graph, sx, sy, ex, ey);
		algo.setHeuristicWeight(0f);
		return algo;
	}

	public static StrictThetaStar postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
		StrictThetaStar algo = new StrictThetaStar(graph, sx, sy, ex, ey);
		algo.setPostSmoothingOn(true);
		return algo;
	}

	protected float heuristic(int x, int y) {
		return getHeuristicWeight() * getGraph().distance(x, y, getEx(), getEy());

		// MOD 2 :: Increased Goal Heuristic - Not needed when a Penalty value of 0.42 is used.
	    /*if (x == ex && y == ey) {
            return 1.1f;
        } else { 
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }*/
	}

	@Override
	protected boolean relax(int u, int v, float weightUV) {
		// return true iff relaxation is done.
		int par = getParent(u);
		if (lineOfSight(getParent(u), v)) {
			float newWeight = distance(par) + physicalDistance(par, v);
			return relaxTarget(v, par, newWeight);
		} else {
			float newWeight = distance(u) + physicalDistance(u, v);
			return relaxTarget(v, u, newWeight);
		}
	}

	private boolean relaxTarget(int v, int par, float newWeight) {
		if (newWeight < distance(v)) {
			if (!isTaut(v, par)) {
				newWeight += BUFFER_VALUE;
				par += Integer.MIN_VALUE;
			}
			setDistance(v, newWeight);
			setParent(v, par);
			return true;
		}
		return false;
	}
}
