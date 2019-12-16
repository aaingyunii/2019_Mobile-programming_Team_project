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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Date;

public class ChatFragment extends Fragment {

    ListView m_ListView;
    ChatAdapter m_Adapter;
    MyDBHandler myDBHandler;
    int position;
    String packName;
    ArrayList tab_list;


    public ChatFragment() {

    }

    public void setInfo(int position, ArrayList tab_list, String packName) {
        this.position = position;
        this.packName = packName;
        this.tab_list = tab_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment, container, false);

        //connect listview
        m_ListView = (ListView) v.findViewById(R.id.listView1);

        //chat custom adapter
        m_Adapter = new ChatAdapter();

        //scroll always goes down
        m_ListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                m_ListView.setSelection(m_ListView.getCount() - 1);
            }
        });

        //connect adapter with view
        m_ListView.setAdapter(m_Adapter);

        myDBHandler = MyDBHandler.open(getActivity(), "chatlog");

        if (position == 0) {
            //Update first page
            listUdpate(packName, tab_list.get(position).toString());
        }
        final EditText editText = v.findViewById(R.id.editText1);
        ImageButton imageButton = v.findViewById(R.id.button1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        imageButton.setLayoutParams(params);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString();
                if (msg != null && msg.length() != 0) {
                    try {
                        try {
                            //reply action using notification listener
                            MyNotificationListener.replyModel.get(packName + tab_list.get(position)).sendReply(getActivity(), msg);
                            Date date = new Date(System.currentTimeMillis());
                            myDBHandler.insert(packName, 999999999, date.getTime(), tab_list.get(position).toString(), msg, "");

                            editText.setText(null);
                            listUdpate(packName, tab_list.get(position).toString());
                        } catch (NullPointerException e) {
                            Toast.makeText(getActivity(), "cannot reply to notification", Toast.LENGTH_SHORT);
                        }
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return v;
    }

    // When updating, sending in database, show the fragment name to be updated
    public void listUdpate(String packName, String tabName) {
        Cursor cursor = myDBHandler.select(packName);

        //make new chat custom adapter and connect
        m_Adapter = new ChatAdapter();

        String checkNull = "";
        while (cursor.moveToNext()) {
            try {
                if (cursor.getString(3).equals(tabName)) {
                    String chat_message = cursor.getString(4);
                    checkNull += chat_message;
                    if ((!cursor.getString(5).equals(""))) {
                        m_Adapter.add(cursor.getString(5) + " : " + chat_message, 0);
                    } else {
                        if (cursor.getInt(1) == 999999999) {
                            m_Adapter.add(chat_message, 1);
                        } else {
                            m_Adapter.add(chat_message, 0);
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("No title", "okay");
            }
        }

        //connect view with adapter
        if (checkNull.length() != 0) {
            //scroll always goes down
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