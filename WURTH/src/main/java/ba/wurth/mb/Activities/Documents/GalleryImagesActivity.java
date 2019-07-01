package ba.wurth.mb.Activities.Documents;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

import ba.wurth.mb.Adapters.ImageAdapter;
import ba.wurth.mb.Classes.ImageLoader;
import ba.wurth.mb.Classes.Objects.Document;
import ba.wurth.mb.Classes.Objects.Visit;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Visits.DL_Visits;
import ba.wurth.mb.R;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class GalleryImagesActivity extends Activity implements AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory, View.OnTouchListener {

    private Gallery gallery = null;
    private ImageSwitcher mSwitcher;
    private ImageAdapter adapter = null;
    private ArrayList<Document> imageItems = new ArrayList<Document>();
    public ImageLoader imageLoader;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery_images);

        try {

            imageLoader = new ImageLoader(this.getApplicationContext());

            int pos = getIntent().getExtras().getInt("selectedIntex");

            gallery = (Gallery) findViewById(R.id.gallery);

            mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
            mSwitcher.setFactory(this);
            mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

            if (getIntent().hasExtra("OptionID")) {
                switch (getIntent().getIntExtra("OptionID", 0)) {
                    case 4: //PRODUCTS

                        if (getIntent().hasExtra("_id")) {
                            Cursor cur = DL_Wurth.GET_ProductImages(getIntent().getLongExtra("_id", 0L));

                            if (cur != null) {
                                while (cur.moveToNext()) {
                                    Document d = new Document();
                                    d.url = cur.getString(0);
                                    imageItems.add(d);
                                }
                                cur.close();
                            }

                            adapter = new ImageAdapter(GalleryImagesActivity.this, "gallery", imageItems);
                            gallery.setAdapter(adapter);
                            gallery.setSelection(pos);
                            gallery.setOnItemSelectedListener(this);

                        }
                        break;

                    case 9: //VISITS

                        if (getIntent().hasExtra("_id")) {
                            Long VisitID = getIntent().getLongExtra("_id", 0L);
                            Visit mVisit = DL_Visits.GetByID(VisitID);

                            if (mVisit != null) {
                                for (int i = 0; i < mVisit.documents.size(); i++) {
                                    imageItems.add(mVisit.documents.get(i));
                                }

                                adapter = new ImageAdapter(GalleryImagesActivity.this, "gallery", imageItems);
                                gallery.setAdapter(adapter);
                                gallery.setSelection(pos);
                                gallery.setOnItemSelectedListener(this);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            wurthMB.AddError("Client", ex.getMessage(), ex);
        }
    }

    public View makeView() {
        ImageViewTouch i = new ImageViewTouch(this, null);
        i.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return i;
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        try {
            if (imageItems.get(position).data != null && imageItems.get(position).data.length > 0) {
                Bitmap b = BitmapFactory.decodeByteArray(imageItems.get(position).data, 0, imageItems.get(position).data.length);
                mSwitcher.setImageDrawable(new BitmapDrawable(getResources(), b));
            } else if (imageItems.get(position).url != null && !imageItems.get(position).url.equals("")) {
                mSwitcher.setImageDrawable(new BitmapDrawable(getResources(), wurthMB.imageLoader.getBitmap(imageItems.get(position).url)));
            }
        } catch (Exception ex) {

        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                mode = NONE;
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                oldDist = spacing(event);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                } else if (mode == ZOOM) {
                    // pinch zooming
                    float newDist = spacing(event);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float )Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}