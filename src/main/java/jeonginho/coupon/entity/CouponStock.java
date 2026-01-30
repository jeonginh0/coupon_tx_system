package jeonginho.coupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jeonginho.coupon.exception.BusinessException;
import jeonginho.coupon.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponStock {

    @Id
    private Long couponId;

    private int quantity;

    public CouponStock(Long couponId, int quantity) {
        this.couponId = couponId;
        this.quantity = quantity;
    }

    public void decrease() {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.COUPON_OUT_OF_STOCK);
        }
        this.quantity--;
    }
}
