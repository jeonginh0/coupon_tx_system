package jeonginho.coupon.controller;

import jeonginho.coupon.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponIssueService couponIssueService;

    @PostMapping("/{couponId}/issue")
    public ResponseEntity<Void> issue(@PathVariable Long couponId, @RequestParam Long userId){
        couponIssueService.issueCoupon(couponId, userId);

        return ResponseEntity.ok().build();
    }

}
