package cz.schrek.filemanager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private File rootFile;
    private File[] fileContent;
    boolean doubleBackToExitPressedOnce = false;
    private LinkedList<ListSettings> listStates = new LinkedList<>();
    private SharedPreferences preferences;

    private TextView path;
    private TextView isEmpty;
    private ListView fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        getFileContent(Operation.NOTHING);

    }

    private void init() {
        path = (TextView) findViewById(R.id.path);
        isEmpty = (TextView) findViewById(R.id.isEmpty);
        fileList = (ListView) findViewById(R.id.fileList);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultPath = preferences.getString("default_path", "/");
        rootFile = new File("/storage/sdcard/Download");
        rootFile = new File(defaultPath);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                File selected = fileContent[position];
                if (selected.isDirectory()) {
                    if (!selected.canRead()) {
                        Toast.makeText(MainActivity.this, "Nelze cist ze slozky", Toast.LENGTH_SHORT).show();
                    } else {
                        rootFile = selected;
                        saveListPostition();
                        getFileContent(Operation.OPEN);
                    }
                } else if (selected.isFile()) {
                    if (selected.canExecute()) {
                        MimeTypeMap myMime = MimeTypeMap.getSingleton();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String mimeType = myMime.getMimeTypeFromExtension(fileExt(selected.getName()));
                        intent.setDataAndType(Uri.fromFile(selected), mimeType);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            MainActivity.this.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "Nenalezena aplikace pro tento typ souboru(" + mimeType + ").", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Nelze spustit soubor", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void saveListPostition() {
        View v = fileList.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - fileList.getPaddingTop());
        listStates.addFirst(new ListSettings(top, fileList.getFirstVisiblePosition()));
    }


    private void getFileContent(Operation operation) {
        new FileContentTask().execute(operation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_toolbar, menu);

        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.addFile: {
                String[] files = rootFile.list();
                String name = "test";
                item.setEnabled(false);
                if (rootFile.canWrite()) {
                    if (files != null && files.length != 0) {
                        boolean contains;
                        int count = 0;
                        Arrays.sort(files);
                        do {
                            int result;
                            if (count == 0) {
                                result = Arrays.binarySearch(files, name);
                            } else {
                                result = Arrays.binarySearch(files, name + count);
                            }


                            if (result <= -1) {
                                contains = false;
                                if (count != 0) {
                                    name = name + count;
                                }
                            } else {
                                count++;
                                contains = true;
                            }
                        } while (contains == true);
                    }
                    File newFile = new File(rootFile.getAbsolutePath() + "/" + name);
                    try {
                        newFile.createNewFile();
                        Toast.makeText(MainActivity.this, "Soubor " + name + " byl vytvoren.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        item.setEnabled(true);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Nelze zpisovat", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.refresh: {
                saveListPostition();
                getFileContent(Operation.UPDATE);
                return true;
            }
            case R.id.settings: {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            case R.id.shutdown: {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //TODO opravit
        File parent = rootFile.getParentFile();
        if (parent != null) {
            rootFile = parent;
            getFileContent(Operation.CLOSE);
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

    private void animationSlideRight(final File[] files, boolean empty) {
        Animation animation;
        if (empty) {
            animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_empty);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isEmpty.setVisibility(View.GONE);
                    fileList.setAdapter(new FileListAdapter(MainActivity.this, files));
                    restoreListPosition();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fileList.setAdapter(new FileListAdapter(MainActivity.this, files));
                    restoreListPosition();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        fileList.startAnimation(animation);
    }

    private void restoreListPosition() {
        if (!listStates.isEmpty()) {
            final ListSettings x = listStates.pollFirst();
            fileList.post(new Runnable() {
                @Override
                public void run() {
                    fileList.setSelectionFromTop(x.selected, x.fromTop);
                }
            });
        }
    }

    private void animationSlideLeft(final File[] files, final boolean empty) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fileList.setAdapter(new FileListAdapter(MainActivity.this, files));
                if (empty) {
                    isEmpty.setVisibility(View.VISIBLE);
                } else {
                    isEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fileList.startAnimation(animation);
    }

    private void animationShake(final File[] files) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                restoreListPosition();
                fileList.setAdapter(new FileListAdapter(MainActivity.this, files));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fileList.startAnimation(animation);
    }

    @Nullable
    private String fileExt(String fileName) {
        if (fileName.indexOf("?") > -1) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        if (fileName.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    public static class ListSettings {
        public int selected;
        public int fromTop;

        public ListSettings(int fromTop, int selected) {
            this.fromTop = fromTop;
            this.selected = selected;
        }

        @Override
        public String toString() {
            return "ListSettings{" +
                    "fromTop=" + fromTop +
                    ", selected=" + selected +
                    '}';
        }
    }

    class FileContentTask extends AsyncTask<Operation, Integer, File[]> {
        private Operation operation;
        private int numDirs = 0, numFiles = 0;

        @Override
        protected void onPreExecute() {
            path.setText(rootFile.getPath());
        }

        @Override
        protected File[] doInBackground(Operation... oper) {
            operation = oper[0];

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

            publishProgress(numDirs, numFiles);

            if (dirs != null) {
                System.arraycopy(dirs, 0, fileContent, 0, dirs.length);
            }
            if (files != null) {
                System.arraycopy(files, 0, fileContent, dirs.length, files.length);
            }

            return fileContent;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(File[] files) {

            if (rootFile.getParentFile() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            if (operation == Operation.OPEN) {
                animationSlideLeft(files, (numDirs + numFiles == 0) ? true : false);
            } else if (operation == Operation.CLOSE) {
                animationSlideRight(files, isEmpty.getVisibility() == View.VISIBLE ? true : false);
            } else if (operation == Operation.UPDATE) {
                animationShake(files);
            } else if (operation == Operation.NOTHING) {
                fileList.setAdapter(new FileListAdapter(MainActivity.this, files));
            }

        }
    }

    private enum Operation {
        OPEN, CLOSE, UPDATE, NOTHING
    }
}
