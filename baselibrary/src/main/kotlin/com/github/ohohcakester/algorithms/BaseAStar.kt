package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.SnapshotItem
import com.github.ohohcakester.grid.GridGraph
import java.awt.Color

abstract class BaseAStar(graph: GridGraph, sizeX: Int, sizeY: Int,
                sx: Int, sy: Int, ex: Int, ey: Int) : PathFindingAlgorithm(graph, sizeX, sizeY, sx, sy, ex, ey) {
	protected abstract fun getParent(index: Int): Int

	protected abstract fun setParent(index: Int, value: Int)

	protected abstract val parentSize: Int

	override fun computeSearchSnapshot(): List<SnapshotItem> {
		val list = java.util.ArrayList<SnapshotItem>()
		var current = goalParentIndex()
		var finalPathSet: MutableSet<Int>? = null
		if (getParent(current) >= 0) {
			finalPathSet = java.util.HashSet<Int>()
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
}