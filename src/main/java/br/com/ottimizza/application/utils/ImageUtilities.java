package br.com.ottimizza.application.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.imageio.ImageIO;

public class ImageUtilities { // @formatter:off

    private Dimension calculateFinalSize(BufferedImage image, int maxSize) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w == h) {
            // se imagem for quadrada, retorna tamanho máximo para altura e largura.
            return new Dimension(maxSize, maxSize);
        } else if (w > h) {
            // se largura for maior que altura,
            // largura -> recebe tamanho máximo
            // altura -> recebe a divisão da altura pela largura multiplicado pelo
            // tamanho máximo, para manter o aspect ratio.
            return new Dimension(maxSize, (int) (((double) h / (double) w) * maxSize));
        } else {
            // se largura for maior que altura,
            // altura -> recebe tamanho máximo
            // largura -> recebe a divisão da largura pela altura multiplicado pelo
            // tamanho máximo, para manter o aspect ratio.
            return new Dimension((int) (((double) w / (double) h) * maxSize), maxSize);
        }
    }

    private BufferedImage removeTransparency(BufferedImage bufferedImage, Color color) {
        BufferedImage copy = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();

        return copy;
    }

    private BufferedImage removeTransparency(BufferedImage bufferedImage) {
        return removeTransparency(bufferedImage, Color.WHITE);
    }

    public void writeFile(String output, BufferedImage image) throws IOException {
        String formatName = output.substring(output.lastIndexOf(".") + 1);
        ImageIO.write(image, formatName, new File(output));
    }

    public void writeFile(File output, BufferedImage image) throws IOException {
        writeFile(output.getAbsolutePath(), image);
    }

    private BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint,
            boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        
        int w, h;

        if (targetWidth > img.getWidth() || targetHeight > img.getHeight() ) {
            System.out.println("Final size is bigger");
            return img;
        }

        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    /* ************************************************************************************************** * 
     * IMAGE COMPRESSION
     * ************************************************************************************************** */
    public BufferedImage compress(BufferedImage image, int size, boolean removeTransparency, boolean higherQuality) { 
        // calcula o width x height final da imagem, baseado no tamanho
        // máximo mantendo aspect ratio.
        final Dimension dimension = this.calculateFinalSize(image, size);

        // compresses the image. 
        BufferedImage compressed = getScaledInstance(
            image, 
            (int) dimension.getWidth(), 
            (int) dimension.getHeight(),
            RenderingHints.VALUE_INTERPOLATION_BILINEAR, 
            higherQuality
        );

        // removes image's transparency.
        if (removeTransparency) {
            compressed = removeTransparency(compressed);
        }

        return compressed;
    }

    public BufferedImage compress(BufferedImage image, int size, boolean removeTransparency) { // @formatter:off
        return compress(image, size, removeTransparency, false);
    }

    public BufferedImage compress(File image, int size, boolean removeTransparency, boolean higherQuality) throws IOException { // @formatter:off
        return compress(ImageIO.read(image), size, removeTransparency, higherQuality);
    }

    public BufferedImage compress(File image, int size, boolean removeTransparency) throws IOException { // @formatter:off
        return compress(ImageIO.read(image), size, removeTransparency, false);
    }

}