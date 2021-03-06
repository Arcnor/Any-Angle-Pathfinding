package main;

import com.github.ohohcakester.datatypes.SnapshotItem;
import com.github.ohohcakester.grid.GridGraph;
import com.github.ohohcakester.grid.StartGoalPoints;
import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;
import uiandio.GraphImporter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TextOutputVisualisation {

	public static void run() {
		loadFromFile("anyacont2b.txt");
	}

	private static void loadFromFile(String mazeFileName) {
		String textData = readStandardInput();

		GridGraph gridGraph = GraphImporter.importGraphFromFile(mazeFileName);
		StartGoalPoints p = new StartGoalPoints(0, 0, 0, 0);
		displayTextVisualisation(gridGraph, p, textData);
	}

	private static String readStandardInput() {
		Scanner sc = new Scanner(System.in);
		StringBuilder sb = new StringBuilder();
		String line = sc.nextLine();
		while (!"#".equals(line)) {
			sb.append(line);
			sb.append("\n");
			line = sc.nextLine();
		}
		return sb.toString();
	}

	private static void displayTextVisualisation(GridGraph gridGraph, StartGoalPoints p, String textData) {
		GridLineSet gridLineSet = new GridLineSet();

		String[] args = textData.split("\n");
		int[][] paths = new int[args.length][];
		for (int i = 0; i < args.length; ++i) {
			String[] tokens = args[i].split(" ");
			int[] path = new int[tokens.length];
			for (int j = 0; j < tokens.length; ++j) {
				path[j] = Integer.parseInt(tokens[j]);
			}
			paths[i] = path;
		}

		ArrayList<GridObjects> lineSetList = new ArrayList<>();
		for (int i = 0; i < args.length; ++i) {
			List<SnapshotItem> snapshot = new ArrayList<>();
			for (int j = 0; j <= i; ++j) {
				Color col = Color.red;
				if (j == i) col = Color.green;

				SnapshotItem e = SnapshotItem.Companion.generate(paths[j], col);
				snapshot.add(e);
			}

			lineSetList.add(GridObjects.create(snapshot));
		}

		lineSetList.add(new GridObjects(gridLineSet, null));
		DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
		drawCanvas.setStartAndEnd(p.sx, p.sy, p.ex, p.ey);

		Visualisation.setupMainFrame(drawCanvas, lineSetList);
	}
}
