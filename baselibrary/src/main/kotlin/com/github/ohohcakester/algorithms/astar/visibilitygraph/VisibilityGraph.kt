package com.github.ohohcakester.algorithms.astar.visibilitygraph

import com.github.ohohcakester.datatypes.Point
import com.github.ohohcakester.grid.GridGraph

import java.util.ArrayList

class VisibilityGraph(private val graph: GridGraph, private val sx: Int, private val sy: Int, private val ex: Int, private val ey: Int) {
	private var nodeList: ArrayList<Point>? = null
	private var outgoingEdgeList = ArrayList<ArrayList<Edge>>()
	private var nodeIndex: Array<IntArray>? = null
	private var startIndex: Int = 0
	private var startIsNewNode: Boolean = false
	private var endIndex: Int = 0
	private var endIsNewNode: Boolean = false
	private var saveSnapshot: (() -> Unit)? = null

	fun setSaveSnapshotFunction(saveSnapshot: () -> Unit) {
		this.saveSnapshot = saveSnapshot
	}

	fun initialise() {
		if (nodeList != null) {
			//("already initialised.");
			return
		}

		nodeList = ArrayList<Point>()
		outgoingEdgeList.clear()

		addNodes()
		addAllEdges()
		addStartAndEnd(sx, sy, ex, ey)
	}

	private fun addNodes() {
		nodeIndex = Array(graph.sizeY + 1) { y ->
			IntArray(graph.sizeX + 1) { x ->
				if (isCorner(x, y)) {
					assignNode(x, y)
				} else {
					-1
				}
			}
		}
	}

	private fun assignNode(x: Int, y: Int): Int {
		val index = nodeList!!.size
		nodeList!!.add(Point(x, y))
		outgoingEdgeList.add(ArrayList<Edge>())
		return index
	}

	private fun assignNodeAndConnect(x: Int, y: Int): Int {
		val index = nodeList!!.size
		nodeList!!.add(Point(x, y))
		outgoingEdgeList.add(ArrayList<Edge>())

		for (i in 0..nodeList!!.size - 1 - 1) {
			val (x1, y1) = coordinateOf(i)
			if (graph.lineOfSight(x, y, x1, y1)) {
				val weight = computeWeight(x, y, x1, y1)
				addEdge(i, index, weight)
				addEdge(index, i, weight)
			}
		}

		return index
	}

	/**
	 * Assumption: start and end are the last two nodes, if they exist.
	 */
	private fun removeStartAndEnd() {
		if (startIsNewNode) {
			val index = nodeList!!.size - 1
			nodeIndex!![sy][sx] = -1
			nodeList!!.removeAt(index)
			outgoingEdgeList.removeAt(index)
			removeInstancesOf(index)

			startIsNewNode = false
		}
		if (endIsNewNode) {
			val index = nodeList!!.size - 1
			nodeIndex!![ey][ex] = -1
			nodeList!!.removeAt(index)
			outgoingEdgeList.removeAt(index)
			removeInstancesOf(index)

			endIsNewNode = false
		}
	}

	protected fun removeInstancesOf(index: Int) {
		for (edgeList in outgoingEdgeList) {
			val edge = Edge(0, index, 0f)
			edgeList.remove(edge)
		}
	}

	private fun addStartAndEnd(sx: Int, sy: Int, ex: Int, ey: Int) {
		if (isNode(sx, sy)) {
			startIndex = indexOf(sx, sy)
			startIsNewNode = false
		} else {
			nodeIndex!![sy][sx] = assignNodeAndConnect(sx, sy)
			startIndex = nodeIndex!![sy][sx]
			startIsNewNode = true
		}

		if (isNode(ex, ey)) {
			endIndex = indexOf(ex, ey)
			endIsNewNode = false
		} else {
			nodeIndex!![ey][ex] = assignNodeAndConnect(ex, ey)
			endIndex = nodeIndex!![ey][ex]
			endIsNewNode = true
		}
	}

