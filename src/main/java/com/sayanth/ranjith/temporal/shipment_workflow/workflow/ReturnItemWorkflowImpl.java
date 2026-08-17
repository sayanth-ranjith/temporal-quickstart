package com.sayanth.ranjith.temporal.shipment_workflow.workflow;

import com.sayanth.ranjith.temporal.shipment_workflow.activity.ReturnItemActivities;
import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnRequest;
import com.sayanth.ranjith.temporal.shipment_workflow.model.ReturnResult;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Workflow implementations are instantiated by the Temporal SDK (one instance per
 * workflow execution), not by Spring, so this class stays a plain POJO - no
 * {@code @Component} and no injected mutable state.
 */
@WorkflowImpl(taskQueues = TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
public class ReturnItemWorkflowImpl implements ReturnItemWorkflow {

    private final ReturnItemActivities activities = Workflow.newActivityStub(
            ReturnItemActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2.0)
                            .setMaximumInterval(Duration.ofSeconds(10))
                            .setMaximumAttempts(5)
                            .build())
                    .build());

    @Override
    public ReturnResult processReturn(ReturnRequest request) {
        activities.pickupOrderItem(request);
        double refundedAmount = activities.refundMoney(request);
        return new ReturnResult(request.orderId(), "REFUNDED", refundedAmount);
    }
}
