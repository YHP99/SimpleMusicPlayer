package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;

import java.util.ArrayList;

// 음악 재생을 할 수 있도록 MediaPlayer를 갖고있는 클래스
public class AudioService extends Service {
    private final IBinder mBinder = new AudioServiceBinder();
    private ArrayList<Long> mAudioIds = new ArrayList<>();
    private MediaPlayer mMediaPlayer;
    private boolean isPrepared;
    private int mCurrentPosition;
    private AudioAdapter.AudioItem mAudioItem;

    // 서비스에서 제공해주는 public 함수들을 사용하기 위한 통신채널
    public class AudioServiceBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();

        /* wakeMode를 설정해주는 이유는 오디오앱 특성상 백그라운드에서 음악이 재생되는게 일반적인 기능인데
        장기간 핸드폰을 사용하지 않으면 안드로이드 시스템이 휴면모드로 진입하게 될 수 있으며
        이럴 경우 cpu 사용을 최소화 하기 위해 재생중인 MediaPlayer에 문제가 생길 수 있음. */
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mp.start();
                sendBroadcast(new Intent(BroadcastActions.PREPARED)); // prepared 전송
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
                AudioApplication.getInstance().getServiceInterface().forward();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    // 재생컨트롤, 현재 재생할 오디오의 정보를 불러옴
    // 재생목록 기준으로 사용자로부터 재생할 목록의 position값을 받아서 현재 재생할 음악에 대한 정보를 불러와 AudioItem에 저장
    private void queryAudioItem(int position) {
        mCurrentPosition = position;
        long audioId = mAudioIds.get(position);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = {String.valueOf(audioId)};
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                mAudioItem = AudioAdapter.AudioItem.bindCursor(cursor);
            }
            cursor.close();
        }
    }

    // MediaPlayer를 재생가능한 상태로 만들어주는 함수
    private void prepare() {
        try {
            mMediaPlayer.setDataSource(mAudioItem.mDataPath);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 음악 종료
    private void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    // 재생목록 리스트
    public void setPlayList(ArrayList<Long> audioIds) {
        if (!mAudioIds.equals(audioIds)) {
            mAudioIds.clear();
            mAudioIds.addAll(audioIds);
        }
    }

    // 음악 재생
    public void play(int position) {
        queryAudioItem(position);
        stop();
        prepare();
    }

    public void play() {
        if (isPrepared) {
            mMediaPlayer.start();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
        }
    }

    // 음악 일시정지
    public void pause() {
        if (isPrepared) {
            mMediaPlayer.pause();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
        }
    }

    public void forward() {
        if (mAudioIds.size() - 1 > mCurrentPosition) {
            mCurrentPosition++; // 다음 포지션으로 이동.
        } else {
            mCurrentPosition = 0; // 처음 포지션으로 이동.
        }
        play(mCurrentPosition);
    }

    public void rewind() {
        if (mCurrentPosition > 0) {
            mCurrentPosition--; // 이전 포지션으로 이동.
        } else {
            mCurrentPosition = mAudioIds.size() - 1; // 마지막 포지션으로 이동.
        }
        play(mCurrentPosition);
    }

    public AudioAdapter.AudioItem getAudioItem() {
        return mAudioItem;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }


}
