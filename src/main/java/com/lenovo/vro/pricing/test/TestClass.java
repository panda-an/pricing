package com.lenovo.vro.pricing.test;

public class TestClass {

    final static long m = 1l;

    static int n = 2;

    public void desc(){}

    public static void test(String... name) {}

    public int inc() {
        int x;
        try {
            x = 1;
            return x;
        } catch (Exception e) {
            x = 2;
            return x;
        } finally {
            x = 3;
        }
    }

    public static void main(String[] args) {

    }
}
