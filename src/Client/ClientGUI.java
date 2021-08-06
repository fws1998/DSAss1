/**
 * @author Wangshu Fu
 * @studentID 1112531
 */
package Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.JSONObject;

public class ClientGUI {

    private static String host;
    private static int port;
    //Word input text
    private final static JTextArea jTextArea = new JTextArea(5, 5);
    //Meaning input text
    private final static JTextField jTextField = new JTextField(5);

    public ClientGUI() {
        JFrame jFrame = new JFrame("Online dictionary");
        Box hbox1 = Box.createHorizontalBox();
        Box hbox2 = Box.createVerticalBox();
        Box hbox3 = Box.createVerticalBox();
        Box hbox4 = Box.createVerticalBox();
        jFrame.add(hbox1);
        hbox1.add(hbox4);

        JButton find = new JButton("Find");
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Remove");
        JButton help = new JButton("Help");
        JButton quit = new JButton("Quit");

        find.addActionListener(e -> find());
        add.addActionListener(e -> add());
        update.addActionListener(e -> update());
        delete.addActionListener(e -> remove());
        help.addActionListener(e -> help());
        quit.addActionListener(e -> System.exit(0));

        hbox4.add(find);
        hbox4.add(add);
        hbox4.add(update);
        hbox4.add(delete);
        hbox4.add(help);
        hbox4.add(quit);

        hbox1.add(hbox2);
        hbox2.add(new JLabel("word"));
        hbox2.add(Box.createRigidArea(new Dimension(20, 80)));
        hbox2.add(new JLabel("meaning"));
        hbox2.add(Box.createRigidArea(new Dimension(20, 20)));

        hbox1.add(Box.createRigidArea(new Dimension(20, 20)));
        hbox1.add(hbox3);
        hbox3.add(Box.createRigidArea(new Dimension(20, 60)));
        hbox3.add(new JScrollPane(jTextField));
        hbox3.add(Box.createRigidArea(new Dimension(20, 30)));
        jTextArea.setLineWrap(true);
        hbox3.add(new JScrollPane(jTextArea));
        hbox3.add(Box.createRigidArea(new Dimension(20, 40)));

        jFrame.setBounds(400,400,450,300);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void find(){
        //Search for a word
        String text = ClientGUI.jTextField.getText();
        if(text.length() == 0){
            nullWord();
        }else {
            String[] feedback = communicate(1);
            if(feedback[0].equals("0")){
                jTextArea.setText(feedback[1]);
            }else if (feedback[0].equals("1")){
                JOptionPane.showMessageDialog(jTextField, "Cannot find the word "+ text, "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void add(){
        //Add a new word
        String newWord = ClientGUI.jTextField.getText();
        String mean = ClientGUI.jTextArea.getText();
        if (newWord.length() == 0){
            nullWord();
        }else if (mean.length() == 0){
            nullMean();
        }else {
            String[] feedback = communicate(2);
            if(feedback[0].equals("0")){
                JOptionPane.showMessageDialog(jTextField, "Successfully added " + newWord +"!", "Succeed", JOptionPane.INFORMATION_MESSAGE);
            }else if (feedback[0].equals("1")){
                JOptionPane.showMessageDialog(jTextField, "The word "+ newWord + " already exist!", "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void remove(){
        //Delete an exist word
        String word = ClientGUI.jTextField.getText();
        if(word.length() == 0){
            nullWord();
        }else {
            String[] feedback = communicate(4);
            if(feedback[0].equals("0")){
                JOptionPane.showMessageDialog(ClientGUI.jTextField, "Successfully removed " + word +"!", "Succeed", JOptionPane.INFORMATION_MESSAGE);
            }else if (feedback[0].equals("1")){
                JOptionPane.showMessageDialog(ClientGUI.jTextField, "Cannot find the word "+ word, "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void update(){
        //Update an exist word
        String newWord = ClientGUI.jTextField.getText();
        String mean = ClientGUI.jTextArea.getText();
        if (newWord.length() == 0){
            nullWord();
        }else if (mean.length() == 0){
            nullMean();
        }else {
            String[] feedback = communicate(3);
            if(feedback[0].equals("0")){
                JOptionPane.showMessageDialog(jTextField, "Successfully updated the word " + newWord +"!", "Succeed", JOptionPane.INFORMATION_MESSAGE);
            }else if (feedback[0].equals("1")){
                JOptionPane.showMessageDialog(jTextField, "Cannot find the word "+ newWord, "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void help(){
        //Show help information
        JOptionPane.showMessageDialog(jTextField, """
                Press Enter to split multiple meanings.
                Use button to find, add, update or delete a word.
                Press "Quit" to exit.
                """, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String[] communicate(int code){
        //Communicate with server
        String result = null;
        Socket socket;
        for(int i = 0; i <=10; i++) {
            try {
                socket = new Socket(host, port);
            } catch (UnknownHostException e) {
                JOptionPane.showMessageDialog(jTextField, "Cannot resolve the host, please check the host or try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
                break;
            }catch (ConnectException e){
                JOptionPane.showMessageDialog(jTextField, "Cannot connect to server, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
                break;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(jTextField, "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
                break;
            }

            try {
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                JSONObject json = new JSONObject();
                String send = jTextField.getText() + "-" + jTextArea.getText();
                json.put("command", code);
                json.put("para", send);
                dataOutputStream.writeUTF(String.valueOf(json));

                JSONObject cache = new JSONObject(dataInputStream.readUTF());

                //Check it is the correct reply.
                if(cache.get("command").equals(code) && cache.getString("para").equals(send)){
                    String reply = cache.getString("reply");
                    if(reply.startsWith("1") || reply.startsWith("0")){
                        //If the reply data is illegal, depose it and try again.
                        result = reply;
                        break;
                    }
                }
                outputStream.close();
                inputStream.close();
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
                //low probability of IO error.
            } catch (IOException ignore) {
               //Keep trying
            }
        }
        //Avoid NullPointException.
        if (result == null){
            JOptionPane.showMessageDialog(new JLabel("error"), "Unknown error occurs, please try again later", "Something went wrong", JOptionPane.ERROR_MESSAGE);
            return new String[]{"error"};
        }else {
            return result.split("-");
        }
    }

    private static void nullWord(){
        //Error message for no word input
        JOptionPane.showMessageDialog(jTextField, "You must enter a word!", "Error!", JOptionPane.ERROR_MESSAGE);
    }

    private static void nullMean(){
        //Error message for no meaning input
        JOptionPane.showMessageDialog(jTextField, "You should enter the meaning of this word!", "Error!", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        //Check input parameters
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }catch (IndexOutOfBoundsException | NumberFormatException e){
            JOptionPane.showMessageDialog(jTextArea, "Illegal parameters, please check it again.", "Illegal parameters", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        if(port>65535 || port<1024){
            JOptionPane.showMessageDialog(jTextField, "You entered an illegal port number, please restart the client and try again!", "Illegal port number", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        new ClientGUI();
    }
}
