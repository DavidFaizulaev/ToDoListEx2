package il.ac.shenkar.david.todolistex2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InviteMember extends AppCompatActivity
{
    private EditText member_email;
    private EditText member_phone;
    private final String email_Subject = "Invitation to Join Team OTS";
    private final String email_body = "Hi,\n\n" +
                                "You have been invited to be a team member in an OTS Team " + Globals.team_name + " created by me.\n" +
                                "Your username is your email address and password is your phone number\n" +
                                "Use this link to download and install the App from Google Play.\n\n" +
                                "<LINK to Google Play download>\n\n" +
                                "Best Regards,\n" +
                                "You OTS Manager.";

    private ParseObject parse_usr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onInviteMember (View view)
    {
        member_email = (EditText) findViewById(R.id.editemailaddress);
        member_phone = (EditText) findViewById(R.id.memberuserphonenumber);
        Intent email = new Intent(Intent.ACTION_SEND);

        String[] new_users = member_email.getText().toString().split(",");
        addNewUsers(new_users,member_phone.getText().toString());

        for (String str_usrname : new_users)
        {
            parse_usr = new ParseObject("OTSUser");
            parse_usr.put("Username",str_usrname);
            parse_usr.put("Password", member_phone.getText().toString());
            parse_usr.put("Email",str_usrname);
            parse_usr.put("IsManager",0);
            parse_usr.put("TeamName",Globals.team_name);
            parse_usr.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // if null, it means the save has succeeded
                        Log.d("new team member created", "good");
                    } else {
                        // the save call was not successful.
                    }
                }
            });
        }

        email.putExtra(Intent.EXTRA_EMAIL, new String[]{ member_email.getText().toString()});
        email.putExtra(Intent.EXTRA_SUBJECT, email_Subject);
        email.putExtra(Intent.EXTRA_TEXT, email_body);

        //need this to prompts email client only
        email.setType("message/rfc822");

        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    public void onDonebtn (View view)
    {
        Intent i = getIntent();
        Intent returnIntent = null;

        String from_act = (String)i.getSerializableExtra("from");

        if(from_act.equals("from_main_activity"))
        {
            returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        }

        else
        {
            returnIntent = new Intent(this,MainActivity.class);
            setResult(RESULT_OK, returnIntent);
            startActivity(returnIntent);
        }
    }

    public void onExitbtn(View view)
    {
        finish();
        System.exit(0);
    }

    private void addNewUsers (String[] newUsers, String usrpwd)
    {
        ParseObject parse_otsusr = null;
        for (String usr : newUsers)
        {
            parse_otsusr = new ParseObject("OTSUser");
            parse_otsusr.put("Username", usr);
            parse_otsusr.put("Password",usrpwd);
            parse_otsusr.put("Email", usr);
            parse_otsusr.put("IsManager", 1);
            parse_otsusr.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // if null, it means the save has succeeded
                        Log.d("addded user", "good");
                    } else {
                        // the save call was not successful.
                    }
                }
            });
        }
    }
}
