package jteissler.csci1302.sqlgui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;

/**
 * @author J Teissler
 * @date 3/15/17
 */
public class SQLWorkbench
{
	@FXML
	private MenuItem openScript;

	@FXML
	private MenuItem runScript;

	@FXML
	private MenuItem saveScript;

	@FXML
	private MenuItem cut;

	@FXML
	private MenuItem copy;

	@FXML
	private MenuItem paste;

	@FXML
	private MenuItem preferences;

	@FXML
	private MenuItem undo;

	@FXML
	private MenuItem redo;

	@FXML
	private MenuItem about;

	@FXML
	private Button runSingleCommand;

	@FXML
	private Button runAllCommands;

	@FXML
	private Button clearCommands;

	@FXML
	private TreeView<String> structure;

	@FXML
	private TextArea commandField;

	@FXML
	private ListView commandLog;

	@FXML
	private ListView statusLog;

	@FXML
	private void initialize()
	{

	}



}