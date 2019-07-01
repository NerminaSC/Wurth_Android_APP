package ba.wurth.mb.Fragments.Clients.Add;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class ClientAddBranchFragment extends Fragment {

    public JSONObject mTemp;

    private Button btnAdd;

    private SpinnerItem[] branchtype1_items;
    private SpinnerItem[] branchtype2_items;
    private SpinnerItem[] branchtype2_00_items;
    private SpinnerItem[] branchtype2_01_items;
    private SpinnerItem[] branchtype3_items;

    private Spinner spBranchType1;
    private Spinner spBranchType2_00;
    private Spinner spBranchType2_01;
    private Spinner spBranchType2;
    private Spinner spBranchType6;

    private SpinnerAdapter adapter_branchtype1;
    private SpinnerAdapter adapter_branchtype2;
    private SpinnerAdapter adapter_branchtype2_00;
    private SpinnerAdapter adapter_branchtype2_01;
    private SpinnerAdapter adapter_branchtype3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_add_branch, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAdd = (Button) getView().findViewById(R.id.btnAdd);

        spBranchType1 = (Spinner) getView().findViewById(R.id.spBranchType1);
        spBranchType2 = (Spinner) getView().findViewById(R.id.spBranchType2);
        spBranchType6 = (Spinner) getView().findViewById(R.id.spBranchType6);

        spBranchType2_00 = (Spinner) getView().findViewById(R.id.spBranchType2_00);
        spBranchType2_01 = (Spinner) getView().findViewById(R.id.spBranchType2_01);

        branchtype1_items = new SpinnerItem[2];
        branchtype1_items[0] = new SpinnerItem(1L, getString(R.string.Basic), "", "");
        branchtype1_items[1] = new SpinnerItem(2L, getString(R.string.Extra), "", "");
        adapter_branchtype1 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, branchtype1_items);
        spBranchType1.setAdapter(adapter_branchtype1);

        branchtype3_items = new SpinnerItem[5];
        branchtype3_items[0] = new SpinnerItem(1L, getString(R.string.Excelent), "1", "");
        branchtype3_items[1] = new SpinnerItem(2L, getString(R.string.VeryGood), "1", "");
        branchtype3_items[2] = new SpinnerItem(3L, getString(R.string.Good), "0.8", "");
        branchtype3_items[3] = new SpinnerItem(4L, getString(R.string.Satisfying), "0.6", "");
        branchtype3_items[4] = new SpinnerItem(5L, getString(R.string.NotSatisfying), "0", "");
        adapter_branchtype3 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, branchtype3_items);
        spBranchType6.setAdapter(adapter_branchtype3);

        bindList();
        bindListeners();
    }

    private void bindListeners() {

        try {

            ((TextView) getView().findViewById(R.id.lit_BranchType3)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                @Override
                public void afterTextChanged(Editable editable) {
                    try {bindTotals();} catch (Exception e) {}
                }
            });

            ((TextView) getView().findViewById(R.id.lit_BranchType4)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                @Override
                public void afterTextChanged(Editable editable) {
                    try {bindTotals();} catch (Exception e) {}
                }
            });

            ((TextView) getView().findViewById(R.id.lit_BranchType8)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                @Override
                public void afterTextChanged(Editable editable) {
                    try {bindTotals();} catch (Exception e) {}
                }
            });


            spBranchType1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0) {
                        spBranchType6.setEnabled(true);
                    }
                    else {
                        spBranchType6.setEnabled(false);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spBranchType6.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    bindTotals();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            adapter_branchtype2 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, new SpinnerItem[0]);
            adapter_branchtype2_00 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, new SpinnerItem[0]);
            adapter_branchtype2_01 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, new SpinnerItem[0]);
            spBranchType2.setAdapter(adapter_branchtype2);
            spBranchType2_00.setAdapter(adapter_branchtype2_00);
            spBranchType2_01.setAdapter(adapter_branchtype2_01);

            Cursor cur = DL_Wurth.GET_BusinessCategory_1st();

            if (cur != null) {
                branchtype2_items = new SpinnerItem[cur.getCount()];
                while (cur.moveToNext()) {
                    branchtype2_items[cur.getPosition()] = new SpinnerItem(0L, cur.getString(cur.getColumnIndex("Naziv")), cur.getString(cur.getColumnIndex("KodBranse")), "");
                }
                cur.close();
                adapter_branchtype2 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, branchtype2_items);
                spBranchType2.setAdapter(adapter_branchtype2);
            }


            spBranchType2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Cursor cur = DL_Wurth.GET_BusinessCategory_2st(branchtype2_items[spBranchType2.getSelectedItemPosition()].getCode());

                    if (cur != null) {
                        branchtype2_00_items = new SpinnerItem[cur.getCount()];
                        while (cur.moveToNext()) {
                            branchtype2_00_items[cur.getPosition()] = new SpinnerItem(0L, cur.getString(cur.getColumnIndex("Naziv")), cur.getString(cur.getColumnIndex("KodBranse")), "");
                        }
                        cur.close();
                        adapter_branchtype2_00= new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, branchtype2_00_items);
                        spBranchType2_00.setAdapter(adapter_branchtype2_00);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            spBranchType2_00.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Cursor cur = DL_Wurth.GET_BusinessCategory_3st(branchtype2_00_items[spBranchType2_00.getSelectedItemPosition()].getCode());

                    if (cur != null) {
                        branchtype2_01_items = new SpinnerItem[cur.getCount()];
                        while (cur.moveToNext()) {
                            branchtype2_01_items[cur.getPosition()] = new SpinnerItem(0L, cur.getString(cur.getColumnIndex("Naziv")), cur.getString(cur.getColumnIndex("KodBranse")), "");
                        }
                        cur.close();
                        adapter_branchtype2_01= new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, branchtype2_01_items);
                        spBranchType2_01.setAdapter(adapter_branchtype2_01);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            spBranchType2_01.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    Cursor cur = DL_Wurth.GET_BusinessCategory_ByCode(branchtype2_01_items[spBranchType2_01.getSelectedItemPosition()].getCode());
                    Toast toast = null;
                    if (cur.moveToFirst()) {

                        switch (cur.getInt(cur.getColumnIndex("IDKalkulacije"))) {
                            case 1:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint1), Toast.LENGTH_LONG);
                                break;
                            case 2:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint2), Toast.LENGTH_LONG);
                                break;
                            case 3:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint3), Toast.LENGTH_LONG);
                                break;
                            case 4:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint4), Toast.LENGTH_LONG);
                                break;
                            case 5:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint5), Toast.LENGTH_LONG);
                                break;
                            case 6:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint6), Toast.LENGTH_LONG);
                                break;
                            case 7:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint7), Toast.LENGTH_LONG);
                                break;
                            case 8:
                                toast = Toast.makeText(getActivity(),getString(R.string.Notification_BranchHint8), Toast.LENGTH_LONG);
                                break;
                            default:break;
                        }
                        cur.close();
                    }

                    if (toast != null) {
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                    bindTotals();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });


            btnAdd.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     try {

                         if (mTemp == null) mTemp = new JSONObject();

                         if (!mTemp.has(getString(R.string.Branches))) mTemp.put(getString(R.string.Branches), new JSONArray());

                         for (int i = 0; i < mTemp.getJSONArray(getString(R.string.Branches)).length(); i++) {
                             JSONObject jsonObject = mTemp.getJSONArray(getString(R.string.Branches)).getJSONObject(i);
                             if (jsonObject.getString(getString(R.string.BranchType2)).equals(branchtype2_01_items[spBranchType2_01.getSelectedItemPosition()].getCode())) {
                                 Notifications.showNotification(getActivity(), "", getString(R.string.Notification_BranchExists), 2);
                                 return;
                             }
                             if (jsonObject.getString(getString(R.string.BranchType1)).equals(branchtype1_items[spBranchType1.getSelectedItemPosition()].getName()) && spBranchType1.getSelectedItemPosition() == 0) {
                                 Notifications.showNotification(getActivity(), "", getString(R.string.Notification_BranchBaseExists), 2);
                                 return;
                             }
                         }

                         JSONObject branch = new JSONObject();

                         ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
                         for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                             View view = group.getChildAt(i);
                             if (view instanceof EditText) {
                                 String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                                 int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                                 branch.put(getString(resID), ((EditText) view).getText().toString());
                             }
                         }

                         branch.put(getString(R.string.BranchType1), branchtype1_items[spBranchType1.getSelectedItemPosition()].getName());
                         branch.put(getString(R.string.BranchType2), branchtype2_01_items[spBranchType2_01.getSelectedItemPosition()].getCode());
                         branch.put(getString(R.string.BranchType6), branchtype3_items[spBranchType6.getSelectedItemPosition()].getName());


                         if (branch.getString(getString(R.string.BranchType3)).equals("")
                                 || branch.getString(getString(R.string.BranchType4)).equals("")
                                 || branch.getString(getString(R.string.BranchType5)).equals("")
                                 || branch.getString(getString(R.string.BranchType7)).equals("") ) {
                             Notifications.showNotification(getActivity(), "", getString(R.string.Notification_MissingField), 2);
                             return;
                         }

                         mTemp.getJSONArray(getString(R.string.Branches)).put(branch);

                         bindList();

                         for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                             View view = group.getChildAt(i);
                             if (view instanceof EditText) {
                                 ((EditText) view).setText("");
                             }
                         }
                     }
                     catch (Exception ex) {
                        ex.printStackTrace();
                     }
                 }
             });
         }
         catch (Exception ex) {

         }
    }

    private void bindTotals() {
        try {

            if (branchtype2_01_items == null) return;

            Cursor cur = DL_Wurth.GET_BusinessCategory_ByCode(branchtype2_01_items[spBranchType2_01.getSelectedItemPosition()].getCode());
            if (cur != null) {
                if (cur.moveToFirst()) {

                    Double potention = 0D;
                    Double total = 0D;
                    Double FaktorKalkulacije = cur.getDouble(cur.getColumnIndex("FaktorKalkulacije"));

                    switch (cur.getInt(cur.getColumnIndex("IDKalkulacije"))) {
                        case 1:
                            if (!((TextView) getView().findViewById(R.id.lit_BranchType3)).getText().equals("")) {
                                potention = Double.parseDouble(((TextView) getView().findViewById(R.id.lit_BranchType3)).getText().toString()) * FaktorKalkulacije;
                                total = potention * Double.parseDouble(branchtype3_items[spBranchType6.getSelectedItemPosition()].getCode());
                            }
                            break;
                        case 2:
                            if (!mTemp.isNull(getString(R.string.AutoServiceType4))) {
                                potention = mTemp.getDouble(getString(R.string.AutoServiceType4)) * FaktorKalkulacije;
                                total = potention * Double.parseDouble(branchtype3_items[spBranchType6.getSelectedItemPosition()].getCode());
                            }
                            break;
                        case 3:
                            if (!mTemp.isNull(getString(R.string.AutoServiceType6))) {
                                potention = mTemp.getDouble(getString(R.string.AutoServiceType6)) * FaktorKalkulacije;
                                total = potention * Double.parseDouble(branchtype3_items[spBranchType6.getSelectedItemPosition()].getCode());
                            }
                            break;
                        case 4:
                            if (!mTemp.isNull(getString(R.string.VehicleType1)) && !mTemp.isNull(getString(R.string.VehicleType2)) ) {
                                potention = (mTemp.getDouble(getString(R.string.VehicleType1)) + mTemp.getDouble(getString(R.string.VehicleType2))) * FaktorKalkulacije;
                                total = potention * Double.parseDouble(branchtype3_items[spBranchType6.getSelectedItemPosition()].getCode());
                            }
                            break;
                        case 5:
                            if (!mTemp.isNull(getString(R.string.VehicleType3)) && !mTemp.isNull(getString(R.string.VehicleType4)) && !mTemp.isNull(getString(R.string.VehicleType5))) {
                                potention = (mTemp.getDouble(getString(R.string.VehicleType3)) + mTemp.getDouble(getString(R.string.VehicleType4)) + mTemp.getDouble(getString(R.string.VehicleType5))) * FaktorKalkulacije;
                                total = potention * Double.parseDouble(branchtype3_items[spBranchType6.getSelectedItemPosition()].getCode());
                            }
                            break;
                        case 6:
                            if (!((TextView) getView().findViewById(R.id.lit_BranchType4)).getText().equals("")) {
                                potention =  Double.parseDouble(((TextView) getView().findViewById(R.id.lit_BranchType4)).getText().toString()) * FaktorKalkulacije;
                                total = potention;
                            }
                            break;
                        case 7:
                            if (!((TextView) getView().findViewById(R.id.lit_BranchType8)).getText().equals("")) {
                                potention =  Double.parseDouble(((TextView) getView().findViewById(R.id.lit_BranchType8)).getText().toString()) * FaktorKalkulacije;
                                total = potention;
                            }
                            break;
                        case 8:
                            if (!mTemp.isNull(getString(R.string.MachineParkType1))) {
                                potention = (mTemp.getDouble(getString(R.string.MachineParkType1))) * FaktorKalkulacije;
                                total = potention * Double.parseDouble(branchtype3_items[spBranchType6.getSelectedItemPosition()].getCode());
                            }
                            break;
                        default:break;
                    }

                    ((TextView) getView().findViewById(R.id.lit_BranchType5)).setText(Double.toString(potention));
                    ((TextView) getView().findViewById(R.id.lit_BranchType7)).setText(Double.toString(total));

                }
            }
        } catch (Exception e) {

        }
    }

    private void bindList() {
        try {

            if (mTemp != null && mTemp.has(getString(R.string.Branches))) {

                final JSONArray jsonArray = mTemp.getJSONArray(getString(R.string.Branches));

                TableLayout tblBranches = (TableLayout) getView().findViewById(R.id.tblBranches);

                while (tblBranches.getChildCount() > 3) {
                    TableRow row =  (TableRow) tblBranches.getChildAt(3);
                    tblBranches.removeView(row);
                }

                if (tblBranches.getChildCount() > 0 ) {
                    ((TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(1)).setText("0");
                    ((TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(2)).setText("0");
                    ((TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(3)).setText("0");
                    ((TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(5)).setText("0");
                }

                for(int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (tblBranches.getChildCount() > 0 ) {

                        if (jsonObject.getString(getString(R.string.BranchType1)).equals(branchtype1_items[0].getName())) {
                            TextView number6 = (TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(0);
                            number6.setText(jsonObject.getString(getString(R.string.BranchType2)));
                        }

                        TextView number0 = (TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(4);
                        number0.setText(jsonObject.getString(getString(R.string.BranchType6)));

                        TextView number1 = (TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(1);
                        number1.setText(Integer.toString(Integer.parseInt(number1.getText().toString()) + jsonObject.getInt(getString(R.string.BranchType3))));

                        TextView number2 = (TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(2);
                        number2.setText(Integer.toString(Integer.parseInt(number2.getText().toString()) + jsonObject.getInt(getString(R.string.BranchType4))));

                        TextView number3 = (TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(3);
                        number3.setText(Integer.toString(Integer.parseInt(number3.getText().toString()) + jsonObject.getInt(getString(R.string.BranchType5))));

                        TextView number5 = (TextView) ((TableRow) tblBranches.getChildAt(1)).getChildAt(5);
                        number5.setText(Integer.toString(Integer.parseInt(number5.getText().toString()) + jsonObject.getInt(getString(R.string.BranchType7))));
                    }

                    TableRow row = new TableRow(getActivity());
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
                    row.setLayoutParams(lp);

                    TextView tv1 = new TextView(getActivity());
                    tv1.setText(jsonObject.getString(getString(R.string.BranchType2)));
                    tv1.setTextColor(Color.BLACK);
                    tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textSizeSmall));
                    tv1.setPadding(5, 5, 5, 5);
                    tv1.setGravity(Gravity.CENTER);
                    tv1.setSingleLine(false);
                    tv1.setEllipsize(null);
                    tv1.setLines(2);
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv1);

                    TextView tv2 = new TextView(getActivity());
                    tv2.setText(jsonObject.getString(getString(R.string.BranchType3)));
                    tv2.setTextColor(Color.BLACK);
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textSizeSmall));
                    tv2.setPadding(5, 5, 5, 5);
                    tv2.setGravity(Gravity.CENTER);
                    tv2.setSingleLine(false);
                    tv2.setEllipsize(null);
                    tv2.setLines(2);
                    tv2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv2);

                    TextView tv3 = new TextView(getActivity());
                    tv3.setText(jsonObject.getString(getString(R.string.BranchType4)));
                    tv3.setTextColor(Color.BLACK);
                    tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textSizeSmall));
                    tv3.setPadding(5, 5, 5, 5);
                    tv3.setGravity(Gravity.CENTER);
                    tv3.setSingleLine(false);
                    tv3.setEllipsize(null);
                    tv3.setLines(2);
                    tv3.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv3);

                    TextView tv4 = new TextView(getActivity());
                    tv4.setText(jsonObject.getString(getString(R.string.BranchType5)));
                    tv4.setTextColor(Color.BLACK);
                    tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textSizeSmall));
                    tv4.setPadding(5, 5, 5, 5);
                    tv4.setGravity(Gravity.CENTER);
                    tv4.setSingleLine(false);
                    tv4.setEllipsize(null);
                    tv4.setLines(2);
                    tv4.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv4);

                    TextView tv5 = new TextView(getActivity());
                    tv5.setText(jsonObject.getString(getString(R.string.BranchType7)));
                    tv5.setTextColor(Color.BLACK);
                    tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textSizeSmall));
                    tv5.setPadding(5, 5, 5, 5);
                    tv5.setGravity(Gravity.CENTER);
                    tv5.setSingleLine(false);
                    tv5.setEllipsize(null);
                    tv5.setLines(2);
                    tv5.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv5);

                    TextView tv6 = new TextView(getActivity());
                    tv6.setText("");
                    tv6.setTextColor(Color.BLACK);
                    tv6.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textSizeSmall));
                    tv6.setPadding(5, 5, 5, 5);
                    tv6.setGravity(Gravity.CENTER);
                    tv6.setSingleLine(false);
                    tv6.setEllipsize(null);
                    tv6.setLines(2);
                    tv6.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv6);                    
                    
                    row.setTag(i);

                    row.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            try {
                                mTemp.put(getString(R.string.Branches), Common.remove(Integer.parseInt(view.getTag().toString()), jsonArray));
                                bindList();
                            } catch (Exception exx) {

                            }
                            return true;
                        }
                    });

                    tblBranches.addView(row);
                }
            }
        }
        catch (Exception ex) {

        }
    }
}
