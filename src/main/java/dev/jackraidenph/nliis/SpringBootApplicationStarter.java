package dev.jackraidenph.nliis;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootApplicationStarter {
    public static void main(String[] args) {
        Application.launch(ApplicationInit.class, args);
    }
}
