/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kuboschek.jsonprocessing;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import me.kuboschek.jsonprocessing.Quantile;

/**
 *
 * @author rubin
 */
public class QuantileTest {
    @Test
    public void test1() {
        Quantile quantile = new Quantile();
        
        assertEquals(quantile.getMax(), 0);
        assertEquals(quantile.getMin(), 0);
        assertEquals(quantile.getSize(), 0);
        assertEquals(quantile.getSum(), 0);
        assertEquals(quantile.getMedian(), 0);
        assertEquals(quantile.getFirstQuantile(), 0);
        assertEquals(quantile.getThirdQuantile(), 0);
    }
    
    @Test
    public void test2() {
        Quantile quantile = new Quantile();
        quantile.addNumber(50);
        
        assertEquals(quantile.getMax(), 50);
        assertEquals(quantile.getMin(), 50);
        assertEquals(quantile.getSize(), 1);
        assertEquals(quantile.getSum(), 50);
        assertEquals(quantile.getMedian(), 50);
        assertEquals(quantile.getFirstQuantile(), 0);
        assertEquals(quantile.getThirdQuantile(), 0);
    }
    
    @Test
    public void test3() {
        Quantile quantile = new Quantile();
        quantile.addNumber(50);
        quantile.addNumber(100);
        quantile.addNumber(20);
        quantile.addNumber(10);
        quantile.addNumber(25);
        
        assertEquals(quantile.getMax(), 100);
        assertEquals(quantile.getMin(), 10);
        assertEquals(quantile.getSize(), 5);
        assertEquals(quantile.getSum(), 205);
        assertEquals(quantile.getMedian(), 25);
        assertEquals(quantile.getFirstQuantile(), 20);
        assertEquals(quantile.getThirdQuantile(), 50);
    }
    
    @Test
    public void test4() {
        Quantile quantile = new Quantile();
        quantile.addNumber(50);
        quantile.addNumber(100);
        quantile.addNumber(20);
        quantile.addNumber(5);
        quantile.addNumber(25);
        quantile.addNumber(70);
        
        assertEquals(quantile.getMax(), 100);
        assertEquals(quantile.getMin(), 5);
        assertEquals(quantile.getSize(), 6);
        assertEquals(quantile.getSum(), 270);
        assertEquals(quantile.getMedian(), 37);
        assertEquals(quantile.getFirstQuantile(), 20);
        assertEquals(quantile.getThirdQuantile(), 70);
    }
    
    @Test
    public void test5() {
        Quantile quantile = new Quantile();
        quantile.addNumber(50);
        quantile.addNumber(100);
        
        assertEquals(quantile.getMax(), 100);
        assertEquals(quantile.getMin(), 50);
        assertEquals(quantile.getSize(), 2);
        assertEquals(quantile.getSum(), 150);
        assertEquals(quantile.getMedian(), 75);
        assertEquals(quantile.getFirstQuantile(), 50);
        assertEquals(quantile.getThirdQuantile(), 100);
    }
}
