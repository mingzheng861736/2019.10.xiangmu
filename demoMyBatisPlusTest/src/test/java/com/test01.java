package com;

import org.junit.Test;

import java.util.Date;


public class test01 {

    @Test
    public void al() {
        Date a = new Date();
        System.out.println(a.getTime());
        System.out.println(System.currentTimeMillis());

    }
}
