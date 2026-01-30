package jeonginho.coupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import jeonginho.coupon.exception.BusinessException;
import jeonginho.coupon.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id @GeneratedValue
    private Long id;

    private String name;

    private LocalDateTime issueStartTime;
    private LocalDateTime issueEndTime;

    public Coupon(String name, LocalDateTime issueStartTime, LocalDateTime issueEndTime) {
        this.name = name;
        this.issueStartTime = issueStartTime;
        this.issueEndTime = issueEndTime;
    }

    public void validateIssuableTime(LocalDateTime now) {
        if (now.isBefore(issueStartTime) || now.isAfter(issueEndTime)) {
            throw new BusinessException(ErrorCode.COUPON_NOT_ISSUABLE_TIME);
        }
    }

}
