package sync;

import model.entities.Issue;

/**
 * One-shot worker that pushes a single support issue update to the client
 * backend through the Gateway. Runs as a daemon thread so it never blocks
 * Tomcat shutdown. When the issue is resolved and the final sync succeeds,
 * it asks the manager to stop tracking the reference.
 */
public class IssueSyncThread extends Thread {

    private final IssueSyncManager manager;
    private final IssueSyncClient client;
    private final Issue issue;

    public IssueSyncThread(IssueSyncManager manager, IssueSyncClient client, Issue issue) {
        super("IssueSync-" + issue.getReference());
        this.manager = manager;
        this.client = client;
        this.issue = issue;
        setDaemon(true);
    }

    @Override
    public void run() {
        String reference = issue.getReference();
        boolean resolved = issue.isResolved();
        boolean success = false;
        try {
            success = client.sendClientUpdate(
                    reference,
                    issue.getClassification(),
                    issue.getStatus(),
                    issue.getResolutionComment());
        } finally {
            // Free the in-flight slot; if the issue is resolved and the final
            // sync succeeded, the reference is unregistered completely.
            manager.onWorkerFinished(reference, resolved && success);
        }
    }
}
