package ninja.lbs.filechooser;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ninja.views.FileChooserDialog;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);

        String dialogTag = FileChooserDialog.class.getName();
        Fragment oldDialog = getFragmentManager().findFragmentByTag(dialogTag);
        if (oldDialog == null) {
            FileChooserDialog.Builder builder = new FileChooserDialog.Builder();
            builder.setInitialDirectory(new File(""));
            final Set<String> ACCEPT_EXTENSIONS = new HashSet<>();
            ACCEPT_EXTENSIONS.add("pcm");
            builder.setAcceptExtensions(ACCEPT_EXTENSIONS);
            FileChooserDialog fileChooserDialog = builder.build();
            fileChooserDialog.show(getFragmentManager(), dialogTag);
        }
    }
}
