package main;

import com.github.ohohcakester.algorithms.BaseAStar;
import com.github.ohohcakester.algorithms.BaseAStarRecorder;
import com.github.ohohcakester.algorithms.PathFindingAlgorithm;
import com.github.ohohcakester.algorithms.PathFindingRecorder;
import com.github.ohohcakester.algorithms.anya.Anya;
import com.github.ohohcakester.algorithms.anya.AnyaRecorder;
import com.github.ohohcakester.datatypes.Point;
import com.github.ohohcakester.datatypes.SnapshotItem;
import com.github.ohohcakester.grid.GridAndGoals;
import com.github.ohohcakester.grid.GridGraph;
import com.github.ohohcakester.grid.StartGoalPoints;
import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;
import draw.KeyToggler;
import main.utility.Utility;
import uiandio.CloseOnExitWindowListener;

import javax.swing.JFrame;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Visualisation {

	public static void run() {
		traceAlgorithm();
	}

	/**
	 * Conducts a trace of the current algorithm
	 */
	private static void traceAlgorithm() {
		AlgoFunction algo = AnyAnglePathfinding.setDefaultAlgoFunction();           // choose an algorithm (go into this method to choose)
		GridAndGoals gridAndGoals = AnyAnglePathfinding.loadMaze();   // choose a grid (go into this method to choose)

		// Call this to record and display the algorithm in operation.
		displayAlgorithmOperation(algo, gridAndGoals.gridGraph, gridAndGoals.startGoalPoints);
	}

	/**
	 * Records the algorithm, the final path computed, and displays a trace of the algorithm.<br>
	 * Note: the algorithm used is the one specified in the algoFunction.
	 * Use setDefaultAlgoFunction to choose the algorithm.
	 *
	 * @param gridGraph the grid to operate on.
	 */
	private static void displayAlgorithmOperation(AlgoFunction algo, GridGraph gridGraph, StartGoalPoints p) {
		GridLineSet gridLineSet = new GridLineSet();

		try {
			List<Point> path = Utility.generatePath(algo, gridGraph, p.sx, p.sy, p.ex, p.ey);

			for (int i = 0; i < path.size() - 1; i++) {
				gridLineSet.addLine(path.get(i).getX(), path.get(i).getY(),
						path.get(i + 1).getX(), path.get(i + 1).getY(), Color.BLUE);
			}
			double pathLength = Utility.computePathLength(gridGraph, path);
			System.out.println("Path Length: " + pathLength);

			boolean isTaut = Utility.isPathTaut(gridGraph, path);
			System.out.println("Is Taut: " + (isTaut ? "YES" : "NO"));

			System.out.println(Arrays.deepToString(path.toArray(new Point[path.size()])));
		} catch (Exception e) {
			System.out.println("Exception occurred during algorithm operation!");
			e.printStackTrace();
		}

		ArrayList<GridObjects> lineSetList = recordAlgorithmOperation(algo, gridGraph, p.sx, p.sy, p.ex, p.ey);
		lineSetList.add(new GridObjects(gridLineSet, null));
		DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
		drawCanvas.setStartAndEnd(p.sx, p.sy, p.ex, p.ey);

		setupMainFrame(drawCanvas, lineSetList);
	}

	/**
	 * Records a trace of the current algorithm into a LinkedList of GridObjects.
	 */
	private static ArrayList<GridObjects> recordAlgorithmOperation(AlgoFunction<Point> algoFunction,
	                                                               GridGraph gridGraph, int sx, int sy, int ex, int ey) {
		final PathFindingAlgorithm<Point> algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey, Point::new);

		final PathFindingRecorder recorder;
		if (algo instanceof BaseAStar) {
			recorder = new BaseAStarRecorder<>((BaseAStar<Point>) algo);
			algo.setRecorder(recorder);
		} else if (algo instanceof Anya) {
			recorder = new AnyaRecorder<>((Anya<Point>) algo);
			algo.setRecorder(recorder);
		} else {
			throw new RuntimeException("Unknown algorithm");
		}

		recorder.startRecording();
		try {
			algo.computePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		recorder.stopRecording();
		List<List<SnapshotItem>> snapshotList = recorder.retrieveSnapshotList();
		ArrayList<GridObjects> gridObjectsList = new ArrayList<>();
		for (List<SnapshotItem> snapshot : snapshotList) {
			gridObjectsList.add(GridObjects.create(snapshot));
		}
		return gridObjectsList;
	}

	/**
	 * Spawns the visualisation window for the algorithm.
	 */
	protected static void setupMainFrame(DrawCanvas drawCanvas, ArrayList<GridObjects> gridObjectsList) {
		KeyToggler keyToggler = new KeyToggler(drawCanvas, gridObjectsList);

		JFrame mainFrame = new JFrame();
		mainFrame.add(drawCanvas);
		mainFrame.addKeyListener(keyToggler);
		mainFrame.addWindowListener(new CloseOnExitWindowListener());
		mainFrame.setResizable(false);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}
}
