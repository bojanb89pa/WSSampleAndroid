package rs.bojanb89.wssample.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;

import rs.bojanb89.wssample.ws.Channel;
import rs.bojanb89.wssample.ws.OkHttpWebSocket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class WSService extends BaseIntentService {
    private static final String ACTION_START_WS = "rs.bojanb89.wssample.action.START_WS";

    private OkHttpWebSocket okHttpWebSocket;


    public WSService() {
        super("WSService");
    }

    /**
     * Starts this service to perform action Start WS.
     *
     * @see IntentService
     */
    public static void startWS(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, WSService.class);
        intent.setAction(ACTION_START_WS);
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_WS.equals(action)) {
                handleActionStartWS();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        okHttpWebSocket.close();
    }

    /**
     * Handle action Start WS in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStartWS() {
        Channel channel = new Channel("0", "ws://echo.websocket.org", Channel.Status.INIT);
        okHttpWebSocket = new OkHttpWebSocket(channel, this);
        okHttpWebSocket.connect();
    }

    public void testEcho() {
        if(okHttpWebSocket != null) {
            okHttpWebSocket.sendMessage("Echo message!!!");
        }
    }
}
