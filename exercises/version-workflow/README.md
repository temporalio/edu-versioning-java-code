# Exercise #1: Version the Change with the `getVersion` API

During this exercise, you will

- Run a Workflow Execution that completes successfully
- Make and deploy a change that does not affect compatibility
- Make and deploy a change that breaks compatibility, causing a non-deterministic error
- Develop an automated test to check compatibility with previous executions
- Use the `getVersion` API to implement versioning for the Workflow

Make your changes to the code in the `practice` subdirectory (look for
`TODO` comments that will guide you to where you should make changes to
the code). If you need a hint or want to verify your changes, look at
the complete version in the `solution` subdirectory.

## Part A: Run a Workflow to Completion

1. Change directories to the `exercises/version-workflow/practice` directory.
1. Run `mvn clean compile exec:java -Dexec.mainClass="getversion.LoanProcessingWorker"`
   in a terminal to start a Worker
1. Run `mvn clean compile exec:java -Dexec.mainClass="getversion.Starter" -Dexec.args="a100"`
   in another terminal. This will start a Workflow that processes the loan for
   customer ID `a100`.
1. Let this Workflow run to completion. This customer has a loan
   with 10 payments, and since the Workflow in this exercise uses
   a Timer to add a three-second delay between each payment, it
   should complete within 30 seconds.
1. You will now download the history of this execution in JSON
   format so that you can replay it in an automated test that
   you will develop later in this exercise. Use the `temporal` cli to
   download the event history:
   ```bash
   temporal workflow show \
      --workflow-id loan-processing-workflow-customer-a100 \
      --output json > history_for_original_execution.json
   ```
   - You an also use the `ex1h` command within the GitPod environment to save yourself some typing.
   - **NOTE** You can also download the event history from the Web UI, however, this
     is currently not supported in the GitPod environment. To download the
     history navigate to the **Event History** section
   of the detail page for this execution, and then click the
   **Download** button just above the table showing the Event History.
   In the **Download JSON** dialog, leave the
   **Encoded** options as is, and then click **Download**. Save
   the file as `history_for_original_execution.json` in your
   `practice` directory.
1. In the next section, you will make and deploy an incompatible
   change, causing a non-deterministic error for an open execution.
   To allow time for you to do these things, edit the `LoanProcessingWorkflowImpl.java`
   file and change the duration in the `Workflow.sleep` call from
   3 seconds to 90 seconds.
1. Save your change to the `LoanProcessingWorkflowImpl.java` file and exit the editor
1. Compile the code with `mvn clean compile`
1. Restart the Worker by pressing Ctrl-C in the terminal window
   from step 1 and running the `mvn clean compile exec:java -Dexec.mainClass="getversion.LoanProcessingWorker"` command again
1. Run the Workflow again: `mvn clean compile exec:java -Dexec.mainClass="getversion.Starter" -Dexec.args="a100"`
1. Use the Web UI to verify that the Workflow Execution from the
   previous step is running before proceeding with the next part
   of this exercise.

## Part B: Deploy an Incompatible Change (without Versioning)

1. This Workflow uses the `sendThankYouToCustomer` Activity to
   send a thank you message to the customer before charging
   them with the first loan payment, but this was a mistake.
   This Activity should run after the last payment. To fix this, edit the `LoanProcessingWorkflowImpl.java`
   file and move the line of code used start to Activity execution
   `String confirmation = activities.sendThankYouToCustomer(info);` from just
   before the loop to just after the loop.
1. Save your change and exit the editor.
1. Stop the Worker by pressing Ctrl-C in the terminal window where you stasrted it.
1. Compile the code with `mvn clean compile`
1. Restart the Worker by then running the
   `mvn clean compile exec:java -Dexec.mainClass="getversion.LoanProcessingWorker"` command again.
1. The change you just made to the Workflow logic takes effect immediately, although
   the Worker immediately begins using the updated code you
   deployed, it may take up to 90 seconds before that is
   evident for this Workflow Execution, due to the duration of
   the Timer.
1. Refresh the detail page for this execution in the Web UI.
   Continue to refresh the page until the non-deterministic
   error is visible.

