/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.perlin;

/**
 *
 * @author Kay Jay O'Nail
 */
public class Pair<N>
{
    private N x;
    private N y;
    
    public Pair(N x, N y)
    {
        this.x = x;
        this.y = y;
    }
    
    public N getX()
    {
        return x;
    }
    
    public N getY()
    {
        return y;
    }
}
