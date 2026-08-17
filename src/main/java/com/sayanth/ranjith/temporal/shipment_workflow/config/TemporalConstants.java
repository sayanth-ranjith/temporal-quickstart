package com.sayanth.ranjith.temporal.shipment_workflow.config;

public final class TemporalConstants {

    /** Single task queue for both the worker and all workflows/activities in this service. */
    public static final String ORDER_SHIPMENT_TASK_QUEUE = "order-shipment-worker";

    private TemporalConstants() {
    }
}
