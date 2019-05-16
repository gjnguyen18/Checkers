package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Group root = new Group();
			Pane background = new Pane();
			background.setId("backgroundColor");
			background.getChildren().add(root);
			Scene scene = new Scene(background,Driver.Width,Driver.Height);
			primaryStage.setResizable(false);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Checkers");
			primaryStage.show();
			
			CheckersVsBot board = new CheckersVsBot(root);
			board.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
