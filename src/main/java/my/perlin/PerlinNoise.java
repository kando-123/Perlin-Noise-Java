/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.perlin;

import java.util.*;

/**
 * Perlin Noise generator. Works on a pixel map (integral point coordinates).
 * 
 * @author Kay Jay O'Nail
 */
public class PerlinNoise
{
    /**
     * The width of the noised area, in pixels.
     * Shall be positive.
     */
    private final int areaWidth;
    
    /**
     * The height of the noised area, in pixels.
     * Shall be positive.
     */
    private final int areaHeight;
    
    /**
     * The side of the square-shaped virtual chunks the area is divided into.
     * Shall be positive. Should be less than both <code>areaWidth</code> and
     * <code>areaHeight</code>.
     */
    private final int chunkSize;
    
    /**
     * The number of columns of the grid of the gradients.
     */
    private final int gradientCols;
    
    /**
     * The number of rows of the grid of the gradients.
     */
    private final int gradientRows;
    
    /**
     * The grid of the gradients.
     * The gradients are stored as pairs of <i>x</i> and <i>y</i> coordinates.
     */
    private final ArrayList<ArrayList<Coords<Double>>> gradientVectors;
    
    /**
     * Number of octaves in noise generation.
     * The default value is <code>3</code>.
     */
    private int octavesCount;
    
    /**
     * The parameter determining how successive octaves influence the final
     * result.
     * 
     * Shall be a positive fraction. The default value is <code>0.5</code>.
     */
    private double persistence;
    
    /**
     * The parameter determining how many times the area is fractally repeated.
     * Shall be greater than <code>1.0</code>. The default value is
     * <code>2.0</code>.
     */
    private double lacunarity;
    
    /**
     * The maximal possible value of the <i>"raw noise"</i> (the sum of the dot
     * products of the offsets times the gradients).<br>
     * <i>The value is mathematically derived, not user-defined.</i>
     */
    private static final double maxRawNoise = (double) (2.0d * Math.sqrt(2.0d));
    
    /**
     * The scalar (dot) product of vectors (x1, y1) and (x2, y2).
     * 
     * @param x1 x-coordinate of the first vector
     * @param y1 y-coordinate of the first vector
     * @param x2 x-coordinate of the second vector
     * @param y2 y-coordinate of the second vector
     * @return the product of the vectors
     */
    private double dotProduct(double x1, double y1, double x2, double y2)
    {
        return x1 * x2 + y1 * y2;
    }
    
    /**
     * Linear interpolation.
     * 
     * @param left first value of interpolation
     * @param right secondxvalue of interpolation
     * @param weight weight of the interpolation
     * @return 
     */
    private double lerp(double left, double right, double weight)
    {
        return left + weight * (right - left);
    }
    
    private double smoothstep(double x)
    {
        return (3.f - 2.f * x) * x * x;
    }

    /**
     * Contructor.
     * 
     * Initializes the fields and generates the gradient vectors.
     * 
     * @param areaWidth width of the noised area, in pixels
     * @param areaHeight height of the noised area, in pixels
     * @param chunkSize side of the chunk
     * @throws Exception if any argument is non-positive
     */
    public PerlinNoise(int areaWidth, int areaHeight, int chunkSize) throws Exception
    {
        if (areaWidth > 0 && areaHeight > 0 && chunkSize > 0)
        {
            this.areaWidth = areaWidth;
            this.areaHeight = areaHeight;
            this.chunkSize = chunkSize;
            
            gradientCols = Math.ceilDiv(areaWidth, chunkSize) + 1;
            gradientRows = Math.ceilDiv(areaHeight, chunkSize) + 1;
            gradientVectors = new ArrayList<>(gradientCols);
            Random random = new Random();
            for (int i = 0; i < gradientCols; ++i)
            {
                var column = new ArrayList<Coords<Double>>(gradientRows);
                for (int j = 0; j < gradientRows; ++j)
                {
                    double angle = random.nextDouble(0.0d, Math.TAU);
                    double xCoord = Math.cos(angle);
                    double yCoord = Math.sin(angle);
                    column.add(new Coords(xCoord, yCoord));
                }
                gradientVectors.add(column);
            }
            octavesCount = 3;
            persistence = 0.5f;
            lacunarity = 2.0f;
        }
        else
        {
            throw new Exception("PerlinNoise.PerlinNoise");
        }
    }
    
