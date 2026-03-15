package ru.shvalieva.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AgentDataDto {
    private String hostId;
    private String token;
    private String timestamp;
    private Map<String, String> osInfo;
    private String kernelVersion;
    private String architecture;
    private List<PackageDto> packages;
    private List<RepositoryDto> repositories;
    private List<ModuleDto> modules;

    @Data
    public static class PackageDto {
        private String name;
        private String version;
        private String architecture;
    }

    @Data
    public static class RepositoryDto {
        private String name;
        private String url;
        private String distribution;
        private List<String> components;
    }

    @Data
    public static class ModuleDto {
        private String name;
        private String version;
    }
}