package jeonginho.coupon.service;

import static jeonginho.coupon.exception.ErrorCode.*;

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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private static final int MAX_RETRY_COUNT = 3;

    private final CouponIssueTransactionalService txService;

    public void issueCoupon(Long userId, Long couponId) {

        int attempt = 0;

        while (true) {
            try {
                txService.issueOnce(userId, couponId);
                return;
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRY_COUNT) {
                    throw e;
                }
            }
        }
    }
}
