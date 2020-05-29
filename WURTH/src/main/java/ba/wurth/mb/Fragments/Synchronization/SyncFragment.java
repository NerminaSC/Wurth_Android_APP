package ba.wurth.mb.Fragments.Synchronization;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Sync.DL_Sync;
import ba.wurth.mb.R;

public class SyncFragment extends Fragment {

    private Spinner spSections;
    private Button btnStart;
    private TextView litProgress;
    private CheckBox chkOnlyUpdates;
    private CheckBox chkLastMonth;
    private boolean busy = false;
    private String retString = "";
    public SyncTask syncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.synchronization, container, false);

        String[] array_spinner = new String[13];
        array_spinner[0]=getString(R.string.AllSections).toUpperCase();
        array_spinner[1]=getString(R.string.Clients).toUpperCase();
        array_spinner[2]=getString(R.string.Products).toUpperCase();
        array_spinner[3]=getString(R.string.PriceList).toUpperCase();
        array_spinner[4]=getString(R.string.Orders).toUpperCase();
        array_spinner[5]=getString(R.string.ADDITIONAL).toUpperCase();
        array_spinner[6]=getString(R.string.Visits).toUpperCase();
        array_spinner[7]=getString(R.string.Activities).toUpperCase();
        array_spinner[8]=getString(R.string.Documents).toUpperCase();
        array_spinner[9]=getString(R.string.Users).toUpperCase();
        array_spinner[10]=getString(R.string.Actions).toUpperCase();
        array_spinner[11]=getString(R.string.Branches).toUpperCase();
        array_spinner[12]=getString(R.string.Routes).toUpperCase();

        spSections = (Spinner) v.findViewById(R.id.spSections);
        ArrayAdapter<?> adapter = new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_spinner_item, array_spinner);
        adapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
        spSections.setAdapter(adapter);

        spSections.setSelection(3);

        btnStart = (Button) v.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!busy)  {
                    syncTask = new SyncTask();
                    syncTask.execute(spSections.getSelectedItemPosition());
                }
            }
        });

        litProgress = (TextView) v.findViewById(R.id.litProgress);
        chkOnlyUpdates = (CheckBox) v.findViewById(R.id.chkOnlyUpdates);
        chkLastMonth = (CheckBox) v.findViewById(R.id.chkLastMonth);

        return v;
    }


    public class SyncTask extends AsyncTask<Integer, String, Integer> {
        @Override
        protected void onPreExecute() {

            wurthMB.loadComplete = false;

            litProgress.setText(getString(R.string.CheckInternet));

            if (((wurthMB) getActivity().getApplication()).isNetworkAvailable()){
                litProgress.setText(getString(R.string.InternetAvailable));
                litProgress.setText(getString(R.string.StartingSynchronization));
            }
            else {
                litProgress.setText(getString(R.string.NoInternet));
                cancel(true);
            }

            if (chkLastMonth.isChecked()) wurthMB.loadMonth = true;
            else wurthMB.loadMonth = false;

            if (chkOnlyUpdates.isChecked()) wurthMB.loadComplete = false;
            else wurthMB.loadComplete = true;

            btnStart.setVisibility(View.INVISIBLE);

            DL_Sync.mThreadReference = this;
            busy = true;

            getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);

        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            int ret = 0;
            int currentRet;

            try {
                switch (integers[0]) {
                    case 0:
                        retString = getString(R.string.ClientsSynchronized);
                        retString = "";
                        currentRet = DL_Sync.Load_Clients(wurthMB.getUser().UserID, wurthMB.loadComplete);
                        ret += currentRet;

                        retString = getString(R.string.ProductsSynchronized);
                        currentRet = DL_Sync.Load_Products(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = getString(R.string.PricelistSynchronized);
                        retString = "";
                        currentRet = DL_Sync.Load_Pricelist(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = getString(R.string.OrdersSynchronized);
                        currentRet = DL_Sync.Load_Orders(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = getString(R.string.AdditionalSynchronized);
                        currentRet = DL_Sync.Load_Additional();
                        ret += currentRet;

                        retString = getString(R.string.VisitsSynchronized);
                        currentRet = DL_Sync.Load_Visits(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = "Sinhronizovano ruta";
                        currentRet = DL_Sync.Load_Routes(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = getString(R.string.Activities);
                        currentRet = DL_Sync.LOAD_Activites();
                        DL_Sync.LOAD_UserActivityLog();
                        ret += currentRet;

                        if (wurthMB.USE_3G_DOCUMENTS || (!wurthMB.USE_3G_DOCUMENTS && !wurthMB.MOBILE_DATA)) {
                            retString = getString(R.string.Documents);
                            doProgress(Integer.toString(0));
                            currentRet = DL_Sync.Load_Documents(wurthMB.getUser().UserID);
                            ret += currentRet;
                        }

                        retString = getString(R.string.Users);
                        currentRet = DL_Sync.Load_Users(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = getString(R.string.Actions);
                        currentRet = DL_Sync.Load_Actions(wurthMB.getUser().UserID);
                        ret += currentRet;

                        retString = getString(R.string.Branches);
                        currentRet = DL_Sync.Load_Branches();
                        ret += currentRet;

                        break;

                    case 1:
                        retString = getString(R.string.ClientsSynchronized);
                        retString = "";
                        currentRet = DL_Sync.Load_Clients(wurthMB.getUser().UserID, wurthMB.loadComplete);
                        ret += currentRet;
                        break;

                    case 2:
                        retString = getString(R.string.ProductsSynchronized);
                        currentRet = DL_Sync.Load_Products(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    case 3:
                        retString = getString(R.string.PricelistSynchronized);
                        retString = "";
                        currentRet = DL_Sync.Load_Pricelist(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    case 4:
                        retString = getString(R.string.OrdersSynchronized);
                        currentRet = DL_Sync.Load_Orders(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    case 5:
                        retString = getString(R.string.AdditionalSynchronized);
                        currentRet = DL_Sync.Load_Additional();
                        ret += currentRet;
                        break;

                    case 6:
                        retString = getString(R.string.VisitsSynchronized);
                        currentRet = DL_Sync.Load_Visits(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    case 7:
                        retString = getString(R.string.Activities);
                        currentRet = DL_Sync.LOAD_Activites();
                        DL_Sync.LOAD_UserActivityLog();
                        ret += currentRet;
                        break;

                    case 8:
                        if (wurthMB.USE_3G_DOCUMENTS || (!wurthMB.USE_3G_DOCUMENTS && !wurthMB.MOBILE_DATA)) {
                            retString = getString(R.string.Documents);
                            doProgress(Integer.toString(0));
                            currentRet = DL_Sync.Load_Documents(wurthMB.getUser().UserID);
                            ret += currentRet;
                        }
                        break;

                    case 9:
                        retString = getString(R.string.Users);
                        currentRet = DL_Sync.Load_Users(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    case 10:
                        retString = getString(R.string.Actions);
                        currentRet = DL_Sync.Load_Actions(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    case 11:
                        retString = getString(R.string.Branches);
                        currentRet = DL_Sync.Load_Branches();
                        ret += currentRet;
                        break;


                    case 12:
                        retString = getString(R.string.Routes);
                        currentRet = DL_Sync.Load_Routes(wurthMB.getUser().UserID);
                        ret += currentRet;
                        break;

                    default:
                        break;
                }

                if (wurthMB.dbHelper.getDB().inTransaction()) wurthMB.dbHelper.getDB().endTransaction();

            }
            catch (Exception ex) {
                busy = false;
                wurthMB.AddError("Sync", ex.getMessage(), ex);
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            try {
                if (getView() != null ) {
                    litProgress.setText(getString(R.string.ItemsSynchronized) + ": " + Integer.toString(integer));
                    busy = false;
                    btnStart.setVisibility(View.VISIBLE);
                    DL_Sync.mThreadReference = null;
                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Sync", ex.getMessage(), ex);
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (getView() != null) litProgress.setText(values[0]);
        }

        public void doProgress(String str) {
            publishProgress(retString + ": " + str);
        }
    }
}
