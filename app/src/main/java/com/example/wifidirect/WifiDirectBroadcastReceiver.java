package com.example.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;


    public WifiDirectBroadcastReceiver (WifiP2pManager manager,WifiP2pManager.Channel channel,
                                        MainActivity mainActivity) {
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            if(manager!=null){//android.net.wifi.p2p.CONNECTION_STATE_CHANGE
                manager.requestPeers(channel,mainActivity.peerListListener);

            }
            //Check to see if Wifi is enabled and notify approprite activity
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            //Call WifiP2PManager.req
            // uestPeers() to get a list of current peers
            if(manager!=null){
                manager.requestPeers(channel,mainActivity.peerListListener);
            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //respond to new connection or disconnection
            if(manager!=null){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    manager.requestConnectionInfo(channel,mainActivity.connectionInfoListener);
                }else{
                    mainActivity.connectionText.setText("Not Connected");
                }
            }

        }

    }
}
