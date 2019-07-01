package ba.wurth.mb.Fragments.Clients;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import ba.wurth.mb.Activities.Clients.ClientActivity;
import ba.wurth.mb.Adapters.ClientsAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.R;

public class ClientsFragment extends Fragment {

    private ClientsAdapter adapter = new ClientsAdapter();
    private GestureDetector mGestureDetector;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private static float sideIndexX;
    private static float sideIndexY;
    private int indexListSize;
    private ListView mListView;

    private EditText txbSearch;
    private ImageButton btnClear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_alphabet, container, false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (ListView) getView().findViewById(R.id.list);
        mGestureDetector = new GestureDetector(getActivity(), new SideIndexGestureListener());
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);

        txbSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                handler.removeMessages(TRIGGER_SEARCH);
                handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbSearch.setText("");
            }
        });

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
        else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = sideIndexX - distanceX;
            sideIndexY = sideIndexY - distanceY;

            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public void updateList() {

        try {

            LinearLayout sideIndex = (LinearLayout) getView().findViewById(R.id.sideIndex);
            sideIndex.removeAllViews();
            indexListSize = alphabet.size();
            if (indexListSize < 1) {
                return;
            }

            int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
            int tmpIndexListSize = indexListSize;
            while (tmpIndexListSize > indexMaxSize) {
                tmpIndexListSize = tmpIndexListSize / 2;
            }
            double delta;
            if (tmpIndexListSize > 0) {
                delta = indexListSize / tmpIndexListSize;
            } else {
                delta = 1;
            }

            TextView tmpTV;
            for (double i = 1; i <= indexListSize; i = i + delta) {
                Object[] tmpIndexItem = alphabet.get((int) i - 1);
                String tmpLetter = tmpIndexItem[0].toString();

                tmpTV = new TextView(getActivity());
                tmpTV.setText(tmpLetter);
                tmpTV.setGravity(Gravity.CENTER);
                //tmpTV.setTextSize(getResources().getDimension(R.dimen.textSizeSmall));
                tmpTV.setTextColor(Color.parseColor("#FFFFFF"));
                tmpTV.setPadding(10,0,10,0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                tmpTV.setLayoutParams(params);
                sideIndex.addView(tmpTV);
            }

            sideIndexHeight = sideIndex.getHeight();

            sideIndex.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // now you know coordinates of touch
                    sideIndexX = event.getX();
                    sideIndexY = event.getY();

                    // and can display a proper item it country list
                    displayListItem();

                    return true;
                }
            });
        }
        catch (Exception ex) {

        }
    }

    public void displayListItem() {
        try {
            LinearLayout sideIndex = (LinearLayout) getView().findViewById(R.id.sideIndex);
            sideIndexHeight = sideIndex.getHeight();
            // compute number of pixels for every side index item
            double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

            // compute the item index for given event position belongs to
            int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

            // get the item (we can do it since we know item index)
            if (itemPosition < alphabet.size()) {
                Object[] indexItem = alphabet.get(itemPosition);
                int subitemPosition = sections.get(indexItem[0]);

                //ListView listView = (ListView) findViewById(android.R.id.list);
                mListView.setSelection(subitemPosition);
            }
        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            try {
                getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
            }
            catch (Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                Cursor cur = DL_Clients.Get(txbSearch.getText().toString());
                List<ClientsAdapter.Row> rows = new ArrayList<ClientsAdapter.Row>();
                int start = 0;
                int end = 0;
                String previousLetter = null;
                Object[] tmpIndexItem = null;
                Pattern numberPattern = Pattern.compile("[0-9]");
                alphabet.clear();

                if (cur != null) {

                    while(cur.moveToNext()) {
                        String title = cur.getString(cur.getColumnIndex("Name"));
                        String subtitle = cur.getString(cur.getColumnIndex("Address"));
                        String desc = cur.getString(cur.getColumnIndex("Code")) + "<br />" + cur.getString(cur.getColumnIndex("Address"));
                        Long clientid = cur.getLong(cur.getColumnIndex("ClientID"));
                        Long _id = cur.getLong(cur.getColumnIndex("_id"));
                        Long DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));

                        String firstLetter = title.substring(0, 1);

                        // Group numbers together in the scroller
                        if (numberPattern.matcher(firstLetter).matches()) {
                            firstLetter = "#";
                        }

                        // If we've changed to a new letter, add the previous letter to the alphabet scroller
                        if (previousLetter != null && !firstLetter.equals(previousLetter)) {
                            end = rows.size() - 1;
                            tmpIndexItem = new Object[3];
                            tmpIndexItem[0] = previousLetter.toUpperCase(wurthMB.getLocale());
                            tmpIndexItem[1] = start;
                            tmpIndexItem[2] = end;
                            alphabet.add(tmpIndexItem);

                            start = end + 1;
                        }

                        // Check if we need to add a header row
                        if (!firstLetter.equals(previousLetter)) {
                            rows.add(new ClientsAdapter.Section(firstLetter));
                            sections.put(firstLetter, start);
                        }

                        // Add the country to the list
                        rows.add(new ClientsAdapter.Item(title, subtitle, desc, clientid, DeliveryPlaceID, _id));
                        previousLetter = firstLetter;
                    }

                    if (previousLetter != null) {
                        // Save the last letter
                        tmpIndexItem = new Object[3];
                        tmpIndexItem[0] = previousLetter.toUpperCase(wurthMB.getLocale());
                        tmpIndexItem[1] = start;
                        tmpIndexItem[2] = rows.size() - 1;
                        alphabet.add(tmpIndexItem);
                    }
                    adapter.setRows(rows);
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Clients", ex.getMessage(), ex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {

            try {

                mListView.setAdapter(adapter);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        try {
                            Long _id = ((ClientsAdapter.Item) adapter.getItem(i))._id;
                            Long ClientID = ((ClientsAdapter.Item) adapter.getItem(i)).ClientID;
                            Long DeliveryPlaceID = ((ClientsAdapter.Item) adapter.getItem(i)).DeliveryPlaceID;

                            if (ClientID > 0) {
                                Intent k = new Intent(getActivity(), ClientActivity.class);
                                k.putExtra("_id", _id);
                                k.putExtra("ClientID", ClientID);
                                k.putExtra("DeliveryPlaceID", DeliveryPlaceID);
                                startActivity(k);
                            }
                        }
                        catch (Exception ex) {

                        }
                    }
                });

                updateList();
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            }
            catch (Exception ex) {
                wurthMB.AddError("Clients", ex.getMessage(), ex);
            }
        }
    }

    private final int TRIGGER_SEARCH = 1;
    private final long SEARCH_TRIGGER_DELAY_IN_MS = 1000;

    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SEARCH) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    new LongTask().execute();
                } else {
                    new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    };
}
