package com.elimelvy.artifacts;

public class FakeBank extends Bank {


    @Override
    public int getBankQuantity(String code) {
        return Integer.MAX_VALUE;
    }

}
