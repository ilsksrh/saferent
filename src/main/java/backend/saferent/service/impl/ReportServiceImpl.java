package backend.saferent.service.impl;

import backend.saferent.dto.request.report.CreateReportRequest;
import backend.saferent.dto.request.report.ResolveReportRequest;
import backend.saferent.dto.response.report.ReportResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Message;
import backend.saferent.entity.Report;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ReportStatus;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.ReportMapper;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.MessageRepository;
import backend.saferent.repository.ReportRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final MessageRepository messageRepository;
    private final ReportMapper reportMapper;

    @Override
    @Transactional
    public ReportResponse create(UUID reporterId, CreateReportRequest request) {
        if (request.getTargetUserId() == null
                && request.getApartmentId() == null
                && request.getMessageId() == null) {
            throw new BadRequestException("Должен быть указан targetUserId, apartmentId или messageId");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Report.ReportBuilder builder = Report.builder()
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.OPEN);

        if (request.getTargetUserId() != null) {
            User target = userRepository.findById(request.getTargetUserId())
                    .orElseThrow(() -> new NotFoundException("Пользователь-цель не найден"));
            builder.targetUser(target);
        }
        if (request.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                    .orElseThrow(() -> new NotFoundException("Квартира не найдена"));
            builder.apartment(apartment);
        }
        if (request.getMessageId() != null) {
            Message message = messageRepository.findById(request.getMessageId())
                    .orElseThrow(() -> new NotFoundException("Сообщение не найдено"));
            builder.message(message);
        }

        Report saved = reportRepository.save(builder.build());
        return reportMapper.toResponse(saved);
    }

    @Override
    public ReportResponse getById(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жалоба не найдена"));
        return reportMapper.toResponse(report);
    }

    @Override
    public Page<ReportResponse> getMyReports(UUID reporterId, Pageable pageable) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId, pageable)
                .map(reportMapper::toResponse);
    }

    @Override
    public Page<ReportResponse> getAll(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(reportMapper::toResponse);
        }
        return reportRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(reportMapper::toResponse);
    }

    @Override
    @Transactional
    public ReportResponse resolve(UUID id, UUID moderatorId, ResolveReportRequest request) {
        if (request.getStatus() != ReportStatus.RESOLVED
                && request.getStatus() != ReportStatus.REJECTED) {
            throw new BadRequestException("status должен быть RESOLVED или REJECTED");
        }

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жалоба не найдена"));

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NotFoundException("Модератор не найден"));

        report.setStatus(request.getStatus());
        report.setModerator(moderator);
        report.setResolutionComment(request.getComment());
        report.setResolvedAt(LocalDateTime.now());

        return reportMapper.toResponse(reportRepository.save(report));
    }
}
