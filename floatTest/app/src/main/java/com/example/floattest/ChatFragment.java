package com.example.floattest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class ChatFragment extends Fragment {
    public ChatFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = (TextView)getActivity().findViewById(R.id.textt);
        tv.setText("hello");
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }
}