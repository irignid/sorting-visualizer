package com.irignid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.irignid.algos.AlgorithmRegistry;
import com.irignid.core.Direction;
import com.irignid.core.Order;
import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import com.irignid.ui.Player;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;


public class App extends Application {
    private static final double W = 900, H = 420, STRIP_Y = 120, STRIP_H = 70;

    private final Canvas canvas = new Canvas(W, H);
    private final Label narration = new Label("Press Play or Next");
    private final Label counters = new Label();
    private final Button playBtn = new Button("Play");

    private ComboBox<SortAlgorithm> algoBox;
    private ComboBox<Order> orderBox;
    private ComboBox<Direction> dirBox;
    private Slider sizeSlider;

    private int[] input;
    private Player player;
    private Timeline timeline;

    private static final double LIFT = 45;
    private double[] liftFrom, liftTo, liftCur, stripFrom, stripTo, stripCur;

    private int dragId = -1;
    private double dragX;

    private boolean dark;
    private BorderPane rootPane;
    private Color bgCol = Color.web("#fafafa"), lineCol = Color.web("#bbbbbb"),
                textCol = Color.BLACK, subCol = Color.web("#666666"), hiddenCol = Color.web("#cfcfcf");
    private String codeHi = "#ffe08a";

    @Override
    public void start(Stage stage) {
        algoBox = new ComboBox<>(FXCollections.observableArrayList(AlgorithmRegistry.ALL));
        algoBox.setConverter(new StringConverter<>() {
            public String toString(SortAlgorithm a) { return a == null ? "" : a.name(); }
            public SortAlgorithm fromString(String s) { return null; }
        });
        algoBox.setValue(AlgorithmRegistry.ALL.get(0));

        orderBox = new ComboBox<>(FXCollections.observableArrayList(Order.values()));
        orderBox.setValue(Order.ASC);
        dirBox = new ComboBox<>(FXCollections.observableArrayList(Direction.values()));
        dirBox.setValue(Direction.LEFT);

        presetBox = new ComboBox<>(FXCollections.observableArrayList(Preset.values()));
        presetBox.setValue(Preset.RANDOM);
        presetBox.setOnAction(e -> { newInput(); rebuild(); });

        stepSlider = new Slider(0, 1, 0);
        stepSlider.setBlockIncrement(1);
        stepSlider.valueProperty().addListener((o, a, b) -> {
            if (syncing) return;
            pause();
            player.goTo(b.intValue());
            refresh(true);
        });

        sizeSlider = new Slider(5, 30, 10);
        sizeSlider.setPrefWidth(120);
        Button shuffleBtn = new Button("Shuffle");

        Button prevBtn = new Button("Prev");
        Button nextBtn = new Button("Next");
        Button resetBtn = new Button("Reset");
        speed = new Slider(0.25, 10, 1);
        speed.setPrefWidth(160);

        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.rateProperty().bind(speed.valueProperty());
        prog.addListener((o, a, b) -> { updateCur(); draw(); });

        playBtn.setOnAction(e -> togglePlay());
        nextBtn.setOnAction(e -> { pause(); player.next(); refresh(true); });
        prevBtn.setOnAction(e -> { pause(); player.prev(); refresh(true); });
        resetBtn.setOnAction(e -> { pause(); player.goTo(0); refresh(true); });
        shuffleBtn.setOnAction(e -> { newInput(); rebuild(); });
        algoBox.setOnAction(e -> rebuild());
        orderBox.setOnAction(e -> rebuild());
        dirBox.setOnAction(e -> rebuild());
        sizeSlider.valueProperty().addListener((o, a, b) -> {
            if (b.intValue() != a.intValue()) { newInput(); rebuild(); }
        });

        canvas.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || player == null) return;
            int n = player.view().values.length;
            double slot = W / n;
            for (int id = 0; id < n; id++) {
                if (Math.abs(e.getX() - (curPos[id] + 0.5) * slot) <= slot / 2) { renamePerson(id); return; }
            }
        });
        canvas.setOnMousePressed(e -> {
            if (e.getClickCount() != 1) return;
            if (player.index() != 0 || timeline.getStatus() == Animation.Status.RUNNING) {
                narration.setText("Order is locked while running. Press Reset to rearrange.");
                return;
            }
            dragId = personAt(e.getX());
            dragX = e.getX();
        });
        canvas.setOnMouseDragged(e -> { if (dragId >= 0) { dragX = e.getX(); draw(); } });
        canvas.setOnMouseReleased(e -> {
            if (dragId < 0) return;
            int from = dragId, n = input.length;
            dragId = -1;
            int to = (int) Math.max(0, Math.min(n - 1, e.getX() / (W / n)));
            if (to == from) { draw(); return; }          // plain click: nothing moved
            moveInput(from, to);
            rebuild();
        });
        Tooltip.install(canvas, new Tooltip("Drag to reorder (before starting) · Double-click to rename"));

        ToggleButton themeBtn = new ToggleButton("Dark");
        themeBtn.setOnAction(e -> { dark = themeBtn.isSelected(); applyTheme(); });

        ToggleButton codeBtn = new ToggleButton("Code");
        codeBtn.setSelected(true);
        codeBtn.setOnAction(e -> {
            codeBox.setVisible(codeBtn.isSelected());
            codeBox.setManaged(codeBtn.isSelected());
            stage.sizeToScene();
        });

        HBox top = new HBox(10, algoBox, new Label("Order"), orderBox, new Label("Start"), dirBox,
                new Label("People"), sizeSlider, new Label("Input"), presetBox, shuffleBtn , themeBtn, codeBtn);
        top.setAlignment(Pos.CENTER_LEFT);

        narration.setFont(Font.font(16));
        HBox controls = new HBox(10, resetBtn, prevBtn, playBtn, nextBtn, new Label("Speed"), speed);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox bottom = new VBox(8, narration, counters, stepSlider, controls);
        BorderPane root = new BorderPane(canvas);
        rootPane = root;
        root.setTop(top);
        root.setBottom(bottom);
        codeBox.setPadding(new Insets(0, 0, 0, 12));
        codeBox.setPrefWidth(340);
        root.setRight(codeBox);
        BorderPane.setMargin(canvas, new Insets(10, 0, 10, 0));
        root.setPadding(new Insets(12));

        newInput();
        rebuild();

        stage.setTitle("Sorting Visualizer");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void newInput() {
        int n = (int) sizeSlider.getValue();
        Random r = new Random();
        int[] a = new int[n];
        switch (presetBox.getValue()) {
            case RANDOM -> { for (int i = 0; i < n; i++) a[i] = 140 + r.nextInt(60); }
            case NEARLY_SORTED -> {
                for (int i = 0; i < n; i++) a[i] = 140 + 2 * i;
                for (int s = 0; s < Math.max(1, n / 10); s++) {      // a few adjacent swaps
                    int k = r.nextInt(n - 1);
                    int t = a[k]; a[k] = a[k + 1]; a[k + 1] = t;
                }
            }
            case REVERSED -> { for (int i = 0; i < n; i++) a[i] = 140 + 2 * (n - 1 - i); }
            case FEW_UNIQUE -> { for (int i = 0; i < n; i++) a[i] = 150 + 20 * r.nextInt(3); }
        }
        input = a;
    }

    private void rebuild() {
        pause();
        Tracer t = new Tracer(input, orderBox.getValue(), dirBox.getValue());
        algoBox.getValue().sort(t);
        player = new Player(input, t.steps());
        syncing = true;
        stepSlider.setValue(0);
        stepSlider.setMax(player.total());
        syncing = false;
        fillCode(algoBox.getValue().pseudocode());
        refresh(false);
    }

    private void tick() {
        if (player.atEnd()) { pause(); return; }
        player.next();
        refresh(true);
    }

    private void togglePlay() {
        if (timeline.getStatus() == Animation.Status.RUNNING) { pause(); return; }
        if (player.atEnd()) player.goTo(0);
        timeline.play();
        playBtn.setText("Pause");
    }

    private void pause() { timeline.pause(); playBtn.setText("Play"); }

    private void draw() {
        Player.View v = player.view();
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(bgCol);
        g.fillRect(0, 0, W, H);

        drawPeople(g, v);
        narration.setText(v.text.isEmpty() ? "Press Play or Next" : v.text);
        counters.setText("Step " + player.index() + " / " + player.total()
                + "    Comparisons: " + v.comparisons + "    Writes: " + v.writes);

        for (int i = 0; i < codeLabels.size(); i++) {
            codeLabels.get(i).setStyle(CODE_STYLE + (i == v.codeLine ? "-fx-background-color: " + codeHi + ";" : ""));
        }
        if ((int) stepSlider.getValue() != player.index()) {
            syncing = true;
            stepSlider.setValue(player.index());
            syncing = false;
        }
        counters.setText("Step " + player.index() + " / " + player.total()
        + "    Comparisons: " + v.comparisons + "    Writes: " + v.writes
        + (v.flag >= 0 ? "    Swapped flag: " + (v.flag == 1) : ""));
    }

    private void fillCode(List<String> lines) {
        codeBox.getChildren().clear();
        codeLabels.clear();
        for (String line : lines) {
            Label l = new Label(line);
            l.setMaxWidth(Double.MAX_VALUE);
            l.setWrapText(true);
            l.setStyle(CODE_STYLE);
            codeLabels.add(l);
            codeBox.getChildren().add(l);
        }
    }

    private enum Preset {
        RANDOM("Random"), NEARLY_SORTED("Nearly sorted"), REVERSED("Reversed"), FEW_UNIQUE("Few unique");
        private final String label;
        Preset(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private static final String CODE_STYLE = "-fx-font-family: monospace; -fx-font-size: 13; -fx-padding: 3 8;";

    private ComboBox<Preset> presetBox;
    private Slider stepSlider;
    private boolean syncing;
    private final VBox codeBox = new VBox(2);
    private final List<Label> codeLabels = new ArrayList<>();

    private Slider speed;
    private Timeline anim = new Timeline();
    private final DoubleProperty prog = new SimpleDoubleProperty(1);
    private double[] fromPos, toPos, curPos;   // slot position per person id

    private static final String[] NAMES = {"Ama","Ben","Chen","Dev","Eli","Fay","Gus","Hana","Ivan","Jo",
        "Kai","Lena","Mo","Nia","Omar","Pia","Quin","Raj","Sam","Tara","Uma","Vik","Wen","Xia","Yan",
        "Zoe","Abe","Bea","Cy","Dee"};

    private final String[] names = NAMES.clone();
    
    private void fillTargets(Player.View v, double[] pos, double[] lift, double[] strip) {
        java.util.Arrays.fill(lift, 0);
        java.util.Arrays.fill(strip, 0);
        for (int s = 0; s < v.ids.length; s++) if (v.ids[s] >= 0) pos[v.ids[s]] = s;
        for (int j = 0; j < v.auxIds.length; j++) {
            if (v.auxUsed[j]) continue;
            pos[v.auxIds[j]] = v.auxLo + j;
            strip[v.auxIds[j]] = 1;
        }
        if (v.lifted >= 0) lift[v.ids[v.lifted]] = LIFT;
    }

    private void refresh(boolean animate) {
        Player.View v = player.view();
        int n = v.ids.length;
        anim.stop();
        if (!animate || curPos == null || curPos.length != n) {
            curPos = new double[n]; fromPos = new double[n]; toPos = new double[n];
            liftFrom = new double[n]; liftTo = new double[n]; liftCur = new double[n];
            stripFrom = new double[n]; stripTo = new double[n]; stripCur = new double[n];
            fillTargets(v, toPos, liftTo, stripTo);
            System.arraycopy(toPos, 0, curPos, 0, n);   System.arraycopy(toPos, 0, fromPos, 0, n);
            System.arraycopy(liftTo, 0, liftCur, 0, n); System.arraycopy(liftTo, 0, liftFrom, 0, n);
            System.arraycopy(stripTo, 0, stripCur, 0, n); System.arraycopy(stripTo, 0, stripFrom, 0, n);
            prog.set(1);
            draw();
            return;
        }
        System.arraycopy(curPos, 0, fromPos, 0, n);
        System.arraycopy(liftCur, 0, liftFrom, 0, n);
        System.arraycopy(stripCur, 0, stripFrom, 0, n);
        fillTargets(v, toPos, liftTo, stripTo);
        prog.set(0);
        double ms = Math.max(60, 300 / speed.getValue());
        anim = new Timeline(new KeyFrame(Duration.millis(ms), new KeyValue(prog, 1, Interpolator.EASE_BOTH)));
        anim.play();
    }

    private void updateCur() {
        double p = prog.get();
        for (int k = 0; k < curPos.length; k++) {
            curPos[k] = fromPos[k] + (toPos[k] - fromPos[k]) * p;
            liftCur[k] = liftFrom[k] + (liftTo[k] - liftFrom[k]) * p;
            stripCur[k] = stripFrom[k] + (stripTo[k] - stripFrom[k]) * p;
        }
    }

    private void drawPeople(GraphicsContext g, Player.View v) {
        int n = v.values.length;
        int max = 0;
        for (int x : input) max = Math.max(max, x);
        double slot = W / n, w = Math.min(slot * 0.8, 60);
        double ground = H - 40;

        if (v.rangeLo >= 0) {                                   // group being worked on
            g.setFill(dark ? Color.web("#2d3140") : Color.web("#eef2fb"));
            g.fillRect(v.rangeLo * slot, 0, (v.rangeHi - v.rangeLo + 1) * slot, ground);
        }
        boolean holding = false;
        for (boolean u : v.auxUsed) if (!u) holding = true;
        g.setStroke(lineCol);
        g.setLineWidth(1);
        g.strokeLine(0, ground, W, ground);
        if (holding) {
            g.strokeLine(0, STRIP_Y, W, STRIP_Y);
            g.setFill(subCol);
            g.setFont(Font.font(11));
            g.setTextAlign(TextAlignment.LEFT);
            g.fillText("holding row", 6, 16);
        }

        for (int pass = 0; pass < 2; pass++) {                  // pass 1 = lifted/dragged person, on top
            for (int s = 0; s < n; s++) {
                int id = v.ids[s];
                if (id < 0) continue;                           // gap left by the held group
                if ((s == v.lifted || id == dragId) != (pass == 1)) continue;
                Color shirt = Color.web("#8aa4c8");
                if (v.sorted[s]) shirt = Color.web("#4caf7a");
                if (v.hidden[s]) shirt = hiddenCol;
                if (s == v.reveal || s == v.lifted) shirt = Color.web("#9b6fd1");
                if (s == v.cmpA || s == v.cmpB) shirt = Color.web("#f2c744");
                if (s == v.swapA || s == v.swapB) shirt = Color.web("#e5534b");
                drawOne(g, id, v.values[s], shirt, v.hidden[s], s == v.marker, s == v.pivot, slot, w, max);
            }
            if (pass == 0) {
                for (int j = 0; j < v.auxIds.length; j++) {     // people waiting in the holding row
                    if (v.auxUsed[j]) continue;
                    Color shirt = (j == v.auxCmpA || j == v.auxCmpB) ? Color.web("#f2c744") : Color.web("#8aa4c8");
                    drawOne(g, v.auxIds[j], v.auxVals[j], shirt, false, false, false, slot, w, max);
                }
            }
        }
    }

    private void drawOne(GraphicsContext g, int id, int value, Color shirt, boolean hidden,
                         boolean marker, boolean pivot, double slot, double w, int max) {
        double ground = H - 40, maxH = H - 190;
        double t = stripCur[id];                                // 0 = line, 1 = holding row
        double cx = (curPos[id] + 0.5) * slot;
        double hop = fromPos[id] != toPos[id] ? Math.sin(Math.PI * prog.get()) * 28 : 0;
        hop += liftCur[id];
        if (id == dragId) { cx = dragX; hop = 24; }
        double base = ground + (STRIP_Y - ground) * t - hop;
        double h = value / (double) max * (maxH + (STRIP_H - maxH) * t);
        drawBar(g, cx, base, h, w, shirt, id);
        double top = base - h;

        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(textCol);
        g.setFont(Font.font(Math.min(14, slot * 0.4)));
        double labelY = (H - 20) + ((STRIP_Y + 14) - (H - 20)) * t;
        g.fillText(hidden ? "?" : String.valueOf(value), cx, labelY);
        if (slot >= 20) {
            g.setFont(Font.font(11));
            g.setFill(subCol);
            String nm = names[id];
            g.fillText(slot >= 56 || nm.isEmpty() ? nm : nm.substring(0, 1), cx, top - 6);
        }
        if (marker) {
            g.setFill(Color.web("#ff8c1a"));
            g.fillOval(cx - 8, top - (slot >= 20 ? 30 : 22), 16, 16);
        }
        if (pivot) {
            double y = top - (slot >= 20 ? 20 : 8);
            g.setFill(Color.web("#3b82f6"));
            g.fillPolygon(new double[]{cx - 7, cx + 7, cx}, new double[]{y - 10, y - 10, y}, 3);
        }
    }

    private void drawBar(GraphicsContext g, double cx, double ground, double h, double w, Color fill, int id) {
        g.setFill(fill);
        g.fillRoundRect(cx - w / 2, ground - h, w, h, 6, 6);
        g.setFill(Color.hsb((id * 47) % 360, 0.55, 0.55));   // identity cap, unique per person
        g.fillRoundRect(cx - w / 2, ground - h, w, Math.min(8, h), 6, 6);
    }

    private void renamePerson(int id) {
        pause();
        TextInputDialog d = new TextInputDialog(names[id]);
        d.setTitle("Rename");
        d.setHeaderText("Rename this person");
        d.setContentText("Name:");
        d.showAndWait()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .ifPresent(s -> { names[id] = s; draw(); });
    }

    private int personAt(double x) {
        double slot = W / input.length;
        for (int id = 0; id < input.length; id++)
            if (Math.abs(x - (curPos[id] + 0.5) * slot) <= slot / 2) return id;
        return -1;
    }

    private void moveInput(int from, int to) {
        int n = input.length;
        List<Integer> vals = new ArrayList<>();
        List<String> nms = new ArrayList<>();
        for (int i = 0; i < n; i++) { vals.add(input[i]); nms.add(names[i]); }
        vals.add(to, vals.remove(from));
        nms.add(to, nms.remove(from));               // the name travels with the person
        for (int i = 0; i < n; i++) { input[i] = vals.get(i); names[i] = nms.get(i); }
    }

    private void applyTheme() {
        bgCol     = dark ? Color.web("#1e1f24") : Color.web("#fafafa");
        lineCol   = dark ? Color.web("#555a66") : Color.web("#bbbbbb");
        textCol   = dark ? Color.web("#e6e6e6") : Color.BLACK;
        subCol    = dark ? Color.web("#9aa0ab") : Color.web("#666666");
        hiddenCol = dark ? Color.web("#5b606b") : Color.web("#cfcfcf");
        codeHi    = dark ? "#5a4a10" : "#ffe08a";
        rootPane.setStyle(dark ? "-fx-base: #2b2d33;" : "");   // JavaFX derives all control colours from this
        draw();
    }

    public static void main(String[] args) { launch(args); }
}