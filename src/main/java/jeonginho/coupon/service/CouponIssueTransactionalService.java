package jeonginho.coupon.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import jeonginho.coupon.entity.Coupon;
import jeonginho.coupon.entity.CouponIssue;
import jeonginho.coupon.entity.CouponStock;
import jeonginho.coupon.exception.BusinessException;
import jeonginho.coupon.exception.ErrorCode;
import jeonginho.coupon.repository.CouponIssueRepository;
import jeonginho.coupon.repository.CouponRepository;
import jeonginho.coupon.repository.CouponStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueTransactionalService {
    private final CouponRepository couponRepository;
    private final CouponStockRepository couponStockRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public void issueOnce(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        coupon.validateIssuableTime(LocalDateTime.now());

        CouponStock stock = couponStockRepository.findByIdForUpdate(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_STOCK_NOT_FOUND));

        stock.decrease();

        couponIssueRepository.save(
            CouponIssue.issue(userId, couponId)
        );
    }

}
