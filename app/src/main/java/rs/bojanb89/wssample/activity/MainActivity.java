package rs.bojanb89.wssample.activity;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rs.bojanb89.wssample.receiver.WSReceiver;
import rs.bojanb89.wssample.service.BaseIntentService;
import rs.bojanb89.wssample.ws.WSHandler;
import rs.bojanb89.wssample.R;
import rs.bojanb89.wssample.service.WSService;

public class MainActivity extends AppCompatActivity implements WSHandler {

    @BindView(R.id.messageTV)
    TextView messageTV;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private WSReceiver wsReceiver;


    private WSService wsService;

    private ServiceConnection wsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            wsService = (WSService) ((BaseIntentService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            wsService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        WSService.startWS(this, wsConnection);
        wsReceiver = new WSReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(wsReceiver, WSReceiver.WS_FILTER);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(wsReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(wsReceiver);
        }
        if(wsService != null) {
            unbindService(wsConnection);
        }
    }

    public void sendMessage(View v) {
        if(wsService != null) {
            wsService.testEcho();
        }
    }

    @Override
    public void onMessage(String text) {
        messageTV.setText(text);
    }
}
