package com.sayanth.ranjith.temporal.shipment_workflow.workflow;

import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnResult;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Child workflow started by {@link OrderShipmentWorkflow} when a customer requests a
 * return within the post-delivery window. Two steps: item picked up, money refunded.
 */
@WorkflowInterface
public interface ReturnItemWorkflow {

    @WorkflowMethod
    ReturnResult processReturn(ReturnRequest request);
}
