package com.example.fayf_android002;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Map;
import java.util.logging.Logger;

public class ContactFragment extends Fragment {

    Logger logger = Logger.getLogger(ContactFragment.class.getName());

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = null;
    private View rootView;
    private QRMode showTenant = QRMode.HOMEPAGE_LINK;

    enum QRMode {
        HOMEPAGE_LINK,
        TENANT_LINK_DISABLED,
        DOWNLOAD_LINK;

        QRMode toggle() {
            QRMode[] values = QRMode.values();
            //return this.ordinal() + 1 < values.length ? values[this.ordinal() + 1] : values[0];
            // TODO we do not need multiple QR as we transfer same data in different formats
            // TODO - check - is that a good idea? can we export systemID?
            return HOMEPAGE_LINK.equals(this) ? DOWNLOAD_LINK : HOMEPAGE_LINK;
        }
    }

    public static String createLink(String path, Map<String, String> params) {
        StringBuilder linkBuilder = new StringBuilder("https://fayf.info");
        if (path != null && !path.isEmpty()) {
            if (!path.startsWith("/")) {
                linkBuilder.append("/");
            }
            linkBuilder.append(path);
        }
        if (params != null && !params.isEmpty()) {
            linkBuilder.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                linkBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            // Remove the trailing '&'
            linkBuilder.setLength(linkBuilder.length() - 1);
        }
        return linkBuilder.toString();
    }

    public static String extractTenantIdFromQrContent(String content) {
        return content != null ? content.replaceAll("https://fayf.info(/.*)?[?&](tenant|tid)=|&.*$", "") : null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        logger.info("ContactFragment onViewCreated called");
        rootView = inflater.inflate(R.layout.fragment_contact, view, false);

        Button scanQrCodeButton = rootView.findViewById(R.id.scanQrCodeButton);
        Button scanQrCodeShareAppButton = rootView.findViewById(R.id.qrCodeToggleButton);

        barcodeLauncher = registerForActivityResult(
                new ScanContract(), result -> {
                    if (result != null) {
                        if (result.getContents() != null) {
                            // extract tenant ID from scanned QR code - might be HomePage or Download link
                            String content =  result.getContents();
                            logger.info("QR Scan result: " + content);
                            String scannedTenantId = extractTenantIdFromQrContent(content);
                            setTenantId(scannedTenantId);
                        } else {
                            logger.warning("QR Scan result is empty.");
                        }
                    } else {
                        logger.warning("QR Scan result is null.");
                    }
                });

        scanQrCodeButton.setOnClickListener(v -> scanQRCode());
        scanQrCodeShareAppButton.setOnClickListener(v -> {
            showTenant = showTenant.toggle();
            showQR();
        });
        showQR();
        return rootView;
    }

