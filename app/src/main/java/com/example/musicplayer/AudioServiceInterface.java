package com.example.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;

// AudioService에서 정의했던 public 함수들을 사용하기 위해선 Service에 Bind를 해야 직접 접근이 가능함
// 따라서 해당 클래스가 그 역할을 수행함
public class AudioServiceInterface {
    private ServiceConnection mServiceConnection;
    private AudioService mService;

    public AudioServiceInterface(Context context) {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((AudioService.AudioServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceConnection = null;
                mService = null;
            }
        };

        // bindService를 통해서 AudioService와 직접 연결함
        context.bindService(new Intent(context, AudioService.class).setPackage(context.getPackageName()), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setPlayList(ArrayList<Long> audioIds) {
        if (mService != null) {
            mService.setPlayList(audioIds);
        }
    }

    public void play(int position) {
        if (mService != null) {
            mService.play(position);
        }
    }

    public void play() {
        if (mService != null) {
            mService.play();
        }
    }

    public void pause() {
        if (mService != null) {
            mService.pause();
        }
    }

    public void forward() {
        if (mService != null) {
            mService.forward();
        }
    }

    public void rewind() {
        if (mService != null) {
            mService.rewind();
        }
    }

    public void togglePlay() {
        if (isPlaying()) {
            mService.pause();
        } else {
            mService.play();
        }
    }

    public boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public AudioAdapter.AudioItem getAudioItem() {
        if (mService != null) {
            return mService.getAudioItem();
        }
        return null;
    }
}
