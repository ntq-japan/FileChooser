package ninja.lbs.utils;

import android.util.Log;

import ninja.lbs.filechooser.BuildConfig;
import ninja.lbs.utils.models.Typography;

/**
 * Default log manager
 */
public class NinjaLog {
    /**
     * 現在のクラス名
     */
    private static final String CLASS_NAME = NinjaLog.class.getName();

    /**
     * デバッグフラグ。デバッグフラグがtrueの場合、ログをadbに送信します
     */
    private static final boolean ENABLE_VERBOSE = true;

    /**
     * 現在のアプリケーションルートパッケージ名
     */
    private static final String ROOT_PACKAGE = BuildConfig.APPLICATION_ID;

    /**
     * ログメソッドフロータイプ
     */
    private static final String LOG_TYPE_FLOW = "Ninja.flow";

    /**
     * ログメソッド情報タイプ
     */
    private static final String LOG_TYPE_INFO = "Ninja.info";

    /**
     * スタックインデックスをカウントし、メソッド名でビルダーに追加します
     *
     * @param builder    プロセスビルダー
     * @param className  クラス名
     * @param methodName ビルドメソッド名
     * @return 処理されたビルダー
     */
    private static StringBuilder getIndexMethodBuilder(StringBuilder builder, String className, String methodName) {
        // 現在のスレッドを取得する
        Thread currentThread = Thread.currentThread();

        // スタックインデックスのカウント
        StackTraceElement[] stackTrace = currentThread.getStackTrace();
        for (StackTraceElement traceElement : stackTrace) {
            String traceName = traceElement.getClassName();
            // Check class name from this application except this class
            if (traceName != null && !traceName.contains(CLASS_NAME) && traceName.contains(ROOT_PACKAGE)) {
                // Input double '-' index
                builder.append(Typography.HYPHEN);
                builder.append(Typography.HYPHEN);
            }
        }

        // 現在のスレッド名を取得する
        String threadName = currentThread.getName();

        // メソッドタグのメッセージを更新する
        builder.append(Typography.DOLAD);
        builder.append(threadName);
        builder.append(Typography.SPACE);
        builder.append(className.replace(ROOT_PACKAGE, ""));
        builder.append(Typography.DOT);
        builder.append(methodName);
        builder.append(Typography.SPACE);

        return builder;
    }

    /**
     * ログを書き込みます（メソッド起動時のログ）
     *
     * @param className  呼び出し元クラスの名前
     * @param methodName 呼び出し元メソッドの名前
     */
    public static void enter(String className, String methodName) {
        // デバッグフラグがtrueの場合、デバッグログレベルをadbに送信します
        if (ENABLE_VERBOSE) {
            // ログタグIDを作成する
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append(Typography.SHARP);

            // Input log thread index builder with name
            getIndexMethodBuilder(tagBuilder, className, methodName);

            // Enter log flag
            tagBuilder.append(Typography.CURLY_BRACKET_OPEN);

            // Send log to adb
            Log.d(LOG_TYPE_FLOW, tagBuilder.toString());
        }
    }

    /**
     * Write debug log message.
     *
     * @param className  Name of the calling class.
     * @param methodName Name of the calling method.
     * @param msg        Debug message.
     */
    public static void d(String className, String methodName, String msg) {
        // When debug is true, send debug log level to adb
        if (ENABLE_VERBOSE) {
            // Create log tag id
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append(Typography.SHARP);

            // Input log thread index builder with name
            getIndexMethodBuilder(tagBuilder, className, methodName);

            // Add message log
            tagBuilder.append(Typography.DOUBLE_DOT);
            tagBuilder.append(Typography.SPACE);
            tagBuilder.append(msg);

            // Send log to adb
            Log.d(LOG_TYPE_INFO, tagBuilder.toString());
        }
    }

    /**
     * Write error log message.
     *
     * @param className  Name of the calling class.
     * @param methodName Name of the calling method.
     * @param msg        Error message.
     */
    public static void e(String className, String methodName, String msg) {
        // Create log tag id
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append(Typography.SHARP);

        // Input log thread index builder with name
        getIndexMethodBuilder(tagBuilder, className, methodName);

        // Add message log
        tagBuilder.append(Typography.DOUBLE_DOT);
        tagBuilder.append(Typography.SPACE);
        tagBuilder.append(msg);

        // Send log to adb
        Log.e(LOG_TYPE_INFO, tagBuilder.toString());
    }

    /**
     * Write information log message.
     *
     * @param className  Name of the calling class.
     * @param methodName Name of the calling method.
     * @param msg        Information message.
     */
    public static void i(String className, String methodName, String msg) {
        // When debug is true, send debug log level to adb
        if (ENABLE_VERBOSE) {
            // Create log tag id
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append(Typography.SHARP);

            // Input log thread index builder with name
            getIndexMethodBuilder(tagBuilder, className, methodName);

            // Add message log
            tagBuilder.append(Typography.DOUBLE_DOT);
            tagBuilder.append(Typography.SPACE);
            tagBuilder.append(msg);

            // Send log to adb
            Log.i(LOG_TYPE_INFO, tagBuilder.toString());
        }
    }

    /**
     * Write warning log message.
     *
     * @param className  Name of the calling class.
     * @param methodName Name of the calling method.
     * @param msg        Warning message.
     */
    public static void w(String className, String methodName, String msg) {
        // Create log tag id
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append(Typography.SHARP);

        // Input log thread index builder with name
        getIndexMethodBuilder(tagBuilder, className, methodName);

        // Add message log
        tagBuilder.append(Typography.DOUBLE_DOT);
        tagBuilder.append(Typography.SPACE);
        tagBuilder.append(msg);

        // Send log to adb
        Log.w(LOG_TYPE_INFO, tagBuilder.toString());
    }

    /**
     * Write log. (Log when a method end)
     *
     * @param className  Name of the calling class.
     * @param methodName Name of the calling method.
     */
    public static void exit(String className, String methodName) {
        // When debug is true, send debug log level to adb
        if (ENABLE_VERBOSE) {
            // Create log tag id
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append(Typography.SHARP);

            // Input log thread index builder with name
            getIndexMethodBuilder(tagBuilder, className, methodName);

            // Exit log flag
            tagBuilder.append(Typography.CURLY_BRACKET_CLOSE);

            // Send log to adb
            Log.d(LOG_TYPE_FLOW, tagBuilder.toString());
        }
    }
}
