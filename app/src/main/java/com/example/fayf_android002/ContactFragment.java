package com.example.fayf_android002;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.fayf_android002.Entry.Entries;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.logging.Logger;

public class ContactFragment extends Fragment {

    Logger logger = Logger.getLogger(ContactFragment.class.getName());

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        logger.info("ContactFragment onViewCreated called");
        View rootView = inflater.inflate(R.layout.fragment_contact, view, false);

        ImageView qrCodeImageView = rootView.findViewById(R.id.qrCodeImageView);
        Button scanQrCodeButton = rootView.findViewById(R.id.scanQrCodeButton);

        String tenantId = Config.TENANT.getValue();
        generateQRCode(tenantId, qrCodeImageView);

        barcodeLauncher = registerForActivityResult(
                new ScanContract(), result -> {
                    if (result != null) {
                        if (result.getContents() != null) {
                            logger.info("QR Scan result: " + result.getContents());
                            String scannedTenantId = result.getContents();
                            setTenantId(scannedTenantId);
                        } else {
                            logger.warning("QR Scan result is empty.");
                        }
                    } else {
                        logger.warning("QR Scan result is null.");
                    }
                });

        scanQrCodeButton.setOnClickListener(v -> scanQRCode());
        return rootView;
    }

    private void generateQRCode(String tenantId, ImageView qrCodeImageView) {
        assert tenantId != null;
        assert qrCodeImageView != null;

        int width = qrCodeImageView.getWidth();
        int height = qrCodeImageView.getHeight();
        // Use default size if dimensions are not available
        int size = Math.min(width > 0 ? width : 400, height > 0 ? height : 400);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(tenantId, BarcodeFormat.QR_CODE, size, size);
            // color of Primary and Background
            int colorForeground =  ContextCompat.getColor(requireContext(), R.color.colorOnSecondary);
            int colorBackground = ContextCompat.getColor(requireContext(), R.color.colorPrimaryVeryLight);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? colorForeground : colorBackground);
                }
            }
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    private void scanQRCode() {
        logger.info("Starting QR Code scan...");
        try {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a QR Code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(false);
            logger.info("Launching barcode scanner...");
            barcodeLauncher.launch(options);
        } catch (Exception e) {
            logger.severe("Error launching QR scanner: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setTenantId(String tenantId) {
        // Logic to set the tenant ID
        logger.info("Tenant ID set to: " + tenantId + " (by QR Scan)");
        MainActivity.getInstance().userInfo("Tenant ID set to: " + tenantId);
        Config.TENANT.setValue(tenantId);
    }
}
