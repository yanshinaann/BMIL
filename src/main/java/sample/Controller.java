package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import javax.xml.soap.Node;
import java.io.IOException;
import java.util.*;

import static sample.PasswordUtils.*;


public class Controller {
    @FXML
    private TextField pswField;
    @FXML
    private BarChart barChart;
    @FXML
    private LineChart lineChart;
    @FXML
    private Label holdTime;
    @FXML
    private Label expectation;
    @FXML
    private Label dispersion;
    @FXML
    private ProgressBar complexityProgressBar;
    @FXML
    private Label overlay;
    @FXML
    private Label vectorParamLabel;
    @FXML
    private Label identUserName;
    ;

    private String password;
    private List<Long> passwordDynamic;
    private List<Long> passwordEntrySpeed;
    private XYChart.Series barChartSeries = new XYChart.Series();
    private XYChart.Series lineChartSeries = new XYChart.Series();
    private PasswordService passwordService;
    private List<KeyEntity> allKeyPressed;
    private int counter = 0;
    private double[] vector;


    @FXML
    private void initialize() {
        barChart.getData().add(barChartSeries);
        lineChart.getData().add(lineChartSeries);
        passwordDynamic = new ArrayList();
        passwordEntrySpeed = new ArrayList();
        passwordService = new PasswordService();
        allKeyPressed = new ArrayList<>();
    }


    public void onKeyReleased(KeyEvent keyEvent) {
        KeyEntity k = passwordService.getKeyEntity(keyEvent.getText());
        k.setUpTime(new Date().getTime());
        allKeyPressed.add(k);
        if (passwordService.clampedKeysSize() == 1 && passwordService.isOverlay()) {
            passwordService.checkOverlay(keyEvent.getText());
            overlay.setText(passwordService.getOverlayStatistic());

        }
        passwordService.clearClampedKeys(keyEvent.getText());
        password = pswField.getText();
        passwordEntrySpeed.add(k.getUpTime() - k.getDownTime());
        showComplexity(passwordComplexity(password));
        expectation.setText(String.valueOf(PasswordUtils.expectation(passwordEntrySpeed)));
        dispersion.setText(String.valueOf(PasswordUtils.dispersion(passwordEntrySpeed)));
        barChartSeries.getData().add(new XYChart.Data(counter + " " + keyEvent.getText(), passwordEntrySpeed.get(counter)));
        holdTime.setText(passwordEntrySpeed.stream().mapToLong(Long::longValue).sum() + " мс");
        counter++;

    }

    public void onKeyPressed(KeyEvent keyEvent) {
        long downTime = new Date().getTime();
        KeyEntity k = new KeyEntity(keyEvent.getText(), downTime);
        passwordService.addClampedKey(keyEvent.getText(), k);
        if (passwordService.clampedKeysSize() > 1) {
            passwordService.setOverlay(true);
        }
        passwordDynamic.add(downTime);
        if (passwordDynamic.size() > 1) {
            lineChartSeries.getData().add(new XYChart.Data(String.valueOf(counter), passwordDynamic.get(counter) - passwordDynamic.get(counter - 1)));
        }
    }

    private void showComplexity(int complexity) {
        String style = "";
        switch (complexityMap.get(complexityMap.floorKey(complexity))) {
            case LOW:
                style = "-fx-accent: red";
                break;
            case MEDIUM:
                style = "-fx-accent: orange";
                break;
            case HARD:
                style = "-fx-accent: green";
                break;
        }
        complexityProgressBar.setStyle(style);
        complexityProgressBar.setProgress(Double.valueOf(passwordComplexity(password)) / MAX_PSWD_COMPLEXITY);
    }

    public void onMousePressedClearBtn(MouseEvent mouseEvent) {
        pswField.setText("");
        barChartSeries.getData().clear();
        lineChartSeries.getData().clear();
        counter = 0;
        expectation.setText("");
        dispersion.setText("");
        complexityProgressBar.setProgress(0);
        holdTime.setText("");
        overlay.setText("");
        passwordEntrySpeed.clear();
        passwordDynamic.clear();
        vectorParamLabel.setText("");
        allKeyPressed.clear();
        identUserName.setText("");

    }

    public void showVector(ActionEvent actionEvent) {
        vector = passwordService.calculateVector(allKeyPressed);
        vectorParamLabel.setText(Arrays.toString(vector));
    }

    public void saveUser(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/user.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setScene(
                new Scene(
                        (Pane) loader.load()
                )
        );

        UserController controller =
                loader.getController();
        controller.initialize(vector, password);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    public void identification(ActionEvent actionEvent) {
        identUserName.setText("Имя пользователя : " + passwordService.userIdentification(vector));
    }

    public void verification(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/verification.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setScene(
                new Scene(
                        (Pane) loader.load()
                )
        );

        VerificationController controller =
                loader.getController();
        controller.initialize(vector,password);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

}
