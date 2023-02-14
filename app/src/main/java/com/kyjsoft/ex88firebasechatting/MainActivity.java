package com.kyjsoft.ex88firebasechatting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kyjsoft.ex88firebasechatting.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btn.setOnClickListener(view -> clickBtn());
        binding.civProfile.setOnClickListener(view -> clickSelect());

        // 디바이스에 저장되어있는 account정보가 있는지 확인해보셈.
        loadData();
        if(G.name!=null) {
            binding.etName.setText(G.name);
            Glide.with(this).load(G.url).into(binding.civProfile);
            isFirst = false;
        }

    }

    void loadData(){

        SharedPreferences pref = getSharedPreferences("account", MODE_PRIVATE);
        G.name = pref.getString("name", null);
        G.url = pref.getString("url", null);

    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if(result.getResultCode()==RESULT_CANCELED) return;

            uri = result.getData().getData(); // getData() -> 한번만 하면 intent를 부른거임.
            Glide.with(MainActivity.this).load(uri).into(binding.civProfile);

            isChanged = true;
        }
    });

    Uri uri; // 버튼눌렀을 때 storage에 uri를 저장하기 위한 멤버변수

    void clickSelect(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultLauncher.launch(intent);

    }

    boolean isFirst = true; // 처음 앱을 실행하여 프로필 데이터가 없는가?
    boolean isChanged = false; // 프로필 사진 변경했는지 여부

    void clickBtn(){

        if(isFirst || isChanged){
            // 처음 이면 프로필 저장 해야함. 변경한 적이 없으면
            saveData();

        }else{ // 변경안하고 데이터가 있으면 실행되는 곳
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
            finish();
        }
    }

    void saveData(){
        if(uri == null) return; // 이미지 선택안하면 채팅 못하게 할거임.

        G.name = binding.etName.getText().toString();

        // 이미지 파일부터 업로드하기 -> 다운로드 URL이 생김 -> 서버에 있는 이미지 URL를 db에 저장
        // 서버(Storage)에 저장될 파일명이 중복되지 않도록 날짜를 이용하기
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = sdf.format(new Date()) + ".png"; // jpg를 png로 하는 건 무손실 반대는 파일 날아갈 수도 있음.

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference imgRef = firebaseStorage.getReference("profileImage/"+fileName);
        imgRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 업로드에 성공하면 참조객체한테 업로드된 파일의 url얻어오기
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) { // 요 uri가 다운로드 URL
                        G.url = uri.toString();
//                        Toast.makeText(MainActivity.this, "profile image save success \n" + G.url, Toast.LENGTH_SHORT).show();

                        // 1. firebaseFireStore
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        // 'profiles'라는 이름의 Collection참조 객체 만들기
                        CollectionReference profileRef = firebaseFirestore.collection("profiles");
                        // 닉네임을 Document명, 이미지경로 URL을 저장
                        HashMap<String,String> profile = new HashMap<>();
                        profile.put("profileUrl",G.url);
                        profileRef.document(G.name).set(profile);

                        // 2. shared preference -> 앱 껐다켜도 저장되게
                        SharedPreferences pref = getSharedPreferences("account" , MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("name", G.name);
                        editor.putString("url", G.url);
                        editor.commit();

                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });
            }
        });







    }


}