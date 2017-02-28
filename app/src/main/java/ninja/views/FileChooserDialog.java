package ninja.views;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import ninja.DefaultApplication;
import ninja.lbs.filechooser.BuildConfig;
import ninja.lbs.filechooser.R;
import ninja.lbs.utils.NinjaLog;
import ninja.views.filechooser.ChooseMode;
import ninja.views.filechooser.IChooserListener;

public class FileChooserDialog extends DialogFragment implements DirectoryViewAdapter.OnFileSelected, View.OnClickListener {
    public final static String DEFAULT_BTN_SUBMIT_TITLE = "Submit";
    public final static String DEFAULT_BTN_BACK_TITLE = "Back";
    private static final String TAG = FileChooserDialog.class.getSimpleName();
    private final static String KEY_ACCEPT_EXTENSIONS = "acceptExtensions";
    private final static String KEY_DIRECTORY = "initialDirectory";
    private final static String KEY_BTN_BACK = "btn.back";
    private final static String KEY_BTN_SUBMIT = "btn.submit";
    private final static String KEY_BTN_HOME = "btn.home";
    private final static String KEY_BTN_SDCARD = "btn.sdcard";
    // Root folder
    private static final String ROOT = File.separator;
    // Which type of file should be shown on File Chooser? This filter decide which directory and file should be shown.
    private final ChooserFileFilter mFileFilter = new ChooserFileFilter();
    // Which type of file should be shown on File Chooser? This list hold extensions of those kinds of file.
    private List<String> mAcceptExtensions;
    // Adapter for the RecyclerView
    private DirectoryViewAdapter mFileListAdapter;
    // Hold current showing directory
    private String mCurrentDirectory;

