package com.example.inventario.lucesrgb;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;




public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    Button buttonIzq,buttonDer,buttonAmb,buttonOn,buttonOff;
    String color = "255-255-255";

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefe=getSharedPreferences("datos",Context.MODE_PRIVATE);
        String memoria =  prefe.getString("color","");
        if(memoria.isEmpty()||memoria == null){
            color = "255-255-255";
        }else {
            color =memoria;
        }

        ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                // Handle on color change
                color =  hex2Rgb(Integer.toHexString(selectedColor));
                Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
                //Toast.makeText(MainActivity.this,"selectedColor: " + color,Toast.LENGTH_SHORT).show();
                mConnectedThread.write(color);
            }
        });
        colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
              color =  hex2Rgb(Integer.toHexString(selectedColor));
                //Toast.makeText(MainActivity.this,"selectedColor: " + color,Toast.LENGTH_SHORT).show();
                mConnectedThread.write(color);
            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        buttonIzq = (Button) findViewById(R.id.buttonIzq);
        buttonIzq.setOnClickListener(buttonIzqOnClickListener);
        buttonDer = (Button)findViewById(R.id.buttonDer);
        buttonDer.setOnClickListener(buttonDerOnClickListener);
        buttonAmb = (Button)findViewById(R.id.buttonAmb);
        buttonAmb.setOnClickListener(buttonAmbOnClickListener);

        buttonOn = (Button)findViewById(R.id.buttonON);
        buttonOn.setOnClickListener(buttonOnOnClickListener);
        buttonOff = (Button)findViewById(R.id.buttonOFF);
        buttonOff.setOnClickListener(buttonOffOnClickListener);

    }

    View.OnClickListener buttonIzqOnClickListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mConnectedThread.write("III-III-III");
                }};

    View.OnClickListener buttonDerOnClickListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mConnectedThread.write("DDD-DDD-DDD");
                }};

    View.OnClickListener buttonAmbOnClickListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mConnectedThread.write("AAA-AAA-AAA");
                }};

    View.OnClickListener buttonOnOnClickListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mConnectedThread.write("UUU-UUU-UUU");
                }};

    View.OnClickListener buttonOffOnClickListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mConnectedThread.write("EEE-EEE-EEE");
                }};


    public static String hex2Rgb(String colorStr) {
        return  String.format("%03d",Integer.valueOf(colorStr.substring( 2, 4 ), 16 ))+"-"+
                String.format("%03d",Integer.valueOf(colorStr.substring( 4, 6 ), 16 ))+"-"+
                String.format("%03d",Integer.valueOf(colorStr.substring( 6, 8 ), 16 ));
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }

        SharedPreferences prefe=getSharedPreferences("datos",Context.MODE_PRIVATE);
        String memoria =  prefe.getString("color","");
        if(memoria.isEmpty()||memoria == null){
            color = "255-255-255";
        }else {
            color =memoria;
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.write(color);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread  {
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }


            mmOutStream = tmpOut;
        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                SharedPreferences preferencias=getSharedPreferences("datos",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferencias.edit();
                editor.putString("color", input);
                editor.commit();
                mmOutStream.write(msgBuffer);
                mmOutStream.flush();
                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }


}