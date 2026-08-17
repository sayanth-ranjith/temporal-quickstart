package com.sayanth.ranjith.temporal.shipment_workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface RetryDemoWorkflow {

    @WorkflowMethod
    String runDemo(int succeedOnAttempt);
}
