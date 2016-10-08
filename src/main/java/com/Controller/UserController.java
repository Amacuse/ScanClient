package com.Controller;

import com.Bean.User;
import com.Service.Impl.UserServiceImpl;
import com.Service.Interface.UserService;
import com.SpringFxmlLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(UserController.class);
    private static final int MAX_AGE = 90;
    private static final int NAME_LENGTH_MAX = 15;
    private static final int PASSWORD_LENGTH_MIN = 6;
    private static final int PASSWORD_LENGTH_MAX = 15;

    private User user = SpringFxmlLoader.getContext().getBean("user", User.class);
    private UserService service = SpringFxmlLoader.getContext().getBean("userServiceImpl", UserServiceImpl.class);
    private MessageSource ms = SpringFxmlLoader.getContext().getBean("messageSource", MessageSource.class);

    @FXML
    private Label lbName;
    @FXML
    private Label lbEmail;
    @FXML
    private Label lbBirthday;
    @FXML
    private TextField txtName;
    @FXML
    private Label nameValidate;
    @FXML
    private TextField txtEmail;
    @FXML
    private Label emailValidate;
    @FXML
    private DatePicker txtBirthday;
    @FXML
    private Label dateValidate;
    @FXML
    private Label lbPassword;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label passwordValidate;
    @FXML
    private Label lbConfirmPassword;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private Label lbConfirmPasswordValid;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Label lbDBConnect;
    @FXML
    private Button ok;
    @FXML
    private Button cancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //for i18n
        changeWindowLanguage();
    }

    /**
     * The button for save a user
     */
    public void saveUser() {
        boolean validate = true;
        //get locale for i18n
        Locale currentLocale = getLocale();

        String name = txtName.getText().trim();
        LOGGER.debug("User enters name: " + name);
        //only letters and numbers are available
        if (name.isEmpty() || !name.matches("[\\w]+") || name.length() > NAME_LENGTH_MAX) {
            validate = false;
            nameValidate.setText(ms.getMessage("userController.label.nameValidate", null, currentLocale));
        } else {
            nameValidate.setText("");
        }

        String email = txtEmail.getText().trim();
        LOGGER.debug("User enters email: " + email);
        //validate for an appropriate user email
        if (email.isEmpty() || !email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*" +
                "@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            validate = false;
            emailValidate.setText(ms.getMessage("userController.label.emailValidate", null, currentLocale));
        } else {
            emailValidate.setText("");
        }

        //get a date from the date picker
        java.time.LocalDate value = txtBirthday.getValue();
        LocalDate birthday = null;
        if (value == null) {
            validate = false;
            dateValidate.setText(ms.getMessage("userController.label.birthdayEmpty", null, currentLocale));
        } else {
            String localDate = value.toString();
            LOGGER.debug("User enters birthday: " + localDate);
            birthday = LocalDate.parse(localDate);
            //a users older then '80' don't need this staff
            if ((LocalDate.now().getYear() - birthday.getYear()) > MAX_AGE) {
                validate = false;
                dateValidate.setText(ms.getMessage("userController.label.birthdayValidate", null, currentLocale));
            } else {
                dateValidate.setText("");
            }
        }

        //get a password from the password field
        String password = txtPassword.getText();
        LOGGER.debug("User enters password: " + password);
        if (password.isEmpty() || password.length() < PASSWORD_LENGTH_MIN ||
                password.length() > PASSWORD_LENGTH_MAX) {
            validate = false;
            passwordValidate.setText(ms.getMessage("userController.label.passwordValid", null, currentLocale));
        } else {
            passwordValidate.setText("");
        }

        //confirm the password
        String confirmPassword = txtConfirmPassword.getText();
        LOGGER.debug("User enters confirm: " + confirmPassword);
        if (!confirmPassword.equals(password)) {
            validate = false;
            lbConfirmPasswordValid.setText(ms.getMessage("userController.label.passwordConfirmValid",
                    null, currentLocale));
        } else {
            lbConfirmPasswordValid.setText("");
        }

        //if everything is ok send the request to the server
        if (validate) {
            lbDBConnect.setVisible(true);
            progress.setVisible(true);

            user.setName(name);
            user.setEmail(email);
            user.setBirthday(birthday);
            user.setPassword(password.toCharArray());

            boolean confirmation = service.saveUser(user);

            lbDBConnect.setVisible(false);
            progress.setVisible(false);

            //if there are some problems, whether the server was unavailable
            // or a user chose the email which has already been on the server,
            //don't close the window
            if (confirmation) {
                cancel();
            }
        }
    }

    /**
     * Change the window language
     */
    public void changeWindowLanguage() {
        Locale currentLocale = getLocale();

        //User settings labels
        lbName.setText(ms.getMessage("userController.label.name", null, currentLocale));
        lbEmail.setText(ms.getMessage("userController.label.email", null, currentLocale));
        lbBirthday.setText(ms.getMessage("userController.label.birthday", null, currentLocale));
        lbPassword.setText(ms.getMessage("userController.label.password", null, currentLocale));
        lbConfirmPassword.setText(ms.getMessage("userController.label.confirmPassword", null, currentLocale));

    }

    /**
     * Retrieve a locale from the context
     */
    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Close the window
     */
    public void cancel() {
        ((Stage) cancel.getScene().getWindow()).close();
    }

}

