package dev.jackraidenph.nliis.frontend.utility;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class JavaFXCommonComponents {

    public static <T> ListView<T> createGenericStringListView(
            Function<T, String> textFunction,
            String emptyPlaceholder,
            boolean focusTraversable,
            boolean mouseTransparent,
            boolean multiselect
    ) {
        ListView<T> listView = new ListView<>();

        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                String text = empty ? emptyPlaceholder : (item == null ? emptyPlaceholder : textFunction.apply(item));
                setText(text);
            }
        });

        if (multiselect) {
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        listView.setFocusTraversable(focusTraversable);
        listView.setMouseTransparent(mouseTransparent);

        return listView;
    }

    public static <T> ListView<T> createGenericStringListView(
            Function<T, String> textFunction,
            boolean focusTraversable,
            boolean mouseTransparent,
            boolean multiselect
    ) {
        return createGenericStringListView(textFunction, "", focusTraversable, mouseTransparent, multiselect);
    }

    public static <T> ListView<T> createGenericStringListView(
            Function<T, String> textFunction,
            String emptyPlaceholder,
            boolean multiselect
    ) {
        return createGenericStringListView(textFunction, emptyPlaceholder, true, false, multiselect);
    }

    public static <T> ListView<T> createGenericStringListView(Function<T, String> textFunction, boolean multiselect) {
        return createGenericStringListView(textFunction, "", multiselect);
    }

    public static <T> ListView<T> createGenericStringListView() {
        return createGenericStringListView(Object::toString, "", false);
    }

    public static <T> TableView<T> createGenericStringTableView(
            Map<String, Function<T, String>> columnTextFunctions,
            String emptyPlaceholder,
            boolean editable,
            boolean reorderable,
            boolean sortable,
            boolean resizable,
            Callback<ResizeFeatures, Boolean> resizePolicy,
            BiConsumer<TableRow<T>, MouseEvent> onRowClick
    ) {
        TableView<T> tableView = new TableView<>();
        tableView.setEditable(editable);
        tableView.setEditable(editable);

        tableView.setColumnResizePolicy(resizePolicy);

        tableView.setRowFactory(view -> {
            TableRow<T> tableRow = new TableRow<>();
            if (onRowClick != null) {
                tableRow.setOnMouseClicked(mouseEvent -> onRowClick.accept(tableRow, mouseEvent));
            }
            return tableRow;
        });

        for (Entry<String, Function<T, String>> columnFactory : columnTextFunctions.entrySet()) {
            TableColumn<T, String> column = new TableColumn<>();
            column.setReorderable(reorderable);
            column.setSortable(sortable);
            column.setResizable(resizable);
            column.setText(columnFactory.getKey());
            column.setCellValueFactory(p -> {
                T pValue = p.getValue();
                if (pValue == null) {
                    return new SimpleObjectProperty<>();
                }
                String mappedValue = columnFactory.getValue().apply(pValue);

                return new SimpleStringProperty(
                        (mappedValue == null || mappedValue.isBlank()) ? emptyPlaceholder : mappedValue
                );
            });

            tableView.getColumns().add(column);
        }

        return tableView;
    }

    public static <T> TableView<T> createGenericStringTableView(
            Map<String, Function<T, String>> columnTextFunctions,
            String emptyPlaceholder,
            boolean editable,
            boolean reorderable,
            boolean sortable,
            boolean resizable,
            Callback<ResizeFeatures, Boolean> resizePolicy
    ) {
        return createGenericStringTableView(
                columnTextFunctions,
                emptyPlaceholder,
                editable,
                reorderable,
                sortable,
                resizable,
                resizePolicy,
                null
        );
    }

    public static <T> TableView<T> createGenericStringTableView(
            Map<String, Function<T, String>> columnTextFunctions,
            String emptyPlaceholder,
            boolean editable,
            boolean reorderable,
            boolean sortable,
            boolean resizable
    ) {
        return createGenericStringTableView(
                columnTextFunctions,
                emptyPlaceholder,
                editable,
                reorderable,
                sortable,
                resizable,
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS,
                null
        );
    }

    public static <T> TableView<T> createGenericStringTableView(
            Map<String, Function<T, String>> columnTextFunctions,
            boolean editable,
            boolean reorderable,
            boolean sortable,
            boolean resizable
    ) {
        return createGenericStringTableView(
                columnTextFunctions,
                "",
                editable,
                reorderable,
                sortable,
                resizable,
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS,
                null
        );
    }

    public static <T> TableView<T> createGenericStringTableView(
            Map<String, Function<T, String>> columnTextFunctions,
            boolean editable,
            boolean sortable,
            boolean resizable,
            BiConsumer<TableRow<T>, MouseEvent> onRowClick
    ) {
        return createGenericStringTableView(
                columnTextFunctions,
                "",
                editable,
                false,
                sortable,
                resizable,
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS,
                onRowClick
        );
    }

    public static <T> TableView<T> createGenericStringTableView(
            Map<String, Function<T, String>> columnTextFunctions,
            boolean editable,
            boolean sortable,
            boolean resizable
    ) {
        return createGenericStringTableView(
                columnTextFunctions,
                "",
                editable,
                false,
                sortable,
                resizable,
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS,
                null
        );
    }

    public static <T> TableView<T> createGenericStringTableView(
            Iterable<String> toStringTextColumns,
            boolean editable,
            boolean sortable,
            boolean resizable
    ) {
        Map<String, Function<T, String>> toStringFactoryMap = new LinkedHashMap<>();

        for (String columnName : toStringTextColumns) {
            toStringFactoryMap.put(columnName, Objects::toString);
        }

        return createGenericStringTableView(
                toStringFactoryMap,
                "",
                editable,
                false,
                sortable,
                resizable,
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS,
                null
        );
    }

    public static Button createGenericButton(
            String text,
            Consumer<ActionEvent> action
    ) {
        Button button = new Button();
        button.setText(text);
        button.setOnAction(action::accept);
        return button;
    }

    public static AnchorPane createGenericColumnarLayout(
            Node[][] nodes,
            int spacing,
            int padding
    ) {
        HBox horizontalContainer = new HBox();
        horizontalContainer.setSpacing(spacing);
        horizontalContainer.setPadding(new Insets(padding));

        for (Node[] column : nodes) {
            VBox vBoxColumn = new VBox();
            vBoxColumn.setSpacing(spacing);
            vBoxColumn.getChildren().addAll(column);
            for (Node node : column) {
                VBox.setVgrow(node, Priority.ALWAYS);
            }
            horizontalContainer.getChildren().add(vBoxColumn);
            HBox.setHgrow(vBoxColumn, Priority.ALWAYS);
        }

        AnchorPane.setTopAnchor(horizontalContainer, 0.);
        AnchorPane.setBottomAnchor(horizontalContainer, 0.);
        AnchorPane.setRightAnchor(horizontalContainer, 0.);
        AnchorPane.setLeftAnchor(horizontalContainer, 0.);

        return new AnchorPane(horizontalContainer);
    }

    public static TextField createGenericTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        return textField;
    }
}
