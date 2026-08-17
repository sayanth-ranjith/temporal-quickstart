package com.sayanth.ranjith.temporal.shipment_workflow.model;

public record OrderShipmentResult(
        String orderId,
        OrderStatus status,
        ReturnResult returnResult) {
}
