package com.github.ohohcakester.algorithms.bst

/**
 * Class for a binary tree that stores type E objects.
 * Node is a public class.
 */
open class BinarySearchTree<E : Comparable<E>> : BinaryTree<E> {

	/**
	 * Construct an empty BinaryTree
	 */
	constructor() {
		root = null
	}


	/**
	 * Construct a BinaryTree with a specified root.
	 * Should only be used by subclasses.

	 * @param root The node that is the root of the tree.
	 */
	protected constructor(root: Node<E>) {
		this.root = root
	}

	/**
	 * Constructs a new binary tree with data in its root,leftTree
	 * as its left subtree and rightTree as its right subtree.
	 */
	constructor(data: E, leftTree: BinaryTree<E>?,
	            rightTree: BinaryTree<E>?) {
		root = Node(data)
		if (leftTree != null) {
			root!!.left = leftTree.root
		} else {
			root!!.left = null
		}
		if (rightTree != null) {
			root!!.right = rightTree.root
		} else {
			root!!.right = null
		}
	}

	protected fun rotateLeft(pivot: Node<E>): Node<E> {
		val temp = pivot.right
		pivot.right = pivot.right!!.left
		temp!!.left = pivot
		return temp
	}

	protected fun rotateRight(pivot: Node<E>): Node<E> {
		val temp = pivot.left
		pivot.left = pivot.left!!.right
		temp!!.right = pivot
		return temp
	}

	fun insertBST(value: E): Boolean {
		// Returns true iff insertion is successful.

		val newRoot = insertBST(root, value) ?: return false

		// newRoot != null.
		root = newRoot
		return true
	}

	private fun insertBST(current: Node<E>?, value: E): Node<E>? {
		if (current == null)
			return Node(value)

		val result = value.compareTo(current.data!!)
		if (result < 0) {
			val newCurrent = insertBST(current.left, value) ?: return null
			current.left = newCurrent
			return current
		}
		if (result > 0) {
			val newCurrent = insertBST(current.right, value) ?: return null
			current.right = newCurrent
			return current
		} else
			return null
	}

	operator fun contains(data: E): Boolean {
		return contains(root, data)
	}

	private fun contains(current: Node<E>?, data: E): Boolean {
		if (current == null) return false

		val compare = current.data!!.compareTo(data)
		if (compare == 0)
			return true
		if (compare < 0)
			return contains(current.right, data)
		else
			return contains(current.left, data)
	}

	fun search(data: E): Node<E>? {
		var current = root
		while (current != null) {
			val result = data.compareTo(current.data!!)
			if (result < 0) {
				current = current.left
			} else if (result > 0) {
				current = current.right
			} else {
				//equal
				return current
			}
		}
		return null
	}

	fun ceiling(data: E): Node<E>? {
		var current = root
		var currentCeiling: Node<E>? = null
		while (current != null) {
			val result = data.compareTo(current.data)
			if (result < 0) {
				currentCeiling = current
				current = current.left
			} else if (result > 0) {
				current = current.right
			} else {
				// equal
				return current
			}
		}
		return currentCeiling
	}
}