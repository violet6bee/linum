package ru.shvalieva.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HostSummaryDto {
    private UUID id;
    private String name;
    private String osPrettyName;
    private String kernelVersion;
    private Instant lastUpdated;
    private int outdatedCount;
}