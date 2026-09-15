package com.sayanth.ranjith.temporal.shipment_workflow.config;

import java.time.Duration;

public final class TemporalConstants {

    /** Single task queue for both the worker and all workflows/activities in this service. */
    public static final String ORDER_SHIPMENT_TASK_QUEUE = "order-shipment-worker";

    /**
     * Hard cap on how long an OrderShipmentWorkflow execution may run end to
     * end. Enforced by the Temporal service itself (via
     * WorkflowOptions#setWorkflowExecutionTimeout), so it's honored even if
     * this worker crashes or is never around to act on it - the run is
     * force-timed-out server-side.
     */
    public static final Duration ORDER_SHIPMENT_WORKFLOW_TIMEOUT = Duration.ofMinutes(15);

    private TemporalConstants() {
    }
}
