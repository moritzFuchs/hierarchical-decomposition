package mf.gui;
	
import java.io.File;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;

public class Main extends Application {
//	@Override
//	public void start(Stage primaryStage) {
//		try {
//			BorderPane root = new BorderPane();
//			Scene scene = new Scene(root,400,400);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	 
	public void init(Stage primaryStage) {
		primaryStage.setResizable(false);
		
		Group root = new Group();
	    primaryStage.setScene(new Scene(root));
	    BorderPane borderPane = new BorderPane();
	    final TabPane tabPane = new TabPane();
	    tabPane.setPrefSize(1200, 600);
	    tabPane.setSide(Side.TOP);
	    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
	    
	    final Tab tabPlus = new Tab();
	    tabPlus.setText("+");
	    tabPlus.setClosable(false);
	    tabPlus.setOnSelectionChanged(new NewTabHandler());
	    
	    final Tab tabWelcome = new Tab();
	    tabWelcome.setText("Welcome");
	    tabWelcome.setClosable(false);
	    
	    tabPane.getTabs().addAll(tabWelcome , tabPlus);
	    
	    borderPane.setCenter(tabPane);
	    root.getChildren().add(borderPane);
	}
	
	 @Override
	 public void start(Stage primaryStage) {
		init(primaryStage);

	    primaryStage.show();
		 /*
	    primaryStage.setTitle("Load Image");
	        
	    StackPane sp = new StackPane();
	    Image img = new Image("file:/Users/moritzfuchs/Desktop/image.jpg");
	    ImageView imgView = new ImageView(img);
	    sp.getChildren().add(imgView);
	       
	    imgView.setOnMouseClicked(new ClickHandler());
	        
	    System.out.println(imgView.onMouseClickedProperty());
	        
	    //Adding HBox to the scene
	    Scene scene = new Scene(sp);
	    primaryStage.setScene(scene);
	    primaryStage.show();*/
	}
}
