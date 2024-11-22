package dev.jackraidenph.nliis.frontend.view;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@Component
@RequiredArgsConstructor
public class CommonView implements View {

    private final RetrievalView retrievalView;
    private final LanguageDetectionView languageDetectionView;
    private final SummaryView summaryView;
    private final TranslationView translationView;
    private final SpeechView speechView;
    private Region rootRegion;

    @Override
    public Region getRootRegion() {
        if (this.rootRegion == null) {
            this.rootRegion = this.build();
        }
        return this.rootRegion;
    }

    private Region build() {
        TabPane tabPane = new TabPane();

        tabPane.setTabDragPolicy(TabDragPolicy.FIXED);
        tabPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        Map<String, Node> tabs = new LinkedHashMap<>();

        tabs.put("Search", this.retrievalView.getRootRegion());
        tabs.put("Detect Language", this.languageDetectionView.getRootRegion());
        tabs.put("Summary", this.summaryView.getRootRegion());
        tabs.put("Translation", this.translationView.getRootRegion());
        tabs.put("Speech", this.speechView.getRootRegion());

        for (Entry<String, Node> titledNode : tabs.entrySet()) {
            Tab tab = new Tab(titledNode.getKey(), titledNode.getValue());
            tab.setClosable(false);
            tabPane.getTabs().add(tab);
        }

        AnchorPane.setTopAnchor(tabPane, 0.);
        AnchorPane.setBottomAnchor(tabPane, 0.);
        AnchorPane.setLeftAnchor(tabPane, 0.);
        AnchorPane.setRightAnchor(tabPane, 0.);

        return new AnchorPane(tabPane);
    }
}
