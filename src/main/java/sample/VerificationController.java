package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import org.json.JSONArray;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Arrays;

import static sample.DBConstants.*;
import static sample.UserQueries.SELECT_ALL_USERS;
import static sample.UserQueries.SELECT_USER_BY_ID;


public class VerificationController {
    @FXML
    public PasswordField password;
    @FXML
    public TextField id;
    @FXML
    public Label result;
    private double[] vector;
    private String passwd;
    private static final String SUCCESSFULLY = "УСПЕШНО";
    private static final String UNSUCCESSFULLY = "НЕУДАЧА";

    public void initialize(double[] vector, String password) {
        this.vector = vector;
        this.passwd = password;
    }

    public void verification(ActionEvent actionEvent) {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_BY_ID)) {
            stmt.setInt(1,Integer.parseInt(id.getText()));
            ResultSet set = stmt.executeQuery();
            if (!set.next()) {
                showResult(UNSUCCESSFULLY);
                return;
            }
            JSONArray dbVector = new JSONArray(set.getArray("vector").toString());
            if (set.getString("id").equals(id.getText()) &&
                    BCrypt.checkpw(passwd, set.getString("password")) &&
                    PasswordService.compareVector(dbVector, vector)) {
                showResult(SUCCESSFULLY);
            } else {
                showResult(UNSUCCESSFULLY);
            }

        } catch (SQLException throwables) {
            System.out.println(throwables.fillInStackTrace());
        }
    }

    private void showResult(String s) {
        switch (s) {
            case SUCCESSFULLY:
                result.setTextFill(Color.GREEN);
                break;
            default:
                result.setTextFill(Color.RED);
                break;
        }
        result.setText(s);
    }
}
