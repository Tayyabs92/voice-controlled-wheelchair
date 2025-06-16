package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_VOICE = 10;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;

    String deviceName = "HC-05";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button btnConnect, btnForward, btnBackward, btnLeft, btnRight, btnStop;
    ImageButton btnMic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnStop = findViewById(R.id.btnStop);
        btnMic = findViewById(R.id.btnMic);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkAndRequestPermissions();

        btnConnect.setOnClickListener(v -> connectBluetooth());
        btnForward.setOnClickListener(v -> sendCommand("F"));
        btnStop.setOnClickListener(v -> sendCommand("S"));
        btnBackward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand("B");
                    return true;
                case MotionEvent.ACTION_UP:
                    sendCommand("S");
                    return true;
            }
            return false;
        });

        btnLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand("L");
                    return true;
                case MotionEvent.ACTION_UP:
                    sendCommand("S");
                    return true;
            }
            return false;
        });

        btnRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand("R");
                    return true;
                case MotionEvent.ACTION_UP:
                    sendCommand("S");
                    return true;
            }
            return false;
        });
        btnMic.setOnClickListener(v -> startVoiceRecognition());
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, REQUEST_PERMISSIONS);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS);
            }
        }
    }

    private void connectBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;

        for (BluetoothDevice device : pairedDevices) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (deviceName.equals(device.getName())) {
                targetDevice = device;
                break;
            }
        }

        if (targetDevice == null) {
            Toast.makeText(this, "Device not found. Pair first!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(uuid);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
                return;
            }

            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCommand(String cmd) {
        if (outputStream != null) {
            try {
                outputStream.write(cmd.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, REQUEST_VOICE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String voiceCommand = results.get(0).toLowerCase();

            switch (voiceCommand) {
                case "forward":
                    sendCommand("F");
                    break;
                case "backward":
                    sendCommand("B");
                    break;
                case "left":
                    sendCommand("L");
                    break;
                case "right":
                    sendCommand("R");
                    break;
                case "stop":
                    sendCommand("S");
                    break;
                default:
                    Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "All permissions are required", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }
}
