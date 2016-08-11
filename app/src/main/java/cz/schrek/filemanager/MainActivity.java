package cz.schrek.filemanager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

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

    private void init() {
        path = (TextView) findViewById(R.id.path);
        isEmpty = (TextView) findViewById(R.id.isEmpty);
        fileList = (ListView) findViewById(R.id.fileList);
//        rootFile = Environment.getRootDirectory().getParentFile();
        rootFile = new File("/storage/sdcard/Download");

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                File selected = fileContent[position];
                if (selected.isDirectory()) {
                    if (!selected.canRead()) {
                        Toast.makeText(MainActivity.this, "Nelze cist ze slozky", Toast.LENGTH_SHORT).show();
                    } else {
                        rootFile = selected;
                        animationSlideLeft();
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

        if (rootFile.getParentFile() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
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
                animationShake();
                getFileContent();
                return true;
            }
            case R.id.settings:
                return true;
            case R.id.shutdown:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        File parent = rootFile.getParentFile();
        if (parent != null) {
            rootFile = parent;
            animationSlideRight();
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

    private void animationSlideRight() {
        Animation animation;
        if (isEmpty.getVisibility() == View.VISIBLE) {
            getFileContent();
            animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_empty);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isEmpty.setVisibility(View.GONE);
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
                    getFileContent();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        fileList.startAnimation(animation);
    }

    private void animationSlideLeft() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getFileContent();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fileList.startAnimation(animation);
    }

    private void animationShake() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getFileContent();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fileList.startAnimation(animation);
    }

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
}
