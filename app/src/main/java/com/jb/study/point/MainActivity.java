package com.jb.study.point;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.jb.study.point.authentication.LoginActivity;
import com.jb.study.point.authentication.UserInterface;
import com.jb.study.point.helper.SessionManager;
import com.jb.study.point.payment.PaymentActivity;
import com.jb.study.point.payment.SubscriptionActivity;
import com.jb.study.point.youtube.model.playlist.PlaylistModel;
import com.jb.study.point.youtube.model.search.SearchModel;
import com.jb.study.point.youtube.network.YoutubeAPI;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ImageView nav_button, back_nav, status_icon;
    private CircleImageView profile_pic;
    private ImageView thumbnail1, thumbnail2, thumbnail3;
    private LinearLayout payment_layout, sign_out, profile, subscription;
    private TextView heading_name, name, subscription_status, showAll, practiceShowAll, premiumShowAll, visitChannel, exploreVideos, share;
    private DrawerLayout drawerLayout;
    private String search_videoId1, search_videoId2, search_videoId3;
    private String playlist_videoId1, playlist_videoId2, playlist_videoId3;
    private HorizontalScrollView horizontalScrollView;
    private ProgressBar progressBar;
    private SessionManager session;
    private CardView practice_beo, practice_history, practice_tgt;
    private CardView premium_mock_test, premium_history, premium_practice_sets;
    private CardView tgt_playlist, history_playlist, net_playlist;
    private boolean getStatus = false;
    private ConstraintLayout premium_card, demo_card;
    private AlertDialog dialog;
    private TextView feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_nav_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();

        session = new SessionManager(this);

        getCurrentUserDetails(getSharedPreferences("SHARED_PREFS", MODE_PRIVATE).getString("email", ""));

        getNewestJson();

        clickListeners();
    }

    private void getCurrentUserDetails(String user_email) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        @SuppressLint("HardwareIds") Call<String> call = api.getCurrentUser(user_email);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonresponse = response.body();
                        try {
                            saveInfo(jsonresponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(MainActivity.this, "Nothing returned", Toast.LENGTH_LONG).show();
                    }
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Toast.makeText(MainActivity.this, jsonObject.getJSONObject("errors").getJSONArray("email").get(0).toString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clickListeners() {
        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
                //startActivity(new Intent(MainActivity.this, NavBarActivity.class));
            }
        });

        back_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        payment_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PaymentActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        thumbnail1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("videoId", search_videoId1).putExtra("type", "search").putExtra("section", "new"));
            }
        });

        thumbnail2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("videoId", search_videoId2).putExtra("type", "search").putExtra("section", "new"));
            }
        });

        thumbnail3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("videoId", search_videoId3).putExtra("type", "search").putExtra("section", "new"));
            }
        });

        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("videoId", search_videoId1).putExtra("type", "search").putExtra("section", "new"));
            }
        });

        practice_beo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1QbwbMrMtmhC4bgEOzgfoNJ"));
            }
        });

        practice_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1TqPZwqQjWrGDJ-UYPIHznv"));
            }
        });

        practice_tgt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1QEMbb8nzzNrQZ5DW5l8WvZ"));
            }
        });

        practiceShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AllPlaylistActivity.class).putExtra("type", "demo"));
            }
        });

        premium_mock_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1SpHIc_yLTT4HlaU2xRYxIz"));
            }
        });

        premium_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1RD58QcX-9Us1f_2ipMViKL"));
            }
        });

        premium_practice_sets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1Qx6E_7hAMdorOq6ZZyAbPp"));
            }
        });

        tgt_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1TGKCsvsQ74GnEX44MkxQed"));
            }
        });

        history_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1SbSE-1E77dwTnoCssrD6hU"));
            }
        });

        net_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", "PLNosGDY8Dh1SWbnEf4qNW12ksBmO4ugxV"));
            }
        });

        premiumShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AllPlaylistActivity.class).putExtra("type", "premium"));
            }
        });

        visitChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/channel/" + YoutubeAPI.CHANNEL_ID)));
            }
        });

        exploreVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, YoutubePlayerActivity.class).putExtra("type", "search").putExtra("section", "all"));
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIt();
            }
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
    }

    private void sendFeedback() {
        String subject = "Feedback on ";
        String message = "Hello, Sir. My name is " + name.getText().toString() + ". I have some feedback regarding ";
        Intent intent = new Intent(Intent.ACTION_SEND);
        String[] strTo = {"jbstudypoint2020@gamil.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, strTo);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/email");
        intent.setPackage("com.google.android.gm");
        startActivity(intent);
    }

    private void shareIt() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "JB STUDY POINT");
        intent.putExtra(Intent.EXTRA_TEXT, "Find our official app on https://play.google.com/store/apps/details?id=com.jb.study.point ");
        intent.setType("text/plain");
        startActivity(intent);

    }

    private void logout() {
        session.setLogin(false);
        getSharedPreferences("SHARED_PREFS", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("USER_DETAILS", MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void getNewestJson() {
        horizontalScrollView.setVisibility(View.INVISIBLE);
        progressBar.setIndeterminate(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String url = YoutubeAPI.BASE_URL + YoutubeAPI.SEARCH + YoutubeAPI.PART + YoutubeAPI.CHANNEL + YoutubeAPI.CHANNEL_ID + YoutubeAPI.MAX_RESULTS_10 + YoutubeAPI.ORDER + YoutubeAPI.API + YoutubeAPI.API_KEY;
                Call<SearchModel> call = YoutubeAPI.getSearchVideo().getSearchVideos(url);
                call.enqueue(new Callback<SearchModel>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchModel> call, Response<SearchModel> response) {
                        if (response.errorBody() != null) {
                            Log.e("MainActivity", "" + response.errorBody());
                            try {
                                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                Toast.makeText(MainActivity.this, jsonObject.getJSONObject("error").getJSONArray("errors").getJSONObject(0).get("reason").toString(), Toast.LENGTH_LONG).show();
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            SearchModel searchModel = response.body();
                            search_videoId1 = searchModel.getItems().get(1).getId().getVideoId();
                            search_videoId2 = searchModel.getItems().get(2).getId().getVideoId();
                            search_videoId3 = searchModel.getItems().get(3).getId().getVideoId();
                            Glide.with(MainActivity.this).load(searchModel.getItems().get(0).getSnippet().getThumbnails().getHigh().getUrl()).centerCrop().placeholder(R.drawable.loading).into(thumbnail1);
                            Glide.with(MainActivity.this).load(searchModel.getItems().get(1).getSnippet().getThumbnails().getHigh().getUrl()).centerCrop().placeholder(R.drawable.loading).into(thumbnail2);
                            Glide.with(MainActivity.this).load(searchModel.getItems().get(2).getSnippet().getThumbnails().getHigh().getUrl()).centerCrop().placeholder(R.drawable.loading).into(thumbnail3);
                            getDemoPlaylistJson();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SearchModel> call, @NotNull Throwable t) {
                        Log.e("MainActivity", "onFailure" + t);
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 2000);
    }

    private void getDemoPlaylistJson() {
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.PLAYLISTS + YoutubeAPI.PART + YoutubeAPI.CHANNEL + YoutubeAPI.CHANNEL_ID + YoutubeAPI.ORDER + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<PlaylistModel> call = YoutubeAPI.getPlaylistVideo().getPlaylistVideos(url);
        call.enqueue(new Callback<PlaylistModel>() {
            @Override
            public void onResponse(@NonNull Call<PlaylistModel> call, Response<PlaylistModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    PlaylistModel playlistModel = response.body();
                    playlist_videoId1 = playlistModel.getItems().get(0).getId();
                    playlist_videoId2 = playlistModel.getItems().get(1).getId();
                    playlist_videoId3 = playlistModel.getItems().get(2).getId();
                    findViewById(R.id.course_content_card).setVisibility(View.VISIBLE);
                    if (getStatus) {
                        premium_card.setVisibility(View.VISIBLE);
                        demo_card.setVisibility(View.GONE);
                    } else {
                        demo_card.setVisibility(View.VISIBLE);
                        premium_card.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaylistModel> call, Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
        progressBar.setVisibility(View.GONE);
        horizontalScrollView.setVisibility(View.VISIBLE);
        findViewById(R.id.course_content_card).setVisibility(View.VISIBLE);
        findViewById(R.id.demo_videos_card).setVisibility(View.VISIBLE);
    }

    @SuppressLint("HardwareIds")
    private void saveInfo(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonArray = jsonObject.getJSONArray("userDetails");
        JSONObject user = (JSONObject) jsonArray.get(0);
        if (user.getString("device_token").equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))) {
            try {
                getSharedPreferences("USER_DETAILS", MODE_PRIVATE).edit()
                        .putString("name", user.getString("name"))
                        .putString("email", user.getString("email"))
                        .putString("gender", user.getString("gender"))
                        .putString("subscription", user.getString("subscription"))
                        .putString("validity", user.getString("validity"))
                        .putString("photo", user.getString("photo"))
                        .putString("dob", user.getString("dob"))
                        .putString("device_token", user.getString("device_token"))
                        .putString("payment_date", user.getString("payment_date"))
                        .putString("payment_id", user.getString("payment_id"))
                        .putString("payment_status", user.getString("payment_status"))
                        .putString("payment_history", user.getString("payment_history"))
                        .putString("payment_amount", user.getString("payment_amount"))
                        .apply();
                settingsValues();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            showReLoginDialog();
        }
    }

    private void saveInfoAfterValidity(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject jsonArray = jsonObject.getJSONObject("userDetails");
        try {
            getSharedPreferences("USER_DETAILS", MODE_PRIVATE).edit()
                    .putString("name", jsonArray.getString("name"))
                    .putString("email", jsonArray.getString("email"))
                    .putString("gender", jsonArray.getString("gender"))
                    .putString("subscription", jsonArray.getString("subscription"))
                    .putString("validity", jsonArray.getString("validity"))
                    .putString("photo", jsonArray.getString("photo"))
                    .putString("dob", jsonArray.getString("dob"))
                    .putString("device_token", jsonArray.getString("device_token"))
                    .apply();
            dialog.hide();
            settingsValues();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void showReLoginDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.relogin_alert_dailog,
                        null);
        builder.setCancelable(false);
        Button buttonOk = customLayout.findViewById(R.id.reLogin);
        builder.setView(customLayout);
        AlertDialog dialog
                = builder.create();

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        dialog.show();
    }

    private void showSessionExpiredDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.validity_expired,
                        null);
        builder.setCancelable(false);
        Button buttonOk = customLayout.findViewById(R.id.session);
        ProgressBar progressBar = customLayout.findViewById(R.id.progressBarSession);
        builder.setView(customLayout);
        dialog = builder.create();

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOk.setVisibility(View.GONE);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                updateSubscription();
            }
        });

        dialog.show();
    }

    @SuppressLint("SimpleDateFormat")
    private void settingsValues() {
        //User user = SharedPrefManager.getInstance(this).getUser();
        SharedPreferences editor = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);

        heading_name.setText(String.format("Hello, %s", editor.getString("name", "")));
        name.setText(editor.getString("name", ""));
        if (editor.getString("photo", null) != null) {
            Glide.with(this)
                    .load(editor.getString("photo", ""))
                    .centerCrop()
                    .placeholder(R.drawable.circle_cropped)
                    .into(profile_pic);
        }
        if (editor.getString("subscription", "inactive").equals("active")) {
            subscription_status.setText(R.string.active);
            status_icon.setImageResource(R.drawable.ic_baseline_check_circle_24);
            getStatus = true;
        } else {
            subscription_status.setText(R.string.not_active);
            status_icon.setImageResource(R.drawable.ic_baseline_cancel_24);
            getStatus = false;
        }

        if (premium_card.getVisibility() == View.VISIBLE || demo_card.getVisibility() == View.VISIBLE) {
            if (getStatus) {
                premium_card.setVisibility(View.VISIBLE);
                demo_card.setVisibility(View.GONE);
            } else {
                demo_card.setVisibility(View.VISIBLE);
                premium_card.setVisibility(View.GONE);
            }
        }

        if (editor.getString("subscription", "inactive").equals("active") && editor.getString("validity", "NA").equals(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()))) {
            showSessionExpiredDialog();
        }
    }

    private void updateSubscription() {
        SharedPreferences editor = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        MultipartBody.Part part = null;

        //Create request body with text description and text media type
        RequestBody subscriptionUpdate = RequestBody.create(MediaType.parse("text/plain"), "Expired");
        RequestBody validityUpdate = RequestBody.create(MediaType.parse("text/plain"), "N/A");

        RequestBody paymentStatus = RequestBody.create(MediaType.parse("text/plain"), editor.getString("payment_status", ""));
        RequestBody approvalNo = RequestBody.create(MediaType.parse("text/plain"), editor.getString("payment_id", ""));
        RequestBody paymentDate = RequestBody.create(MediaType.parse("text/plain"), editor.getString("payment_date", ""));
        RequestBody emailUpdate = RequestBody.create(MediaType.parse("text/plain"), editor.getString("email",""));
        RequestBody paymentHistory = RequestBody.create(MediaType.parse("text/plain"), editor.getString("payment_history", ""));
        RequestBody paymentAmount = RequestBody.create(MediaType.parse("text/plain"), editor.getString("payment_amount", ""));
        //
        Call<String> call = api.getSubscriptionUpdatedUser(part, paymentStatus, subscriptionUpdate, approvalNo, paymentDate, validityUpdate, emailUpdate, paymentHistory, paymentAmount);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonresponse = response.body();
                        try {
                            saveInfoAfterValidity(jsonresponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(MainActivity.this, "Nothing returned", Toast.LENGTH_LONG).show();

                    }
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Toast.makeText(MainActivity.this, jsonObject.getJSONObject("errors").getJSONArray("email").get(0).toString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        nav_button = findViewById(R.id.nav_button);
        heading_name = findViewById(R.id.heading_name);
        horizontalScrollView = findViewById(R.id.newest_horizontal_card);
        progressBar = findViewById(R.id.progressBar1);
        thumbnail1 = findViewById(R.id.thumbnail1);
        thumbnail2 = findViewById(R.id.thumbnail2);
        thumbnail3 = findViewById(R.id.thumbnail3);
        showAll = findViewById(R.id.show_all);
        practiceShowAll = findViewById(R.id.practice_show_all);
        practice_beo = findViewById(R.id.beo_card);
        practice_history = findViewById(R.id.history_card);
        practice_tgt = findViewById(R.id.tgt_pgt_card);
        premium_mock_test = findViewById(R.id.mock_test_card);
        premium_history = findViewById(R.id.history_sets_card);
        premium_practice_sets = findViewById(R.id.practice_sets_card);
        premiumShowAll = findViewById(R.id.premium_show_all);
        visitChannel = findViewById(R.id.visitChannel);
        exploreVideos = findViewById(R.id.exploreVideos);
        premium_card = findViewById(R.id.premium_videos_card);
        demo_card = findViewById(R.id.demo_videos_card);
        tgt_playlist  =findViewById(R.id.tgt_playlist);
        history_playlist  =findViewById(R.id.history_playlist);
        net_playlist  =findViewById(R.id.net_playlist);
        // reLogin = findViewById(R.id.reLogin);

        back_nav = navigationView.getHeaderView(0).findViewById(R.id.back_nav_button);
        payment_layout = navigationView.getHeaderView(0).findViewById(R.id.payment_layout);
        sign_out = navigationView.getHeaderView(0).findViewById(R.id.signout_layout);
        profile = navigationView.getHeaderView(0).findViewById(R.id.profile_layout);
        subscription = navigationView.getHeaderView(0).findViewById(R.id.subscription_nav_layout);
        profile_pic = navigationView.getHeaderView(0).findViewById(R.id.profile);
        name = navigationView.getHeaderView(0).findViewById(R.id.name);
        subscription_status = navigationView.getHeaderView(0).findViewById(R.id.subscription_status);
        status_icon = navigationView.getHeaderView(0).findViewById(R.id.status_icon);
        share = navigationView.getHeaderView(0).findViewById(R.id.share);
        feedback = navigationView.getHeaderView(0).findViewById(R.id.feedback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        settingsValues();
        if (premium_card.getVisibility() == View.VISIBLE || demo_card.getVisibility() == View.VISIBLE) {
            if (getStatus) {
                premium_card.setVisibility(View.VISIBLE);
                demo_card.setVisibility(View.GONE);
            } else {
                demo_card.setVisibility(View.VISIBLE);
                premium_card.setVisibility(View.GONE);
            }
        }
    }
}