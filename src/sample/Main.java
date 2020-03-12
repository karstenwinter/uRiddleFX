package sample;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import uriddle.logic.*;

import java.util.List;

import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Block.BlockType.DEFAULT;
import static uriddle.logic.Level.State.REACHED_EXIT;
import static uriddle.logic.U.UType.OBSTACLE;

public class Main extends Application implements EventHandler<KeyEvent> {

  private WritableImage writableImage;

  private List<String> levels;
  private Level level;
  private int index = 0;
  private Stage primaryStage;
  private ImageView imageView;
  private Block inputBlock;
  private ImageView inputBlockView;
  private VBox editorPane;
  private Button saveButton;

  final String saveText = "Save to Clip";
  private Level levelClone;

  @Override
  public void start(Stage primaryStage) throws Exception {
    //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
    this.primaryStage = primaryStage;
    //root.addEventHandler(KeyEvent.KEY_PRESSED, this);
    VBox root = new VBox();
    // BMPImageWriter writer = new BMPImageWriter();
    //writer.write(new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR));

    // image = new Image(getClass().getResource("bg.png").openStream());

//        e.addEventHandler(KeyEvent.KEY_PRESSED, this);

    // root.setOnKeyPressed(this);

    Background blackBg = new Background(new BackgroundFill(Paint.valueOf("#000000"), null, null));
    root.setBackground(blackBg);
    primaryStage.setScene(new Scene(root, 1000, 900));
    primaryStage.show();

    levels = Game.getLevels();
    level = Game.get(levels, index);
    // TextField keyboardInput = new TextField();
    //keyboardInput.setOnKeyReleased(this);
    //keyboardInput.setOpacity(0.0);

    // ToolBar toolBar = new ToolBar();

    FlowPane toolBar = new FlowPane();
    toolBar.setBackground(blackBg);

    for (Block.BlockType value : Block.BlockType.values()) {
      Block block = new Block();
      block.type = value;
      if (value == DOOR) {
        for (Door.DoorType d : Door.DoorType.values()) {
          block.door = new Door(d, 1);
          addToolbarElement(toolBar, block);
          block.door = new Door(d, 2);
          addToolbarElement(toolBar, block);
        }
      } else if (value == PORTAL) {
        for (Direction d : Direction.values()) {
          block.portal = new Portal(1, d);
          addToolbarElement(toolBar, block);
          block.portal = new Portal(2, d);
          addToolbarElement(toolBar, block);
        }
      } else if (value == DEFAULT) {
        addToolbarElement(toolBar, block);
        for (Direction d : Direction.values()) {
          for (U.UType uType : U.UType.values()) {
            block.smallU = null;
            block.bigU = new U(uType, d);
            addToolbarElement(toolBar, block);
            block.bigU = null;
            block.smallU = new U(uType, d);
            addToolbarElement(toolBar, block);
          }
        }
      } else if (value == SWITCH) {
        block.switchVal = new Switch(1);
        addToolbarElement(toolBar, block);
        block.switchVal = new Switch(2);
        addToolbarElement(toolBar, block);
      } else {
        addToolbarElement(toolBar, block);
      }
    }
    // image = new Image(getClass().getResource("bg.png").openStream());

    writableImage = new WritableImage(900, 900);

    imageView = new ImageView(writableImage);
    imageView.setSmooth(true);

    //toolBar.setVgap(8);
    //toolBar.setHgap(4);
    toolBar.setPrefWrapLength(300);

    /*ScrollPane scrollPane = new ScrollPane(toolBar);
    scrollPane.setBackground(blackBg);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);*/


    toolBar.setMinWidth(310);
    toolBar.setMinHeight(700);

    editorPane = new VBox();
    saveButton = buttonIo(saveText, true);
    fillEditorPane();
    HBox hBox = new HBox(
            new VBox(
                    label("Select"),
                    toolBar),
            new VBox(
                    label("Edit"),
                    editorPane,
                    label("Test"),
                    imageView)
    );
    root.getChildren().add(
            new VBox(
                    new HBox(
                            label("Box Code Editor v1"),
                            new Label("  "),
                            label("Manage"),
                            saveButton,
                            buttonIo("Load from Clip", false),
                            new Label("  "),
                            label("Resize"),
                            buttonSize(-1, 0),
                            buttonSize(1, 0),
                            buttonSize(0, -1),
                            buttonSize(0, 1),
                            label(" wasd; move, (r)eset, samples: (n)ext, (p)rev")
                    ),
                    hBox
            )
    );
    root.setOnKeyReleased(this);

    updateView();
  }

