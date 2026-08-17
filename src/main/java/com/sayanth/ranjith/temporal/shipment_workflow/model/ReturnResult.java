package com.sayanth.ranjith.temporal.shipment_workflow.model;

public record ReturnResult(
        String orderId,
        String status,
        double refundedAmount) {
}
