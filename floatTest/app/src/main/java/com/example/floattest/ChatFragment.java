package com.example.floattest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class ChatFragment extends Fragment {

    ListView m_ListView;
    ChatAdapter m_Adapter;
    public ChatFragment(){

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment, container, false);

        //채팅 커스텀 어댑터
        m_Adapter = new ChatAdapter();

        //Listview 연결
        m_ListView = (ListView)v.findViewById(R.id.listView1);

        //어뎁터랑 view 연결
        m_ListView.setAdapter(m_Adapter);

        //이부분 동적으로 만들기
        m_Adapter.add("인균아",1);
        m_Adapter.add("돈좀빌려줘",1);
        m_Adapter.add("말만해",0);
        m_Adapter.add("5만원빌려줘",1);
        m_Adapter.add("고마워",1);
        m_Adapter.add("내일갚을게",1);
        m_Adapter.add("갚을필요없어",0);
        m_Adapter.add("2015/11/20",2);
        m_Adapter.add("ㅇㅋㅇㅋ",1);
        m_Adapter.add("ㅅㄱㅅㄱ",1);


        return v;
    }
}