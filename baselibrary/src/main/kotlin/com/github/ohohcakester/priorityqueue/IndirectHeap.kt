package com.github.ohohcakester.priorityqueue

import java.io.Serializable
import java.util.ArrayList
import java.util.Comparator

/**
 * Indirect binary heap. Used for O(lgn) deleteMin and O(lgn) decreaseKey.
 */
class IndirectHeap<E : Comparable<E>>(private val minHeap: Boolean) : Serializable {
	private val keyList = ArrayList<E>()
	private val inList = ArrayList<Int>()
	private val outList = ArrayList<Int>()

	private var comparator: Comparator<E>? = null

	/**
	 * Runtime: O(n)
	 */
	constructor(array: Array<E>, minHeap: Boolean) : this(minHeap) {
		keyList.ensureCapacity(array.size)
		inList.ensureCapacity(array.size)
		outList.ensureCapacity(array.size)

		for ((index, e) in array.withIndex()) {
			keyList.add(e)
			inList.add(index)
			outList.add(index)
		}
	}

	private fun parent(index: Int) = (index - 1) / 2

	private fun leftChild(index: Int) = 2 * index + 1

	private fun rightChild(index: Int) = 2 * index + 2

	private fun compare(a: E, b: E) = when (comparator) {
		null -> a.compareTo(b)
		else -> comparator!!.compare(a, b)
	}

	/**
	 * Increases the capacity of the indirect heap, so that it can hold at
	 * least the number of elements specified by capacity without having to
	 * reallocate the heap.
	 */
	fun reserve(capacity: Int) {
		keyList.ensureCapacity(capacity)
		inList.ensureCapacity(capacity)
		outList.ensureCapacity(capacity)
	}

	/**
	 * Returns the handle to the value.
	 */
	fun insert(value: E): Int {
		val index = keyList.size

		keyList.add(value)
		inList.add(index)
		outList.add(index)
		bubbleUp(index)

		return index
	}

	fun setComparator(comparator: Comparator<E>) {
		this.comparator = comparator
	}

	/**
	 * Runtime: O(n)
	 */
	fun heapify() {
		for (i in keyList.size / 2 - 1 downTo 0) {
			bubbleDown(i)
		}
	}

	private fun bubbleUp(index: Int) {
		if (index == 0)
		// Reached root
			return

		val parent = (index - 1) / 2
		if (compare(keyList[index], keyList[parent]) < 0 == minHeap) {
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

		swapE(keyList, a, b)
		swapI(inList, s, t)
		swapI(outList, a, b)
	}

	/**
	 * swap integers in list
	 */
	private fun swapI(list: ArrayList<Int>, i1: Int, i2: Int) {
		val temp = list[i1]
		list[i1] = list[i2]
		list[i2] = temp
	}

	/**
	 * swap elements in list.
	 */
	private fun swapE(list: ArrayList<E>, i1: Int, i2: Int) {
		val temp = list[i1]
		list[i1] = list[i2]
		list[i2] = temp
	}

	private fun smallerNodeIffMin(index1: Int, index2: Int): Int {
		if (index1 >= keyList.size) {
			if (index2 >= keyList.size)
				return -1

			return index2
		}
		if (index2 >= keyList.size)
			return index1

		return if (compare(keyList[index1], keyList[index2]) < 0 == minHeap) index1 else index2
	}

	private fun bubbleDown(index: Int) {
		val leftChild = leftChild(index)
		val rightChild = rightChild(index)

		val smallerChild = smallerNodeIffMin(leftChild, rightChild)
		if (smallerChild == -1) return

		if (compare(keyList[index], keyList[smallerChild]) > 0 == minHeap) {
			// If meets the conditions to bubble down,
			swapData(index, smallerChild)
			bubbleDown(smallerChild)
		}
	}

	/**
	 * Runtime: O(lgn)
	 */
	fun decreaseKey(outIndex: Int, newKey: E) {
		// Assume newKey < old key
		//System.out.println(keyList);
		//System.out.println(inList);
		//System.out.println(outList);
		val inIndex = inList[outIndex]
		keyList[inIndex] = newKey
		bubbleUp(inIndex)
	}

	val minValue: E
		get() = keyList[0]

	/**
	 * Runtime: O(lgn)

	 * @return index of min element
	 */
	fun popMinIndex(): Int {
		if (keyList.size == 0)
			throw NullPointerException("Indirect Heap is empty!")
		else if (keyList.size == 1) {
			val s = outList[0]
			inList[s] = -1
			keyList.removeAt(0)
			return outList.removeAt(0)
		}
		// nodeList.size() > 1

		// s = Data at 0 = out[0]
		// t = Data at lastIndex = out[lastIndex]
		// key[0] = key[lastIndex], remove key[lastIndex]
		// in[s] = -1
		// in[t] = 0
		// out[0] = out[lastIndex], remove out[lastIndex]

		//E temp = keyList.get(0);
		val lastIndex = keyList.size - 1

		val s = outList[0]
		val t = outList[lastIndex]

		keyList[0] = keyList[lastIndex]
		keyList.removeAt(lastIndex)
		inList[s] = -1
		inList[t] = 0
		outList[0] = outList[lastIndex]
		outList.removeAt(lastIndex)

		bubbleDown(0)

		return s
	}

	/**
	 * Runtime: O(lgn)

	 * @return value of min element
	 */
	fun popMinValue(): E {
		if (keyList.size == 0)
			throw NullPointerException("Indirect Heap is empty!")
		else if (keyList.size == 1) {
			val s = outList[0]
			inList[s] = -1
			val value = keyList[0]
			keyList.removeAt(0)
			outList.removeAt(0)
			return value
		}
		// nodeList.size() > 1

		// s = Data at 0 = out[0]
		// t = Data at lastIndex = out[lastIndex]
		// key[0] = key[lastIndex], remove key[lastIndex]
		// in[s] = -1
		// in[t] = 0
		// out[0] = out[lastIndex], remove out[lastIndex]

		//E temp = keyList.get(0);
		val lastIndex = keyList.size - 1

		val s = outList[0]
		val t = outList[lastIndex]

		keyList[0] = keyList[lastIndex]
		val value = keyList[lastIndex]
		keyList.removeAt(lastIndex)
		inList[s] = -1
		inList[t] = 0
		outList[0] = outList[lastIndex]
		outList.removeAt(lastIndex)

		bubbleDown(0)

		return value
	}

	fun arrayToString(): String {
		val sb = StringBuilder()
		for (i in keyList.indices) {
			sb.append(i)
			sb.append(" ")
			sb.append(keyList[i])
			sb.append("\n")
		}
		return sb.toString()
	}

	val isEmpty: Boolean
		get() = keyList.isEmpty()
}