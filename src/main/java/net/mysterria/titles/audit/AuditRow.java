package net.mysterria.titles.audit;

import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditRisk;

import java.util.Map;
import java.util.UUID;

/**
 * Built from plain values already in hand.
 *
 * @param reason audit reason for the row
 */
public record AuditRow(String eventType, AuditRisk risk, UUID correlationId, String businessId,
                       UUID actorId, UUID subjectId, String reason, Map<String, ?> metadata) {
}
