/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package my.perlin;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

class PerlinPanel extends JPanel
{
    private final int width = 320;
    private final int height = 240;
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
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            for (int i = 0; i < width; ++i)
            {
                for (int j = 0; j < height; ++j)
                {
                    int noise = Math.min((int) (perlin.getNoise(i, j) * 255.f), 255);
                    int color = (noise << 16) | (noise << 8) | noise;
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
//        JFrame frame = new JFrame("Perlin Noise");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setResizable(false);
//
//        try
//        {
//            PerlinPanel panel = new PerlinPanel();
//            frame.add(panel);
//            frame.pack();
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getMessage());
//        }
//
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
        try
        {
            PerlinNoise tester = new PerlinNoise(100, 100, 10);
            if (tester.$test())
            {
                System.out.println("Test passed!");
            }
            else
            {
                System.out.println("Test failed!");
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
}
