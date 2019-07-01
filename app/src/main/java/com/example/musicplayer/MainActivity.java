package com.example.musicplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int LOADER_ID = 0x001;

    private RecyclerView mRecyclerView;
    private AudioAdapter mAdapter;

    private ImageView mImgAlbumArt;
    private TextView mTxtTitle;
    private ImageButton mBtnPlayPause;
    private CountDownTimer countDownTimer;

    // AudioService에서 전송한 Broadcast 액션값을 받아서 UI에 적용
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OS가 Marshmallow 이상일 경우 권한체크를 해야함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                // READ_EXTERNAL_STORAGE 에 대한 권한이 있음.
                getAudioListFromMediaDatabase();
            }
        }
        // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else{
            getAudioListFromMediaDatabase();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new AudioAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        // MiniPlayer UI를 셋팅
        mImgAlbumArt = (ImageView) findViewById(R.id.img_albumart);
        mTxtTitle = (TextView) findViewById(R.id.txt_title);
        mBtnPlayPause = (ImageButton) findViewById(R.id.btn_play_pause);
        findViewById(R.id.lin_miniplayer).setOnClickListener(this);
        findViewById(R.id.btn_rewind).setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        findViewById(R.id.btn_forward).setOnClickListener(this);

        // Broadcast 등록
        registerBroadcast();
        updateUI();
    }

    // 액션바 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // 액션바 이벤트 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sleep :
                alertDialog_sleep();
                return true ;

            case R.id.action_nosleep :
                alertDialog_nosleep();
                return true ;

            case R.id.action_information :
                alertDialog_information();
                return true ;

            case R.id.action_contact :
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/younghoon.park.3150"));
                startActivity(intent);
                return true ;

            default :
                return false;
        }
    }

    // countDownTimer를 활용한 취침모드
    public void countDownTimer(){
        countDownTimer = new CountDownTimer(7200 * 1000, 1800 * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {      // 정해놓은 시간마다 이벤트 발생
                Toast.makeText(getApplicationContext(), "취침모드 종료까지\n"+ millisUntilFinished / 3600000 + "시간 "
                        + (millisUntilFinished % 3600000) / 60000 + "분 "
                        + (millisUntilFinished % 60000) / 1000 + "초 남았습니다.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                AudioApplication.getInstance().getServiceInterface().pause();
                Toast.makeText(getApplicationContext(), "잘자요~♥", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    // 취침모드 클릭 시 alertDialog 창 띄움
    public void alertDialog_sleep(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // 제목셋팅
        alertDialogBuilder.setTitle("취침 모드");

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("취침 모드를 실행하시겠습니까?\n실행 시 2시간 후 음악을 중지합니다.")
                .setCancelable(false)
                .setPositiveButton("실행",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                countDownTimer();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    // 취침모드종료 클릭 시 alertDialog 창 띄움
    public void alertDialog_nosleep(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // 제목셋팅
        alertDialogBuilder.setTitle("취침 모드 종료");

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("취침 모드를 정말로 종료하시겠습니까?\n<일찍 자야 키크죠~!>")
                .setCancelable(false)
                .setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                countDownTimer.cancel();
                                Toast.makeText(getApplicationContext(), "취침 모드를 종료합니다.", Toast.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    // 앱 정보 클릭 시 alertDialog 창 띄움
    public void alertDialog_information(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // 제목셋팅
        alertDialogBuilder.setIcon(R.drawable.music);
        alertDialogBuilder.setTitle("Music Player");

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("Version: 1.0.0\n\n©2019  3CP-B  10조.")
                .setCancelable(true)
                .setNegativeButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    // 권한 획득 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // READ_EXTERNAL_STORAGE 에 대한 권한 획득.
            getAudioListFromMediaDatabase();
            Toast.makeText(this, "권한 승인 완료", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "권한 승인 실패", Toast.LENGTH_LONG).show();
        }
    }

    // 권한 획득이 이루어졌다면 LoaderManager를 통해 Android Media Database의 내용을 받아오는 함수를 작성 (Query)
    private void getAudioListFromMediaDatabase() {
        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            // 실제 DB의 값을 조회하는 Query 내용을 작성
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                // Media Database에서 조회하고자 하는 컬럼ID
                String[] projection = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                };

                // 조건문 (db 에서 where절과 동일하며 IS_MUSIC값이 1인 내용만 조회)
                String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";

                // order 조건 값 (오디오 타이틀 기준으로 로케일 순으로 정렬 됩니다. 특수문자->한글->영어)
                String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
                return new CursorLoader(getApplicationContext(), uri, projection, selection, null, sortOrder);
            }

            // 조회 결과가 Cursor를 통해 저장되어 리턴됨
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapCursor(data);
            }


            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapCursor(null);
            }
        });
    }

    // onClickListener를 등록했기 때문에 onClick를 오버라이딩해줌
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_miniplayer:
                // 플레이어 화면으로 이동할 코드가 들어갈 예정
                break;
            case R.id.btn_rewind:
                // 이전곡으로 이동
                AudioApplication.getInstance().getServiceInterface().rewind();
                break;
            case R.id.btn_play_pause:
                // 재생 또는 일시정지
                AudioApplication.getInstance().getServiceInterface().togglePlay();
                break;
            case R.id.btn_forward:
                // 다음곡으로 이동
                AudioApplication.getInstance().getServiceInterface().forward();
                break;
        }
    }

    // Broadcast 해제
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    // UI 업데이트 함수
    private void updateUI() {
        if (AudioApplication.getInstance().getServiceInterface().isPlaying()) {
            mBtnPlayPause.setImageResource(R.drawable.pause);
        } else {
            mBtnPlayPause.setImageResource(R.drawable.play);
        }
        AudioAdapter.AudioItem audioItem = AudioApplication.getInstance().getServiceInterface().getAudioItem();
        if (audioItem != null) {
            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioItem.mAlbumId);
            Picasso.with(getApplicationContext()).load(albumArtUri).error(R.drawable.music).into(mImgAlbumArt);
            mTxtTitle.setText(audioItem.mTitle);
        } else {
            mImgAlbumArt.setImageResource(R.drawable.music);
            mTxtTitle.setText("재생중인 음악이 없습니다.");
        }
    }

    public void registerBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.PREPARED);
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void unregisterBroadcast(){
        unregisterReceiver(mBroadcastReceiver);
    }

}
