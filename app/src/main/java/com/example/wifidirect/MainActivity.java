package com.example.wifidirect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wifidirect.adapters.BookCategoryAdapter;
import com.example.wifidirect.adapters.BookNamesAdapter;
import com.example.wifidirect.models.BookModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    EditText input, bookNameEDT;
    Button enter, cancel, addBookNameBTN, bookNameCancelBTN;
    AlertDialog dialog;
    AlertDialog bookNameDialog;
    RecyclerView bookListRV, bookNamesListRV;
    BookNamesAdapter bookNamesAdapter;
    BookCategoryAdapter bookCategoryAdapter;
    //ArrayList<String> bookModelArrayList;
    ArrayList<BookModel> bookModelArrayList;
    TextView connectionText, messageTextView;
    Button aSwitch, discoverBtn;
    ListView listView;
    Button sendButton;
    Socket socket;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    ClientClass clientClass;
    ServerClass serverClass;

    boolean isHost;


    @Override
    protected void onResume() {
        super.onResume();
        if (isWifiEnabled()) {
            aSwitch.setText("Turn Off Wifi");
        } else {
            aSwitch.setText("Turn ON Wifi");
        }

        registerReceiver(receiver, intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void deleteCategory(BookModel category) {
        bookModelArrayList.remove(category);
        bookCategoryAdapter.notifyDataSetChanged();
    }

    private BookModel selectedModel;

    public void addBookNames(BookModel bookModel) {
        selectedModel = bookModel;

        bookNamesAdapter = new BookNamesAdapter(MainActivity.this, MainActivity.this, selectedModel);
        bookNamesListRV.setAdapter(bookNamesAdapter);

        bookNameDialog.show();

    }

    public class ServerClass extends Thread {

        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;


        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;

                    while (socket != null) {
                        try {
                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer, 0, finalBytes);//[{"bookName":[],"categoryName":"gfhg"}]
                                        Gson gson = new Gson();

                                        // Define the type of the ArrayList<String>
                                        Type type = new TypeToken<ArrayList<BookModel>>() {
                                        }.getType();
                                        //Type type = new ArrayList<BookModel>().getClass();

                                        // Convert JSON string to ArrayList<String>
                                        ArrayList<BookModel> stringArrayList = gson.fromJson(tempMSG, type);
                                        bookModelArrayList.clear();
                                        bookModelArrayList.addAll(stringArrayList);

                                        HashSet<BookModel> stringHashSet = new HashSet<>(bookModelArrayList);
                                        ArrayList<BookModel> uniqueStringArrayList = new ArrayList<>(stringHashSet);
                                        bookModelArrayList.clear();
                                        bookModelArrayList.addAll(uniqueStringArrayList);

                                        bookCategoryAdapter.notifyDataSetChanged();
//                                        bookCategoryAdapter = new BookCategoryAdapter(MainActivity.this,
//                                                stringArrayList);
//                                        bookListRV.setAdapter(bookCategoryAdapter);
                                        //messageTextView.setText(tempMSG);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }


    public class ClientClass extends Thread {
        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;


        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();

        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;

                    while (socket != null) {
                        try {
                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer, 0, finalBytes);
                                        Gson gson = new Gson();

                                        // Define the type of the ArrayList<String>
                                        //Type type = new ArrayList<BookModel>().getClass();
                                        Type type = new TypeToken<ArrayList<BookModel>>() {
                                        }.getType();

                                        // Convert JSON string to ArrayList<String>
                                        ArrayList<BookModel> stringArrayList = gson.fromJson(tempMSG, type);
                                        bookModelArrayList.clear();
                                        bookModelArrayList.addAll(stringArrayList);

                                        HashSet<BookModel> stringHashSet = new HashSet<>(bookModelArrayList);
                                        ArrayList<BookModel> uniqueStringArrayList = new ArrayList<>(stringHashSet);
                                        bookModelArrayList.clear();
                                        bookModelArrayList.addAll(uniqueStringArrayList);

                                        bookCategoryAdapter.notifyDataSetChanged();
//                                        bookCategoryAdapter = new BookCategoryAdapter(MainActivity.this,
//                                                stringArrayList);
//                                        bookListRV.setAdapter(bookCategoryAdapter);


                                        //messageTextView.setText(tempMSG);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bookModelArrayList = new ArrayList<>();
        bookListRV = findViewById(R.id.bookListRV);


        bookListRV = findViewById(R.id.bookListRV);


        bookListRV.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        bookListRV.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(bookListRV.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        bookListRV.addItemDecoration(dividerItemDecoration);

        bookCategoryAdapter = new BookCategoryAdapter(MainActivity.this, MainActivity.this, bookModelArrayList);
        bookListRV.setAdapter(bookCategoryAdapter);


        ///////////////////////////
        ///////////////////////////
        ///////////////////////////




        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES},
                    1001);
        }


        initViews();
        exqListner();
    }

    private boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions are granted, proceed with your task
                // Your code here
            } else {
                // Some permissions are denied, handle accordingly
                // Your code here
            }
        }
    }

    private void exqListner() {

        aSwitch.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(intent, 1);
        });

        discoverBtn.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES},
                        1001);
                return;
            }

            if (isWifiEnabled()) {
                aSwitch.setText("Turn Off Wifi");
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionText.setText("Discovery started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionText.setText("Discovery not started");
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Turn on Wifi to proceed", Toast.LENGTH_SHORT).show();
                aSwitch.setText("Turn ON Wifi");
            }


        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES},
                            1001);
                    return;
                }
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        connectionText.setText("Connected to " + device.deviceAddress);
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionText.setText("Not Connected to " + device.deviceAddress);
                    }
                });
            }
        });

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        enter.setOnClickListener(v -> {
            if (!input.getText().toString().isEmpty()) {
                BookModel bookModel = new BookModel();
                bookModel.categoryName = input.getText().toString();
                bookModelArrayList.add(bookModel);
//                bookCategoryAdapter = new BookCategoryAdapter(MainActivity.this, bookModelArrayList);
//                bookListRV.setAdapter(bookCategoryAdapter);
                bookCategoryAdapter.notifyDataSetChanged();
                input.setText("");

                dialog.dismiss();
            } else {
                input.setError("Book Category cannot be empty");
            }

        });

        findViewById(R.id.addBook).setOnClickListener(view -> {
            dialog.show();
        });

        addBookNameBTN.setOnClickListener(view -> {
            if (!bookNameEDT.getText().toString().isEmpty()) {
                selectedModel.bookNames.add(bookNameEDT.getText().toString().trim());
                bookNamesAdapter.notifyDataSetChanged();
                bookNameEDT.setText("");
            } else {
                bookNameEDT.setError("Book Name cannot be empty");
            }
        });
        bookNameCancelBTN.setOnClickListener(view -> {
            bookCategoryAdapter.notifyDataSetChanged();
            bookNameDialog.dismiss();
        });

        sendButton.setOnClickListener(view -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(new Runnable() {
                @Override
                public void run() {

                    String listStr = new Gson().toJson(bookModelArrayList);
                    if (isHost) {
                        if (serverClass != null && bookModelArrayList.size() > 0) {
                            if (bookModelArrayList.size() > 0) {
                                serverClass.write(listStr.getBytes());
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Add Category to proceed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Select a device to proceed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    } else if (!isHost) {
                        if (clientClass != null) {
                            if (bookModelArrayList.size() > 0) {
                                clientClass.write(listStr.getBytes());
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Add Category to proceed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Select a device to proceed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }


//                    if(msg!=null && isHost){
//                        serverClass.write(msg.getBytes());
//                    }else if(msg!=null && !isHost){
//                        clientClass.write(msg.getBytes());
//                    }
                }
            });


        });

    }

    public void deleteBookNameFromList(String bookName){
        selectedModel.bookNames.remove(bookName);
        bookNamesAdapter.notifyDataSetChanged();
    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.equals(peers)) {

                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, deviceNameArray);

                listView.setAdapter(adapter);

                if (peers.size() == 0) {
                    connectionText.setText("No Device Found");
                    connectionText.setTextColor(getResources().getColor(R.color.yellow));


                } else {
                    connectionText.setTextColor(getResources().getColor(R.color.green));
                    connectionText.setText(wifiP2pDeviceList.getDeviceList().size() + " Device Found");
                }

            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                connectionText.setText("Host");
                isHost = true;
                serverClass = new ServerClass();
                serverClass.start();
            } else {
                connectionText.setText("Client");
                isHost = false;
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }

        }
    };

    private void initViews() {


        connectionText = findViewById(R.id.connectionTV);
        messageTextView = findViewById(R.id.messageTV);
        aSwitch = findViewById(R.id.onOffBTN);


        discoverBtn = findViewById(R.id.onDiscoverBTN);

        listView = findViewById(R.id.listview);
        sendButton = findViewById(R.id.sendButton);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        receiver = new WifiDirectBroadcastReceiver(manager, channel, this);


        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        //Add Book Category DialogView
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_category_layout, null);

        input = dialogView.findViewById(R.id.input);
        cancel = dialogView.findViewById(R.id.btn_cancel);
        enter = dialogView.findViewById(R.id.btn_save);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        dialog = builder.create();


        //Add BookName DialogView
        View dialogBookView = getLayoutInflater().inflate(R.layout.dialog_book_name_layout, null);

        bookNameEDT = dialogBookView.findViewById(R.id.bookNameEDT);
        bookNameCancelBTN = dialogBookView.findViewById(R.id.bookNameCancelBTN);
        addBookNameBTN = dialogBookView.findViewById(R.id.addBookNameBTN);

        AlertDialog.Builder builderBookName = new AlertDialog.Builder(MainActivity.this);
        builderBookName.setView(dialogBookView);
        bookNameDialog = builderBookName.create();
        bookNameDialog.setCancelable(false);



        bookNamesListRV = dialogBookView.findViewById(R.id.bookNamesListRV);



        bookNamesListRV.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        bookNamesListRV.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(bookNamesListRV.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        bookNamesListRV.addItemDecoration(dividerItemDecoration);




//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

    }
}