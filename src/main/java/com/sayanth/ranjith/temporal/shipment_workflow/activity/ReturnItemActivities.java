package com.sayanth.ranjith.temporal.shipment_workflow.activity;

import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ReturnItemActivities {

    @ActivityMethod
    void pickupOrderItem(ReturnRequest request);

    @ActivityMethod
    double refundMoney(ReturnRequest request);
}
