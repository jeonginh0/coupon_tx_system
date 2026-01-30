package jeonginho.coupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "couponId"}))
public class CouponIssue {

    @Id @GeneratedValue
    private Long id;

    private Long userId;
    private Long couponId;
    private LocalDateTime issuedAt;

    private CouponIssue(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
        this.issuedAt = LocalDateTime.now();
    }

    public static CouponIssue issue(Long userId, Long couponId) {
        return new CouponIssue(userId, couponId);
    }
}
