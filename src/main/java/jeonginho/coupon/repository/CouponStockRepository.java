package jeonginho.coupon.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import jeonginho.coupon.entity.CouponStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CouponStockRepository extends JpaRepository<CouponStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cs from CouponStock cs where cs.couponId = :couponId")
    Optional<CouponStock> findByIdForUpdate(Long couponId);
}
