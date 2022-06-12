package com.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private String TextURL;
    private String[] TextContents;
    private String ip = "172.16.10.154"; // IP 번호 -> 이걸 자동으로 얻어야 하나..?
    private int port = 6000; // port 번호

    private Socket socket;
    private static final int FILE_REQUEST_CODE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() 메서드 호출됨.");

        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = parseSmsMessage(bundle);
        if (messages != null && messages.length > 0) {
            String sender = messages[0].getOriginatingAddress();
            Log.i(TAG, "SMS sender : " + sender);

            String contents = messages[0].getMessageBody();
            Log.i(TAG, "SMS contents : " + contents);

            TextURL = regexURL(contents);
            //TextContents = regexContens(contents);

            //Log.i(TAG, "SMS contents : " + TextContents);

            SendToServer(sender, TextURL, TextContents);
        }
    }

    public String regexURL(String contents) {
        String text = null;
        Pattern p = Pattern.compile("(http|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?");
        Matcher m = p.matcher(contents);

        while (m.find()) {
            System.out.println("URL : " + m.group());
            text = m.group();
        }
        return text;
    }

    /*public String[] regexContens(String contents){
        String text2 = null;
        String[] arr = null;
        Pattern p = Pattern.compile("([가-힣])+");
        Matcher m = p.matcher(contents);

        while (m.find()) {
            System.out.println("내용 : " + m.group());
            text2 = m.group();
            arr = text2.split("");

        }
        System.out.println("내용 : " + arr);
        return arr;
    }*/

    public void SendToServer(String sender, String TextURL, String[] contents){
        Thread thread = new Thread() {
            public void run() {
                // 서버 접속
                try {
                    socket = new Socket(ip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                try {
                    OutputStream out = socket.getOutputStream();
                    // socket의 OutputStream 정보를 OutputStream out에 넣은 뒤
                    PrintWriter writer = new PrintWriter(out, true);
                    // PrintWriter에 위 OutputStream을 담아 사용

                    writer.println(sender);
                    // 클라이언트에서 서버로 전화번호 보내기
                    Log.d("Client", "서버로 보냄");

                    OutputStream out2 = socket.getOutputStream();
                    // socket의 OutputStream 정보를 OutputStream out에 넣은 뒤
                    PrintWriter writer2 = new PrintWriter(out2, true);
                    // PrintWriter에 위 OutputStream을 담아 사용

                    writer2.println(sender);
                    // 클라이언트에서 서버로 전화번호 보내기

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }

                try {
                    OutputStream out = socket.getOutputStream();
                    // socket의 OutputStream 정보를 OutputStream out에 넣은 뒤
                    PrintWriter writer = new PrintWriter(out, true);
                    // PrintWriter에 위 OutputStream을 담아 사용

                    writer.println(TextURL);
                    // 클라이언트에서 서버로 URL 보내기

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }

                // 메시지 내용 텍스트 파일저장
                /*File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test"); // 저장 경로

                if(!saveFile.exists()){ // 폴더 없을 경우
                    saveFile.mkdir(); // 폴더 생성
                }

                try {
                    BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/Content.txt", true));
                    buf.append(contents); // 파일 쓰기
                    buf.newLine(); // 개행
                    buf.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //sendFile();*/

            }
        };
        thread.start();
    }

    /*private void sendFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        startActivityForResult(intent, FILE_REQUEST_CODE);

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    System.out.println(filename);

                    //출력스트림 구하기

                    File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    System.out.println(downloadFolder);
                    Log.e("Download", "" + downloadFolder.getAbsolutePath());

                    File dir = new File(downloadFolder.getAbsolutePath() + "/" + filename);


                    System.out.println("파일 전송 시작");
                    // 보낼 파일의 입력 스트림 객체 생성
                    FileInputStream fis = new FileInputStream(dir);
                    //FileInputStream fis = openFileInput(filename);


                    // 파일의 내용을 보낸다
                    byte []b = new byte[1024];

                    int n =0;
                    while((n=fis.read(b))>0){
                        os.write(b, 0, n);
                        System.out.println(n + "bytes 전송");
                    }

                    System.out.println("파일 전송 완료");


                    sock[0].close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connecting...");
            }
        });
        }
    }*/

    private SmsMessage[] parseSmsMessage(Bundle bundle) {
        Object[] objs = (Object[]) bundle.get("pdus");

        SmsMessage[] messages = new SmsMessage[objs.length];
        int smsCount = objs.length;
        for (int i = 0; i < smsCount; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String format = bundle.getString("format");
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i], format);
            } else {
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i]);
            }
        }

        return messages;
    }
}