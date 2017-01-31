package com.github.ohohcakester.priorityqueue

import java.util.Arrays


/**
 * Indirect binary heap. Used for O(lgn) deleteMin and O(lgn) decreaseKey.
 * Runtime: O(1)
 */
class FastVariableSizeIndirectHeap
@JvmOverloads constructor(capacity: Int = 11) {

	private var keyList: FloatArray
	private var inList: IntArray
	private var outList: IntArray
	private var heapSize: Int = 0
	private var nextIndex: Int = 0

	init {
		keyList = FloatArray(capacity)
		inList = IntArray(capacity)
		outList = IntArray(capacity)

		heapSize = 0
		nextIndex = 0
	}

	private fun parent(index: Int) = (index - 1) / 2

	private fun leftChild(index: Int) = 2 * index + 1

	private fun rightChild(index: Int) = 2 * index + 2

	/**
	 * Increases the capacity of the indirect heap, so that it can hold at
	 * least the number of elements specified by capacity without having to
	 * reallocate the heap.
	 */
	fun reserve(capacity: Int) {
		if (keyList.size < capacity) {
			keyList = Arrays.copyOf(keyList, capacity)
			inList = Arrays.copyOf(inList, capacity)
			outList = Arrays.copyOf(outList, capacity)
		}
	}

	/**
	 * Returns the handle to the value.
	 */
	fun insert(value: Float): Int {
		if (nextIndex >= keyList.size) {
			val newLength = keyList.size * 2
			// Too small.
			keyList = Arrays.copyOf(keyList, newLength)
			inList = Arrays.copyOf(inList, newLength)
			outList = Arrays.copyOf(outList, newLength)
		}

		val inIndex = heapSize
		val outIndex = nextIndex

		keyList[heapSize] = value
		inList[nextIndex] = heapSize
		outList[heapSize] = nextIndex
		heapSize++
		nextIndex++

		bubbleUp(inIndex)

		return outIndex
	}

	private fun bubbleUp(index: Int) {
		if (index == 0)
		// Reached root
			return

		val parent = (index - 1) / 2
		if (keyList[index] < keyList[parent]) {
			// If meets the conditions to bubble up,
			swapData(index, parent)
			bubbleUp(parent)
		}
	}

	private fun swapData(a: Int, b: Int) {
		// s = Data at a = out[a]
		// t = Data at b = out[b]
		// key[a] <-> key[b]
		// in[s] <-> in[t]
		// out[a] <-> out[b]

		val s = outList[a]
		val t = outList[b]

		swapKey(a, b)
		swapIn(s, t)
		swapOut(a, b)
	}

	/**
	 * swap integers in list
	 */
	private fun swapKey(i1: Int, i2: Int) {
		val temp = keyList[i1]
		keyList[i1] = keyList[i2]
		keyList[i2] = temp
	}

	/**
	 * swap integers in list
	 */
	private fun swapOut(i1: Int, i2: Int) {
		val temp = outList[i1]
		outList[i1] = outList[i2]
		outList[i2] = temp
	}

	/**
	 * swap integers in list
	 */
	private fun swapIn(i1: Int, i2: Int) {
		val temp = inList[i1]
		inList[i1] = inList[i2]
		inList[i2] = temp
	}

	private fun smallerNode(index1: Int, index2: Int): Int {
		if (index1 >= heapSize) {
			if (index2 >= heapSize)
				return -1

			return index2
		}
		if (index2 >= heapSize)
			return index1

		return if (keyList[index1] < keyList[index2]) index1 else index2
	}

	private fun bubbleDown(index: Int) {
		val leftChild = leftChild(index)
		val rightChild = rightChild(index)

		val smallerChild = smallerNode(leftChild, rightChild)
		if (smallerChild == -1) return

		if (keyList[index] > keyList[smallerChild]) {
			// If meets the conditions to bubble down,
			swapData(index, smallerChild)
			bubbleDown(smallerChild)
		}
	}

	/**
	 * Runtime: O(lgn)
	 */
	fun decreaseKey(outIndex: Int, newKey: Float) {
		// Assume newKey < old key
		//System.out.println(keyList);
		//System.out.println(inList);
		//System.out.println(outList);
		val inIndex = inList[outIndex]
		keyList[inIndex] = newKey
		bubbleUp(inIndex)
	}

	val minValue: Float
		get() = keyList[0]

	val minIndex: Int
		get() = outList[0]

	/**
	 * Runtime: O(lgn)

	 * @return index of min element
	 */
	fun popMinIndex(): Int {
		if (heapSize == 0)
			throw NullPointerException("Indirect Heap is empty!")
		else if (heapSize == 1) {
			val s = outList[0]
			inList[s] = -1
			heapSize--
			return s
		}
		// nodeList.size() > 1

		// s = Data at 0 = out[0]
		// t = Data at lastIndex = out[lastIndex]
		// key[0] = key[lastIndex], remove key[lastIndex]
		// in[s] = -1
		// in[t] = 0
		// out[0] = out[lastIndex], remove out[lastIndex]

		//E temp = keyList.get(0);
		val lastIndex = heapSize - 1

		val s = outList[0]
		val t = outList[lastIndex]

		keyList[0] = keyList[lastIndex]
		inList[s] = -1
		inList[t] = 0
		outList[0] = outList[lastIndex]

		heapSize--

		bubbleDown(0)

		return s
	}

	fun arrayToString(): String {
		val sb = StringBuilder()
		for (i in 0..heapSize - 1) {
			sb.append(i)
			sb.append(" ")
			sb.append(keyList[i])
			sb.append("\n")
		}
		return sb.toString()
	}

	val isEmpty: Boolean
		get() = heapSize <= 0

	fun size(): Int {
		return heapSize
	}
}