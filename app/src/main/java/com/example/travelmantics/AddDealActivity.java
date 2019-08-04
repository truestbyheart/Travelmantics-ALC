package com.example.travelmantics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AddDealActivity extends AppCompatActivity {
    private static final int PICTURE_RESULT = 42;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference  mDatabaseReference;
    EditText  txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    Button btnImage;
    TravelDeal deal;
    ImageView imageView;
    ProgressDialog progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference  = FirebaseUtil.mDatabaseReference;
        txtTitle  = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        btnImage = findViewById(R.id.btnImage);
        imageView = (ImageView) findViewById(R.id.image);
        progressBar = new ProgressDialog(this);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });


        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");

        if(deal ==  null){
            deal = new TravelDeal();
        }

        this.deal = deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this,"Deal saved",Toast.LENGTH_LONG).show();
                clean();
                BackToList();
                return true;
            case R.id.delete_menu:
                DeleteDeal();
                BackToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void clean() {
       txtPrice.setText("");
       txtDescription.setText("");
       txtPrice.setText("");
    }

    private void saveDeal() {
       deal.setTitle(txtTitle.getText().toString());
        deal.setDescription( txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());


        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }

    private void DeleteDeal(){
        if(deal.getId() == null){
            Toast.makeText(this,"Please save the deal before deleting",Toast.LENGTH_LONG).show();
        }else{
            mDatabaseReference.child(deal.getId()).removeValue();
            Toast.makeText(this,"Deal deleted",Toast.LENGTH_LONG).show();

            if(deal.getImageName()!= null && deal.getImageName().isEmpty() ==false) {
                StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
                picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("deleted", "Deleted successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }
    private void BackToList(){
        Intent intent = new Intent(this,ListActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =    getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);

        if(FirebaseUtil.isAdmin == true){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            enableText(true);
            findViewById(R.id.btnImage).setEnabled(true);
        } else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            enableText(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }

    private void enableText(boolean isEnabled){
      txtTitle.setEnabled(isEnabled);
      txtDescription.setEnabled(isEnabled);
      txtPrice.setEnabled(isEnabled);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStorageRef.child(((Uri) imageUri).getLastPathSegment());
            final UploadTask uploadTask = ref.putFile(imageUri);
            progressBar.setMessage("uploading image....");
            progressBar.show();


            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String pictureName = task.getResult().getPath();
                        deal.setImageUrl(downloadUri.toString());
                        deal.setImageName(pictureName);
                        showImage(downloadUri.toString());
                        progressBar.dismiss();
                    }
                }
            });
        }

    }

    public void showImage(String url){

if(url != null && url.isEmpty() == false) {
    int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    Picasso.with(this)
            .load(url)
            .resize(width,width*2/3)
            .centerCrop()
            .into(imageView);
}
    }
}
