package com.github.ohohcakester.algorithms.astarstatic.thetastar.strict

import com.github.ohohcakester.grid.GridGraph

/**
 * An modification of Theta* that I am experimenting with. -Oh
 *
 *
 * ||| Experimental Versions: These versions can be found in source control |||
 * V1: Add buffer value to basic Theta*. that's all. [CURRENT - Strict Theta*]
 * V1b: An attempt to improve V1 using tryLocateTautParent. Not useful.
 *
 *
 * V2: With recursive taut-parent finding.
 * V2b: With collinear point merging to reduce depth of taut-parent searches.
 * [V2c NOT INCLUDED] <-- change the way distance comparison worked during relaxation to not include the buffer value.
 * '-> Did not perform well. Change was reversed.
 * V2d: Identify when buffer value has been added, and removes the buffer value on dequeue from PQ.
 * V2e: Add heuristic and change buffer value. [CURRENT - Recursive Strict Theta*]
 * '-> Amend: Remove Heuristic Trap (described below)
 *
 *
 * V3: Lazy ver <-- did not perform well. Discarded.
 *
 *
 * Ideas:
 * Heuristic trap (No longer used):
 * - The heuristic value of the final node is 1.1f instead of 0.
 * - A lot of inoptimality comes because the algorithm is too eager to relax
 * the final vertex. The slightly higher heuristic encourages the algorithm
 * to explore a little more first.
 */
class RecursiveStrictThetaStar(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AbstractStrictThetaStar(graph, sx, sy, ex, ey) {
	companion object {
		fun setBuffer(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, bufferValue: Float): RecursiveStrictThetaStar {
			val algo = RecursiveStrictThetaStar(graph, sx, sy, ex, ey)
			algo.BUFFER_VALUE = bufferValue
			return algo
		}

		fun depthLimit(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int, depthLimit: Int): RecursiveStrictThetaStar {
			val algo = RecursiveStrictThetaStar(graph, sx, sy, ex, ey)
			algo.DEPTH_LIMIT = depthLimit
			return algo
		}

		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = postSmooth(graph, sx, sy, ex, ey, ::RecursiveStrictThetaStar)
		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = repeatedPostSmooth(graph, sx, sy, ex, ey, ::RecursiveStrictThetaStar)
		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = dijkstra(graph, sx, sy, ex, ey, ::RecursiveStrictThetaStar)
	}

	private var DEPTH_LIMIT = -1
	private var BUFFER_VALUE = 0.42f

	override fun relax(u: Int, v: Int, weightUV: Float): Boolean {
		// return true iff relaxation is done.
		return tautRelax(u, v, DEPTH_LIMIT)
	}

	// This gives very good paths... but the recursion level is too deep.
	private fun tautRelax(u: Int, v: Int, depth: Int): Boolean {
		if (isTaut(v, u)) {
			return tryRelaxVertex(u, v, false)
		} else {
			val par = getParent(u)
			if (lineOfSight(par, v)) {
				if (depth == 0) {
					return tryRelaxVertex(par, v, !isTaut(v, par))
				}
				return tautRelax(par, v, depth - 1)
			} else {
				return tryRelaxVertex(u, v, true)
			}
		}
	}

	private fun tryRelaxVertex(u: Int, v: Int, addBuffer: Boolean): Boolean {
		var newParent = u
		var newWeight = distance(u) + physicalDistance(u, v)
		if (addBuffer) {
			newWeight += BUFFER_VALUE
			newParent += Integer.MIN_VALUE
		}
		if (newWeight < distance(v)) {
			if (isMergeableWithParent(u, v)) {
				newParent = getParent(u)
			}
			setDistance(v, newWeight)
			setParent(v, newParent)
			return true
		}
		return false
	}

	// If getParent(u),u,v collinear, remove u from path, except when u is at an outer corner.
	private fun isMergeableWithParent(u: Int, v: Int): Boolean {
		if (u == -1) return false
		val p = getParent(u)
		if (p == -1) return false // u is start point.
		val ux = toTwoDimX(u)
		val uy = toTwoDimY(u)
		if (isOuterCorner(ux, uy)) return false // u is outer corner

		val vx = toTwoDimX(v)
		val vy = toTwoDimY(v)
		val px = toTwoDimX(p)
		val py = toTwoDimY(p)

		return isCollinear(px, py, ux, uy, vx, vy)
	}

	private fun isOuterCorner(x: Int, y: Int): Boolean {
		val a = graph.isBlocked(x - 1, y - 1)
		val b = graph.isBlocked(x, y - 1)
		val c = graph.isBlocked(x, y)
		val d = graph.isBlocked(x - 1, y)

		return (!a && !c || !d && !b) && (a || b || c || d)

		/* NOTE:
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *
         *  The definition below excludes the above case.
         */
	}

	private fun isCollinear(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int): Boolean {
		// (y2-y1)/(x2-x1) == (y3-y2)/(x3-x2)
		// <=>
		return (y2 - y1) * (x3 - x2) == (y3 - y2) * (x2 - x1)
	}
}
