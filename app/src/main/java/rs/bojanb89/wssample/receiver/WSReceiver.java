package rs.bojanb89.wssample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import rs.bojanb89.wssample.ws.WSHandler;

public class WSReceiver extends BroadcastReceiver {

    public static final IntentFilter WS_FILTER = new IntentFilter();

    public static final String ACTION_WS_RECEIVED = "rs.bojanb89.WSReceiver.ACTION_WS_RECEIVED";

    public static final String EXTRA_WS_MESSAGE = "rs.bojanb89.WSReceiver.EXTRA_WS_MESSAGE";

    static {
        WS_FILTER.addAction(ACTION_WS_RECEIVED);
    }

    WSHandler wsHandler;

    public WSReceiver(WSHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(ACTION_WS_RECEIVED.equals(action)) {
            String wsMessage = intent.getStringExtra(EXTRA_WS_MESSAGE);

            if (wsMessage != null) {
                wsHandler.onMessage(wsMessage);
            }
        }

    }
}
