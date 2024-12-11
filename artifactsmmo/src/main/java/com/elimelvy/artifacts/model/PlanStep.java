package com.elimelvy.artifacts.model;

import java.util.concurrent.Semaphore;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;

public class PlanStep {
    public final PlanAction action;
    public final String code;
    public final int quantity;
    public final String description;
    private final Semaphore waitForLock;

    public PlanStep(PlanAction action, String code, int quantity, String description) {
        this.action = action;
        this.code = code;
        this.quantity = quantity;
        this.description = description;
        this.waitForLock = new Semaphore(0);
    }

    public void completeStep() {
        this.waitForLock.release();
    }

    public void waitForCompletion() throws InterruptedException {
        this.waitForLock.acquire();
    }

    @Override
    public String toString() {
        return "PlanStep [action=" + action + ", code=" + code + ", quantity=" + quantity + ", description="
                + description + "]";
    }

    
}
