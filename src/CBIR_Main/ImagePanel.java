package CBIR_Main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel
{
    private BufferedImage image;

    public ImagePanel()
    {
        this.image = null;
    }
    public ImagePanel(BufferedImage im)
    {
        this.image = im;
    }

    public BufferedImage getImage()
    {
        return image;
    }

    public void setImage(BufferedImage image)
    {
        this.image = image;
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = this.getWidth() - 10;
        int h = this.getHeight() - 10;
        int iw = image.getWidth();
        int ih = image.getHeight();

        if (iw > w || ih > h) //scale image
        {
            int tmpw, tmph;
            tmpw = w;
            tmph = (int) ((float) ih * ((float) w / (float) iw));
            if (tmph > h)
            {
                tmph = h;
                tmpw = (int) ((float) iw * ((float) h / (float) ih));
            }
            ih = tmph;
            iw = tmpw;
        }
        g2.drawImage(image, 5 + (w - iw) / 2, 5, iw, ih, null);
    }
}
