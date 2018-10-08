package ba.wurth.mb.Fragments.Clients;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ClientBusinessCategoryFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_business_category, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binData();
    }

    private void binData() {
        try {

            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                Cursor cur = DL_Wurth.GET_ClientBusinessCategory(getArguments().getLong("ClientID"));

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                if (cur != null) {
                    LinearLayout ll = (LinearLayout) getView().findViewById(R.id.ll);
                    ll.removeAllViews();

                    while (cur.moveToNext()) {

                        String desc = "";

                        desc += "<strong>" + getString(R.string.NumberOfUsers) + ": </strong>" + (cur.isNull(cur.getColumnIndex("BrojUposlenika")) ? "N/A" : cur.getString(cur.getColumnIndex("BrojUposlenika"))) + "<br />";
                        desc += "<strong>" + getString(R.string.SalesPerson) + ": </strong>" + (cur.isNull(cur.getColumnIndex("Komercijalista")) ? "N/A" : cur.getString(cur.getColumnIndex("Komercijalista"))) + "<br />";
                        desc += "<strong>" + getString(R.string.Revenue) + ": </strong>" + (cur.isNull(cur.getColumnIndex("PlaniraniPrometBranse")) ? "N/A" : cur.getString(cur.getColumnIndex("PlaniraniPrometBranse"))) + "<br />";
                        desc += "<strong>" + getString(R.string.GeneralRevenue) + ": </strong>" + (cur.isNull(cur.getColumnIndex("UkupniPlaniraniPromet")) ? "N/A" : cur.getString(cur.getColumnIndex("UkupniPlaniraniPromet"))) + "<br />";
                        desc += "<strong>" + getString(R.string.DayOfVisit) + ": </strong>" + (cur.isNull(cur.getColumnIndex("DanPosjete")) ? "N/A" : cur.getString(cur.getColumnIndex("DanPosjete"))) + "<br />";
                        desc += "<strong>" + getString(R.string.VisitFrequency) + ": </strong>" + (cur.isNull(cur.getColumnIndex("FrekvencijaPosjete")) ? "N/A" : cur.getString(cur.getColumnIndex("FrekvencijaPosjete")));

                        View v = mInflater.inflate(R.layout.list_row, ll, false);
                        ((TextView) v.findViewById(R.id.litTitle)).setText(cur.isNull(cur.getColumnIndex("NazivBranse")) ? "N/A" : cur.getString(cur.getColumnIndex("NazivBranse")));
                        ((TextView) v.findViewById(R.id.litSupTitle)).setText(cur.isNull(cur.getColumnIndex("Bransa")) ? "N/A" : cur.getString(cur.getColumnIndex("Bransa")));
                        ((TextView) v.findViewById(R.id.litTotal)).setText(getString(R.string.Potention) + ": " + (cur.isNull(cur.getColumnIndex("Potencijal")) ? "N/A" : cur.getString(cur.getColumnIndex("Potencijal"))));
                        ((TextView) v.findViewById(R.id.litSupTotal)).setText(getString(R.string.Status) + ": " + (cur.isNull(cur.getColumnIndex("Osnovna")) ? "N/A" : cur.getString(cur.getColumnIndex("Osnovna"))));
                        ((TextView) v.findViewById(R.id.litSubTitle)).setText(Html.fromHtml(desc));

                        v.findViewById(R.id.litSupTitle).setVisibility(View.VISIBLE);

                        ll.addView(v);
                        ll.addView(mInflater.inflate(R.layout.divider, ll, false));
                    }
                    cur.close();
                }
            }
        }
        catch (Exception ex) {

        }
    }
}
