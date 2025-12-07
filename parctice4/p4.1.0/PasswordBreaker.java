import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class PasswordBreaker {
    private List<String> passwords = new ArrayList<>();
    private String targetHash;
    private AtomicBoolean found = new AtomicBoolean(false);
    private String resultPassword = null;
    private AtomicInteger specialHashCount = new AtomicInteger(0);
    private List<Boolean> specialFlags = new ArrayList<>();

    public void loadPasswords(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                passwords.add(line);
                specialFlags.add(false);
            }
        }
    }

    public void setTargetHash(String hash) {
        this.targetHash = hash;
    }

    public int getSpecialHashCount() {
        return specialHashCount.get();
    }

    public String getFoundPassword() {
        return found.get() ? resultPassword : null;
    }

    private boolean hasSpecialFeature(String hash) {
        for (int i = 0; i < 3; i++) {
            char c = hash.charAt(i);
            if (c >= '0' && c <= '6') {
                return true;
            }
        }
        return false;
    }

    public void startCracking(int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int totalPasswords = passwords.size();
        int currentIndex = 0;

        try {
            while (!found.get() && resultPassword == null && currentIndex < totalPasswords) {
                List<Callable<Boolean>> tasks = new ArrayList<>();
                int endIndex = Math.min(currentIndex + numThreads, totalPasswords);

                for (int i = currentIndex; i < endIndex; i++) {
                    final int index = i;
                    tasks.add(() -> {
                        String password = passwords.get(index);
                        String hash = CryptoHash.hashString(password);

                        if (hasSpecialFeature(hash)) {
                            specialHashCount.incrementAndGet();
                        }

                        if (hash.equals(targetHash)) {
                            resultPassword = password;
                            found.set(true);
                        }

                        return true;
                    });
                }

                List<Future<Boolean>> results = executor.invokeAll(tasks);
                for (Future<Boolean> f : results) {
                    try {
                        f.get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                currentIndex = endIndex;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}