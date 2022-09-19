
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Launches Application
 * 
 * @author Maimouna Diallo
 * @version PX
 */
public class main extends Application {

	public static void main(String[] args) {
		//Launch the application
		launch(args);
		}

	@Override
	public void start(Stage primaryStage) throws Exception {
		//Set up the Application
		Client root = new Client(primaryStage);
		Scene scene = new Scene(root,800,700);
		primaryStage.setTitle("PracX");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	
}
