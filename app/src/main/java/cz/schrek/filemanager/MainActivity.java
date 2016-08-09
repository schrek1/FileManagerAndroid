package cz.schrek.filemanager;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private File rootFile;
    private File[] fileContent;
    boolean doubleBackToExitPressedOnce = false;


    private TextView path;
    private TextView isEmpty;
    private ListView fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        getFileContent();

    }

    private void getFileContent() {
        int numDirs = 0, numFiles = 0;

        path.setText(rootFile.getPath());

        File[] dirs = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                else
                    return false;
            }
        });
        File[] files = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isFile())
                    return true;
                else
                    return false;
            }
        });
        if (dirs != null) {
            Arrays.sort(dirs, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            numDirs = dirs.length;
        }
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            numFiles = files.length;
        }
        fileContent = new File[numDirs + numFiles];

        if (numDirs + numFiles == 0) {
            isEmpty.setVisibility(View.VISIBLE);
        } else {
            isEmpty.setVisibility(View.GONE);
        }

        if (dirs != null) {
            System.arraycopy(dirs, 0, fileContent, 0, dirs.length);
        }
        if (files != null) {
            System.arraycopy(files, 0, fileContent, dirs.length, files.length);
        }

        Log.wtf("array print", Arrays.toString(fileContent));

        fileList.setAdapter(new FileListAdapter(MainActivity.this, fileContent));
    }


    private void init() {
        path = (TextView) findViewById(R.id.path);
        isEmpty = (TextView) findViewById(R.id.isEmpty);
        fileList = (ListView) findViewById(R.id.fileList);
        rootFile = Environment.getRootDirectory().getParentFile();

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                File selected = fileContent[position];
                if (selected.isDirectory()) {
                    rootFile = selected;
                    getFileContent();
                } else if (selected.isFile()) {
                    //TODO dodelat otevreni defaultni aplikaci
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        File parent = rootFile.getParentFile();
        if (parent != null) {
            rootFile = parent;
            getFileContent();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Pro ukonceni aplikace stisknete zpet.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

}
