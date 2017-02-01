package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.Memory
import com.github.ohohcakester.datatypes.SnapshotItem
import com.github.ohohcakester.grid.GridGraph

import java.awt.Color
import java.util.ArrayList
import java.util.HashSet

/**
 * ABSTRACT<br></br>
 * Template for all Path Finding Algorithms used.<br></br>
 */
abstract class PathFindingAlgorithm(protected val graph: GridGraph, protected val sizeX: Int, protected val sizeY: Int,
                                    protected val sx: Int, protected val sy: Int, protected val ex: Int, protected val ey: Int) {
	protected val sizeXplusOne: Int
	private var snapshotCountdown = 0
	private val snapshotList = mutableListOf<List<SnapshotItem>>()
	private var ticketNumber = -1

	protected var isRecording: Boolean = false
		private set
	private var usingStaticMemory = false

	init {
		this.sizeXplusOne = sizeX + 1
	}

	protected fun initialiseMemory(size: Int, defaultDistance: Float, defaultParent: Int, defaultVisited: Boolean) {
		usingStaticMemory = true
		ticketNumber = Memory.initialise(size, defaultDistance, defaultParent, defaultVisited)
	}

	/**
	 * Call to start tracing the algorithm's operation.
	 */
	fun startRecording() {
		isRecording = true
	}

	/**
	 * Call to stop tracing the algorithm's operation.
	 */
	fun stopRecording() {
		isRecording = false
	}

	/**
	 * @return retrieve the trace of the algorithm that has been recorded.
	 */
	fun retrieveSnapshotList(): List<List<SnapshotItem>> {
		return snapshotList
	}

	/**
	 * Call this to compute the path.
	 */
	abstract fun computePath()

	/**
	 * @return retrieve the path computed by the algorithm
	 */
	abstract val path: Array<IntArray>

	protected fun toOneDimIndex(x: Int, y: Int): Int {
		return graph.toOneDimIndex(x, y)
	}

	protected fun toTwoDimX(index: Int): Int {
		return graph.toTwoDimX(index)
	}

	protected fun toTwoDimY(index: Int): Int {
		return graph.toTwoDimY(index)
	}

	protected fun maybeSaveSearchSnapshot() {
		if (isRecording) {
			if (usingStaticMemory && ticketNumber != Memory.currentTicket())
				throw UnsupportedOperationException("Ticket does not match!")

			saveSearchSnapshot()
		}
	}


	private fun saveSearchSnapshot() {
		if (snapshotCountdown > 0) {
			snapshotCountdown--
			return
		}
		snapshotCountdown = SNAPSHOT_INTERVAL

		snapshotList.add(computeSearchSnapshot())
	}

	protected fun addSnapshot(snapshotItemList: List<SnapshotItem>) {
		snapshotList.add(snapshotItemList)
	}

	protected open fun goalParentIndex(): Int {
		return toOneDimIndex(ex, ey)
	}

	protected abstract fun getParent(index: Int): Int

	protected abstract fun setParent(index: Int, value: Int)

	protected abstract val parentSize: Int

	protected open fun computeSearchSnapshot(): List<SnapshotItem> {
		val list = ArrayList<SnapshotItem>()
		var current = goalParentIndex()
		var finalPathSet: MutableSet<Int>? = null
		if (getParent(current) >= 0) {
			finalPathSet = HashSet<Int>()
			while (current != -1) {
				finalPathSet.add(current)
				current = getParent(current)
			}
		}

		val size = parentSize
		for (i in 0..size - 1) {
			if (getParent(i) != -1) {
				if (finalPathSet != null && finalPathSet.contains(i)) {
					list.add(SnapshotItem.generate(snapshotEdge(i), Color.BLUE))
				} else {
					list.add(SnapshotItem.generate(snapshotEdge(i)))
				}
			}
			val vertexSnapshot = snapshotVertex(i)
			if (vertexSnapshot != null) {
				list.add(SnapshotItem.generate(vertexSnapshot))
			}
		}

		return list
	}

	protected open fun snapshotEdge(endIndex: Int): Array<Int> {
		val edge = arrayOf(0, 0, toTwoDimX(endIndex), toTwoDimY(endIndex))
		val startIndex = getParent(endIndex)
		if (startIndex < 0) {
			edge[0] = edge[2]
			edge[1] = edge[3]
		} else {
			edge[0] = toTwoDimX(startIndex)
			edge[1] = toTwoDimY(startIndex)
		}

		return edge
	}

	protected open fun snapshotVertex(index: Int) = when {
		selected(index) -> arrayOf(toTwoDimX(index), toTwoDimY(index))
		else -> null
	}

	protected open fun selected(index: Int) = false

	companion object {
		private val SNAPSHOT_INTERVAL = 0
	}
}
