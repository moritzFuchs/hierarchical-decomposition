package mf.gui;
	
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.geometry.Side;

/**
 * GUI starting point. Creates the {@link TabView}, a Welcome-Tab and a '+' Button.
 * 
 * @author moritzfuchs
 * @date 04.09.2013
 *
 */
public class Main extends Application {
	
	/**
	 * Launches the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	 /**
	  * Initialize the application. Generates a {@link BorderPane} with a {@link TabPane} on the Top-position.
	  * Adds a 'Welcome'-Tab and a '+'-Button
	  * 
	  * @param primaryStage : The primary stage for the application
	  */
	public void init(Stage primaryStage) {
		primaryStage.setResizable(false);
		
		primaryStage.setTitle("Image Segmentation GUI");
		
		Group root = new Group();
		Scene scene= new Scene(root);
		scene.getStylesheets().add(this.getClass()
				.getResource("style.css").toExternalForm());
		
	    primaryStage.setScene(scene);
	    BorderPane borderPane = new BorderPane();
	    
	    final TabPane tabPane = new TabPane();
	    tabPane.setPrefSize(1200, 600);
	    tabPane.setSide(Side.TOP);
	    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
	    
	    final Tab tabPlus = new Tab();
	    tabPlus.setText("+");
	    tabPlus.setContent(new ProgressIndicator());
	    tabPlus.setClosable(false);
	    tabPlus.setOnSelectionChanged(new NewTabHandler());
	    
	    final Tab tabWelcome = new Tab();
	    tabWelcome.setText("Welcome");
	   
	    FlowPane welcome = new FlowPane();
	    welcome.setPrefWrapLength(100);
	    
	    welcome.setId("welcome_pane");
	    welcome.getStylesheets().add(this.getClass()
				.getResource("welcome.css").toExternalForm());
	    
	    Label title = new Label("WELCOME");
	    title.setId("title");
	    title.setMinWidth(1000);
	    
	    Label content = new Label("Click '+' and select a folder containing 'image.jpg' as well as the decomposition files (e.g. .rst)");
	    content.setId("welcome_content");
	    
	    welcome.getChildren().add(title);
	    welcome.getChildren().add(content);
	    
        tabWelcome.setContent(welcome);
	    tabWelcome.setClosable(false);
	    
	    tabPane.getTabs().addAll(tabWelcome , tabPlus);
	    
	    borderPane.setCenter(tabPane);
	    root.getChildren().add(borderPane);
	}

	 @Override
	 public void start(Stage primaryStage) {
		init(primaryStage);

	    primaryStage.show();
	}
}
