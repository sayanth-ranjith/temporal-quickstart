package com.sayanth.ranjith.temporal.shipment_workflow.workflow;

import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderDetails;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderShipmentResult;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderStatus;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Order placed -> (wait for callback) -> received by team -> (wait for callback) ->
 * packed by team -> (wait for callback) -> dispatched -> delivered. A 15 second
 * window after delivery allows a return to be requested, which spins up
 * {@link ReturnItemWorkflow} as a child workflow.
 */
@WorkflowInterface
public interface OrderShipmentWorkflow {

    @WorkflowMethod
    OrderShipmentResult shipOrder(OrderDetails order);

    /** Callback signal: team has received the order. */
    @SignalMethod
    void confirmOrderReceived();

    /** Callback signal: team has packed the order. */
    @SignalMethod
    void confirmOrderPacked();

    /** Callback signal: order has been dispatched. */
    @SignalMethod
    void confirmOrderDispatched();

    /** Only honored during the 15 second window right after delivery. */
    @SignalMethod
    void requestReturn(ReturnRequest returnRequest);

    @QueryMethod
    OrderStatus getStatus();
}
