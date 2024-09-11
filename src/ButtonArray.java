import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonArray extends JPanel {

    JButton[][] buttons;
    String[] labels;

    public ButtonArray(MainWindow parent, int width, int height, String[] buttonLabels) {
        buttons = new JButton[height][width];
        labels = buttonLabels;
        int id;

        setFont(new Font("Ariel", 0, 40));
        setLayout(new GridLayout(width, height));

        for (int y=0; y<height; y++) {
            for (int x = 0; x < width; x++) {
                JButton temp = new JButton();
                temp.setFont(getFont());
                buttons[y][x] = temp;
                if ((id = x+y*width)<labels.length) temp.setText(labels[id]);
                temp.addActionListener(e -> {
                    int index = parent.equationField.getCaretPosition();
                    parent.equationField.setText(parent.equationField.getText().substring(0,index)+temp.getText()+
                            parent.equationField.getText().substring(index));
                    parent.equationField.grabFocus(); //prevent having to re-click the text box
                    parent.equationField.setCaretPosition(index+1);
                });
                add(temp);

            }
        }
    }
}
