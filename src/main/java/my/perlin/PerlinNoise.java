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
    private final ArrayList<ArrayList<Pair<Float>>> gradientVectors;
    
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
    private float persistence;
    
    /**
     * The parameter determining how many times the area is fractally repeated.
     * Shall be greater than <code>1.0</code>. The default value is
     * <code>2.0</code>.
     */
    private float lacunarity;
    
    /**
     * The maximal possible value of the <i>"raw noise"</i> (the sum of the dot
     * products of the offsets times the gradients).<br>
     * <i>The value is mathematically derived, not user-defined.</i>
     */
    private static final float maxRawNoise = (float) (2.0 * Math.sqrt(2.0));
    
    /**
     * The scalar (dot) product of vectors (x1, y1) and (x2, y2).
     * 
     * @param x1 x-coordinate of the first vector
     * @param y1 y-coordinate of the first vector
     * @param x2 x-coordinate of the second vector
     * @param y2 y-coordinate of the second vector
     * @return the product of the vectors
     */
    private float dotProduct(float x1, float y1, float x2, float y2)
    {
        return x1 * x2 + y1 * y2;
    }
    
    /**
     * Linear interpolation.
     * 
     * @param value1 first value of interpolation
     * @param value2 secondxvalue of interpolation
     * @param weight weight of the interpolation
     * @return 
     */
    private float lerp(float value1, float value2, float weight)
    {
        return value1 + weight * (value2 - value1);
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
                var column = new ArrayList<Pair<Float>>(gradientRows);
                for (int j = 0; j < gradientRows; ++j)
                {
                    float angle = random.nextFloat() * (float) Math.TAU;
                    float xCoord = (float) Math.cos(angle);
                    float yCoord = (float) Math.sin(angle);
                    column.add(new Pair(xCoord, yCoord));
                }
                gradientVectors.add(column);
            }
            
            octavesCount = 3;
            persistence = 0.5f;
            lacunarity = 2.0f;
        }
        else
        {
            throw new Exception("PerlinNoice.<constructor>");
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
    
    public void setPersistence(float newValue) throws Exception
    {
        if (newValue > 0.0f && newValue < 1.0f)
        {
            persistence = newValue;
        }
        else
        {
            throw new Exception("PerlinNoise.setPersistence");
        }
    }
    
    public void setLacunarity(float newValue) throws Exception
    {
        if (newValue > 1.0f)
        {
            lacunarity = newValue;
        }
        else
        {
            throw new Exception("PerlinNoise.setLacunarity");
        }
    }
    
    public float getNoise(int pixelGlobalX, int pixelGlobalY)
    {
        /* Naming */
        /* x, y - refer to position counted in pixels
         * col, row - refer to indices
         */
        
        /* CLASS PAIR MIGHT BE USEFUL! */
        
        /* Adjust the coords. */
        pixelGlobalX %= areaWidth;
        pixelGlobalY %= areaHeight;
        if (pixelGlobalX < 0)
        {
            pixelGlobalX += areaWidth;
        }
        if (pixelGlobalY < 0)
        {
            pixelGlobalY += areaHeight;
        }
        
        /* Find the chunk's indices. */
        int chunkCol = pixelGlobalX % chunkSize;
        int chunkRow = pixelGlobalY % chunkSize;
        
        /* Global positions of chunk's sides. */
        int chunkGlobalLeftX = chunkCol * chunkSize;
        int chunkGlobalRightX = chunkGlobalLeftX + chunkSize;
        int chunkGlobalTopY = chunkRow * chunkSize;
        int chunkGlobakBottomY = chunkGlobalTopY + chunkSize;
        
        /* Pixel's position within the chunk, in pixels. */
        int pixelLocalX = pixelGlobalX - chunkGlobalLeftX;
        int pixelLocalY = pixelGlobalY - chunkGlobalTopY;
        
        /* Pixel's position within the chunk, as fraction from range [0, 1). */
        float pixelFractionalLocalX = (float) pixelLocalX / (float) chunkSize;
        float pixelFractionalLocalY = (float) pixelLocalY / (float) chunkSize;
        
        /* Offsets, as fractions */
        float topLeftOffsetX = pixelFractionalLocalX;
        float topLeftOffsetY = pixelFractionalLocalY;
        float topRightOffsetX = (float) chunkSize - pixelFractionalLocalX;
        float topRightOffsetY = pixelFractionalLocalY;
        float bottomLeftOffsetX = pixelFractionalLocalX;
        float bottomLeftOffsetY = (float) chunkSize - pixelFractionalLocalY;
        float bottomRightOffsetX = topRightOffsetX;
        float bottomRightOffsetY = bottomLeftOffsetY;
        
        
        
        return 0;
    }
}
