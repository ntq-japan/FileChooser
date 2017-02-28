package ninja.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ninja.lbs.filechooser.BuildConfig;
import ninja.lbs.filechooser.R;
import ninja.lbs.utils.NinjaLog;
import ninja.views.filechooser.ChooseMode;

/**
 * Adapter for {@link RecyclerView} to show list files of a directory.
 */
class DirectoryViewAdapter extends RecyclerView.Adapter<DirectoryViewAdapter.DirectoryViewHolder> {
    private static final String TAG = DirectoryViewAdapter.class.getSimpleName();
    // Format of file size showing on file info in RecyclerView
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#,###,### bytes");
    // Formatter to format Date showing on file info in RecyclerView
    private static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance();
    // Handle tap event when file has been tap
    private final ItemClickListener mItemClickListener = new ItemClickListener();
    private Context mContext;
    private ChooseMode chooseMode = ChooseMode.SINGLE;
    // For outside use. Set this listener to listen on tap-to-file-in-file-list event
    private OnFileSelected mOnFileSelectedListener;
    // Hold file which has been selected
    private List<String> mListSelectedPath = new ArrayList<>();

    // Hold list of file current viewing directory
    private File[] mFiles;

    public List<String> getListSelectedPath() {
        return mListSelectedPath;
    }

    /**
     * Set data for the RecyclerView
     *
     * @param fileList list of {@link File}
     */
    public void setFileList(File[] fileList) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "setFileList(File[])");
        }

        this.mFiles = fileList;
        mListSelectedPath.clear();
        notifyDataSetChanged();

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "setFileList(File[])");
        }
    }

    /**
     * For outside use. Set the listener to listen on tap-to-file-in-file-list event.
     *
     * @param onFileSelectedListener listener
     */
    public void setOnFileSelectedListener(OnFileSelected onFileSelectedListener) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "setFileList(File[])");
        }

        this.mOnFileSelectedListener = onFileSelectedListener;

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "setOnFileSelectedListener(OnFileSelected)");
        }
    }

    public void setChooseMode(ChooseMode chooseMode) {
        this.chooseMode = chooseMode;
    }

    @Override
    public DirectoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onCreateViewHolder(ViewGroup, int)");
        }

        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.list_directory_view_item, parent, false);
        itemView.setOnClickListener(mItemClickListener);

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onCreateViewHolder(ViewGroup, int)");
        }

        return new DirectoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DirectoryViewHolder holder, int position) {
        File file = mFiles[position];

        holder.fileName.setText(file.getName());

        long lastModified = file.lastModified();
        if (lastModified != 0L) {
            holder.lastModified.setText(DATE_FORMATTER.format(new Date(lastModified)));
        } else {
            holder.lastModified.setText("");
        }

        if (file.isFile()) {
            // Show file type view
            holder.icon.setImageResource(R.drawable.ic_document);

            // Update file size
            long fileSize = file.length();
            holder.fileSize.setText(DECIMAL_FORMATTER.format(fileSize));

            // Highlight feature
            String path = file.getAbsolutePath();
            if (mListSelectedPath.contains(path)) {
                // Update file name
                holder.fileName.setTextColor(ContextCompat.getColor(mContext, R.color.icon));
                holder.fileSize.setTextColor(ContextCompat.getColor(mContext, R.color.icon));
                holder.lastModified.setTextColor(ContextCompat.getColor(mContext, R.color.icon));
                holder.itemView.setSelected(true);
            } else {
                // Update file name
                holder.fileName.setTextColor(ContextCompat.getColor(mContext, R.color.primary));
                holder.fileSize.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text));
                holder.lastModified.setTextColor(ContextCompat.getColor(mContext, R.color.primary_text));
                holder.itemView.setSelected(false);
            }
        } else {
            // Show file type view
            holder.icon.setImageResource(R.drawable.ic_folder);

            // Default folder do not have size
            holder.fileSize.setText("");

            // Update file name
            holder.fileName.setTextColor(ContextCompat.getColor(mContext, R.color.primary_text_dark));
            holder.fileSize.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text));
            holder.lastModified.setTextColor(ContextCompat.getColor(mContext, R.color.primary_text));
            holder.itemView.setSelected(false);
        }

        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.length;
    }

    /**
     * For outside use. Set this listener to listen on tap-to-file-in-file-list event
     */
    interface OnFileSelected {
        /**
         * Called when file has been selected on RecyclerView
         *
         * @param path selected file path
         */
        void onFileSelected(String path);
    }

    class DirectoryViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileSize, lastModified;
        ImageView icon;

        DirectoryViewHolder(View view) {
            super(view);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            lastModified = (TextView) itemView.findViewById(R.id.last_modified);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }

    /**
     * Handle tap event when file has been tap
     */
    private class ItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (BuildConfig.DEBUG) {
                NinjaLog.enter(TAG, "onClick(View)");
            }

            // Check null for the first time file load
            if (mFiles != null) {
                Integer position = (Integer) v.getTag();
                String path = mFiles[position].getPath();
                if (mListSelectedPath.contains(path)) {
                    mListSelectedPath.remove(path);
                } else {
                    if (chooseMode == ChooseMode.SINGLE) {
                        mListSelectedPath.clear();
                    }
                    mListSelectedPath.add(path);
                }
                if (mOnFileSelectedListener != null) {
                    mOnFileSelectedListener.onFileSelected(path);
                }
                notifyDataSetChanged();
            }

            if (BuildConfig.DEBUG) {
                NinjaLog.exit(TAG, "onClick(View)");
            }
        }
    }
}