  Node buttonSize(int dx, int dy) {
    String text = dx == 0
            ?
            dy > 0 ? "vv" // ""Increase Height"
                    : "^^" //"Decrease Height"
            : dx > 0 ? ">>" // "Increase Width"
            : "<<"; // ""Decrease Width";

    Button button = new Button(text);

    button.setOnMouseClicked(e -> {
      Block e2 = new Block();
      e2.type = BOUNDS;
      if (dy > 0) {
        Row e1 = new Row();
        int width = levelClone.rows.get(0).cols.size();
        for (int i = 0; i < width; i++) {
          e1.cols.add(e2.clone());
        }
        levelClone.rows.add(e1);
      } else if (dy < 0 && levelClone.rows.size() > 1) {
        levelClone.rows.remove(levelClone.rows.size() - 1);
      }
      if (dx > 0) {
        for (Row r : levelClone.rows) {
          r.cols.add(e2.clone());
        }
      } else if (dx < 0 && levelClone.rows.get(0).cols.size() > 1) {
        for (Row r : levelClone.rows) {
          r.cols.remove(r.cols.size() - 1);
        }
      }

      level = levelClone;
      updateView();
      fillEditorPane();
    });
    return button;
  }

  Button buttonIo(String text, boolean saveMode) {
    Button button = new Button(text);
    Clipboard clipboard = Clipboard.getSystemClipboard();
    button.setOnMouseClicked(e -> {
      if (saveMode) {
        ClipboardContent content = new ClipboardContent();
        content.putString(level.toString());
        clipboard.setContent(content);
        button.setText("Saved!");

      } else {
        if (clipboard.hasString()) {
          String str = clipboard.getString();
          level = LevelReader.instance.fromString(str);
          updateView();
          fillEditorPane();
        }
      }
    });
    return button;
  }

  Label label(String text) {
    Label label = new Label(text);
    Font aDefault = Font.getDefault();
    label.setPadding(new Insets(4, 4, 4, 4));
    label.setFont(new Font(aDefault.getName(), 20));
    label.setTextFill(Color.web("#ffffff"));
    return label;
  }

  void fillEditorPane() {
    int y = -1;
    editorPane.getChildren().clear();
    levelClone = this.level.clone();
    for (Row r : levelClone.rows) {
      y++;
      HBox hbox = new HBox();
      int x = -1;
      for (Block block : r.cols) {
        x++;
        final int xNow = x;
        WritableImage eImg = new WritableImage(70, 70);
        Game.drawToBitmap(new Level("", "", new Row(block)), eImg);
        final ImageView img = new ImageView(eImg);
        // img.setOnKeyReleased(this);

        final int yNow = y;

        img.setOpacity(0.8);
        img.setOnMouseEntered(e -> img.setOpacity(1.0));
        img.setOnMouseExited(e -> img.setOpacity(0.8));

        img.setOnMouseClicked((MouseEvent event) -> {
          levelClone.rows.get(yNow).cols.set(xNow, inputBlock.clone());
          Game.drawToBitmap(new Level("", "", new Row(inputBlock)), eImg);
          img.setImage(eImg);
          level = levelClone.clone();
          updateView();
          saveButton.setText(saveText);
        });
        hbox.getChildren().add(img);
      }
      editorPane.getChildren().add(hbox);
    }
  }

  void addToolbarElement(FlowPane toolBar, Block block0) {
    Block block = block0.clone();

    WritableImage eImg = new WritableImage(70, 70);
    Game.drawToBitmap(new Level("", "", new Row(block)), eImg);
    ImageView e = new ImageView(eImg);
    // e.setOnKeyReleased(this);

    e.setOpacity(0.6);
    if (inputBlockView == null) {
      inputBlockView = e;
      inputBlock = block;
      e.setOpacity(1.0);
    }
    e.setCursor(Cursor.HAND);
    e.setOnMouseClicked((MouseEvent event) -> {
      System.out.println("select " + block);
      if (inputBlock != block) {

        inputBlockView.setOpacity(0.6);

        inputBlock = block;
        inputBlockView = e;
        e.setOpacity(1.0);
      }
    });

    toolBar.getChildren().add(e);
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

    // if (writableImage == null) {
    writableImage = new WritableImage(900, 900);

    //}
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
    if (read.equals("n") || read.equals("r")) {
      if (read.equals("n")) {
        index++;
      }
      level = Game.get(levels, index);
      fillEditorPane();
      updateView();
    }
    if (read.equals("p")) {
      index--;
      if (index < 0) {
        index = levels.size() - 1;
      }
      level = Game.get(levels, index);
      fillEditorPane();
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
