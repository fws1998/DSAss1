/**
 * @author Wangshu Fu
 * @studentID 1112531
 */
package Server;

import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

   private static String FILE_PATH;
   private static final String BACKUP = "data";
   protected static HashMap<String, String> dictionary;

    public static void main(String[] args) {
        int port = 0;
        //Check input parameters
        try {
            port = Integer.parseInt(args[0]);
            FILE_PATH = args[1];
        }catch (IndexOutOfBoundsException | NumberFormatException e){
            JOptionPane.showMessageDialog(new JLabel("error"), "Illegal parameters, please check it again.", "Illegal parameters", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        if(port>65535 || port<1024){
            JOptionPane.showMessageDialog(new JLabel("error"), "You entered an illegal port number, please restart the application and try again!", "Illegal port number", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        //Read file and create GUI
        readDict();
        new ServerGUI();

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try {
            ServerSocket socket = factory.createServerSocket(port);
            while (true){
                Socket connection = socket.accept();
                ServerThread serverThread = new ServerThread(connection);
                serverThread.start();
                writeDict();
            }
        }catch (BindException e){
            JOptionPane.showMessageDialog(new JLabel("error"), "The port has been occupied, please try another one", "Port occupied", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException ignore){}
    }

    private static void readDict() {
        //Read from file
        try {
            File file = new File(FILE_PATH);
            FileInputStream fileInputStream;
            if(!file.exists()){
                //If cannot find the file, use backup file
                JOptionPane.showMessageDialog(new JLabel("error"), "Cannot find the file, the application will start with the backup dictionary!", "Error", JOptionPane.ERROR_MESSAGE);
                fileInputStream = new FileInputStream(BACKUP);
            }else {
                fileInputStream = new FileInputStream(FILE_PATH);
            }
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            dictionary = (HashMap<String, String>) objectInputStream.readObject();
        }catch (IOException| ClassNotFoundException e){
            JOptionPane.showMessageDialog(new JLabel("error"), "Cannot read the file, the application will start with a blank dictionary!", "Error", JOptionPane.ERROR_MESSAGE);
            dictionary = new HashMap<>();
        }
    }

    private static void writeDict(){
        //Save dictionary yo the file
        try {
            File file = new File(FILE_PATH);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(FILE_PATH);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(dictionary);

            FileOutputStream fileOutputStream1 = new FileOutputStream(BACKUP);
            objectOutputStream = new ObjectOutputStream(fileOutputStream1);
            objectOutputStream.writeObject(dictionary);
            objectOutputStream.close();
            fileOutputStream.close();
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(new JLabel("error"), "Cannot write to dictionary", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected synchronized static String add(String word, String meaning, ServerThread thread){
        //Add a new word
        if(Server.dictionary.get(word) == null){
            Server.dictionary.put(word, meaning);
            thread.writeLog("added", word);
            ServerGUI.refresh();
            return "0";
        }else {
            return "1";
        }
    }

    protected synchronized static String update(String word, String meaning, ServerThread thread){
        //Update a word
        String old = Server.dictionary.get(word);
        if(old == null){
            return "1";
        }else {
            Server.dictionary.put(word, meaning);
            thread.writeLog("updated", word);
            ServerGUI.refresh();
            return "0";
        }
    }

    protected synchronized static String remove(String word, ServerThread thread){
        //Remove a word
        if (Server.dictionary.get(word) == null){
            return "1";
        }else {
            Server.dictionary.remove(word);
            thread.writeLog("removed", word);
            ServerGUI.refresh();
            return "0";
        }
    }

    protected static void quit(){
        //Save the dictionary and quit
        writeDict();
        System.exit(0);
    }
}
