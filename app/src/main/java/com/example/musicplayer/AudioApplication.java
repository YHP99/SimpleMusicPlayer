package com.example.musicplayer;

import android.app.Application;

// AudioServiceInterface 클래스에 대한 객체생성은 앱이 실행되고 Process가 생성될 때 호출되는 Application에서 단 한번 해주는게 좋다.
public class AudioApplication extends Application {
    private static AudioApplication mInstance;
    private AudioServiceInterface mInterface;

    // AudioServiceInterface 객체를 생성 및 BindService 할 수 있도록 구현
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mInterface = new AudioServiceInterface(getApplicationContext());
    }

    // mInstance 변수를 static으로 선언하여 어느위치에서도 접근할 수 있도록 getIntance() 함수를 만듬
    public static AudioApplication getInstance() {
        return mInstance;
    }

    public AudioServiceInterface getServiceInterface() {
        return mInterface;
    }
}

