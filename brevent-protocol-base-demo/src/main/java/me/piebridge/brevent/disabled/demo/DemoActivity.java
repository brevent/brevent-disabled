package me.piebridge.brevent.disabled.demo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import me.piebridge.brevent.protocol.Brevent;
import me.piebridge.brevent.protocol.BreventDisabled;

/**
 * Created by thom on 2018/3/17.
 */
public class DemoActivity extends Activity {

    private TextView output;

    private static final int MY_PERMISSIONS_REQUEST_BREVENT_DISABLED = 59526;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        output = findViewById(R.id.output);
        Brevent.with(this, new BreventDisabled());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BreventDisabled.getInstance().hasBrevent()) {
            checkAsync();
            checkPermission();
        } else {
            updateMessage("没有安装黑阈，或者黑阈不支持停用 API。");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BREVENT_DISABLED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAsync();
                } else {
                    noPermission();
                }
                break;
            default:
                break;

        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, BreventDisabled.PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {BreventDisabled.PERMISSION},
                    MY_PERMISSIONS_REQUEST_BREVENT_DISABLED);
        }
    }

    private void noPermission() {
        updateMessage("用户没有授予权限");
    }

    private void checkAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                check();
            }
        }).start();
    }

    void check() {
        try {
            checkDisabled();
        } catch (IOException | SecurityException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            updateMessage(sw.toString());
        }
    }

    private void checkDisabled() throws IOException {
        final StringBuilder sb = new StringBuilder();
        if (checkStatus(sb)) {

            checkDisabledPackages(sb);

            final String packageName = "com.zhihu.android";
            enable(sb, packageName, false);
            checkDisabled(sb, packageName);
            checkDisabledPackages(sb);

            enable(sb, packageName, true);
            checkDisabled(sb, packageName);
            checkDisabledPackages(sb);
        }
    }

    private boolean checkStatus(StringBuilder sb) throws IOException {
        sb.append("停用: ");
        boolean result = BreventDisabled.getInstance().isAvailable();
        if (result) {
            sb.append("支持\n");
        } else {
            sb.append("不支持\n");
        }
        updateMessage(sb);
        return result;
    }

    private void enable(StringBuilder sb, String packageName, boolean enable) throws IOException {
        sb.append("设置");
        sb.append(packageName);
        sb.append("为");
        if (enable) {
            sb.append("启用");
        } else {
            sb.append("用户停用");
        }
        sb.append("状态: ");
        if (BreventDisabled.getInstance().setPackageEnabled(packageName, 0, enable)) {
            sb.append("成功\n");
        } else {
            sb.append("失败\n");
        }
        updateMessage(sb);
    }

    private void updateMessage(StringBuilder sb) {
        updateMessage(sb.toString());
    }

    private void updateMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(s);
            }
        });
    }

    private void checkDisabled(StringBuilder sb, String packageName) throws IOException {
        sb.append(packageName);
        sb.append("是否用户停用: ");
        if (BreventDisabled.getInstance().isDisabled(packageName, 0)) {
            sb.append("是\n");
        } else {
            sb.append("否\n");
        }
        updateMessage(sb);
    }

    private void checkDisabledPackages(StringBuilder sb) throws IOException {
        sb.append("停用应用: ");
        List<String> disabledPackages = BreventDisabled.getInstance().getDisabledPackages(0);
        if (disabledPackages == null) {
            sb.append("空\n");
        } else {
            sb.append(disabledPackages);
            sb.append("\n");
        }
        updateMessage(sb);
    }

}
