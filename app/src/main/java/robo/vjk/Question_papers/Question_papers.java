package robo.vjk.Question_papers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Question_papers extends AppCompatActivity implements RecyclerViewAdapter.PDFAdapterListener {
    private static final String TAG = Question_papers.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<PDF> pdfList;
    private RecyclerViewAdapter mAdapter;
    private SearchView searchView;
    String url;
    boolean doubleBackToExitPressedOnce = false;

    ProgressBar progressBar;
    AlertDialog.Builder builder;


    // url to fetch PDFs json
    private static final String URL = "http://192.168.43.158/sqlapi.php";  //https://api.androidhive.info/json/contacts.json

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/testthreepdf/"+extras.name+".pdf");
            boolean deleted = file.delete();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
        fetchPDFs();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_papers);
        Toolbar toolbar = findViewById(R.id.toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setSupportActionBar(toolbar);



        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        recyclerView = findViewById(R.id.recycler_view);
        pdfList = new ArrayList<>();
        mAdapter = new RecyclerViewAdapter(this, pdfList, this);

        // white background notification bar
        whiteNotificationBar(recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        progressBar=(ProgressBar)findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);

        fetchPDFs();
    }



    /**
     * fetches json by making http calls
     */

    private void fetchPDFs() {



        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {

                            Toast.makeText(getApplicationContext(), "Couldn't fetch the Files!\nConnect with Campus wifi and try again!!! ", Toast.LENGTH_LONG).show();
                            System.exit(0);
                            return;
                        }

                        List<PDF> items = new Gson().fromJson(response.toString(), new TypeToken<List<PDF>>() {
                        }.getType());

                        progressBar.setVisibility(View.GONE);
                        // adding contacts to contacts list
                        pdfList.clear();
                        pdfList.addAll(items);

                        // refreshing recycler view
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error: " + error.getMessage());
                //Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "Couldn't fetch the Files!\nConnect with Campus wifi and try again!!! ", Toast.LENGTH_LONG).show();

                builder.setTitle("ERROR")
                        .setMessage("Couldn't fetch the Files!\\nConnect with Campus wifi and try again!!! ")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete

                                dialog.dismiss();
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                                //finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                System.exit(0);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(30);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }



    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onPDFSelected(PDF pdf) {
        url="http://"+pdf.getUrl();
        //Toast.makeText(getApplicationContext(), "Selected: " + pdf.getTitle() + ", "+url + pdf.getYear(), Toast.LENGTH_LONG).show();  //+ pdf.getPhone()

        extras.title=pdf.getTitle();
        extras.name=random();

        //String title=pdf.getTitle();
        new DownloadFile().execute(url, extras.name);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Magic here
                Intent j=new Intent(Question_papers.this,ViewPDF.class);
                startActivity(j);
            }
        }, 500); // Millisecond 1000 = 1 sec



    }



    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            //progressBar.setVisibility(View.VISIBLE);
            String eee=url;
            String fileUrl =eee ;   // -> http://maven.apache.org/maven-1.x/maven.pdf  //"http://192.168.43.158/uploads/CR_pdf.pdf"
            String fileName = extras.name+".pdf";  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().getAbsoluteFile().toString();
            File folder = new File(extStorageDirectory, "testthreepdf");
            folder.mkdir();
            File pdfFile = new File(folder, fileName);

            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);
            //progressBar.setVisibility(View.INVISIBLE);
            return null;
        }
    }
}

