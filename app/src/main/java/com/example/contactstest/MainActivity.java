package com.example.contactstest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.contactstest.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private View root;
    private String[] nameAndNumbers;

    private ActivityResultLauncher<String> permissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        call();
                    }else{
                        Toast.makeText(MainActivity.this, "拒绝授权", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String> readContacts = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        nameAndNumbers = getNameAndNumbers();
                        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,nameAndNumbers);
                        binding.contactsList.setAdapter(adapter);
                    }else{
                        Toast.makeText(MainActivity.this, "拒绝授权", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String> readSms = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        getSms();
                    }else{
                        Toast.makeText(MainActivity.this, "拒绝授权", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        root = binding.getRoot();
        setContentView(root);

        ButtonHandler bh = new ButtonHandler();
        binding.DeprecatedCall.setOnClickListener(bh);
        binding.recommendCall.setOnClickListener(bh);
        readContacts.launch(Manifest.permission.READ_CONTACTS);
        readSms.launch(Manifest.permission.READ_SMS);

    }

    private class ButtonHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.Deprecated_call:
                    // 检查权限是否已授权
                    if(ContextCompat.checkSelfPermission(
                            v.getContext(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        // 未授权则去申请权限
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE}, 110);
                    }else{
                        // 已授权，则拨打电话
                        call();
                    }
                    break;
                case R.id.recommend_call:
                    permissionResult.launch(Manifest.permission.CALL_PHONE);
            }
        }
    }

    /**
     * 拨打电话
     */
    private void call(){
        try{
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:13995576551"));
            startActivity(intent);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    // 如需要看到结果，请在模拟系统中添加通讯录记录
    public  String[] getNameAndNumbers() {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Identity._ID
        };

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        List<String> result = new ArrayList<>();

        // Check if the cursor is not null and not empty
        if (cursor != null && cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int indexId = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Identity._ID);

            do {
                String name = cursor.getString(indexName);
                String number = cursor.getString(indexNumber);
                String id = cursor.getString(indexId);
                result.add(id + " " + name + " " + number);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return result.toArray(new String[0]);
    }
    /**
     * ActivityCompat.requestPermissions执行后会回调本方法
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            // 如果请求编号为110(这是ActivityCompat.requestPermissions调用时传入的自定义值)
            case 110:
                // 用户已授权
                if(grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    call();
                // 用户未授权
                }else{
                    Toast.makeText(MainActivity.this, "拒绝授权", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void getSms(){
        Uri uri = Uri.parse("content://sms/");
        ContentResolver resolver = getContentResolver();
        String[] projection = {"_id", "address", "body", "date", "type"};
        Cursor cursor = resolver.query(uri, projection,null,null,null);
        if(cursor!=null && cursor.getCount()>0){
            int _id;
            String address;
            String body;
            String date;
            int type;
            while(cursor.moveToNext()){
                _id = cursor.getInt(0);
                address=cursor.getString(1);
                body=cursor.getString(2);
                date=cursor.getString(3);
                type=cursor.getInt(4);
                Log.i("sms","_id=" + _id
                                     + " address=" + address
                                     + " body=" + body
                                     + " date=" + date
                                     + " type=" + type);
            }
        }
    }
}