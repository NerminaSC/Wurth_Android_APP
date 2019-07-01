package ba.wurth.mb.DataLayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ba.wurth.mb.Activities.IntroActivity.checkDatabase;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.SqliteAsset.SQLiteAssetHelper;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteStatement;


public class DBHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "wurthMB.db";
    private static final int DATABASE_VERSION = 1;
    private static SQLiteDatabase db = null;
    private static SQLiteDatabase db_readonly = null;

    public checkDatabase mThreadReference = null;

	
	private static final String TABLE_LOGINS = "Logins";
	private static final String TABLE_CLIENTS = "Clients";
	private static final String TABLE_VISITS = "Visits";
	private static final String TABLE_DOCUMENTS = "Documents";
	
	
	private static final String TABLE_PRODUCT_DOCUMENTS = "ProductDocuments";
	private static final String TABLE_PRODUCT_CATEGORIES = "ProductCategories";
	private static final String TABLE_PRODUCTS = "Products";
	private static final String TABLE_PRODUCT_CATEGORY_ASSOCIATIONS = "ProductCategoryAssociations";
    private static final String TABLE_PRODUCT_BRANDS = "ProductBrands";
    private static final String TABLE_PRODUCT_BRAND_ASSOCIATIONS = "ProductBrandAssociations";
    private static final String TABLE_PRODUCT_ASSORTMENTS = "ProductAssortments";
    private static final String TABLE_PRODUCT_TYPES = "ProductTypes";
	
	private static final String TABLE_ORDERS = "Orders";
	private static final String TABLE_ORDER_ITEMS = "OrderItems";
	private static final String TABLE_ORDERSTATUS = "OrderStatus";
	
	
	private static final String TABLE_PRICELIST = "PriceList";

	
	private static final String TABLE_UOM = "UOM";
	private static final String TABLE_PAYMENT_METHODS= "PaymentMethods";
	private static final String TABLE_CLIENT_PAYMENT_METHODS= "ClientPaymentMethods";
	private static final String TABLE_PAYMENT_DATES = "PaymentDates";
	private static final String TABLE_CLIENT_PAYMENT_DATES = "ClientPaymentDates";
	private static final String TABLE_TAX= "Tax";
	private static final String TABLE_MERCHANDISE= "Merchandise";
	
	
	private static final String TABLE_DISCOUNT_GROUPS= "DiscountGroups";
	private static final String TABLE_DISCOUNT_GROUP_CLIENTS= "DiscountGroupClients";
	private static final String TABLE_DISCOUNT_GROUP_PRODUCTS= "DiscountGroupProducts";
	private static final String TABLE_DISCOUNT_GROUP_ACTION= "DiscountGroupActions";
	private static final String TABLE_DISCOUNT_CLIENT_PRODUCTS= "DiscountClientProducts";
	private static final String TABLE_DISCOUNT_GROUP_ACTION_CLIENTS= "DiscountGroupActionClients";
	private static final String TABLE_DISCOUNT_GROUP_ACTION_PRODUCTS= "DiscountGroupActionProducts";
	
	
	private static final String TABLE_DELIVERY_PLACES = "DeliveryPlaces";
	private static final String TABLE_CLIENT_DELIVERY_PLACES_PROPERTIES = "ClientDeliveryPlacesProperties";
	private static final String TABLE_DELIVERY_PLACES_PROPERTIES = "DeliveryPlacesProperties";
	private static final String TABLE_DELIVERY_PLACES_PROPERTIES_OPTIONS = "DeliveryPlacesPropertiesOptions";
	private static final String TABLE_DELIVERY_PLACES_MERCHANDISE = "DeliveryPlaceMerchandise";
	private static final String TABLE_DELIVERY_PLACES_PRODUCT_PLACEMENT = "DeliveryPlaceProductPlacement";
	
	
	private static final String TABLE_GPS = "GPS";
	private static final String TABLE_ROUTES = "Routes";
	private static final String TABLE_NOTIFICATION_MESSAGES = "NotificationMessages";
	private static final String TABLE_TEMP = "TempTable";
	private static final String TABLE_GLOBAL_VARIABLES = "GlobalVariables";
	
	
	private static final String TABLE_COMPETITIONS  = "Competitions";
	private static final String TABLE_COMPETITION_ITEMS  = "Competition_Items";
	private static final String TABLE_COMPETITION_ITEMS_VALUES  = "Competition_Items_Values";

    private static final String TABLE_TASKS = "Tasks";
    private static final String TABLE_TASK_LOG = "TaskLog";


    private static final String LOGINS_CREATE = "create table "
            + TABLE_LOGINS + " (_id integer primary key autoincrement , "
            + " AccountSettings text, UserSettings text, UserID integer, _userid integer, AccountID integer, AccountName text, AccessLevelID integer, Firstname text , "
            + " Lastname text , EmailAddress text , Password text , "
            + " LoginDate integer , SignOutDate integer, Latitude integer, "
            + " Longitude integer, DiscountPercentage real, PaymentDelay integer, hasDeliveryPlaces integer, DeliveryDelay integer, Language text, URL text, Parameters text );";

    private static final String DOCUMENTS_CREATE = "create table "
            + TABLE_DOCUMENTS + " (_id integer primary key autoincrement , "
            + "DocumentID integer, AccountID integer, UserID integer, Type integer, dt integer, DocumentType integer, fileName text, fileSize integer, fileContentType text, data blob, Name text, Description text, Latitude integer, Longitude integer, OptionID integer, ItemID integer, _itemid integer, url text, Active integer, Sync integer );";

    private static final String PRODUCT_CATEGORIES_CREATE = "create table "
            + TABLE_PRODUCT_CATEGORIES
            + " (_id integer primary key autoincrement , "
            + "CategoryID integer, ParentID integer, UserID integer, AccountID integer, Name text, code text, Active integer, DOE integer,"
            + " UNIQUE(CategoryID) );";

    private static final String PRODUCT_CATEGORY_ASSOCIATIONS_CREATE = "create table "
            + TABLE_PRODUCT_CATEGORY_ASSOCIATIONS + " (AccountID integer, "
            + " ProductID integer REFERENCES Products(ProductID) ON DELETE CASCADE, "
            + " CategoryID integer REFERENCES ProductCategories(CategoryID) ON DELETE CASCADE );";

    private static final String PRODUCT_BRANDS_CREATE = "create table "
            + TABLE_PRODUCT_BRANDS
            + " (_id integer primary key autoincrement , "
            + " BrandID integer, ParentID integer, UserID integer, AccountID integer, Name text, code text, Active integer, DOE integer,"
            + " UNIQUE(BrandID) );";

    private static final String PRODUCT_BRAND_ASSOCIATIONS_CREATE = "create table "
            + TABLE_PRODUCT_BRAND_ASSOCIATIONS + " (AccountID integer, "
            + " ProductID integer REFERENCES Products(ProductID) ON DELETE CASCADE, "
            + " BrandID integer REFERENCES ProductBrands(BrandID) ON DELETE CASCADE );";

    private static final String PRODUCT_TYPES_CREATE = "create table "
            + TABLE_PRODUCT_TYPES
            + " (_id integer primary key autoincrement , "
            + " ProductTypeID integer, AccountID integer, Name text, Description text, Active integer, DOE integer, "
            + " UNIQUE(ProductTypeID) );";

    private static final String PRODUCT_ASSORTMENTS_CREATE = "create table "
            + TABLE_PRODUCT_ASSORTMENTS
            + " (_id integer primary key autoincrement , "
            + " AssortmentID integer, AccountID integer, Name text, Description text, Parameters text, Active integer, DOE integer,"
            + " UNIQUE(AssortmentID) );";

    private static final String PRODUCT_DOCUMENTS_CREATE = "create table "
            + TABLE_PRODUCT_DOCUMENTS + " (_id integer primary key autoincrement , "
            + "ProductDocumentID integer, AccountID integer, ProductID integer REFERENCES Products(ProductID) ON DELETE CASCADE, DocumentType integer, fileName text, fileSize integer, fileContentType text, data blob, Name text, Description text, Active integer);";

    private static final String ORDERSTATUS_CREATE = "create table "
            + TABLE_ORDERSTATUS
            + " (_id integer primary key autoincrement, OrderStatusID integer, Name text, Color text );";

    private static final String PRICELIST_CREATE = "create table "
            + TABLE_PRICELIST
            + " ( ProductID integer primarykey REFERENCES Products(ProductID) ON DELETE CASCADE, AccountID integer, PriceDate integer, Price_RT real, Price_WS real, RT_Base real, WS_Base real, RT_TaxID integer, RT_TaxValue real, WS_TaxID integer, WS_TaxValue real,  "
            + " DiscountPercentage real, PaymentDelay integer, DeliveryDelay integer, GroupDiscountPercentage real,"
            + " GroupPaymentDelay integer, GroupDeliveryDelay integer, ClientID integer, fromDate integer, toDate integer, DOE integer );";

    private static final String UOM_CREATE = "create table "
            + TABLE_UOM
            + " ( _id integer primary key autoincrement, UOMID integer primarykey , Name text );";

    private static final String PAYMENT_METHODS_CREATE = "create table "
            + TABLE_PAYMENT_METHODS
            + " ( _id integer primary key autoincrement, AccountID integer, PaymentMethodID integer primarykey , Name text, Description text );";

    private static final String CLIENT_PAYMENT_METHODS_CREATE = "create table "
            + TABLE_CLIENT_PAYMENT_METHODS
            + " ( ClientID integer, PaymentMethodID integer );";

    private static final String PAYMENT_DATES_CREATE = "create table "
            + TABLE_PAYMENT_DATES
            + " ( _id integer primary key autoincrement, AccountID integer, PaymentDateID integer primarykey , Name text, Description text, Delay integer );";

    private static final String CLIENT_PAYMENT_DATES_CREATE = "create table "
            + TABLE_CLIENT_PAYMENT_DATES
            + " ( ClientID integer, PaymentDateID integer, ProductCategoryID intereger, ProductID integer );";

    private static final String TAX_CREATE = "create table "
            + TABLE_TAX
            + " ( _id integer primary key autoincrement, TaxID integer primarykey , Name text, Description text, Percentage real );";

    private static final String DISCOUNT_GROUPS_CREATE = "create table "
            + TABLE_DISCOUNT_GROUPS
            + " ( _id integer primary key autoincrement, DiscountGroupID integer primarykey , "
            + " AccountID integer, code text, Name text, Description text, Percentage real, PaymentDelay integer, DeliveryDelay integer );";

    private static final String DISCOUNT_GROUP_ClIENTS_CREATE = "create table "
            + TABLE_DISCOUNT_GROUP_CLIENTS
            + " ( DiscountGroupID integer, ClientID integer REFERENCES Clients(ClientID) ON DELETE CASCADE); ";

    private static final String DISCOUNT_GROUP_PRODUCTS_CREATE = "create table "
            + TABLE_DISCOUNT_GROUP_PRODUCTS
            + " ( DiscountGroupID integer, ProductGroupID integer, ProductID integer, Percentage real, PaymentDelay integer, DeliveryDelay integer); ";

    private static final String DISCOUNT_GROUP_ACTIONS_CREATE = "create table "
            + TABLE_DISCOUNT_GROUP_ACTION
            + " ( _id integer primary key autoincrement, DiscountGroupActionID integer primarykey , "
            + " AccountID integer, code text, Name text, Description text, Percentage real, PaymentDelay integer, DeliveryDelay integer, startDate integer, endDate integer );";

    private static final String DISCOUNT_CLIENT_PRODUCTS_CREATE = "create table "
            + TABLE_DISCOUNT_CLIENT_PRODUCTS
            + " ( AccountID integer, ClientID integer, ProductID integer, Discount1 real, Discount2 real, Discount3 real, Discount4 real, Discount5 real); ";

    private static final String DISCOUNT_GROUP_ACTION_ClIENTS_CREATE = "create table "
            + TABLE_DISCOUNT_GROUP_ACTION_CLIENTS
            + " ( DiscountGroupActionID integer, ClientID integer REFERENCES Clients(ClientID) ON DELETE CASCADE); ";

    private static final String DISCOUNT_GROUP_ACTION_PRODUCTS_CREATE = "create table "
            + TABLE_DISCOUNT_GROUP_ACTION_PRODUCTS
            + " ( DiscountGroupActionID integer, ProductGroupID integer, ProductID integer, Percentage real, PaymentDelay integer, DeliveryDelay integer); ";

    private static final String CLIENT_DELIVERY_PLACES_PROPERTIES_CREATE = "create table "
            + TABLE_CLIENT_DELIVERY_PLACES_PROPERTIES
            + " ( _id integer primary key autoincrement, AccountID integer, ObjectID integer REFERENCES DeliveryPlaces(ObjectID) ON DELETE CASCADE , "
            + " DPPID integer  REFERENCES DeliveryPlacesProperties(DPPID) ON DELETE CASCADE, DPPOID integer REFERENCES DeliveryPlacesPropertiesOptions(DPPOID) ON DELETE CASCADE, DOE integer, Sync integer );";

    private static final String DELIVERY_PLACES_PROPERTIES_CREATE = "create table "
            + TABLE_DELIVERY_PLACES_PROPERTIES
            + " ( _id integer primary key autoincrement, DPPID integer primarykey , "
            + " AccountID integer, code text, Name text, Description text, Active integer, DOE integer );";

    private static final String DELIVERY_PLACES_PROPERTIES_OPTIONS_CREATE = "create table "
            + TABLE_DELIVERY_PLACES_PROPERTIES_OPTIONS
            + " ( _id integer primary key autoincrement, DPPOID integer primarykey, DPPID integer REFERENCES DeliveryPlacesProperties(DPPID) ON DELETE CASCADE , "
            + " AccountID integer, code text, Name text, Description text, Active integer, DOE integer );";

    private static final String DELIVERY_PLACES_PRODUCT_PLACEMENT_CREATE = "create table "
            + TABLE_DELIVERY_PLACES_PRODUCT_PLACEMENT
            + " ( _id integer primary key autoincrement, "
            + " AccountID integer, ProductCategoryID integer, ProductID integer REFERENCES Products(ProductID) ON DELETE CASCADE, DeliveryPlaceID integer REFERENCES DeliveryPlaces(DeliveryPlaceID) ON DELETE CASCADE, UserID integer, "
            + " Col1 real, Col2 real, Col3 real, Col4 text, Col5 text, Col6 text, Note text, Active integer, Sync integer, DOE integer );";

    private static final String GPS_CREATE = "create table "
            + TABLE_GPS
            + " ( _id integer primary key autoincrement, "
            + " AccountID integer, UserID integer, Latitude integer, Longitude integer, Speed real, Time integer, Accuracy real, Altitude integer, Bearing integer, DOE integer, Sync integer );";

    private static final String ROUTES_CREATE = "create table "
            + TABLE_ROUTES + " (_id integer primary key autoincrement , "
            + " RouteID integer, AccountID integer, UserID integer, Name text , code text, Description text , "
            + " raw text, Active integer, DOE integer, Sync integer );";

    private static final String TABLE_NOTIFICATION_MESSAGES_CREATE = "create table "
            + TABLE_NOTIFICATION_MESSAGES + " (_id integer primary key autoincrement, AccountID integer, MessageID integer, Message text, Type integer);";

    private static final String TABLE_TEMP_CREATE = "create table "
            + TABLE_TEMP + " (_id integer primary key autoincrement, AccountID integer, UserID integer, data text);";

    private static final String TABLE_GLOBAL_VARIABLES_CREATE = "create table "
            + TABLE_GLOBAL_VARIABLES + " (_id integer primary key autoincrement, AccountID integer, UserID integer, ClientID integer);";


    private static final String MERCHANDISE_CREATE = "create table "
            + TABLE_MERCHANDISE
            + " ( _id integer primary key autoincrement, AccountID integer, MerchandiseID integer primarykey , Name text, Description text, Active integer );";

    private static final String DELIVERY_PLACES_MERCHANDISE_CREATE = "create table "
            + TABLE_DELIVERY_PLACES_MERCHANDISE
            + " ( _id integer primary key autoincrement, AccountID integer, MerchandiseID integer, DeliveryPlaceID integer, Notes text, Count real, Type, DOE integer, Sync integer );";

    private static final String COMPETITIONS_CREATE = "create table "
            + TABLE_COMPETITIONS + " (_id integer primary key autoincrement , "
            + " CompetitionID integer, AccountID integer,  Name text , code text, Description text , Telephone text, Fax text , "
            + " Mobile text , EmailAddress text , Website text , Address text , City text , CountryID integer , "
            + " Latitude integer, Longitude integer , IDNumber text, PDVNumber text, WATNumber text, Owner text, WATType integer, "
            + " Active integer, DOE integer, Sync integer );";

    private static final String COMPETITION_ITEMS_CREATE = "create table "
            + TABLE_COMPETITION_ITEMS + " (_id integer primary key autoincrement , "
            + " ItemID integer, CompetitionID integer REFERENCES Competitions(CompetitionID) ON DELETE CASCADE, AccountID integer, Name text, code text, Description text , OptionID integer, "
            + " Active integer, DOE integer, Sync integer );";

    private static final String COMPETITION_ITEMS_VALUES_CREATE = "create table "
            + TABLE_COMPETITION_ITEMS_VALUES + " (_id integer primary key autoincrement , "
            + " ItemID integer, DeliveryPlaceID integer REFERENCES DeliveryPlaces(DeliveryPlaceID) ON DELETE CASCADE, AccountID integer, "
            + " UserID integer, Value real, Col1 real, Col2 real, Col3 real, Col4 text, Col5 text, Col6 text, Note text, "
            + " DOE integer, Sync integer );";

    private static final String TASKS_CREATE = "create table "
            + TABLE_TASKS
            + " (_id integer primary key autoincrement , "
            + " TaskID integer, AccountID integer, StatusID integer, Name text, Description text, Parameters text, Active integer, DOE integer, Sync integer, "
            + " UNIQUE(TaskID) );";

    private static final String TASK_LOG_CREATE = "create table "
            + TABLE_TASK_LOG
            + " (_id integer primary key autoincrement , "
            + " TaskID integer, AccountID integer, UserID integer, StatusID integer, Parameters text, DOE integer, Sync integer );";

    private static final String TEMP_ACQUISITION_CREATE = "create table TEMP_ACQUISITION "
            + " (_id integer primary key autoincrement , "
            + " ID integer, AccountID integer, OptionID integer, jsonObj text, UserID integer, "
            + " DOE integer, Sync integer );";

    private static final String ACTIVITY_CREATE = "create table Activity "
            + " (_id integer primary key autoincrement, ActivityID integer, GroupID integer, ParentID integer, AccountID integer, Name text, "
            + " Description text, Priority integer, Public integer, Billable integer, Prolific integer, MandatoryOptions text, "
            + " Duration integer, AdditionalTextRequired integer, Snooze integer, Reminder integer, Reference integer, "
            + " RecieverGroup text, RecieverID integer, Assigment integer, Unassigment integer, ResourceID integer, SingleMode integer, Active integer, DOE integer, Sync integer );";

    private static final String USER_ACTIVITY_LOG_CREATE = "create table User_Activity_Log "
            + " (_id integer primary key autoincrement, ID integer, AccountID integer, UserID integer, OptionID integer, ItemID integer, startTime integer, "
            + " endTime integer, ProjectID integer, ClientID integer, DeliveryPlaceID integer, startLatitude integer, startLongitude integer, "
            + " endLatitude integer, endLongitude integer, Billable integer, Version integer, Reference text, "
            + " Locked integer, Description text, Duration integer, IP text, Licence text, GroupID integer, "
            + " MediaID integer, TravelOrderID integer, ResourceID integer, ResourceItemID integer, Deviation integer, UserName text, GroupName text, ItemName text, Prolific integer, "
            + " Active integer, DOE integer, Sync integer );";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			if (oldVersion <= 2) {

			}
		}
		
		catch (Exception e) {
			wurthMB.AddError("dbHelper onUpgrade", "", e);
		}
	}
	
    private static DBHelper instance;

    public static synchronized DBHelper getHelper(Context context)
    {
        if (instance == null) instance = new DBHelper(context);
        return instance;
    }

    public SQLiteDatabase getDB()  {

        if (db == null) {
            db = this.getWritableDatabase();
            db.enableWriteAheadLogging();
        }

        if (!Common.isTableExists(db, TABLE_LOGINS)) {

            try {

                if (mThreadReference != null) mThreadReference.doProgress("Create Tables");

                /* Create LOGINS Table */
                db.execSQL(LOGINS_CREATE);

                /* Create VISITS Table */
                //db.execSQL(VISITS_CREATE);

                /* Create DOCUMENTS Table */
                db.execSQL(DOCUMENTS_CREATE);

                /* Create PRODUCT DOCUMENTS Table */
                db.execSQL(PRODUCT_DOCUMENTS_CREATE);

                /* Create PRODUCT CATEGORIES Table */
                db.execSQL(PRODUCT_CATEGORIES_CREATE);

                /* Create PRODUCT CATEGORY ASSOCIATIONS Table */
                db.execSQL(PRODUCT_CATEGORY_ASSOCIATIONS_CREATE);

                /* Create PRODUCT BRANDS Table */
                db.execSQL(PRODUCT_BRANDS_CREATE);

                /* Create PRODUCT BRAND ASSOCIATIONS Table */
                db.execSQL(PRODUCT_BRAND_ASSOCIATIONS_CREATE);

                /* Create PRODUCT TYPES Table */
                db.execSQL(PRODUCT_TYPES_CREATE);

                /* Create PRODUCT ASSORTMENTS Table */
                db.execSQL(PRODUCT_ASSORTMENTS_CREATE);

                /* Create PRICELIST Table */
                db.execSQL(PRICELIST_CREATE);

                /* Create ORDERSTATUS Table */
                db.execSQL(ORDERSTATUS_CREATE);

                /* Create UOM Table */
                db.execSQL(UOM_CREATE);

                /* Create PAYMENT METHODS Table */
                db.execSQL(PAYMENT_METHODS_CREATE);

                /* Create CLIENT PAYMENT METHODS Table */
                db.execSQL(CLIENT_PAYMENT_METHODS_CREATE);

                /* Create PAYMENT DATES Table */
                db.execSQL(PAYMENT_DATES_CREATE);

                /* Create CLIENT PAYMENT DATES Table */
                db.execSQL(CLIENT_PAYMENT_DATES_CREATE);

                /* Create TAX Table */
                db.execSQL(TAX_CREATE);

                /* Create DISCOUNT GROUPS Table */
                db.execSQL(DISCOUNT_GROUPS_CREATE);

                /* Create DISCOUNT GROUP CLIENTS Table */
                db.execSQL(DISCOUNT_GROUP_ClIENTS_CREATE);

                /* Create DISCOUNT GROUP PRODUCTS Table */
                db.execSQL(DISCOUNT_GROUP_PRODUCTS_CREATE);

                /* Create DISCOUNT GROUP ACTIONS Table */
                db.execSQL(DISCOUNT_GROUP_ACTIONS_CREATE);

                /* Create DISCOUNT GROUP ACTION CLIENTS Table */
                db.execSQL(DISCOUNT_GROUP_ACTION_ClIENTS_CREATE);

                /* Create DISCOUNT GROUP ACTION PRODUCTS Table */
                db.execSQL(DISCOUNT_GROUP_ACTION_PRODUCTS_CREATE);

                /* Create DISCOUNT CLIENT PRODUCTS Table */
                db.execSQL(DISCOUNT_CLIENT_PRODUCTS_CREATE);

                /* Create CLIENT DELIVERY PLACES PROPETIES Table */
                db.execSQL(CLIENT_DELIVERY_PLACES_PROPERTIES_CREATE);

                /* Create DELIVERY PLACES PROPERTIES Table */
                db.execSQL(DELIVERY_PLACES_PROPERTIES_CREATE);

                /* Create DELIVERY PLACES PROPERTIES OPTIONS Table */
                db.execSQL(DELIVERY_PLACES_PROPERTIES_OPTIONS_CREATE);

                /* Create GPS Table */
                db.execSQL(GPS_CREATE);

                /* Create ROUTES Table */
                db.execSQL(ROUTES_CREATE);

                /* Create NOTIFICATION_MESSAGE Table */
                db.execSQL(TABLE_NOTIFICATION_MESSAGES_CREATE);

                /* Create TEMP Table */
                db.execSQL(TABLE_TEMP_CREATE);

                /* Create GLOBAL VARIABLES Table */
                db.execSQL(TABLE_GLOBAL_VARIABLES_CREATE);

                /* Create MERCHANDISE Table */
                db.execSQL(MERCHANDISE_CREATE);

                /* Create DELIVERY PLACE MERCHANDISE Table */
                db.execSQL(DELIVERY_PLACES_MERCHANDISE_CREATE);

                /* Create COMPETITIONS Table */
                db.execSQL(COMPETITIONS_CREATE);

                /* Create COMPETITION ITEMS Table */
                db.execSQL(COMPETITION_ITEMS_CREATE);

                /* Create COMPETITION ITEMS VALUES Table */
                db.execSQL(COMPETITION_ITEMS_VALUES_CREATE);

                /* Create DELIVERY PLACE PRODUCT PLACEMENT Table */
                db.execSQL(DELIVERY_PLACES_PRODUCT_PLACEMENT_CREATE);

                /* Create TASKS Table */
                db.execSQL(TASKS_CREATE);

                /* Create TASK_LOG Table */
                db.execSQL(TASK_LOG_CREATE);

                /* Create TEMP ACQUISITION Table */
                db.execSQL(TEMP_ACQUISITION_CREATE);

                /* Create TEMP ACTIVITY Table */
                db.execSQL(ACTIVITY_CREATE);

                /* Create TEMP ACTIVITY LOG Table */
                db.execSQL(USER_ACTIVITY_LOG_CREATE);

                if (mThreadReference != null) mThreadReference.doProgress("Create Index");

                /* CREATE INDEXEX */
                db.execSQL("CREATE INDEX clients_idx ON Clients (ClientID);");
                db.execSQL("CREATE INDEX clients_client_id_idx ON Clients (_clientid);");

                db.execSQL("CREATE INDEX products_idx ON Products (ProductID);");
                db.execSQL("CREATE INDEX deliveryplaces_idx ON DeliveryPlaces (DeliveryPlaceID);");
                db.execSQL("CREATE INDEX visits_idx ON Visits (VisitID);");
                db.execSQL("CREATE INDEX orders_idx ON Orders (OrderID);");
                db.execSQL("CREATE INDEX orders_items_idx ON OrderItems (OrderID);");
                db.execSQL("CREATE INDEX orders_items_order_id_idx ON OrderItems (Order_ID);");
                db.execSQL("CREATE INDEX productcategories_idx ON ProductCategories (CategoryID);");
                db.execSQL("CREATE INDEX productcategoryassociations_idx ON ProductCategoryAssociations (CategoryID, ProductID);");

                db.execSQL("CREATE INDEX clients_name_idx ON Clients (Name COLLATE NOCASE, Code COLLATE NOCASE);");
                db.execSQL("CREATE INDEX products_name_idx ON Products (Name COLLATE NOCASE, Barcode COLLATE NOCASE, Code COLLATE NOCASE);");

                db.execSQL("CREATE INDEX artikli_cover_idx ON ARTIKLI (Naziv, Grupa_Artikla, Status_Prezentacije_Artikla);");

                db.execSQL("CREATE INDEX partner_idx ON PARTNER (ID);");
                db.execSQL("CREATE INDEX partner_detalji_idx ON PARTNER_DETALJI (CustomerID);");
                db.execSQL("CREATE INDEX partner_kontakti_idx ON PARTNER_BRANSE (PartnerID);");
                db.execSQL("CREATE INDEX partner_branse_idx ON PARTNER_KONTAKTI (PartnerID);");

                db.execSQL("CREATE INDEX komercijalisti_idx ON KOMERCIJALISTI (ID);");
                db.execSQL("CREATE INDEX partner_komercijalista_id_idx ON PARTNER (KomercijalistaID);");

                db.execSQL("CREATE INDEX idx_visit_startDT ON Visits (startDT);");
                db.execSQL("CREATE INDEX idx_orders_orderdate ON Orders (OrderDate);");


                String TABLE_PRODUCTS_FTS = "PRODUCTS_FTS";
                String TABLE_CLIENTS_FTS = "CLIENTS_FTS";
                String TABLE_DELIVERYPLACES_FTS = "DELIVERYPLACES_FTS";

                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS_FTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTS_FTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELIVERYPLACES_FTS);

                db.execSQL("CREATE VIRTUAL TABLE " + TABLE_PRODUCTS_FTS + " USING fts4(ArtikalID, ProductID, Name, Code, Description, Keyword, Keyword1, Barcode);");
                db.execSQL("CREATE VIRTUAL TABLE " + TABLE_CLIENTS_FTS + " USING fts4(PartnerID, ClientID, Name, Code, Description, Keyword, Barcode);");
                db.execSQL("CREATE VIRTUAL TABLE " + TABLE_DELIVERYPLACES_FTS + " USING fts4(PartnerID, DeliveryPlaceID, Name, Code, Description, Keyword, Barcode);");

                if (mThreadReference != null) mThreadReference.doProgress("Creating Products FTS");

                db.execSQL("INSERT INTO PRODUCTS_FTS (ProductID, ArtikalID, Name, Code, Description, Keyword, Keyword1, Barcode) " +
                        " SELECT Products.ProductID, Artikli.ID, Artikli.Naziv, Artikli.sifra, " +
                        " CASE WHEN Artikli.Zbirni_Naziv IS NULL THEN ARTIKAL_GRUPE.Naziv ELSE Artikli.Zbirni_Naziv END, " +
                        " REPLACE(REPLACE(Artikli.sifra, ' ' ,''), '-',''), " +
                        " REPLACE(REPLACE(SUBSTR(Artikli.sifra, 2, LENGTH(Artikli.sifra)-1), ' ' ,''), '-',''), " +
                        " Artikli.BarCode " +
                        " FROM ARTIKLI " +
                        " LEFT JOIN ARTIKAL_GRUPE ON ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                        " LEFT JOIN Products ON ARTIKLI.ID = Products._productid " +
                        " WHERE Artikli.Naziv IS NOT NULL");

                if (mThreadReference != null) mThreadReference.doProgress("Creating Clients FTS");

                db.execSQL("INSERT INTO CLIENTS_FTS (ClientID, PartnerID, Name, Code, Keyword) " +
                        " SELECT Clients.ClientID, PARTNER.ID, PARTNER.Naziv, PARTNER.Kod, REPLACE(PARTNER.Kod, ' ', '') FROM PARTNER " +
                        " LEFT JOIN Clients ON PARTNER.ID = Clients._clientid ;");

                db.execSQL("INSERT INTO DELIVERYPLACES_FTS (DeliveryPlaceID, PartnerID, Name, Code, Keyword) " +
                        " SELECT DeliveryPlaces.DeliveryPlaceID, PARTNER.ID, PARTNER.Naziv, PARTNER.Kod, REPLACE(PARTNER.Kod, ' ', '') FROM PARTNER " +
                        " LEFT JOIN DeliveryPlaces ON PARTNER.ID = DeliveryPlaces._deliveryplaceid ;");

                if (mThreadReference != null) mThreadReference.doProgress("Updating Clients");


                new NetworkTaskGetClients().execute();

                try {

                    long _DOE = 0;
                    Cursor _cursor = db_readonly.rawQuery("SELECT MAX(DOE) FROM Orders", null);
                    if (_cursor.moveToFirst()) _DOE = _cursor.getLong(0);
                    _cursor.close();

                    SharedPreferences prefs = wurthMB.getInstance().getSharedPreferences("optimusMBprefs", wurthMB.getInstance().MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("initial_start_date", _DOE);
                    editor.commit();

                } catch (Exception e) {

                }

            }
            catch (Exception ex) {

            }
        }

        return db;
    }

    public SQLiteDatabase get_db_readonly() {

        if (db_readonly == null) {
            db_readonly = this.getReadableDatabase();
        }

        return db_readonly;
    }

    private class NetworkTaskGetClients extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("UserID", Long.toString(wurthMB.getUser().UserID)));
                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "GET_WURTH_Clients", postParameters).toString();

                if (response == null || response.equals("")) return null;

                JSONArray json_data = new JSONArray(response);

                String sql = "UPDATE Clients SET UserID = ? WHERE ClientID = ? AND UserID = 0 ";

                db.beginTransaction();
                SQLiteStatement stmt = db.compileStatement(sql);

                for (int i = 0; i < json_data.length(); i++) {
                    JSONObject jsonObject = json_data.getJSONObject(i);
                    stmt.bindString(1, Long.toString(wurthMB.getUser().UserID));
                    stmt.bindString(2, jsonObject.getString("ClientID"));
                    stmt.execute();
                    stmt.clearBindings();
                }
                db.setTransactionSuccessful();
                db.endTransaction();

            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {}
    }
}
