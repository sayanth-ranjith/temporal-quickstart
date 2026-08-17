package com.sayanth.ranjith.temporal.shipment_workflow.activity;

import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
public class DiagnosticActivitiesImpl implements DiagnosticActivities {

    @Override
    public String flakyStep(int succeedOnAttempt) {
        // Temporal tracks the attempt number per activity execution, so no
        // external counter/state is needed to simulate flakiness.
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();

        if (attempt < succeedOnAttempt) {
            log.warn("flakyStep attempt #{} failing on purpose (will succeed on attempt {})",
                    attempt, succeedOnAttempt);
            throw new RuntimeException("Simulated transient failure on attempt " + attempt);
        }

        log.info("flakyStep attempt #{} succeeded", attempt);
        return "succeeded on attempt " + attempt;
    }
}
