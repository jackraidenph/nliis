package dev.jackraidenph.nliis;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.speech.Central;
import javax.speech.EngineException;
import java.awt.*;

public class ApplicationInit extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void start(Stage primaryStage) throws EngineException {
        ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
            genericApplicationContext.registerBean(Application.class, () -> ApplicationInit.this);
            genericApplicationContext.registerBean(Parameters.class, this::getParameters);
            genericApplicationContext.registerBean(HostServices.class, this::getHostServices);
            genericApplicationContext.registerBean(Stage.class, () -> primaryStage);

            if (Desktop.isDesktopSupported()) {
                genericApplicationContext.registerBean(Desktop.class, Desktop::getDesktop);
            }
        };

        System.setProperty(
                "freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
        );

        Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");

        this.context = new SpringApplicationBuilder().sources(SpringBootApplicationStarter.class)
                .initializers(initializer)
                .web(WebApplicationType.NONE)
                .build()
                .run(getParameters().getRaw().toArray(new String[0]));

        this.context.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }

    public static class StageReadyEvent extends ApplicationEvent {

        public Stage getStage() {
            return (Stage) getSource();
        }

        public StageReadyEvent(Object source) {
            super(source);
        }
    }
}