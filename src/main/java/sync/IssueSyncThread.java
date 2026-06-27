package sync;

import model.entities.Issue;

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
            //se actualiza el hilo cuando el tiquete termina
            manager.onWorkerFinished(reference, resolved && success);
        }
    }
}
