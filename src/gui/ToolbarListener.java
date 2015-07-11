package gui;

import java.sql.SQLException;

public interface ToolbarListener {
	public void saveEventOccured() throws SQLException;
	public void refreshEventOccured();

}
