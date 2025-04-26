package com.shadowhunter.scanner;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.king.camera.scan.AnalyzeResult;
import com.king.camera.scan.CameraScan;
import com.king.camera.scan.analyze.Analyzer;
import com.king.camera.scan.util.PointUtils;
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity;
import com.king.wechat.qrcode.scanning.analyze.WeChatScanningAnalyzer;
import com.king.wechat.qrcode.scanning.view.ViewfinderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WeChatMultiQRCodeActivity extends WeChatCameraScanActivity {
    private static final String TAG = "WeChatMultiQRCodeActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String bottomText = intent.getStringExtra("bottomText");
        setBottomText(bottomText);
    }

    @Override
    public void onScanResultCallback(AnalyzeResult<List<String>> result) {
        ivFlashlight.setVisibility(View.GONE);

        getCameraScan().setAnalyzeImage(false);

        getCameraScan().stopCamera();


        if (result instanceof WeChatScanningAnalyzer.QRCodeAnalyzeResult){
            int width = result.getImageWidth();
            int height = result.getImageHeight();
//            ivResult.setImageBitmap(previewView.getBitmap());

            ArrayList<Point> points = new ArrayList<>();
            Objects.requireNonNull(((WeChatScanningAnalyzer.QRCodeAnalyzeResult<List<String>>) result).getPoints()).forEach(mat -> {
                // 扫码结果二维码的四个点（一个矩形）
                Log.d(TAG, "point0: ${mat[0, 0][0]}, ${mat[0, 1][0]}");
                Log.d(TAG, "point1: ${mat[1, 0][0]}, ${mat[1, 1][0]}");
                Log.d(TAG, "point2: ${mat[2, 0][0]}, ${mat[2, 1][0]}");
                Log.d(TAG, "point3: ${mat[3, 0][0]}, ${mat[3, 1][0]}");
                Point point0 = new Point((int) mat.get(0,0)[0], (int) mat.get(0,1)[0]);
                Point point1 = new Point((int) mat.get(1,0)[0], (int) mat.get(1,1)[0]);
                Point point2 = new Point((int) mat.get(2,0)[0], (int) mat.get(2,1)[0]);
                Point point3 = new Point((int) mat.get(3,0)[0], (int) mat.get(3,1)[0]);

                double centerX = (point0.x + point1.x + point2.x + point3.x) / 4;
                double centerY = (point0.y + point1.y + point2.y + point3.y) / 4;

                Point point = PointUtils.transform(
                        (int)centerX,
                        (int)centerY,
                        width,
                        height,
                        viewfinderView.getWidth(),
                        viewfinderView.getHeight()
                );
                points.add(point);
            });

            viewfinderView.setOnItemClickListener(new ViewfinderView.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent = new Intent();
                    intent.putExtra(CameraScan.SCAN_RESULT, result.getResult().get(position));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            viewfinderView.showResultPoints(points);

            if (result.getResult().size() == 1) {
                Intent intent = new Intent();
                intent.putExtra(CameraScan.SCAN_RESULT, result.getResult().get(0));
                setResult(RESULT_OK, intent);
                finish();
            }
        }else{
            Intent intent = new Intent();
            intent.putExtra(CameraScan.SCAN_RESULT, result.getResult().get(0));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Nullable
    @Override
    public Analyzer<List<String>> createAnalyzer() {
        return new WeChatScanningAnalyzer(true);
    }

//    @Override
//    public int getViewfinderViewId() {
//        return ViewfinderView.NO_ID;
//    }

}
