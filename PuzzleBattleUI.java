import java.io.File;
import java.util.List;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PuzzleBattleUI extends Application {

    private GameFlow gameFlow;
    private Label turnLabel;
    private Label phaseLabel;
    private Label statusLabel;
    private Label playerALabel;
    private Label playerBLabel;
    private Label opponentHpLabel;
    private Label opponentShieldLabel;
    private Label opponentEnergyLabel;
    private Label playerHpLabel;
    private Label playerShieldLabel;
    private Label playerEnergyLabel;
    private GridPane boardPanel;
    private HBox handPanel;
    private HBox opponentHandPanel;
    private HBox playerHandPanel;
    private ScrollPane handScroll;
    private ScrollPane opponentHandScroll;
    private ScrollPane playerHandScroll;
    private Label leftHpValue;
    private Label leftEnergyValue;
    private Label rightHpValue;
    private Label rightEnergyValue;
    private javafx.scene.control.ProgressBar leftHpBar;
    private javafx.scene.control.ProgressBar leftEnergyBar;
    private javafx.scene.control.ProgressBar rightHpBar;
    private javafx.scene.control.ProgressBar rightEnergyBar;
    private javafx.scene.layout.VBox mainLayout;
    private ScrollPane contentScroll;
    private Label opponentHandTitleLabel;
    private Label playerHandTitleLabel;
    private boolean isRotating = false;
    private boolean boardLocked = false; // ล็อกบอร์ดระหว่างรอคว่ำการ์ดที่ไม่ match
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Puzzle Battle");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(760);
        primaryStage.setResizable(true);
        showMenuScene(primaryStage);
        primaryStage.show();
    }

    // =========================================================
    // MENU SCENE
    // =========================================================
    private void showMenuScene(Stage stage) {
        // ── Background ──────────────────────────────────────────
        StackPane root = new StackPane();
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #04090f 0%, #091322 40%, #0d1d38 100%);"
        );

        // ── Glow circle decoration ───────────────────────────────
        javafx.scene.shape.Circle glow = new javafx.scene.shape.Circle(320);
        glow.setFill(Color.TRANSPARENT);
        glow.setStroke(Color.TRANSPARENT);
        glow.setStyle("-fx-effect: dropshadow(gaussian, rgba(60,120,255,0.18), 160, 0.5, 0, 0);");
        StackPane.setAlignment(glow, Pos.CENTER);

        // ── Title ────────────────────────────────────────────────
        Label title = new Label("PUZZLE BATTLE");
        title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 72));
        title.setTextFill(Color.web("#f5cd73"));
        title.setStyle(
            "-fx-effect: dropshadow(gaussian, rgba(245,205,115,0.85), 32, 0.6, 0, 0);"
        );

        Label subtitle = new Label("CARD MEMORY DUEL");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        subtitle.setTextFill(Color.web("#a3b9ff"));
        subtitle.setStyle("-fx-letter-spacing: 6;");

        // ── Divider ──────────────────────────────────────────────
        Region divider = new Region();
        divider.setPrefSize(280, 2);
        divider.setMaxWidth(280);
        divider.setStyle("-fx-background-color: linear-gradient(to right, transparent, rgba(245,205,115,0.7), transparent);");

        // ── START GAME button ────────────────────────────────────
        Button startBtn = new Button("▶  START GAME");
        startBtn.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        startBtn.setPrefWidth(320);
        startBtn.setPrefHeight(62);
        startBtn.setCursor(javafx.scene.Cursor.HAND);
        startBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #e8b84b, #c89120);" +
            "-fx-text-fill: #1a1000;" +
            "-fx-background-radius: 32;" +
            "-fx-border-color: rgba(255,220,100,0.6);" +
            "-fx-border-radius: 32;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(230,180,50,0.65), 18, 0.4, 0, 4);"
        );
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f5cd73, #d4a020);" +
            "-fx-text-fill: #0d0800;" +
            "-fx-background-radius: 32;" +
            "-fx-border-color: rgba(255,230,120,0.9);" +
            "-fx-border-radius: 32;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(245,210,80,0.9), 26, 0.5, 0, 6);"
        ));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #e8b84b, #c89120);" +
            "-fx-text-fill: #1a1000;" +
            "-fx-background-radius: 32;" +
            "-fx-border-color: rgba(255,220,100,0.6);" +
            "-fx-border-radius: 32;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(230,180,50,0.65), 18, 0.4, 0, 4);"
        ));
        startBtn.setOnAction(e -> showGameScene(stage));

        // ── Card type legend ─────────────────────────────────────
        String[] cardNames = {"⚔ Attack", "💚 Heal", "⚡ Energy", "🛡 Shield", "✖ Negate", "🤏 Steal", "＋ Extra"};
        HBox legend = new HBox(18);
        legend.setAlignment(Pos.CENTER);
        for (String name : cardNames) {
            Label lbl = new Label(name);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            lbl.setTextFill(Color.web("#8ab4f8"));
            lbl.setPadding(new Insets(4, 10, 4, 10));
            lbl.setStyle(
                "-fx-background-color: rgba(30,60,120,0.45);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: rgba(100,160,255,0.3);" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;"
            );
            legend.getChildren().add(lbl);
        }

        // ── Rules summary ────────────────────────────────────────
        Label rulesTitle = new Label("กติกาเกม");
        rulesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        rulesTitle.setTextFill(Color.web("#c8d8ff"));

        String rulesText =
            "• ผู้เล่น 2 ฝ่าย  HP 3000 | Energy เริ่มต้น 0\n" +
            "• แต่ละเทิร์น: รับ +2 Energy → เปิดการ์ดจับคู่ (ฟรี 2 คู่, คู่ต่อไปจ่าย 4 Energy)\n" +
            "• จับคู่สำเร็จ → ได้การ์ดเข้ามือ  |  จับไม่ได้ → การ์ดคว่ำกลับ\n" +
            "• ใช้การ์ดในมือ: Attack / Heal / Energy / Shield / Negate / Steal / Extra\n" +
            "• Chain Effect: ฝ่ายตรงข้ามสามารถเชน Negate หรือ Steal ได้\n" +
            "• ฝ่ายที่ HP ≤ 0 แพ้";
        Label rulesLabel = new Label(rulesText);
        rulesLabel.setFont(Font.font("Arial", 13));
        rulesLabel.setTextFill(Color.web("#99b0d4"));
        rulesLabel.setWrapText(true);
        rulesLabel.setMaxWidth(700);
        rulesLabel.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);

        VBox rulesBox = new VBox(6, rulesTitle, rulesLabel);
        rulesBox.setAlignment(Pos.CENTER_LEFT);
        rulesBox.setPadding(new Insets(14, 28, 14, 28));
        rulesBox.setMaxWidth(760);
        rulesBox.setStyle(
            "-fx-background-color: rgba(10,20,50,0.6);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(100,140,255,0.2);" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );

        // ── Footer ────────────────────────────────────────────────
        Label footer = new Label("Player A  vs  Player B   •   2-Player Local");
        footer.setFont(Font.font("Arial", 12));
        footer.setTextFill(Color.web("#4a6080"));

        // ── Layout ───────────────────────────────────────────────
        VBox center = new VBox(22,
            title, subtitle, divider,
            startBtn,
            legend,
            rulesBox,
            footer
        );
        center.setAlignment(Pos.CENTER);
        center.setMaxWidth(820);
        center.setPadding(new Insets(60, 40, 50, 40));

        root.getChildren().addAll(glow, center);

        // ── Fade-in animation ────────────────────────────────────
        center.setOpacity(0);
        Scene menuScene = new Scene(root, 1500, 900);
        stage.setScene(menuScene);

        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(800), center);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    // =========================================================
    // GAME SCENE  (โค้ดเดิมทั้งหมด ย้ายมาอยู่ใน method นี้)
    // =========================================================
    private void showGameScene(Stage primaryStage) {
        gameFlow = new GameFlow();
        gameFlow.startGame();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #091322 0%, #101d35 28%, #0a1325 100%); " +
                "-fx-border-color: rgba(163, 185, 255, 0.35); -fx-border-width: 0 0 2 0;"
        );


        boardPanel = new GridPane();
        boardPanel.setHgap(12);
        boardPanel.setVgap(12);
        boardPanel.setPadding(new Insets(14, 16, 14, 16));
        boardPanel.setMaxWidth(920);
        boardPanel.setMaxHeight(390);
        boardPanel.setAlignment(Pos.CENTER);
        boardPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(18, 42, 32, 0.96), rgba(15, 28, 22, 0.96)); " +
                "-fx-border-color: rgba(245, 205, 115, 0.9); " +
                "-fx-border-width: 4; " +
                "-fx-border-radius: 30; " +
                "-fx-background-radius: 30; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.62), 20, 0.25, 0, 12), innershadow(gaussian, rgba(115, 213, 139, 0.24), 22, 0.45, 0, 0); " +
                "-fx-background-image: url('file:assets/arena/arena_floor_pattern.png'); " +
                "-fx-background-repeat: repeat; " +
                "-fx-background-position: center;"
        );

        HBox top = buildTopPanel();
        VBox center = buildCenterPanel();
        opponentHandPanel = buildHandPanel();
        playerHandPanel = buildHandPanel();
        handPanel = playerHandPanel;

        mainLayout = new VBox(10,
                buildOpponentHandStrip(),
                center,
                buildBottomPanel(),
                buildPlayerHandStrip());
        mainLayout.setPadding(new Insets(10, 16, 16, 16));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setFillWidth(true);
        mainLayout.setPrefWidth(1200);
        mainLayout.setMaxWidth(1280);
        mainLayout.setMinWidth(980);

        contentScroll = new ScrollPane(mainLayout);
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(false);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setPannable(true);
        contentScroll.setPadding(new Insets(0));
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        root.setCenter(contentScroll);

        Scene scene = new Scene(root, 1500, 900);
        primaryStage.setScene(scene);

        updateResponsiveLayout(scene.getWidth(), scene.getHeight());
        refreshUI();
    }

    private void updateResponsiveLayout(double sceneWidth, double sceneHeight) {
        double boardWidth = 860;
        double boardHeight = 340;
        boardPanel.setPrefSize(boardWidth, boardHeight);
        boardPanel.setMaxSize(boardWidth, boardHeight);

        double lowerSectionWidth = 980;
        double handHeight = 88;
        if (playerHandPanel != null) {
            playerHandPanel.setPrefWidth(lowerSectionWidth);
            playerHandPanel.setPrefHeight(handHeight);
            playerHandPanel.setMinWidth(920);
            playerHandPanel.setMaxWidth(lowerSectionWidth);
        }
        if (opponentHandPanel != null) {
            opponentHandPanel.setPrefWidth(lowerSectionWidth);
            opponentHandPanel.setPrefHeight(handHeight);
            opponentHandPanel.setMinWidth(920);
            opponentHandPanel.setMaxWidth(lowerSectionWidth);
        }

        if (playerHandScroll != null) {
            playerHandScroll.setPrefViewportHeight(handHeight - 12);
        }
        if (opponentHandScroll != null) {
            opponentHandScroll.setPrefViewportHeight(handHeight - 12);
        }

        double boardGap = 8;
        boardPanel.setHgap(boardGap);
        boardPanel.setVgap(boardGap * 0.9);
    }

    private HBox buildTopPanel() {
        Label title = new Label("BATTLE HUD");
        title.setTextFill(Color.web("#f5d589"));
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        playerALabel = new Label();
        playerALabel.setTextFill(Color.web("#dff6ff"));
        playerALabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        playerBLabel = new Label();
        playerBLabel.setTextFill(Color.web("#ffeab3"));
        playerBLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        turnLabel = new Label();
        turnLabel.setTextFill(Color.web("#dfe7ff"));
        turnLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        phaseLabel = new Label();
        phaseLabel.setTextFill(Color.web("#aef5d1"));
        phaseLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        phaseLabel.setStyle("-fx-padding: 6 12 6 12; -fx-background-color: rgba(8, 34, 23, 0.8); -fx-border-color: #66e7a6; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16;");

        VBox centerInfo = new VBox(4, turnLabel, phaseLabel);
        centerInfo.setAlignment(Pos.CENTER);

        HBox statsRow = new HBox(18,
                createPlayerStatusCard(gameFlow.getPlayerA(), true),
                centerInfo,
                createPlayerStatusCard(gameFlow.getPlayerB(), false));
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(8, 10, 8, 10));

        VBox header = new VBox(6, title, statsRow);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setMinHeight(120);
        header.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(17, 26, 46, 0.96), rgba(10, 17, 30, 0.9)); -fx-border-color: rgba(160, 185, 255, 0.75); -fx-border-width: 2; -fx-border-radius: 22; -fx-background-radius: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 16, 0.2, 0, 4);");

        HBox top = new HBox(header);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPrefHeight(120);
        return top;
    }

    private VBox buildCenterPanel() {
        statusLabel = new Label();
        statusLabel.setTextFill(Color.web("#dfe7ff"));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(980);
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setStyle("-fx-padding: 8 14 8 14; -fx-background-color: rgba(10, 23, 39, 0.72); -fx-border-color: rgba(136, 170, 255, 0.65); -fx-border-width: 1.5; -fx-border-radius: 14; -fx-background-radius: 14;");

        Label boardTitle = new Label("FIELD BOARD");
        boardTitle.setTextFill(Color.web("#f5d589"));
        boardTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        VBox boardFrame = new VBox(10, boardTitle, boardPanel);
        boardFrame.setAlignment(Pos.CENTER);
        boardFrame.setPadding(new Insets(10, 12, 12, 12));
        boardFrame.setStyle("-fx-background-color: rgba(14, 24, 40, 0.5); -fx-border-color: rgba(173, 194, 255, 0.5); -fx-border-width: 2; -fx-border-radius: 22; -fx-background-radius: 22;");

        VBox panel = new VBox(12, statusLabel, boardFrame);
        panel.setPadding(new Insets(0, 8, 0, 8));
        panel.setAlignment(Pos.CENTER);
        return panel;
    }

    private VBox createPlayerStatusCard(Player player, boolean isLeftSide) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 18, 10, 18));
        card.setMinWidth(260);
        card.setMaxWidth(320);
        card.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(18, 29, 54, 0.9), rgba(10, 16, 29, 0.95)); -fx-border-color: rgba(154, 176, 255, 0.7); -fx-border-width: 1.5; -fx-border-radius: 18; -fx-background-radius: 18;");

        Label name = new Label(player.getName());
        name.setTextFill(isLeftSide ? Color.web("#9fe8ff") : Color.web("#ffe7a6"));
        name.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        HBox stats = new HBox(14);
        stats.setAlignment(Pos.CENTER);

        Label hp = new Label("HP " + player.getHp());
        hp.setTextFill(Color.web("#fff1ab"));
        hp.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Label energy = new Label("EN " + player.getEnergy());
        energy.setTextFill(Color.web("#9ee5ff"));
        energy.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Label shield = new Label("SH " + player.getShield());
        shield.setTextFill(Color.web("#dfe7ff"));
        shield.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        stats.getChildren().addAll(hp, energy, shield);
        card.getChildren().addAll(name, stats);
        return card;
    }

    private VBox buildStatPanel(Player player, boolean isLeftSide) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(0, 0, 0, 0));
        panel.setPrefWidth(110);

        String hpAsset = isLeftSide ? "assets/puzzle_battle_assets/status_hp_left.png" : "assets/puzzle_battle_assets/status_hp_right.png";
        String energyAsset = isLeftSide ? "assets/puzzle_battle_assets/status_energy_left.png" : "assets/puzzle_battle_assets/status_energy_right.png";

        ImageView hpPanel = new ImageView(new Image("file:" + hpAsset));
        hpPanel.setFitWidth(90);
        hpPanel.setFitHeight(240);
        hpPanel.setPreserveRatio(false);

        ImageView energyPanel = new ImageView(new Image("file:" + energyAsset));
        energyPanel.setFitWidth(90);
        energyPanel.setFitHeight(220);
        energyPanel.setPreserveRatio(false);

        Label hpValue = new Label(String.valueOf(player.getHp()));
        hpValue.setTextFill(Color.web("#fff2bf"));
        hpValue.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        hpValue.setStyle("-fx-font-smoothing-type: lcd;");

        Label energyValue = new Label(String.valueOf(player.getEnergy()));
        energyValue.setTextFill(Color.web("#dff6ff"));
        energyValue.setFont(Font.font("Verdana", FontWeight.BOLD, 24));

        if (isLeftSide) {
            leftHpValue = hpValue;
            leftEnergyValue = energyValue;
            leftHpBar = new javafx.scene.control.ProgressBar();
            leftEnergyBar = new javafx.scene.control.ProgressBar();
            leftHpBar.setPrefWidth(54);
            leftHpBar.setPrefHeight(10);
            leftEnergyBar.setPrefWidth(54);
            leftEnergyBar.setPrefHeight(10);
        } else {
            rightHpValue = hpValue;
            rightEnergyValue = energyValue;
            rightHpBar = new javafx.scene.control.ProgressBar();
            rightEnergyBar = new javafx.scene.control.ProgressBar();
            rightHpBar.setPrefWidth(54);
            rightHpBar.setPrefHeight(10);
            rightEnergyBar.setPrefWidth(54);
            rightEnergyBar.setPrefHeight(10);
        }

        StackPane hpStack = new StackPane(hpPanel, hpValue);
        hpStack.setAlignment(Pos.CENTER);
        hpStack.setPrefSize(90, 240);

        StackPane energyStack = new StackPane(energyPanel, energyValue);
        energyStack.setAlignment(Pos.CENTER);
        energyStack.setPrefSize(90, 220);

        panel.getChildren().addAll(hpStack, energyStack);
        return panel;
    }

    private VBox buildBottomPanel() {
        HBox actions = new HBox(18);
        actions.setPadding(new Insets(10, 18, 10, 18));
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setMinHeight(80);
        actions.setMaxWidth(Double.MAX_VALUE);
        actions.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(30, 42, 72, 0.96), rgba(10, 16, 28, 0.96)); " +
                "-fx-border-color: rgba(236, 203, 128, 0.95); " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 24; " +
                "-fx-background-radius: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(138, 180, 255, 0.55), 18, 0.25, 0, 0);"
        );

        VBox currentInfo = new VBox(5);
        currentInfo.setAlignment(Pos.CENTER_LEFT);

        Player current = gameFlow.getCurrentPlayer();

        Label playerLabel = new Label(current.getName() + " ACTION BAR");
        playerLabel.setTextFill(Color.web("#f8e7a0"));
        playerLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        HBox playerStats = new HBox(16);
        playerStats.setAlignment(Pos.CENTER_LEFT);

        playerHpLabel = new Label("HP: " + current.getHp());
        playerHpLabel.setTextFill(Color.web("#fff1ab"));
        playerHpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        playerShieldLabel = new Label("Shield: " + current.getShield());
        playerShieldLabel.setTextFill(Color.web("#dfe7ff"));
        playerShieldLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 14));

        playerEnergyLabel = new Label("Energy: " + current.getEnergy());
        playerEnergyLabel.setTextFill(Color.web("#9ee5ff"));
        playerEnergyLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 14));

        playerStats.getChildren().addAll(playerHpLabel, playerShieldLabel, playerEnergyLabel);
        currentInfo.getChildren().addAll(playerLabel, playerStats);

        // Chain Window Buttons (shown only when chain is open)
        Button chainBtn = new Button();
        styleActionButton(chainBtn, "CHAIN!", "assets/puzzle_battle_assets/button_end_turn.png", "⛓");
        chainBtn.setStyle(chainBtn.getStyle() + "-fx-opacity: 1.0;");

        Button passBtn = new Button();
        styleActionButton(passBtn, "PASS", "assets/puzzle_battle_assets/button_end_turn.png", "✋");

        chainBtn.setOnAction(e -> {
            if (isRotating) return;
            // Open chain card selector for the chain-turn player
            showChainCardSelector();
        });

        passBtn.setOnAction(e -> {
            if (isRotating) return;
            gameFlow.passChain();
            refreshUI();
        });

        HBox chainButtons = new HBox(14, chainBtn, passBtn);
        chainButtons.setAlignment(Pos.CENTER_RIGHT);
        chainButtons.setStyle(
            "-fx-padding: 6 10 6 10; -fx-background-color: rgba(80,20,20,0.85); " +
            "-fx-border-color: #ff7c7c; -fx-border-width: 2; -fx-border-radius: 16; -fx-background-radius: 16;"
        );

        // Normal action buttons
        Button drawButton = new Button();
        String drawLabel = (gameFlow.getDrawsThisTurn() >= 2 && current.getEnergy() >= 4)
                ? "PAY 4E + CONTINUE" : "เปิดการ์ด";
        styleActionButton(drawButton, drawLabel, "assets/puzzle_battle_assets/button_end_turn.png", "⤴");
        drawButton.setOnAction(e -> {
            if (isRotating) return;
            if (gameFlow.getDrawsThisTurn() >= 2 && current.getEnergy() >= 4) {
                gameFlow.payEnergyToContinueMatchingBoard();
            } else if (gameFlow.canDrawCardForCurrentPlayer()) {
                gameFlow.drawCardForCurrentPlayer();
            }
            refreshUI();
        });

        Button endTurnButton = new Button();
        styleActionButton(endTurnButton, "END TURN", "assets/puzzle_battle_assets/button_end_turn.png", "⏭");
        endTurnButton.setOnAction(e -> {
            if (!isRotating) {
                animateTurnTransition();
            }
        });

        HBox normalButtons = new HBox(14, drawButton, endTurnButton);
        normalButtons.setAlignment(Pos.CENTER_RIGHT);

        // Select which button set to show
        HBox actionButtons = gameFlow.isChainWindowOpen() ? chainButtons : normalButtons;

        // Chain window info label
        if (gameFlow.isChainWindowOpen()) {
            Player chainPlayer = gameFlow.getChainTurnPlayer();
            String who = chainPlayer != null ? chainPlayer.getName() : "?";
            Label chainInfo = new Label("⛓ CHAIN WINDOW — " + who + " ตอบโต้");
            chainInfo.setTextFill(Color.web("#ffb3b3"));
            chainInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            currentInfo.getChildren().add(chainInfo);
        }

        actions.getChildren().addAll(currentInfo, actionButtons);
        actions.setSpacing(30);

        VBox wrapper = new VBox(actions);
        wrapper.setPadding(new Insets(0));
        wrapper.setFillWidth(true);
        wrapper.setMinHeight(80);
        return wrapper;
    }

    private void animateTurnTransition() {
        if (isRotating || gameFlow.isGameOver()) {
            return;
        }
        isRotating = true;

        if (contentScroll != null) {
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }

        // Phase 1: Rotate mainLayout 0 -> 90 degrees with scale down
        RotateTransition rot1 = new RotateTransition(Duration.millis(260), mainLayout);
        rot1.setFromAngle(0);
        rot1.setToAngle(90);
        rot1.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition scale1 = new ScaleTransition(Duration.millis(260), mainLayout);
        scale1.setFromX(1.0);
        scale1.setToX(0.90);
        scale1.setFromY(1.0);
        scale1.setToY(0.90);
        scale1.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt1 = new ParallelTransition(rot1, scale1);
        pt1.setOnFinished(event -> {
            // Halfway: Switch turn in GameFlow
            gameFlow.endTurn();

            // Refresh UI: active player rotates to bottom, opponent to top
            refreshUI();

            // Prepare layout at -90 degrees so the rotation continues clockwise to 0
            mainLayout.setRotate(-90);

            // Phase 2: Rotate clockwise -90 -> 0 degrees with scale back up
            RotateTransition rot2 = new RotateTransition(Duration.millis(260), mainLayout);
            rot2.setFromAngle(-90);
            rot2.setToAngle(0);
            rot2.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition scale2 = new ScaleTransition(Duration.millis(260), mainLayout);
            scale2.setFromX(0.90);
            scale2.setToX(1.0);
            scale2.setFromY(0.90);
            scale2.setToY(1.0);
            scale2.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition pt2 = new ParallelTransition(rot2, scale2);
            pt2.setOnFinished(finishEvent -> {
                mainLayout.setRotate(0);
                mainLayout.setScaleX(1.0);
                mainLayout.setScaleY(1.0);
                if (contentScroll != null) {
                    contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                }
                isRotating = false;
            });
            pt2.play();
        });
        pt1.play();
    }

    /** Show a popup dialog letting the chain-turn player pick a Negate or Steal card */
    private void showChainCardSelector() {
        Player chainPlayer = gameFlow.getChainTurnPlayer();
        if (chainPlayer == null) return;

        javafx.scene.control.Dialog<Integer> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Chain — " + chainPlayer.getName());
        dialog.setHeaderText(chainPlayer.getName() + " เลือกการ์ดที่จะ Chain (เฉพาะ Negate / Steal)");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new Insets(10));
        javafx.scene.control.ToggleGroup group = new javafx.scene.control.ToggleGroup();

        java.util.List<Card> cards = gameFlow.getHand(chainPlayer).getCards();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if ("Negate".equals(c.getName()) || "Steal".equals(c.getName())) {
                final int idx = i;
                javafx.scene.control.RadioButton rb = new javafx.scene.control.RadioButton(
                        c.getName() + " (Cost " + c.getCost() + ")");
                rb.setToggleGroup(group);
                rb.setUserData(idx);
                rb.setTextFill(Color.web("#fffdf3"));
                rb.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
                content.getChildren().add(rb);
            }
        }

        if (content.getChildren().isEmpty()) {
            content.getChildren().add(new Label("ไม่มีการ์ดที่สามารถ Chain ได้"));
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(
            "-fx-background-color: #0c1624; -fx-border-color: #5a8fff; -fx-border-width: 2;");

        javafx.scene.control.ButtonType chainType = new javafx.scene.control.ButtonType("Chain!", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(chainType, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == chainType && group.getSelectedToggle() != null) {
                return (Integer) group.getSelectedToggle().getUserData();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(idx -> {
            gameFlow.chainWithCard(idx);
            refreshUI();
        });
    }



    private void styleActionButton(Button button, String text, String assetPath, String icon) {
        ImageView background = new ImageView(new Image("file:" + assetPath));
        background.setFitWidth(170);
        background.setFitHeight(54);
        background.setPreserveRatio(false);

        Label iconLabel = new Label(icon);
        iconLabel.setTextFill(Color.web("#fff9d6"));
        iconLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

        Label label = new Label(text);
        label.setTextFill(Color.web("#fffdf3"));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 13));

        HBox content = new HBox(8, iconLabel, label);
        content.setAlignment(Pos.CENTER);

        StackPane graphic = new StackPane(background, content);
        graphic.setPrefSize(170, 54);
        graphic.setAlignment(Pos.CENTER);

        button.setGraphic(graphic);
        button.setPrefSize(170, 54);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-cursor: hand;");
    }

    private HBox buildHandPanel() {
        HBox panel = new HBox(12);
        panel.setPadding(new Insets(8));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setMinHeight(110);
        panel.setStyle("-fx-background-color: transparent;");
        return panel;
    }

    private VBox buildOpponentHandStrip() {
        opponentHandTitleLabel = new Label("Opponent Hand");
        opponentHandTitleLabel.setTextFill(Color.web("#f8d77a"));
        opponentHandTitleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        opponentHandTitleLabel.setStyle("-fx-padding: 6 14 6 14; -fx-background-color: rgba(23, 36, 64, 0.8); -fx-border-color: #f8d77a; -fx-border-width: 1.5; -fx-border-radius: 15; -fx-background-radius: 15;");

        opponentHandPanel = buildHandPanel();
        opponentHandPanel.setMinHeight(72);
        opponentHandPanel.setAlignment(Pos.CENTER_LEFT);
        opponentHandPanel.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(14, 23, 42, 0.92), rgba(18, 30, 60, 0.88)); -fx-border-color: rgba(160, 192, 255, 0.9); -fx-border-width: 2; -fx-border-radius: 18; -fx-background-radius: 18; -fx-effect: innershadow(gaussian, rgba(45,58,92,0.65), 10, 0.2, 0, 0);");
        opponentHandPanel.setPadding(new Insets(8));
        opponentHandPanel.setMinWidth(900);
        opponentHandPanel.setPrefWidth(1000);
        opponentHandPanel.setMaxWidth(Double.MAX_VALUE);

        opponentHandScroll = new ScrollPane(opponentHandPanel);
        opponentHandScroll.setFitToWidth(true);
        opponentHandScroll.setFitToHeight(false);
        opponentHandScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        opponentHandScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        opponentHandScroll.setPannable(true);
        opponentHandScroll.setPrefViewportHeight(82);
        opponentHandScroll.setMinViewportHeight(70);
        opponentHandScroll.setMaxWidth(Double.MAX_VALUE);
        opponentHandScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent; -fx-box-border: transparent;");

        VBox zone = new VBox(8, opponentHandTitleLabel, opponentHandScroll);
        zone.setPadding(new Insets(4, 6, 0, 6));
        zone.setMinHeight(90);
        zone.setMaxHeight(90);
        zone.setMinWidth(900);
        zone.setMaxWidth(Double.MAX_VALUE);
        zone.setStyle("-fx-background-color: rgba(9, 16, 30, 0.5); -fx-border-color: rgba(162, 181, 255, 0.45); -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20; -fx-effect: innershadow(gaussian, rgba(33,54,86,0.75), 10, 0.2, 0, 0);");
        return zone;
    }

    private VBox buildChainTurnArea() {
        Label chainTitle = new Label("CHAIN");
        chainTitle.setTextFill(Color.web("#f5d589"));
        chainTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        HBox chainRow = new HBox(12);
        chainRow.setAlignment(Pos.CENTER_LEFT);
        for (String code : new String[]{"A", "N", "S"}) {
            StackPane chip = new StackPane();
            chip.setPrefSize(48, 28);
            chip.setStyle("-fx-background-color: rgba(17, 31, 52, 0.86); -fx-border-color: rgba(255, 223, 130, 0.8); -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");
            Label text = new Label(code);
            text.setTextFill(Color.web("#f8e7a0"));
            text.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            chip.getChildren().add(text);
            chainRow.getChildren().add(chip);
        }

        Label turnInfo = new Label("TURN");
        turnInfo.setTextFill(Color.web("#dfe7ff"));
        turnInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

        Label freeFlipInfo = new Label("FREE FLIP");
        freeFlipInfo.setTextFill(Color.web("#aef5d1"));
        freeFlipInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 14));

        Label energyInfo = new Label("ENERGY INFO");
        energyInfo.setTextFill(Color.web("#9ee5ff"));
        energyInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 14));

        HBox row = new HBox(18, chainTitle, chainRow, turnInfo, freeFlipInfo, energyInfo);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setMinHeight(50);
        row.setMaxHeight(50);
        row.setStyle("-fx-background-color: rgba(15, 25, 48, 0.34); -fx-border-color: rgba(166, 187, 255, 0.55); -fx-border-width: 2; -fx-border-radius: 18; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 14, 0.18, 0, 0);");
        return new VBox(row);
    }

    private VBox buildPlayerHandStrip() {
        playerHandTitleLabel = new Label("Active Player Hand");
        playerHandTitleLabel.setTextFill(Color.web("#f8d77a"));
        playerHandTitleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        playerHandTitleLabel.setStyle("-fx-padding: 6 14 6 14; -fx-background-color: rgba(23, 36, 64, 0.8); -fx-border-color: #f8d77a; -fx-border-width: 1.5; -fx-border-radius: 15; -fx-background-radius: 15;");

        playerHandPanel = buildHandPanel();
        playerHandPanel.setMinHeight(72);
        playerHandPanel.setAlignment(Pos.CENTER_LEFT);
        playerHandPanel.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(14, 23, 42, 0.92), rgba(18, 30, 60, 0.88)); -fx-border-color: rgba(160, 192, 255, 0.9); -fx-border-width: 2; -fx-border-radius: 18; -fx-background-radius: 18; -fx-effect: innershadow(gaussian, rgba(45,58,92,0.65), 10, 0.2, 0, 0);");
        playerHandPanel.setPadding(new Insets(8));
        playerHandPanel.setMinWidth(900);
        playerHandPanel.setPrefWidth(1000);
        playerHandPanel.setMaxWidth(Double.MAX_VALUE);

        playerHandScroll = new ScrollPane(playerHandPanel);
        playerHandScroll.setFitToWidth(true);
        playerHandScroll.setFitToHeight(false);
        playerHandScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        playerHandScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        playerHandScroll.setPannable(true);
        playerHandScroll.setPrefViewportHeight(82);
        playerHandScroll.setMinViewportHeight(70);
        playerHandScroll.setMaxWidth(Double.MAX_VALUE);
        playerHandScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent; -fx-box-border: transparent;");

        VBox zone = new VBox(8, playerHandTitleLabel, playerHandScroll);
        zone.setPadding(new Insets(4, 6, 0, 6));
        zone.setMinHeight(90);
        zone.setMaxHeight(90);
        zone.setMinWidth(900);
        zone.setMaxWidth(Double.MAX_VALUE);
        zone.setStyle("-fx-background-color: rgba(9, 16, 30, 0.5); -fx-border-color: rgba(162, 181, 255, 0.45); -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20; -fx-effect: innershadow(gaussian, rgba(33,54,86,0.75), 10, 0.2, 0, 0);");
        return zone;
    }

    private void refreshUI() {
        Player current = gameFlow.getCurrentPlayer();
        Player opponent = gameFlow.getOpponent();

        if (opponentHpLabel != null) {
            opponentHpLabel.setText("HP: " + opponent.getHp());
        }
        if (opponentShieldLabel != null) {
            opponentShieldLabel.setText("Shield: " + opponent.getShield());
        }
        if (opponentEnergyLabel != null) {
            opponentEnergyLabel.setText("Energy: " + opponent.getEnergy());
        }
        if (playerHpLabel != null) {
            playerHpLabel.setText("HP: " + current.getHp());
        }
        if (playerShieldLabel != null) {
            playerShieldLabel.setText("Shield: " + current.getShield());
        }
        if (playerEnergyLabel != null) {
            playerEnergyLabel.setText("Energy: " + current.getEnergy());
        }

        if (playerHandTitleLabel != null) {
            playerHandTitleLabel.setText(current.getName() + " Hand (เทิร์นของคุณ)");
        }
        if (opponentHandTitleLabel != null) {
            opponentHandTitleLabel.setText(opponent.getName() + " Hand (รอก่อน)");
        }

        if (playerALabel != null) {
            playerALabel.setText(String.format("%s • HP %d • EN %d • SH %d",
                    gameFlow.getPlayerA().getName(), gameFlow.getPlayerA().getHp(),
                    gameFlow.getPlayerA().getEnergy(), gameFlow.getPlayerA().getShield()));
        }
        if (playerBLabel != null) {
            playerBLabel.setText(String.format("%s • HP %d • EN %d • SH %d",
                    gameFlow.getPlayerB().getName(), gameFlow.getPlayerB().getHp(),
                    gameFlow.getPlayerB().getEnergy(), gameFlow.getPlayerB().getShield()));
        }

        if (leftHpValue != null) {
            leftHpValue.setText(String.valueOf(gameFlow.getPlayerA().getHp()));
            leftHpBar.setProgress(Math.max(0, Math.min(1.0, gameFlow.getPlayerA().getHp() / 3000.0)));
            leftEnergyValue.setText(String.valueOf(gameFlow.getPlayerA().getEnergy()));
            leftEnergyBar.setProgress(Math.max(0, Math.min(1.0, gameFlow.getPlayerA().getEnergy() / 10.0)));
        }

        if (rightHpValue != null) {
            rightHpValue.setText(String.valueOf(gameFlow.getPlayerB().getHp()));
            rightHpBar.setProgress(Math.max(0, Math.min(1.0, gameFlow.getPlayerB().getHp() / 3000.0)));
            rightEnergyValue.setText(String.valueOf(gameFlow.getPlayerB().getEnergy()));
            rightEnergyBar.setProgress(Math.max(0, Math.min(1.0, gameFlow.getPlayerB().getEnergy() / 10.0)));
        }

        turnLabel.setText("Turn " + gameFlow.getTurnNumber() + " • " + current.getName() + " to act");
        turnLabel.setTextFill(current == gameFlow.getPlayerA() ? Color.web("#9fe8ff") : Color.web("#ffd98b"));

        String phaseText = switch (gameFlow.getCurrentPhase()) {
            case "START_TURN" -> "START TURN";
            case "DRAW", "ACTION", "REVEAL", "MATCH", "RESET" -> "MAIN PHASE";
            case "END" -> "END TURN";
            default -> "SETUP";
        };

        phaseLabel.setText(phaseText);
        phaseLabel.setTextFill(Color.web("#eafcff"));
        switch (phaseText) {
            case "START TURN" -> phaseLabel.setStyle("-fx-padding: 6 12 6 12; -fx-background-color: rgba(11, 59, 34, 0.9); -fx-border-color: #6af7a6; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16;");
            case "MAIN PHASE" -> phaseLabel.setStyle("-fx-padding: 6 12 6 12; -fx-background-color: rgba(24, 42, 82, 0.9); -fx-border-color: #8ab4ff; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16;");
            case "END TURN" -> phaseLabel.setStyle("-fx-padding: 6 12 6 12; -fx-background-color: rgba(66, 26, 18, 0.9); -fx-border-color: #ffb57a; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16;");
            default -> phaseLabel.setStyle("-fx-padding: 6 12 6 12; -fx-background-color: rgba(17, 22, 31, 0.9); -fx-border-color: rgba(160, 185, 255, 0.75); -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16;");
        }

        statusLabel.setText(gameFlow.getStatusMessage());
        refreshBoardUI();

        // Rebuild bottom panel so chain-window / normal buttons update dynamically
        // mainLayout child index 2 = bottom panel (index: 0=opponent hand strip, 1=center, 2=bottom, 3=player hand strip)
        if (mainLayout != null && mainLayout.getChildren().size() >= 3) {
            mainLayout.getChildren().set(2, buildBottomPanel());
        }

        renderHandPanel(opponent, opponentHandPanel, opponentHandScroll);
        renderHandPanel(current, playerHandPanel, playerHandScroll);
    }

    private void renderHandPanel(Player player, HBox panel, ScrollPane scroll) {
        if (panel == null || scroll == null) {
            return;
        }

        panel.getChildren().clear();
        List<Card> cards = gameFlow.getHand(player).getCards();

        if (cards.isEmpty()) {
            StackPane emptySlot = new StackPane();
            emptySlot.setPrefSize(150, 120);
            emptySlot.setStyle("-fx-background-color: rgba(27, 42, 70, 0.7); -fx-border-color: rgba(164, 186, 255, 0.65); -fx-border-width: 2; -fx-border-radius: 16; -fx-background-radius: 16;");

            ImageView back = new ImageView(new Image("file:assets/puzzle_battle_assets/central_card_back_r1_c1.png"));
            back.setFitWidth(150);
            back.setFitHeight(110);
            back.setPreserveRatio(false);

            Label empty = new Label("EMPTY");
            empty.setTextFill(Color.web("#dfeaff"));
            empty.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
            emptySlot.getChildren().addAll(back, empty);
            panel.getChildren().add(emptySlot);
            return;
        }

        Player current = gameFlow.getCurrentPlayer();
        double cardWidth = Math.max(90, Math.min(148, (panel.getWidth() > 0 ? panel.getWidth() : 700) / Math.max(3, cards.size() + 0.7)));
        double cardHeight = cardWidth * 1.18;
        handScrollSnapToCardWidth(scroll, panel, cardWidth);

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            final int cardIndex = i;

            Button cardButton = new Button();
            cardButton.setPrefWidth(cardWidth);
            cardButton.setPrefHeight(cardHeight);
            cardButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-background-insets: 0; -fx-cursor: hand;");

            StackPane cardGraphic = createHandCardGraphic(card, cardWidth, cardHeight);
            cardButton.setGraphic(cardGraphic);

            if (player == current && card.getCost() > current.getEnergy()) {
                cardButton.setDisable(true);
                cardButton.setOpacity(0.55);
            }

            if (player == current) {
                cardButton.setOnAction(e -> {
                    if (isRotating) return;
                    if (gameFlow.playCardFromCurrentHand(cardIndex)) {
                        refreshUI();
                    }
                });
            }
            panel.getChildren().add(cardButton);
        }
    }

    private StackPane createHandCardGraphic(Card card, double width, double height) {
        String name = card.getName();
        String accent = switch (name.toLowerCase()) {
            case "attack" -> "#ff6a6a";
            case "heal" -> "#62d7a3";
            case "energy" -> "#5db9ff";
            case "shield" -> "#d7d4ff";
            case "negate" -> "#ffd86c";
            case "steal" -> "#d291ff";
            case "extra" -> "#ffb284";
            default -> "#9db4ff";
        };

        StackPane outer = new StackPane();
        outer.setPrefSize(width, height);
        outer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(12, 18, 34, 0.92), rgba(21, 31, 52, 0.92)); " +
                "-fx-border-color: rgba(244, 206, 114, 0.9); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 16, 0.22, 0, 6);"
        );

        StackPane face = new StackPane();
        face.setPrefSize(width - 12, height - 12);
        face.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(22, 33, 62, 0.96), rgba(12, 21, 39, 0.96)); " +
                "-fx-border-color: rgba(255,255,255,0.2); " +
                "-fx-border-width: 1.2; " +
                "-fx-border-radius: 14; " +
                "-fx-background-radius: 14;"
        );

        Region header = new Region();
        header.setPrefSize(width - 24, 28);
        header.setStyle("-fx-background-color: linear-gradient(to right, " + accent + ", rgba(255,255,255,0.18)); -fx-background-radius: 10 10 0 0;");
        StackPane.setAlignment(header, Pos.TOP_CENTER);

        Label glyph = new Label(switch (name.toLowerCase()) {
            case "attack" -> "⚔";
            case "heal" -> "✚";
            case "energy" -> "✦";
            case "shield" -> "🛡";
            case "negate" -> "⛨";
            case "steal" -> "❖";
            case "extra" -> "✧";
            default -> "✦";
        });
        glyph.setTextFill(Color.web("#fff8d4"));
        glyph.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
        StackPane.setAlignment(glyph, Pos.TOP_CENTER);

        Label cardName = new Label(name.toUpperCase());
        cardName.setTextFill(Color.web("#fff9d6"));
        cardName.setFont(Font.font("Verdana", FontWeight.BOLD, 13));

        Label cost = new Label("COST " + card.getCost());
        cost.setTextFill(Color.web("#dfeaff"));
        cost.setFont(Font.font("Verdana", FontWeight.BOLD, 11));

        VBox details = new VBox(6, cardName, cost);
        details.setAlignment(Pos.CENTER);
        details.setPadding(new Insets(8, 10, 12, 10));
        details.setStyle("-fx-background-color: rgba(9, 15, 25, 0.48); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 1; -fx-border-radius: 12;");

        String imagePath = resolveCardImagePath(name);
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                ImageView cardImage = new ImageView(new Image(imagePath));
                cardImage.setFitWidth(width - 32);
                cardImage.setFitHeight(height - 70);
                cardImage.setPreserveRatio(false);
                StackPane.setMargin(cardImage, new Insets(34, 0, 42, 0));
                face.getChildren().addAll(header, glyph, cardImage, details);
            } catch (Exception ignored) {
                face.getChildren().addAll(header, glyph, details);
            }
        } else {
            face.getChildren().addAll(header, glyph, details);
        }

        outer.getChildren().add(face);
        return outer;
    }

    private void handScrollSnapToCardWidth(ScrollPane scroll, HBox panel, double cardWidth) {
        if (scroll == null || panel == null || cardWidth <= 0) {
            return;
        }

        double gap = 12;
        double totalScrollWidth = Math.max(0, panel.getWidth() - scroll.getViewportBounds().getWidth());
        if (totalScrollWidth <= 0) {
            return;
        }

        double currentScroll = scroll.getHvalue() * totalScrollWidth;
        double stepWidth = cardWidth + gap;
        double nearest = Math.round(currentScroll / stepWidth) * stepWidth;
        double targetScroll = Math.max(0, Math.min(totalScrollWidth, nearest));
        double targetHValue = targetScroll / totalScrollWidth;

        Timeline snap = new Timeline(
                new KeyFrame(Duration.millis(180), new KeyValue(scroll.hvalueProperty(), targetHValue, Interpolator.EASE_BOTH))
        );
        snap.play();
    }

    private void refreshBoardUI() {
        boardPanel.getChildren().clear();

        double boardAreaWidth = boardPanel.getWidth() > 0 ? boardPanel.getWidth() : 700;
        double boardAreaHeight = boardPanel.getHeight() > 0 ? boardPanel.getHeight() : 300;
        double slotWidth = Math.max(90, Math.min(150, boardAreaWidth / 5.2));
        double slotHeight = Math.max(72, Math.min(120, boardAreaHeight / 3.5));

        for (int i = 0; i < gameFlow.getBoardSize(); i++) {
            final int index = i;
            Button boardButton = new Button();
            boardButton.setFocusTraversable(false);
            boardButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-background-insets: 0;");

            StackPane slot = new StackPane();
            slot.setPrefSize(slotWidth, slotHeight);
            slot.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgba(22, 34, 26, 0.92), rgba(11, 16, 19, 0.92)); " +
                    "-fx-border-color: rgba(218, 179, 84, 0.8); " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 14; " +
                    "-fx-background-radius: 14; " +
                    "-fx-effect: dropshadow(gaussian, rgba(6, 11, 15, 0.5), 10, 0.25, 0, 4);"
            );

            int row = i / 4;
            int column = i % 4;
            String slotPath = "assets/puzzle_battle_assets/central_card_back_r" + (row + 1) + "_c" + (column + 1) + ".png";
            ImageView slotImage = new ImageView(new Image("file:" + slotPath));
            slotImage.setFitWidth(slotWidth);
            slotImage.setFitHeight(slotHeight);
            slotImage.setPreserveRatio(false);
            slot.getChildren().add(slotImage);

            if (gameFlow.isBoardFaceUp(i)) {
                String imagePath = resolveCardImagePath(gameFlow.getBoardCardType(i));
                ImageView image = new ImageView(new Image(imagePath));
                image.setFitWidth(slotWidth * 0.8);
                image.setFitHeight(slotHeight * 0.8);
                image.setPreserveRatio(true);
                slot.getChildren().add(image);
            }

            boardButton.setGraphic(slot);
            animateFlip(boardButton);

            boardButton.setOnAction(e -> {
                if (isRotating || boardLocked) return;
                if (gameFlow.revealBoardCard(index)) {
                    animateFlip(boardButton);
                    String phase = gameFlow.getCurrentPhase();
                    if ("RESET".equals(phase)) {
                        // ไม่ตรงกัน → แสดงการ์ดทั้งคู่ค้างไว้ 1.2 วิ แล้วค่อยคว่ำกลับ
                        boardLocked = true;
                        refreshUI(); // แสดงการ์ดใบที่ 2 ให้เห็นก่อน
                        PauseTransition pause = new PauseTransition(Duration.millis(1200));
                        pause.setOnFinished(ev -> {
                            boardLocked = false;
                            refreshUI(); // คว่ำกลับ
                        });
                        pause.play();
                    } else {
                        refreshUI();
                    }
                }
            });

            boardPanel.add(boardButton, column, row);
        }
    }

    private void animateFlip(Button button) {
        RotateTransition rotate = new RotateTransition(Duration.millis(260), button);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.EASE_BOTH);
        rotate.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(180), button);
        scale.setFromX(0.94);
        scale.setToX(1.0);
        scale.setFromY(0.94);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.play();
    }

    private String resolveCardImagePath(String cardType) {
        String baseName = cardType.toLowerCase();
        String[] candidates = {
                baseName + "_card.png",
                baseName + "_card.jpg",
                baseName + "_card.jpeg",
                baseName + "_card.png.jpg",
                baseName + "_card.jpg.png",
                baseName + ".png",
                baseName + ".jpg",
                baseName + ".jpeg"
        };

        for (String candidate : candidates) {
            File imageFile = new File("assets/cards/" + candidate);
            if (imageFile.exists()) {
                return "file:assets/cards/" + candidate;
            }
        }

        return "file:assets/cards/default_card.png";
    }

    public static void main(String[] args) {
        launch(args);
    }
}