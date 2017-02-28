package ninja.views.filechooser;

/**
 * Implement this interface with {@link android.app.Activity} to handle callback.
 */
public interface IChooserListener {
    /**
     * Called when file has been decided to take.
     *
     * @param path List path of the selected files.
     */
    void onFileSelect(String[] path);
}
