package com.heimann.andreas.visualvault;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String DEBUGTAG = "AHD";
    public static final String RESET_PASSPOINTS = "ResetPassoints";
    public static final String RESET_IMAGE = "ResetImage";
    public static final String GALLERY_URI = "GalleryUri";
    private static final int BROWSE_GALLERY_REQUEST = 2;
    private Database db = new Database(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addTouchListener();
        addSaveButtonListener();
        addLockButtonListener();
        setUpMessages();
    }

    private void addTouchListener() {
        final View view = findViewById(R.id.main_layout);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("AHD", "LAYOUT was touched");
                EditText editText = (EditText) findViewById(R.id.text);
                editText.clearFocus();
                addSaveButtonListener();
                addLockButtonListener();
                hideSoftKeyboard(MainActivity.this, v);
                return true;
            }
        });
    }

    private void setUpMessages() {
        List<Message> messages = db.getMessages();

        final MessageAdapter adapter = new MessageAdapter(this, messages);

        GridView gridView = (GridView) findViewById(R.id.listView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayMessage(position);
                view.setSelected(true);
                updateListeners(position);
            }
        });
    }

    private void updateListeners(int position) {
            addDeleteButtonListener(position);
            addOverwriteButtonListener(position);
    }

    private void addDeleteButtonListener(final int position) {
        Button deleteBtn = (Button) findViewById(R.id.main_lock);
        deleteBtn.setText(R.string.button_delete);
        List<Message> messages = db.getMessages();
        final int id = messages.get(position).getId();

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteMessage(id);
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    private void addOverwriteButtonListener(final int position) {
        Button overwriteBtn = (Button) findViewById(R.id.save);
        overwriteBtn.setText(R.string.button_overwrite);
        final List<Message> messages = db.getMessages();
        final int id = messages.get(position).getId();

        overwriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText textView = (EditText) findViewById(R.id.text);
                    String text = textView.getText().toString();
                    Message message = db.getMessage(id);
                    if (message.getTitle().equals(text)) {
                        Log.d(DEBUGTAG, "Could not update identical Message: " + message.getTitle() + " and " + text);
                        Toast.makeText(MainActivity.this, R.string.main_update_identical, Toast.LENGTH_SHORT).show();
                    } else {
                        if (text.length() > 1) {
                            db.updateMessage(id, text);
                            Intent i = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.button_save_shortString, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Log.d(DEBUGTAG, "Could not update Message");
                }
            }
        });
    }

    private void displayMessage(int position) {
        TextView text = (TextView) findViewById(R.id.text);
        List<Message> messages = db.getMessages();
        text.setText(messages.get(position).getTitle());
    }

    private void addLockButtonListener() {
        Button button = (Button) findViewById(R.id.main_lock);
        button.setText(R.string.lock);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockMain(v);
            }
        });
    }

    private void lockMain(View view) {
        hideSoftKeyboard(MainActivity.this, view);
        Intent i = new Intent(this, ImageActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_password_reset) {
            Intent i = new Intent(this, ImageActivity.class);
            i.putExtra(RESET_PASSPOINTS, true);
            startActivity(i);
        }

        if (id == R.id.menu_password_browse_Gallery) {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, BROWSE_GALLERY_REQUEST);
        }

        if (id == R.id.menu_image_reset) {
            Intent i = new Intent(MainActivity.this, ImageActivity.class);
            i.putExtra(RESET_IMAGE, true);
            startActivity(i);
        }
        if (id == R.id.menu_delete_message_all) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.menu_delete_messages);
            builder.setMessage(R.string.menu_delete_messages_querry);
            builder.setPositiveButton(R.string.menu_app_reset_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.deleteAllMessages();
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
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

    private void addSaveButtonListener() {
        final Button saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setText(R.string.save);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.text);
                String text = editText.getText().toString();
                hideSoftKeyboard(MainActivity.this, v);

                try {
                    if (text.length() > 1) {
                        Message message = new Message(text);
                        db.storeMessage(message);
                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.button_save_shortString, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d(DEBUGTAG, "Unable to save file. " + text);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == BROWSE_GALLERY_REQUEST && resultCode == RESULT_OK) {
            String[] columns = {MediaStore.Images.Media.DATA};
            Uri imageUri = intent.getData();
            Log.d(DEBUGTAG, "imageuri is: " + imageUri.toString());

            Cursor cursor = getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(columns[0]);
                String imagePath = cursor.getString(columnIndex);
                cursor.close();

                changeImage(imagePath);
            }
        }

    }

    private void changeImage(String imagePath) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra(GALLERY_URI, imagePath);
        startActivity(intent);
    }

    public void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
