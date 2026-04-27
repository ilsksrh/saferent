package backend.saferent.service;

import backend.saferent.dto.response.favorite.FavoriteResponse;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {

    String toggleFavorite(UUID userId, UUID apartmentId);

    List<FavoriteResponse> getMyFavorites(UUID userId);

    boolean isFavorite(UUID userId, UUID apartmentId);
}