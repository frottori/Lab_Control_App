package org.pada.ice.frottori.lab_control;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import android.app.Activity;
import android.widget.TextView;

public class TcpClient {

    public static void sendCommand(String host, int port, String command, Activity activity,
                                   TextView responseTextView, ResponseHandler handler) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END"))
                    break;
                String finalLine = line;
                activity.runOnUiThread(() -> {
                    responseTextView.append(finalLine + "\n");
                    handler.handle(finalLine); // Handle response in the UI
                });
            }
        } catch (IOException e) {
            String error = "Error: " + e.getMessage();
            activity.runOnUiThread(() -> {
                responseTextView.append(error + "\n");
                handler.handle(error); // Handle error in the UI
            });
        }
    }
}

