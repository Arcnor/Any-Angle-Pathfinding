package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.SnapshotItem
import com.github.ohohcakester.grid.GridGraph

/**
 * ABSTRACT<br></br>
 * Template for all Path Finding Algorithms used.<br></br>
 */
abstract class PathFindingAlgorithm(
		protected val graph: GridGraph, protected val sizeX: Int, protected val sizeY: Int,
		protected val sx: Int, protected val sy: Int, protected val ex: Int, protected val ey: Int) {

	var recorder: PathFindingRecorder? = null

	/**
	 * Call this to compute the path.
	 */
	abstract fun computePath()

	/**
	 * @return retrieve the path computed by the algorithm
	 */
	abstract val path: Array<IntArray>

	protected fun maybeSaveSearchSnapshot() {
		recorder?.maybeSaveSearchSnapshot()
	}

	protected fun addSnapshot(snapshotItemList: List<SnapshotItem>) {
		recorder?.addSnapshot(snapshotItemList)
	}
}
