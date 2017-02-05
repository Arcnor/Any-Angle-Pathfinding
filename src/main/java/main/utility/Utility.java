package main.utility;

import com.github.ohohcakester.algorithms.PathFindingAlgorithm;
import com.github.ohohcakester.algorithms.astar.visibilitygraph.VisibilityGraphAlgorithm;
import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.grid.GridGraph;
import main.AlgoFunction;
import main.testgen.StartEndPointData;

import java.util.ArrayList;
import java.util.List;

public class Utility {

	/**
	 * Compute the length of a given path. (Using euclidean distance)
	 */
	public static double computePathLength(GridGraph gridGraph, List<Point> path) {
		path = removeDuplicatesInPath(path);
		double pathLength = 0;
		for (int i = 0; i < path.size() - 1; i++) {
			pathLength += gridGraph.distance_double(path.get(i).getX(), path.get(i).getY(),
					path.get(i + 1).getX(), path.get(i + 1).getY());
		}
		return pathLength;
	}

	public static double computeOptimalPathLength(GridGraph gridGraph, Point start, Point end) {
		// Optimal algorithm.
		PathFindingAlgorithm<Point> algo = new VisibilityGraphAlgorithm<>(gridGraph, start.getX(), start.getY(), end.getX(), end.getY(), Point::new);
		algo.computePath();
		List<Point> path = algo.getPath();
		path = removeDuplicatesInPath(path);
		return computePathLength(gridGraph, path);
	}

	public static ArrayList<StartEndPointData> fixProblemPathLength(GridGraph gridGraph, ArrayList<StartEndPointData> problems) {
		ArrayList<StartEndPointData> fixedProblems = new ArrayList<>();
		for (StartEndPointData problem : problems) {
			//System.out.println(problem.start + " | " + problem.end);
			double shortestPathLength = computeOptimalPathLength(gridGraph, problem.start, problem.end);
			fixedProblems.add(new StartEndPointData(problem.start, problem.end, shortestPathLength));
			if (shortestPathLength > problem.shortestPath + 0.0001)
				System.out.println("REPAIRING: " + problem.shortestPath + " -> " + shortestPathLength);
		}
		return fixedProblems;
	}

	public static List<Point> removeDuplicatesInPath(List<Point> path) {
		if (path.size() <= 2) return path;

		List<Point> newPath = new ArrayList<>(path.size());
		for (int j = 0; j < path.size(); j++) {
			newPath.add(new Point(0, 0));
		}
		int index = 0;
		newPath.add(path.get(0));
		for (int i = 1; i < path.size() - 1; ++i) {
			if (isCollinear(path.get(i).getX(), path.get(i).getY(), path.get(i + 1).getX(), path.get(i + 1).getY(), newPath.get(index).getX(), newPath.get(index).getY())) {
				// skip
			} else {
				index++;
				newPath.set(index, path.get(i));
			}
		}
		index++;
		newPath.set(index, path.get(path.size() - 1));
		return newPath;
	}

	private static boolean isCollinear(int x1, int y1, int x2, int y2, int x3, int y3) {
		return (y3 - y1) * (x2 - x1) == (x3 - x1) * (y2 - y1);
	}

	/**
	 * Generates a path between two points on a grid.
	 *
	 * @return an array of int[2] indicating the coordinates of the path.
	 */
	public static List<Point> generatePath(AlgoFunction<Point> algoFunction, GridGraph gridGraph,
	                                   int sx, int sy, int ex, int ey) {
		PathFindingAlgorithm<Point> algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey, Point::new);
		algo.computePath();

		return algo.getPath();
	}

	public static boolean isPathTaut(GridGraph gridGraph, List<Point> path) {
		int v1 = 0;
		int v2 = 1;
		for (int v3 = 2; v3 < path.size(); ++v3) {
			if (!gridGraph.isTaut(path.get(v1).getX(), path.get(v1).getY(), path.get(v2).getX(), path.get(v2).getY(), path.get(v3).getX(), path.get(v3).getY()))
				return false;
			++v1;
			++v2;
		}
		return true;
	}

	public static boolean isOptimal(double length, double optimalLength) {
		//System.out.println(length + " | " + optimalLength + " | " + ((length - optimalLength) < 0.0001));
		return (length - optimalLength) < 0.0001;
	}
}
