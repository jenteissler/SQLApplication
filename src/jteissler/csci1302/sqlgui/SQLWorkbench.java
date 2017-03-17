package jteissler.csci1302.sqlgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import jteissler.csci1302.simplesql.AssignmentParser;
import jteissler.csci1302.simplesql.Log;
import jteissler.csci1302.simplesql.Pair;
import jteissler.csci1302.simplesql.Parser;
import jteissler.csci1302.simplesql.SQLDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author J Teissler
 * @date 3/15/17
 */
public class SQLWorkbench
{
	private Stage preferencesStage;
	private Stage aboutStage;
	private CommandSelector selector;
	private Parser sql;

	private Image success;
	private Image failure;

	@FXML
	private Window parentWindow;

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
	private TreeItem<String> structureRoot;

	@FXML
	private TextArea commandField;

	@FXML
	private TextArea outputField;

	@FXML
	private ListView<String> commandLog;
	private ObservableList<String> commands = FXCollections.observableArrayList();

	@FXML
	private void initialize()
	{
		success = new Image("file:resources/status.png");
		failure = new Image("file:resources/error.png");

		sql = new AssignmentParser(new SQLDatabase());
		selector = new CommandSelector(commandField);

		commandLog.setItems(commands);
/*
		statusLog.setCellFactory((ListView<Pair<Boolean, String>> list) -> new ListCell<Pair<Boolean, String>>()
		{
			@Override
			public void updateItem(Pair<Boolean, String> item, boolean empty)
			{
				super.updateItem(item, empty);

				if (!empty || item != null)
				{
					Rectangle rect = new Rectangle(20, 20);
					rect.setFill(Color.PURPLE);
					setGraphic(rect);
					setText(item.getRight());
				}
				else
				{
					setGraphic(null);
					setText(null);
				}
			}
		});*/


		Log.setStatusWriter((command, status) ->
		{
			commands.add(command);
			commandLog.scrollTo(commands.size() - 1);
			outputField.setText(status);
			outputField.positionCaret(status.length());
		});

		Log.setErrorWriter((command, status) ->
		{
			commands.add(command);
			commandLog.scrollTo(commands.size() - 1);
			outputField.setText(status);
			outputField.positionCaret(status.length());
		});

		structureRoot = new TreeItem<>("SQL Database Schema");
		structure.setRoot(structureRoot);
		walkDirectoryTree();
		structureRoot.setExpanded(true);
	}

	@FXML
	private void onRunSingleCommandPressed(ActionEvent event)
	{
		sql.parse(selector.getCommand());
		walkDirectoryTree();
	}

	@FXML
	private void onRunAllCommandsPressed(ActionEvent event)
	{
		sql.parse(selector.getAllCommands());
		walkDirectoryTree();
	}

	@FXML
	private void onClearCommandsPressed(ActionEvent event)
	{
		commandField.clear();
	}

	private void walkDirectoryTree()
	{
		structureRoot.getChildren().clear();
		File dir = new File(WorkbenchOptions.MASTER_DIRECTORY);
		File[] files = dir.listFiles();

		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			if (file.isDirectory())
			{
				TreeItem<String> database = new TreeItem<>(file.getName());
				File[] subFiles = file.listFiles();

				for (File subFile : subFiles)
				{
					if (subFile.isFile() && subFile.getName().endsWith("." + WorkbenchOptions.TABLE_FILE_EXTENSION))
					{
						database.getChildren().add(new TreeItem<>(subFile.getName().replace("." + WorkbenchOptions.TABLE_FILE_EXTENSION, "")));
					}
				}

				structureRoot.getChildren().add(database);
				database.setExpanded(true);
			}
		}
	}

	@FXML
	private void onAboutPressed(ActionEvent event)
	{
		if (aboutStage == null)
		{
			try
			{
				Parent root = FXMLLoader.load(getClass().getResource("resources/about.fxml"));
				Scene scene = new Scene(root, 600, 400);
				aboutStage = new Stage();
				aboutStage.setScene(scene);
				String css = getClass().getResource("resources/workbench.css").toExternalForm();
				scene.getStylesheets().add(css);
				aboutStage.setTitle("SQL Workbench - About");
				aboutStage.setOnCloseRequest(e -> preferencesStage = null);
				aboutStage.setResizable(false);
				aboutStage.show();



			}
			catch (IOException e)
			{
				e.printStackTrace();
			}


		}
	}

	@FXML
	private void onOpenScript(ActionEvent event)
	{

		FileChooser fc = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Sql Scripts", "*." + WorkbenchOptions.SAVE_FILE_EXTENSION);

		fc.getExtensionFilters().add(filter);


		File returnVal = fc.showOpenDialog(parentWindow);
		if (returnVal == null)
		{
			return;

		}
		try
		{

			Scanner inFile = new Scanner(returnVal);

			String someString = "";

			if (inFile.hasNext())
			{
				someString = inFile.useDelimiter("\\Z").next();
			}

			commandField.setText(someString);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@FXML
	private void setParent(Window parent)
	{
		parentWindow = parent;
	}

	@FXML
	private void onRunScript(ActionEvent event)
	{

		FileChooser fc = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Sql Scripts", "*." + WorkbenchOptions.SAVE_FILE_EXTENSION);

		fc.getExtensionFilters().add(filter);


		File returnVal = fc.showOpenDialog(parentWindow);
		if (returnVal == null)
		{
			return;

		}
		try
		{

			Scanner inFile = new Scanner(returnVal);

			String someString = "";

			if (inFile.hasNext())
			{
				someString = inFile.useDelimiter("\\Z").next();
			}

			commandField.setText(someString);

			sql.parse(selector.getAllCommands());
			walkDirectoryTree();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}


	@FXML
	private void onSaveScript(ActionEvent event)
	{

		FileChooser fc = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Sql Scripts", "*." + WorkbenchOptions.SAVE_FILE_EXTENSION);

		fc.getExtensionFilters().add(filter);

		File returnVal = fc.showSaveDialog(parentWindow);

		if (returnVal == null)
		{
			return;
		}

		PrintWriter writer = null;
		try
		{
			String someString = commandField.getText();
			writer = new PrintWriter(new FileWriter(returnVal, true));


			writer.print(someString);

			writer.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}


	@FXML
	private void onCut(ActionEvent event)
	{
		commandField.cut();
	}

	@FXML
	private void onCopy(ActionEvent event)
	{
		commandField.copy();
	}

	@FXML
	private void onPaste(ActionEvent event)
	{
		commandField.paste();
	}

	@FXML
	private void onUndo(ActionEvent event)
	{
		commandField.undo();
	}

	@FXML
	private void onRedo(ActionEvent event)
	{
		commandField.redo();
	}

	@FXML
	private void onPreferences(ActionEvent event)
	{
		if (preferencesStage == null)
		{
			try
			{
				Parent root = FXMLLoader.load(getClass().getResource("resources/preferences.fxml"));
				Scene scene = new Scene(root, 517, 331);
				preferencesStage = new Stage();
				preferencesStage.setScene(scene);
				String css = getClass().getResource("resources/workbench.css").toExternalForm();
				scene.getStylesheets().add(css);
				preferencesStage.setTitle("SQL Workbench - Preferences");
				preferencesStage.setResizable(false);
				preferencesStage.setOnCloseRequest(e -> {preferencesStage = null; walkDirectoryTree();});
				preferencesStage.show();


				/*
				Equivalent to
				preferencesStage.setOnCloseRequest(new EventHandler<WindowEvent>()
                {
                    @Override
                    public void handle(WindowEvent event)
                    {
                        preferencesStage = null;
                    }
                });

				 */

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}


		}
	}


}
