package il.ac.shenkar.david.todolistex2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener {

    ListView list;
    List<Task> itemList;
    Context context = MainActivity.this;
    TaskItemAdapter adapter;
    DBManager dbM;

    private TextView emptylist_txt;
    private TextView minutes_text;
    private Task tmp_task;

    public final int REQUEST_CODE_NEW_TASK = 1;
    public final int REQUEST_CODE_UPDATE_TASK = 2;
    public final int REQUEST_CODE_REMOVE_TASK = 3;
    public final int REQUEST_CODE_INVITE_MEMBER = 4;

    private static Dialog minute_diag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbM = DBManager.getInstance(context);

        //check if any tasks exist in Parse DB
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Task");
        query.whereContains("TeamName", Globals.team_name);
        query.whereContains("IsCompleted","0");
        if(Globals.IsManager==false)
        {
            SharedPreferences sharedpreferences = getSharedPreferences("il.ac.shenkar.david.todolistex2", Context.MODE_PRIVATE);
            query.whereContains("Employee",sharedpreferences.getString("LoginUsr",null));
        }

        List<ParseObject> tsks=null;

        try {
            tsks = query.find();
            for (ParseObject tmp : tsks)
            {
                tmp_task = new Task();
                tmp_task.setDescription(tmp.getString("Description"));

                int position = tmp.getInt("Category");
                switch(position)
                {
                    case 0:
                        tmp_task.setTask_catg(Category.GENERAL);
                        break;
                    case 1:
                        tmp_task.setTask_catg(Category.CLEANING);
                        break;
                    case 2:
                        tmp_task.setTask_catg(Category.ELECTRICITY);
                        break;
                    case 3:
                        tmp_task.setTask_catg(Category.COMPUTERS);
                        break;
                    case 4:
                        tmp_task.setTask_catg(Category.OTHER);
                        break;
                }

                position = tmp.getInt("Priority");
                switch(position)
                {
                    case 0:
                        tmp_task.setPriority(Priority.LOW);
                        break;
                    case 1:
                        tmp_task.setPriority(Priority.NORMAL);
                        break;
                    case 2:
                        tmp_task.setPriority(Priority.URGENT);
                        break;
                    default:
                        tmp_task.setPriority(Priority.NORMAL);
                        break;
                }

                position = tmp.getInt("Status");
                switch(position)
                {
                    case 0:
                        tmp_task.setTask_sts(Task_Status.WAITING);
                        break;
                    case 1:
                        tmp_task.setTask_sts(Task_Status.INPROGESS);
                        break;
                    case 2:
                        tmp_task.setTask_sts(Task_Status.DONE);
                        break;
                    default:
                        tmp_task.setTask_sts(Task_Status.WAITING);
                        break;
                }

                position = tmp.getInt("Location");
                switch(position)
                {
                    case 0:
                        tmp_task.setTsk_location(Locations.Meeting_Room);
                        break;
                    case 1:
                        tmp_task.setTsk_location(Locations.Office_245);
                        break;
                    case 2:
                        tmp_task.setTsk_location(Locations.Lobby);
                        break;
                    case 3:
                        tmp_task.setTsk_location(Locations.NOC);
                        break;
                    case 4:
                        tmp_task.setTsk_location(Locations.VPsoffice);
                        break;
                    default:
                        tmp_task.setTsk_location(Locations.Meeting_Room);
                        break;
                }

                tmp_task.setDueDate(tmp.getDate("DueDate"));
                tmp_task.setParse_task_id(tmp.getObjectId());

                long seq_tsk_id = dbM.addTask(tmp_task);
                tmp_task.setTaskId(seq_tsk_id);
                dbM.updateParseID(tmp_task);
            }
        } catch (ParseException e) {}

        itemList = new ArrayList<Task>();
        list  = (ListView)findViewById(R.id.listView);
        itemList = dbM.getAllTasks();
        list.setAdapter(new TaskItemAdapter(context, itemList));

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {

                //get item instance from list
                Task tt = (Task) ((TaskItemAdapter) parent.getAdapter()).getItem(position);

                //start the create activity again, now for editing
                Intent i = new Intent(getApplicationContext(), EditTaskActivity.class);
                i.putExtra("task", tt);
                startActivityForResult(i, REQUEST_CODE_UPDATE_TASK);

                return false;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(context, "Long press to edit task", Toast.LENGTH_SHORT).show();
            }
        });

        emptylist_txt = (TextView) findViewById(R.id.emptylist);

        if(itemList.size()==0) {
            emptylist_txt.setVisibility(View.VISIBLE);}
        else
        {
            emptylist_txt.setVisibility(View.GONE);
        }
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
            onAction_Settings ();
        }

        if (id == R.id.action_manageteam)
        {
            Intent returnIntent = new Intent(this,InviteMember.class);
            returnIntent.putExtra("from", "from_main_activity");
            startActivityForResult(returnIntent, REQUEST_CODE_INVITE_MEMBER);
        }

        if (id == R.id.action_Logout)
        {
            Intent returnIntent = new Intent(this,Login_activity.class);
            setResult(RESULT_OK, returnIntent);
            startActivity(returnIntent);
        }

        if (id == R.id.action_About)
        {
            int versionCode = BuildConfig.VERSION_CODE;
            String versionName = BuildConfig.VERSION_NAME;

            new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("Application version " + versionCode + "\nVersion Name " + versionName + "\n\n" + "Created by David Faizulaev")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            return;
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void newTaskButtonClick (View view)
    {
        Intent intent = new Intent(this,ListNodeActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Task returned_task;

        if(resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case REQUEST_CODE_NEW_TASK:{
                    Toast.makeText(context, "New Task Added", Toast.LENGTH_SHORT).show();
                    returned_task = (Task)data.getSerializableExtra("task");
                    itemList.add(returned_task);
                    emptylist_txt = (TextView) findViewById(R.id.emptylist);
                    emptylist_txt.setVisibility(View.GONE);
                    adapter =  new TaskItemAdapter(context, itemList);
                    list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                    break;

                case REQUEST_CODE_UPDATE_TASK:
                {
                    returned_task = (Task)data.getSerializableExtra("task");
                    if(returned_task.getToDelete())
                    {
                        for(int i=0;i<itemList.size();i++)
                        {
                            if(itemList.get(i).getTaskId()==returned_task.getTaskId())
                            {
                                dbM.deleteTask(returned_task);
                                itemList.remove(i);
                                emptylist_txt = (TextView) findViewById(R.id.emptylist);

                                if(itemList.size()==0) {
                                    emptylist_txt.setVisibility(View.VISIBLE);}

                                adapter =  new TaskItemAdapter(context, itemList);
                                list.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    else
                    {
                        for(int i=0;i<itemList.size();i++)
                        {
                            if(itemList.get(i).getTaskId()==returned_task.getTaskId())
                            {
                                itemList.set(i, returned_task);
                                adapter =  new TaskItemAdapter(context, itemList);
                                list.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    break;
                }

                case REQUEST_CODE_INVITE_MEMBER:
                {
                    adapter = new TaskItemAdapter(context, itemList);
                    list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                }

                default:
                {
                    adapter = new TaskItemAdapter(context, itemList);
                    list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        //get the list
        // list = (ListView) findViewById(R.id.listView);
        //fill the list with tasks
        // list.setAdapter(new TaskItemAdapter(context, itemList));

      /*  list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {

                Toast.makeText(context, "Long pressed to edit task", Toast.LENGTH_SHORT).show();

                return false;
            }
        }); */

       /* list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(context, "Long press to edit task", Toast.LENGTH_SHORT).show();
            }
        }); */
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal)
    {;}

    public void onAction_Settings ()
    {
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("Select Number of Minutes for New Tasks Refresh");
        d.setContentView(R.layout.selectminutes);
        Button b1 = (Button) d.findViewById(R.id.setnumberofminbtn);
        Button b2 = (Button) d.findViewById(R.id.cancelminbtn);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        np.setMaxValue(10); // max value 10
        np.setMinValue(0);   // min value 0
        np.setValue(Globals.refresh_minutes);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Globals.refresh_minutes = np.getValue();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //connect to SQLite
        dbM = DBManager.getInstance(context);
        //get all tasks from db
        list  = (ListView)findViewById(R.id.listView);
        itemList = dbM.getAllTasks();

        //fill the list with tasks
        list.setAdapter(new TaskItemAdapter(context, itemList));
    }
}