    private void showQR() {
        // Logic to toggle between different QR codes
        logger.info("Toggling QR Codes.");

        String tenantId = Config.TENANT.getValue(); // may be <id>:<name>
        TextView title = (TextView) rootView.findViewById(R.id.qrCodeTitleTextView);
        ImageView qrCodeImageView = rootView.findViewById(R.id.qrCodeImageView);
        TextView qrCodeUrlTextView = rootView.findViewById(R.id.qrCodeUrlTextView);
        Button qrCodeToggleButton = rootView.findViewById(R.id.qrCodeToggleButton);
        Button scanQrCodeButton = rootView.findViewById(R.id.scanQrCodeButton);


        if (showTenant.equals(QRMode.TENANT_LINK_DISABLED)) {
            // TODO proper split of tenant ID and name
            title.setContentDescription("QR Code for Tenant ID: \n" + tenantId.replaceAll(".*:", " "));
            title.setVisibility(View.VISIBLE);
            title.setText("Tenant: " + tenantId.replaceAll(".*:", " "));
            qrCodeUrlTextView.setText(tenantId);
            generateTenantQRCode(tenantId, qrCodeImageView);
            scanQrCodeButton.setText("Scan other Tenants QR Code.");
            scanQrCodeButton.setVisibility(View.VISIBLE);
        } else if (showTenant.equals(QRMode.DOWNLOAD_LINK)) {
            logger.info("Share App button clicked.");
            MainActivity.getInstance().updateActionBarTitle("Share App via QR");
            //title.setText("Just share \n the app via QR!");
            String randomSessionId = Util.generateRandomString(12);
            String downloadLink = createLink("fayf.apk", Map.of(
                    //"sid", randomSessionId,
                    "src", "qr",
                    "tid", tenantId
            ));
            qrCodeUrlTextView.setText(downloadLink);
            qrCodeUrlTextView.setClickable(true);
            qrCodeUrlTextView.setFocusable(true);
            qrCodeUrlTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            android.text.util.Linkify.addLinks(qrCodeUrlTextView, android.text.util.Linkify.WEB_URLS);
            qrCodeUrlTextView.setPaintFlags(qrCodeUrlTextView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            generateDownloadLinkQR(downloadLink, qrCodeImageView);
            scanQrCodeButton.setText(""); // TODO disable scan button for app download QR, w/o changing layout
            scanQrCodeButton.setVisibility(View.INVISIBLE);
        } else if (showTenant.equals(QRMode.HOMEPAGE_LINK)) {
            logger.info("Share Homepage button clicked.");
            //title.setText("Fayf Homepage");
            MainActivity.getInstance().updateActionBarTitle("Fayf Homepage");
            String homepageLink = createLink("", Map.of(
                    "tid", tenantId
            ));
            qrCodeUrlTextView.setText(homepageLink);
            qrCodeUrlTextView.setClickable(true);
            qrCodeUrlTextView.setFocusable(true);
            qrCodeUrlTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            android.text.util.Linkify.addLinks(qrCodeUrlTextView, android.text.util.Linkify.WEB_URLS);
            qrCodeUrlTextView.setPaintFlags(qrCodeUrlTextView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            generateDownloadLinkQR(homepageLink , qrCodeImageView);
            if (QRMode.TENANT_LINK_DISABLED.name().contains("DISABLED")) {
                scanQrCodeButton.setText("Scan Tenants QR to join.");
                scanQrCodeButton.setVisibility(View.VISIBLE);
            } else {
                scanQrCodeButton.setText(""); // TODO disable scan button for homepage QR, w/o changing layout
                scanQrCodeButton.setVisibility(View.INVISIBLE);
            }
        }
        // simulate toggle button text change
        QRMode toggle = showTenant.toggle();
        if (toggle.equals(QRMode.TENANT_LINK_DISABLED)) {
            qrCodeToggleButton.setText("Show Tenant QR");
        }  else if (toggle.equals(QRMode.HOMEPAGE_LINK)) {
            qrCodeToggleButton.setText("Show Homepage QR");
        } else if (toggle.equals(QRMode.DOWNLOAD_LINK)) {
            qrCodeToggleButton.setText("Show App Download QR");
        }

    }

    private void generateTenantQRCode(String tenantId, ImageView qrCodeImageView) {
        logger.info("Generating QR Code for tenant ID: " + tenantId);
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

    private void generateDownloadLinkQR(String downloadLink, ImageView qrCodeImageView) {
        logger.info("Generating QR Code for download link: " + downloadLink);
        assert downloadLink != null;
        assert qrCodeImageView != null;

        int width = qrCodeImageView.getWidth();
        int height = qrCodeImageView.getHeight();
        // Use default size if dimensions are not available
        int defaultSize = Util.dpToPx(requireContext(), 300);
        int size = Math.min(width > 0 ? width : defaultSize, height > 0 ? height : defaultSize);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(downloadLink, BarcodeFormat.QR_CODE, size, size);
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
        MainActivity.userInfo("Tenant ID set to: " + tenantId);
        Config.TENANT.setValue(tenantId);
    }
}