    /**
     * Overwrites the octaves count.
     * 
     * The new value shall be positive. It should not be "too big" (a few
     * octaves should be fine).
     * 
     * In case of a non-positive value, an exception is thrown.
     * 
     * @param newValue new octaves count
     * @throws Exception if <code>newValue</code> is non-positive
     */
    public void setOctavesCount(int newValue) throws Exception
    {
        if (newValue > 0)
        {
            octavesCount = newValue;
        }
        else
        {
            throw new Exception("PerlinNoise.setOctavesCount");
        }
    }
    
    public void setPersistence(double newValue) throws Exception
    {
        if (newValue > 0.0d && newValue < 1.0d)
        {
            persistence = newValue;
        }
        else
        {
            throw new Exception("PerlinNoise.setPersistence");
        }
    }
    
    public void setLacunarity(double newValue) throws Exception
    {
        if (newValue > 1.0d)
        {
            lacunarity = newValue;
        }
        else
        {
            throw new Exception("PerlinNoise.setLacunarity");
        }
    }
    
    private double getRawNoise(int pixelX, int pixelY)
    {
        /* Naming */
        // x, y - refer to global position within the area, counted in pixels
        // p, q - refer to local position within the chunk, counted as fractions
        // col, row - refer to indices
        // a, b, c, d - refer to corners of the chunk, respectively:
        //  top left, top right, bottom left, bottom right.
        
        /* Adjust the coords. */
        pixelX %= areaWidth;
        pixelY %= areaHeight;
        if (pixelX < 0)
        {
            pixelX += areaWidth;
        }
        if (pixelY < 0)
        {
            pixelY += areaHeight;
        }
        
        /* Find the chunk's indices. */
        int chunkCol = pixelX / chunkSize;
        int chunkRow = pixelY / chunkSize;
        
        /* Find the coordinates of the corners. */
        int chunkLeftX = chunkCol * chunkSize;
        int chunkTopY = chunkRow * chunkSize;
        int chunkRightX = chunkLeftX + chunkSize;
        int chunkBottomY = chunkTopY + chunkSize;
        
        /* Find the local coordinates of the point. */
        double pixelLeftP = (double) (pixelX - chunkLeftX) / (double) chunkSize;
        double pixelTopQ = (double) (pixelY - chunkTopY) / (double) chunkSize;
        double pixelRightP = (double) (pixelX - chunkRightX) / (double) chunkSize;
        double pixelBottomQ = (double) (pixelY - chunkBottomY) / (double) chunkSize;
        
        /* Find the gradients. */
        var gradientA = gradientVectors.get(chunkCol).get(chunkRow);
        var gradientB = gradientVectors.get(chunkCol + 1).get(chunkRow);
        var gradientC = gradientVectors.get(chunkCol).get(chunkRow + 1);
        var gradientD = gradientVectors.get(chunkCol + 1).get(chunkRow + 1);
        
        /* Calculate the products. */
        double productA = dotProduct(pixelLeftP, pixelTopQ, gradientA.x, gradientA.y);
        double productB = dotProduct(pixelRightP, pixelTopQ, gradientB.x, gradientB.y);
        double productC = dotProduct(pixelLeftP, pixelBottomQ, gradientC.x, gradientC.y);
        double productD = dotProduct(pixelRightP, pixelBottomQ, gradientD.x, gradientD.y);
        
        /* Interpolate. */
        double horizontalTop = lerp(productA, productB, pixelLeftP);
        double horizontalBottom = lerp(productC, productD, pixelLeftP);
        double vertical = lerp(horizontalTop, horizontalBottom, pixelTopQ);

        return vertical / maxRawNoise;
    }
    
    public double getNoise(int pixelX, int pixelY)
    {
        double noise = getRawNoise(pixelX, pixelY);
        double frequency = lacunarity;
        double amplitude = persistence;
        float maxValue = 1.f;
        for (int i = 1; i < octavesCount; ++i)
        {
            int newX = (int) (frequency * pixelX);
            int newY = (int) (frequency * pixelY);
            noise += amplitude * getRawNoise(newX, newY);
            maxValue += amplitude;
            frequency *= lacunarity;
            amplitude *= persistence;
        }
        
        /* Normalize and smoothen. */
        return smoothstep(noise / maxValue);
    }
    
    public boolean $test()
    {
        boolean success = true;
        Random random = new Random();
        for (int i = 0; i < 1000000; ++i)
        {
            int x = random.nextInt(areaWidth);
            int y = random.nextInt(areaHeight);
            double n = getRawNoise(x, y);
            if (n < 0.d || n > 1.d)
            {
                success = false;
                break;
            }
        }
        return success;
    }
}
