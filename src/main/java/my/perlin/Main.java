/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package my.perlin;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

class PerlinPanel extends JPanel
{
    private final int width = 640;
    private final int height = 480;
    private final int chunk = 40;

    private final BufferedImage image;
    private final PerlinNoise perlin;

    public PerlinPanel() throws Exception
    {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.black);

        setDoubleBuffered(true); // ?

        try
        {
            perlin = new PerlinNoise(width, height, chunk);
            perlin.setBounds(-1d, +1d);
            perlin.setOctaves(3);
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            
            var pixels = new ArrayList<Point<Integer>>(width * height);
            for (int i = 0; i < width; ++i)
            {
                for (int j = 0; j < height; ++j)
                {
                    pixels.add(new Point(i, j));
                }
            }
            
            var noise = perlin.makeNoise(pixels);
            
            int k = 0;
            for (int i = 0; i < width; ++i)
            {
                for (int j = 0; j < height; ++j)
                {
                    double rawNoise = noise.get(k++);
                    int color;
                    if (rawNoise > 0d)
                    {
                        int value = (int) (rawNoise * 255d);
                        color = (value << 16) | (0x00 << 8) | 0x00;
                    }
                    else
                    {
                        int value = (int) (-rawNoise * 255d);
                        color = (0x00 << 16) | (0x00 << 8) | value;
                    }
                    image.setRGB(i, j, color);
                }
            }
        }
        catch (Exception e)
        {
            throw new Exception("PerlinPanel.PerlinPanel");
        }
    }

    @Override
    public void paintComponent(Graphics graphics)
    {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics.drawImage(image, 0, 0, null);
    }
}

/**
 *
 * @author Kay Jay O'Nail
 */
public class Main
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Perlin Noise");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        try
        {
            PerlinPanel panel = new PerlinPanel();
            frame.add(panel);
            frame.pack();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
