package ru.shvalieva.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shvalieva.dto.HostDetailsDto;
import ru.shvalieva.dto.HostSummaryDto;
import ru.shvalieva.dto.StatsDto;
import ru.shvalieva.model.Host;
import ru.shvalieva.model.HostPackage;
import ru.shvalieva.repository.HostPackageRepository;
import ru.shvalieva.repository.HostRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class HostController {

    private final HostRepository hostRepository;
    private final HostPackageRepository hostPackageRepository;

    @GetMapping("/hosts")
    public ResponseEntity<List<HostSummaryDto>> getHosts() {
        List<Host> hosts = hostRepository.findAll();
        List<HostSummaryDto> result = hosts.stream().map(host -> {
            int outdatedCount = 0;
            return HostSummaryDto.builder()
                    .id(host.getId())
                    .name(host.getName())
                    .osPrettyName(host.getOsPrettyName())
                    .kernelVersion(host.getKernelVersion())
                    .lastUpdated(host.getLastUpdated())
                    .outdatedCount(outdatedCount)
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hosts/{id}")
    public ResponseEntity<HostDetailsDto> getHostDetails(@PathVariable UUID id) {
        Host host = hostRepository.findById(id).orElse(null);
        if (host == null) {
            return ResponseEntity.notFound().build();
        }
        List<HostPackage> hostPackages = hostPackageRepository.findByHost(host);
        List<HostDetailsDto.PackageInfo> packages = hostPackages.stream()
                .map(hp -> HostDetailsDto.PackageInfo.builder()
                        .name(hp.getPackageEntity().getName())
                        .version(hp.getVersion())
                        .architecture(hp.getArchitecture())
                        .build())
                .collect(Collectors.toList());

        List<HostDetailsDto.UpgradablePackageInfo> upgradable = List.of();

        HostDetailsDto dto = HostDetailsDto.builder()
                .id(host.getId())
                .name(host.getName())
                .ipAddress(host.getIpAddress())
                .osPrettyName(host.getOsPrettyName())
                .kernelVersion(host.getKernelVersion())
                .architecture(host.getArchitecture())
                .lastUpdated(host.getLastUpdated())
                .packages(packages)
                .upgradablePackages(upgradable)
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats() {
        long totalHosts = hostRepository.count();
        long outdatedHosts = 0;
        Map<String, Long> osCount = hostRepository.findAll().stream()
                .map(Host::getOsPrettyName)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        List<StatsDto.OsDistribution> osDistribution = osCount.entrySet().stream()
                .map(e -> StatsDto.OsDistribution.builder().os(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());

        StatsDto stats = StatsDto.builder()
                .totalHosts(totalHosts)
                .outdatedHosts(outdatedHosts)
                .osDistribution(osDistribution)
                .build();
        return ResponseEntity.ok(stats);
    }
}