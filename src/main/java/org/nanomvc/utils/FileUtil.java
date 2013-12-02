/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.imgscalr.Scalr;

/**
 *
 * @author edmundas
 */
public class FileUtil
{
    public static void thumb(String image, String path, int width, int height, int method)
            throws IOException
    {
        path = (path.endsWith("/")) ? path : path + "/";
        
        String fullPath = new StringBuilder().append(path).append(width).append("x").append(height).toString();
        File newImageFile = new File(new StringBuilder().append(fullPath).append("/").append(image).toString());   
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File oldImageFile = new File(new StringBuilder().append(path).append(image).toString());
        ImageInputStream is = ImageIO.createImageInputStream(oldImageFile);
        BufferedImage srcImage = ImageIO.read(is);
        BufferedImage scaledImage = null;
        switch (method) {
            case 1:
                scaledImage = Scalr.crop(srcImage, width, height);
                break;
            case 2:
            default:
                scaledImage = Scalr.resize(srcImage, Scalr.Mode.AUTOMATIC, width, height);
                break;
            case 3:
                int rWidth = width;
                int rHeight = height;
                double fWidth = ((double)srcImage.getWidth() / ((double)srcImage.getHeight() / (double)height));
                double fHeight = ((double)srcImage.getHeight() / ((double)srcImage.getWidth() / (double)width));
                Scalr.Mode mode = Scalr.Mode.FIT_TO_WIDTH;
                if (srcImage.getWidth() > srcImage.getHeight()) {
                    mode = Scalr.Mode.FIT_TO_HEIGHT;
                    if (width > fWidth) {
                        rHeight = (int)fHeight + ((fHeight > (int)fHeight) ? 1 : 0);
                    } else {

                    }
                } else if (height > fHeight) {
                    rWidth = (int)fWidth + ((fWidth > (int)fWidth) ? 1 : 0);
                }
                scaledImage = Scalr.resize(srcImage, mode, rWidth, rHeight);
                scaledImage = Scalr.crop(scaledImage, width, height);
        }

        String format = RequestUtil.getImageFormat(RequestUtil.getMimeType(oldImageFile));
        ImageIO.write(scaledImage, format, newImageFile);
        scaledImage.flush();
        srcImage.flush();
    }
}
