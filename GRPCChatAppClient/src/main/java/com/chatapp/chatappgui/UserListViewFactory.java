package com.chatapp.chatappgui;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
//package com.mariano.chatapp.chatappgui;
//
///**
// *
// * @author Mariano
// */
//public class UserListViewFactory {
//    
//    private static class CustomThing {
//        private String name;
//        private int price;
//        public String getName() {
//            return name;
//        }
//        public int getPrice() {
//            return price;
//        }
//        public CustomThing(String name, int price) {
//            super();
//            this.name = name;
//            this.price = price;
//        }
//    }
//
//
//    @Override
//    public void start(Stage primaryStage) {
//        ObservableList<CustomThing> data = FXCollections.observableArrayList();
//        data.addAll(new CustomThing("Cheese", 123), new CustomThing("Horse", 456), new CustomThing("Jam", 789));
//
//        final ListView<CustomThing> listView = new ListView<CustomThing>(data);
//        listView.setCellFactory(new Callback<ListView<CustomThing>, ListCell<CustomThing>>() {
//            @Override
//            public ListCell<CustomThing> call(ListView<CustomThing> listView) {
//                return new CustomListCell();
//            }
//        });
//
//        StackPane root = new StackPane();
//        root.getChildren().add(listView);
//        primaryStage.setScene(new Scene(root, 200, 250));
//        primaryStage.show();
//    }
//
//    private class CustomListCell extends ListCell<CustomThing> {
//        private HBox content;
//        private Text name;
//        private Text price;
//
//        public CustomListCell() {
//            super();
//            name = new Text();
//            price = new Text();
//            VBox vBox = new VBox(name, price);
//            content = new HBox(new Label("[Graphic]"), vBox);
//            content.setSpacing(10);
//        }
//
//        @Override
//        protected void updateItem(CustomThing item, boolean empty) {
//            super.updateItem(item, empty);
//            if (item != null && !empty) { // <== test for null item and empty parameter
//                name.setText(item.getName());
//                price.setText(String.format("%d $", item.getPrice()));
//                setGraphic(content);
//            } else {
//                setGraphic(null);
//            }
//        }
//    }
//
//}
