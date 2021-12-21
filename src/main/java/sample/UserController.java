package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import static sample.DBConstants.*;
import static sample.UserQueries.UPDATE_USER_QUERY;

public class UserController {
    @FXML
    private TextField name;
    @FXML
    private TextField email;

    @FXML
    private Button save;

    private double[] vector;
    private String password;



    public void initialize(double[] vector,String password) {
        this.vector = vector;
        this.password = password;
    }

    public void saveUser(ActionEvent actionEvent) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_QUERY)) {
            stmt.setString(1,name.getText());
            stmt.setString(2,BCrypt.hashpw(password,BCrypt.gensalt()));
            stmt.setString(3,Arrays.toString(vector));
            stmt.executeUpdate();

        }
        ((Stage)save.getScene().getWindow()).close();
    }
}
