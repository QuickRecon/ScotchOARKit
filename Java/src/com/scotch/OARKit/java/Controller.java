package com.scotch.OARKit.java;

import com.scotch.OARKit.java.Command.Interpreter;
import com.scotch.OARKit.java.ServerList.*;
import com.scotch.OARKit.java.helpers.*;
import com.sun.xml.internal.bind.v2.TODO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.lang.Thread;

import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable, Runnable{

    public static boolean running = true;
    public static boolean connected = false;

    public static ServerConnect serverConnect;

    @FXML
    ToggleButton ButtonY;
    @FXML
    ToggleButton ButtonB;
    @FXML
    ToggleButton ButtonA;
    @FXML
    ToggleButton ButtonX;

    @FXML
    ToggleButton ButtonUp;
    @FXML
    ToggleButton ButtonDown;
    @FXML
    ToggleButton ButtonRight;
    @FXML
    ToggleButton ButtonLeft;

    @FXML
    ToggleButton ButtonL1;
    @FXML
    ToggleButton ButtonL2;

    @FXML
    ToggleButton ButtonR1;
    @FXML
    ToggleButton ButtonR2;

    @FXML
    Label ServerConnectionStatus;
    @FXML
    Label ControllerStatus;

    @FXML
    Button StopServer;
    @FXML
    WebView CameraWebView;
    WebEngine engine;
    @FXML
    TextArea consoleLog;
    @FXML
    TextField consoleTextField;
    @FXML
    Button restartNginx;
    @FXML
    Button sendButton;
    @FXML
    ToggleButton connectButton;
    public static ToggleButton connectButton1;
    @FXML
    ProgressBar StrengthBar;
    @FXML
    Label StrengthDBMLabel;
    @FXML
    Label StrengthLabel;
    @FXML
    Button addNewConfiguration;
    @FXML
    Label ConnectionType;
    @FXML
    ToggleButton on;
    @FXML
    ToggleButton off;
    @FXML
    MenuButton ipSelector;
    public static MenuButton ipSelector1;
    @FXML
    Label nameLabel;
    public static Label nameLabel1;
    @FXML
    Label ipLabel;
    public static Label ipLabel1;
    @FXML
    Label portLabel;
    public static Label portLabel1;
    @FXML
    TitledPane consoleTitle;

    @FXML
    ProgressBar LeftX;
    @FXML
    ProgressBar LeftY;
    @FXML
    ProgressBar RightX;
    @FXML
    ProgressBar RightY;

    Console console;
    PrintStream ps;
    //Keep old output system
    PrintStream out;
    //PrintStream err;
    NetworkManager networkManager;

    gamepad gamepad;

    String currentip;
    int currentport;

    public void currentTime1() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Timeline currentTime;
        currentTime = new Timeline(new KeyFrame(Duration.seconds(1), event -> consoleTitle.setText("Console - Time: " + format.format(Calendar.getInstance().getTime()))));
        currentTime.setCycleCount(Animation.INDEFINITE);
        currentTime.play();
    }

    //this is meant to be called with in the running loop for each button on the controller and should provide feedback in the UI of what buttons are pressed. i don't know how to test if a button is pressed so i have used console input to test

    public void controllerButtons(Boolean ButtonPressed, ToggleButton FeedBackViewer) {

        if (ButtonPressed) {
            FeedBackViewer.setSelected(true);
        } else {
            FeedBackViewer.setSelected(false);
        }
    }

    public static void AddConfigToList() {
        ipSelector1.getItems().clear();
        String[] servers = ServerList.getKeys();

        for (int i = 0; i < servers.length; i++) {
            //Logger.info(servers[i]);
            MenuItem newServerName =new MenuItem(servers[i]);
            newServerName.setId(servers[i]);
            final int finalI = i;
            newServerName.setOnAction(event -> {
                String ip = ServerList.getIPAndPort(servers[finalI])[0];
                String port = ServerList.getIPAndPort(servers[finalI])[1];
                Logger.info("Loaded config '"+servers[finalI]+"' ("+ip+", "+port+")");
                nameLabel1.setText("Name: "+servers[finalI]);
                ipLabel1.setText("IP: "+ip);
                portLabel1.setText("Port: "+port);
                if (ServerConnect.connected){
                    ServerDisconnect();
                }
            });
            Platform.runLater(() -> ipSelector1.getItems().addAll(newServerName));
        }
    }

    public static void ServerDisconnect() {
        if (ServerConnect.connected) {
            //connectButton1.setSelected(false);
            Logger.info("Closing Socket");
            serverConnect.socketClose();
            //connectButton1.setText("Connect");
            connected = false;
        }
    }

    public void ServerConnect(String name, String ip, String port) {
        if (!ServerConnect.connected){
            serverConnect = new ServerConnect(ip, port);
            if (ServerConnect.connected == true) {
                engine.load("http://"+ip);
                Logger.info("Connected to new Server "+name+" ("+ip+", "+port+")");
                //connectButton.setText("Disconnect");
                connected = true;
            }
        }
    }

    public void ToggleServerConnection(String name, String ip, String port) {
        if (!ServerConnect.connected){
            ServerConnect(name, ip, port);
        }else {
            ServerDisconnect();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            createEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ipSelector1 = ipSelector;
        connectButton1 = connectButton;
        nameLabel1 = nameLabel;
        ipLabel1 = ipLabel;
        portLabel1 = portLabel;
        currentTime1();
        new GetServerList("com/scotch/OARKit/assets/properties/servers.sList");
        try {
            networkManager = new NetworkManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AddConfigToList();
        gamepad = new gamepad();
        gamepad.gamepad();
        out = System.out;
        //err = System.err;
        console = new Console(consoleLog);
        ps = new PrintStream(console, true);
        redirectOutput(ps);

        engine = CameraWebView.getEngine();
        new Thread(this).start();
        if(Main.properties.getProperty("insideDev").equals("true")){
            //Logger.info("Inside Dev Environment");
            engine.load("https://i.redditmedia.com/J3mJIH7RUqrmZ5KD_dWCJ-K8Mx-dJ2kSQWbsWqg4Fbc.jpg?w=557&s=ecd9c0b7d7023a17edd305e6af52cdae");
            //connectIP.setText("192.168.100.1");
            //engine.loadContent("");
            Logger.info("Inside Dev Environment");
        }
        if(ServerConnect.connected&&Main.properties.getProperty("insideDev").equals("false")){
            //connectButton.setSelected(true);
            connected = true;
            engine.load("http://192.168.100.1");
        }
    }

    public void createEvents() throws IOException {
        restartNginx.setOnAction(event -> new Interpreter("print hello").returnCommand().runCommand());
        StopServer.setOnAction(event -> new Interpreter("stopserver").returnCommand().runCommand());

        sendButton.setOnAction(event -> {
            new Interpreter(consoleTextField.getText()).returnCommand().runCommand();
            consoleTextField.setText("");
        });

        addNewConfiguration.setOnAction(event -> {
            try {
                Stage NetWindow = new Stage();
                Parent NetRoot = FXMLLoader.load(getClass().getClassLoader().getResource("com/scotch/OARKit/assets/layout/NetworkManager.fxml"));
                NetWindow.setTitle("Network Manager");
                NetWindow.setScene(new Scene(NetRoot, 358, 191));
                NetWindow.setAlwaysOnTop(true);
                NetWindow.setResizable(false);
                NetWindow.initModality(Modality.WINDOW_MODAL);
                NetWindow.initOwner(addNewConfiguration.getScene().getWindow());
                NetWindow.show();
            } catch (IOException e){
                e.printStackTrace();
            }
        });

        connectButton.setOnAction(event -> {
            if (!nameLabel.getText().equals("Name:")) {
                ToggleServerConnection(nameLabel.getText().replace("Name: ", ""), ipLabel.getText().replace("IP: ", ""), portLabel.getText().replace("Port: ", ""));
            } else {
                Logger.info("Please select a server");
                //connectButton.setSelected(false);
            }
        });

        /*on.setOnAction(event -> {
            on.setDisable(true);
            off.setDisable(false);
        });
        off.setOnAction(event -> {
            off.setDisable(true);
            on.setDisable(false);
        });/**/
    }

    @Override
    public void run() {
        while(running) try {
            try{networkManager.update();} catch (SocketException e) {}
            Platform.runLater(() -> StrengthBar.setProgress(networkManager.getSignalStrength()));
            Platform.runLater(() -> StrengthDBMLabel.setText("Strength: "+networkManager.getRawSignalStrength()+ " Dbm"));
            Platform.runLater(() -> ConnectionType.setText("Connection: "+networkManager.getConnectionType()));

            if (networkManager.getSignalStrength() == -1){
                Platform.runLater(() ->StrengthDBMLabel.setVisible(false));
                Platform.runLater(() ->StrengthBar.setVisible(false));
                Platform.runLater(() ->StrengthLabel.setVisible(false));
                //REPEATS THE OUTPUT
                //Logger.info("Not connected to wifi.");
            } else {
                Platform.runLater(() ->StrengthDBMLabel.setVisible(true));
                Platform.runLater(() ->StrengthBar.setVisible(true));
                Platform.runLater(() ->StrengthLabel.setVisible(true));
            }
            if (gamepad.connected){
                gamepad.pollgamepad();
                Platform.runLater(() -> LeftX.setProgress(gamepad.leftstickx/100));
                Platform.runLater(() -> LeftY.setProgress(gamepad.leftsticky/100));

                Platform.runLater(() -> RightX.setProgress(gamepad.rightstickx/100));
                Platform.runLater(() -> RightY.setProgress(gamepad.rightsticky/100));
                ControllerStatus.setText("Controller: Connected");
            } else {
                ControllerStatus.setText("Controller: Not Connected");
            }
            if (!connected){
                Platform.runLater(() -> connectButton.setText("Connect"));
                Platform.runLater(() -> connectButton.setSelected(false));
                Platform.runLater(() -> ServerConnectionStatus.setText("Server: Not Connected"));
            } else if (connected){
                Platform.runLater(() -> connectButton.setText("Disconnect"));
                Platform.runLater(() -> connectButton.setSelected(true));
                Platform.runLater(() -> ServerConnectionStatus.setText("Server: Connected"));
            }
            if (!consoleTextField.getText().isEmpty()) {
                sendButton.setDisable(false);
            } else {
                sendButton.setDisable(true);
            }
            //these are to test the controller visual feedback using the console input field as a substitute for the controller input
            controllerButtons(consoleTextField.getText().equals("A"), ButtonA);
            controllerButtons(consoleTextField.getText().equals("B"), ButtonB);
            controllerButtons(consoleTextField.getText().equals("X"), ButtonX);
            controllerButtons(consoleTextField.getText().equals("Y"), ButtonY);
            controllerButtons(consoleTextField.getText().equals("Up"), ButtonUp);
            controllerButtons(consoleTextField.getText().equals("Down"), ButtonDown);
            controllerButtons(consoleTextField.getText().equals("Left"), ButtonLeft);
            controllerButtons(consoleTextField.getText().equals("Right"), ButtonRight);
            controllerButtons(consoleTextField.getText().equals("R1"), ButtonR1);
            controllerButtons(consoleTextField.getText().equals("R2"), ButtonR2);
            controllerButtons(consoleTextField.getText().equals("L1"), ButtonL1);
            controllerButtons(consoleTextField.getText().equals("L2"), ButtonL2);
            //Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Defines Output Stream
    private class Console extends OutputStream {

        private TextArea txtArea;
        File baseFile;

        public Console(TextArea txtArea) {
            this.txtArea = txtArea;
            baseFile = new File(OSUtils.osAppData()+"/log.log");
            if(!baseFile.exists()){
                try {
                    baseFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        public void write(int b) throws IOException {
            Files.write(baseFile.toPath(),new String(new char[]{(char)b}).getBytes(),StandardOpenOption.APPEND);
            Platform.runLater(() -> txtArea.appendText(String.valueOf((char) b)));
            out.print(String.valueOf((char) b));
        }

    }
    public void redirectOutput(PrintStream printStream){
        System.setOut(printStream);
        System.setErr(printStream);
    }
    public static void disconnectServer(){
        connected = false;
    }/**/
}