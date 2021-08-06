/**
 * @author Wangshu Fu
 * @studentID 1112531
 */
package Server;

import org.json.JSONObject;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Calendar;

class ServerThread extends Thread {

    private final Socket connection;
    public ServerThread(Socket connection){
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            //Communicate with client
            InputStream inputStream = connection.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            JSONObject pack = new JSONObject(dataInputStream.readUTF());
            int command = pack.getInt("command");
            String para = pack.getString("para");

            OutputStream outputStream = connection.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            String[] input = para.split("-");

            switch (command) {
                case 1 -> dataOutputStream.writeUTF(String.valueOf(pack.put("reply", search(input[0]))));
                case 2 -> dataOutputStream.writeUTF(String.valueOf(pack.put("reply", Server.add(input[0], input[1], this))));
                case 3 -> dataOutputStream.writeUTF(String.valueOf(pack.put("reply", Server.update(input[0], input[1], this))));
                case 4 -> dataOutputStream.writeUTF(String.valueOf(pack.put("reply", Server.remove(input[0], this))));
            }
            connection.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JLabel(""), "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String search(String word){
        //Look for a word
        String meaning = Server.dictionary.get(word);
        if(meaning != null){
            ServerGUI.refresh();
            writeLog("searched", word);
            return "0-" + meaning;
        }else {
            return "1";
        }
    }

    protected void writeLog(String action, String word){
        //Show a log on Server GUI
        ServerGUI.log.setText(ServerGUI.log.getText() + this.connection.getInetAddress() + " " + action + " the word \"" + word + "\"." + Calendar.getInstance().getTime() + "\n");
    }
}
