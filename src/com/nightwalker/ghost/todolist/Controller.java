package com.nightwalker.ghost.todolist;

import com.nightwalker.ghost.todolist.datamodel.TodoData;
import com.nightwalker.ghost.todolist.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    private List<TodoItem> todoItems;
    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private Label deadLineLabel;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodayItems;

    public void initialize(){
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);

            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);
//        TodoItem item1 = new TodoItem("Mail Birthday card",
//                "Buy a 30th birthday card for John",
//                LocalDate.of(2021, Month.JUNE, 25));
//
//
//        TodoItem item2 = new TodoItem("Java Masterclass",
//                "finish java course",
//                LocalDate.of(2021, Month.AUGUST, 25));
//
//
//        TodoItem item3 = new TodoItem("Ife Birthday",
//                "Ife's birthday is coming up next month, get ready!",
//                LocalDate.of(2021, Month.SEPTEMBER, 25));
//
//
//        TodoItem item4 = new TodoItem("Python crash course",
//                "learn Django",
//                LocalDate.of(2021, Month.JULY, 10));
//
//        TodoItem item5 = new TodoItem("Node master course",
//                "learn websockets",
//                LocalDate.of(2021, Month.OCTOBER, 20));
//        todoItems = new ArrayList<TodoItem>();
//        todoItems.add(item1);
//        todoItems.add(item2);
//        todoItems.add(item3);
//        todoItems.add(item4);
//        todoItems.add(item5);
//        TodoData.getInstance().setTodoItems(todoItems);

        todoListView.getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<TodoItem>() {
                    @Override
                    public void changed(ObservableValue<? extends TodoItem>
                                                observableValue, TodoItem oldValue,
                                        TodoItem newValue) {
                        if(newValue != null){
                            TodoItem item = todoListView
                                    .getSelectionModel().getSelectedItem();

                            itemDetailsTextArea.setText(item.getDetails());
                            DateTimeFormatter df =
                                    DateTimeFormatter.ofPattern("MMM d, yyyy");
                            deadLineLabel.setText(df.format(item.getDeadline()));
                        }

                    }
                }
        );

//        todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
        wantAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        };

        wantTodayItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return (todoItem.getDeadline().equals(LocalDate.now()));
            }
        };
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);
        SortedList<TodoItem> sortedList =
                new SortedList<TodoItem>(filteredList,
                        new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });
//        todoListView.setItems(TodoData.getInstance().getTodoItems());
        todoListView.setItems(sortedList);

        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();
        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>(){
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            } else if(item.getDeadline()
                                    .equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty)->{
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }

                );

                return cell;
            }
        });



    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new Todo Item");
        dialog.setHeaderText("Use this dialog to create a new to do Item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass()
                .getResource("todoItemDialog.fxml"));
        try{

       dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch(IOException e){
            System.out.println("Couldn't load the dialog.");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
           TodoItem newItem = controller.processResults();
//            todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
            todoListView.getSelectionModel().select(newItem);
            System.out.println("OK pressed");
        } else {
            System.out.println("Canceled pressed");
        }




    }







    @FXML
    public void handleClickListView(){
        TodoItem item = todoListView.getSelectionModel()
                        .getSelectedItem();

        itemDetailsTextArea.setText(item.getDetails());
        deadLineLabel.setText(item.getDeadline().toString());

//        StringBuilder sb = new StringBuilder(item.getDetails());
//        sb.append("\n\n\n\n");
//        sb.append("Due: ");
//        sb.append(item.getDeadline().toString());


//        itemDetailsTextArea.setText(sb.toString());


    }
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }

    }

    public void deleteItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm," +
                " or CANCEL to back out");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && (result.get()== ButtonType.OK)){
            TodoData.getInstance().deleteTodoItem(item);
        }


    }

    public void handleFilterButton(){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodayItems);
            if(filteredList.isEmpty()){
                itemDetailsTextArea.clear();
                deadLineLabel.setText("");

            } else if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);

            }else {
                todoListView.getSelectionModel().selectFirst();
            }


        } else {
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);

        }
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

}
