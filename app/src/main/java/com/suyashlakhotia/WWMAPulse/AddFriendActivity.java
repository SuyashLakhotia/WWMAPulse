package com.suyashlakhotia.WWMAPulse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AddFriendActivity extends Activity {

    Button btnAdd;
    EditText name;
    EditText user_id;

    private FriendsDBAdapter mDbHelper2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        btnAdd = (Button)findViewById(R.id.button2);
        name = (EditText)findViewById(R.id.editText);
        user_id = (EditText)findViewById(R.id.editText2);

        mDbHelper2 = new FriendsDBAdapter(this);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDbHelper2.open();
                mDbHelper2.createFriends(name.getText().toString(), user_id.getText().toString());

                Toast.makeText(AddFriendActivity.this, "Friend Added.", Toast.LENGTH_LONG).show();

                createReminder();
            }
        });
    }

    private void createReminder() {
        Intent i = new Intent(this, AddReminderActivity.class);
        startActivityForResult(i, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_friend, menu);
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
