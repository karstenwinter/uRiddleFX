package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Block.BlockType.DEFAULT;
import static uriddle.logic.Level.State.REACHED_EXIT;

public class Main extends Application implements EventHandler<KeyEvent> {

  private WritableImage writableImage;
  private List<Level> levels = new ArrayList<Level>();
  private Level levelToPlay;
  private int index = 0;
  private Stage primaryStage;
  private ImageView imageView;
  private Block inputBlock;
  private ImageView inputBlockView;
  private VBox editorPane;
  private Button saveButton;

  final String saveText = "Save to Clip";
  final String try_solve = "Try Solve";

  private Level levelToEdit;
  private ScrollPane packList;
  private ScrollPane levelList;
  private Path currentFile;
  private Path sampleLevels = Paths.get("p99-sample-levels.txt");
  private FlowPane toolBar;
  private Button solveButton;
  private boolean animations;

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
    primaryStage.setScene(new Scene(root, 1100, 900));
    primaryStage.show();


    // TextField keyboardInput = new TextField();
    //keyboardInput.setOnKeyReleased(this);
    //keyboardInput.setOpacity(0.0);

    // ToolBar toolBar = new ToolBar();

    toolBar = new FlowPane();
    toolBar.setBackground(blackBg);

    refreshToolbar();
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


    toolBar.setMinWidth(400);
    toolBar.setMinHeight(700);

    editorPane = new VBox();
    saveButton = buttonIo(saveText, true);

    HBox editorArea = new HBox(
            new VBox(
                    label("Select"),
                    toolBar),
            new VBox(
                    label("Edit"),
                    editorPane,
                    label("Test"),
                    imageView)
    );

    packList = newIoList(true);

    readFile(sampleLevels);

    levelList = newIoList(false);

    VBox manage = new VBox(
            label("Quicksave"),
            new HBox(
                    saveButton,
                    buttonIo("Load from Clip", false)
            ),
            label("Pack list"),
            packList,
            label("Level list"),
            levelList
    );
    HBox belowTitle = new HBox(
            manage,
            editorArea
    );
    manage.setMinWidth(300);
    solveButton = new Button(); // createSolveButton();

    final String s = "animations (beta) are ";
    Button button = new Button(s + "off");
    button.setOnMouseClicked(x -> {
      this.animations = !this.animations;
      button.setText(s + (this.animations ? "on" : "off"));
    });
    HBox titleResize = new HBox(
            label("Box Code Editor v3"),
            createGap(),
            label("Resize"),
            buttonSize(-1, 0),
            buttonSize(1, 0),
            buttonSize(0, -1),
            buttonSize(0, 1),
            label(" scale"),
            buttonScale(-1),
            buttonScale(1),
            label(" wasd: move, (r)eset, samples: (n)ext, (p)rev"),
            //solveButton
            button
    );
    root.getChildren().add(
            new VBox(
                    titleResize,
                    belowTitle
            )
    );
    root.setOnKeyReleased(this);
    if (levels.isEmpty()) {
      levels = parseLevels(Game.getLevels());
    }

