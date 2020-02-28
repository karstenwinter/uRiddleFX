package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import uriddle.logic.Direction;
import uriddle.logic.Level;

import java.util.List;

import static uriddle.logic.Level.State.REACHED_EXIT;

public class Main extends Application implements EventHandler<KeyEvent> {

  private WritableImage writableImage;

  private List<String> levels;
  private Level level;
  private int index = 0;
  private Stage primaryStage;
  private ImageView imageView;

  @Override
  public void start(Stage primaryStage) throws Exception {
    //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
    this.primaryStage = primaryStage;
    //root.addEventHandler(KeyEvent.KEY_PRESSED, this);
    VBox root = new VBox();
    // BMPImageWriter writer = new BMPImageWriter();
    //writer.write(new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR));

    writableImage = new WritableImage(800, 800);

    // image = new Image(getClass().getResource("bg.png").openStream());

    imageView = new ImageView(writableImage);
    imageView.setSmooth(true);
    root.getChildren().add(imageView);
//        e.addEventHandler(KeyEvent.KEY_PRESSED, this);
    TextField f = new TextField();
    root.getChildren().add(f);
    f.setOnKeyReleased(this);

    // root.setOnKeyPressed(this);
    f.setOpacity(0.0);
    root.setBackground(new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null)));
    primaryStage.setScene(new Scene(root, 600, 600));
    primaryStage.show();

    levels = Game.getLevels();
    level = Game.get(levels, index);
    updateView();
  }

  private void updateView() {
    primaryStage.setTitle("uRiddle level " + level.id + " (" + level.name + ")");
    /*writableImage
            .getPixelWriter()
            .setPixels(
                    0, 0,
                    (int) writableImage.widthProperty().get(),
                    (int) writableImage.heightProperty().get(),
                    writableImage.getPixelReader(),
                    0, 0);*/


    writableImage = new WritableImage(800, 800);

    // image = new Image(getClass().getResource("bg.png").openStream());

    imageView.setImage(writableImage);

    Game.drawToBitmap(level, writableImage);
  }

  @Override
  public void handle(KeyEvent event) {
    //int x = (int) (Math.random() * 100);
    //int y = (int) (Math.random() * 100);
    //writableImage.getPixelWriter().setArgb(x, y, 0xFF000000);
    //System.out.println(event);
    String read = event.getText().toLowerCase();
    if (read.equals("n")) {
      index++;
      level = Game.get(levels, index);
      updateView();
    }
    if (read.equals("p")) {
      index--;
      if (index < 0) {
        index = levels.size() - 1;
      }
      level = Game.get(levels, index);
      updateView();
    }

    Direction d = Game.getDir(read);
    if (d != null) {
      Level.State go = level.go(d);
      //System.out.println(level.toString());
      if (go == REACHED_EXIT) {
        System.out.println("You reached the exit! Well done!");
        index++;
        level = Game.get(levels, index);
      }
      updateView();
    }
  }


  public static void main(String[] args) {
    launch(args);
  }

}
