package com.sayanth.ranjith.temporal.shipment_workflow.model;

import java.util.List;

public record OrderDetails(
        String orderId,
        String customerId,
        List<String> items,
        double amount) {
}
