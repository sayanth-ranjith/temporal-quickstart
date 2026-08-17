package com.sayanth.ranjith.temporal.shipment_workflow.workflow;

import com.sayanth.ranjith.temporal.shipment_workflow.activity.DiagnosticActivities;
import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(taskQueues = TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
public class RetryDemoWorkflowImpl implements RetryDemoWorkflow {

    // Capped at 5 attempts so a permanent-failure demo fails fast instead of
    // retrying forever - this is what makes the "visible failure" case possible.
    private final DiagnosticActivities activities = Workflow.newActivityStub(
            DiagnosticActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2.0)
                            .setMaximumInterval(Duration.ofSeconds(5))
                            .setMaximumAttempts(5)
                            .build())
                    .build());

    @Override
    public String runDemo(int succeedOnAttempt) {
        return activities.flakyStep(succeedOnAttempt);
    }
}
