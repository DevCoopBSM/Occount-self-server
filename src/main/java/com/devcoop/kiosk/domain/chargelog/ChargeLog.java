package com.devcoop.kiosk.domain.chargelog;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "occount_chargeLog")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chargeId;

    @Column(length = 255)
    private String userCode;  // 유저 코드 (바코드 혹은 전화번호)

    private LocalDateTime chargeDate; // 충전 날짜

    @Column(columnDefinition = "text")
    private String chargeType; // 충전 타입

    private Integer beforePoint; // 충전 전 포인트

    private int chargedPoint; // 충전한 포인트

    private int afterPoint; // 충전 후 포인트

    @Column(length = 255)
    private String managedEmail; // 담당 매점부 이메일

    @Column(length = 255)
    private String reason; // 충전 사유

    @Column(length = 255, columnDefinition = "varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci")
    private String paymentId; // PG 거래 번호

    @Column(columnDefinition = "text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci")
    private String refundState; // 환불 상태

    private LocalDateTime refundDate; // 환불 완료 날짜

    @Column(columnDefinition = "text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci")
    private String refundRequesterId; // 환불 신청 아이디
}
