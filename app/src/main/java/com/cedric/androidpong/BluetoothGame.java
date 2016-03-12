package com.cedric.androidpong;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * Created by Cedric on 12/03/2016.
 */
public class BluetoothGame {

    BluetoothAdapter bluetoothAdapter;

    public BluetoothGame()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Appareil ne supporte pas le bluetooth
        }

        ////Vérifier ou activer le bluetooth sur l'appareil
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
    }
}
