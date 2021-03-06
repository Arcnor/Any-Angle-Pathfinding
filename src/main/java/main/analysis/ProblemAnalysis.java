package main.analysis;

import com.github.ohohcakester.algorithms.astar.visibilitygraph.BFSVisibilityGraph;
import com.github.ohohcakester.algorithms.astar.visibilitygraph.VisibilityGraph;
import com.github.ohohcakester.algorithms.astar.visibilitygraph.VisibilityGraphAlgorithm;
import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.grid.GridGraph;
import main.utility.Utility;

import java.util.List;

public class ProblemAnalysis {


	public final int sx, sy, ex, ey;
	public final double shortestPathLength;
	public final double straightLineDistance;
	public final double directness;
	public final double distanceCoverage;
	public final double minMapCoverage;

	public final int shortestPathHeadingChanges;
	public final int minHeadingChanges;


	public ProblemAnalysis(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
		this.sx = sx;
		this.sy = sy;
		this.ex = ex;
		this.ey = ey;

		VisibilityGraphAlgorithm<Point> algo = VisibilityGraphAlgorithm.Companion.graphReuse(gridGraph, sx, sy, ex, ey, Point::new);
		algo.computePath();
		List<Point> path = algo.getPath();
		int sizeX = gridGraph.getSizeX();
		int sizeY = gridGraph.getSizeY();

		shortestPathLength = Utility.computePathLength(gridGraph, path);
		straightLineDistance = gridGraph.distance(sx, sy, ex, ey);
		directness = computeDirectness(shortestPathLength, straightLineDistance);
		distanceCoverage = computeDistanceCoverage(straightLineDistance, sizeX, sizeY);
		minMapCoverage = computerMinMapCoverage(shortestPathLength, sizeX, sizeY);

		shortestPathHeadingChanges = path.size();
		VisibilityGraph visibilityGraph = algo.getVisibilityGraph();
		minHeadingChanges = computeMinHeadingChanges(gridGraph);
	}

	public static double computerMinMapCoverage(double shortestPathLength, int sizeX,
	                                            int sizeY) {
		return shortestPathLength / Math.sqrt(sizeX * sizeX + sizeY * sizeY);
	}


	public static double computeDistanceCoverage(double straightLineDistance,
	                                             int sizeX, int sizeY) {
		return straightLineDistance / Math.sqrt(sizeX * sizeX + sizeY * sizeY);
	}


	public static double computeDirectness(double shortestPathLength,
	                                       double straightLineDistance) {
		return shortestPathLength / straightLineDistance;
	}

	public int computeMinHeadingChanges(GridGraph gridGraph) {
		BFSVisibilityGraph algo = BFSVisibilityGraph.Companion.graphReuse(gridGraph, sx, sy, ex, ey, Point::new);
		algo.computePath();
		return algo.getPath().size();
	}

	@Override
	public String toString() {
		return "sx=" + sx +
				"\nsy=" + sy +
				"\nex=" + ex +
				"\ney=" + ey +
				"\nshortestPathLength=" + shortestPathLength +
				"\nstraightLineDistance=" + straightLineDistance +
				"\ndirectness=" + directness +
				"\ndistanceCoverage=" + distanceCoverage +
				"\nminMapCoverage=" + minMapCoverage +
				"\nshortestPathHeadingChanges=" + shortestPathHeadingChanges +
				"\nminHeadingChanges=" + minHeadingChanges;
	}
}