package backend.saferent.entity.enums;
public enum InspectionResult {
    NO_DAMAGE,       // SSIM >= 0.92 → депозит арендатору
    MINOR_DAMAGE,    // 0.70 <= SSIM < 0.92 → модератор смотрит
    MAJOR_DAMAGE     // SSIM < 0.70 → депозит арендодателю
}