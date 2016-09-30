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
    private ProgressIndicator progress;
    @FXML
    private Label lbDBConnect;
    @FXML
    private Button ok;
    @FXML
    private Button cancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeWindowLanguage();
    }

    public void saveUser() {
        boolean validate = true;
        Locale currentLocale = getLocale();

        String name = txtName.getText().trim();
        LOGGER.debug("User enters name: " + name);
        if (name.isEmpty() || !name.matches("[\\w]+") || name.length() > 15) {
            validate = false;
            nameValidate.setText(ms.getMessage("userController.label.nameValidate", null, currentLocale));
        } else {
            nameValidate.setText("");
        }

        String email = txtEmail.getText().trim();
        LOGGER.debug("User enters email: " + email);
        if (email.isEmpty() || !email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*" +
                "@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            validate = false;
            emailValidate.setText(ms.getMessage("userController.label.emailValidate", null, currentLocale));
        } else {
            emailValidate.setText("");
        }

        java.time.LocalDate value = txtBirthday.getValue();
        LocalDate birthday = null;
        if (value == null) {
            validate = false;
            dateValidate.setText(ms.getMessage("userController.label.birthdayEmpty", null, currentLocale));
        } else {
            String localDate = value.toString();
            LOGGER.debug("User enters birthday: " + localDate);
            birthday = LocalDate.parse(localDate);
            if ((LocalDate.now().getYear() - birthday.getYear()) > 80) {
                validate = false;
                dateValidate.setText(ms.getMessage("userController.label.birthdayValidate", null, currentLocale));
            } else {
                dateValidate.setText("");
            }
        }

        if (validate) {
            lbDBConnect.setVisible(true);
            progress.setVisible(true);

            user.setName(name);
            user.setEmail(email);
            user.setBirthday(birthday);


            boolean confirmation = service.saveUser(user);

            lbDBConnect.setVisible(false);
            progress.setVisible(false);

            if (confirmation) {
                cancel();
            }
        }
    }

    public void changeWindowLanguage() {
        Locale currentLocale = getLocale();

        //User settings labels
        lbName.setText(ms.getMessage("userController.label.name", null, currentLocale));
        lbEmail.setText(ms.getMessage("userController.label.email", null, currentLocale));
        lbBirthday.setText(ms.getMessage("userController.label.birthday", null, currentLocale));
    }

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public void cancel() {
        ((Stage) cancel.getScene().getWindow()).close();
    }

}

