package ba.wurth.mb.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ba.wurth.mb.Activities.Tasks.TaskActivity;
import ba.wurth.mb.Classes.Objects.Task;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class TasksAdapter extends ArrayAdapter<Task> {

    private ArrayList<Task> items;
    private Context mContext;
    private java.text.Format formatter = new SimpleDateFormat("dd.MMM.yyyy HH:mm");
    private Fragment mFragment;

    public TasksAdapter(Context context, int textViewResourceId, ArrayList<Task> objects, Fragment f)
    {
        super(context, textViewResourceId);
        this.items = objects;
        this.mContext = context;
        this.mFragment = f;
    }

    @Override
    public int getCount()
    {
        return this.items.size();
    }

    @Override
    public Task getItem(int index)
    {
        try {
            if (index <= getCount()) return this.items.get(index);
            return this.items.get(getCount() - 1);
        }
        catch (Exception e) { }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View inflatedView = convertView;

        if (inflatedView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflatedView = inflater.inflate(R.layout.list_row, parent, false);
        }

        try {
            if (inflatedView == null) return null;

            final Task element = getItem(position);

            String statusText = "";
            String dateText = "";
            String createdByText = "";

            switch ((int) element.StatusID) {
                case 1:
                    break;
                default:break;
            }

            switch ((int) element.StatusID) {
                case 1:
                    statusText = mContext.getString(R.string.Draft);
                    inflatedView.findViewById(R.id.litStatus).setBackgroundColor(mContext.getResources().getColor(R.color.grey_1));
                    break;
                case 2:
                    statusText = mContext.getString(R.string.Sent);
                    inflatedView.findViewById(R.id.litStatus).setBackgroundColor(mContext.getResources().getColor(R.color.blue));
                    break;
                case 3:
                    statusText = mContext.getString(R.string.Progress);
                    inflatedView.findViewById(R.id.litStatus).setBackgroundColor(mContext.getResources().getColor(R.color.blue));
                    break;
                case 5:
                    statusText = mContext.getString(R.string.Rejected);
                    inflatedView.findViewById(R.id.litStatus).setBackgroundColor(mContext.getResources().getColor(R.color.red));
                    break;
                case 7:
                    statusText = mContext.getString(R.string.Completed);
                    inflatedView.findViewById(R.id.litStatus).setBackgroundColor(mContext.getResources().getColor(R.color.green));
                    break;
                case 8:
                    statusText = mContext.getString(R.string.Canceled);
                    inflatedView.findViewById(R.id.litStatus).setBackgroundColor(mContext.getResources().getColor(R.color.red));
                    break;
                default:
                    break;
            }

            ((TextView) inflatedView.findViewById(R.id.litTitle)).setText(element.Name);
            ((TextView) inflatedView.findViewById(R.id.litSupTotal)).setText(mContext.getString(R.string.Status));
            ((TextView) inflatedView.findViewById(R.id.litTotal)).setText(statusText);

            try {

                JSONObject jsonObject = new JSONObject(element.Parameters);

                if (!jsonObject.isNull("startDate")) {
                    Date startDate = new Date(jsonObject.getLong("startDate"));
                    dateText = mContext.getString(R.string.StartTime) + ": " + formatter.format(startDate);
                }

                if (!jsonObject.isNull("endDate")) {
                    Date endDate = new Date(jsonObject.getLong("endDate"));
                    dateText += " - " + mContext.getString(R.string.EndTime) + ": " + formatter.format(endDate);

                    if (endDate.getTime() < System.currentTimeMillis()) {
                        ((TextView) inflatedView.findViewById(R.id.litTitle)).setTextColor(Color.RED);
                    }
                }

                if (!jsonObject.isNull("CreatedBy")) {
                    Cursor cur = DL_Wurth.GET_User(jsonObject.getLong("CreatedBy"));

                    if (cur != null){
                        createdByText = cur.getString(cur.getColumnIndex("Firstname")) + " " + cur.getString(cur.getColumnIndex("Lastname"));
                        cur.close();
                    }
                }

                ((TextView) inflatedView.findViewById(R.id.litSupTitle)).setText(mContext.getString(R.string.CreatedBy) + ": " + createdByText);
                ((TextView) inflatedView.findViewById(R.id.litSubTitle)).setText(dateText);

            }
            catch (Exception exx) {

            }

            if (element.Name.length() > 0) ((TextView) inflatedView.findViewById(R.id.litStatus)).setText(element.Name.substring(0,1));

            inflatedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, TaskActivity.class);
                    i.putExtra("TaskID", element.TaskID);
                    mContext.startActivity(i);
                }
            });

        }
        catch (Exception ex) {

        }

        return inflatedView;
    }

}

