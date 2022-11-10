/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chatapp.chatappgui;

import javafx.scene.control.ListCell;
import javafx.scene.text.Text;


/**
 *
 * @author Mariano
 */
public class RequestListCell extends ListCell<Friend> {


    private Text content;


    @Override
    protected void updateItem(Friend item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) { // <== test for null item and empty parameter
            
            Text text = new Text();
            text.setText(item.getUsername());
            content = text;
            setGraphic(content);
        } else {
            setGraphic(null);
        }
    }
}
