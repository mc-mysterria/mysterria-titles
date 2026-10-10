package net.mysterria.titles.audit;

import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditOutcome;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditPrivacy;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditProducer;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditRisk;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Every call is guarded, so a missing or failing audit client never changes title, item or command behaviour. */
public final class TitlesAuditEmitter implements AutoCloseable {

    private static final int MAX_KEYS = 32;
    private static final int MAX_TEXT = 256;
    private static final int MAX_LONG_TEXT = 1_024;
    private static final Set<String> LONG_TEXT_KEYS = Set.of("granted", "revoked", "pathways");

    private final AuditProducer producer;
    private final Logger logger;

    public TitlesAuditEmitter(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.producer = createProducer(plugin, logger);
    }

    private static AuditProducer createProducer(JavaPlugin plugin, Logger logger) {
        try {
            return AuditProducer.create(plugin.getDataFolder().toPath().toAbsolutePath().getParent()
                            .resolve("mysterria-audit-spool"),
                    "mysterria-titles", plugin.getPluginMeta().getVersion());
        } catch (RuntimeException | LinkageError failure) {
            logger.log(Level.WARNING, "Audit client unavailable; title audit events are disabled", failure);
            return null;
        }
    }

    public void emit(AuditOutcome outcome, AuditRow row) {
        emit(outcome, row.risk(), row.reason(), row);
    }

    private void emit(AuditOutcome outcome, AuditRisk risk, String reason, AuditRow row) {
        if (producer == null || row.eventType() == null || row.eventType().isBlank()) {
            return;
        }
        try {
            producer.emit(row.eventType(), outcome, risk, AuditPrivacy.STAFF_RESTRICTED,
                    row.correlationId() == null ? UUID.randomUUID() : row.correlationId(),
                    row.businessId() == null ? null : bounded(row.businessId(), MAX_TEXT),
                    row.actorId(), row.subjectId(), null,
                    reason == null ? null : bounded(reason, MAX_TEXT),
                    boundedMetadata(row.metadata()));
        } catch (RuntimeException | LinkageError failure) {
            logger.log(Level.FINE, "Failed to emit audit event " + row.eventType(), failure);
            recordFailure();
        }
    }

    private void recordFailure() {
        try {
            producer.recordFailure();
        } catch (RuntimeException | LinkageError ignored) {
            // Failure accounting is itself best effort.
        }
    }

    private static Map<String, Object> boundedMetadata(Map<String, ?> metadata) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null && result.size() < MAX_KEYS) {
                    String boundedKey = bounded(key, MAX_TEXT);
                    result.putIfAbsent(boundedKey, boundedValue(boundedKey, value));
                }
            });
        }
        return Collections.unmodifiableMap(result);
    }

    private static Object boundedValue(String key, Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        int limit = LONG_TEXT_KEYS.contains(key) ? MAX_LONG_TEXT : MAX_TEXT;
        return bounded(value instanceof String text ? text : String.valueOf(value), limit);
    }

    private static String bounded(String value, int limit) {
        if (value.codePointCount(0, value.length()) <= limit) return value;
        return value.substring(0, value.offsetByCodePoints(0, limit));
    }

    @Override
    public void close() {
        if (producer == null) {
            return;
        }
        try {
            producer.close();
        } catch (RuntimeException | LinkageError failure) {
            logger.log(Level.WARNING, "Failed to close audit producer", failure);
        }
    }
}
