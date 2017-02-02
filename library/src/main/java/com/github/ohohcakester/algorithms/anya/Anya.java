package com.github.ohohcakester.algorithms.anya;

import com.github.ohohcakester.algorithms.PathFindingAlgorithm;
import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.datatypes.SnapshotItem;
import com.github.ohohcakester.priorityqueue.FastVariableSizeIndirectHeap;
import com.github.ohohcakester.grid.GridGraph;
import kotlin.NotImplementedError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Anya extends PathFindingAlgorithm {

	private static int[][] rightDownExtents;
	private static int[][] leftDownExtents;
	private AnyaState goalState;
	private AnyaState[] states;
	private FastVariableSizeIndirectHeap pq;
	private HashMap<AnyaState, Integer> existingStates;

	public Anya(GridGraph graph, int sx, int sy, int ex, int ey) {
		super(graph, graph.getSizeX(), graph.getSizeY(), sx, sy, ex, ey);
	}

	public static void initialiseUpExtents(GridGraph graph) {
		// Don't reinitialise if graph is the same size as the last time.
		if (rightDownExtents != null && graph.getSizeY() + 2 == rightDownExtents.length && graph.getSizeX() + 1 == rightDownExtents[0].length)
			return;

		rightDownExtents = new int[graph.getSizeY() + 2][];
		leftDownExtents = new int[graph.getSizeY() + 2][];
		for (int y = 0; y < graph.getSizeY() + 2; ++y) {
			rightDownExtents[y] = new int[graph.getSizeX() + 1];
			leftDownExtents[y] = new int[graph.getSizeX() + 1];
		}
	}

	@Override
	public void computePath() {
		existingStates = new HashMap<>();
		pq = new FastVariableSizeIndirectHeap();
		states = new AnyaState[11];
		goalState = null;

		computeExtents();
		generateStartingStates();

		while (!pq.isEmpty()) {
			maybeSaveSearchSnapshot();
			int currentID = pq.popMinIndex();
			AnyaState currState = states[currentID];
			currState.setVisited(true);

			//System.out.println("Explore " + currState + " :: " + currState.fValue);
			// Check if goal state.
			if (currState.getY() == getEy() && currState.getXL().isLessThanOrEqual(getEx()) && !currState.getXR().isLessThan(getEx())) {
				goalState = currState;
				break;
			}

			generateSuccessors(currState);
		}
	}

	private void generateStartingStates() {
		boolean bottomLeftOfBlocked = getGraph().bottomLeftOfBlockedTile(getSx(), getSy());
		boolean bottomRightOfBlocked = getGraph().bottomRightOfBlockedTile(getSx(), getSy());
		boolean topLeftOfBlocked = getGraph().topLeftOfBlockedTile(getSx(), getSy());
		boolean topRightOfBlocked = getGraph().topRightOfBlockedTile(getSx(), getSy());

		Point start = new Point(getSx(), getSy());

		// Generate up
		if (!bottomLeftOfBlocked || !bottomRightOfBlocked) {
			Fraction leftExtent, rightExtent;

			if (bottomLeftOfBlocked) {
				// Explore up-left
				leftExtent = new Fraction(leftUpExtent(getSx(), getSy()));
				rightExtent = new Fraction(getSx());
			} else if (bottomRightOfBlocked) {
				// Explore up-right
				leftExtent = new Fraction(getSx());
				rightExtent = new Fraction(rightUpExtent(getSx(), getSy()));
			} else {
				// Explore up-left-right
				leftExtent = new Fraction(leftUpExtent(getSx(), getSy()));
				rightExtent = new Fraction(rightUpExtent(getSx(), getSy()));
			}

			this.generateUpwardsStart(leftExtent, rightExtent, start);
		}

		// Generate down
		if (!topLeftOfBlocked || !topRightOfBlocked) {
			Fraction leftExtent, rightExtent;

			if (topLeftOfBlocked) {
				// Explore down-left
				leftExtent = new Fraction(leftDownExtent(getSx(), getSy()));
				rightExtent = new Fraction(getSx());
			} else if (topRightOfBlocked) {
				// Explore down-right
				leftExtent = new Fraction(getSx());
				rightExtent = new Fraction(rightDownExtent(getSx(), getSy()));
			} else {
				// Explore down-left-right
				leftExtent = new Fraction(leftDownExtent(getSx(), getSy()));
				rightExtent = new Fraction(rightDownExtent(getSx(), getSy()));
			}

			this.generateDownwardsStart(leftExtent, rightExtent, start);
		}

		// Generate left
		if (!topRightOfBlocked || !bottomRightOfBlocked) {
			this.generateSameLevelStart(start, leftAnyExtent(getSx(), getSy()), getSx());
		}

		// Generate right
		if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
			this.generateSameLevelStart(start, getSx(), rightAnyExtent(getSx(), getSy()));
		}
	}

	private void addSuccessor(AnyaState source, AnyaState successor) {
		Integer existingHandle = existingStates.get(successor);
		if (existingHandle == null) {
			addToOpen(successor);
		} else {
			relaxExisting(source, successor, existingHandle);
		}
		//maybeSaveSearchSnapshot();
	}

	private void addToOpen(AnyaState successor) {
		// set heuristic and f-value
		successor.setHValue(heuristic(successor));

		int handle = pq.insert(successor.getFValue());
		if (handle >= states.length) {
			states = Arrays.copyOf(states, states.length * 2);
		}
		states[handle] = successor;
		existingStates.put(successor, handle);

		//System.out.println("Generate " + successor + " -> " + handle);
	}

	private void relaxExisting(AnyaState source, AnyaState successorCopy, int existingHandle) {
		AnyaState successor = states[existingHandle];
		if (successor.getVisited()) return;

		int dx = successor.getBasePoint().getX() - source.getBasePoint().getX();
		int dy = successor.getBasePoint().getY() - source.getBasePoint().getY();
		float newgValue = source.getGValue() + (float) Math.sqrt(dx * dx + dy * dy);

		if (newgValue < successor.getGValue()) {
			successor.setGValue(newgValue);
			successor.setParent(successorCopy.getParent());
			pq.decreaseKey(existingHandle, successor.getFValue());

			//System.out.println("Relax " + successor + " : " + successor.fValue);
		}
		//else System.out.println("Failed to relax " + successor + ": " + successor.fValue);
	}


	private void computeExtents() {
		// graph.isBlocked(x,y) is the same as graph.bottomLeftOfBlockedTile(x,y)
		Anya.initialiseUpExtents(getGraph());

		for (int y = 0; y < getSizeY() + 2; ++y) {
			boolean lastIsBlocked = true;
			int lastX = -1;
			for (int x = 0; x <= getSizeX(); ++x) {
				leftDownExtents[y][x] = lastX;
				if (getGraph().isBlocked(x, y - 1) != lastIsBlocked) {
					lastX = x;
					lastIsBlocked = !lastIsBlocked;
				}
			}
			lastIsBlocked = true;
			lastX = getSizeX() + 1;
			for (int x = getSizeX(); x >= 0; --x) {
				rightDownExtents[y][x] = lastX;
				if (getGraph().isBlocked(x - 1, y - 1) != lastIsBlocked) {
					lastX = x;
					lastIsBlocked = !lastIsBlocked;
				}
			}
		}
	}


	/// === GENERATE SUCCESSORS - PATTERNS - START ===

	private void generateSuccessors(AnyaState currState) {
		Point basePoint = currState.getBasePoint();

		if (basePoint.getY() == currState.getY()) {
			exploreFromSameLevel(currState, basePoint);
		} else if (basePoint.getY() < currState.getY()) {
			explorefromBelow(currState, basePoint);
		} else {
			explorefromAbove(currState, basePoint);
		}
	}

	private void exploreFromSameLevel(AnyaState currState, Point basePoint) {
		// Note: basePoint.y == currState.y
		// Note: basePoint == currState.basePoint
		// Property 1: basePoint is not strictly between the two endpoints of the interval.
		// Property 2: the endpoints of the interval are integers.

		assert basePoint.getY() == currState.getY();
		assert currState.getXL().isWholeNumber();
		assert currState.getXR().isWholeNumber();

		int y = basePoint.getY();

		if (currState.getXR().getN() <= basePoint.getX()) { // currState.xR <= point.x  (explore left)
			int xL = currState.getXL().getN();
			if (getGraph().bottomLeftOfBlockedTile(xL, y)) {
				if (!getGraph().bottomRightOfBlockedTile(xL, y)) {
	                /* ----- |XXXXXXXX|
                     *       |XXXXXXXX|
                     * ----- P========B
                     */
					Fraction leftBound = new Fraction(leftUpExtent(xL, y));
					generateUpwardsUnobservable(new Point(xL, y), leftBound, currState.getXL(), currState);
				}
			} else if (getGraph().topLeftOfBlockedTile(xL, y)) {
				if (!getGraph().topRightOfBlockedTile(xL, y)) {
                    /* ----- P========B
                     *       |XXXXXXXX|
                     * ----- |XXXXXXXX|
                     */
					Fraction leftBound = new Fraction(leftDownExtent(xL, y));
					generateDownwardsUnobservable(new Point(xL, y), leftBound, currState.getXL(), currState);
				}
			}

			if (!getGraph().bottomRightOfBlockedTile(xL, y) || !getGraph().topRightOfBlockedTile(xL, y)) {
				int leftBound = leftAnyExtent(xL, y);
				generateSameLevelObservable(leftBound, xL, currState);
			}

		} else { // point.x <= currState.xL  (explore right)
			assert basePoint.getX() <= currState.getXL().getN();

			int xR = currState.getXR().getN();
			if (getGraph().bottomRightOfBlockedTile(xR, y)) {
				if (!getGraph().bottomLeftOfBlockedTile(xR, y)) {
                    /*  |XXXXXXXX| -----
                     *  |XXXXXXXX|
                     *  B========P -----
                     */
					Fraction rightBound = new Fraction(rightUpExtent(xR, y));
					generateUpwardsUnobservable(new Point(xR, y), currState.getXR(), rightBound, currState);
				}
			} else if (getGraph().topRightOfBlockedTile(xR, y)) {
				if (!getGraph().topLeftOfBlockedTile(xR, y)) {
                    /*  B========P -----
                     *  |XXXXXXXX|
                     *  |XXXXXXXX| -----
                     */
					Fraction rightBound = new Fraction(rightDownExtent(xR, y));
					generateDownwardsUnobservable(new Point(xR, y), currState.getXR(), rightBound, currState);
				}
			}

			if (!getGraph().bottomLeftOfBlockedTile(xR, y) || !getGraph().topLeftOfBlockedTile(xR, y)) {
				int rightBound = rightAnyExtent(xR, y);
				generateSameLevelObservable(xR, rightBound, currState);
			}

		}
	}


	private void explorefromBelow(AnyaState currState, Point basePoint) {
		// Note: basePoint.y < currState.y
		// Note: basePoint == currState.basePoint

		assert basePoint.getY() < currState.getY();

		if (getGraph().bottomLeftOfBlockedTile(currState.getXL().floor(), currState.getY())) {
			// Is Blocked Above
			if (currState.getXL().isWholeNumber()) {
				int xL = currState.getXL().getN();
				if (xL < basePoint.getX() && !getGraph().bottomRightOfBlockedTile(xL, currState.getY())) {
                    /* 
                     * .-----|XXXXXXX
                     *  '.   |XXXXXXXX
                     *    '. |XXXXXXXX
                     *      'P========
                     *        '.    ? 
                     *          '. ? 
                     *            B  
                     */

					// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
					int dy = currState.getY() - basePoint.getY();
					Fraction leftProjection = new Fraction((xL - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());

					int leftBound = leftUpExtent(xL, currState.getY());
					if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
						leftProjection = new Fraction(leftBound);
					}

					generateUpwardsUnobservable(new Point(xL, currState.getY()), leftProjection, currState.getXL(), currState);
				}
			}

			if (currState.getXR().isWholeNumber()) {
				int xR = currState.getXR().getN();
				if (basePoint.getX() < xR && !getGraph().bottomLeftOfBlockedTile(xR, currState.getY())) {
                    /* 
                     *  XXXXXXX|-----.
                     * XXXXXXXX|   .'
                     * XXXXXXXX| .'
                     * ========P' 
                     *  ?    .'
                     *   ? .'
                     *    B
                     */

					// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
					int dy = currState.getY() - basePoint.getY();
					Fraction rightProjection = new Fraction((xR - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());

					int rightBound = rightUpExtent(xR, currState.getY());
					if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
						rightProjection = new Fraction(rightBound);
					}

					generateUpwardsUnobservable(new Point(xR, currState.getY()), currState.getXR(), rightProjection, currState);
				}
			}


		} else {
			// Is not Blocked Above
            /*
             * =======      =====    =====
             *  \   /       / .'      '. \
             *   \ /   OR  /.'    OR    '.\
             *    B       B                B
             */

			// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
			int dy = currState.getY() - basePoint.getY();
			Fraction leftProjection = currState.getXL().minus(basePoint.getX()).multiplyDivide(dy + 1, dy).plus(basePoint.getX());

			int leftBound = leftUpExtent(currState.getXL().floor() + 1, currState.getY());
			if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
				leftProjection = new Fraction(leftBound);
			}

			// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
			Fraction rightProjection = currState.getXR().minus(basePoint.getX()).multiplyDivide(dy + 1, dy).plus(basePoint.getX());

			int rightBound = rightUpExtent(currState.getXR().ceil() - 1, currState.getY());
			if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
				rightProjection = new Fraction(rightBound);
			}

			if (leftProjection.isLessThan(rightProjection)) {
				generateUpwardsObservable(leftProjection, rightProjection, currState);
			}
		}


		if (currState.getXL().isWholeNumber()) {
			int xL = currState.getXL().getN();
			if (getGraph().topRightOfBlockedTile(xL, currState.getY()) && !getGraph().bottomRightOfBlockedTile(xL, currState.getY())) {
                /*
                 * .------P======
                 * |XXXXXX|\   /
                 * |XXXXXX| \ /
                 *           B
                 */
				Point pivot = new Point(xL, currState.getY());

				{
					int leftBound = leftAnyExtent(xL, currState.getY());
					generateSameLevelUnobservable(pivot, leftBound, xL, currState);
				}

				{
					int dy = currState.getY() - basePoint.getY();
					Fraction leftProjection = new Fraction((xL - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());

					int leftBound = leftUpExtent(xL, currState.getY());
					if (!leftProjection.isLessThanOrEqual(leftBound)) { // leftBound < leftProjection
						this.generateUpwardsUnobservable(pivot, new Fraction(leftBound), leftProjection, currState);
					}
				}
			}
		}

		if (currState.getXR().isWholeNumber()) {
			int xR = currState.getXR().getN();
			if (getGraph().topLeftOfBlockedTile(xR, currState.getY()) && !getGraph().bottomLeftOfBlockedTile(xR, currState.getY())) {
                /*
                 * ======P------.
                 *  \   /|XXXXXX|
                 *   \ / |XXXXXX|
                 *    B
                 */
				Point pivot = new Point(xR, currState.getY());

				{
					int rightBound = rightAnyExtent(xR, currState.getY());
					generateSameLevelUnobservable(new Point(xR, currState.getY()), xR, rightBound, currState);
				}

				{
					int dy = currState.getY() - basePoint.getY();
					Fraction rightProjection = new Fraction((xR - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());
					int rightBound = rightUpExtent(xR, currState.getY());
					if (rightProjection.isLessThan(rightBound)) { // rightProjection < rightBound
						this.generateUpwardsUnobservable(pivot, rightProjection, new Fraction(rightBound), currState);
					}
				}
			}
		}
	}

	private void explorefromAbove(AnyaState currState, Point basePoint) {
		// Note: basePoint.y > currState.y
		// Note: basePoint == currState.basePoint

		assert basePoint.getY() > currState.getY();

		if (getGraph().topLeftOfBlockedTile(currState.getXL().floor(), currState.getY())) {
			// Is Blocked Below
			if (currState.getXL().isWholeNumber()) {
				int xL = currState.getXL().getN();
				if (xL < basePoint.getX() && !getGraph().topRightOfBlockedTile(xL, currState.getY())) {
                    /* 
                     *            B  
                     *          .' ? 
                     *        .'    ? 
                     *      .P========
                     *    .' |XXXXXXXX
                     *  .'   |XXXXXXXX
                     * '-----|XXXXXXX
                     */

					// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
					int dy = basePoint.getY() - currState.getY();
					Fraction leftProjection = new Fraction((xL - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());

					int leftBound = leftDownExtent(xL, currState.getY());
					if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
						leftProjection = new Fraction(leftBound);
					}

					generateDownwardsUnobservable(new Point(xL, currState.getY()), leftProjection, currState.getXL(), currState);
				}
			}

			if (currState.getXR().isWholeNumber()) {
				int xR = currState.getXR().getN();
				if (basePoint.getX() < xR && !getGraph().topLeftOfBlockedTile(xR, currState.getY())) {
                    /* 
                     *    B
                     *   ? '.
                     *  ?    '.
                     * ========P. 
                     * XXXXXXXX| '.
                     * XXXXXXXX|   '.
                     *  XXXXXXX|-----'
                     */

					// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
					int dy = basePoint.getY() - currState.getY();
					Fraction rightProjection = new Fraction((xR - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());

					int rightBound = rightDownExtent(xR, currState.getY());
					if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
						rightProjection = new Fraction(rightBound);
					}

					generateDownwardsUnobservable(new Point(xR, currState.getY()), currState.getXR(), rightProjection, currState);
				}
			}

		} else {
			// Is not Blocked Below
            /*
             *    B       B                B
             *   / \   OR  \'.    OR    .'/
             *  /   \       \ '.      .' /
             * =======      =====    =====
             */

			// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
			int dy = basePoint.getY() - currState.getY();
			Fraction leftProjection = currState.getXL().minus(basePoint.getX()).multiplyDivide(dy + 1, dy).plus(basePoint.getX());

			int leftBound = leftDownExtent(currState.getXL().floor() + 1, currState.getY());
			if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
				leftProjection = new Fraction(leftBound);
			}

			// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
			Fraction rightProjection = currState.getXR().minus(basePoint.getX()).multiplyDivide(dy + 1, dy).plus(basePoint.getX());

			int rightBound = rightDownExtent(currState.getXR().ceil() - 1, currState.getY());
			if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
				rightProjection = new Fraction(rightBound);
			}

			if (leftProjection.isLessThan(rightProjection)) {
				generateDownwardsObservable(leftProjection, rightProjection, currState);
			}
		}


		if (currState.getXL().isWholeNumber()) {
			int xL = currState.getXL().getN();
			if (getGraph().bottomRightOfBlockedTile(xL, currState.getY()) && !getGraph().topRightOfBlockedTile(xL, currState.getY())) {
                /*
                 *           B
                 * |XXXXXX| / \
                 * |XXXXXX|/   \
                 * '------P======
                 */
				Point pivot = new Point(xL, currState.getY());

				{
					int leftBound = leftAnyExtent(xL, currState.getY());
					generateSameLevelUnobservable(pivot, leftBound, xL, currState);
				}

				{
					int dy = basePoint.getY() - currState.getY();
					Fraction leftProjection = new Fraction((xL - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());

					int leftBound = leftDownExtent(xL, currState.getY());
					if (!leftProjection.isLessThanOrEqual(leftBound)) { // leftBound < leftProjection
						this.generateDownwardsUnobservable(pivot, new Fraction(leftBound), leftProjection, currState);
					}
				}
			}
		}

		if (currState.getXR().isWholeNumber()) {
			int xR = currState.getXR().getN();
			if (getGraph().bottomLeftOfBlockedTile(xR, currState.getY()) && !getGraph().topLeftOfBlockedTile(xR, currState.getY())) {
                /*
                 *    B
                 *   / \ |XXXXXX|
                 *  /   \|XXXXXX|
                 * ======P------'
                 */
				Point pivot = new Point(xR, currState.getY());

				{
					int rightBound = rightAnyExtent(xR, currState.getY());
					generateSameLevelUnobservable(new Point(xR, currState.getY()), xR, rightBound, currState);
				}

				{
					int dy = basePoint.getY() - currState.getY();
					Fraction rightProjection = new Fraction((xR - basePoint.getX()) * (dy + 1), dy).plus(basePoint.getX());
					int rightBound = rightDownExtent(xR, currState.getY());
					if (rightProjection.isLessThan(rightBound)) { // rightProjection < rightBound
						this.generateDownwardsUnobservable(pivot, rightProjection, new Fraction(rightBound), currState);
					}
				}
			}
		}
	}

	/// === GENERATE SUCCESSORS - PATTERNS - END ===

	/// === GENERATE SUCCESSORS - UTILITY - START ===

	private int leftUpExtent(int xL, int y) {
		return leftDownExtents[y + 1][xL];
	}

	private int leftDownExtent(int xL, int y) {
		return leftDownExtents[y][xL];
	}

	private int leftAnyExtent(int xL, int y) {
		return Math.max(leftDownExtents[y][xL], leftDownExtents[y + 1][xL]);
	}

	private int rightUpExtent(int xR, int y) {
		return rightDownExtents[y + 1][xR];
	}

	private int rightDownExtent(int xR, int y) {
		return rightDownExtents[y][xR];
	}

	private int rightAnyExtent(int xR, int y) {
		return Math.min(rightDownExtents[y][xR], rightDownExtents[y + 1][xR]);
	}

	/**
	 * Can be used for exploreLeftwards or exploreRightwards.
	 * This function will not split intervals.
	 */
	private void generateSameLevelObservable(int leftBound, int rightBound, AnyaState source) {
		addSuccessor(source,
				AnyaState.Companion.createObservableSuccessor(new Fraction(leftBound), new Fraction(rightBound), source.getY(), source));
	}

	/**
	 * Can be used for exploreLeftwards or exploreRightwards.
	 * This function will not split intervals.
	 */
	private void generateSameLevelUnobservable(Point basePoint, int leftBound, int rightBound, AnyaState source) {
		addSuccessor(source,
				AnyaState.Companion.createUnobservableSuccessor(new Fraction(leftBound), new Fraction(rightBound), source.getY(), basePoint, source));
	}

	/**
	 * Can be used for exploreLeftwards or exploreRightwards.
	 * This function will not split intervals.
	 */
	private void generateSameLevelStart(Point start, int leftBound, int rightBound) {
		addSuccessor(null,
				AnyaState.Companion.createStartState(new Fraction(leftBound), new Fraction(rightBound), start.getY(), start));
	}

	private void generateUpwardsUnobservable(Point basePoint, Fraction leftBound, Fraction rightBound, AnyaState source) {
		generateAndSplitIntervals(
				source.getY() + 2, source.getY() + 1,
				basePoint,
				leftBound, rightBound,
				source);
	}

	private void generateUpwardsObservable(Fraction leftBound, Fraction rightBound, AnyaState source) {
		generateAndSplitIntervals(
				source.getY() + 2, source.getY() + 1,
				null,
				leftBound, rightBound,
				source);
	}

	private void generateUpwardsStart(Fraction leftBound, Fraction rightBound, Point start) {
		generateAndSplitIntervals(
				start.getY() + 2, start.getY() + 1,
				start,
				leftBound, rightBound,
				null);
	}

	private void generateDownwardsUnobservable(Point basePoint, Fraction leftBound, Fraction rightBound, AnyaState source) {
		generateAndSplitIntervals(
				source.getY() - 1, source.getY() - 1,
				basePoint,
				leftBound, rightBound,
				source);
	}

	private void generateDownwardsObservable(Fraction leftBound, Fraction rightBound, AnyaState source) {
		generateAndSplitIntervals(
				source.getY() - 1, source.getY() - 1,
				null,
				leftBound, rightBound,
				source);
	}

	private void generateDownwardsStart(Fraction leftBound, Fraction rightBound, Point start) {
		generateAndSplitIntervals(
				start.getY() - 1, start.getY() - 1,
				start,
				leftBound, rightBound,
				null);
	}

	/**
	 * Called by generateUpwards / Downwards.
	 * basePoint is null if observable. Not null if unobservable.
	 * source is null if start state.
	 * <p>
	 * This is used to avoid repeated code in generateUpwardsUnobservable, generateUpwardsObservable,
	 * // generateDownwardsUnobservable, generateDownwardsObservable, generateDownwardsStart, generateDownwardsStart.
	 */
	private void generateAndSplitIntervals(int checkY, int newY, Point basePoint, Fraction leftBound, Fraction rightBound, AnyaState source) {
		Fraction left = leftBound;
		int leftFloor = left.floor();

		// Divide up the intervals.
		while (true) {
			int right = rightDownExtents[checkY][leftFloor]; // it's actually rightDownExtents for exploreDownwards. (thus we use checkY = currY - 2)
			if (rightBound.isLessThanOrEqual(right)) break; // right < rightBound

			if (basePoint == null) {
				addSuccessor(source, AnyaState.Companion.createObservableSuccessor(left, new Fraction(right), newY, source));
			} else {
				if (source == null) {
					addSuccessor(null, AnyaState.Companion.createStartState(left, new Fraction(right), newY, basePoint));
				} else {
					addSuccessor(source, AnyaState.Companion.createUnobservableSuccessor(left, new Fraction(right), newY, basePoint, source));
				}
			}

			leftFloor = right;
			left = new Fraction(leftFloor);
		}

		if (basePoint == null) {
			addSuccessor(source, AnyaState.Companion.createObservableSuccessor(left, rightBound, newY, source));
		} else {
			if (source == null) {
				addSuccessor(null, AnyaState.Companion.createStartState(left, rightBound, newY, basePoint));
			} else {
				addSuccessor(source, AnyaState.Companion.createUnobservableSuccessor(left, rightBound, newY, basePoint, source));
			}
		}
	}

	/// === GENERATE SUCCESSORS - UTILITY - END ===


	private float heuristic(AnyaState currState) {
		int baseX = currState.getBasePoint().getX();
		int baseY = currState.getBasePoint().getY();
		Fraction xL = currState.getXL();
		Fraction xR = currState.getXR();

		// Special case: base, goal, interval all on same row.
		if (currState.getY() == baseY && currState.getY() == getEy()) {

			// Case 1: base and goal on left of interval.
			// baseX < xL && ex < xL
			if (!xL.isLessThanOrEqual(baseX) && !xL.isLessThanOrEqual(getEx())) {
				return 2 * xL.toFloat() - baseX - getEx(); // (xL-baseX) + (xL-ex);
			}

			// Case 2: base and goal on right of interval.
			// xR < baseX && xR < ex
			else if (xR.isLessThan(baseX) && xR.isLessThan(getEx())) {
				return baseX + getEx() - 2 * xL.toFloat(); // (baseX-xL) + (ex-xL)
			}

			// Case 3: Otherwise, the direct path from base to goal will pass through the interval.
			else {
				return Math.abs(baseX - getEx());
			}
		}


		int dy1 = baseY - currState.getY();
		int dy2 = getEy() - currState.getY();

		// If goal and base on same side of interval, reflect goal about interval -> ey2.
		int ey2 = getEy();
		if (dy1 * dy2 > 0) ey2 = 2 * currState.getY() - getEy();
        
        /*  E
         *   '.
         * ----X----- <--currState.y
         *      '.
         *        B
         */
		// (ey-by)/(ex-bx) = (cy-by)/(cx-bx)
		// cx = bx + (cy-by)(ex-bx)/(ey-by)

		// Find the pivot point on the interval for shortest path from base to goal.
		float intersectX = baseX + (float) (currState.getY() - baseY) * (getEx() - baseX) / (ey2 - baseY);
		float xlf = xL.toFloat();
		float xrf = xR.toFloat();

		// Snap to endpoints of interval if intersectX it lies outside interval.
		if (intersectX < xlf) intersectX = xlf;
		if (intersectX > xrf) intersectX = xrf;

		{
			// Return sum of euclidean distances. (base~intersection~goal)
			float dx1 = intersectX - baseX;
			float dx2 = intersectX - getEx();

			return (float) (Math.sqrt(dx1 * dx1 + dy1 * dy1) + Math.sqrt(dx2 * dx2 + dy2 * dy2));
		}
	}


	private int pathLength() {
		int length = 1;
		AnyaState current = goalState;
		while (current != null) {
			current = current.getParent();
			length++;
		}
		return length;
	}

	@Override
	public int[][] getPath() {
		if (goalState == null) return new int[0][]; // Fail

		// Start from goalState and traverse backwards.
		int length = pathLength();
		int[][] path = new int[length][];
		AnyaState current = goalState;

		path[length - 1] = new int[2];
		path[length - 1][0] = getEx();
		path[length - 1][1] = getEy();

		int index = length - 2;
		while (current != null) {
			path[index] = new int[2];
			path[index][0] = current.getBasePoint().getX();
			path[index][1] = current.getBasePoint().getY();

			index--;
			current = current.getParent();
		}

		return path;
	}

	//@Override
	protected float getPathLength() {
		if (goalState == null) return -1; // Fail

		// Start from goalState and traverse backwards.
		double pathLength = 0;
		int currX = getEx();
		int currY = getEy();
		AnyaState current = goalState;

		while (current != null) {
			int nextX = current.getBasePoint().getX();
			int nextY = current.getBasePoint().getY();

			pathLength += getGraph().distance_double(currX, currY, nextX, nextY);
			current = current.getParent();
			currX = nextX;
			currY = nextY;
		}

		return (float) pathLength;
	}


	@Override
	protected List<SnapshotItem> computeSearchSnapshot() {
		ArrayList<SnapshotItem> list = new ArrayList<>(states.length);

		for (AnyaState in : states) {
			// y, xLn, xLd, xRn, xRd, px, py
			if (in == null) continue;

			Integer[] line = new Integer[7];
			line[0] = in.getY();
			line[1] = in.getXL().getN();
			line[2] = in.getXL().getD();
			line[3] = in.getXR().getN();
			line[4] = in.getXR().getD();
			line[5] = in.getBasePoint().getX();
			line[6] = in.getBasePoint().getY();
			list.add(SnapshotItem.Companion.generate(line));
		}

		if (!pq.isEmpty()) {
			int index = pq.getMinIndex();
			AnyaState in = states[index];

			Integer[] line = new Integer[5];
			line[0] = in.getY();
			line[1] = in.getXL().getN();
			line[2] = in.getXL().getD();
			line[3] = in.getXR().getN();
			line[4] = in.getXR().getD();
			list.add(SnapshotItem.Companion.generate(line));
		}

		return list;
	}

	@Override
	protected int getParent(int index) {
		throw new NotImplementedError();
	}

	@Override
	protected void setParent(int index, int value) {
		throw new NotImplementedError();
	}

	@Override
	protected int getParentSize() {
		throw new NotImplementedError();
	}
}
