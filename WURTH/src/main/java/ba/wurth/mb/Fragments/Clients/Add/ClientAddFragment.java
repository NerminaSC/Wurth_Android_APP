package ba.wurth.mb.Fragments.Clients.Add;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class ClientAddFragment extends Fragment {

    public JSONObject mTemp;
    public JSONObject mPartner;
    public JSONArray clientArray;

    private SpinnerItem[] clienttype_items;
    private SpinnerItem[] clientorgenizationtype_items;
    private SpinnerItem[] ethicscore_items;

    private Spinner spClientType;
    private Spinner spClientOrgenizationType;
    private Spinner spEthicScore;

    private ImageButton btnClearClients;
    private ImageButton btnClearClientAssociations;

    private SpinnerAdapter adapter_clienttype;
    private SpinnerAdapter adapter_clientorgenizationtype;
    private SpinnerAdapter adapter_ethicscore;

    private AutoCompleteTextView txbClients;
    private AutoCompleteTextView txbClientAssociation;
    private SimpleCursorAdapter mAdapterClients;
    private SimpleCursorAdapter mAdapterClientAssociation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
        mPartner = ((ClientAddActivity) getActivity()).mPartner;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (mTemp != null && !mTemp.isNull("Latitude")) ((EditText) getView().findViewById(R.id.lit_Latitude)).setText(mTemp.getString("Latitude"));
            if (mTemp != null && !mTemp.isNull("Longitude")) ((EditText) getView().findViewById(R.id.lit_Longitude)).setText(mTemp.getString("Longitude"));
        } catch (Exception e) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_add_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        txbClientAssociation = (AutoCompleteTextView) getView().findViewById(R.id.txbClientAssociation);

        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnClearClientAssociations = (ImageButton) getView().findViewById(R.id.btnClearClientAssociations);

        spClientType = (Spinner) getView().findViewById(R.id.spClientType);
        spClientOrgenizationType = (Spinner) getView().findViewById(R.id.spClientOrgenizationType);
        spEthicScore = (Spinner) getView().findViewById(R.id.spEthicScore);

        clienttype_items = new SpinnerItem[4];
        clienttype_items[0] = new SpinnerItem(0L, "", "", "");
        clienttype_items[1] = new SpinnerItem(1L, "PR", "", "");
        clienttype_items[2] = new SpinnerItem(2L, "PK", "", "");
        clienttype_items[3] = new SpinnerItem(3L, "SP", "", "");
        adapter_clienttype = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, clienttype_items);
        spClientType.setAdapter(adapter_clienttype);

        clientorgenizationtype_items = new SpinnerItem[4];
        clientorgenizationtype_items[0] = new SpinnerItem(0L, "", "", "");
        clientorgenizationtype_items[1] = new SpinnerItem(1L, "D.O.O.", "", "");
        clientorgenizationtype_items[2] = new SpinnerItem(2L, "Obrtnik", "", "");
        clientorgenizationtype_items[3] = new SpinnerItem(3L, "Fizičko lice", "", "");
        adapter_clientorgenizationtype = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, clientorgenizationtype_items);
        spClientOrgenizationType.setAdapter(adapter_clientorgenizationtype);

        ethicscore_items = new SpinnerItem[6];
        ethicscore_items[0] = new SpinnerItem(0L, "", "", "");
        ethicscore_items[1] = new SpinnerItem(1L, getString(R.string.Excelent), "", "");
        ethicscore_items[2] = new SpinnerItem(2L, getString(R.string.VeryGood), "", "");
        ethicscore_items[3] = new SpinnerItem(3L, getString(R.string.Good), "", "");
        ethicscore_items[4] = new SpinnerItem(4L, getString(R.string.Satisfying), "", "");
        ethicscore_items[5] = new SpinnerItem(5L, getString(R.string.NotSatisfying), "", "");
        adapter_ethicscore = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, ethicscore_items);
        spEthicScore.setAdapter(adapter_ethicscore);

        bindData();

        btnClearClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbClients.setText("");
                txbClients.setContentDescription("0");
                mTemp = new JSONObject();
                bindData();
            }
        });

        btnClearClientAssociations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbClientAssociation.setText("");
                txbClientAssociation.setContentDescription("0");
                bindData();
            }
        });

        mAdapterClients = new AutoCompleteClientsAdapter(getActivity());
        mAdapterClientAssociation = new AutoCompleteClientsAdapter(getActivity());

        mAdapterClients.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                try {
                    Cursor cur = DL_Clients.Get(txbClients.getText().toString());
                    return cur;
                }
                catch (Exception ex) { }
                return null;
            }
        });

        mAdapterClients.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                try {
                    int index = cur.getColumnIndex("Name");
                    return cur.getString(index);
                }
                catch (Exception ex) {}
                return "";
            }
        });

        txbClients.setAdapter(mAdapterClients);
        txbClients.setThreshold(0);

        txbClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    txbClients.setContentDescription(Long.toString(l));
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbClients.getWindowToken(), 0);
                    mPartner = Common.cur2JsonObject(DL_Wurth.GET_Partner_Details(l));
                    if (!mPartner.isNull("JSON") && !mPartner.getString("JSON").equals("")) mPartner.put("JSON", new JSONObject(mPartner.getString("JSON")));
                    else mPartner.put("JSON", new JSONObject());
                    mPartner.put("Contacts", Common.cur2JsonArray(DL_Wurth.GET_Partner_Contacts(l)));
                    mPartner.put("Branches", Common.cur2JsonArray(DL_Wurth.GET_Partner_Branches(l)));
                    //txbDeliveryPlaces.requestFocus();
                    bindPartner();
                } catch (Exception e) {
                }
            }
        });


        mAdapterClientAssociation.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                try {
                    Cursor cur = DL_Clients.Get_All(txbClientAssociation.getText().toString());
                    return cur;
                }
                catch (Exception ex) { }
                return null;
            }
        });

        mAdapterClientAssociation.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                try {
                    int index = cur.getColumnIndex("Name");
                    return cur.getString(index);
                }
                catch (Exception ex) {}
                return "";
            }
        });

        txbClientAssociation.setAdapter(mAdapterClientAssociation);
        txbClientAssociation.setThreshold(0);

        txbClientAssociation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                txbClientAssociation.setContentDescription(Long.toString(l));
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(txbClientAssociation.getWindowToken(), 0);
                saveTemp();
                //txbDeliveryPlaces.requestFocus();
            }
        });

        bindListeners();
    }

    private void bindData() {
        try {
            ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
            for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                View view = group.getChildAt(i);
                if (view instanceof TextView && view.getId() > -1 && getResources().getResourceName(view.getId()).contains("_lit_")) {
                    ((TextView) view).setText("");
                }
            }

            if (mTemp != null) {
                for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                    View view = group.getChildAt(i);

                    if (view instanceof EditText) {
                        String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                        int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                        if (!mTemp.isNull(getString(resID))) ((EditText) view).setText(mTemp.getString(getString(resID)));
                    }
                }

                if (!mTemp.isNull(getString(R.string.ClientType))) {
                    for (int i = 0; i < clienttype_items.length; i++) {
                        if (clienttype_items[i].getName().equals(mTemp.getString(getString(R.string.ClientType)))) {
                            spClientType.setSelection(i);
                            break;
                        }
                    }
                }

                if (!mTemp.isNull(getString(R.string.ClientOrganizationType))) {
                    for (int i = 0; i < clientorgenizationtype_items.length; i++) {
                        if (clientorgenizationtype_items[i].getName().equals(mTemp.getString(getString(R.string.ClientOrganizationType)))) {
                            spClientOrgenizationType.setSelection(i);
                            break;
                        }
                    }
                }

                if (!mTemp.isNull(getString(R.string.EthicScore))) {
                    for (int i = 0; i < ethicscore_items.length; i++) {
                        if (ethicscore_items[i].getName().equals(mTemp.getString(getString(R.string.EthicScore)))) {
                            spEthicScore.setSelection(i);
                            break;
                        }
                    }
                }


                if (!mTemp.isNull(getString(R.string.Competitions))) ((CheckBox) getView().findViewById(R.id.chkCompetitions)).setChecked(mTemp.getBoolean(getString(R.string.Competitions)));
                if (!mTemp.isNull(getString(R.string.ClientWholesale))) ((CheckBox) getView().findViewById(R.id.chkClientWholesale)).setChecked(mTemp.getBoolean(getString(R.string.ClientWholesale)));

                if (!mTemp.isNull("PartnerID")) txbClients.setText(mTemp.getString("PartnerName"));
                if (!mTemp.isNull("PartnerName")) txbClients.setContentDescription(mTemp.getString("PartnerID"));
                if (!mTemp.isNull("PartnerVezaName")) txbClientAssociation.setText(mTemp.getString("PartnerVezaName"));
                if (!mTemp.isNull(getString(R.string.ClientRelation))) txbClientAssociation.setContentDescription(mTemp.getString(getString(R.string.ClientRelation)));


                if (txbClients.getContentDescription() != null && !txbClients.getContentDescription().toString().equals("")) {
                    mPartner = Common.cur2JsonObject(DL_Wurth.GET_Partner_Details(Long.parseLong(txbClients.getContentDescription().toString())));
                    mPartner.put("Contacts", Common.cur2JsonArray(DL_Wurth.GET_Partner_Contacts(Long.parseLong(txbClients.getContentDescription().toString()))));
                    mPartner.put("Branches", Common.cur2JsonArray(DL_Wurth.GET_Partner_Branches(Long.parseLong(txbClients.getContentDescription().toString()))));
                }
            }

            /*if (clientArray != null && clientArray.length() > 0) {

                JSONObject client = clientArray.getJSONObject(0);

                Iterator<String> iter = client.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        Object value = client.get(key);
                        int resID = getResources().getIdentifier("_lit_" + key, "id", "ba.wurth.mb");
                        if (resID > 0 && !value.toString().toUpperCase().equals("NULL")) ((TextView) getView().findViewById(resID)).setText(value.toString());
                    } catch (Exception e) {
                    }
                }

                if (client.getInt("VrstaObveznika") == 1) ((CheckBox) getView().findViewById(R.id.chkClientWholesale)).setChecked(true);
                else ((CheckBox) getView().findViewById(R.id.chkClientWholesale)).setChecked(false);
            }*/
        }
        catch (Exception ex) {
            wurthMB.AddError("ClientAddFragment", "" ,ex);
        }
    }

    private void bindListeners() {
         try {
             ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
             for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                 View view = group.getChildAt(i);
                 if (view instanceof EditText) {
                     ((EditText) view).addTextChangedListener(new textWatcher());
                 }
             }

             spClientType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { saveTemp();}
                 @Override
                 public void onNothingSelected(AdapterView<?> adapterView) {}
             });

             spClientOrgenizationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { saveTemp();}
                 @Override
                 public void onNothingSelected(AdapterView<?> adapterView) {}
             });

             spEthicScore.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { saveTemp();}
                 @Override
                 public void onNothingSelected(AdapterView<?> adapterView) {}
             });

             getView().findViewById(R.id.chkCompetitions).setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     saveTemp();
                 }
             });

             getView().findViewById(R.id.chkClientWholesale).setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     saveTemp();
                 }
             });

         }
         catch (Exception ex) {

         }
    }

    private void bindPartner() {
        try {
            if (mPartner != null) {
                /*((EditText) getView().findViewById(R.id.lit_Address)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_Municipal)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_Place)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_PostalCode)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_IDNumber)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_PDVNumber)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_ClientCourtRegister)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_ClientMunicipalRegister)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_Bank1)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_Bank2)).setText(mPartner.getString("Adresa"));
                ((EditText) getView().findViewById(R.id.lit_Bank3)).setText(mPartner.getString("Adresa"));*/

                mTemp.put("Adresa", mPartner.getString("Adresa"));

                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("RadionickihDizalica")) mTemp.put("Radioničkih dizalica", mPartner.getJSONObject("JSON").getInt("RadionickihDizalica"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("AutoDijagnostika")) mTemp.put("Auto dijagnostika", mPartner.getJSONObject("JSON").getInt("AutoDijagnostika"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("KlimaServisnihUredjaja")) mTemp.put("Klima servisni uređaj", mPartner.getJSONObject("JSON").getInt("KlimaServisnihUredjaja"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("UredjajiGeometrijeTrapa")) mTemp.put("Uređaji geometrije trapa", mPartner.getJSONObject("JSON").getInt("UredjajiGeometrijeTrapa"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BoksoviZaPranjeVozila")) mTemp.put("Boksovi za pranje vozila", mPartner.getJSONObject("JSON").getInt("BoksoviZaPranjeVozila"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("KomoraZaLakiranje")) mTemp.put("Komora za lakiranje", mPartner.getJSONObject("JSON").getInt("KomoraZaLakiranje"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SistemMjesanjaBojeILaka")) mTemp.put("Sistem mješanja boja i laka", mPartner.getJSONObject("JSON").getInt("SistemMjesanjaBojeILaka"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("MasinaZaBalansiranjeTockova")) mTemp.put("Mašina za balansiranje točkova", mPartner.getJSONObject("JSON").getInt("MasinaZaBalansiranjeTockova"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("MasinaZaMontazuTockova")) mTemp.put("Mašina za montažu točkova", mPartner.getJSONObject("JSON").getInt("MasinaZaMontazuTockova"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("UredjajZaPranjeMotora")) mTemp.put("Uređaj za sistemsko pranje motora", mPartner.getJSONObject("JSON").getInt("UredjajZaPranjeMotora"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("UredjajZaIspitivanjeIzduvnihGasova")) mTemp.put("Uređaj za ispitivanje izduvnih gasova", mPartner.getJSONObject("JSON").getInt("UredjajZaIspitivanjeIzduvnihGasova"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("CentralniKompresorSaRazvedenimSistemom")) mTemp.put("Centralni kompresor sa razvedenim sistemom", mPartner.getJSONObject("JSON").getBoolean("CentralniKompresorSaRazvedenimSistemom"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("LinijaZaTehnickiPregledVozila")) mTemp.put("Linija za tehnički pregled ili prijem vozila", mPartner.getJSONObject("JSON").getInt("LinijaZaTehnickiPregledVozila"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Bank1")) mTemp.put("Poslovna banka 1", mPartner.getJSONObject("JSON").getString("Bank1"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Bank2")) mTemp.put("Poslovna banka 2", mPartner.getJSONObject("JSON").getString("Bank2"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Bank3")) mTemp.put("Poslovna banka 3", mPartner.getJSONObject("JSON").getString("Bank3"));

                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("DeliveryCars")) mTemp.put("Lakih dostavnih vozila", mPartner.getJSONObject("JSON").getInt("DeliveryCars"));
                mTemp.put("Ime", mPartner.getString("Naziv"));
                mTemp.put("Mjesto", mPartner.getString("Grad"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("ConstructionMachines")) mTemp.put("Građevinskih i rudarskih strojeva", mPartner.getJSONObject("JSON").getInt("ConstructionMachines"));
                mTemp.put("ID broj", mPartner.getString("IDBroj"));
                mTemp.put("PDV broj", mPartner.getString("PDVBroj"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SalesForceCars")) mTemp.put("Komercijalna vozila", mPartner.getJSONObject("JSON").getInt("SalesForceCars"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Competitor")) mTemp.put("Kompeticija", mPartner.getJSONObject("JSON").getBoolean("Competitor"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SmallCars")) mTemp.put("Putničkih vozila", mPartner.getJSONObject("JSON").getInt("SmallCars"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("MasineZaObraduMetala")) mTemp.put("Stacionarne mašine za obradu metala rezanjem", mPartner.getJSONObject("JSON").getInt("MasineZaObraduMetala"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("MasineZaObraduDrveta")) mTemp.put("Stacionarne mašine za obradu drveta", mPartner.getJSONObject("JSON").getInt("MasineZaObraduDrveta"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("MasineZaObraduOstalihMaterijala")) mTemp.put("Stacionarne mašine za obradu ostalih materijala", mPartner.getJSONObject("JSON").getInt("MasineZaObraduOstalihMaterijala"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("PneumatskeIOstaleMasine")) mTemp.put("Pneumatske i elektro mašine", mPartner.getJSONObject("JSON").getInt("PneumatskeIOstaleMasine"));

                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Description")) mTemp.put("Opis", mPartner.getJSONObject("JSON").getString("Description"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Municipality")) mTemp.put("Općina", mPartner.getJSONObject("JSON").getString("Municipality"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("RegistrationMunicipality")) mTemp.put("Broj opštinskog rješenja za samostalne obrte", mPartner.getJSONObject("JSON").getString("RegistrationMunicipality"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("FarmingCars")) mTemp.put("Poljoprivrednih i šumarskih vozila", mPartner.getJSONObject("JSON").getInt("FarmingCars"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BusinessEthics")) mTemp.put("Ocjena poslovne etike", mPartner.getJSONObject("JSON").getString("BusinessEthics"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("PostalCode")) mTemp.put("Poštanski broj", mPartner.getJSONObject("JSON").getString("PostalCode"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("UredskiProstor")) mTemp.put("Uredski prostor", mPartner.getJSONObject("JSON").getInt("UredskiProstor"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("RadionickiProstor")) mTemp.put("Radionički prostor", mPartner.getJSONObject("JSON").getInt("RadionickiProstor"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("ProdajniProstor")) mTemp.put("Prodajni prostor", mPartner.getJSONObject("JSON").getInt("ProdajniProstor"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SkladisniProstor")) mTemp.put("Skladišni prostor", mPartner.getJSONObject("JSON").getInt("SkladisniProstor"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("ProstoriKuhinje")) mTemp.put("Prostori kuhinje", mPartner.getJSONObject("JSON").getInt("ProstoriKuhinje"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("ProstoriRestorana")) mTemp.put("Prostori restorana", mPartner.getJSONObject("JSON").getInt("ProstoriRestorana"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BrojRadinickihMokrihCvorova")) mTemp.put("Broj radioničkih mokrih čvorova", mPartner.getJSONObject("JSON").getInt("BrojRadinickihMokrihCvorova"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BrojOstalihMokrihCvorova")) mTemp.put("Broj ostalih mokrih čvorova", mPartner.getJSONObject("JSON").getInt("BrojOstalihMokrihCvorova"));

                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("OtvoreniProstor")) mTemp.put("Otvoreni prostor", mPartner.getJSONObject("JSON").getInt("OtvoreniProstor"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("RadnihMjestaKanal")) mTemp.put("Radnih mjesta kanal", mPartner.getJSONObject("JSON").getInt("RadnihMjestaKanal"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("HotelRooms")) mTemp.put("Broj soba hotela", mPartner.getJSONObject("JSON").getInt("HotelRooms"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("CentralniKompresorSaRazvedenimSistemom")) mTemp.put("Centralni kompresor sa razvedenim sistemom", mPartner.getJSONObject("JSON").getBoolean("CentralniKompresorSaRazvedenimSistemom") ? "1" : "0");

                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BankAcc1")) mTemp.put("Žiro račun 1", mPartner.getJSONObject("JSON").getString("BankAcc1"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BankAcc2")) mTemp.put("Žiro račun 2", mPartner.getJSONObject("JSON").getString("BankAcc2"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("BankAcc3")) mTemp.put("Žiro račun 3", mPartner.getJSONObject("JSON").getString("BankAcc3"));

                mTemp.put("Matični broj subjekta upisa u sudski registar", mPartner.getString("BrojSudskogRegistra"));
                //if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Bank3")) mTemp.put("Šifra djelatnosti", mPartner.getJSONObject("JSON").getString("Adresa"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("OrgType")) mTemp.put("Oblik organizacije", mPartner.getJSONObject("JSON").getString("OrgType"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("PartnerType")) mTemp.put("Tip partnera", mPartner.getJSONObject("JSON").getString("PartnerType"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("Wholesale")) mTemp.put("Prijaviti partnera za veleprodaju", mPartner.getJSONObject("JSON").getBoolean("Wholesale"));
                //mTemp.put("Veza na partnera", mPartner.getJSONObject("JSON").getString("RelatedCustomerCode"));

                mTemp.put("Vlasnik", mPartner.getString("Vlasnik"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("TotalEmployees")) mTemp.put("Ukupan broj zaposlenih", mPartner.getJSONObject("JSON").getInt("TotalEmployees"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SuggestedPaymentTerm")) mTemp.put("Predloženi rok plaćanja (u danima)", mPartner.getJSONObject("JSON").getInt("SuggestedPaymentTerm"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SuggestedBorrowingLimit")) mTemp.put("Predloženi limit zaduživanja", mPartner.getJSONObject("JSON").getInt("SuggestedBorrowingLimit"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("SuggestedTradingPlan")) mTemp.put("Predloženi plan prometa", mPartner.getJSONObject("JSON").getInt("SuggestedTradingPlan"));
                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("WebAddress")) mTemp.put("WWW adresa", mPartner.getJSONObject("JSON").getString("WebAddress"));


                for (int i = 0; i < mPartner.getJSONArray("Contacts").length(); i++){

                    JSONObject item = mPartner.getJSONArray("Contacts").getJSONObject(i);
                    JSONObject client = new JSONObject();

                    client.put("Ime", item.getString("Ime"));
                    client.put("Prezime", item.getString("Prezime"));
                    client.put("Titula", item.getString("Titula"));
                    client.put("Radno mijesto", item.getString("Pozicija"));
                    //client.put("Ime", item.getString("TipKontakta"));
                    client.put("Telefon", item.getString("Broj"));
                    client.put("Email adresa", item.getString("Email"));
                    client.put("Datum rođenja", item.getString("DatumRodjenja"));

                    if (!mTemp.has(getString(R.string.Contacts))) mTemp.put(getString(R.string.Contacts), new JSONArray());
                    mTemp.getJSONArray(getString(R.string.Contacts)).put(client);

                }

                for (int i = 0; i < mPartner.getJSONArray("Branches").length(); i++){

                    JSONObject item = mPartner.getJSONArray("Branches").getJSONObject(i);
                    JSONObject client = new JSONObject();

                    client.put("Broj radnika u branši", item.getString("BrojUposlenika"));
                    client.put("Potencijal branše", item.getString("Potenencijal"));
                    client.put("Broj prodatih vozila", "0");
                    client.put("Generalni plan prometa", item.getString("UkupniPlaniraniPromet"));
                    client.put("Nivo poslovne aktivnosti", "");
                    client.put("Branša (kod ili naziv)", item.getString("Bransa"));
                    client.put("Prihod branše", item.getString("PlaniraniPrometBranse"));
                    client.put("Nivo branše", item.getString("Osnovna"));

                    if (!mTemp.has(getString(R.string.Branches))) mTemp.put(getString(R.string.Branches), new JSONArray());
                    mTemp.getJSONArray(getString(R.string.Branches)).put(client);

                }

                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("WorkingTimes")) {
                    for (int i = 0; i < mPartner.getJSONObject("JSON").getJSONArray("WorkingTimes").length(); i++){

                        JSONObject item = mPartner.getJSONObject("JSON").getJSONArray("WorkingTimes").getJSONObject(i);
                        JSONObject client = new JSONObject();

                        client.put("endTime", item.getString("EndTime"));
                        client.put("startTime", item.getString("StartTime"));
                        client.put("enabled", item.getBoolean("Enabled"));
                        client.put("day", item.getInt("Day"));

                        if (!mTemp.has("workingTime")) mTemp.put("workingTime", new JSONArray());
                        mTemp.getJSONArray("workingTime").put(client);

                    }
                }


                if (!mPartner.isNull("JSON") && !mPartner.getJSONObject("JSON").isNull("VisitTimes")) {
                    for (int i = 0; i < mPartner.getJSONObject("JSON").getJSONArray("VisitTimes").length(); i++){

                        JSONObject item = mPartner.getJSONObject("JSON").getJSONArray("VisitTimes").getJSONObject(i);
                        JSONObject client = new JSONObject();

                        client.put("endTime", item.getString("EndTime"));
                        client.put("startTime", item.getString("StartTime"));
                        client.put("enabled", item.getBoolean("Enabled"));
                        client.put("day", item.getInt("Day"));

                        if (!mTemp.has("terms")) mTemp.put("terms", new JSONArray());
                        mTemp.getJSONArray("terms").put(client);

                    }

                    /*JSONObject fixedTime = new JSONObject();
                    fixedTime.put("enabled", mPartner.getJSONObject("JSON").getBoolean("FixedTimeEnabled"));
                    fixedTime.put("startTime", mPartner.getJSONObject("JSON").getString("FixedTimeStart"));
                    fixedTime.put("endTime",  mPartner.getJSONObject("JSON").getString("FixedTimeEnd"));
                    mTemp.put("fixedTime", fixedTime);*/

                }

                mTemp.put("PartnerID", txbClients.getContentDescription().toString());
                mTemp.put("PartnerName", txbClients.getText().toString());

                Intent i = new Intent(getActivity(), ClientAddActivity.class);
                i.putExtra("mTemp", mTemp.toString());
                startActivity(i);
                getActivity().finish();
            }
        }
        catch (Exception ex) {
            wurthMB.AddError("ClientAddFragment", "" ,ex);
        }
    }

    private class textWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            try {
                saveTemp();
            }
            catch (Exception ex) {
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }



    private void saveTemp() {
        try {

            if (mTemp == null) mTemp = new JSONObject();

            ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
            for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                View view = group.getChildAt(i);
                if (view instanceof EditText) {
                    String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                    int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                    mTemp.put(getString(resID), ((EditText) view).getText().toString());
                }
            }

            mTemp.put(getString(R.string.ClientType), clienttype_items[spClientType.getSelectedItemPosition()].getName());
            mTemp.put(getString(R.string.ClientOrganizationType), clientorgenizationtype_items[spClientOrgenizationType.getSelectedItemPosition()].getName());
            mTemp.put(getString(R.string.EthicScore), ethicscore_items[spEthicScore.getSelectedItemPosition()].getName());

            mTemp.put(getString(R.string.Competitions), ((CheckBox) getView().findViewById(R.id.chkCompetitions)).isChecked());
            mTemp.put(getString(R.string.ClientWholesale), ((CheckBox) getView().findViewById(R.id.chkClientWholesale)).isChecked());

            mTemp.put(getString(R.string.ClientRelation), txbClientAssociation.getContentDescription().toString());
            mTemp.put("PartnerVezaName", txbClientAssociation.getText().toString());
            mTemp.put("PartnerID", txbClients.getContentDescription().toString());
            mTemp.put("PartnerName", txbClients.getText().toString());

        }
        catch (Exception ex) {

        }
    }
}
