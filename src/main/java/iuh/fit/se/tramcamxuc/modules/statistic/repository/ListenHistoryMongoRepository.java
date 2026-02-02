package iuh.fit.se.tramcamxuc.modules.statistic.repository;

import iuh.fit.se.tramcamxuc.modules.statistic.document.ListenHistoryDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ListenHistoryMongoRepository extends MongoRepository<ListenHistoryDoc, String> {

    Page<ListenHistoryDoc> findByUserIdOrderByListenedAtDesc(UUID userId, Pageable pageable);

    long countBySongId(UUID songId);

    long countByListenedAtBetween(LocalDateTime start, LocalDateTime end);

    @Aggregation(pipeline = {
            "{ '$match': { 'listened_at': { '$gte': ?0 } } }",
            "{ '$group': { '_id': '$song_id', 'total_plays': { '$count': {} } } }",
            "{ '$sort': { 'total_plays': -1 } }",
            "{ '$limit': ?1 }"
    })
    List<TopSongResult> findTopTrending(LocalDateTime since, int limit);

    public static class TopSongResult {
        public UUID _id;
        public long total_plays;
    }
}