/**
 * @author Wangshu Fu
 * @studentID 1112531
 */
package Server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class ServerGUI{

    protected static JTextArea log = new JTextArea(10, 40);
    private static final String[] title = {"Word", "Meaning"};
    private static final DefaultTableModel tableModel = new DefaultTableModel(getDict(), title);
    private static final JTable dict = new JTable(tableModel);

    public ServerGUI(){
        JFrame jFrame = new JFrame("Server Manager");
        jFrame.setSize(400, 200);
        dict.setAutoCreateRowSorter(true);
        dict.setUpdateSelectionOnSort(true);

        Box box1 = Box.createHorizontalBox();
        Box box2 = Box.createVerticalBox();
        Box box3 = Box.createVerticalBox();
        Box box4 = Box.createVerticalBox();

        jFrame.add(box1);
        box1.add(box2);
        box1.add(box3);
        box1.add(box4);

        box2.add(new JLabel("Log:"));
        box4.add(new JLabel("Current dictionary"));
        JScrollPane dictScroll = new JScrollPane(dict);
        JScrollPane logScroll = new JScrollPane(log);
        box2.add(logScroll);
        box4.add(dictScroll);

        JButton quit = new JButton("Quit");
        quit.addActionListener(e -> Server.quit());

        box3.add(Box.createRigidArea(new Dimension(20, 20)));
        box3.add(Box.createRigidArea(new Dimension(20, 20)));
        box3.add(quit, BorderLayout.CENTER);

        jFrame.setBounds(300,200,600,300);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static String[][] getDict(){
        //Transfer HashMap to 2-d list
        String[][] d = new String[Server.dictionary.size()][2];
        for (int i = 0; i < Server.dictionary.size(); i++){
            String word = (String) Server.dictionary.keySet().toArray()[i];
            d[i][0] = word;
            d[i][1] = Server.dictionary.get(word);
        }
        return d;
    }

    protected static void refresh(){
        //Refresh the dictionary.
        tableModel.setDataVector(getDict(), title);
    }
}
