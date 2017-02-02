package com.github.ohohcakester.algorithms.anya

import com.github.ohohcakester.algorithms.PathFindingAlgorithm
import com.github.ohohcakester.datatypes.Point
import com.github.ohohcakester.datatypes.SnapshotItem
import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.FastVariableSizeIndirectHeap
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

class Anya(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : PathFindingAlgorithm(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey) {
	companion object {
		private var rightDownExtents: Array<IntArray>? = null
		private var leftDownExtents: Array<IntArray>? = null

		fun initialiseUpExtents(graph: GridGraph) {
			// Don't reinitialise if graph is the same size as the last time.
			if (rightDownExtents != null && graph.sizeY + 2 == rightDownExtents!!.size && graph.sizeX + 1 == rightDownExtents!![0].size)
				return

			rightDownExtents = Array(graph.sizeY + 2) { IntArray(graph.sizeX + 1) }
			leftDownExtents = Array(graph.sizeY + 2) { IntArray(graph.sizeX + 1) }
		}
	}

	private var goalState: AnyaState? = null
	private var states = arrayOfNulls<AnyaState>(11)
	private var pq: FastVariableSizeIndirectHeap? = null
	private var existingStates: HashMap<AnyaState, Int>? = null

	override fun computePath() {
		existingStates = HashMap<AnyaState, Int>()
		pq = FastVariableSizeIndirectHeap()
		states.fill(null)
		goalState = null

		computeExtents()
		generateStartingStates()

		while (!pq!!.isEmpty) {
			maybeSaveSearchSnapshot()
			val currentID = pq!!.popMinIndex()
			val currState = states[currentID]
			currState!!.visited = true

			//System.out.println("Explore " + currState + " :: " + currState.fValue);
			// Check if goal state.
			if (currState.y == ey && currState.xL.isLessThanOrEqual(ex) && !currState.xR.isLessThan(ex)) {
				goalState = currState
				break
			}

			generateSuccessors(currState)
		}
	}

	private fun generateStartingStates() {
		val bottomLeftOfBlocked = graph.bottomLeftOfBlockedTile(sx, sy)
		val bottomRightOfBlocked = graph.bottomRightOfBlockedTile(sx, sy)
		val topLeftOfBlocked = graph.topLeftOfBlockedTile(sx, sy)
		val topRightOfBlocked = graph.topRightOfBlockedTile(sx, sy)

		val start = Point(sx, sy)

		// Generate up
		if (!bottomLeftOfBlocked || !bottomRightOfBlocked) {
			val leftExtent: Fraction
			val rightExtent: Fraction

			if (bottomLeftOfBlocked) {
				// Explore up-left
				leftExtent = Fraction(leftUpExtent(sx, sy))
				rightExtent = Fraction(sx)
			} else if (bottomRightOfBlocked) {
				// Explore up-right
				leftExtent = Fraction(sx)
				rightExtent = Fraction(rightUpExtent(sx, sy))
			} else {
				// Explore up-left-right
				leftExtent = Fraction(leftUpExtent(sx, sy))
				rightExtent = Fraction(rightUpExtent(sx, sy))
			}

			this.generateUpwardsStart(leftExtent, rightExtent, start)
		}

		// Generate down
		if (!topLeftOfBlocked || !topRightOfBlocked) {
			val leftExtent: Fraction
			val rightExtent: Fraction

			if (topLeftOfBlocked) {
				// Explore down-left
				leftExtent = Fraction(leftDownExtent(sx, sy))
				rightExtent = Fraction(sx)
			} else if (topRightOfBlocked) {
				// Explore down-right
				leftExtent = Fraction(sx)
				rightExtent = Fraction(rightDownExtent(sx, sy))
			} else {
				// Explore down-left-right
				leftExtent = Fraction(leftDownExtent(sx, sy))
				rightExtent = Fraction(rightDownExtent(sx, sy))
			}

			this.generateDownwardsStart(leftExtent, rightExtent, start)
		}

		// Generate left
		if (!topRightOfBlocked || !bottomRightOfBlocked) {
			this.generateSameLevelStart(start, leftAnyExtent(sx, sy), sx)
		}

		// Generate right
		if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
			this.generateSameLevelStart(start, sx, rightAnyExtent(sx, sy))
		}
	}

	private fun addSuccessor(source: AnyaState?, successor: AnyaState) {
		val existingHandle = existingStates!![successor]
		if (existingHandle == null) {
			addToOpen(successor)
		} else {
			relaxExisting(source!!, successor, existingHandle)
		}
		//maybeSaveSearchSnapshot();
	}

	private fun addToOpen(successor: AnyaState) {
		// set heuristic and f-value
		successor.hValue = heuristic(successor)

		val handle = pq!!.insert(successor.fValue)
		if (handle >= states.size) {
			states = Arrays.copyOf(states, states.size * 2)
		}
		states[handle] = successor
		existingStates!!.put(successor, handle)

		//System.out.println("Generate " + successor + " -> " + handle);
	}

	private fun relaxExisting(source: AnyaState, successorCopy: AnyaState, existingHandle: Int) {
		val successor = states[existingHandle]!!
		if (successor.visited) return

		val dx = successor.basePoint.x - source.basePoint.x
		val dy = successor.basePoint.y - source.basePoint.y
		val newgValue = source.gValue + Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

		if (newgValue < successor.gValue) {
			successor.gValue = newgValue
			successor.parent = successorCopy.parent
			pq!!.decreaseKey(existingHandle, successor.fValue)

			//System.out.println("Relax " + successor + " : " + successor.fValue);
		}
		//else System.out.println("Failed to relax " + successor + ": " + successor.fValue);
	}


	private fun computeExtents() {
		// graph.isBlocked(x,y) is the same as graph.bottomLeftOfBlockedTile(x,y)
		initialiseUpExtents(graph)

		for (y in 0..sizeY + 2 - 1) {
			var lastIsBlocked = true
			var lastX = -1
			for (x in 0..sizeX) {
				leftDownExtents!![y][x] = lastX
				if (graph.isBlocked(x, y - 1) != lastIsBlocked) {
					lastX = x
					lastIsBlocked = !lastIsBlocked
				}
			}
			lastIsBlocked = true
			lastX = sizeX + 1
			for (x in sizeX downTo 0) {
				rightDownExtents!![y][x] = lastX
				if (graph.isBlocked(x - 1, y - 1) != lastIsBlocked) {
					lastX = x
					lastIsBlocked = !lastIsBlocked
				}
			}
		}
	}


	/// === GENERATE SUCCESSORS - PATTERNS - START ===

	private fun generateSuccessors(currState: AnyaState) {
		val basePoint = currState.basePoint

		if (basePoint.y == currState.y) {
			exploreFromSameLevel(currState, basePoint)
		} else if (basePoint.y < currState.y) {
			explorefromBelow(currState, basePoint)
		} else {
			explorefromAbove(currState, basePoint)
		}
	}

	private fun exploreFromSameLevel(currState: AnyaState, basePoint: Point) {
		// Note: basePoint.y == currState.y
		// Note: basePoint == currState.basePoint
		// Property 1: basePoint is not strictly between the two endpoints of the interval.
		// Property 2: the endpoints of the interval are integers.

		assert(basePoint.y == currState.y)
		assert(currState.xL.isWholeNumber)
		assert(currState.xR.isWholeNumber)

		val y = basePoint.y

		if (currState.xR.n <= basePoint.x) { // currState.xR <= point.x  (explore left)
			val xL = currState.xL.n
			if (graph.bottomLeftOfBlockedTile(xL, y)) {
				if (!graph.bottomRightOfBlockedTile(xL, y)) {
					/* ----- |XXXXXXXX|
                     *       |XXXXXXXX|
                     * ----- P========B
                     */
					val leftBound = Fraction(leftUpExtent(xL, y))
					generateUpwardsUnobservable(Point(xL, y), leftBound, currState.xL, currState)
				}
			} else if (graph.topLeftOfBlockedTile(xL, y)) {
				if (!graph.topRightOfBlockedTile(xL, y)) {
					/* ----- P========B
                     *       |XXXXXXXX|
                     * ----- |XXXXXXXX|
                     */
					val leftBound = Fraction(leftDownExtent(xL, y))
					generateDownwardsUnobservable(Point(xL, y), leftBound, currState.xL, currState)
				}
			}

			if (!graph.bottomRightOfBlockedTile(xL, y) || !graph.topRightOfBlockedTile(xL, y)) {
				val leftBound = leftAnyExtent(xL, y)
				generateSameLevelObservable(leftBound, xL, currState)
			}

		} else { // point.x <= currState.xL  (explore right)
			assert(basePoint.x <= currState.xL.n)

			val xR = currState.xR.n
			if (graph.bottomRightOfBlockedTile(xR, y)) {
				if (!graph.bottomLeftOfBlockedTile(xR, y)) {
					/*  |XXXXXXXX| -----
                     *  |XXXXXXXX|
                     *  B========P -----
                     */
					val rightBound = Fraction(rightUpExtent(xR, y))
					generateUpwardsUnobservable(Point(xR, y), currState.xR, rightBound, currState)
				}
			} else if (graph.topRightOfBlockedTile(xR, y)) {
				if (!graph.topLeftOfBlockedTile(xR, y)) {
					/*  B========P -----
                     *  |XXXXXXXX|
                     *  |XXXXXXXX| -----
                     */
					val rightBound = Fraction(rightDownExtent(xR, y))
					generateDownwardsUnobservable(Point(xR, y), currState.xR, rightBound, currState)
				}
			}

			if (!graph.bottomLeftOfBlockedTile(xR, y) || !graph.topLeftOfBlockedTile(xR, y)) {
				val rightBound = rightAnyExtent(xR, y)
				generateSameLevelObservable(xR, rightBound, currState)
			}

		}
	}


	private fun explorefromBelow(currState: AnyaState, basePoint: Point) {
		// Note: basePoint.y < currState.y
		// Note: basePoint == currState.basePoint

		assert(basePoint.y < currState.y)

		if (graph.bottomLeftOfBlockedTile(currState.xL.floor(), currState.y)) {
			// Is Blocked Above
			if (currState.xL.isWholeNumber) {
				val xL = currState.xL.n
				if (xL < basePoint.x && !graph.bottomRightOfBlockedTile(xL, currState.y)) {
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
					val dy = currState.y - basePoint.y
					var leftProjection = Fraction((xL - basePoint.x) * (dy + 1), dy).plus(basePoint.x)

					val leftBound = leftUpExtent(xL, currState.y)
					if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
						leftProjection = Fraction(leftBound)
					}

					generateUpwardsUnobservable(Point(xL, currState.y), leftProjection, currState.xL, currState)
				}
			}

			if (currState.xR.isWholeNumber) {
				val xR = currState.xR.n
				if (basePoint.x < xR && !graph.bottomLeftOfBlockedTile(xR, currState.y)) {
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
					val dy = currState.y - basePoint.y
					var rightProjection = Fraction((xR - basePoint.x) * (dy + 1), dy).plus(basePoint.x)

					val rightBound = rightUpExtent(xR, currState.y)
					if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
						rightProjection = Fraction(rightBound)
					}

					generateUpwardsUnobservable(Point(xR, currState.y), currState.xR, rightProjection, currState)
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
			val dy = currState.y - basePoint.y
			var leftProjection = currState.xL.minus(basePoint.x).multiplyDivide(dy + 1, dy).plus(basePoint.x)

			val leftBound = leftUpExtent(currState.xL.floor() + 1, currState.y)
			if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
				leftProjection = Fraction(leftBound)
			}

			// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
			var rightProjection = currState.xR.minus(basePoint.x).multiplyDivide(dy + 1, dy).plus(basePoint.x)

			val rightBound = rightUpExtent(currState.xR.ceil() - 1, currState.y)
			if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
				rightProjection = Fraction(rightBound)
			}

			if (leftProjection.isLessThan(rightProjection)) {
				generateUpwardsObservable(leftProjection, rightProjection, currState)
			}
		}


		if (currState.xL.isWholeNumber) {
			val xL = currState.xL.n
			if (graph.topRightOfBlockedTile(xL, currState.y) && !graph.bottomRightOfBlockedTile(xL, currState.y)) {
				/*
                 * .------P======
                 * |XXXXXX|\   /
                 * |XXXXXX| \ /
                 *           B
                 */
				val pivot = Point(xL, currState.y)

				run {
					val leftBound = leftAnyExtent(xL, currState.y)
					generateSameLevelUnobservable(pivot, leftBound, xL, currState)
				}

				run {
					val dy = currState.y - basePoint.y
					val leftProjection = Fraction((xL - basePoint.x) * (dy + 1), dy).plus(basePoint.x)

					val leftBound = leftUpExtent(xL, currState.y)
					if (!leftProjection.isLessThanOrEqual(leftBound)) { // leftBound < leftProjection
						this.generateUpwardsUnobservable(pivot, com.github.ohohcakester.algorithms.anya.Fraction(leftBound), leftProjection, currState)
					}
				}
			}
		}

		if (currState.xR.isWholeNumber) {
			val xR = currState.xR.n
			if (graph.topLeftOfBlockedTile(xR, currState.y) && !graph.bottomLeftOfBlockedTile(xR, currState.y)) {
				/*
                 * ======P------.
                 *  \   /|XXXXXX|
                 *   \ / |XXXXXX|
                 *    B
                 */
				val pivot = Point(xR, currState.y)

				run {
					val rightBound = rightAnyExtent(xR, currState.y)
					generateSameLevelUnobservable(Point(xR, currState.y), xR, rightBound, currState)
				}

				run {
					val dy = currState.y - basePoint.y
					val rightProjection = Fraction((xR - basePoint.x) * (dy + 1), dy).plus(basePoint.x)
					val rightBound = rightUpExtent(xR, currState.y)
					if (rightProjection.isLessThan(rightBound)) { // rightProjection < rightBound
						this.generateUpwardsUnobservable(pivot, rightProjection, com.github.ohohcakester.algorithms.anya.Fraction(rightBound), currState)
					}
				}
			}
		}
	}

	private fun explorefromAbove(currState: AnyaState, basePoint: Point) {
		// Note: basePoint.y > currState.y
		// Note: basePoint == currState.basePoint

		assert(basePoint.y > currState.y)

		if (graph.topLeftOfBlockedTile(currState.xL.floor(), currState.y)) {
			// Is Blocked Below
			if (currState.xL.isWholeNumber) {
				val xL = currState.xL.n
				if (xL < basePoint.x && !graph.topRightOfBlockedTile(xL, currState.y)) {
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
					val dy = basePoint.y - currState.y
					var leftProjection = Fraction((xL - basePoint.x) * (dy + 1), dy).plus(basePoint.x)

					val leftBound = leftDownExtent(xL, currState.y)
					if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
						leftProjection = Fraction(leftBound)
					}

					generateDownwardsUnobservable(Point(xL, currState.y), leftProjection, currState.xL, currState)
				}
			}

			if (currState.xR.isWholeNumber) {
				val xR = currState.xR.n
				if (basePoint.x < xR && !graph.topLeftOfBlockedTile(xR, currState.y)) {
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
					val dy = basePoint.y - currState.y
					var rightProjection = Fraction((xR - basePoint.x) * (dy + 1), dy).plus(basePoint.x)

					val rightBound = rightDownExtent(xR, currState.y)
					if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
						rightProjection = Fraction(rightBound)
					}

					generateDownwardsUnobservable(Point(xR, currState.y), currState.xR, rightProjection, currState)
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
			val dy = basePoint.y - currState.y
			var leftProjection = currState.xL.minus(basePoint.x).multiplyDivide(dy + 1, dy).plus(basePoint.x)

			val leftBound = leftDownExtent(currState.xL.floor() + 1, currState.y)
			if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
				leftProjection = Fraction(leftBound)
			}

			// (Px-Bx)*(Py-By+1)/(Py-By) + Bx
			var rightProjection = currState.xR.minus(basePoint.x).multiplyDivide(dy + 1, dy).plus(basePoint.x)

			val rightBound = rightDownExtent(currState.xR.ceil() - 1, currState.y)
			if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
				rightProjection = Fraction(rightBound)
			}

			if (leftProjection.isLessThan(rightProjection)) {
				generateDownwardsObservable(leftProjection, rightProjection, currState)
			}
		}


		if (currState.xL.isWholeNumber) {
			val xL = currState.xL.n
			if (graph.bottomRightOfBlockedTile(xL, currState.y) && !graph.topRightOfBlockedTile(xL, currState.y)) {
				/*
                 *           B
                 * |XXXXXX| / \
                 * |XXXXXX|/   \
                 * '------P======
                 */
				val pivot = Point(xL, currState.y)

				run {
					val leftBound = leftAnyExtent(xL, currState.y)
					generateSameLevelUnobservable(pivot, leftBound, xL, currState)
				}

				run {
					val dy = basePoint.y - currState.y
					val leftProjection = Fraction((xL - basePoint.x) * (dy + 1), dy).plus(basePoint.x)

					val leftBound = leftDownExtent(xL, currState.y)
					if (!leftProjection.isLessThanOrEqual(leftBound)) { // leftBound < leftProjection
						this.generateDownwardsUnobservable(pivot, com.github.ohohcakester.algorithms.anya.Fraction(leftBound), leftProjection, currState)
					}
				}
			}
		}

		if (currState.xR.isWholeNumber) {
			val xR = currState.xR.n
			if (graph.bottomLeftOfBlockedTile(xR, currState.y) && !graph.topLeftOfBlockedTile(xR, currState.y)) {
				/*
                 *    B
                 *   / \ |XXXXXX|
                 *  /   \|XXXXXX|
                 * ======P------'
                 */
				val pivot = Point(xR, currState.y)

				run {
					val rightBound = rightAnyExtent(xR, currState.y)
					generateSameLevelUnobservable(Point(xR, currState.y), xR, rightBound, currState)
				}

				run {
					val dy = basePoint.y - currState.y
					val rightProjection = Fraction((xR - basePoint.x) * (dy + 1), dy).plus(basePoint.x)
					val rightBound = rightDownExtent(xR, currState.y)
					if (rightProjection.isLessThan(rightBound)) { // rightProjection < rightBound
						this.generateDownwardsUnobservable(pivot, rightProjection, com.github.ohohcakester.algorithms.anya.Fraction(rightBound), currState)
					}
				}
			}
		}
	}

	/// === GENERATE SUCCESSORS - PATTERNS - END ===

	/// === GENERATE SUCCESSORS - UTILITY - START ===

	private fun leftUpExtent(xL: Int, y: Int): Int {
		return leftDownExtents!![y + 1][xL]
	}

	private fun leftDownExtent(xL: Int, y: Int): Int {
		return leftDownExtents!![y][xL]
	}

	private fun leftAnyExtent(xL: Int, y: Int): Int {
		return Math.max(leftDownExtents!![y][xL], leftDownExtents!![y + 1][xL])
	}

	private fun rightUpExtent(xR: Int, y: Int): Int {
		return rightDownExtents!![y + 1][xR]
	}

	private fun rightDownExtent(xR: Int, y: Int): Int {
		return rightDownExtents!![y][xR]
	}

	private fun rightAnyExtent(xR: Int, y: Int): Int {
		return Math.min(rightDownExtents!![y][xR], rightDownExtents!![y + 1][xR])
	}

	/**
	 * Can be used for exploreLeftwards or exploreRightwards.
	 * This function will not split intervals.
	 */
	private fun generateSameLevelObservable(leftBound: Int, rightBound: Int, source: AnyaState) {
		addSuccessor(source,
				AnyaState.createObservableSuccessor(Fraction(leftBound), Fraction(rightBound), source.y, source))
	}

	/**
	 * Can be used for exploreLeftwards or exploreRightwards.
	 * This function will not split intervals.
	 */
	private fun generateSameLevelUnobservable(basePoint: Point, leftBound: Int, rightBound: Int, source: AnyaState) {
		addSuccessor(source,
				AnyaState.createUnobservableSuccessor(Fraction(leftBound), Fraction(rightBound), source.y, basePoint, source))
	}

	/**
	 * Can be used for exploreLeftwards or exploreRightwards.
	 * This function will not split intervals.
	 */
	private fun generateSameLevelStart(start: Point, leftBound: Int, rightBound: Int) {
		addSuccessor(null,
				AnyaState.createStartState(Fraction(leftBound), Fraction(rightBound), start.y, start))
	}

	private fun generateUpwardsUnobservable(basePoint: Point, leftBound: Fraction, rightBound: Fraction, source: AnyaState) {
		generateAndSplitIntervals(
				source.y + 2, source.y + 1,
				basePoint,
				leftBound, rightBound,
				source)
	}

	private fun generateUpwardsObservable(leftBound: Fraction, rightBound: Fraction, source: AnyaState) {
		generateAndSplitIntervals(
				source.y + 2, source.y + 1,
				null,
				leftBound, rightBound,
				source)
	}

	private fun generateUpwardsStart(leftBound: Fraction, rightBound: Fraction, start: Point) {
		generateAndSplitIntervals(
				start.y + 2, start.y + 1,
				start,
				leftBound, rightBound,
				null)
	}

	private fun generateDownwardsUnobservable(basePoint: Point, leftBound: Fraction, rightBound: Fraction, source: AnyaState) {
		generateAndSplitIntervals(
				source.y - 1, source.y - 1,
				basePoint,
				leftBound, rightBound,
				source)
	}

	private fun generateDownwardsObservable(leftBound: Fraction, rightBound: Fraction, source: AnyaState) {
		generateAndSplitIntervals(
				source.y - 1, source.y - 1,
				null,
				leftBound, rightBound,
				source)
	}

	private fun generateDownwardsStart(leftBound: Fraction, rightBound: Fraction, start: Point) {
		generateAndSplitIntervals(
				start.y - 1, start.y - 1,
				start,
				leftBound, rightBound,
				null)
	}

	/**
	 * Called by generateUpwards / Downwards.
	 * basePoint is null if observable. Not null if unobservable.
	 * source is null if start state.
	 *
	 *
	 * This is used to avoid repeated code in generateUpwardsUnobservable, generateUpwardsObservable,
	 * // generateDownwardsUnobservable, generateDownwardsObservable, generateDownwardsStart, generateDownwardsStart.
	 */
	private fun generateAndSplitIntervals(checkY: Int, newY: Int, basePoint: Point?, leftBound: Fraction, rightBound: Fraction, source: AnyaState?) {
		var left = leftBound
		var leftFloor = left.floor()

		// Divide up the intervals.
		while (true) {
			val right = rightDownExtents!![checkY][leftFloor] // it's actually rightDownExtents for exploreDownwards. (thus we use checkY = currY - 2)
			if (rightBound.isLessThanOrEqual(right)) break // right < rightBound

			if (basePoint == null) {
				addSuccessor(source, AnyaState.createObservableSuccessor(left, Fraction(right), newY, source!!))
			} else {
				if (source == null) {
					addSuccessor(null, AnyaState.createStartState(left, Fraction(right), newY, basePoint))
				} else {
					addSuccessor(source, AnyaState.createUnobservableSuccessor(left, Fraction(right), newY, basePoint, source))
				}
			}

			leftFloor = right
			left = Fraction(leftFloor)
		}

		if (basePoint == null) {
			addSuccessor(source, AnyaState.createObservableSuccessor(left, rightBound, newY, source!!))
		} else {
			if (source == null) {
				addSuccessor(null, AnyaState.createStartState(left, rightBound, newY, basePoint))
			} else {
				addSuccessor(source, AnyaState.createUnobservableSuccessor(left, rightBound, newY, basePoint, source))
			}
		}
	}

	/// === GENERATE SUCCESSORS - UTILITY - END ===


	private fun heuristic(currState: AnyaState): Float {
		val baseX = currState.basePoint.x
		val baseY = currState.basePoint.y
		val xL = currState.xL
		val xR = currState.xR

		// Special case: base, goal, interval all on same row.
		if (currState.y == baseY && currState.y == ey) {

			// Case 1: base and goal on left of interval.
			// baseX < xL && ex < xL
			if (!xL.isLessThanOrEqual(baseX) && !xL.isLessThanOrEqual(ex)) {
				return 2 * xL.toFloat() - baseX.toFloat() - ex.toFloat() // (xL-baseX) + (xL-ex);
			} else if (xR.isLessThan(baseX) && xR.isLessThan(ex)) {
				return baseX + ex - 2 * xL.toFloat() // (baseX-xL) + (ex-xL)
			} else {
				return Math.abs(baseX - ex).toFloat()
			}// Case 3: Otherwise, the direct path from base to goal will pass through the interval.
			// Case 2: base and goal on right of interval.
			// xR < baseX && xR < ex
		}


		val dy1 = baseY - currState.y
		val dy2 = ey - currState.y

		// If goal and base on same side of interval, reflect goal about interval -> ey2.
		var ey2 = ey
		if (dy1 * dy2 > 0) ey2 = 2 * currState.y - ey

		/*  E
         *   '.
         * ----X----- <--currState.y
         *      '.
         *        B
         */
		// (ey-by)/(ex-bx) = (cy-by)/(cx-bx)
		// cx = bx + (cy-by)(ex-bx)/(ey-by)

		// Find the pivot point on the interval for shortest path from base to goal.
		var intersectX = baseX + (currState.y - baseY).toFloat() * (ex - baseX) / (ey2 - baseY)
		val xlf = xL.toFloat()
		val xrf = xR.toFloat()

		// Snap to endpoints of interval if intersectX it lies outside interval.
		if (intersectX < xlf) intersectX = xlf
		if (intersectX > xrf) intersectX = xrf

		run {
			// Return sum of euclidean distances. (base~intersection~goal)
			val dx1 = intersectX - baseX
			val dx2 = intersectX - ex

			return (Math.sqrt((dx1 * dx1 + dy1 * dy1).toDouble()) + Math.sqrt((dx2 * dx2 + dy2 * dy2).toDouble())).toFloat()
		}
	}


	private fun pathLength(): Int {
		var length = 1
		var current = goalState
		while (current != null) {
			current = current.parent
			length++
		}
		return length
	}

	override // Fail
			// Start from goalState and traverse backwards.
	val path: Array<IntArray>
		get() {
			if (goalState == null) return emptyArray()
			val length = pathLength()
			val path = arrayOfNulls<IntArray>(length)
			var current = goalState

			path[length - 1] = intArrayOf(ex, ey)

			var index = length - 2
			while (current != null) {
				path[index] = intArrayOf(current.basePoint.x, current.basePoint.y)

				index--
				current = current.parent
			}

			return path as Array<IntArray>
		}

	//@Override
	protected // Fail
			// Start from goalState and traverse backwards.
	val pathLength: Float
		get() {
			if (goalState == null) return -1f
			var pathLength = 0.0
			var currX = ex
			var currY = ey
			var current = goalState

			while (current != null) {
				val nextX = current.basePoint.x
				val nextY = current.basePoint.y

				pathLength += graph.distance_double(currX, currY, nextX, nextY)
				current = current.parent
				currX = nextX
				currY = nextY
			}

			return pathLength.toFloat()
		}


	override fun computeSearchSnapshot(): List<SnapshotItem> {
		val list = ArrayList<SnapshotItem>(states.size)

		for (state in states) {
			// y, xLn, xLd, xRn, xRd, px, py
			if (state == null) continue

			val line = arrayOf(
					state.y,
					state.xL.n,
					state.xL.d,
					state.xR.n,
					state.xR.d,
					state.basePoint.x,
					state.basePoint.y
			)
			list.add(SnapshotItem.generate(line))
		}

		if (!pq!!.isEmpty) {
			val index = pq!!.minIndex
			val state = states[index]!!

			val line = arrayOf(
					state.y,
					state.xL.n,
					state.xL.d,
					state.xR.n,
					state.xR.d
			)
			list.add(SnapshotItem.generate(line))
		}

		return list
	}
}
