package com.albermonte.extendeddialog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class SvgImageLoader {

    private static final String SVG_DATA_URL_PREFIX = "data:image/svg+xml";
    private static final String DATA_URL_PREFIX = "data:";
    private static final String SVG_SUFFIX = ".svg";

    private SvgImageLoader() {}

    static boolean isDataUrl(String source) {
        return source != null && source.regionMatches(true, 0, DATA_URL_PREFIX, 0, DATA_URL_PREFIX.length());
    }

    static boolean isSvgSource(String source) {
        if (source == null) return false;
        if (source.regionMatches(true, 0, SVG_DATA_URL_PREFIX, 0, SVG_DATA_URL_PREFIX.length())) return true;
        Uri uri = Uri.parse(source);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            return false;
        }
        String path = uri.getPath();
        return path != null && path.toLowerCase(Locale.ROOT).endsWith(SVG_SUFFIX);
    }

    /** Supports `;base64` and percent-encoded payloads. */
    static byte[] decodeDataUrl(String source) throws IOException {
        int comma = source.indexOf(',');
        if (comma < 0) throw new IOException("Malformed data URL");
        String header = source.substring(0, comma);
        String payload = source.substring(comma + 1);
        if (header.toLowerCase(Locale.ROOT).contains(";base64")) {
            return Base64.decode(payload, Base64.DEFAULT);
        }
        return URLDecoder.decode(payload, StandardCharsets.UTF_8.name()).getBytes(StandardCharsets.UTF_8);
    }

    /** Sizes Bitmap to {@code targetView}'s layout params (or 64dp default). */
    static Bitmap render(String source, ImageView targetView) throws SVGParseException, IOException {
        SVG svg = parse(source);
        ViewGroup.LayoutParams params = targetView.getLayoutParams();
        int width = params != null ? params.width : 0;
        int height = params != null ? params.height : 0;
        if (width <= 0 || height <= 0) {
            float density = targetView.getResources().getDisplayMetrics().density;
            int fallback = (int) (64 * density);
            if (width <= 0) width = fallback;
            if (height <= 0) height = fallback;
        }

        // Rescale via viewBox + preserveAspectRatio so small-viewport SVGs
        // (e.g. width="20") aren't rendered tiny in the top-left corner.
        svg.setDocumentWidth(width);
        svg.setDocumentHeight(height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        svg.renderToCanvas(canvas);
        return bitmap;
    }

    private static SVG parse(String source) throws SVGParseException, IOException {
        if (isDataUrl(source)) {
            byte[] bytes = decodeDataUrl(source);
            try (InputStream in = new ByteArrayInputStream(bytes)) {
                return SVG.getFromInputStream(in);
            }
        }
        try (InputStream in = new URL(source).openStream()) {
            return SVG.getFromInputStream(in);
        }
    }
}
