package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.SnapshotItem
import com.github.ohohcakester.grid.GridGraph

/**
 * ABSTRACT<br></br>
 * Template for all Path Finding Algorithms used.<br></br>
 */
abstract class PathFindingAlgorithm<out P>(
		protected val graph: GridGraph, protected val sizeX: Int, protected val sizeY: Int,
		val sx: Int, val sy: Int, val ex: Int, val ey: Int,
		private val pointConstructor: (x: Int, y: Int) -> P) {

	var recorder: PathFindingRecorder? = null

	/**
	 * Call this to compute the path.
	 */
	abstract fun computePath()

	/**
	 * @return retrieve the path computed by the algorithm
	 */
	abstract val path: List<P>

	protected fun makePoint(x: Int, y: Int) = pointConstructor(x, y)

	protected fun maybeSaveSearchSnapshot() {
		recorder?.maybeSaveSearchSnapshot()
	}

	protected fun addSnapshot(snapshotItemList: List<SnapshotItem>) {
		recorder?.addSnapshot(snapshotItemList)
	}
}
