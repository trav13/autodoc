// @Author Delvison Castillo

package gov.nasa.cassini;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Watches directories for file drops and automatically generates documents
 * when both text files and template files are present.
 */
public class DirectoryWatcher extends Thread {
    private final Path watchDirectory;
    private final Path outputDirectory;
    private WatchService watchService;
    private volatile boolean running = true;
    private Map<String, FileInfo> textFiles = new ConcurrentHashMap<>();
    private Map<String, FileInfo> templateFiles = new ConcurrentHashMap<>();
    
    // Delay before processing (to ensure file is fully written)
    private static final long PROCESSING_DELAY_MS = 2000;
    
    // ProcessThread configuration constants
    private static final boolean TERMINAL_MODE = true;
    private static final boolean AUTO_OPEN = false;
    
    private static class FileInfo {
        String path;
        long lastModified;
        
        FileInfo(String path, long lastModified) {
            this.path = path;
            this.lastModified = lastModified;
        }
    }
    
    public DirectoryWatcher(String watchDir, String outputDir) throws IOException {
        this.watchDirectory = Paths.get(watchDir);
        this.outputDirectory = Paths.get(outputDir);
        
        // Create directories if they don't exist
        Files.createDirectories(this.watchDirectory);
        Files.createDirectories(this.outputDirectory);
        
        // Initialize watch service
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchDirectory.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY
        );
        
        System.out.println("DirectoryWatcher initialized for: " + watchDirectory);
        System.out.println("Output directory: " + outputDirectory);
        
        // Scan existing files
        scanExistingFiles();
    }
    
    /**
     * Scan for existing files in the directory
     */
    private void scanExistingFiles() {
        File dir = watchDirectory.toFile();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    categorizeFile(file);
                }
            }
        }
        // Try to match and process existing files
        tryMatchAndProcess();
    }
    
    /**
     * Categorize a file as text or template
     */
    private void categorizeFile(File file) {
        String fileName = file.getName().toLowerCase();
        String path = file.getAbsolutePath();
        long lastModified = file.lastModified();
        
        if (fileName.endsWith(".txt")) {
            String baseName = getBaseName(fileName);
            textFiles.put(baseName, new FileInfo(path, lastModified));
            System.out.println("Found text file: " + fileName);
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".pptx") || 
                   fileName.endsWith(".xlsx")) {
            String baseName = getBaseName(fileName);
            templateFiles.put(baseName, new FileInfo(path, lastModified));
            System.out.println("Found template file: " + fileName);
        }
    }
    
    /**
     * Get base name without extension
     */
    private String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
    
    /**
     * Try to match text files with templates and process them
     */
    private void tryMatchAndProcess() {
        for (String baseName : textFiles.keySet()) {
            if (templateFiles.containsKey(baseName)) {
                FileInfo textInfo = textFiles.get(baseName);
                FileInfo templateInfo = templateFiles.get(baseName);
                
                // Check if files are stable (not currently being written)
                File textFile = new File(textInfo.path);
                File templateFile = new File(templateInfo.path);
                
                if (textFile.exists() && templateFile.exists()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - textInfo.lastModified > PROCESSING_DELAY_MS &&
                        currentTime - templateInfo.lastModified > PROCESSING_DELAY_MS) {
                        processFiles(textInfo.path, templateInfo.path, baseName);
                        // Remove from maps after processing
                        textFiles.remove(baseName);
                        templateFiles.remove(baseName);
                    }
                }
            }
        }
    }
    
    /**
     * Process text and template files to generate output document
     */
    private void processFiles(String textFilePath, String templateFilePath, String baseName) {
        try {
            System.out.println("\n===========================================");
            System.out.println("Processing files:");
            System.out.println("  Text: " + textFilePath);
            System.out.println("  Template: " + templateFilePath);
            
            // Determine output path
            String extension = templateFilePath.substring(templateFilePath.lastIndexOf('.'));
            String outputPath = outputDirectory.resolve(baseName).toString();
            
            System.out.println("  Output: " + outputPath + extension);
            System.out.println("===========================================\n");
            
            // Use ProcessThread to generate document
            ProcessThread pt = new ProcessThread(
                textFilePath, 
                templateFilePath, 
                outputPath,
                TERMINAL_MODE,
                AUTO_OPEN
            );
            pt.run();
            
            System.out.println("Document generated successfully!");
            
            // Move processed files to a 'processed' subdirectory
            moveProcessedFiles(textFilePath, templateFilePath);
            
        } catch (Exception e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Move processed files to a 'processed' subdirectory
     */
    private void moveProcessedFiles(String textFilePath, String templateFilePath) {
        try {
            Path processedDir = watchDirectory.resolve("processed");
            Files.createDirectories(processedDir);
            
            // Move text file
            File textFile = new File(textFilePath);
            if (textFile.exists()) {
                Path targetText = processedDir.resolve(textFile.getName());
                Files.move(textFile.toPath(), targetText, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Move template file
            File templateFile = new File(templateFilePath);
            if (templateFile.exists()) {
                Path targetTemplate = processedDir.resolve(templateFile.getName());
                Files.move(templateFile.toPath(), targetTemplate, StandardCopyOption.REPLACE_EXISTING);
            }
            
            System.out.println("Processed files moved to: " + processedDir);
        } catch (IOException e) {
            System.err.println("Warning: Could not move processed files: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        System.out.println("DirectoryWatcher thread started for: " + watchDirectory);
        
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                
                if (key == null) {
                    // No events, check for matches periodically
                    tryMatchAndProcess();
                    continue;
                }
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path child = watchDirectory.resolve(filename);
                    
                    File file = child.toFile();
                    if (file.isFile()) {
                        System.out.println("Detected file: " + filename + " (" + kind.name() + ")");
                        
                        // Categorize immediately, but processing will be delayed
                        // until file is stable (checked in tryMatchAndProcess)
                        categorizeFile(file);
                    }
                }
                
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
                
            } catch (InterruptedException e) {
                System.out.println("DirectoryWatcher interrupted");
                break;
            } catch (Exception e) {
                System.err.println("Error in DirectoryWatcher: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("DirectoryWatcher stopped for: " + watchDirectory);
    }
    
    /**
     * Stop the directory watcher
     */
    public void stopWatching() {
        running = false;
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing watch service: " + e.getMessage());
        }
    }
}