	private fun addAllEdges() {
		var saveFactor = nodeList!!.size / 10
		if (saveFactor == 0) saveFactor = 1

		for (i in nodeList!!.indices) {
			val (x, y) = coordinateOf(i)
			for (j in i + 1..nodeList!!.size - 1) {
				val (x1, y1) = coordinateOf(j)
				if (graph.lineOfSight(x, y, x1, y1)) {
					val weight = computeWeight(x, y, x1, y1)
					addEdge(i, j, weight)
					addEdge(j, i, weight)
				}
			}

			if (i % saveFactor == 0)
				maybeSaveSnapshot()
		}
	}

	private fun computeWeight(x1: Int, y1: Int, x2: Int, y2: Int): Float {
		val dx = x2 - x1
		val dy = y2 - y1
		return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
	}

	protected fun addEdge(fromI: Int, toI: Int, weight: Float) {
		val edgeList = outgoingEdgeList[fromI]
		edgeList.add(Edge(fromI, toI, weight))
	}

	private fun indexOf(x: Int, y: Int): Int {
		return nodeIndex!![y][x]
	}

	private fun isNode(x: Int, y: Int): Boolean {
		return nodeIndex!![y][x] != -1
	}

	private fun isCorner(x: Int, y: Int): Boolean {
		val a = graph.isBlocked(x - 1, y - 1)
		val b = graph.isBlocked(x, y - 1)
		val c = graph.isBlocked(x, y)
		val d = graph.isBlocked(x - 1, y)

		return (!a && !c || !d && !b) && (a || b || c || d)

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

	fun coordinateOf(index: Int): Point {
		return nodeList!![index]
	}

	fun size(): Int {
		return nodeList!!.size
	}

	fun computeSumDegrees(): Int {
		var sum = 0
		for (list in outgoingEdgeList) {
			sum += list.size
		}
		return sum
	}

	fun edgeIterator(source: Int): Iterator<Edge> {
		return outgoingEdgeList[source].iterator()
	}

	fun getEdge(source: Int, dest: Int): Edge {

		val edges = outgoingEdgeList[source]
		for (edge in edges) {
			if (edge.dest == dest) {
				return edge
			}
		}
		return Edge(source, dest, java.lang.Float.POSITIVE_INFINITY)
	}

	fun startNode(): Int {
		return startIndex
	}

	fun endNode(): Int {
		return endIndex
	}

	private fun maybeSaveSnapshot() {
		saveSnapshot?.invoke()
	}

	companion object {
		private var storedVisibilityGraph: VisibilityGraph? = null
		private var storedGridGraph: GridGraph? = null

		fun repurpose(oldGraph: VisibilityGraph, sx: Int, sy: Int, ex: Int, ey: Int): VisibilityGraph {
			oldGraph.removeStartAndEnd()
			val newGraph = VisibilityGraph(oldGraph.graph, sx, sy, ex, ey)
			newGraph.nodeIndex = oldGraph.nodeIndex
			newGraph.startIndex = oldGraph.startIndex
			newGraph.startIsNewNode = oldGraph.startIsNewNode
			newGraph.endIndex = oldGraph.endIndex
			newGraph.endIsNewNode = oldGraph.endIsNewNode
			newGraph.nodeList = oldGraph.nodeList
			newGraph.outgoingEdgeList = oldGraph.outgoingEdgeList

			newGraph.addStartAndEnd(sx, sy, ex, ey)

			return newGraph
		}

		fun getStoredGraph(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int): VisibilityGraph {
			val result: VisibilityGraph
			if (storedGridGraph != graph || storedVisibilityGraph == null) {
				//("Get new graph");
				result = VisibilityGraph(graph, sx, sy, ex, ey)
				storedVisibilityGraph = result
				storedGridGraph = graph
			} else {
				//("Reuse graph");
				result = repurpose(storedVisibilityGraph!!, sx, sy, ex, ey)
				storedVisibilityGraph = result
			}
			return result
		}
	}


}