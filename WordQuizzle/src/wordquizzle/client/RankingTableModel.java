/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.client;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import wordquizzle.common.RankEntry;

//modello della tabella contenente la classifica
public class RankingTableModel extends AbstractTableModel {

	private List<RankEntry> ranking; // classifica
	private String[] columnNames = { "User", "Points" }; // nome delle colonne

	public RankingTableModel(List<RankEntry> ranking) {
		this.ranking = ranking;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return ranking.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RankEntry entry = ranking.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return entry.getUsername();
		case 1:
			return entry.getPoints();
		default:
			return null;
		}
	}

}
