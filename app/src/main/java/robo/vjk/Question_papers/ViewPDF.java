package robo.vjk.Question_papers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.List;


public class ViewPDF extends AppCompatActivity  implements OnPageChangeListener,OnLoadCompleteListener,OnErrorListener,OnPageErrorListener {


    String title = extras.title;
    public static final String SAMPLE_FILE = "CR_pdf.pdf";
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;




    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_pdf);

        pdfView= (PDFView)findViewById(R.id.pdfView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        displayFromAsset(SAMPLE_FILE);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/testthreepdf/"+extras.name+".pdf");
        boolean deleted = file.delete();
    }

    public static void restartActivity(Activity act){

        Intent intent=new Intent();
        intent.setClass(act, act.getClass());
        act.startActivity(intent);
        act.finish();

    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = title;
        File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/testthreepdf/"+extras.name+".pdf");

        try {
            pdfView.fromFile(file1)
                    .defaultPage(pageNumber)
                    .enableSwipe(true)

                    .swipeHorizontal(false)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .load();
        } catch (Exception e){

            Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
            recreate();
        }

    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        try {
            PdfDocument.Meta meta = pdfView.getDocumentMeta();
            printBookmarksTree(pdfView.getTableOfContents(), "-");
        }catch (Exception e){

            Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
            recreate();
        }

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {


            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onError(Throwable t) {


        Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
        //recreate();

    }

    @Override
    public void onPageError(int page, Throwable t) {
        Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
        //recreate();
    }
}
