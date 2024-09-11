import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class MainWindow extends JFrame {

    String[] buttonLabels = new String[] {"⋀", "⋁", "¬","→","↔","⊕", "↑"};
    JTextField equationField;

    public String readCache() {
        try {
            File file = new File("equation.cache");
            Scanner stdin = new Scanner(file);
            String data = "";
            if (stdin.hasNext()) data = stdin.nextLine();
            stdin.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeCache(String data) {
        try {
            PrintWriter printWriter = new PrintWriter("equation.cache");
            printWriter.print("");
            printWriter.close();

            FileWriter fileWriter = new FileWriter("equation.cache");
            fileWriter.write(data);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainWindow() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);
        setTitle("");
        setLocationRelativeTo(null);

        ButtonArray inputs = new ButtonArray(this, 4,4, buttonLabels);
        add(inputs, BorderLayout.WEST);

        TruthTable dataOut = new TruthTable(this);

        equationField = new JTextField();
        equationField.setFont(new Font("Ariel", 0, 40));
        equationField.setMargin(new Insets(10,10,10,10));
        equationField.addActionListener(e -> {
            if (!equationField.getText().isEmpty()) {
                try {
                    writeCache(equationField.getText());
                    dataOut.calculate(equationField.getText());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

        });

        equationField.setText(readCache());
        add(equationField, BorderLayout.NORTH);
        setVisible(true);
    }


    public static void main(String[] args) {
        MainWindow window = new MainWindow();
    }


}