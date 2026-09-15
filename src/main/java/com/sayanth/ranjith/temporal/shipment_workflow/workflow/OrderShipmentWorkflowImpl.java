package com.sayanth.ranjith.temporal.shipment_workflow.workflow;

import com.sayanth.ranjith.temporal.shipment_workflow.activity.OrderShipmentActivities;
import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderDetails;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderShipmentResult;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderStatus;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnResult;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

@WorkflowImpl(taskQueues = TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
public class OrderShipmentWorkflowImpl implements OrderShipmentWorkflow {

    private static final Logger log = Workflow.getLogger(OrderShipmentWorkflowImpl.class);
    private static final Duration RETURN_WINDOW = Duration.ofSeconds(15);

    // Deliberate pause between a stage-confirmation signal and the activity
    // that acts on it - gives a visible, demonstrable gap in the event
    // history/logs instead of the next activity firing instantly.
    private static final Duration SIGNAL_PROCESSING_DELAY = Duration.ofSeconds(10);

    private final OrderShipmentActivities activities = Workflow.newActivityStub(
            OrderShipmentActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2.0)
                            .setMaximumInterval(Duration.ofSeconds(10))
                            .setMaximumAttempts(5)
                            .build())
                    .build());

    private OrderStatus status = OrderStatus.ORDER_PLACED;

    // Signal-driven state. Workflow code is single-threaded and deterministic,
    // so plain fields (no volatile/locks) are the correct way to hold this.
    private boolean receivedByTeam = false;
    private boolean packedByTeam = false;
    private boolean dispatched = false;
    private boolean returnWindowOpen = false;
    private ReturnRequest pendingReturnRequest;

    @Override
    public OrderShipmentResult shipOrder(OrderDetails order) {
        activities.recordOrderPlaced(order);
        status = OrderStatus.ORDER_PLACED;

        Workflow.await(() -> receivedByTeam);
        Workflow.sleep(SIGNAL_PROCESSING_DELAY);
        activities.markOrderReceivedByTeam(order.orderId());
        status = OrderStatus.ORDER_RECEIVED_BY_TEAM;

        Workflow.await(() -> packedByTeam);
        Workflow.sleep(SIGNAL_PROCESSING_DELAY);
        activities.markOrderPackedByTeam(order.orderId());
        status = OrderStatus.ORDER_PACKED_BY_TEAM;

        Workflow.await(() -> dispatched);
        Workflow.sleep(SIGNAL_PROCESSING_DELAY);
        activities.markOrderDispatched(order.orderId());
        status = OrderStatus.ORDER_DISPATCHED;

        activities.markOrderDelivered(order.orderId());
        status = OrderStatus.ORDER_DELIVERED;

        return waitForPossibleReturn(order);
    }

    private OrderShipmentResult waitForPossibleReturn(OrderDetails order) {
        returnWindowOpen = true;
        Workflow.await(RETURN_WINDOW, () -> pendingReturnRequest != null);
        returnWindowOpen = false;

        if (pendingReturnRequest == null) {
            return new OrderShipmentResult(order.orderId(), status, null);
        }

        status = OrderStatus.RETURN_INITIATED;
        log.info("Return requested for order {}, starting ReturnItemWorkflow", order.orderId());

        ReturnItemWorkflow returnWorkflow = Workflow.newChildWorkflowStub(
                ReturnItemWorkflow.class,
                ChildWorkflowOptions.newBuilder()
                        .setWorkflowId("return-" + order.orderId())
                        .setTaskQueue(TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
                        .build());
        ReturnResult returnResult = returnWorkflow.processReturn(pendingReturnRequest);

        status = OrderStatus.RETURN_COMPLETED;
        return new OrderShipmentResult(order.orderId(), status, returnResult);
    }

    @Override
    public void confirmOrderReceived() {
        receivedByTeam = true;
    }

    @Override
    public void confirmOrderPacked() {
        packedByTeam = true;
    }

    @Override
    public void confirmOrderDispatched() {
        dispatched = true;
    }

    @Override
    public void requestReturn(ReturnRequest returnRequest) {
        if (returnWindowOpen && pendingReturnRequest == null) {
            pendingReturnRequest = returnRequest;
        } else {
            log.warn("Ignoring return request for order {}: return window is closed", returnRequest.orderId());
        }
    }

    @Override
    public OrderStatus getStatus() {
        return status;
    }
}
