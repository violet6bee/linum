package ru.shvalieva.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HostDetailsDto {
    private UUID id;
    private String name;
    private String ipAddress;
    private String osPrettyName;
    private String kernelVersion;
    private String architecture;
    private Instant lastUpdated;
    private List<PackageInfo> packages;
    private List<UpgradablePackageInfo> upgradablePackages;

    @Data
    @Builder
    public static class PackageInfo {
        private String name;
        private String version;
        private String architecture;
    }

    @Data
    @Builder
    public static class UpgradablePackageInfo {
        private String name;
        private String currentVersion;
        private String targetVersion;
    }
}