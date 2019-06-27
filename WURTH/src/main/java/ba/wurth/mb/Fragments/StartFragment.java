package ba.wurth.mb.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import ba.wurth.mb.Activities.Activities.ActivityActivity;
import ba.wurth.mb.Activities.Diary.DiaryActivity;
import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.TextProgressBar;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Reports.DL_Reports;
import ba.wurth.mb.DataLayer.Tasks.DL_Tasks;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Fragments.Clients.ClientsFragment;
import ba.wurth.mb.Fragments.Orders.OrdersFragment;
import ba.wurth.mb.Fragments.Tasks.TasksFragment;
import ba.wurth.mb.R;

public class StartFragment extends Fragment {

    private Button btnCreateOrder;
    private Button btnClients;
    private Button btnProducts;
    private Button btnNoteVisit;
    private Button btnMemo;
    private Button btnDiary;
    private Button btnActivity;
    private Button btnTasks;
    private Button btnOrders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetTotals().execute();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnCreateOrder = (Button) getView().findViewById(R.id.btnCreateOrder);
        btnClients = (Button) getView().findViewById(R.id.btnClients);
        btnProducts = (Button) getView().findViewById(R.id.btnProducts);
        btnNoteVisit = (Button) getView().findViewById(R.id.btnNoteVisit);
        btnMemo = (Button) getView().findViewById(R.id.btnMemo);
        btnDiary = (Button) getView().findViewById(R.id.btnDiary);
        btnActivity = (Button) getView().findViewById(R.id.btnActivity);
        btnTasks = (Button) getView().findViewById(R.id.btnTasks);
        btnOrders = (Button) getView().findViewById(R.id.btnOrders);

        btnCreateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), OrderActivity.class);
                startActivity(i);
            }
        });

        btnOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Fragment f : getActivity().getSupportFragmentManager().getFragments()) if (!(f instanceof StartFragment)) getActivity().getSupportFragmentManager().beginTransaction().remove(f);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, new OrdersFragment(), "ORDERS").addToBackStack(null).commit();
            }
        });

        btnTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Fragment f : getActivity().getSupportFragmentManager().getFragments()) if (!(f instanceof StartFragment)) getActivity().getSupportFragmentManager().beginTransaction().remove(f);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, new TasksFragment(), "TASKS").addToBackStack(null).commit();
            }
        });

        btnActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ActivityActivity.class);
                startActivity(i);
            }
        });

        btnClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Fragment f : getActivity().getSupportFragmentManager().getFragments()) if (!(f instanceof StartFragment)) getActivity().getSupportFragmentManager().beginTransaction().remove(f);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content, new ClientsFragment(), "CLIENTS").addToBackStack(null).commit();
            }
        });

        btnProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ProductsActivity.class);
                startActivity(i);
            }
        });

        btnNoteVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), VisitActivity.class);
                startActivity(i);
            }
        });

        btnDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DiaryActivity.class);
                startActivity(i);
            }
        });

        btnMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert);

                ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                dialog.findViewById(R.id.text).setVisibility(View.GONE);

                dialog.findViewById(R.id.dialogButtonNEW).setVisibility(View.GONE);
                dialog.findViewById(R.id.dialogButtonCANCEL).setVisibility(View.VISIBLE);
                ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.Submit);
                dialog.findViewById(R.id.note).setVisibility(View.VISIBLE);

                dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        try {

                            JSONObject mTemp = new JSONObject();
                            mTemp.put("UserID", wurthMB.getUser().UserID);
                            mTemp.put("Note", ((EditText) dialog.findViewById(R.id.note)).getText().toString());
                            mTemp.put("DOE", System.currentTimeMillis());

                            Temp_Acquisition temp = new Temp_Acquisition();

                            temp.AccountID = wurthMB.getUser().AccountID;
                            temp.UserID = wurthMB.getUser().UserID;
                            temp.OptionID = 31;
                            temp.ID = 0;
                            temp.Sync = 0;
                            temp.DOE = System.currentTimeMillis();
                            temp.jsonObj = mTemp.toString();

                            if (DL_Temp.AddOrUpdate(temp) > 0 ) {
                                Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_Saved), 0);
                            }
                            else {
                                Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.SystemError), 1);
                            }
                        }
                        catch (Exception ex) {

                        }
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start, container, false);
    }


    private class GetTotals extends AsyncTask<Void, Void, Void> {
        private Double Today = 0D;
        private Double ThisMonth = 0D;
        private Double YesterDay = 0D;

        private Double ThisMonthPlaned = 0D;
        private Double PlanedByDay = 0D;
        private Double ThisMonthPecentage = 0D;

        private int[] TaskPending = new int[] {0,0};

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {

                ((TextView) getView().findViewById(R.id.litMonth)).setText(CustomNumberFormat.GenerateFormatCurrency(ThisMonth));
                ((TextView) getView().findViewById(R.id.litYesterday)).setText(CustomNumberFormat.GenerateFormatCurrency(YesterDay));
                ((TextView) getView().findViewById(R.id.litToday)).setText(CustomNumberFormat.GenerateFormatCurrency(Today));
                ((TextView) getView().findViewById(R.id.litTask)).setText(Integer.toString(TaskPending[1]) + "/" + Integer.toString(TaskPending[0]));

                ((TextView) getView().findViewById(R.id.litThisMonthPlaned)).setText(CustomNumberFormat.GenerateFormatCurrency(ThisMonthPlaned));
                //((TextView) getView().findViewById(R.id.litThisMonthPlanedByDay)).setText(CustomNumberFormat.GenerateFormatCurrency(ThisMonthPlaned / 22));
                //if (ThisMonthPlaned > 0) ((TextView) getView().findViewById(R.id.litThisMonthPlanedPercentage)).setText(CustomNumberFormat.GenerateFormat((ThisMonth / ThisMonthPlaned) * 100D) + " %");
                if (ThisMonthPlaned > 0) {
                    ((TextProgressBar) getView().findViewById(R.id.pg)).setProgress((int) ((ThisMonth / ThisMonthPlaned) * 100D));
                    ((TextProgressBar) getView().findViewById(R.id.pg)).setText(CustomNumberFormat.GenerateFormat((ThisMonth / ThisMonthPlaned) * 100D) + " %");

                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    ((TextProgressBar) getView().findViewById(R.id.pgAlt)).setProgress((int) (((day * (ThisMonthPlaned / 31))  / ThisMonthPlaned) * 100D));
                    ((TextProgressBar) getView().findViewById(R.id.pgAlt)).setText("");

                }

            }
            catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Long endTime;
                Long startTime;

                Long endTimeYesterday;
                Long startTimeYesterday;

                Long endTimeThisMonth;
                Long startTimeThisMonth;

                Calendar c = Calendar.getInstance();
                endTime = System.currentTimeMillis();
                endTimeThisMonth = System.currentTimeMillis();

                c.set(Calendar.HOUR_OF_DAY,0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                startTime = c.getTimeInMillis();
                endTimeYesterday = startTime;
                Today = DL_Reports.GetOrderTotals(startTime, endTime, 0L, 0L);

                c.set(Calendar.DATE, 1);
                startTimeThisMonth = c.getTimeInMillis();
                ThisMonth = DL_Reports.GetOrderTotals(startTimeThisMonth, endTimeThisMonth, 0L, 0L);

                startTimeYesterday = endTimeYesterday - (24 * 60 * 60 * 1000);
                YesterDay = DL_Reports.GetOrderTotals(startTimeYesterday, endTimeYesterday, 0L, 0L);

                try {

                    JSONObject jsonObject = wurthMB.getUser().Parameters;

                    if (!jsonObject.isNull("SalesPlan") && jsonObject.getJSONArray("SalesPlan").length() > 0) {

                        Calendar calendar = Calendar.getInstance();
                        int month = calendar.get(Calendar.MONTH) + 1;

                        for (int i = 0; i <jsonObject.getJSONArray("SalesPlan").length() ; i++ ) {
                            JSONObject jObj = jsonObject.getJSONArray("SalesPlan").getJSONObject(i);

                            if (jObj.getLong("startDate") <= System.currentTimeMillis() && jObj.getLong("endDate") >= System.currentTimeMillis()) {

                                JSONObject SalesPlan_Product_Associations = jObj.getJSONArray("SalesPlan_Product_Associations").getJSONObject(0);
                                JSONObject SalesPlan_SalesPerson_Associations = jObj.getJSONArray("SalesPlan_SalesPerson_Associations").getJSONObject(0);

                                for (int j = 0; j < jObj.getJSONArray("SalesPlan_Segment_Associations").length(); j++) {
                                    JSONObject SalesPlan_Segment_Associations = jObj.getJSONArray("SalesPlan_Segment_Associations").getJSONObject(j);

                                    if (SalesPlan_Segment_Associations.getInt("Segment") == month) {
                                        if (jObj.getInt("Kind") == 1) {
                                            ThisMonthPlaned = SalesPlan_Product_Associations.getDouble("Value") * SalesPlan_SalesPerson_Associations.getDouble("Percentage") / 100 ;
                                        }
                                        if (jObj.getInt("Kind") == 2) {
                                            ThisMonthPlaned = SalesPlan_Product_Associations.getDouble("Quantity") * SalesPlan_SalesPerson_Associations.getDouble("Percentage") / 100 ;
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }

                } catch (JSONException e) {

                }

                TaskPending = DL_Tasks.GET_TaskPending();

            }
            catch (Exception e) {

            }
            return null;
        }
    }
}
