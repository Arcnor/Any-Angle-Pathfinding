package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.Memory
import com.github.ohohcakester.datatypes.SnapshotItem

abstract class PathFindingRecorder() {
	private companion object {
		const val SNAPSHOT_INTERVAL = 0
	}

	private var snapshotCountdown = 0
	private val snapshotList = mutableListOf<List<SnapshotItem>>()

	internal var isRecording: Boolean = false
		private set

	protected abstract fun computeSearchSnapshot(): List<SnapshotItem>

	private var usingStaticMemory = false
	private var ticketNumber = -1

	fun initializeStaticMemory(ticketNumber: Int) {
		usingStaticMemory = true
		this.ticketNumber = ticketNumber
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

	internal fun maybeSaveSearchSnapshot() {
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

	internal fun addSnapshot(snapshotItemList: List<SnapshotItem>) {
		snapshotList.add(snapshotItemList)
	}
}