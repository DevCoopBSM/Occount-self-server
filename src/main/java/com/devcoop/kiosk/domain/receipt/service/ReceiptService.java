package com.devcoop.kiosk.domain.receipt.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.item.Item;
import com.devcoop.kiosk.domain.item.repository.ItemRepository;
import com.devcoop.kiosk.domain.payment.dto.PaymentRequest.PaymentItem;
import com.devcoop.kiosk.domain.receipt.KioskReceipt;
import com.devcoop.kiosk.domain.receipt.repository.KioskReceiptRepository;
import com.devcoop.kiosk.domain.receipt.types.SaleType;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {
    private final KioskReceiptRepository kioskReceiptRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void saveReceipt(List<PaymentItem> items, String userId) throws GlobalException {
        boolean anyValidItemSaved = false;
        
        for (PaymentItem paymentItem : items) {
            log.info("결제 아이템 정보: itemId={}, itemName={}, totalPrice={}", 
                paymentItem.itemId(), paymentItem.itemName(), paymentItem.totalPrice());
            
            try {
                Item item = itemRepository.findById(Long.parseLong(paymentItem.itemId()))
                    .orElseThrow(() -> new GlobalException(ErrorCode.ITEM_NOT_FOUND));
                
                KioskReceipt receipt = KioskReceipt.builder()
                    .tradedPoint(paymentItem.totalPrice())
                    .itemName(paymentItem.itemName())
                    .saleQty((short) paymentItem.quantity())
                    .userCode(userId)
                    .itemCode(paymentItem.itemId())
                    .saleType(SaleType.NORMAL)
                    .eventType(item.getEvent())
                    .build();
                    
                kioskReceiptRepository.save(receipt);
                anyValidItemSaved = true;
            } catch (NumberFormatException e) {
                log.error("잘못된 itemId 형식: {}", paymentItem.itemId());
                throw new GlobalException(ErrorCode.INVALID_ITEM_ID_FORMAT);
            }
        }
        
        if (!anyValidItemSaved) {
            throw new GlobalException(ErrorCode.RECEIPT_SAVE_FAILED);
        }
    }
}
