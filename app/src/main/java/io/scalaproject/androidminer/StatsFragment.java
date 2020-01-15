// Copyright (c) 2020, Scala Project
//
// Please see the included LICENSE file for more information.

package io.scalaproject.androidminer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import io.scalaproject.androidminer.api.Data;
import io.scalaproject.androidminer.api.PoolManager;
import io.scalaproject.androidminer.api.PoolItem;
import io.scalaproject.androidminer.api.ProviderAbstract;
import io.scalaproject.androidminer.api.ProviderListenerInterface;

public class StatsFragment extends Fragment {

    private static final String LOG_TAG = "MiningSvc";

    private TextView tvStatCheckOnline;

    private TextView data;
    private TextView dataNetwork;

    Timer timer;
    long delay = 30000L;
    protected ProviderListenerInterface statsListener;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        data = (TextView) view.findViewById(R.id.fetchdata);
        dataNetwork = (TextView) view.findViewById(R.id.fetchdataNetwork);
        tvStatCheckOnline = view.findViewById(R.id.statCheckOnline);

        ProviderAbstract api = PoolManager.getSelectedPool().getInterface();
        statsListener = api.setStatsChangeListener(new ProviderListenerInterface(){
            void onStatsChange(Data d) {
                data.setText("");
                dataNetwork.setText("");
            }
        });

        if (!checkValidState()) {
            return view;
        }



        api.setStatsChangeListener(statsListener);
        api.execute();
        repeatTask();

        return view;
    }

    private boolean checkValidState() {

        PoolItem pi = PoolManager.getSelectedPool();

        if (Config.read("init").equals("1") == false || pi == null) {
            data.setText("(start mining to view stats)");
            tvStatCheckOnline.setText("");
            return false;
        }

        if (pi.getPoolType() == 0) {
            data.setText("(stats are not available for custom pools)");
            tvStatCheckOnline.setText("");
            return false;
        }

//        wallet = Config.read("address");
//        apiUrl = pi.getPoolUrl();
//        statsUrl = pi.getStatsURL();
//
//        tvStatCheckOnline.setText(Html.fromHtml("<a href=\"" + statsUrl + "?wallet=" + wallet + "\">Check Stats Online</a>"));
//        tvStatCheckOnline.setMovementMethod(LinkMovementMethod.getInstance());

        return true;
    }

    private void repeatTask() {

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        timer = new Timer("Timer");

        if (!checkValidState()) {
            return;
        }

        TimerTask task = new TimerTask() {
            public void run() {
                ProviderAbstract process = PoolManager.getSelectedPool().getInterface();
                process.setStatsChangeListener(statsListener);
                process.execute();
                repeatTask();
            }
        };

        timer.schedule(task, delay);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume of StatsFragment");
        repeatTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "OnPause of StatsFragment");
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

}

