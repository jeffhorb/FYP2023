package com.ecom.fyp2023.MiroWhiteBoardIntegration;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface MiroApiService {
    @GET("boards")
    Call<List<Whiteboard>> getBoards(@Header("Authorization") String accessToken);

    @POST("boards")
    Call<Whiteboard> createBoard(@Header("Authorization") String accessToken);
}


