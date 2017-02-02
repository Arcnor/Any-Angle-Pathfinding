package com.github.ohohcakester.algorithms.bst

import java.util.LinkedList
import java.util.Queue
import java.util.Stack

/**
 * Base class for a binary tree that stores type E objects.
 * Node is a public class.
 */
open class BinaryTree<E> : Iterable<E> {

	// Data Field
	/**
	 * The root of the binary tree
	 */
	var root: Node<E>? = null

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

	override fun iterator(): Iterator<E> = TreeIterator(this)

	/**
	 * Return the left subtree.

	 * @return The left subtree or null if either the root or
	 * * the left subtree is null
	 */
	val leftSubtree: BinaryTree<E>?
		get() {
			val left = root?.left
			return if (left != null) {
				BinaryTree(left)
			} else {
				null
			}
		}

	/**
	 * Return the right sub-tree

	 * @return the right sub-tree or
	 * * null if either the root or the
	 * * right subtree is null.
	 */
	val rightSubtree: BinaryTree<E>?
		get() {
			val right = root?.right
			return if (right != null) {
				BinaryTree(right)
			} else {
				null
			}
		}

	/**
	 * Return the data field of the root

	 * @return the data field of the root
	 * * or null if the root is null
	 */
	val data: E?
		get() {
			if (root != null) {
				return root!!.data
			} else {
				return null
			}
		}

	/**
	 * Determine whether this tree is a leaf.

	 * @return true if the root has no children
	 */
	val isLeaf: Boolean
		get() = root == null || root!!.left == null && root!!.right == null

	@JvmOverloads fun height(r: Node<E>? = root): Int {
		if (r == null) return 0
		return 1 + Math.max(height(r.left), height(r.right))
	}

	@JvmOverloads fun size(r: Node<E>? = root): Int {
		if (r == null) return 0
		return 1 + size(r.left) + size(r.right)
	}

	override fun toString(): String {
		val sb = StringBuilder()
		preOrderTraverse(root, 1, sb)
		return sb.toString()
	}

	/**
	 * Perform a preorder traversal.

	 * @param node  The local root
	 * *
	 * @param depth The depth
	 * *
	 * @param sb    The string buffer to save the output
	 */
	private fun preOrderTraverse(node: Node<E>?, depth: Int,
	                             sb: StringBuilder) {
		for (i in 1..depth - 1) {
			sb.append("  ")
		}
		if (node == null) {
			sb.append("null\n")
		} else {
			sb.append(node.toString())
			sb.append("\n")
			preOrderTraverse(node.left, depth + 1, sb)
			preOrderTraverse(node.right, depth + 1, sb)
		}
	}

	/**
	 * Method to return the preorder traversal of the binary tree
	 * as a sequence of strings each separated by a space.

	 * @return A preorder traversal as a string
	 */
	fun preorderToString(): String {
		val stb = StringBuilder()
		preorderToString(stb, root!!)
		return stb.toString()
	}

	private fun preorderToString(stb: StringBuilder, root: Node<E>) {
		stb.append(root)
		if (root.left != null) {
			stb.append(" ")
			preorderToString(stb, root.left!!)
		}
		if (root.right != null) {
			stb.append(" ")
			preorderToString(stb, root.right!!)
		}
	}

	/**
	 * Method to return the postorder traversal of the binary tree
	 * as a sequence of strings each separated by a space.

	 * @return A postorder traversal as a string
	 */
	fun postorderToString(): String {
		val stb = StringBuilder()
		postorderToString(stb, root!!)
		return stb.toString()
	}

	private fun postorderToString(stb: StringBuilder, root: Node<E>) {
		if (root.left != null) {
			postorderToString(stb, root.left!!)
			stb.append(" ")
		}
		if (root.right != null) {
			postorderToString(stb, root.right!!)
			stb.append(" ")
		}
		stb.append(root)
	}

	/**
	 * A method to display the inorder traversal of a binary tree
	 * placeing a left parenthesis before each subtree and a right
	 * parenthesis after each subtree. For example the expression
	 * tree shown in Figure 6.12 would be represented as
	 * (((x) + (y)) * ((a) / (b))).

	 * @return An inorder string representation of the tree
	 */
	fun inorderToString(): String {
		if (root == null) return "null"

		val stb = StringBuilder()
		inorderToString(stb, root!!)
		return stb.toString()
	}

	private fun inorderToString(stb: StringBuilder, root: Node<E>) {
		val left = root.left
		val right = root.right

		if (left != null) {
			stb.append("(")
			inorderToString(stb, left)
			stb.append(") ")
		}
		stb.append(root)
		if (right != null) {
			stb.append(" (")
			inorderToString(stb, right)
			stb.append(")")
		}
	}

	fun levelorderToString(): String {
		val stb = StringBuilder()
		levelorderToString(stb, root)
		return stb.toString()
	}

	private fun levelorderToString(stb: StringBuilder, root: Node<E>?) {
		if (root == null) {
			stb.append("An empty tree")
			return
		}
		val q = LinkedList<Node<E>>()

		q.offer(root)
		while (!q.isEmpty()) {
			val curr = q.poll()
			stb.append(curr)
			stb.append(" ")

			if (curr.left != null) {
				q.offer(curr.left)
			}
			if (curr.right != null) {
				q.offer(curr.right)
			}
		}
	}

	private fun levelorderToStringPositions(stb: StringBuilder, root: Node<E>?) {
		if (root == null) {
			stb.append("An empty tree")
			return
		}
		val q = LinkedList<Node<E>>()
		val strQueue = LinkedList<String>()

		q.offer(root)
		strQueue.offer("")

		while (!q.isEmpty()) {
			val curr = q.poll()
			val currPath = strQueue.poll()
			stb.append("(")
			stb.append(curr)
			stb.append(",")
			stb.append(currPath)
			stb.append(") ")

			if (curr.left != null) {
				q.offer(curr.left)
				strQueue.offer(currPath + "L")
			}
			if (curr.right != null) {
				q.offer(curr.right)
				strQueue.offer(currPath + "R")
			}
		}
	}

	val isEmpty: Boolean
		get() = root == null

	private inner class TreeIterator<E>(private val tree: BinaryTree<E>) : Iterator<E> {
		private var current: Node<E>? = null
		private val nodeStack = Stack<Node<E>>()

		override fun hasNext(): Boolean {
			if (current == null)
				return root != null

			if (current!!.right == null)
				return nodeStack.isNotEmpty()

			return true
		}

		override fun next(): E {
			if (current == null) {
				if (root != null)
					current = tree.root
				else
					throw IndexOutOfBoundsException()

				// From start, go to far left.
				goToFarLeft()
				return current!!.data
			} else {
				if (current!!.right != null) {
					// Has Right Child: go right, then go to far left, pushing lefts into stack.
					current = current!!.right
					goToFarLeft()
					return current!!.data
				} else {
					// Has no Right Child: pop from stack.
					current = nodeStack.pop()
					return current!!.data
				}
			}
		}

		private fun goToFarLeft() {
			while (current!!.left != null) {
				nodeStack.push(current)
				current = current!!.left
			}
		}
	}
}