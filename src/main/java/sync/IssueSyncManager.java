package sync;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.entities.Issue;

/**
 * Support-side synchronization manager.
 *
 * Tracks issue references known to this backend and launches one-shot worker
 * threads to push supporter updates to the client backend through the Gateway.
 *
 * Concurrency-safe by design:
 *  - {@code tracked}: references this backend knows about (registered on incoming creation).
 *  - {@code activeWorkers}: references with an in-flight sync worker, used to avoid
 *    starting two workers for the same reference at the same time.
 */
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

    /**
     * Registers a reference for support-side sync. Called after an incoming issue
     * from the client backend is stored. This does NOT sync back to the client
     * (the change originated there) — it only marks the reference as syncable.
     */
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

    /**
     * Triggers a support -> client sync for an updated issue. Must be called only
     * after the support DB update has succeeded. Sends reference, classification,
     * status and resolutionComment. Skips silently if a worker is already running
     * for the same reference (duplicate protection).
     */
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

    /**
     * Called by a worker when it finishes. Frees the in-flight slot and, when the
     * issue was resolved and the final sync succeeded, stops tracking it.
     */
    void onWorkerFinished(String reference, boolean resolvedAndSynced) {
        activeWorkers.remove(reference);
        if (resolvedAndSynced) {
            tracked.remove(reference);
            System.out.println("[IssueSync] Reference " + reference
                    + " resolved and final sync completed; tracking stopped.");
        }
    }

    /** Stops tracking a reference and clears any in-flight worker slot. */
    public void unregister(String reference) {
        if (reference == null) {
            return;
        }
        activeWorkers.remove(reference);
        tracked.remove(reference);
    }
}
