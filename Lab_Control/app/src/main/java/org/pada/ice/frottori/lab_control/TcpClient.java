package org.pada.ice.frottori.lab_control;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import android.widget.TextView;

public class TcpClient {
    public static String sendCommandTo(String host, int port, String command) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END"))
                    break;
                responseBuilder.append(line).append("\n");
            }
            return responseBuilder.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
