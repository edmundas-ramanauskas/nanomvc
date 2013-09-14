package com.nanomvc.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public final class RequestUtil {

    public static String getMimeType(String url)
            throws IOException {
        try {
            InputStream is = getInputStream(url);
            byte[] data = IOUtils.toByteArray(is);
            return Magic.getMagicMatch(data).getMimeType();
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException ex) {
        }
        return null;
    }

    public static String getMimeType(File file) throws IOException {
        try {
            return Magic.getMagicMatch(file, true).getMimeType();
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException ex) {
        }
        return null;
    }

    public static String getMimeType(byte[] data) throws IOException {
        try {
            return Magic.getMagicMatch(data).getMimeType();
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException ex) {
        }
        return null;
    }

    public static String getMimeType(ImageInputStream is) throws IOException {
        return getMimeType(IOUtils.toByteArray((InputStream) is));
    }

    public static String getMimeType(InputStream is) throws IOException {
        return getMimeType(IOUtils.toByteArray(is));
    }

    public static String getImageFormat(String mime) throws IOException {
        if (mime.startsWith("image")) {
            switch (mime) {
                case "image/jpeg":
                case "image/jpg":
                    return FORMAT.JPG.toString();
                case "image/png":
                    return FORMAT.PNG.toString();
                case "image/gif":
                    return FORMAT.GIF.toString();
                case "image/bmp":
                    return FORMAT.BMP.toString();
            }
        }
        return null;
    }

    public static String saveImage(InputStream is, String path, String file) throws IOException, NullPointerException {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        byte[] data = IOUtils.toByteArray(is);
        String format = getImageFormat(getMimeType(data));
        if (!isWithImageExt(file).booleanValue()) {
            file = file + "." + format.toLowerCase();
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
        if (ImageIO.write(image, format, new File(dir + "/" + file))) {
            is.close();
            return file;
        }
        return null;
    }

    private static Boolean isWithImageExt(String file) {
        return Boolean.valueOf((file.endsWith(".jpg")) || (file.endsWith(".png")) || (file.endsWith(".gif")) || (file.endsWith(".bmp")));
    }

    public static String saveImage(String url, String path, String file) throws IOException, NullPointerException {
        return saveImage(getInputStream(url), path, file);
    }

    public static String httpRequest(String url) throws IOException {
        return getData(getInputStream(url));
    }

    public static String httpRequest(String method, String url, Map<String, Object> params) throws IOException {
        return getData(getInputStream(url, method, params));
    }

    public static InputStream getInputStream(String url) throws IOException {
        return getConnection(url, "GET").getInputStream();
    }

    public static InputStream getInputStream(String url, String method) throws IOException {
        return getConnection(url, method).getInputStream();
    }

    public static InputStream getInputStream(String url, String method, Map<String, Object> params) throws IOException {
        HttpURLConnection conn = getConnection(url, method);
        conn.getOutputStream().write(buildQuery(params).getBytes("UTF-8"));
        return conn.getInputStream();
    }

    private static HttpURLConnection getConnection(String url, String method) throws IOException {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method.toUpperCase());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            return conn;
        } catch (MalformedURLException e) {
        }
        return null;
    }

    public static String buildQuery(Map<String, Object> map) {
        List parts = new ArrayList();
        for (Map.Entry entry : map.entrySet()) {
            StringBuffer sb = new StringBuffer();
            sb.append((String) entry.getKey()).append("=").append(entry.getValue());
            parts.add(sb.toString());
        }
        return StringUtils.join(parts, "&");
    }

    private static String getData(InputStream is) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }

    private static String getData(String url) throws IOException {
        URL requestUrl = new URL(url);

        URLConnection conn = requestUrl.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return getData(conn.getInputStream());
    }

    public static enum FORMAT {

        JPG, PNG, GIF, BMP;
    }
}