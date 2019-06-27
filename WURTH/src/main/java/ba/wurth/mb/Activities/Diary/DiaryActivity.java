package ba.wurth.mb.Activities.Diary;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Reports.DiaryFragment;
import ba.wurth.mb.R;

public class DiaryActivity extends AppCompatActivity  {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary);

        try {

            getSupportActionBar().setTitle(R.string.Diary);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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

            if (savedInstanceState == null ) {
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new DiaryFragment(), "Diary").commit();
            }

        }
        catch (Exception ex) {
            wurthMB.AddError("Diary", ex.getMessage(), ex);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}