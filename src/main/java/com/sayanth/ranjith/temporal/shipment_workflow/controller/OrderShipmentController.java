package com.sayanth.ranjith.temporal.shipment_workflow.controller;

import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderDetails;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderStatus;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import com.sayanth.ranjith.temporal.shipment_workflow.workflow.OrderShipmentWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderShipmentController {

    private final WorkflowClient workflowClient;

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderDetails order) {
        String workflowId = workflowId(order.orderId());
        OrderShipmentWorkflow workflow = workflowClient.newWorkflowStub(
                OrderShipmentWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .build());
        WorkflowClient.start(workflow::shipOrder, order);
        return ResponseEntity.accepted().body(workflowId);
    }

    @PostMapping("/{orderId}/received-by-team")
    public ResponseEntity<Void> orderReceivedByTeam(@PathVariable String orderId) {
        stub(orderId).confirmOrderReceived();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/packed-by-team")
    public ResponseEntity<Void> orderPackedByTeam(@PathVariable String orderId) {
        stub(orderId).confirmOrderPacked();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/dispatched")
    public ResponseEntity<Void> orderDispatched(@PathVariable String orderId) {
        stub(orderId).confirmOrderDispatched();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<Void> requestReturn(@PathVariable String orderId, @RequestBody ReturnRequest request) {
        stub(orderId).requestReturn(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatus> getStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(stub(orderId).getStatus());
    }

    private OrderShipmentWorkflow stub(String orderId) {
        return workflowClient.newWorkflowStub(OrderShipmentWorkflow.class, workflowId(orderId));
    }

    private String workflowId(String orderId) {
        return "order-" + orderId;
    }
}
