package rs.bojanb89.wssample.ws;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import rs.bojanb89.wssample.receiver.WSReceiver;

/**
 * Created by bojanb on 1/11/17.
 */

public class OkHttpWebSocket  implements WebSocketListener {
    private static final String TAG = "OKHTTP_WS";

    public static final int RECONNECT_TIME_MAX = 15; // time in seconds
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private Channel channel;

    private OkHttpClient mClient;

    private WebSocket webSocket;

    private Context context;

    private int reconnectCounter;

    public static int WS_CLOSE = 1001;
    public static int WS_RESTART = 1002;

    public OkHttpWebSocket(Channel channel, Context context) {

        this.channel = channel;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void connect() {
        if(isCanceled()) {
            close(WS_RESTART, "Restarting ws...");
        }
        if(!isOpenOrOpening()) {
            if (mClient != null && mClient.dispatcher() != null) {
                mClient.dispatcher().cancelAll();
            }
            open();
        }
    }

    public void close() {
        close(WS_CLOSE, "Closed by user");
    }

    private void open() {

        if(mClient == null) {
            mClient = new OkHttpClient.Builder()
                    .readTimeout(0,  TimeUnit.NANOSECONDS)
                    .build();
        }

        Request request = new Request.Builder().url(channel.getChannelUrl()).tag(channel.getChannelId()).build();

        WebSocketCall call = WebSocketCall.create(mClient, request);
        this.channel.setStatus(Channel.Status.OPENING);
        call.enqueue(this);
    }

    public void close(final int code, final String reason) {
        channel.setStatus(Channel.Status.CLOSING);
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!OkHttpWebSocket.this.isClosedOrClosing()) {
                        if(webSocket != null) {
                            webSocket.close(code, reason);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private boolean isCanceled() {
        return this.channel.getStatus().equals(Channel.Status.CANCELED);
    }

    public boolean isOpen() {
        return this.channel.getStatus().equals(Channel.Status.OPEN);
    }

    public boolean isOpenOrOpening() {
        return this.channel.getStatus().equals(Channel.Status.OPEN)
                || this.channel.getStatus().equals(Channel.Status.OPENING);
    }

    private boolean isClosedOrClosing() {
        return this.channel.getStatus().equals(Channel.Status.CLOSED)
                || this.channel.getStatus().equals(Channel.Status.CLOSING);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {

        this.webSocket = webSocket;
        channel.setStatus(Channel.Status.OPEN);

        reconnectCounter = 1;

        if (response != null && response.code() == 101) {
            Log.d(TAG, "WS successfully connected to: " + response.request().url());
        } else {
            Log.w(TAG, "WS opened connection with code != 101.");
        }
    }

    @Override
    public void onFailure(IOException e, Response response) {

        Log.e(TAG, "OKHTTP WS connection failed " + e);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(reconnectCounter <= RECONNECT_TIME_MAX) {
                    if(reconnectCounter < RECONNECT_TIME_MAX) {
                        reconnectCounter++;
                    }
                    connect();
                }
            }
        }, reconnectCounter * 1000);
    }

    @Override
    public void onMessage(ResponseBody responseBody) throws IOException {
        final String responseString = responseBody.string();
        Log.d(TAG, "Response: "+ responseString);
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(context.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, responseString);
                Intent data = new Intent();
                data.setAction(WSReceiver.ACTION_WS_RECEIVED);
                data.putExtra(WSReceiver.EXTRA_WS_MESSAGE, responseString);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(data);
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void onPong(Buffer payload) {

    }

    @Override
    public void onClose(int code, String reason) {
        if(code == WS_RESTART && !isOpenOrOpening()) {
            open();
        } else {
            Log.d(TAG, "WS closed - reason: " + reason);
            channel.setStatus(Channel.Status.CLOSED);
        }
    }



    public void sendMessage(final String message) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                RequestBody requestBody = RequestBody.create(okhttp3.ws.WebSocket.TEXT, message);
                try {
                    webSocket.sendMessage(requestBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d(TAG, "WS message sent.");
            }
        }.execute();

    }
}
