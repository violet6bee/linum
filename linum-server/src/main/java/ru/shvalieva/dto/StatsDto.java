package ru.shvalieva.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StatsDto {
    private long totalHosts;
    private long outdatedHosts;
    private List<OsDistribution> osDistribution;

    @Data
    @Builder
    public static class OsDistribution {
        private String os;
        private long count;
    }
}