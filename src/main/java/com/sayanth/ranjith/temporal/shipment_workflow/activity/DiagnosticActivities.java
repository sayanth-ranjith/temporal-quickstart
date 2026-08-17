package com.sayanth.ranjith.temporal.shipment_workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DiagnosticActivities {

    /**
     * Fails on every attempt before {@code succeedOnAttempt}, then succeeds.
     * Pass a value larger than the workflow's configured max attempts to
     * observe retries being exhausted and the activity failing permanently.
     */
    @ActivityMethod
    String flakyStep(int succeedOnAttempt);
}