    select(levels.get(index));
    fillEditorPane();
    updateView();
  }

  Button createSolveButton() {
    Button solve = new Button(try_solve);
    solve.setOnMouseClicked(e -> {

      Solver solver = new Solver();
      //solver.levelConsumer = x -> {
        /*Platform.runLater(() -> {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
          Game.drawToBitmap(x, writableImage);
          imageView.setImage(writableImage);
        });*/
      //    System.out.println(x);
      //  };
      solver.verbose = true;
      Res res = solver.solve(levelToEdit);
      solve.setText(res.solved ? "Solved, steps: " + res.iterations : "Not solved within " + solver.maxIt);
    });
    return solve;
  }

  private void refreshToolbar() {
    toolBar.getChildren().clear();
    for (Block.BlockType value : Block.BlockType.values()) {
      Block block = new Block();
      block.type = value;

      if (value == ONEWAY) {
        for (OneWay.OneWayType type : OneWay.OneWayType.values()) {
          for (Direction d : Direction.values()) {
            block.oneWay = new OneWay(type, d);
            addToolbarElement(toolBar, block);
          }
        }
      } else if (value == RYTHM) {
        for (int i = 1; i <= 4; i++) {
          block.num = i;
          addToolbarElement(toolBar, block);
        }
      } else if (value == DOOR) {
        for (Door.DoorType d : Door.DoorType.values()) {
          block.door = new Door(d);
          block.num = 1;
          addToolbarElement(toolBar, block);
          block.door = new Door(d);
          block.num = 2;
          addToolbarElement(toolBar, block);
        }
      } else if (value == PORTAL) {
        for (Direction d : Direction.values()) {
          block.portal = new Portal(d);
          block.num = 1;
          addToolbarElement(toolBar, block);
          block.portal = new Portal(d);
          block.num = 2;
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
        block.switchVal = new Switch(Switch.SwitchType.TOGGLE);
        block.num = 1;
        addToolbarElement(toolBar, block);
        block.switchVal = new Switch(Switch.SwitchType.TOGGLE);
        block.num = 2;
        addToolbarElement(toolBar, block);
      } else {
        addToolbarElement(toolBar, block);
      }
    }
  }

  List<Level> parseLevels(List<String> levels) {
    return levels.stream()
            .map(LevelReader.instance::fromString)
            .collect(Collectors.toList());
  }

  ScrollPane newIoList(boolean pack) {
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setMaxHeight(pack ? 150 : 350);
    VBox vBox = new VBox();
    if (!pack) {
      createLevelList(vBox, sampleLevels);
    } else {
      createPackList(vBox);
    }
    scrollPane.setContent(vBox);
    return scrollPane;
  }

  void createPackList(VBox vBox) {
    Button refresh = new Button("Refresh");
    EventHandler<MouseEvent> mouseEventEventHandler = event -> {
      vBox.getChildren().clear();
      vBox.getChildren().add(refresh);

      List<Path> files = listTextFiles();
      if (files.stream().noneMatch(x -> x.toFile().getName().equals(sampleLevels.toFile().getName()))) {
        Stream<Level> levels = Stream.empty();
        try {
          levels = Game.getLevels().stream()
                  .filter(x -> x.startsWith("i") || x.startsWith("l"))
                  .map(LevelReader.instance::fromString);
        } catch (Exception e) {
          e.printStackTrace();
        }
        Path sampleLevels = this.sampleLevels;

        writeLevelListTo(levels, sampleLevels);
        files = listTextFiles();
      }
      for (Path file : files) {
        HBox hBox = new HBox();
        hBox.setUserData(file);

        Button e = new Button("Load");
        e.setOnMouseClicked(event2 -> {
          VBox vBox1 = (VBox) levelList.getContent();
          createLevelList(vBox1, file);
        });
        hBox.getChildren().add(e);
        Button dupFile = new Button("Duplicate");
        dupFile.setOnMouseClicked(event2 -> {
          File target = Paths.get(file.getParent().toString(),
                  "dup-" + System.currentTimeMillis() + "-" + file.toFile().getName()).toFile();
          try {
            Files.copy(file, new FileOutputStream(target));
          } catch (IOException ex) {
            ex.printStackTrace();
          }
          refresh.getOnMouseClicked().handle(null);
        });
        hBox.getChildren().add(dupFile);

        Label textField = new Label(file.toFile().getName());
        hBox.getChildren().add(textField);

        vBox.getChildren().add(hBox);
      }
    };
    refresh.setOnMouseClicked(mouseEventEventHandler);
    mouseEventEventHandler.handle(null);
  }

  void createLevelList(VBox vBox, Path fileToRead) {
    this.currentFile = fileToRead;

    if (fileToRead != null) {
      readFile(fileToRead);
    }
    vBox.getChildren().clear();

    Button saveButton2 = new Button("Save Pack");
    saveButton2.setOnMouseClicked(e -> {
      levels = vBox.getChildren().stream()
              .filter(x -> x.getUserData() != null)
              .map(x -> ((Level) x.getUserData()).clone())
              .collect(Collectors.toList());
      saveCurrentPack();
    });
    vBox.getChildren().add(saveButton2);

    int index = -1;
    for (Level levelForInit : levels) {
      index++;
      //  if (levelStr.startsWith("i") || levelStr.startsWith("l")) {
      HBox hBox = new HBox();
      hBox.setUserData(levelForInit);

      Button e = new Button("Select");
      e.setOnMouseClicked(event -> {
        Level userData = (Level) hBox.getUserData();
        select(userData);
        updateView();
        fillEditorPane();
      });
      hBox.getChildren().add(e);
     /* Button saveBtn = new Button("Save");
      saveBtn.setOnMouseClicked(ev2 -> {
        hBox.setUserData(level.clone());
      });
      // saveBtn.setDisable(!current);
      hBox.getChildren().add(saveBtn);
      */
      Button delButton = new Button("Delete");
      delButton.setOnMouseClicked(event -> {
        if (levels.size() > 1) {
          Level userData = (Level) hBox.getUserData();
          levels = levels.stream()
                  .filter(x -> !x.id.equals(userData.id))
                  .collect(Collectors.toList());

          saveCurrentPack();
          createLevelList(vBox, this.currentFile);
        }
      });
      hBox.getChildren().add(delButton);

      Button dupFile = new Button("Duplicate");
      // saveBtn.setDisable(!current);
      final int indexNow = index;
      dupFile.setOnMouseClicked(event2 -> {
        Level userData = (Level) hBox.getUserData();
        Level clone = userData.clone();
        clone.id = "dup-" + clone.id + "-" + System.currentTimeMillis();
        levels.add(indexNow, clone);
        saveCurrentPack();
        createLevelList((VBox) levelList.getContent(), this.currentFile);
      });
      hBox.getChildren().add(dupFile);

      TextField textFieldID = new TextField(levelForInit.id);
      textFieldID.setMaxWidth(60);
      textFieldID.setOnKeyReleased(ev -> {
        Level userData = (Level) hBox.getUserData();
        userData.id = textFieldID.getText();
      });
      //  boolean current = level.id == l.id;
      //  textFieldID.setEditable(current);
      hBox.getChildren().add(textFieldID);

      TextField textFieldNMW = new TextField(levelForInit.name);
      hBox.getChildren().add(textFieldNMW);
      textFieldNMW.setOnKeyReleased(ev -> {
        Level userData = (Level) hBox.getUserData();
        userData.name = textFieldNMW.getText();
      });

      vBox.getChildren().add(hBox);
    }
    vBox.getChildren().add(createGap());
  }

  void select(Level userData) {
    levelToEdit = userData;
    levelToPlay = userData.clone();
  }

  private Label createGap() {
    return new Label("  ");
  }

/*  void updateCurrentLevelInList() {
    levels = levels.stream()
            .map(x -> x.id.equals(level.id) ? level.clone() : x)
            .collect(Collectors.toList());
  }*/

  void writeLevelListTo(Stream<Level> levels, Path file) {
    try {
      /*Files.copy(
              Game.getResourceAsStream(),
              Paths.get("p99-sample-levels.txt"),
              StandardCopyOption.REPLACE_EXISTING);*/

      String str = levels
              .map(Level::toString)
              .reduce((a, b) -> a + "\n" + b)
              .get() + "\nEND";

      str = str.replaceAll("\r", "");
      if (str.endsWith("\n\n")) {
        str = str.substring(0, str.length() - 2);
      }
      Files.write(
              file,
              Arrays.asList(str)
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void saveCurrentPack() {
    writeLevelListTo(
            levels.stream(),
            this.currentFile
    );
  }

  void readFile(Path fileToRead) {
    try {
      levels = parseLevels(Game.getLevels(Files.newBufferedReader(fileToRead)));
      System.out.println(levels.size() + " levels read from " +
              fileToRead + " (length: " + fileToRead.toFile().length() + ")");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  List<Path> listTextFiles() {
    List<Path> lines = Arrays.asList();
    try {
      lines = Files.list(Paths.get("."))
              .filter(x -> x.toFile().getName().endsWith(".txt"))
              .sorted(Comparator.comparing(a -> a.toFile().getName()))
              .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }


  private Node buttonScale(int delta) {
    String text = delta > 0 ? "+" // ""Increase Height"
            : "-";

    Button button = new Button(text);

    button.setOnMouseClicked(e -> {
      if (Game.SCALE > 1 && delta < 0) {
        Game.SCALE--;
      }
      if (Game.SCALE < 20 && delta > 0) {
        Game.SCALE++;
      }


      refreshToolbar();
      select(levelToEdit);
      updateView();
      fillEditorPane();
    });
    return button;
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
        int width = levelToEdit.rows.get(0).cols.size();
        for (int i = 0; i < width; i++) {
          e1.cols.add(e2.clone());
        }
        levelToEdit.rows.add(e1);
      } else if (dy < 0 && levelToEdit.rows.size() > 1) {
        levelToEdit.rows.remove(levelToEdit.rows.size() - 1);
      }
      if (dx > 0) {
        for (Row r : levelToEdit.rows) {
          r.cols.add(e2.clone());
        }
      } else if (dx < 0 && levelToEdit.rows.get(0).cols.size() > 1) {
        for (Row r : levelToEdit.rows) {
          r.cols.remove(r.cols.size() - 1);
        }
      }

      select(levelToEdit);
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
        content.putString(levelToPlay.toString());
        clipboard.setContent(content);
        button.setText("Saved!");

      } else {
        if (clipboard.hasString()) {
          String str = clipboard.getString();
          Level loaded = LevelReader.instance.fromString(str);
          levelToEdit.rows.clear();
          levelToEdit.rows.addAll(loaded.rows);
          levelToEdit.name = loaded.name;
          levelToEdit.id = loaded.id;
          levelToEdit.pixelate = loaded.pixelate;
          select(loaded);
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
    for (Row r : levelToEdit.rows) {
      y++;
      HBox hbox = new HBox();
      int x = -1;
      for (Block block : r.cols) {
        x++;
        final int xNow = x;
        WritableImage eImg = new WritableImage(7 * Game.SCALE, 7 * Game.SCALE);
        Game.drawToBitmap(eImg, LevelWriter.instance.toString(block), false, null);
        final ImageView img = new ImageView(eImg);
        // img.setOnKeyReleased(this);

        final int yNow = y;

        img.setOpacity(0.8);
        img.setOnMouseEntered(e -> img.setOpacity(1.0));
        img.setOnMouseExited(e -> img.setOpacity(0.8));

        img.setOnMouseClicked((MouseEvent event) -> {
          levelToEdit.rows.get(yNow).cols.set(xNow, inputBlock.clone());
          Game.drawToBitmap(eImg, LevelWriter.instance.toString(inputBlock), false, null);
          img.setImage(eImg);
          levelToPlay = levelToEdit.clone();
          updateView();
          solveButton.setText(try_solve);
          saveButton.setText(saveText);
        });
        hbox.getChildren().add(img);
      }
      editorPane.getChildren().add(hbox);
    }
  }

  void addToolbarElement(FlowPane toolBar, Block block0) {
    Block block = block0.clone();

    WritableImage eImg = new WritableImage(7 * Game.SCALE, 7 * Game.SCALE);
    Game.drawToBitmap(eImg, LevelWriter.instance.toString(block), false, null);
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
    primaryStage.setTitle("uRiddle level " + levelToPlay.id + " (" + levelToPlay.name + ")");
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

    //  System.out.println(levelToPlay.toString());
    Game.drawToBitmap(levelToPlay, writableImage);
  }

  @Override
  public void handle(KeyEvent event) {
    //int x = (int) (Math.random() * 100);
    //int y = (int) (Math.random() * 100);
    //writableImage.getPixelWriter().setArgb(x, y, 0xFF000000);
    //System.out.println(event);
    String read = event.getText().toLowerCase();
    if (read.equals("r")) {
      select(levelToEdit);
      fillEditorPane();
      updateView();
    }

    if (read.equals("n")) {
      index++;
      select(levels.get(index));
      fillEditorPane();
      updateView();
    }

    if (read.equals("p")) {
      index--;
      if (index < 0) {
        index = levels.size() - 1;
      }
      select(levels.get(index));
      fillEditorPane();
      updateView();
    }

    Direction d = Game.getDir(read);
    if (d != null) {
      Map.Entry<Level.State, String[]> go = Logic.instance.goWithAnimation(levelToPlay, d);
      //System.out.println(level.toString());
      if (animations) {
        int i = 0;
        for (String s : go.getValue()) {
          final int iFinal = i++;

          Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              try {
                Thread.sleep(iFinal * 10);
              } catch (InterruptedException e) {
              }
              return null;
            }
          };
          sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
              if (s != null) {
                writableImage = new WritableImage(900, 900);
                imageView.setImage(writableImage);
                Game.drawToBitmap(writableImage, s, levelToEdit.pixelate, levelToPlay.counter);
              }
            }
          });
          new Thread(sleeper).start();
        }
      } else {
        updateView();
      }
      if (go.getKey() == REACHED_EXIT) {
        System.out.println("You reached the exit! Well done!");
        solveButton.setText("You solved it :)");
        //index++;
        //select(levels.get(index));
      }

    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
