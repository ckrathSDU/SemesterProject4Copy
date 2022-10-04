package dk.sdu.mmmi.coreinit.ui;

import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;
import dk.sdu.mmmi.coreinit.CoreInitApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

public class ProductionApplication extends Application {

    private static AnnotationConfigApplicationContext applicationContext;

    public static AnnotationConfigApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = (AnnotationConfigApplicationContext) new SpringApplicationBuilder(CoreInitApplication.class).run();
        }
        return applicationContext;
    }

    @Override
    public void init() {
        getApplicationContext().scan("dk.sdu.mmmi");
        AssetManager assetManager = getApplicationContext().getBean(AssetManager.class);

        for (Map.Entry<String, IAssetManagerPluginService> iAssetManagerPluginServiceEntry : getApplicationContext().getBeansOfType(IAssetManagerPluginService.class).entrySet()) {
            iAssetManagerPluginServiceEntry.getValue().start(assetManager);
        }

    }

    @Override
    public void stop() {
        getApplicationContext().close();
        Platform.exit();
    }

    @Override
    public void start(Stage primaryStage) {
        getApplicationContext().publishEvent(new StageReadyEvent(primaryStage));
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage primaryStage) {
            super(primaryStage);
        }

        public Stage getStage(){
            return ((Stage) getSource());
        }
    }
}
