package com.example.floattest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ChatFragment extends Fragment{

    ListView m_ListView;
    ChatAdapter m_Adapter;
    MyDBHandler myDBHandler ;
    int position ;



    public ChatFragment(){

    }
    public void getPosition(int position){
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment, container, false);

        //Listview 연결
        m_ListView = (ListView)v.findViewById(R.id.listView1);

        //채팅 커스텀 어댑터
        m_Adapter = new ChatAdapter();

        //어뎁터랑 view 연결
        m_ListView.setAdapter(m_Adapter);

        myDBHandler = MyDBHandler.open(getActivity(),"chatlog");
        //listUdpate("comkakaotalk");
        return v;


    }


    //데이터베이스에서 업데이트, 전달할때 업데이트할 프래그먼트 이름 알려주기ㅣ
    public void listUdpate(String packName, String tabName){
        Log.i("updateconfirm","update!");

        Cursor cursor = myDBHandler.select(packName);

        //채팅 커스텀 어댑터 새로 만들어서 연결
        m_Adapter = new ChatAdapter();

        //어뎁터랑 view 연결
        m_ListView.setAdapter(m_Adapter);


        while (cursor.moveToNext()) {
            if(cursor.getInt(1)==2 && cursor.getString(3).equals(tabName)){
                String chat_message = cursor.getString(4);
                m_Adapter.add(chat_message,0);
            }
        }

        //데이터베이스에서 업데이트 하는 메소드 만들기.
        //어뎁터 업데이트
        m_Adapter.notifyDataSetChanged();
        //프래그먼트 화면 업데이트
        //fragmentUpdate();
    }

    //프래그먼트 화면 업데이트 메소드
    public void fragmentUpdate(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }


}