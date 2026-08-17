package com.sayanth.ranjith.temporal.shipment_workflow.model;

public record ReturnRequest(
        String orderId,
        String reason,
        double amount) {
}