    /**
     * Check if we can access to external storage or not.
     * - Check external storage available
     * - Check permission
     *
     * @return {@code true} if we can, {@code false} otherwise.
     */
    private static boolean canAccessExternalStorage() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "canAccessExternalStorage()");
        }

        String externalStorageState = Environment.getExternalStorageState();
        boolean canAccess = Environment.MEDIA_MOUNTED.equals(externalStorageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
        if (Build.VERSION.SDK_INT >= 16) {
            canAccess &= PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(DefaultApplication.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "canAccessExternalStorage()");
        }

        return canAccess;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onCreateDialog(Bundle)");
        }

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onCreateDialog(Bundle)");
        }
        return dialog;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onCreateView(LayoutInflater, ViewGroup, Bundle)");
        }

        // Inflate view
        View view = inflater.inflate(R.layout.fg_file_chooser, container);

        Bundle argument = getArguments();
        if (savedInstanceState != null) {// In case, recreate this screen
            if (savedInstanceState.containsKey(KEY_DIRECTORY)) {
                // Check current directory
                mCurrentDirectory = savedInstanceState.getString(KEY_DIRECTORY);
            }
        } else {// If the first time create this screen
            // Check current directory
            if (argument.containsKey(KEY_DIRECTORY)) {
                mCurrentDirectory = argument.getString(KEY_DIRECTORY);
            }
        }

        // Verify current directory
        if (TextUtils.isEmpty(mCurrentDirectory)) {
            mCurrentDirectory = Environment.getExternalStorageDirectory().getPath();
        }

        // Define accept extensions
        String[] acceptExtensionsArray = argument.getStringArray(KEY_ACCEPT_EXTENSIONS);
        if (acceptExtensionsArray != null) {
            mAcceptExtensions = Arrays.asList(acceptExtensionsArray);
        }

        // Create adapter and update recycler view
        RecyclerView listView = (RecyclerView) view.findViewById(R.id.directory_file_list);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (mFileListAdapter == null) {
            mFileListAdapter = new DirectoryViewAdapter();
            mFileListAdapter.setChooseMode(ChooseMode.MULTIPLE);
        }
        listView.setAdapter(mFileListAdapter);
        listView.addItemDecoration(new SimpleDividerDecoration(getActivity()));
        mFileListAdapter.setOnFileSelectedListener(this);
        if (mFileListAdapter.getItemCount() == 0) {
            view.findViewById(R.id.directory_file_list).setVisibility(View.GONE);
            view.findViewById(R.id.empty).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.directory_file_list).setVisibility(View.VISIBLE);
            view.findViewById(R.id.empty).setVisibility(View.GONE);
        }

        Button btnBack = (Button) view.findViewById(R.id.back);
        btnBack.setOnClickListener(this);
        if (argument.containsKey(KEY_BTN_BACK)) {
            btnBack.setText(argument.getString(KEY_BTN_BACK));
        } else {
            btnBack.setText(DEFAULT_BTN_BACK_TITLE);
        }

        Button btnSubmit = (Button) view.findViewById(R.id.decide);
        btnSubmit.setOnClickListener(this);
        if (argument.containsKey(KEY_BTN_SUBMIT)) {
            btnSubmit.setText(argument.getString(KEY_BTN_SUBMIT));
        } else {
            btnBack.setText(DEFAULT_BTN_SUBMIT_TITLE);
        }

        Button btnHome = (Button) view.findViewById(R.id.navigate_root);
        btnHome.setOnClickListener(this);
        if (argument.containsKey(KEY_BTN_HOME)) {
            btnHome.setVisibility(View.VISIBLE);
            btnHome.setText(argument.getString(KEY_BTN_HOME));
        } else {
            btnHome.setVisibility(View.GONE);
        }

        Button btnSdcard = (Button) view.findViewById(R.id.navigate_sdcard);
        btnSdcard.setOnClickListener(this);
        if (argument.containsKey(KEY_BTN_SDCARD)) {
            btnSdcard.setVisibility(View.VISIBLE);
            btnSdcard.setText(argument.getString(KEY_BTN_SDCARD));
        } else {
            btnSdcard.setVisibility(View.GONE);
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onCreateView(LayoutInflater, ViewGroup, Bundle)");
        }

        return view;
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onResume()");
        }

        super.onResume();
        if (!TextUtils.isEmpty(mCurrentDirectory)) {
            loadFileList(mCurrentDirectory, true);
        } else {
            dismiss();
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onResume()");
        }
    }

    @Override
    public void onClick(View v) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onClick(View)");
        }

        int id = v.getId();
        switch (id) {
            case R.id.decide:
                decideFileToTake();
                break;
            case R.id.back:
                upOneLevel();
                break;
            case R.id.navigate_sdcard:
                navigateSdcard();
                break;
            case R.id.navigate_root:
                navigateRoot();
                break;
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onClick(View)");
        }
    }

    @Override
    public void onFileSelected(String path) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onFileSelected(String)");
        }

        File selectedFile = new File(path);
        if (selectedFile.exists() && selectedFile.isDirectory()) {
            loadFileList(path, false);
        } else {
            syncButtonState();
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onFileSelected(String)");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onSaveInstanceState(Bundle)");
        }

        outState.putString(KEY_DIRECTORY, mCurrentDirectory);
        super.onSaveInstanceState(outState);

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onSaveInstanceState(Bundle)");
        }
    }

    /**
     * Show root directory content.
     */
    private void navigateRoot() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "navigateRoot()");
        }

        Context context = getActivity();
        if (context != null) {
            loadFileList(ROOT, false);
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "navigateRoot()");
        }
    }

    /**
     * Show sdcard directory content.
     */
    private void navigateSdcard() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "navigateSdcard()");
        }

        Context context = getActivity();
        if (context != null && canAccessExternalStorage()) {
            loadFileList(Environment.getExternalStorageDirectory().getAbsolutePath(), false);
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "navigateSdcard()");
        }
    }

    /**
     * Show parent directory content.
     */
    private void upOneLevel() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "upOneLevel()");
        }

        if (canUpOneLevel()) {
            File currentFile = new File(mCurrentDirectory);
            File parent = currentFile.getParentFile();
            loadFileList(parent.getAbsolutePath(), false);
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "upOneLevel()");
        }
    }

    /**
     * Check if can up one level
     */
    private boolean canUpOneLevel() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "canUpOneLevel()");
        }

        File currentFile = new File(mCurrentDirectory);
        File parent = currentFile.getParentFile();

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "canUpOneLevel()");
        }

        return parent != null && parent.canRead();
    }

    /**
     * Take the selected file and dismiss File Chooser dialog (and then notify to the callback).
     */
    private void decideFileToTake() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "decideFileToTake()");
        }

        Activity activity = getActivity();
        if (activity instanceof IChooserListener) {
            List<String> listSelectedFile = mFileListAdapter.getListSelectedPath();
            int length = listSelectedFile.size();
            if (length > 0) {
                ((IChooserListener) activity).onFileSelect(listSelectedFile.toArray(new String[length]));
            }
        }
        dismissAllowingStateLoss();

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "decideFileToTake()");
        }
    }

    /**
     * Show content of the directory.
     *
     * @param path     the directory whose content will be shown.
     * @param isReload Allow to reload this list.
     */
    private void loadFileList(String path, boolean isReload) {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "loadFileList(String, boolean)");
        }

        if (!TextUtils.isEmpty(path)) {
            if (isReload || !mCurrentDirectory.equals(path)) {
                File currentFile = new File(path);
                if (currentFile.exists() && currentFile.isDirectory()) {
                    File[] files = currentFile.listFiles(mFileFilter);
                    if (files != null) {
                        Arrays.sort(files, new FileComparator());
                    }
                    mFileListAdapter.setFileList(files);
                    if (files == null || files.length == 0) {
                        Dialog dialog = getDialog();
                        dialog.findViewById(R.id.directory_file_list).setVisibility(View.GONE);
                        dialog.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                    } else {
                        Dialog dialog = getDialog();
                        dialog.findViewById(R.id.directory_file_list).setVisibility(View.VISIBLE);
                        dialog.findViewById(R.id.empty).setVisibility(View.GONE);
                    }
                    mCurrentDirectory = path;
                }

                syncButtonState();
            }
        }

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "loadFileList(String, boolean)");
        }
    }

    /**
     * Sync button with the current state.
     */
    private void syncButtonState() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "loadFileList()");
        }
        Dialog dialog = getDialog();

        // Update title and sync back button
        if (!TextUtils.isEmpty(mCurrentDirectory)) {
            File currentFile = new File(mCurrentDirectory);
            if (currentFile.exists()) {
                // Update window title
                if (currentFile.isDirectory()) {
                    TextView txtTitle = (TextView) dialog.findViewById(R.id.title);
                    String title = currentFile.getName();
                    if (TextUtils.isEmpty(title)) {
                        title = ROOT;
                    }
                    txtTitle.setText(String.valueOf(title));
                }

                // Update button upper one folder
                dialog.findViewById(R.id.back).setEnabled(canUpOneLevel());
            }
        }

        // Navigate folder button
        boolean canAccessExternalStorage = canAccessExternalStorage();
        View btnSdcard = dialog.findViewById(R.id.navigate_sdcard);
        if (canAccessExternalStorage) {
            btnSdcard.setEnabled(!mCurrentDirectory.equals(Environment.getExternalStorageDirectory().getPath()));
        } else {
            btnSdcard.setEnabled(false);
        }

        View btnRoot = dialog.findViewById(R.id.navigate_root);
        btnRoot.setEnabled(!mCurrentDirectory.equals(ROOT));

        // Update submit button
        dialog.findViewById(R.id.decide).setEnabled(mFileListAdapter.getListSelectedPath().size() > 0);

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "loadFileList(String)");
        }
    }

    /**
     * Builder class help to build FileChooserDialog.
     */
    public static class Builder {
        private final String TAG = FileChooserDialog.TAG + ".Builder";
        private String[] mAcceptExtensions;
        private String mInitialDirectory;
        private String mBtnBack = DEFAULT_BTN_BACK_TITLE;
        private String mBtnSubmit = DEFAULT_BTN_SUBMIT_TITLE;
        private String mBtnHome;
        private String mBtnSdcard;

        public Builder setBtnBack(String btnBack) {
            if (TextUtils.isEmpty(btnBack)) {
                if (BuildConfig.DEBUG) {
                    NinjaLog.w(TAG, "setBtnBack(String)", "Button back title is empty");
                }
            } else {
                this.mBtnBack = btnBack;
            }

            return this;
        }

        public Builder setBtnSubmit(String btnSubmit) {
            if (TextUtils.isEmpty(btnSubmit)) {
                if (BuildConfig.DEBUG) {
                    NinjaLog.w(TAG, "setBtnBack(String)", "Button submit title is empty");
                }
            } else {
                this.mBtnSubmit = btnSubmit;
            }

            return this;
        }

        public Builder setBtnHome(String btnHome) {
            if (TextUtils.isEmpty(btnHome)) {
                if (BuildConfig.DEBUG) {
                    NinjaLog.w(TAG, "setBtnBack(String)", "Button home title is empty");
                }
            } else {
                this.mBtnHome = btnHome;
            }

            return this;
        }

        public Builder setBtnSdcard(String btnSdcard) {
            if (TextUtils.isEmpty(btnSdcard)) {
                if (BuildConfig.DEBUG) {
                    NinjaLog.w(TAG, "setBtnBack(String)", "Button sdcard title is empty");
                }
            } else {
                this.mBtnSdcard = btnSdcard;
            }

            return this;
        }

        /**
         * Filter only extensions in list could be show
         *
         * @param acceptExtensions Extensions accepted on this dialog
         */
        public Builder setAcceptExtensions(Set<String> acceptExtensions) {
            if (acceptExtensions != null) {
                this.mAcceptExtensions = new String[acceptExtensions.size()];
                acceptExtensions.toArray(this.mAcceptExtensions);
            } else {
                this.mAcceptExtensions = null;
            }

            return this;
        }

        /**
         * The start location in the dialog. If it not real, home location will be show
         *
         * @param initialDirectory The start location path
         */
        public Builder setInitialDirectory(File initialDirectory) {
            if (initialDirectory == null)
                throw new NullPointerException("initialDirectory can't be null.");

            if (!initialDirectory.exists()) {
                if (canAccessExternalStorage()) {
                    initialDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                } else {
                    initialDirectory = new File(ROOT);
                }
            }

            if (!initialDirectory.isDirectory()) {
                initialDirectory = initialDirectory.getParentFile();
            }

            if (!initialDirectory.canRead())
                throw new IllegalArgumentException("Can't access " + initialDirectory.getPath());

            this.mInitialDirectory = initialDirectory.getPath();
            return this;
        }

        public FileChooserDialog build() {
            if (BuildConfig.DEBUG) {
                NinjaLog.enter(TAG, "build()");
            }

            FileChooserDialog fragment = new FileChooserDialog();

            Bundle args = new Bundle();

            // Set list accept extensions
            args.putStringArray(KEY_ACCEPT_EXTENSIONS, mAcceptExtensions);

            // Initial start directory
            if (!TextUtils.isEmpty(mInitialDirectory)) {
                if (canAccessExternalStorage()) {
                    mInitialDirectory = Environment.getExternalStorageDirectory().getPath();
                } else {
                    mInitialDirectory = ROOT;
                }
            }
            args.putString(KEY_DIRECTORY, mInitialDirectory);

            // Set button back title
            if (!TextUtils.isEmpty(mBtnBack)) {
                args.putString(KEY_BTN_BACK, mBtnBack);
            }

            // Set button submit title
            if (!TextUtils.isEmpty(mBtnSubmit)) {
                args.putString(KEY_BTN_SUBMIT, mBtnSubmit);
            }

            // Set button home title
            if (!TextUtils.isEmpty(mBtnHome)) {
                args.putString(KEY_BTN_HOME, mBtnHome);
            }

            // Set button sdcard title
            if (!TextUtils.isEmpty(mBtnSdcard)) {
                args.putString(KEY_BTN_SDCARD, mBtnSdcard);
            }

            fragment.setArguments(args);

            if (BuildConfig.DEBUG) {
                NinjaLog.enter(TAG, "build()");
            }

            return fragment;
        }
    }

    /**
     * Accept only file and directory.
     * If {@code mAcceptExtensions} is not null, file whose extension isn't in {@code mAcceptExtensions} is not accepted.
     */
    private class ChooserFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.canRead()) {
                if (pathname.isFile()) {
                    return (mAcceptExtensions == null) || (mAcceptExtensions.contains(findExtension(pathname)));
                } else {
                    return pathname.isDirectory();
                }
            }
            return false;
        }

        /**
         * Return lower case extension of file. If the file didn't have extension, empty string would be returned.
         *
         * @param file the file
         * @return lower case extension
         */
        private String findExtension(@NonNull File file) {
            String extension = "";

            if (file.isFile()) {
                String fileName = file.getName();
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i + 1);
                }

                return extension.toLowerCase();
            } else {
                return "";
            }
        }
    }

    /**
     * Compare setting between two files
     */
    public class FileComparator implements Comparator<File> {
        @Override
        public int compare(File source, File dest) {
            if (source.isDirectory() ^ dest.isDirectory()) {
                return source.isDirectory() ? -1 : 1;
            } else {
                String sourceName = source.getName();
                String destName = dest.getName();
                return sourceName.compareTo(destName);
            }
        }
    }
}
