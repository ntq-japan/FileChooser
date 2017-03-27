# FileChooser
Demo a file chooser dialog in Android. This project base on [Android Matterial Design](https://developer.android.com/design/material/index.html)

### Versions
##### v1.0
- Create dialog fragment
- Implement basic function
- Define GUI base on [Android Matterial Design](https://developer.android.com/design/material/index.html)

### Getting Started
Eazy way to call the FileChooser Dialog like a AlertDialog. Please note that don't forget to check the runtime permission.

##### Dependence:
```
compile 'ninja.lbs.filechooser:filechooser:1.0.0'
```

##### Demo code:
```java
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
```

### License
Do whatever you want. However, I'm feel happy if this project helpful and please let me know by star it. T^T

### Credit
- (Trần Đức Tâm)[https://github.com/MrNinja] (Android Developer)
- (Đỗ Trang Vương)[https://github.com/motaro222] (Android Developer)