The non-deterministic error occurs because of your change to the
Workflow logic. By moving the Activity from before the loop to after
it, the sequence of Commands generated during execution is different
with the new code than it was prior to the change.

Recall that you had an open Workflow Execution when you restarted the
Worker during the deployment. The Worker used History Replay to
recover the state of the open execution prior to the restart. Since
the Commands generated when replaying it with the new code did not
correspond to the Events that were generated when the Worker ran the
original code before the restart, it is unable to recover the state
and responds by throwing the non-deterministic error you see.

## Part C: Use the Workflow Replayer to Test Compatibility

1. Edit the `LoanProcessingWorkflowTest.java` file and implement the following
   in the `TestReplayWorkflowHistoryFromFile` function:
   - Create a File object for the Event History you downloaded
   - Use `assertDoesNotThrow` to verify that replaying the history
     does not return an error
     - Create the Workflow Replayer and call it from within the assert
     - Replay the Event History in the JSON file you downloaded
2. Save your changes
3. Run `mvn clean compile test`. You should find that this fails, which confirms
   altering the execution order of the `sendThankYouToCustomer`
   Activity) breaks compatibility. In the final part of this
   exercise, you will use the `getVersion` API to implement
   versioning for your change, thereby making it compatible
   with Workflow Executions started before or after the change.

## Part D: Version the Change with the `getVersion` API

Just above the loop, where the Activity call was prior to
the change, add the following lines:

```java
String versionKey = "MovedThankYouAfterLoop";
int version = Workflow.getVersion(versionKey, Workflow.DEFAULT_VERSION, 1);

if (version != Workflow.DEFAULT_VERSION) {
Workflow.upsertTypedSearchAttributes(TEMPORAL_CHANGE_VERSION
      .valueSet(Arrays.asList((versionKey + "-" + version))));
}
```

This establishes a logical branch for code execution, identified
by the user-defined Change ID `MovedThankYouAfterLoop`. Since there
was no versioning in place prior to this change, the minimum supported
version is `workflow.DEFAULT_VERSION` and the maximum supported version
is `1`.

1. Add a conditional statement just after this new line: If the value
   of `version` is equal to `@Workflow.DEFAULT_VERSION`, meaning that it
   represents a Workflow Execution started when the Activity was called
   before the loop, then invoke the Activity call there. In other
   words, copy the same lines you moved after the loop to inside the
   braces for this conditional statement, so that this Activity will be
   called if the condition evaluates to `true`.
1. Wrap the code you previously moved after the loop in a
   conditional statement that tests if `version` is equal to
   `1`. This will handle the Activity for Workflow
   Executions started after the change.
1. Change the duration of the `Workflow.sleep` statement at the
   bottom of the loop back to 3 seconds. This is unrelated to
   versioning and changing the duration of a timer does not require versioning,
   but will help you see the results more quickly.
1. Run `mvn clean compile test` again. You should find it succeeds this time,
   since you've used the `getVersion` API to restore compatibility with
   the previous execution.
1. Restart the Worker by pressing Ctrl-C in the terminal
   window where you started it and then running the `mvn clean compile exec:java -Dexec.mainClass="getversion.LoanProcessingWorker"` command again.
1. Return to the detail page for this Workflow Execution
1. Click the downward-facing arrow to the right of the
   **Request Cancellation** menu near the upper-right portion of
   the page and select the **Reset** option.
   - Select the last **WorkflowTaskCompleted** event to reset to
   - Enter "Using versioning to fix a bad deployment" as the reason
   - Click the **Confirm** button
1. The Workflow will now be terminated a new Workflow started, resuming from
   the specified Workflow Task. Return to the dashboard and view progress of
   that Workflow Execution.
   1. The WebUI may still show a warning about **Reset Workflow** at the top of
      the page. Monitor the Event History and the log output from the Worker to
      confirm that progress has resumed.
1. Enable the auto-refresh feature using the toggle button near
   the top of the page. You should find that the Workflow Execution
   completes successfully within the next 30 seconds.

### This is the end of the exercise.
