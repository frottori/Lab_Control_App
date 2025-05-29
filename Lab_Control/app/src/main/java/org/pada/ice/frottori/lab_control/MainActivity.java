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

public class MainActivity extends AppCompatActivity {

    Spinner commandSpinner;
    ListView computerListView;
    Button sendButton, wolButton;
    TextView responseTextView;
    String[] commands = {"Echo", "Restart", "Shutdown", "Restore", "Check Online PCs"};
    String[] computers = new String[28];
    Boolean[] online_comp = new Boolean[28];
    String[]  os_comp = new String[28];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout file
        setContentView(R.layout.activity_main);

        // Initialize the UI components
        commandSpinner = findViewById(R.id.commandSpinner);
        computerListView = findViewById(R.id.computerListView);
        sendButton = findViewById(R.id.sendCommandButton);
        wolButton = findViewById(R.id.WOLButton);
        responseTextView = findViewById(R.id.responseTextView);

        // Populate the computers array with PRPC01 to PRPC27
        for (int i = 0; i < 27; i++) {
            computers[i] = String.format(Locale.US, "PRPC%02d", i + 1);
        }
        computers[27] = "172.20.10.2"; // Put you local IP/hostname here to test

        // Set up the spinners and list view
        ArrayAdapter<String> commandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commands);
        commandSpinner.setAdapter(commandAdapter);

        // Set up the list view with the computers array
        ArrayAdapter<String> computerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, computers);
        computerListView.setAdapter(computerAdapter);

        // Set up the send button click listener
        sendButton.setOnClickListener(v -> sendCommand());

        // Set up the WOL button click listener
        wolButton.setOnClickListener(v -> sendWOL());
    }

    private void sendCommand() {
        // Get the selected command from the spinner
        String command = commandSpinner.getSelectedItem().toString();
        // Get the selected items from the list view
        SparseBooleanArray checkedItems = computerListView.getCheckedItemPositions();
        // Print the select command
        responseTextView.append(command + ":\n");

        for (int i = 0; i < checkedItems.size(); i++) {
            int index = checkedItems.keyAt(i); // Get the index of the checked item
            if (checkedItems.valueAt(i)) {
                String host = computers[index]; // Get the host name of the selected computer
                // Send the command to the selected computer using a separate thread
                new Thread(() -> {
                    String response;
                    // response for Check Online PCs
                    if (command.equals("Check Online PCs")) {
                        response = TcpClient.sendCheckCommand(host, 41007, command);
                    } else {
                        TcpClient.sendCommand(host, 41007, command, MainActivity.this, responseTextView);
                        responseTextView.post(() -> {
                            NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
                            nestedScrollView.fullScroll(View.FOCUS_DOWN);
                        });
                        return;
                    }
                    final String finalResponse = response;
                    runOnUiThread(() -> handleResponse(finalResponse, host, index));
                }).start();
            }
        }
    }

    private void handleResponse(String response, String host, int index) {
            if (!response.toLowerCase().contains("error")) {
                os_comp[index] = response.trim();
                online_comp[index] = true;
                responseTextView.append(host + " - " + os_comp[index] + " is ONLINE\n");
            } else {
                os_comp[index] = "unknown";
                online_comp[index] = false;
                responseTextView.append(host + " - " + os_comp[index] + " is OFFLINE\n");
            }
        responseTextView.post(() -> {
            NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
            nestedScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }
    private void sendWOL(){
        
    }
}