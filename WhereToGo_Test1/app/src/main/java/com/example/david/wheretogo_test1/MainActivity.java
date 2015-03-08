package com.example.david.wheretogo_test1;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.Intent;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

import android.view.View;
import com.facebook.Request;
import com.facebook.Response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;




public class MainActivity extends ActionBarActivity {

    //private LoginButton loginBtn;
    //private TextView username;
    private UiLifecycleHelper uiHelper;

    //Graph User
    private View otherView;
    private static final String TAG = "MainActivity";

    private Button mBtnFriendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Graph User
        setContentView(R.layout.activity_main);
        mBtnFriendList = (Button) findViewById(R.id.btnfriendlist);

        mBtnFriendList.setOnClickListener(btnfrienlistOnClick);

        // Set View that should be visible after log-in invisible initially
        otherView = (View) findViewById(R.id.other_views);
        otherView.setVisibility(View.GONE);
        // To maintain FB Login session
        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
    }

    //Jump to the new screen for friend list
    private View.OnClickListener btnfrienlistOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, FriendList.class);
            startActivity(intent);
        }
    };

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (state.isOpened()) {
                Log.d("MainActivity", "Facebook session opened.");

                // Show the Graph User
                onSessionStateChange(session, state, exception);
            } else if (state.isClosed()) {
                Log.d("MainActivity", "Facebook session closed.");
            }
        }
    };

    // Show the Graph User
    // When session is changed, this method is called from callback method
    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        final TextView name = (TextView) findViewById(R.id.name);
        final TextView gender = (TextView) findViewById(R.id.gender);
        final TextView user_ID = (TextView) findViewById(R.id.user_ID);
        final ImageView pic = (ImageView) findViewById(R.id.User_Picture);

        // When Session is successfully opened (User logged-in)
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            // make request to the /me API to get Graph user
            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user
                // object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        // Set view visibility to true
                        otherView.setVisibility(View.VISIBLE);
                        // Set User name
                        name.setText("Hello " + user.getName());
                        // Set Gender
                        gender.setText("Your Gender: "
                                + user.getProperty("gender").toString());
                        // Get user ID to query the picture
                        user_ID.setText("Your User ID: "
                                + user.getId());
                        if (user_ID != null)
                        {
                            Log.i(TAG, "User ID: " + user.getId());
                            getFacebookProfilePicture(pic, user.getId());
                        }
                    }
                }
            }).executeAsync();
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            otherView.setVisibility(View.GONE);
        }
    }

    //Async Task to download the bitmap through URL
    public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

        ImageView imageView = null;

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            this.imageView = imageViews[0];
            return download_Image((String)imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }

        private Bitmap download_Image(String url) {

            Bitmap bmp =null;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;

            }catch(Exception e){}
            return bmp;
        }
    }

    // Use this function to download the Facebook picture
    // We can't get the bitmap in the mainthread, so we have to create one AyncTask to do it
    public void getFacebookProfilePicture(ImageView t_Pic, String userID){
        //ex, my userID is David1984.lin
        final String nomimg = "https://graph.facebook.com/"+userID+"/picture?type=large";
        t_Pic.setTag(nomimg);
        new DownloadImagesTask().execute(t_Pic);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
