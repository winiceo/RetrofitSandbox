package com.studio1r.retrofitsandbox.api.clients;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.studio1r.retrofitsandbox.Constants;
import com.studio1r.retrofitsandbox.api.APIConfiguration;
import com.studio1r.retrofitsandbox.api.model.VideoDetailItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by nelsonramirez on 8/29/14.
 */
public class VideoDetailApiClient {
    private final VideoDetailRetrofitService mVideoDetailClient;
    private WeakReference<Context> context;
    private boolean isMock;

    public interface VideoDetailRetrofitService {

        /**
         * http://{host}/{version}/{sitecode}/video/{video_identifier}
         *
         * @return returns details for a single video
         */
        @GET("/video/{video_identifier}")
        Observable<VideoDetailItem> getVideo(@Path("video_identifier") String id,
                                             @Query("authorization") String auth);
    }


    /**
     * This constructor is used for dev only.
     * It will return mocked objects pulled from resources
     *
     * @param context
     */
    public VideoDetailApiClient(Context context) {
        if (Constants.USE_MOCK_DATA) {
            this.context = new WeakReference<Context>(context);
            isMock = true;
            mVideoDetailClient = null;
        } else {
            mVideoDetailClient = APIConfiguration.getRestAdapter(context).create(
                    VideoDetailRetrofitService.class);
        }
    }


    public Observable<VideoDetailItem> getVideoDetail(String id) {
        if (isMock) {
            return new MockVideoDetail(this.context.get()).getVideo(id, "");
        } else {
            //auth needs to be done on the fly to emcompass full url
            return mVideoDetailClient.getVideo(id, getAuthHash(id));
        }
    }

    //TODO move to Utils or APIConfiguration.
    private String getAuthHash(String id) {
        return APIConfiguration.encode(APIConfiguration.getEndpoint() + "/video/" + id);
    }

    /**
     * MOCK implementation
     */

    private static class MockVideoDetail implements VideoDetailRetrofitService {

        private VideoDetailItem mVideoDetailItem;

        public MockVideoDetail(Context context) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(context.
                        getAssets().open("videodetail.json")));
                Gson gson = new Gson();
                mVideoDetailItem = gson.fromJson(br, VideoDetailItem.class);
            } catch (IOException e) {
                Log.e("ERR", "could not load file!!");
                e.printStackTrace();
            }
        }

        @Override
        public Observable<VideoDetailItem> getVideo(@Path("video_identifier") String id,
                                                    @Query("authorization") String auth) {
            //read json file
            return Observable.from(mVideoDetailItem);
        }
    }

}
