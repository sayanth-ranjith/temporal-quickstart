package com.sayanth.ranjith.temporal.shipment_workflow.controller;

import com.sayanth.ranjith.temporal.shipment_workflow.config.TemporalConstants;
import com.sayanth.ranjith.temporal.shipment_workflow.workflow.RetryDemoWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowException;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Not part of the order flow - this exists purely to make Temporal's retry
 * behavior visible: tail the app logs (or open the Temporal Web UI at
 * http://localhost:8233 and inspect the workflow's event history) while the
 * request is in flight.
 */
@RestController
@RequestMapping("/api/diagnostics")
@RequiredArgsConstructor
public class RetryDemoController {

    private final WorkflowClient workflowClient;

    /**
     * succeedOnAttempt <= 5 (the activity's max attempts): activity fails a
     * few times, retries with backoff, then succeeds - 200 OK.
     * succeedOnAttempt > 5: retries are exhausted - 500 with the failure.
     * This call blocks until the workflow finishes, so retries happen live.
     */
    @PostMapping("/retry-demo")
    public ResponseEntity<Map<String, Object>> runRetryDemo(
            @RequestParam(defaultValue = "3") int succeedOnAttempt) {
        String workflowId = "retry-demo-" + UUID.randomUUID();
        RetryDemoWorkflow workflow = workflowClient.newWorkflowStub(
                RetryDemoWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalConstants.ORDER_SHIPMENT_TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .build());

        try {
            String result = workflow.runDemo(succeedOnAttempt);
            return ResponseEntity.ok(Map.of("workflowId", workflowId, "result", result));
        } catch (WorkflowException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("workflowId", workflowId, "error", e.getMessage()));
        }
    }
}
