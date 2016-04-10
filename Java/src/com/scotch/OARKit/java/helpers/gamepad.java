package com.scotch.OARKit.java.helpers;

import com.kenai.jaffl.struct.Struct;
import com.scotch.OARKit.java.Command.Interpreter;
import com.scotch.OARKit.java.helpers.JInputJoystick;
import javafx.scene.control.Button;
import net.java.games.input.*;


public class gamepad {

    public static boolean connected = true;

    public float leftstickx;
    public float leftsticky;

    public float rightstickx;
    public float rightsticky;

    public int HatSwitchPosition = 0;

    public boolean ButtonX;
    public boolean ButtonY;
    public boolean ButtonA;
    public boolean ButtonB;
    public boolean ButtonRT;
    public boolean ButtonRB;
    public boolean ButtonLT;
    public boolean ButtonLB;

    private int numberOfButtons;

    private JInputJoystick gamepad;

    public void gamepad(){
        gamepad = new JInputJoystick(Controller.Type.GAMEPAD, Controller.Type.STICK);
        gamepad.pollController();
        numberOfButtons = gamepad.getNumberOfButtons();

        // Check if the controller was found.
        if( !gamepad.isControllerConnected() ){
            connected = false;
            Logger.info("No controller found!");
        } else {
            Logger.info("Controller found, printing details:");
            Logger.info(gamepad.getControllerName());
            Logger.info(gamepad.getControllerType());
            Logger.info("This controller has " + numberOfButtons + " buttons");
        }
    }

    public void pollgamepad(){
        if(!gamepad.pollController()) {
            Logger.info("Controller disconnected!");
            connected = false;
        } else {
            // TODO poll all the axis and set them to variables
            leftstickx = gamepad.getX_LeftJoystick_Percentage();
            leftsticky = gamepad.getY_LeftJoystick_Percentage();

            rightstickx = gamepad.getX_RightJoystick_Percentage();
            rightsticky = gamepad.getY_RightJoystick_Percentage();

            if (gamepad.getHatSwitchPosition()==0.25) {
                HatSwitchPosition=1; //up
            } else if (gamepad.getHatSwitchPosition()==0.5) {
                HatSwitchPosition=2; //right
            } else if (gamepad.getHatSwitchPosition()==0.75) {
                HatSwitchPosition=3; //down
            } else if (gamepad.getHatSwitchPosition()==1.0) {
                HatSwitchPosition=4; //left
            } else {
                HatSwitchPosition = 0;
            }

            /*if (gamepad.getHatSwitchPosition()==0.25) {
                ButtonUp=true;
                ButtonRight=false;
                ButtonDown=false;
                ButtonLeft=false;
            } else if (gamepad.getHatSwitchPosition()==0.5) {
                ButtonUp=false;
                ButtonDown=true;
                ButtonLeft=false;
                ButtonRight=false;
            } else if (gamepad.getHatSwitchPosition()==0.75) {
                ButtonUp=false;
                ButtonDown=false;
                ButtonLeft=true;
                ButtonRight=false;
            } else if (gamepad.getHatSwitchPosition()==1.0) {
                ButtonUp=false;
                ButtonDown=false;
                ButtonLeft=false;
                ButtonRight=true;
            } else {
                ButtonUp=false;
                ButtonDown=false;
                ButtonLeft=false;
                ButtonRight=false;
            }/**/

            /*if (gamepad.getControllerName().equals("Controller (Rock Candy Gamepad for Xbox 360)")) {
                ButtonX = gamepad.getButtonValue(2);
                ButtonY = gamepad.getButtonValue(3);
                ButtonA = gamepad.getButtonValue(0);
                ButtonB = gamepad.getButtonValue(1);
                ButtonRT = gamepad.getButtonValue(6);
                ButtonRB = gamepad.getButtonValue(5);
                ButtonLT = gamepad.getButtonValue(10);
                ButtonLB = gamepad.getButtonValue(4);
            } else {
                ButtonX = gamepad.getButtonValue(0);
                ButtonY = gamepad.getButtonValue(3);
                ButtonA = gamepad.getButtonValue(1);
                ButtonB = gamepad.getButtonValue(2);
                ButtonRT = gamepad.getButtonValue(4);
                ButtonRB = gamepad.getButtonValue(5);
                ButtonLT = gamepad.getButtonValue(6);
                ButtonLB = gamepad.getButtonValue(7);
            }/**/
            ButtonX = gamepad.getButtonValue(0);
            ButtonY = gamepad.getButtonValue(3);
            ButtonA = gamepad.getButtonValue(1);
            ButtonB = gamepad.getButtonValue(2);
            ButtonRT = gamepad.getButtonValue(4);
            ButtonRB = gamepad.getButtonValue(5);
            ButtonLT = gamepad.getButtonValue(6);
            ButtonLB = gamepad.getButtonValue(7);

            createCommand();
        }
    }
    private void createCommand() {
        if (ServerConnect.connected) {
            new Interpreter("gamecontroller " + ConvertToHex(Math.round(leftstickx)) + ConvertToHex(Math.round(leftsticky)) + ConvertToHex(Math.round(rightstickx)) + ConvertToHex(Math.round(rightsticky)) + "77000000000000").returnCommand().runCommand();
        }
    }
    private String ConvertToHex(int axis){
        return Integer.toHexString(((axis) * 15)/100);
    }
}
