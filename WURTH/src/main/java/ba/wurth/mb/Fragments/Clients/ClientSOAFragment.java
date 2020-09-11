package ba.wurth.mb.Fragments.Clients;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class ClientSOAFragment extends Fragment {

    private Button btnGet;
    private LinearLayout llContainer;
    private Long ClientID = 0L;
    private Long PartnerID = 0L;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("EEE dd.MMM.yyyy");
    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
            ClientID = getArguments().getLong("ClientID", 0L);
        }
        if (getArguments() != null && getArguments().getLong("PartnerID", 0L) > 0) {
            PartnerID = getArguments().getLong("PartnerID", 0L);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_soa, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnGet = (Button) getView().findViewById(R.id.btnGet);
        llContainer = (LinearLayout) getView().findViewById(R.id.llContainer);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LongTask().execute();
            }
        });
    }

    private class LongTask extends AsyncTask<Void, Void, JsonNode> {

        @Override
        protected void onPreExecute() {
            if (!((wurthMB) getActivity().getApplication()).isNetworkAvailable()) {
                cancel(true);
                Notifications.showNotification(getActivity(), "", getString(R.string.NoInternet), 1);
                return;
            }
            getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        @Override
        protected JsonNode  doInBackground(Void... params) {
            try {
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("customerID", Long.toString(PartnerID)));
                postParameters.add(new BasicNameValuePair("externalKey", "fd0ac005-6e2d-4ff4-8a2c-7da5070e24f5"));

                JsonFactory jfactory = new JsonFactory();
                //JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream("http://www.wurth.ba/WS/External.asmx/GetIOSJson", postParameters));
                JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpsPost("https://eshop.wurth.ba/ws/external.asmx/GetIOSJson", postParameters));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(jsonParser);
                return actualObj;
            }
            catch (Exception e) {
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                Notifications.showNotification(getActivity(), "", getString(R.string.ServiceNotAvailable),1);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JsonNode  params) {

            try {

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                TableLayout tblTotals = (TableLayout) getView().findViewById(R.id.tblTotals);
                TableLayout tblList = (TableLayout) getView().findViewById(R.id.tblList);

                tblTotals.removeAllViews();
                tblList.removeAllViews();

                View v;
                JsonNode IOS = params.get("IOS");

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.Limit));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("LimitString").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.TotalDebt));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("TotalDebtString").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.DebtWithinAgreedperiod));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("DebtWithinAgreedperiodString").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.DebtOutOfAgreedPeriod));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("DebtOutOfAgreedPeriodString").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.PaymentPeriod));
                if (IOS.get("PaymentPeriod").asText().equals("null")) ((TextView) v.findViewById(R.id.litTotal)).setText("N/A");
                else ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("PaymentPeriod").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.ExtraLimit));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("ExtraLimitString").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.ExtraLimitDate));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("ExtraLimitDateString").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.ExtraLimitPayments));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("ExtraLimitPayments").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.Cautions));
                if (IOS.get("Cautions").asText().equals("null")) ((TextView) v.findViewById(R.id.litTotal)).setText("N/A");
                else ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("Cautions").asText());
                tblTotals.addView(v);

                v = mInflater.inflate(R.layout.client_soa_totals_row, tblTotals, false);
                ((TextView) v.findViewById(R.id.litTitle)).setText(getString(R.string.CourtCharges));
                ((TextView) v.findViewById(R.id.litTotal)).setText(IOS.get("CourtCharges").asText());
                tblTotals.addView(v);

                View _v = mInflater.inflate(R.layout.client_soa_list_row, tblList, false);
                ((TextView) _v.findViewById(R.id.litOU)).setText(getString(R.string.OU));
                ((TextView) _v.findViewById(R.id.litDescription)).setText(getString(R.string.Description));
                ((TextView) _v.findViewById(R.id.litNumber)).setText(getString(R.string.Number));
                ((TextView) _v.findViewById(R.id.litDate)).setText(getString(R.string.Date));
                ((TextView) _v.findViewById(R.id.litDueDate)).setText(getString(R.string.Due_Date));
                ((TextView) _v.findViewById(R.id.litAmountToPay)).setText(getString(R.string.AmountToPay));
                ((TextView) _v.findViewById(R.id.litAmountClosed)).setText(getString(R.string.AmountClosed));
                ((TextView) _v.findViewById(R.id.litAmountOpen)).setText(getString(R.string.AmountOpen));
                ((TextView) _v.findViewById(R.id.litExceededDays)).setText(getString(R.string.ExceededDays));

                ViewGroup group = (ViewGroup) _v.findViewById(R.id.table);
                group.setBackgroundColor(Color.parseColor("#AAAAAA"));
                group.setPadding(0, 2, 0, 2);
                for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                    View temp = group.getChildAt(i);
                    if (temp instanceof TextView) {
                        ((TextView) temp).setSingleLine(false);
                        ((TextView) temp).setTextColor(Color.parseColor("#ffffff"));
                        ((TextView) temp).setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                    }
                }
                tblList.addView(_v);


                for (JsonNode item : params.get("IOSDetails")) {
                    _v = mInflater.inflate(R.layout.client_soa_list_row, tblList, false);

                    ((TextView) _v.findViewById(R.id.litOU)).setText(item.get("OU").asText());
                    if(item.get("Description").asText().equals("null")) ((TextView) _v.findViewById(R.id.litDescription)).setText("");
                    else ((TextView) _v.findViewById(R.id.litDescription)).setText(item.get("Description").asText());
                    ((TextView) _v.findViewById(R.id.litNumber)).setText(item.get("Number").asText() + " " + item.get("Number1").asText());
                    ((TextView) _v.findViewById(R.id.litDate)).setText(item.get("TimeStampString").asText());
                    ((TextView) _v.findViewById(R.id.litDueDate)).setText(item.get("DueDateString").asText());
                    ((TextView) _v.findViewById(R.id.litAmountToPay)).setText(item.get("AmountToPayString").asText());
                    ((TextView) _v.findViewById(R.id.litAmountClosed)).setText(item.get("AmountClosedString").asText());
                    ((TextView) _v.findViewById(R.id.litAmountOpen)).setText(item.get("AmountOpenString").asText());
                    ((TextView) _v.findViewById(R.id.litExceededDays)).setText(item.get("ExceededNumberOfDays").asText());

                    tblList.addView(_v);
                }
            }
            catch (Exception e) {
                Notifications.showNotification(getActivity(), "", getString(R.string.ServiceNotAvailable),1);
            }

            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);

        }
    }
}
