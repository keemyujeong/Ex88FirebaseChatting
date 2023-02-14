package com.kyjsoft.ex88firebasechatting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kyjsoft.ex88firebasechatting.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    String chatName = "chat"; // Firebase Firestore DB에 저장될 컬렉션이름이 될거임. -> 이건 나중에 main에서 et으로 채팅방이름 입력 받으면 됨.
    FirebaseFirestore firebaseFirestore;
    CollectionReference chatRef;
    ArrayList<MessageItem> items = new ArrayList<MessageItem>();
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 제목줄 Actionbar 에 제목 글씨와 서브 제목 글씨 설정하기
        getSupportActionBar().setTitle(chatName);
        getSupportActionBar().setSubtitle(G.name + " 외 1명");

        adapter = new ChatAdapter(this, items);
        binding.recyclerView.setAdapter(adapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        chatRef = firebaseFirestore.collection(chatName);

        // chatCollection에 새로운 데이터가 변경되는 것을 인지하는 리스너를 추가
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() { // 얘는 처음 데이터가 없어도 데이터가 변경되었다고 인지함.
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //  변경된 항목들만 찾아와라. 매번 전원 다 찾지말고
                List<DocumentChange> documentChangeList = value.getDocumentChanges();
                for(DocumentChange documentChange: documentChangeList){
                    // 변경된 document의 데이터를 촬영한 Snapshop이 필요. documentChange는 바뀐 정보만 가지고 있는 애
                    DocumentSnapshot snapshot = documentChange.getDocument();

                    // document 안에 있는 field값들 얻어오기
                    Map<String, Object> msg = snapshot.getData();
                    String nickname = msg.get("name").toString();
                    String message = msg.get("message").toString();
                    String profileUrl = msg.get("url").toString();
                    String time = msg.get("time").toString();

                    // 읽어드린 데이터를 리사이클러뷰로 보여주기 위해 arrayList로
                    MessageItem item = new MessageItem(nickname, message, profileUrl, time);
                    items.add(item);

                    adapter.notifyItemInserted(items.size()-1);
                    binding.recyclerView.scrollToPosition(adapter.getItemCount()-1); // 리사이클러뷰의 스크롤 위치를 마지막으로

//                    Toast.makeText(ChatActivity.this, msg.size() + " -> success", Toast.LENGTH_SHORT).show();



                }
            }
        });

        binding.btnSend.setOnClickListener(view -> clickSend());


    }



    void clickSend(){

        // DB에 저장할 데이터들 (닉네임, 메세지, 프로필 이미지 URL, 작성시간)
        String nickname = G.name;
        String message = binding.etMsg.getText().toString();
        String profileUrl = G.url;
        String time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE);

        chatRef.document("MSG_" + System.currentTimeMillis()).set(new MessageItem(nickname, message,profileUrl, time)); // 아이템 객체 통째로 설정

        // 다음 메세지 입력을 위해 입력 없에기
        binding.etMsg.setText("");
        // 소프트 키보드 없에기
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);





    }
}