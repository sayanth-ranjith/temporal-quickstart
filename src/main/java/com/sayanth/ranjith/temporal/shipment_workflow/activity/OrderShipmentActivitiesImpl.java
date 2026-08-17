package com.sayanth.ranjith.temporal.shipment_workflow.activity;

import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderDetails;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Spring-managed activity implementation. Being a regular {@code @Component} means
 * it can have any Spring bean (repositories, REST clients, etc.) injected via the
 * constructor once real integrations replace these log statements.
 */
@Slf4j
@Component
@ActivityImpl(taskQueues = TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
public class OrderShipmentActivitiesImpl implements OrderShipmentActivities {

    @Override
    public void recordOrderPlaced(OrderDetails order) {
        log.info("Order placed: {}", order);
    }

    @Override
    public void markOrderReceivedByTeam(String orderId) {
        log.info("Order {} received by team", orderId);
    }

    @Override
    public void markOrderPackedByTeam(String orderId) {
        log.info("Order {} packed by team", orderId);
    }

    @Override
    public void markOrderDispatched(String orderId) {
        log.info("Order {} dispatched", orderId);
    }

    @Override
    public void markOrderDelivered(String orderId) {
        log.info("Order {} delivered", orderId);
    }
}
