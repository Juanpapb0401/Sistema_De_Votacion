import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class CacheEntry {
    private final List<String> data;
    private final LocalDateTime timestamp;
    private final long ttlMinutes;
    
    public CacheEntry(List<String> data, long ttlMinutes) {
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.ttlMinutes = ttlMinutes;
    }
    
    /**
     * Verifica si la entrada del cache ha expirado
     */
    public boolean isExpired() {
        long minutesElapsed = ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now());
        return minutesElapsed >= ttlMinutes;
    }
    
    /**
     * Obtiene los datos si no han expirado
     */
    public List<String> getData() {
        return isExpired() ? null : data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public long getAge() {
        return ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now());
    }
    
    @Override
    public String toString() {
        return String.format("CacheEntry{data=%s, age=%ds, expired=%b}", 
                data, getAge(), isExpired());
    }
} 