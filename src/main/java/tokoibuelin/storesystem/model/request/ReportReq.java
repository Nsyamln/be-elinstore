package tokoibuelin.storesystem.model.request;

import java.time.OffsetDateTime;

public record ReportReq(
        OffsetDateTime startedAt,
        OffsetDateTime endedAt
) {
}
