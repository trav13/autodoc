// @Author Delvison Castillo

package gov.nasa.cassini;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages multiple directory watchers for profile and mhdocuments
 */
public class WatcherManager {
    private List<DirectoryWatcher> watchers = new ArrayList<>();
    private static WatcherManager instance;
    
    private WatcherManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized WatcherManager getInstance() {
        if (instance == null) {
            instance = new WatcherManager();
        }
        return instance;
    }
    
    /**
     * Start watching directories
     */
    public void startWatching() {
        try {
            System.out.println("\n===========================================");
            System.out.println("Starting Directory Watchers...");
            System.out.println("===========================================\n");
            
            // Create watcher for profile directory
            DirectoryWatcher profileWatcher = new DirectoryWatcher(
                "profile/input",
                "profile/output"
            );
            profileWatcher.start();
            watchers.add(profileWatcher);
            
            // Create watcher for mhdocuments directory
            DirectoryWatcher mhdocumentsWatcher = new DirectoryWatcher(
                "mhdocuments/input",
                "mhdocuments/output"
            );
            mhdocumentsWatcher.start();
            watchers.add(mhdocumentsWatcher);
            
            System.out.println("\nDirectory watchers are now active.");
            System.out.println("Drop text files (.txt) and template files (.docx, .pptx, .xlsx)");
            System.out.println("with matching names into:");
            System.out.println("  - profile/input/");
            System.out.println("  - mhdocuments/input/");
            System.out.println("\nGenerated documents will appear in:");
            System.out.println("  - profile/output/");
            System.out.println("  - mhdocuments/output/");
            System.out.println("\nPress Ctrl+C to stop watching.\n");
            
        } catch (IOException e) {
            System.err.println("Error starting directory watchers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Stop all watchers
     */
    public void stopWatching() {
        System.out.println("\nStopping directory watchers...");
        for (DirectoryWatcher watcher : watchers) {
            watcher.stopWatching();
        }
        watchers.clear();
        System.out.println("All directory watchers stopped.");
    }
    
    /**
     * Wait for all watcher threads to complete
     */
    public void waitForWatchers() {
        for (DirectoryWatcher watcher : watchers) {
            try {
                watcher.join();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for watcher: " + e.getMessage());
            }
        }
    }
}
