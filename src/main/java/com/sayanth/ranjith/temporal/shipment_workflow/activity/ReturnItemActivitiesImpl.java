package com.sayanth.ranjith.temporal.shipment_workflow.activity;

import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
public class ReturnItemActivitiesImpl implements ReturnItemActivities {

    @Override
    public void pickupOrderItem(ReturnRequest request) {
        log.info("Picking up returned item for order {}: reason={}", request.orderId(), request.reason());
    }

    @Override
    public double refundMoney(ReturnRequest request) {
        log.info("Refunding {} for order {}", request.amount(), request.orderId());
        return request.amount();
    }
}
