package com.example.musicplayer;

// 일반 View들과는 다르게 RemoteViews에 클릭이벤트를 연결할 경우 PendingIntent를 사용하게된다. (버튼을 클릭할 때 호출)
// 각각의 버튼들은 Intent Action을 갖고있는 PendingIntent를 갖고 있으며, Intent Action을 정의하는 클래스이다.
public class CommandActions {
    public final static String REWIND = "REWIND";
    public final static String TOGGLE_PLAY = "TOGGLE_PLAY";
    public final static String FORWARD = "FORWARD";
    public final static String CLOSE = "CLOSE";
}
