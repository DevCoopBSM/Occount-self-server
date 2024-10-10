package com.devcoop.kiosk.domain.receipt;

import com.devcoop.kiosk.domain.item.types.EventType;
import com.devcoop.kiosk.domain.receipt.types.SaleType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "occount_kioskReceipts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class KioskReceipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int receiptId; // 거래식별용 id
    
    private String itemCode; // 품목 id
    
    private int tradedPoint; // 거래 가격
    
    private String itemName; // 품목 이름
    
    private int saleQty; // 품목 수량
    
    @CreatedDate
    private LocalDateTime saleDate = LocalDateTime.now(); // 거래 발생 시간
    
    private String userCode; // 사용자 바코드

    @Enumerated(value = EnumType.STRING)
    private SaleType saleType; // 결제 타입 (0: 정상 결제, 1: 환불 결제 등)

    @Enumerated(value = EnumType.STRING)
    private EventType eventType; // 이벤트 여부 ('ONE_PLUS_ONE', 'NONE' 등)

    @Builder
    public KioskReceipt(int receiptId, String itemCode, int tradedPoint, String itemName, int saleQty, LocalDateTime saleDate, String userCode, SaleType saleType, EventType eventType) {
        this.receiptId = receiptId;
        this.itemCode = itemCode;
        this.tradedPoint = tradedPoint;
        this.itemName = itemName;
        this.saleQty = saleQty;
        this.saleDate = saleDate;
        this.userCode = userCode;
        this.saleType = saleType;
        this.eventType = eventType;
    }
}
