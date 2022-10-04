package dk.sdu.mmmi.coreinit;

import dk.sdu.mmmi.coreinit.ui.ProductionApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoreInitApplication {

    public static void main(String[] args) {

        Application.launch(ProductionApplication.class, args);

    }

}
