package org.pada.ice.frottori.lab_control;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.SparseBooleanArray;
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
    String[] computers = new String[28];
    Boolean[] online_comp = new Boolean[28]; 
    String[]  os_comp = new String[28];    

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
            computers[i] = String.format(Locale.US, "PRPC%02d", i + 1);
            online_comp[i] = false;
            os_comp[i] = "Unknown OS";
        }
        computers[27] = "192.168.68.107"; // Put you local IP/hostname here to test
        online_comp[27] = false;
        os_comp[27] = "Unknown OS";

        ComputerListAdapter computerAdapter = new ComputerListAdapter(this, computers, online_comp);
        computerListView.setAdapter(computerAdapter);
        // Set up the spinners and list view
        ArrayAdapter<String> commandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commands);
        commandSpinner.setAdapter(commandAdapter);
        // Set up the list view with the computers array
        //ArrayAdapter<String> computerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, computers);
        //computerListView.setAdapter(computerAdapter);

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
                String host = computers[index]; // Get the host name of the selected computer
                // Send the command to the selected computer using a separate thread
                new Thread(() -> {
                    final int j = index;
                    TcpClient.sendCommand(host, 41007, command,MainActivity.this, responseTextView, response -> {
                        if (command.equals("Shutdown") && response.contains("Shutting down")) {
                            online_comp[j] = false;
                        }
                        else if (command.equals("Echo") && !response.toLowerCase().contains("error")) {
                            String[] parts = response.split(" - ", 2);
                            if (parts.length == 2) {
                                os_comp[j] = parts[1]; 
                            } else {
                                os_comp[j] = response; 
                            }
                            online_comp[j] = true;
                        }
                        else if (command.equals("Restart") && response.contains("Rebooting...")) {
                            online_comp[j] = true;
                        }
                    });
                    scrollResp();
                }).start();
            }
        }
    }
    private void doWOL() {
        // HTML formatting for bold text
        responseTextView.append(Html.fromHtml("<b>Sending Wake-on-LAN to offline PCs:\n</b><br>", Html.FROM_HTML_MODE_LEGACY));
        for (int i = 0; i < computers.length; i++) {
            if (!online_comp[i]) {
                online_comp[i] = true;
                responseTextView.append(computers[i] + " turned ON\n");
            }
        }
        responseTextView.append("\n");
        scrollResp();
    }

    private void checkOnline() {
        for (int i = 0; i < computers.length; i++) {
            computerListView.invalidateViews();
        }
    }

    private void scrollResp() {
        responseTextView.post(() -> {
            NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
            nestedScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }
}