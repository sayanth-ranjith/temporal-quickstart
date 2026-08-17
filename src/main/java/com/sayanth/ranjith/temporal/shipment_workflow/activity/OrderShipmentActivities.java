package com.sayanth.ranjith.temporal.shipment_workflow.activity;

import com.sayanth.ranjith.temporal.shipment_workflow.model.OrderDetails;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderShipmentActivities {

    @ActivityMethod
    void recordOrderPlaced(OrderDetails order);

    @ActivityMethod
    void markOrderReceivedByTeam(String orderId);

    @ActivityMethod
    void markOrderPackedByTeam(String orderId);

    @ActivityMethod
    void markOrderDispatched(String orderId);

    @ActivityMethod
    void markOrderDelivered(String orderId);
}
