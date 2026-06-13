package backend.saferent.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@EnableElasticsearchRepositories
public interface ApartmentSearchRepository extends ElasticsearchRepository<ApartmentDocument, UUID> {
}
