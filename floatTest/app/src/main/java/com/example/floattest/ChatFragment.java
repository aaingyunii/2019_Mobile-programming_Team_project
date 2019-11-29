package com.example.floattest;

import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Date;

public class ChatFragment extends Fragment{

    ListView m_ListView;
    ChatAdapter m_Adapter;
    MyDBHandler myDBHandler ;
    int position ;
    String packName ;
    ArrayList tab_list;



    public ChatFragment(){

    }
    public void setInfo(int position,ArrayList tab_list,String packName){
        this.position = position;
        this.packName = packName;
        this.tab_list = tab_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment, container, false);

        //Listview 연결
        m_ListView = (ListView)v.findViewById(R.id.listView1);

        //채팅 커스텀 어댑터
        m_Adapter = new ChatAdapter();

        //스크롤 항상 아래로
        m_ListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                m_ListView.setSelection(m_ListView.getCount() - 1);
            }
        });

        //어뎁터랑 view 연결
        m_ListView.setAdapter(m_Adapter);

        myDBHandler = MyDBHandler.open(getActivity(),"chatlog");

        if(position==0){
            //이부분 해결해야 한다 -> 첫페이지 갱신하기
            listUdpate(packName,tab_list.get(position).toString());
        }
        final EditText editText = v.findViewById(R.id.editText1);
        ImageButton imageButton = v.findViewById(R.id.button1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100,100);
        imageButton.setLayoutParams(params);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString();
                if(msg != null && msg.length()!=0) {
                    try {
                        Date date = new Date(System.currentTimeMillis());
                        myDBHandler.insert(packName,999999999,date.getTime(),tab_list.get(position).toString(),msg,"");
                        MyNotificationListener.replyModel.get(packName+tab_list.get(position)).sendReply(getActivity(),msg);
                        editText.setText(null);
                        listUdpate(packName,tab_list.get(position).toString());
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        });




        return v;


    }


    //데이터베이스에서 업데이트, 전달할때 업데이트할 프래그먼트 이름 알려주기ㅣ
    public void listUdpate(String packName, String tabName){

        Cursor cursor = myDBHandler.select(packName);

        //채팅 커스텀 어댑터 새로 만들어서 연결
        m_Adapter = new ChatAdapter();


        String checkNull = "";
        while (cursor.moveToNext()) {
            //if(cursor.getInt(1)==2 && cursor.getString(3).equals(tabName)){
            try{
            if(cursor.getString(3).equals(tabName)){
                String chat_message = cursor.getString(4);
                checkNull += chat_message;
                if((!cursor.getString(5).equals(""))){
                    m_Adapter.add(cursor.getString(5)+" : "+chat_message,0);
                }else{
                    if(cursor.getInt(1)==999999999){
                        m_Adapter.add(chat_message,1);
                    }else{
                        m_Adapter.add(chat_message,0);
                    }
                }
            }
            }catch(Exception e){Log.i("No title","dz");}
        }
        //어뎁터랑 view 연결
        if(checkNull.length()!=0){
            //스크롤 항상 아래로
            m_ListView.post(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    m_ListView.setSelection(m_ListView.getCount() - 1);
                }
            });
            m_ListView.setAdapter(m_Adapter);
        }


    }



}