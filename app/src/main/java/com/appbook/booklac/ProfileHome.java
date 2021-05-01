package com.appbook.booklac;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appbook.booklac.Adapters.PostAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileHome extends Fragment {


    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    ProgressDialog pd;
    View parentHolder;

    // ---------- posting post part ----------

    EditText bookName, bookAuthor, bookDescription;
    ImageView bookImg, cameraIcon, showImg;
    Button postBookBtn;
    RelativeLayout postImgContainer;
    Uri img_uri = null;
    String name, email, phone, uid, dp, currentUserId;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMG_PICK_CAMERA_CODE = 300;
    private static final int IMG_PICK_GALLERY_CODE = 400;

    String cameraPermissions[] = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    String storagePermissions[] = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};;


    // ---------- post part ----------

    RecyclerView postsRecyclerView;
    ArrayList<Books> booksArrayList;
    PostAdapter postAdapter;
    private DatabaseReference postsRef;

    // -------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentHolder = inflater.inflate(R.layout.fragment_profile_home, container, false);
        pd = new ProgressDialog(getContext());
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        checkUserStatus();
        Query query = dbRef.orderByChild("email").equalTo(email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()){
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    phone = "" + ds.child("phone").getValue();
                    dp = "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // ---------- posting post part ----------

        bookName = parentHolder.findViewById(R.id.book_name_in);
        bookAuthor = parentHolder.findViewById(R.id.book_author_in);
        bookDescription = parentHolder.findViewById(R.id.book_description_in);
        bookImg = parentHolder.findViewById(R.id.book_img_in);
        cameraIcon = parentHolder.findViewById(R.id.camera_icon);
        showImg = parentHolder.findViewById(R.id.show_camera);
        postBookBtn = parentHolder.findViewById(R.id.post_btn);
        postImgContainer = parentHolder.findViewById(R.id.post_btn_img_container);
        bookImg.setVisibility(View.GONE);
        cameraIcon.setVisibility(View.GONE);
        postImgContainer.getLayoutParams().height = 200;

        // -------------------------------



        // ---------- post part ----------

        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserId = auth.getUid();

        postsRecyclerView = parentHolder.findViewById(R.id.posts_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(linearLayoutManager);

        booksArrayList = new ArrayList<>();
        loadPosts();



        // -------------------------------



        showImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookImg.setVisibility(View.VISIBLE);
                cameraIcon.setVisibility(View.VISIBLE);
                postImgContainer.getLayoutParams().height = 260;
            }
        });

        // get image from gallery
        bookImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showImgPickDialog();
            }
        });


        // post button clickListener
        postBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String bkName = bookName.getText().toString().trim();
                String bkAuthor = bookAuthor.getText().toString().trim();
                String bkDescription = bookDescription.getText().toString().trim();
                if(TextUtils.isEmpty(bkName)){
                    Toast.makeText(getContext(), "Enter Book name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(bkAuthor)){
                    Toast.makeText(getContext(), "Enter Book author", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(bkDescription)){
                    Toast.makeText(getContext(), "Enter Book description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(img_uri == null){

                    uploadData(bkName, bkAuthor, bkDescription, "noImage");
                }
                else {
                    //File file = new File(SiliCompressor.with(getContext()).compress(FileUtils.getPath(getContext(),
                           // img_uri), new File(getContext().getCacheDir(), "temp")));
                    //img_uri = Uri.fromFile(file);
                    uploadData(bkName, bkAuthor, bkDescription, String.valueOf(img_uri));
                }

            }
        });

        return parentHolder;
    }

    private void loadPosts() {

        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("Posts");
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                booksArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){

                    ds.child("pBookAuthor").getValue();
                    Books books = new Books(
                            ds.child("pBookAuthor").getValue(String.class),
                            ds.child("pBookName").getValue(String.class), 
                            ds.child("pBookbImg").getValue(String.class),
                            ds.child("pBookbkDesc").getValue(String.class),
                            ds.child("pId").getValue(String.class),
                            ds.child("pTime").getValue(String.class),
                            ds.child("uDp").getValue(String.class),
                            ds.child("uEmail").getValue(String.class),
                            ds.child("uName").getValue(String.class),
                            ds.child("uPhone").getValue(String.class),
                            ds.child("uid").getValue(String.class)

                    );
                    booksArrayList.add(books);
                    Intent intent = new Intent();
                    postAdapter = new PostAdapter(getContext(), booksArrayList);
                    postsRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // ---------- post part ----------



    // -------------------------------


    // ---------> child(uid) has been added <----------
    private void uploadData(final String bkName, final String bkAuthor, final String bkDescription, final String uri) {

        pd.setMessage("Posting ..");
        pd.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;
        if(!uri.equals("noImage")){
            StorageReference sRef = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            sRef.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pBookName", bkName);
                                hashMap.put("pBookAuthor", bkAuthor);
                                hashMap.put("pBookbkDesc", bkDescription);
                                hashMap.put("pBookbImg", downloadUri);
                                hashMap.put("pTime", timeStamp);
                                hashMap.put("uPhone", phone);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(getContext(), "post Published", Toast.LENGTH_SHORT).show();
                                                bookName.setText("");
                                                bookAuthor.setText("");
                                                bookDescription.setText("");
                                                bookImg.setImageURI(null);
                                                cameraIcon.setVisibility(View.VISIBLE);
                                                img_uri = null;
                                                bookImg.setVisibility(View.GONE);
                                                cameraIcon.setVisibility(View.GONE);
                                                postImgContainer.getLayoutParams().height = 200;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pBookName", bkName);
            hashMap.put("pBookAuthor", bkAuthor);
            hashMap.put("pBookbkDesc", bkDescription);
            hashMap.put("pBookbImg", "noImg");
            hashMap.put("pTime", timeStamp);
            hashMap.put("uPhone", phone);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getContext(), "post Published", Toast.LENGTH_SHORT).show();
                            bookName.setText("");
                            bookAuthor.setText("");
                            bookDescription.setText("");
                            bookImg.setImageURI(null);
                            cameraIcon.setVisibility(View.VISIBLE);
                            img_uri = null;
                            bookImg.setVisibility(View.GONE);
                            cameraIcon.setVisibility(View.GONE);
                            postImgContainer.getLayoutParams().height = 200;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImgPickDialog() {

        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i == 0){

                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }

                if(i == 1){

                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void pickFromGallery() {


        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMG_PICK_GALLERY_CODE);


    }

    private void pickFromCamera() {

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Desc");
        ContentResolver resolver = getActivity().getContentResolver();
        img_uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);
        startActivityForResult(intent, IMG_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){

        boolean result = ActivityCompat.checkSelfPermission
                (getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){

        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ActivityCompat.checkSelfPermission
                (getContext(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ActivityCompat.checkSelfPermission
                (getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){

        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getContext(), "Camera & Storage both permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
            break;

            case STORAGE_REQUEST_CODE:{

                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getContext(), "Storage permission necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == IMG_PICK_GALLERY_CODE){
                cameraIcon.setVisibility(View.GONE);
                img_uri = data.getData();
                bookImg.setImageURI(img_uri);
            }
            else if(requestCode == IMG_PICK_CAMERA_CODE){
                cameraIcon.setVisibility(View.GONE);
                bookImg.setImageURI(img_uri);
            }
        }
    }

    private void checkUserStatus(){
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null){
            email = firebaseUser.getEmail();
            uid = firebaseUser.getUid();

        }
        else {
            startActivity(new Intent(getContext(), MainActivity.class));

        }
    }
}