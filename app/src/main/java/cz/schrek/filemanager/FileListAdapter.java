package cz.schrek.filemanager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;

/**
 * Created by ondra on 7. 8. 2016.
 */
public class FileListAdapter extends ArrayAdapter<File> {
    private File[] fileContent;

    public FileListAdapter(Context context, File[] files) {
        super(context, R.layout.row, R.id.label, files);
        fileContent = files;
    }

    public void setFileContent(File[] fileContent) {
        this.fileContent = fileContent;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        ImageView icon = (ImageView) row.findViewById(R.id.icon);
        TextView label = (TextView) row.findViewById(R.id.label);

        if (fileContent[position].isDirectory()) {
            icon.setImageResource(R.drawable.ic_folder_black_24dp);
        } else if (fileContent[position].isFile()) {
            icon.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        }

        String fileName = label.getText().toString();
        label.setText(fileName.substring(1));

        return row;
    }



}
