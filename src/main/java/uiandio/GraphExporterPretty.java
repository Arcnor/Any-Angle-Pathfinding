package uiandio;

import com.github.ohohcakester.grid.GridGraph;

public class GraphExporterPretty extends GraphExporter {

	public GraphExporterPretty(GridGraph gridGraph) {
		super(gridGraph);
	}

	@Override
	protected String currentLineToString() {
		StringBuilder sb = new StringBuilder("|");
		String delim = "";
		for (int i = 0; i < gridGraph.getSizeX(); i++) {
			char value = gridGraph.isBlocked(i, line) ? '@' : '-';
			sb.append(delim).append(value);
			delim = " ";
		}
		sb.append("|");
		return sb.toString();
	}


}
