package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.List;

import com.elimelvy.artifacts.model.InventoryItem;

public class FakeBank extends Bank {


    @Override
    public int getBankQuantity(String code) {
        return Integer.MAX_VALUE;
    }

}
