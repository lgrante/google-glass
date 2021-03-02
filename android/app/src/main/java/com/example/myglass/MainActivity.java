package com.example.myglass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.myglass.R;


public class MainActivity extends AppCompatActivity {

    private Button deviceListButton;
    private Button glassButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceListButton = (Button) findViewById(R.id.activity_main_bt_device_list_btn);
        glassButton = (Button) findViewById(R.id.activity_main_glass_btn);

        deviceListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent deviceListActivity = new Intent(MainActivity.this, DeviceListActivity.class);

                startActivity(deviceListActivity);
            }
        });
        glassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent glassActivity = new Intent(MainActivity.this, GlassActivity.class);

                startActivity(glassActivity);
            }
        });
    }
}