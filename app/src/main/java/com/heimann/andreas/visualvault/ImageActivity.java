package com.heimann.andreas.visualvault;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity implements PointCollectorListener {

    private static final String PASSWORD_SET = "Password set";
    private static final int PW_COUNT = 4;
    private static final int MAX_DISTANCE = 100;
    private static final String CUSTOM_IMAGE = "Custom Image";
    private PointCollector pointCollector = new PointCollector();
    private Database db = new Database(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        addTouchListener();
        addPictureButtonListener();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "activity"};
            requestPermissions(permissions, 200);
            Boolean resetPasspoints = extras.getBoolean(MainActivity.RESET_PASSPOINTS);
            Boolean resetImage = extras.getBoolean(MainActivity.RESET_IMAGE);
            String imageUri = extras.getString(MainActivity.GALLERY_URI);

            if (resetPasspoints) {
                db.deletePoints();
                changePrefsBool(PASSWORD_SET, false);
            }

            if (resetImage) {
                db.deletePoints();
                changePrefsBool(PASSWORD_SET, false);
                changePrefsString(CUSTOM_IMAGE, null);
            }

            if (imageUri != null) {
                ImageView view = (ImageView) findViewById(R.id.touch_image);
                try {
                    InputStream imageStream = getContentResolver().openInputStream(Uri.parse("file://" + imageUri));
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                    changePrefsString(CUSTOM_IMAGE, "file://" + imageUri);
                    if (bitmap != null) {
                        view.setImageBitmap(bitmap);
                    }
                    db.deletePoints();
                    changePrefsBool(PASSWORD_SET, false);
                } catch (Exception e) {
                    Log.d(MainActivity.DEBUGTAG, e.toString());
                }
            }
        }
        setCustomImage();

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Boolean hasPassword = prefs.getBoolean(PASSWORD_SET, false);

        if (hasPassword) {
            showPrompt();
            Log.d(MainActivity.DEBUGTAG, "Password has been set!");
        } else {
            showInstructions();
            Log.d(MainActivity.DEBUGTAG, "No password has been set!");
        }

        pointCollector.setListener(this);
    }

    private void setCustomImage() {
        try {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            String imagePath = prefs.getString(CUSTOM_IMAGE, null);
            if (imagePath != null) {
                InputStream imageStream = getContentResolver().openInputStream(Uri.parse(imagePath));
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                ImageView view = (ImageView) findViewById(R.id.touch_image);

                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                }

            }
        } catch (Exception e) {
            Log.d(MainActivity.DEBUGTAG, e.toString());
        }
    }

    private void showInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle(R.string.image_instructions_title);
        builder.setMessage(R.string.image_instructions_text);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_app_reset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.menu_app_reset_warning);
            builder.setMessage(R.string.menu_app_reset_info);
            builder.setPositiveButton(R.string.menu_app_reset_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.deletePoints();
                    db.deleteAllMessages();
                    changePrefsBool(PASSWORD_SET, false);
                    changePrefsString(CUSTOM_IMAGE, null);
                    Intent i = new Intent(ImageActivity.this, ImageActivity.class);
                    startActivity(i);
                }
            });
            builder.setNegativeButton(R.string.menu_app_reset_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Passcode");
        builder.setMessage("Touch the Passpoints to enter");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changePrefsBool(String id, boolean update) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(id, update);
        editor.apply();
    }

    private void changePrefsString(String id, String update) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(id, update);
        editor.apply();
    }

    private void addPictureButtonListener() {
        Button button = (Button) findViewById(R.id.button_empty_database);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Point> points = db.getPoints();
                    Point point = points.get(0);
                    String hintText = getString(R.string.image_hint_text);
                    String hintFull = String.format("%s: %d, %d", hintText, point.x, point.y);
                    Toast.makeText(ImageActivity.this, hintFull, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(ImageActivity.this, "No password set", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addTouchListener() {
        ImageView image = (ImageView) findViewById(R.id.touch_image);

        image.setOnTouchListener(pointCollector);
    }

    public void savePassword(final List<Point> points) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.storing_data);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final List<Point> list = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            list.add(points.get(i));
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                db.storePoints(list);
                changePrefsBool(PASSWORD_SET, true);
                Log.d(MainActivity.DEBUGTAG, "Password has been stored in DB");
                return null;
            }

            @Override
            protected void onPostExecute(Void aBoolean) {
                dialog.dismiss();
            }
        };

        task.execute();
        showPrompt();
    }

    public Boolean verifyPoints(final List<Point> points) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.verifying_password);

        final AlertDialog dialog = builder.create();
        dialog.show();

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                List<Point> storedPoints = db.getPoints();

                if (storedPoints.size() != PW_COUNT || points.size() != PW_COUNT) {
                    Log.d(MainActivity.DEBUGTAG, String.format("StoredPoints: %d, points: %d", storedPoints.size(), points.size()));
                    return false;
                }

                for (int i = 0; i < 4; i++) {
                    int xDiff = points.get(i).x - storedPoints.get(i).x;
                    int yDiff = points.get(i).y - storedPoints.get(i).y;

                    int squaredXDiff = xDiff * xDiff;
                    int squaredYDiff = yDiff * yDiff;

                    if (squaredXDiff >= MAX_DISTANCE * MAX_DISTANCE || squaredYDiff >= MAX_DISTANCE * MAX_DISTANCE) {
                        Log.d(MainActivity.DEBUGTAG, "Point " + (i + 1) + " too far away");
                        return false;
                    }
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean pass) {
                dialog.dismiss();

                if (pass) {
                    Intent i = new Intent(ImageActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(ImageActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Boolean isVerified = false;
        try {
            isVerified = task.execute().get();
            Log.d(MainActivity.DEBUGTAG, "Password " + isVerified);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isVerified;
    }

    @Override
    public void pointsCollected(final List<Point> points) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Boolean hasPassword = prefs.getBoolean(PASSWORD_SET, false);

        if (hasPassword) {
            verifyPoints(points);
        } else {
            savePassword(points);
        }

        pointCollector.clear();
    }

}
