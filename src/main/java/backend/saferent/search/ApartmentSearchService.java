package backend.saferent.search;

import backend.saferent.entity.Apartment;
import backend.saferent.entity.enums.ApartmentStatus;
import backend.saferent.repository.ApartmentRepository;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ApartmentSearchService {

    private final ObjectProvider<ApartmentSearchRepository> searchRepositoryProvider;
    private final ObjectProvider<ElasticsearchOperations> elasticsearchOperationsProvider;
    private final ApartmentRepository apartmentRepository;

    public ApartmentSearchService(ObjectProvider<ApartmentSearchRepository> searchRepositoryProvider,
                                  ObjectProvider<ElasticsearchOperations> elasticsearchOperationsProvider,
                                  ApartmentRepository apartmentRepository) {
        this.searchRepositoryProvider = searchRepositoryProvider;
        this.elasticsearchOperationsProvider = elasticsearchOperationsProvider;
        this.apartmentRepository = apartmentRepository;
    }

    public void index(Apartment apartment) {
        ApartmentSearchRepository repo = searchRepositoryProvider.getIfAvailable();
        if (repo == null) return;
        try {
            // В индексе держим только верифицированные ACTIVE без soft-delete
            boolean indexable = apartment.isVerified()
                    && apartment.getStatus() == ApartmentStatus.ACTIVE
                    && apartment.getDeletedAt() == null;
            if (indexable) {
                repo.save(toDocument(apartment));
            } else {
                repo.deleteById(apartment.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to index apartment {}: {}", apartment.getId(), e.getMessage());
        }
    }

    public void delete(UUID id) {
        ApartmentSearchRepository repo = searchRepositoryProvider.getIfAvailable();
        if (repo == null) return;
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            log.warn("Failed to delete apartment {} from index: {}", id, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public long reindexAll() {
        ApartmentSearchRepository repo = searchRepositoryProvider.getIfAvailable();
        if (repo == null) {
            log.warn("Elasticsearch unavailable: skipping reindex");
            return 0;
        }
        try {
            repo.deleteAll();
            // Индексируем только верифицированные ACTIVE
            List<Apartment> all = apartmentRepository
                    .findAllByStatusAndVerifiedTrueAndDeletedAtIsNull(ApartmentStatus.ACTIVE);
            List<ApartmentDocument> docs = all.stream().map(this::toDocument).toList();
            repo.saveAll(docs);
            log.info("Elasticsearch: reindexed {} verified apartments", docs.size());
            return docs.size();
        } catch (Exception e) {
            log.warn("Failed to reindex apartments: {}", e.getMessage());
            return 0;
        }
    }

    public List<ApartmentDocument> search(String q,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice,
                                          UUID districtId,
                                          Short rooms) {
        ElasticsearchOperations operations = elasticsearchOperationsProvider.getIfAvailable();
        if (operations == null) {
            log.warn("Elasticsearch unavailable: returning empty search results");
            return Collections.emptyList();
        }

        var boolBuilder = co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.of(b -> {
            b.filter(f -> f.term(t -> t.field("status").value("ACTIVE")));

            if (q != null && !q.isBlank()) {
                b.must(m -> m.multiMatch(mm -> mm
                        .query(q)
                        .fields("title^3", "description", "address^2")
                        .fuzziness("AUTO")));
            }
            if (minPrice != null) {
                b.filter(f -> f.range(r -> r.number(n -> n.field("price").gte(minPrice.doubleValue()))));
            }
            if (maxPrice != null) {
                b.filter(f -> f.range(r -> r.number(n -> n.field("price").lte(maxPrice.doubleValue()))));
            }
            if (districtId != null) {
                b.filter(f -> f.term(t -> t.field("districtId").value(districtId.toString())));
            }
            if (rooms != null) {
                b.filter(f -> f.term(t -> t.field("rooms").value(rooms.intValue())));
            }
            return b;
        });

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(qb -> qb.bool(boolBuilder)))
                .build();

        try {
            SearchHits<ApartmentDocument> hits = operations.search(query, ApartmentDocument.class);
            return hits.getSearchHits().stream().map(h -> h.getContent()).toList();
        } catch (Exception e) {
            log.warn("Elasticsearch search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private ApartmentDocument toDocument(Apartment a) {
        return ApartmentDocument.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .address(a.getAddress())
                .districtId(a.getDistrict() != null ? a.getDistrict().getId().toString() : null)
                .districtName(a.getDistrict() != null ? a.getDistrict().getName() : null)
                .landlordId(a.getLandlord() != null ? a.getLandlord().getId().toString() : null)
                .price(a.getPrice())
                .area(a.getArea())
                .rooms(a.getRooms())
                .status(a.getStatus() != null ? a.getStatus().name() : null)
                .verified(a.isVerified())
                .build();
    }
}
