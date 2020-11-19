package com.example.BlueToothPrinterApp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.print.sdk.CanvasPrint;
import com.android.print.sdk.FontProperty;
import com.android.print.sdk.PrinterInstance;
import com.android.print.sdk.PrinterType;
import com.android.print.sdk.bluetooth.BluetoothPort;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.utilities.PrintingCallback;

public class BlueToothPrinterApp extends Activity {
    /**
     * Called when the activity is first created.
     */
    EditText message;
    Button printbtn;

    byte FONT_TYPE;
    private static BluetoothSocket btsocket;
    private static OutputStream btoutputstream;
    private BluetoothDevice activeDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Printooth.INSTANCE.init(this);
        setContentView(R.layout.main);
        message = (EditText) findViewById(R.id.message);
        printbtn = (Button) findViewById(R.id.printButton);

        printbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    protected void connect() {
        if (btsocket == null) {
            Intent BTIntent = new Intent(getApplicationContext(), BTDeviceList.class);
            this.startActivityForResult(BTIntent, BTDeviceList.REQUEST_CONNECT_BT);
        } else {

            OutputStream opstream = null;
            try {
                opstream = btsocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btoutputstream = opstream;
            print_bt();

        }
    }

    private void print_bt() {
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            btoutputstream = btsocket.getOutputStream();

//            byte[] printformat = {0x1B, 0x21, FONT_TYPE};
//            btoutputstream.write(printformat);
//            String msg = message.getText().toString();
//            btoutputstream.write(msg.getBytes());
//            btoutputstream.write(0x0D);
//            btoutputstream.write(0x0D);
//            btoutputstream.write(0x0D);
//            btoutputstream.flush();

            //////////////////
            CanvasPrint cp = new CanvasPrint();
            cp.init(PrinterType.TIII);
            cp.setUseSplit(true);
            FontProperty fp = new FontProperty();
            fp.setFont(false, false, false, false, 40, null);
            cp.setFontProperty(fp);
            cp.drawText(message.getText().toString());
            cp.setTextAlignRight(false);
            Bitmap canvasImage = cp.getCanvasImage();

            byte[] imageBytes = Utils.decodeBitmap(canvasImage);
            btoutputstream.write(PrinterCommands.ESC_ALIGN_CENTER);
            btoutputstream.write(PrinterCommands.ESC_HORIZONTAL_CENTERS);

            btoutputstream.write(imageBytes);
            btoutputstream.write(PrinterCommands.FEED_LINE);
            btoutputstream.write(PrinterCommands.FEED_LINE);
            btoutputstream.flush();
            ////////////
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (btsocket != null) {
                btoutputstream.close();
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = BTDeviceList.getSocket();
            activeDevice = BTDeviceList.getActiveDevice();
            if (btsocket != null) {
                print_bt();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}