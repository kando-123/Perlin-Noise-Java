/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.perlin;

/**
 *
 * @author Kay Jay O'Nail
 * @param <N>
 */
public class Point<N extends Number>
{
    public N x;
    public N y;
    
    public Point(N x, N y)
    {
        this.x = x;
        this.y = y;
    }
}
