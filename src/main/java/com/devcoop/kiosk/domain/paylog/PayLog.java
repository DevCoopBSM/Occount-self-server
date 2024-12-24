package com.devcoop.kiosk.domain.paylog;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.devcoop.kiosk.domain.paylog.types.PaymentType;
import com.devcoop.kiosk.domain.paylog.types.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "occount_payLog")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int payId;
    
    @Column(length = 255)
    private String userCode;
    
    @CreationTimestamp
    private LocalDateTime payDate;
    
    @Column(length = 255)
    @Enumerated(EnumType.STRING)
    private PaymentType payType;
    
    private int beforePoint;
    
    private int payedPoint;
    
    private int afterPoint;

    @Column(length = 255)
    private String managedEmail;

    @Column(length = 255)
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;

    private int cardAmount;

    @Column(length = 255)
    private String paymentId;

    @Builder
    public PayLog(int payId, String userCode, LocalDateTime payDate, PaymentType payType,
                  int beforePoint, int payedPoint, int afterPoint,
                  String managedEmail, EventType eventType,
                  int cardAmount, String paymentId, String transactionId) {
        this.payId = payId;
        this.userCode = userCode;
        this.payDate = payDate;
        this.payType = payType;
        this.beforePoint = beforePoint;
        this.payedPoint = payedPoint;
        this.afterPoint = afterPoint;
        this.managedEmail = managedEmail;
        this.eventType = eventType;
        this.cardAmount = cardAmount;
        this.paymentId = paymentId;
    }
}