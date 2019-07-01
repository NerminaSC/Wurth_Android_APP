package ba.wurth.mb.Activities.Visits;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Document;
import ba.wurth.mb.Classes.Objects.Visit;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Visits.DL_Visits;
import ba.wurth.mb.Fragments.Visits.VisitFragment;
import ba.wurth.mb.Fragments.Visits.VisitImagesFragment;
import ba.wurth.mb.R;

public class VisitActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private Button btnTakePhoto;
    private Button btnUpdate;
    private Button btnCancel;

    private Uri fileUri;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private Long _OrderID = 0L;

    public Visit mVisit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit);

        try {

            getSupportActionBar().setTitle(R.string.Visit);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Photos)).setTabListener(this));

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0) {
                Client c = DL_Wurth.GET_Client(wurthMB.getOrder().ClientID);
                if (c != null) {
                    getSupportActionBar().setTitle(c.Name);
                    getSupportActionBar().setSubtitle(c.code);
                }
            }
            else {
                getSupportActionBar().setTitle("");
                getSupportActionBar().setSubtitle("");
            }

            mViewPager = (ViewPager) findViewById(R.id.pager);

            mAdapter = new FragmentAdapter(getSupportFragmentManager());

            Bundle b = new Bundle();

            if (getIntent().hasExtra("_id")) {
                b.putLong("_id", getIntent().getLongExtra("_id", 0L));
            }

            if (getIntent().hasExtra("ClientID")) {
                b.putLong("ClientID", getIntent().getLongExtra("ClientID", 0L));
            }

            if (getIntent().hasExtra("DeliveryPlaceID")) {
                b.putLong("DeliveryPlaceID", getIntent().getLongExtra("DeliveryPlaceID", 0L));
            }

            if (getIntent().hasExtra("_OrderID")) {
                _OrderID = getIntent().getLongExtra("_OrderID", 0L);
                b.putLong("_OrderID", _OrderID);
            }

            if (getIntent().hasExtra("_id")) {
                Long VisitID =  getIntent().getLongExtra("_id", 0L);
                mVisit = DL_Visits.GetByID(VisitID);
            }
            else {
                mVisit = new Visit();
                mVisit.UserID = wurthMB.getUser().UserID;
            }

            VisitFragment vg = new VisitFragment();
            VisitImagesFragment vi = new VisitImagesFragment();

            vg.setArguments(b);
            vi.setArguments(b);

            mAdapter.addFragment(vg);
            mAdapter.addFragment(vi);

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);

            btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
            btnUpdate = (Button) findViewById(R.id.btnUpdate);
            btnCancel = (Button) findViewById(R.id.btnCancel);

            if (mVisit.Sync == 0) bindListeners();
            if (mVisit.Sync == 1) findViewById(R.id.llActions).setVisibility(View.GONE);

        }
        catch (Exception ex) {
            wurthMB.AddError("Visit", ex.getMessage(), ex);
        }
    }

    private void bindListeners() {
        try {
            btnTakePhoto.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    // create Intent to take a picture and return control to the calling application
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                    // start the image capture Intent
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

                }
            });

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mVisit == null) return;

                    if (mVisit.ClientID == 0) {
                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_ClientNotSelected), 2);
                        return;
                    }

                    /*if (wurthMB.getUser().hasDeliveryPlaces == 1 && mVisit.DeliveryPlaceID == 0) {
                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_DeliveryPlaceNotSelected), 2);
                        return;
                    }*/

                    if (mVisit.endDT < mVisit.startDT) {
                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_VisitEndDateHigher), 2);
                        return;
                    }

                    if (wurthMB.currentBestLocation != null) {
                        mVisit.Latitude = (long) (wurthMB.currentBestLocation.getLatitude() * 10000000);
                        mVisit.Longitude = (long) (wurthMB.currentBestLocation.getLongitude() * 10000000);
                    }

                    mVisit.dt = System.currentTimeMillis();

                    Notifications.showLoading(VisitActivity.this);
                    if (DL_Visits.AddOrUpdate(mVisit) > 0 ) {
                        Notifications.hideLoading(VisitActivity.this);
                        Notifications.showNotification(VisitActivity.this, "", getString(R.string.Notification_VisitSaved), 0);
                        finish();
                    }
                    else {
                        Notifications.hideLoading(VisitActivity.this);
                        Notifications.showNotification(VisitActivity.this, "", getString(R.string.SystemError), 1);
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        catch (Exception ex) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {

                    InputStream is = getContentResolver().openInputStream(fileUri);
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = false;
                    o.inPreferredConfig = Bitmap.Config.RGB_565;
                    o.inDither = true;
                    Bitmap bitmap = BitmapFactory.decodeStream(is, null, o);

                    bitmap = getResizedBitmap(bitmap, bitmap.getHeight() * 720 / bitmap.getWidth() , 720);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    Document d = new Document();
                    d.data = baos.toByteArray();
                    d.dt = System.currentTimeMillis();
                    d.OptionID = 9;
                    d.DocumentType = 1;
                    d.ItemID = mVisit.VisitID;
                    d.fileSize = d.data.length;
                    d.Active = 1;
                    d.Sync = 0;
                    if (mVisit != null) mVisit.documents.add(d);
                    is.close();

                    mViewPager.setCurrentItem(1);

                    if (mAdapter.getItem(mViewPager.getCurrentItem()) instanceof  VisitImagesFragment) {
                        ((VisitImagesFragment) mAdapter.getItem(mViewPager.getCurrentItem())).bindData();
                    }


                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                } else {
                    // Image capture failed, advise user
                }
            }
        }
        catch (Exception ex) {
            wurthMB.AddError("VisitImage_onActivityResult", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class FragmentAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
        public FragmentAdapter(FragmentManager fm) { super(fm); }
        @Override
        public int getCount() { return mFragments.size(); }
        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
            notifyDataSetChanged();
        }
        @Override
        public Fragment getItem(int position) { return mFragments.get(position); }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        try {
            if( mViewPager != null) mViewPager.setCurrentItem(tab.getPosition());
        }
        catch (Exception ex) {

        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }
    @Override
    public void onPageScrollStateChanged(int arg0) { }
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }
    @Override
    public void onPageSelected(int position) { getSupportActionBar().setSelectedNavigationItem(position); }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "wurthMB");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("wurthMB", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        if (newHeight == 0) {
            //newHeight = (height * scaleWidth) / width;
        }
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }
}