package com.ecom.fyp2023.MiroWhiteBoardIntegration;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ecom.fyp2023.R;
import com.google.android.material.appbar.MaterialToolbar;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

import io.agora.board.fast.FastRoom;
import io.agora.board.fast.FastboardView;
import io.agora.board.fast.extension.FastResult;
import io.agora.board.fast.model.FastInsertDocParams;
import io.agora.board.fast.model.FastRegion;
import io.agora.board.fast.model.FastRoomOptions;

public class Board extends AppCompatActivity {

    private FastRoom fastRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().hide();

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setupFastboard();

        TextView textView = findViewById(R.id.insert);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addImageToBoard("https://5.imimg.com/data5/DV/ND/CX/SELLER-23177933/campus-sport-shoes.jpg",20,29);
            }
        });
    }

    private void setupFastboard() {
        FastboardView fastboardView = findViewById(R.id.fastboard_view);
        fastRoom = fastboardView.getFastboard().createFastRoom(getRoomOptions());
        fastRoom.join();
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    private FastRoomOptions getRoomOptions() {
        String appIdentifier = "WtuiYPshEe6D_PXT5qj0Fg/DuTZLQ54PfeZTA";
        String roomUUID = "87cad3f0fc1a11ee8f6b69560a95c9aa";
        String roomToken = "NETLESSROOM_YWs9cmF0cjlYU09nQ1hQU2tfUyZub25jZT1lMTRlZDg0MC1mYzJlLTExZWUtYTljMS1iOTkwZWNkNGI4YTkmcm9sZT0wJnNpZz01OGUxYjE3MWM3MDM1N2E2MzhkOThhNjkxMThiYzg4YzMwZGVmMzc0ODBlOGViN2Q5N2U4OTMxNDI4NzJkODUyJnV1aWQ9ODdjYWQzZjBmYzFhMTFlZThmNmI2OTU2MGE5NWM5YWE";
        String userId = "uidr";
        FastRegion region = FastRegion.US_SV;
        return new FastRoomOptions(appIdentifier, roomUUID, roomToken, userId, region);
    }

    // Add methods to add images, audio, video, and documents here
    private void addImageToBoard(String imageUrl, int width, int height) {
        fastRoom.insertImage(imageUrl, width, height);
    }

    private void addVideoToBoard(String videoUrl, String title) {
        fastRoom.insertVideo(videoUrl, title);
    }

    private void addDocumentToBoard(FastInsertDocParams params, FastResult<String> result) {
        fastRoom.insertDocs(params, result);
    }
}
