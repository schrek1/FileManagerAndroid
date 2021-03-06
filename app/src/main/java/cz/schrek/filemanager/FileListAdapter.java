package cz.schrek.filemanager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ondra on 7. 8. 2016.
 */
public class FileListAdapter extends ArrayAdapter<File> {
    private File[] fileContent;
    private Context context;
    private HashMap<Integer, Boolean> listSelection = new HashMap<>();

    public FileListAdapter(Context context, File[] files, int resource) {
        super(context, resource, R.id.label, files);
        fileContent = files;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        ImageView icon = (ImageView) row.findViewById(R.id.icon);
        TextView label = (TextView) row.findViewById(R.id.label);
        TextView details = (TextView) row.findViewById(R.id.details);

        File selected = fileContent[position];

        SimpleDateFormat formater = new SimpleDateFormat("dd.MM. yy HH:mm");
        Date date = new Date(selected.lastModified());

        row.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
        if (listSelection.get(position) != null) {
            row.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
        }

        if (selected.isDirectory()) {
            if (selected.list() == null || selected.list().length == 0) {
                icon.setImageResource(R.drawable.ic_folder_open_black_24dp);
                int color = Color.parseColor("#808080"); //The color u want
                icon.setColorFilter(color);
            } else {
                icon.setImageResource(R.drawable.ic_folder_black_24dp);
                int color = Color.parseColor("#ffc425"); //The color u want
                icon.setColorFilter(color);
            }
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                details.setText("<dir> \t " + formater.format(date));
            }
        } else if (selected.isFile()) {
            icon.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
            int color = Color.parseColor("#004d99"); //The color u want
            icon.setColorFilter(color);

            String fileSize = getFileSize(selected.length());
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                details.setText(fileSize + " \t " + formater.format(date));
            }
        }

        label.setText(selected.getName());

        return row;
    }

    private String getFileSize(double length) {
        int rank = 0;
        while (length >= 1024) {
            length /= 1024;
            rank++;
        }

        DecimalFormat decForm = new DecimalFormat(".#");

        switch (rank) {
            case 0:
                return decForm.format(length) + " B";
            case 1:
                return decForm.format(length) + " kB";
            case 2:
                return decForm.format(length) + " MB";
            default:
                return decForm.format(length) + " GB";
        }
    }

    public void clearSelection() {
        listSelection = new HashMap<>();
        notifyDataSetChanged();
    }

    public Set<Integer> getListSelection() {
        return this.listSelection.keySet();
    }


    public void setNewSelection(int position, boolean checked) {
        listSelection.put(position, checked);
        notifyDataSetChanged();
    }

    public void removeSelection(int position) {
        listSelection.remove(position);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = listSelection.get(position);
        return result == null ? false : result;
    }
}
