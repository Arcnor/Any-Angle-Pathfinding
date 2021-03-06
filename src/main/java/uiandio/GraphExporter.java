package uiandio;

import com.github.ohohcakester.grid.GridGraph;

/**
 * Used to convert a GridGraph to string, line-by-line.<br>
 * use hasNextLine() and nextLine() to read the string.<br>
 * The output format is the type that is read by GraphImporter.
 */
public class GraphExporter {
	protected final int maxLines;
	protected final GridGraph gridGraph;
	protected int line = -1;

	public GraphExporter(GridGraph gridGraph) {
		this.gridGraph = gridGraph;
		this.maxLines = gridGraph.getSizeY();
	}

	/**
	 * @return true iff there are more lines to read.
	 */
	public boolean hasNextLine() {
		return line < maxLines;
	}

	/**
	 * @return the next line in the string.
	 * Throws an exception if there are no more lines.
	 */
	public String nextLine() {
		String result = null;
		if (line == -1) {
			result = gridGraph.getSizeX() + " " + gridGraph.getSizeY();
		} else {
			result = currentLineToString();
		}
		line++;
		return result;
	}

	protected String currentLineToString() {
		StringBuilder sb = new StringBuilder();
		String delim = "";
		for (int i = 0; i < gridGraph.getSizeX(); i++) {
			int value = gridGraph.isBlocked(i, line) ? 1 : 0;
			sb.append(delim).append(value);
			delim = " ";
		}
		return sb.toString();
	}

}
