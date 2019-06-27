package ba.wurth.mb.Fragments.Visits;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ba.wurth.mb.Activities.Documents.GalleryImagesActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Adapters.VisitImagesAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.ImageItem;
import ba.wurth.mb.Classes.Objects.Visit;
import ba.wurth.mb.R;

public class VisitImagesFragment extends Fragment {

    private GridView gridView;
    private VisitImagesAdapter customGridAdapter;
    private Visit mVisit;
    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("EEE dd.MMM.yyyy HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVisit = ((VisitActivity) getActivity()).mVisit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.visit_images, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) getView().findViewById(R.id.gridView);
        bindData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void bindData() {
        try {
            if (mVisit != null) {

                final ArrayList imageItems = new ArrayList();

                for (int i = 0; i < mVisit.documents.size(); i++) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(mVisit.documents.get(i).data, 0, mVisit.documents.get(i).data.length);
                    imageItems.add(new ImageItem(Common.getResizedBitmap(bitmap, 100, 100), gDateFormatDataItem.format(new Date(mVisit.documents.get(i).dt))));
                }

                if (imageItems.size() > 0) {
                    customGridAdapter = new VisitImagesAdapter(getActivity(), R.layout.grid_image, imageItems);
                    gridView.setAdapter(customGridAdapter);

                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long id) {
                            if (mVisit != null && mVisit._id > 0L) {
                                Intent i = new Intent(getActivity(), GalleryImagesActivity.class);
                                i.putExtra("selectedIntex", pos);
                                i.putExtra("OptionID", 9);
                                i.putExtra("_id", mVisit._id);
                                startActivity(i);
                            }
                        }
                    });
                }
            }
        }
        catch (Exception ex) {

        }
    }
}
