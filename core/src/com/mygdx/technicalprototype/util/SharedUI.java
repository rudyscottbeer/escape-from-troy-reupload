package com.mygdx.technicalprototype.util;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.technicalprototype.InputController;
import com.mygdx.technicalprototype.assets.AssetDirectory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SharedUI {
    /** The singleton instance of the shared UI */
    private static SharedUI theUI = null;
    private InputController inputController;

    /**
     * Return the singleton instance of the shared UI
     *
     * @return the singleton instance of the shared UI
     */
    public static SharedUI getInstance() {
        if (theUI == null) {
            theUI = new SharedUI();
        }
        return theUI;
    }

    private AssetDirectory internal;

    private Stage settings;
    private Stage controlSettings;
    private Stage audioDisplaySettings;
    private Stage credits;

    private TextButton[] settingsButtons = new TextButton[5];
    private TextButton[] controlSettingsButtons = new TextButton[8];
    private TextButton[] controlButtons = new TextButton[6];
    private TextButton[] audioDisplaySettingsButtons = new TextButton[5];
    private TextButton creditsButtons[] = new TextButton[2];

    private StringBuilder realCredits = new StringBuilder();
    private StringBuilder ourCredits = new StringBuilder();
    private boolean real = true;

    private InputController.indexKeyToChange keyToChange;

    private Slider master;
    private Slider music;
    private Slider sfx;
    private Slider brightness;

    private BitmapFont font;
    private BitmapFont titleFont;

    /** The main menu button text style */
    private TextButton.TextButtonStyle buttonStyle;
    /** The text style when the mouse hovers over a UI button*/
    private TextButton.TextButtonStyle hoverStyle;
    private Slider.SliderStyle sliderStyle;
    private TextButton.TextButtonStyle controlButtonStyle;

    private int pressState;
    private TextButton pressed = null;
    private Overlay overlay;

    /**
     * Defines the menu UI button events
     */
    class MenuListener extends InputListener {
        private TextButton textButton;

        public MenuListener(TextButton button) {
            this.textButton = button;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            pressState = 1;
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (pressState == 1 && x >= 0 && x < textButton.getWidth() && y >= 0 && y < textButton.getHeight()) {
                pressState = 2;
                pressed = textButton;

                if (Arrays.asList(settingsButtons).contains(textButton)) {
                    overlay = Overlay.SETTINGS;
                } else if (Arrays.asList(controlSettingsButtons).contains(textButton)) {
                    overlay = Overlay.CONTROLS;
                } else if (Arrays.asList(audioDisplaySettingsButtons).contains(textButton)) {
                    overlay = Overlay.AUDIO_DISPLAY;
                } else {
                    overlay = Overlay.CREDITS;
                }

                if (overlay == Overlay.CREDITS && textButton == creditsButtons[0]) {
                    real = !real;
                    creditsButtons[0].setText(real ? realCredits.toString() : ourCredits.toString());
                }
            } else {
                pressState = 0;
            }
            super.touchUp(event, x, y, pointer, button);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (!Arrays.asList(controlButtons).contains(textButton))
                textButton.setStyle(hoverStyle);
            super.enter(event, x, y, pointer, fromActor);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (!Arrays.asList(controlButtons).contains(textButton))
                textButton.setStyle(buttonStyle);
            super.exit(event, x, y, pointer, toActor);

        }
    }

    public InputProcessor getButtonInputProcessor() {
        return new InputProcessor() {
            @Override
            public boolean touchUp(int screenX, int screenY,
                                   int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY,
                                        int pointer) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY,
                                     int pointer, int button) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(float v, float v1) {
                return false;
            }

            @Override
            public boolean keyUp(int i) {
                if(keyToChange != null){
                    switch (keyToChange){
                        case FORWARD:{
                            inputController.setKeyForControl(InputController.indexKeyToChange.FORWARD, i);
                            keyToChange = null;
                            controlButtons[0].setText(Input.Keys.toString(i));
                            return true;
                        }
                        case BACKWARD: {
                            inputController.setKeyForControl(InputController.indexKeyToChange.BACKWARD, i);
                            keyToChange = null;
                            controlButtons[1].setText(Input.Keys.toString(i));
                            return true;
                        }
                        case DECREASE:{
                            inputController.setKeyForControl(InputController.indexKeyToChange.DECREASE, i);
                            keyToChange = null;
                            controlButtons[2].setText(Input.Keys.toString(i));
                            return true;
                        }
                        case INCREASE:{
                            inputController.setKeyForControl(InputController.indexKeyToChange.INCREASE, i);
                            keyToChange = null;
                            controlButtons[3].setText(Input.Keys.toString(i));
                            return true;
                        }
                        case RESTART:{
                            inputController.setKeyForControl(InputController.indexKeyToChange.RESTART, i);
                            keyToChange = null;
                            controlButtons[4].setText(Input.Keys.toString(i));
                            return true;
                        }
                        case PAUSE:{
                            inputController.setKeyForControl(InputController.indexKeyToChange.PAUSE, i);
                            keyToChange = null;
                            controlButtons[5].setText(Input.Keys.toString(i));
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                return false;
            }
        };
    }

    public SharedUI() {
        inputController = InputController.getInstance();

        internal = new AssetDirectory( "loading.json" );
        internal.loadAssets();
        internal.finishLoading();

        realCredits.append("                      TEAM LEAD          SHUNGO NAJIMA \n\n");
        realCredits.append("   SOFTWARE CO-LEAD          RUDY BEER     \n\n");
        realCredits.append("   SOFTWARE CO-LEAD          AIDAN CAMPBELL\n\n");
        realCredits.append("               PROGRAMMER          MIRANDA REN   \n\n");
        realCredits.append("               PROGRAMMER          LIFAN ZENG    \n\n");
        realCredits.append("                  DESIGN LEAD          RAE CHEN      \n\n");
        realCredits.append("                       UI/UX LEAD          JIN PARK      \n\n");
        realCredits.append("        MUSIC COMPOSER          NINA YANG     \n\n");
        realCredits.append("HONORABLE MENTION          CRYSTAL LU    \n");

        ourCredits.append("                      PATH LEAD          SHUNGO NAJIMA \n\n");
        ourCredits.append("   SOUND EFFECT LEAD          RUDY BEER     \n\n");
        ourCredits.append("   REFACTORING LEAD          AIDAN CAMPBELL\n\n");
        ourCredits.append("               QUOTES LEAD          MIRANDA REN   \n\n");
        ourCredits.append("               SPOTIFY LEAD          LIFAN ZENG    \n\n");
        ourCredits.append("                  WEEB LEAD          RAE CHEN      \n\n");
        ourCredits.append("                       CHEESE LEAD          JIN PARK      \n\n");
        ourCredits.append("                  DOGE LEAD          NINA YANG     \n\n");
        ourCredits.append("HONORABLE MENTION          CRYSTAL LU    \n");

        settings = new Stage();
        controlSettings = new Stage();
        credits = new Stage();
        audioDisplaySettings = new Stage();

        Table settingsTable = new Table();
        settingsTable.setFillParent(true);
        settings.addActor(settingsTable);

        Table controlsTable = new Table();
        controlsTable.setFillParent(true);
        controlSettings.addActor(controlsTable);

        Table audioDisplayTable = new Table();
        audioDisplayTable.setFillParent(true);
        audioDisplaySettings.addActor(audioDisplayTable);

        Table creditsTable = new Table();
        creditsTable.setFillParent(true);
        credits.addActor(creditsTable);

        buttonStyle = new TextButton.TextButtonStyle();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("troy/fonts/Montserrat-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.shadowColor = new Color(255, 255, 255, 0.8f);
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);
        parameter.size = 40;
        titleFont = generator.generateFont(parameter);
        generator.dispose(); // don't forget to dispose to avoid memory leaks
        buttonStyle.font = font;
        buttonStyle.fontColor = new Color(255,255,255,0.5f);
        hoverStyle = new TextButton.TextButtonStyle(buttonStyle);
        hoverStyle.fontColor = new Color(255,255,255,0.8f);

        Drawable rect = new TextureRegionDrawable(new TextureRegion(internal.getEntry("rectangle", Texture.class)));
        controlButtonStyle = new TextButton.TextButtonStyle(rect, rect, rect, font);
        controlButtonStyle.fontColor = new Color(255,255,255,0.5f);
        controlButtonStyle.overFontColor = new Color(255,255,255,0.8f);

        sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = new TextureRegionDrawable(internal.getEntry( "slider:full", Texture.class));
        sliderStyle.knob = new TextureRegionDrawable(internal.getEntry( "slider:knob", Texture.class));

        // define the settings buttons
        settingsButtons[0] = new TextButton("CONTROLS", buttonStyle);
        settingsButtons[0].setBounds(0, 0, settingsButtons[0].getWidth(), settingsButtons[0].getHeight());
        settingsButtons[0].addListener(new SharedUI.MenuListener(settingsButtons[0]));
        settingsButtons[1] = new TextButton("\nAUDIO/DISPLAY", buttonStyle);
        settingsButtons[1].setBounds(0, 0, settingsButtons[1].getWidth(), settingsButtons[1].getHeight());
        settingsButtons[1].addListener(new SharedUI.MenuListener(settingsButtons[1]));
        settingsButtons[2] = new TextButton("\nCREDITS", buttonStyle);
        settingsButtons[2].setBounds(0, 0, settingsButtons[2].getWidth(), settingsButtons[2].getHeight());
        settingsButtons[2].addListener(new SharedUI.MenuListener(settingsButtons[2]));
        settingsButtons[3] = new TextButton("\nBACK", buttonStyle);
        settingsButtons[3].setBounds(0, 0, settingsButtons[3].getWidth(), settingsButtons[3].getHeight());
        settingsButtons[3].addListener(new SharedUI.MenuListener(settingsButtons[3]));
        settingsButtons[4] = new TextButton("\nEXIT TO DESKTOP", buttonStyle);
        settingsButtons[4].setBounds(0, 0, settingsButtons[4].getWidth(), settingsButtons[4].getHeight());
        settingsButtons[4].addListener(new SharedUI.MenuListener(settingsButtons[4]));

        // define the controls buttons
        controlButtons[0] = new TextButton("W", controlButtonStyle);
        controlButtons[0].setBounds(0, 0, controlButtons[0].getWidth(), controlButtons[0].getHeight());
        controlButtons[0].addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                keyToChange = InputController.indexKeyToChange.FORWARD;
                controlButtons[0].setText("_");
            }
        });
        controlButtons[1] = new TextButton("S", controlButtonStyle);
        controlButtons[1].setBounds(0, 0, controlButtons[1].getWidth(), controlButtons[1].getHeight());
        controlButtons[1].addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                keyToChange = InputController.indexKeyToChange.BACKWARD;
                controlButtons[1].setText("_");
            }
        });
        controlButtons[2] = new TextButton("A", controlButtonStyle);
        controlButtons[2].setBounds(0, 0, controlButtons[2].getWidth(), controlButtons[2].getHeight());
        controlButtons[2].addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                keyToChange = InputController.indexKeyToChange.DECREASE;
                controlButtons[2].setText("_");
            }
        });
        controlButtons[3] = new TextButton("D", controlButtonStyle);
        controlButtons[3].setBounds(0, 0, controlButtons[3].getWidth(), controlButtons[3].getHeight());
        controlButtons[3].addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                keyToChange = InputController.indexKeyToChange.INCREASE;
                controlButtons[3].setText("_");
            }
        });
        controlButtons[4] = new TextButton("R", controlButtonStyle);
        controlButtons[4].setBounds(0, 0, controlButtons[4].getWidth(), controlButtons[4].getHeight());
        controlButtons[4].addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                keyToChange = InputController.indexKeyToChange.RESTART;
                controlButtons[4].setText("_");
            }
        });
        controlButtons[5] = new TextButton("ESC", controlButtonStyle);
        controlButtons[5].setBounds(0, 0, controlButtons[5].getWidth(), controlButtons[5].getHeight());
        controlButtons[5].addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                keyToChange = InputController.indexKeyToChange.PAUSE;
                controlButtons[5].setText("_");
            }
        });

        controlSettingsButtons[0] = new TextButton("MOVE FORWARD", buttonStyle);
        controlSettingsButtons[0].setBounds(0, 0, controlSettingsButtons[0].getWidth(), controlSettingsButtons[0].getHeight());
        controlSettingsButtons[0].addListener(new SharedUI.MenuListener(controlSettingsButtons[0]));
        controlSettingsButtons[1] = new TextButton("\nMOVE BACKWARD\n", buttonStyle);
        controlSettingsButtons[1].setBounds(0, 0, controlSettingsButtons[1].getWidth(), controlSettingsButtons[1].getHeight());
        controlSettingsButtons[1].addListener(new SharedUI.MenuListener(controlSettingsButtons[1]));
        controlSettingsButtons[2] = new TextButton("SHRINK PLANET", buttonStyle);
        controlSettingsButtons[2].setBounds(0, 0, controlSettingsButtons[2].getWidth(), controlSettingsButtons[2].getHeight());
        controlSettingsButtons[2].addListener(new SharedUI.MenuListener(controlSettingsButtons[2]));
        controlSettingsButtons[3] = new TextButton("\nGROW PLANET\n", buttonStyle);
        controlSettingsButtons[3].setBounds(0, 0, controlSettingsButtons[3].getWidth(), controlSettingsButtons[3].getHeight());
        controlSettingsButtons[3].addListener(new SharedUI.MenuListener(controlSettingsButtons[3]));
        controlSettingsButtons[4] = new TextButton("RETRY FROM CHECKPOINT        ", buttonStyle);
        controlSettingsButtons[4].setBounds(0, 0, controlSettingsButtons[4].getWidth(), controlSettingsButtons[4].getHeight());
        controlSettingsButtons[4].addListener(new SharedUI.MenuListener(controlSettingsButtons[4]));
        controlSettingsButtons[5] = new TextButton("\nPAUSE/BACK\n", buttonStyle);
        controlSettingsButtons[5].setBounds(0, 0, controlSettingsButtons[5].getWidth(), controlSettingsButtons[5].getHeight());
        controlSettingsButtons[5].addListener(new SharedUI.MenuListener(controlSettingsButtons[5]));
        controlSettingsButtons[6] = new TextButton("RESET TO DEFAULTS", buttonStyle);
        controlSettingsButtons[6].setBounds(0, 0, controlSettingsButtons[6].getWidth(), controlSettingsButtons[6].getHeight());
        controlSettingsButtons[6].addListener(new SharedUI.MenuListener(controlSettingsButtons[6]));
        controlSettingsButtons[7] = new TextButton("\nBACK", buttonStyle);
        controlSettingsButtons[7].setBounds(0, 0, controlSettingsButtons[7].getWidth(), controlSettingsButtons[7].getHeight());
        controlSettingsButtons[7].addListener(new SharedUI.MenuListener(controlSettingsButtons[7]));

        // define the audio settings buttons
        audioDisplaySettingsButtons[0] = new TextButton("MASTER", buttonStyle);
        audioDisplaySettingsButtons[0].setBounds(0, 0, audioDisplaySettingsButtons[0].getWidth(), audioDisplaySettingsButtons[0].getHeight());
        audioDisplaySettingsButtons[0].addListener(new SharedUI.MenuListener(audioDisplaySettingsButtons[0]));
        audioDisplaySettingsButtons[1] = new TextButton("\nMUSIC\n", buttonStyle);
        audioDisplaySettingsButtons[1].setBounds(0, 0, audioDisplaySettingsButtons[1].getWidth(), audioDisplaySettingsButtons[1].getHeight());
        audioDisplaySettingsButtons[1].addListener(new SharedUI.MenuListener(audioDisplaySettingsButtons[1]));
        audioDisplaySettingsButtons[2] = new TextButton("SOUND EFFECTS        ", buttonStyle);
        audioDisplaySettingsButtons[2].setBounds(0, 0, audioDisplaySettingsButtons[2].getWidth(), audioDisplaySettingsButtons[2].getHeight());
        audioDisplaySettingsButtons[2].addListener(new SharedUI.MenuListener(audioDisplaySettingsButtons[2]));
        audioDisplaySettingsButtons[3] = new TextButton("\nBRIGHTNESS\n", buttonStyle);
        audioDisplaySettingsButtons[3].setBounds(0, 0, audioDisplaySettingsButtons[3].getWidth(), audioDisplaySettingsButtons[3].getHeight());
        audioDisplaySettingsButtons[3].addListener(new SharedUI.MenuListener(audioDisplaySettingsButtons[3]));
        audioDisplaySettingsButtons[4] = new TextButton("BACK", buttonStyle);
        audioDisplaySettingsButtons[4].setBounds(0, 0, audioDisplaySettingsButtons[4].getWidth(), audioDisplaySettingsButtons[4].getHeight());
        audioDisplaySettingsButtons[4].addListener(new SharedUI.MenuListener(audioDisplaySettingsButtons[4]));

        creditsButtons[0] = new TextButton(realCredits.toString(), buttonStyle);
        creditsButtons[0].setBounds(0, 0, creditsButtons[0].getWidth(), creditsButtons[0].getHeight());
        creditsButtons[0].addListener(new SharedUI.MenuListener(creditsButtons[0]));
        creditsButtons[0].getLabel().setAlignment(Align.left);
        creditsButtons[1] = new TextButton("BACK", buttonStyle);
        creditsButtons[1].setBounds(0, 0, creditsButtons[1].getWidth(), creditsButtons[1].getHeight());
        creditsButtons[1].addListener(new SharedUI.MenuListener(creditsButtons[1]));

        master = new Slider(0,1,0.1f, false, sliderStyle);
        master.setVisualPercent(1);
        music = new Slider(0,1,0.1f, false, sliderStyle);
        music.setVisualPercent(1);
        sfx = new Slider(0,1,0.1f, false, sliderStyle);
        sfx.setVisualPercent(1);
        brightness = new Slider(0, 1, 0.1f, false, sliderStyle);
        brightness.setVisualPercent(1);

        settingsTable.center().padTop(Gdx.graphics.getHeight()/4);
        settingsTable.add(settingsButtons[0]).center();
        settingsTable.row();
        settingsTable.add(settingsButtons[1]).center();
        settingsTable.row();
        settingsTable.add(settingsButtons[2]).center();
        settingsTable.row();
        settingsTable.add(settingsButtons[3]).center();
        settingsTable.row();
        settingsTable.add(settingsButtons[4]).center();

        controlsTable.center().padTop(Gdx.graphics.getHeight()/4);
        controlsTable.add(controlSettingsButtons[0]).align(Align.left);
        controlsTable.add(controlButtons[0]).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[1]).align(Align.left);
        controlsTable.add(controlButtons[1]).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[2]).align(Align.left);
        controlsTable.add(controlButtons[2]).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[3]).align(Align.left);
        controlsTable.add(controlButtons[3]).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[4]).align(Align.left);
        controlsTable.add(controlButtons[4]).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[5]).align(Align.left);
        controlsTable.add(controlButtons[5]).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[6]).colspan(1).center();
        controlsTable.row();
        controlsTable.add(controlSettingsButtons[7]).colspan(1).center();

        audioDisplayTable.center().padTop(Gdx.graphics.getHeight()/4);
        audioDisplayTable.add(audioDisplaySettingsButtons[0]).align(Align.left);
        audioDisplayTable.add(master).center();
        audioDisplayTable.row();
        audioDisplayTable.add(audioDisplaySettingsButtons[1]).align(Align.left);
        audioDisplayTable.add(music);
        audioDisplayTable.row();
        audioDisplayTable.add(audioDisplaySettingsButtons[2]).align(Align.left);
        audioDisplayTable.add(sfx);
        audioDisplayTable.row();
        audioDisplayTable.add(audioDisplaySettingsButtons[3]).align(Align.left);
        audioDisplayTable.add(brightness);
        audioDisplayTable.row();
        audioDisplayTable.add(audioDisplaySettingsButtons[4]).center();

        creditsTable.center().padTop(Gdx.graphics.getHeight()/4);
        creditsTable.add(creditsButtons[0]).center().align(-1);
        creditsTable.row();
        creditsTable.add(creditsButtons[1]).center();
    }

    public int getPressState() {
        return pressState;
    }

    public TextButton getPressedButton() {
        return pressed;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void reset() {
        pressState = 0;
        pressed = null;
        overlay = Overlay.SETTINGS;
    }

    public void setState(Overlay o) {
        overlay = o;
    }

    public Stage getSettings() {
        return settings;
    }

    public Stage getControlSettings() {
        return controlSettings;
    }

    public Stage getAudioDisplaySettings() {
        return audioDisplaySettings;
    }

    public Stage getCredits() {
        return credits;
    }

    public TextButton[] getSettingsButtons() {
        return settingsButtons;
    }

    public TextButton[] getControlSettingsButtons() {
        return controlSettingsButtons;
    }

    public TextButton[] getAudioDisplaySettingsButtons() {
        return audioDisplaySettingsButtons;
    }

    public TextButton[] getCreditsButtons() {
        return creditsButtons;
    }

    public boolean getMasterDragged() {
        return master.isDragging();
    }

    public float getMasterVolume() {
        return master.getPercent();
    }

    public boolean getMusicDragged() {
        return music.isDragging();
    }

    public float getMusicVolume() {
        return music.getPercent();
    }

    public boolean getSFXDragged() {
        return sfx.isDragging();
    }

    public float getSFXVolume() {
        return sfx.getPercent();
    }

    public boolean getBrightnessDragged() {
        return brightness.isDragging();
    }

    public float getBrightness() {
        return brightness.getPercent();
    }

    public TextButton.TextButtonStyle getHoverStyle() {
        return hoverStyle;
    }

    public TextButton.TextButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    public BitmapFont getFont() {
        return font;
    }

    public BitmapFont getTitleFont() {
        return titleFont;
    }

    public enum Overlay {
        SETTINGS, CONTROLS, AUDIO_DISPLAY, CREDITS
    }
}
