package com.chatapp.chatappgui.javafxelements;

import com.chatapp.dataobjects.Friend;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;

/**
 * Custom JavaFX <code>ListCell</code> node for displaying the user's friend
 * requests.
 *
 * @author Mariano Cuneo
 */
public class RequestListCell extends ListCell<Friend> {

    private Text content;

    /**
     * Updates the contents of the <code>ListCell</code>.
     *
     * @param item   <code>Friend</code> to be displayed.
     * @param empty boolean stating if the cell is empty.
     */
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
