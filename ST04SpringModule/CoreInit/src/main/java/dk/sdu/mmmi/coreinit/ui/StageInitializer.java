package dk.sdu.mmmi.coreinit.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<ProductionApplication.StageReadyEvent> {
    @Value("classpath:/core_ui.fxml")
    private Resource coreUi;

    @Override
    public void onApplicationEvent(ProductionApplication.StageReadyEvent event){
        try {
            System.out.println("Initializing stage");
            FXMLLoader fxmlLoader = new FXMLLoader(coreUi.getURL());
            Parent parent = fxmlLoader.load();

            Stage stage = event.getStage();
            stage.setScene(new Scene(parent));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
