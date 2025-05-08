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
    Button sendButton;
    TextView responseTextView;

    String[] commands = {"Echo", "Restart", "Shutdown", "Restore"};
    String[] computers = new String[28];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commandSpinner = findViewById(R.id.commandSpinner);
        computerListView = findViewById(R.id.computerListView);
        sendButton = findViewById(R.id.sendCommandButton);
        responseTextView = findViewById(R.id.responseTextView);

        // Populate the computers array with PRPC01 to PRPC27
        for (int i = 0; i < 27; i++) {
            computers[i] = String.format(Locale.US, "PRPC%02d", i + 1);
        }
        computers[27] = "192.168.68.111"; // Put you local IP/hostname here to test

        // Set up the spinners and list view
        ArrayAdapter<String> commandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commands);
        commandSpinner.setAdapter(commandAdapter);

        // Set up the list view with the computers array
        ArrayAdapter<String> computerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, computers);
        computerListView.setAdapter(computerAdapter);

        // Set up the send button click listener
        sendButton.setOnClickListener(v -> sendCommandsToSelected());
    }

    private void sendCommandsToSelected() {
        String command = commandSpinner.getSelectedItem().toString(); // Get the selected command from the spinner
        SparseBooleanArray checkedItems = computerListView.getCheckedItemPositions(); // Get the selected items from the list view
        responseTextView.append(command + ":\n");

        for (int i = 0; i < checkedItems.size(); i++) {
            int index = checkedItems.keyAt(i);
            if (checkedItems.valueAt(i)) {
                String host = computers[index];
                // Send the command to the selected computer using a separate thread
                new Thread(() -> {
                    String response = TcpClient.sendCommandTo(host, 41007, command);
                    runOnUiThread(() -> {
                        responseTextView.append(response + "\n");

                        // Auto-scroll to the bottom after updating the TextView
                        responseTextView.post(() -> {
                            NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
                            nestedScrollView.fullScroll(View.FOCUS_DOWN);
                        });
                    });
                }).start();
            }
        }
    }
}