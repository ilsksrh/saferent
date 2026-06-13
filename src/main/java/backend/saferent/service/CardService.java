package backend.saferent.service;

import backend.saferent.dto.request.payment.SaveCardRequest;
import backend.saferent.dto.response.payment.SavedCardResponse;
import backend.saferent.entity.SavedCard;
import backend.saferent.entity.User;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.repository.SavedCardRepository;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final SavedCardRepository savedCardRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public SavedCardResponse addCard(SaveCardRequest request) {
        User user = securityUtils.getCurrentUser();

        String digits = request.getCardNumber() == null
                ? "" : request.getCardNumber().replaceAll("\\D", "");
        if (digits.length() < 13) {
            throw new BadRequestException("Invalid card number");
        }
        String last4 = digits.substring(digits.length() - 4);
        String brand = switch (digits.charAt(0)) {
            case '4' -> "VISA";
            case '5' -> "Mastercard";
            default -> "CARD";
        };

        boolean makeDefault = request.isMakeDefault()
                || savedCardRepository.findByUserOrderByCreatedAtDesc(user).isEmpty();
        if (makeDefault) {
            savedCardRepository.findByUserAndDefaultCardTrue(user).forEach(c -> {
                c.setDefaultCard(false);
                savedCardRepository.save(c);
            });
        }

        SavedCard card = SavedCard.builder()
                .user(user)
                .brand(brand)
                .last4(last4)
                .expMonth(request.getExpMonth())
                .expYear(request.getExpYear())
                .defaultCard(makeDefault)
                .build();

        return toResponse(savedCardRepository.save(card));
    }

    public List<SavedCardResponse> getMyCards() {
        User user = securityUtils.getCurrentUser();
        return savedCardRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void setDefault(UUID cardId) {
        User user = securityUtils.getCurrentUser();
        SavedCard card = ownedCardOrThrow(cardId, user);
        savedCardRepository.findByUserAndDefaultCardTrue(user).forEach(c -> {
            c.setDefaultCard(false);
            savedCardRepository.save(c);
        });
        card.setDefaultCard(true);
        savedCardRepository.save(card);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        User user = securityUtils.getCurrentUser();
        savedCardRepository.delete(ownedCardOrThrow(cardId, user));
    }

    private SavedCard ownedCardOrThrow(UUID cardId, User user) {
        SavedCard card = savedCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));
        if (!card.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Not your card");
        }
        return card;
    }

    private SavedCardResponse toResponse(SavedCard c) {
        return SavedCardResponse.builder()
                .id(c.getId())
                .brand(c.getBrand())
                .last4(c.getLast4())
                .expMonth(c.getExpMonth())
                .expYear(c.getExpYear())
                .defaultCard(c.isDefaultCard())
                .build();
    }
}
