package jeonginho.coupon;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jeonginho.coupon.entity.Coupon;
import jeonginho.coupon.entity.CouponStock;
import jeonginho.coupon.repository.CouponIssueRepository;
import jeonginho.coupon.repository.CouponRepository;
import jeonginho.coupon.repository.CouponStockRepository;
import jeonginho.coupon.service.CouponIssueService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CouponIssueServiceTest {

    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponStockRepository couponStockRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    Long couponId;

    @BeforeEach
    void setUp() {
        Coupon coupon = new Coupon("선착순 쿠폰", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
        couponRepository.save(coupon);

        couponStockRepository.save(new CouponStock(coupon.getId(), 500));

        couponId = coupon.getId();
    }

    @AfterEach
    void tearDown() {
        couponIssueRepository.deleteAll();
        couponStockRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    public void 같은_유저는_중복_발급불가() {
        couponIssueService.issueCoupon(1L, couponId);

        assertThatThrownBy(() -> couponIssueService.issueCoupon(1L, couponId))
            .isInstanceOf(Exception.class);

        assertThat(couponIssueRepository.count()).isEqualTo(1);
    }

    @Test
    void 동시에_1000명이_요청해도_500개만_발급가능() throws InterruptedException {
        int threadCount = 1000;
        int stock = 500;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssueService.issueCoupon(userId, couponId);
                } catch (Exception e) {
                    // 실패는 정상 (락 대기 후 재고 소진)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long issuedCount = couponIssueRepository.count();
        assertThat(issuedCount).isEqualTo(stock);
    }
}
