package iuh.fit.se.tramcamxuc.common.listener;

import iuh.fit.se.tramcamxuc.common.event.SongListenedEvent;
import iuh.fit.se.tramcamxuc.modules.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticEventListener {

    private final StatisticService statisticService;

    @Async("taskExecutor")
    @EventListener
    public void handleSongListened(SongListenedEvent event) {
        if (event.getUserId() != null) {
            log.debug("Recording listen history for user {} on song {}", event.getUserId(), event.getSongId());
            try {
                statisticService.recordListenHistory(event.getUserId(), event.getSongId());
            } catch (Exception e) {
                log.error("Failed to record listen history for user {} on song {}: {}", 
                    event.getUserId(), event.getSongId(), e.getMessage());
            }
        }
    }
}
