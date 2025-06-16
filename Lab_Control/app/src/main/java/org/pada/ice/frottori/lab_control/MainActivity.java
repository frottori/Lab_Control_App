package org.pada.ice.frottori.lab_control;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.SparseBooleanArray;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.core.widget.NestedScrollView;
import android.text.Html;

public class MainActivity extends AppCompatActivity {

    Spinner commandSpinner;
    ListView computerListView;
    Button sendButton, wolButton, checkOnlineButton;
    TextView responseTextView;
    String[] commands = {"Echo", "Restart", "Shutdown", "Restore"};
    String[] computers = new String[29];
    Boolean[] online_computers = new Boolean[29];
    String[]  os_computers = new String[29];
    String[] computers_ip = new String[29];
    String[] computers_mac = {
            "50:81:40:2B:91:8D", "50:81:40:2B:7C:78", "50:81:40:2B:78:DD", "50:81:40:2B:7B:3D", "50:81:40:2B:79:91",
            "C8:5A:CF:0F:76:3D", "C8:5A:CF:0D:71:24", "C8:5A:CF:0F:B3:FF", "C8:5A:CF:0E:2C:C4", "C8:5A:CF:0F:7C:D0",
            "C8:5A:CF:0D:71:3A", "C8:5A:CF:0F:EE:01", "C8:5A:CF:0E:1D:88", "C8:5A:CF:0F:F0:1E", "50:81:40:2B:7D:A4",
            "C8:5A:CF:0E:2C:78", "50:81:40:2B:87:F4", "C8:5A:CF:0F:EC:11", "C8:5A:CF:0F:7C:1F", "C8:5A:CF:0D:71:2C",
            "C8:5A:CF:0D:70:95", "50:81:40:2B:5F:D0", "50:81:40:2B:7A:0B", "50:81:40:2B:8F:D3", "50:81:40:2B:72:E0",
            "50:81:40:2B:7A:74", "C8:5A:CF:0F:7C:D4",
            // MACos,  WINDOWS
            "86:cd:34:f2:1f:a8", "2C:F0:5D:99:20:CA" 
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the main layout file
        setContentView(R.layout.activity_main);

        // Initialize the UI components
        commandSpinner = findViewById(R.id.commandSpinner);
        computerListView = findViewById(R.id.computerListView);
        sendButton = findViewById(R.id.sendCommandButton);
        responseTextView = findViewById(R.id.responseTextView);
        wolButton = findViewById(R.id.WOLButton);
        checkOnlineButton = findViewById(R.id.CheckOnline);

        // Populate the computers array with PRPC01 to PRPC27 and their online status
        // and OS information
        for (int i = 0; i < 27; i++) {
            computers[i] = String.format(Locale.US, "PC %02d", i + 1);
            computers_ip[i] = String.format(Locale.US, "192.168.88.%d", i + 2);
            online_computers[i] = false;
            os_computers[i] = "Unknown OS";
        }
        computers[27] = "Frosso's Macbook Air";
        computers_ip[27] = "192.168.68.107";
        online_computers[27] = false;
        os_computers[27] = "Unknown OS";

        computers[28] = "Frosso's PC";
        computers_ip[28] = "192.168.68.116";
        online_computers[28] = false;
        os_computers[28] = "Unknown OS";

        // Set up the list view with the computers array
        ComputerListAdapter computerAdapter = new ComputerListAdapter(this, computers, online_computers);
        computerListView.setAdapter(computerAdapter);
        // Set up the spinners for commands
        ArrayAdapter<String> commandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commands);
        commandSpinner.setAdapter(commandAdapter);

        // Set up the send button click listener
        sendButton.setOnClickListener(v -> sendServerCommand());
        // Set up the check online button click listener
        checkOnlineButton.setOnClickListener(v -> checkOnline());
        // Set up the WOL button click listener
        wolButton.setOnClickListener(v -> doWOL());
    }

    private void sendServerCommand() {
        // Get the selected command from the spinner
        String command = commandSpinner.getSelectedItem().toString();
        // Get the selected items from the list view
        SparseBooleanArray checkedItems = computerListView.getCheckedItemPositions();
        // Print the select command
        responseTextView.append(Html.fromHtml("<b>" + command + ":\n</b><br>", Html.FROM_HTML_MODE_LEGACY));

        for (int i = 0; i < checkedItems.size(); i++) {
            int index = checkedItems.keyAt(i); // Get the index of the checked item
            if (checkedItems.valueAt(i)) {
                String host = computers_ip[index]; // Get the host name of the selected computer
                // Send the command to the selected computer using a separate thread
                new Thread(() -> {
                    final int j = index;
                    TcpClient.sendCommand(host, 41007, command,MainActivity.this, responseTextView, response -> {
                        if (command.equals("Shutdown") && response.contains("Shutting down")) {
                            online_computers[j] = false;
                        }
                        else if (command.equals("Echo") && !response.toLowerCase().contains("error")) {
                            String[] parts = response.split(" - ", 2);
                            os_computers[j] = parts.length == 2 ? parts[1] : response;
                            online_computers[j] = true;
                        }
                        else if (command.equals("Restart") && response.contains("Rebooting...")) {
                            online_computers[j] = true;
                        }
                    });
                    scrollResp();
                }).start();
            }
        }
    }
    private void doWOL() {
        // HTML formatting for bold text
        responseTextView.append(Html.fromHtml("<b>Sent Wake-on-LAN to offline PCs\n</b><br>", Html.FROM_HTML_MODE_LEGACY));
        for (int i = 0; i < computers.length; i++) {
            if (!online_computers[i]) {
                String mac = computers_mac[i];
                new Thread(() -> sendWOLPacket(mac)).start();
            }
        }
        scrollResp();
    }

    private void checkOnline() {
        for (int i = 0; i < 27; i++) {
            computers[i] = String.format(Locale.US, "PC %02d", i + 1) + " - " + os_computers[i];
        }
        computers[27] = "Frosso's Macbook Air" + " - " + os_computers[27];;
        computers[28] = "Frosso's PC"+ " - " + os_computers[28];;

        ((ArrayAdapter) computerListView.getAdapter()).notifyDataSetChanged();
        computerListView.invalidateViews();
    }

    private void scrollResp() {
        responseTextView.post(() -> {
            NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
            nestedScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void sendWOLPacket(String macStr) {
        try {
            byte[] macBytes = new byte[6];
            String[] hex = macStr.split("[:-]");
            for (int i = 0; i < 6; i++) {
                macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }

            byte[] bytes = new byte[6 + 16 * macBytes.length];
            Arrays.fill(bytes, 0, 6, (byte) 0xFF);
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }
            // gmele 192.168.88.255
            InetAddress address = InetAddress.getByName("192.168.2.255"); // your broadcast ip
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            runOnUiThread(() -> responseTextView.append("WOL error: " + e.getMessage() + "\n"));
        }
    }
}