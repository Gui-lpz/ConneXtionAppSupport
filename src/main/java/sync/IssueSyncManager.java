package sync;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.entities.Issue;


public class IssueSyncManager {

    private static final IssueSyncManager INSTANCE = new IssueSyncManager();

    public static IssueSyncManager getInstance() {
        return INSTANCE;
    }

    private final Set<String> tracked = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, IssueSyncThread> activeWorkers = new ConcurrentHashMap<>();
    private final IssueSyncClient client = new IssueSyncClient();

    private IssueSyncManager() {
    }

 //marca la ref del tiquete
    public void register(String reference) {
        if (reference == null || reference.isBlank()) {
            return;
        }
        tracked.add(reference);
        System.out.println("[IssueSync] Registered reference " + reference
                + " (no immediate sync-back to client).");
    }

    public boolean isRegistered(String reference) {
        return reference != null && tracked.contains(reference);
    }

    //crea el hilo para trabajar con un tiquete
    public void triggerSync(Issue issue) {
        if (issue == null) {
            return;
        }
        String reference = issue.getReference();
        if (reference == null || reference.isBlank()) {
            System.err.println("[IssueSync] Cannot sync an issue without reference; skipping.");
            return;
        }
        tracked.add(reference);

        IssueSyncThread worker = new IssueSyncThread(this, client, issue);
        IssueSyncThread existing = activeWorkers.putIfAbsent(reference, worker);
        if (existing != null) {
            System.err.println("[IssueSync] Sync already in progress for reference "
                    + reference + "; skipping duplicate worker.");
            return;
        }
        worker.start();
    }

    //llamada que funciona cuando un tiquete termina
    void onWorkerFinished(String reference, boolean resolvedAndSynced) {
        activeWorkers.remove(reference);
        if (resolvedAndSynced) {
            tracked.remove(reference);
            System.out.println("[IssueSync] Reference " + reference
                    + " resolved and final sync completed; tracking stopped.");
        }
    }

    public void unregister(String reference) {
        if (reference == null) {
            return;
        }
        activeWorkers.remove(reference);
        tracked.remove(reference);
    }
}
