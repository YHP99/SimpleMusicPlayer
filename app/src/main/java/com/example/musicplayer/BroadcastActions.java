package com.example.musicplayer;

// AudioService에서 음악이 변경되거나 재생될 때, 일시정지할때마다 Broadcast 메시지를 전송하도록 구현
public class BroadcastActions {
    public final static String PREPARED = "PREPARED";
    public final static String PLAY_STATE_CHANGED = "PLAY_STATE_CHANGED";
}
