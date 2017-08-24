package com.booyue.qrreader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.camera.open.OpenCamera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * modify by : 2017/8/24 17:51
 * 为了满足需求，修改了几个地方
 * 1，CaptureActivity的方向改为portrait
 * 2.修改{@link com.google.zxing.client.android.camera.CameraManager#getFramingRect}
 * 3.修改{@link com.google.zxing.client.android.camera.CameraConfigurationManager#setDesiredCameraParameters(OpenCamera, boolean)}
 * 4.6.0以上系统如果需要扫描二维码许动态申请权限
 */

public class MainActivity extends AppCompatActivity {
    //    public static final String QRCODE_URL = "http://blog.csdn.net/lmj623565791/article/category/2680605";
    public static final String QRCODE_URL = "https://github.com/RichardDivid/QRCode";
    private ImageView ivQRCode;
    private Button startScan;
    private TextView tvScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivQRCode = (ImageView) findViewById(R.id.iv_zxing);
        startScan = (Button) findViewById(R.id.start_scan);
        tvScanResult = (TextView) findViewById(R.id.tv_result);
        initListener();
        initData();
    }

    private void initData() {
        generateQRCode();
    }

    /**
     * 监听控件
     * 打开扫描页面和二维码长按识别
     */
    private void initListener() {
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
        if (ivQRCode != null) {
            ivQRCode.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Result re = QRCodeUtils.recogizeQRCode(ivQRCode);
                    if (re == null) {
                        showAlert(re.getText());
                    } else {
                        Toast.makeText(MainActivity.this, "识别失败", Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        }

    }

    /**
     * 显示对话框
     */
    private void showAlert(final String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("扫二维码")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterfacem, int i) {
//                        saveImageToGallery(bitmap);
                        if (url.contains("http")) {
                            Intent n = new Intent(Intent.ACTION_VIEW);
                            n.setData(Uri.parse(url));
                            startActivity(n);
                        } else {
                            tvScanResult.setText(url);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterfacem, int i) {
                        builder.create().dismiss();
                    }
                });
        builder.show();
    }

    /**
     * 生成二维码
     */
    public void generateQRCode() {
        final String filePath = FileUtils.getFileRoot(this) + File.separator + "qr_" + System.currentTimeMillis() + ".jpg";
        //二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = QRCodeUtils.createQRImage(QRCODE_URL, 200, 200, BitmapFactory.
                        decodeResource(getResources(), R.drawable.launcher_icon), filePath);
                if (success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                            ivQRCode.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        }).start();
    }

    private List<String> permissionList = new ArrayList<>();//用于存放需要授权的权限
    private static final int REQUEST_CODE_PERMISSION = 100;

    public void checkPermission() {
        permissionList.clear();
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        for (String permission : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(this, permission);
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.isEmpty()) {
            startScan();
//            mPermissionListener.permissionSuccess();
        } else {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
//                        mPermissionListener.permissionFail();
                        Toast.makeText(MainActivity.this, "权限拒绝无法使用相机", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startScan();
//                mPermissionListener.permissionSuccess();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 打开扫描二维码页面
     */
    public void startScan() {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivity(intent);
    }

}
