package com.devcoop.kiosk.domain.paylog.service;

import com.devcoop.kiosk.domain.item.Item;
import com.devcoop.kiosk.domain.item.repository.ItemRepository;
import com.devcoop.kiosk.domain.item.types.EventType;
import com.devcoop.kiosk.domain.paylog.PayLog;
import com.devcoop.kiosk.domain.paylog.presentation.dto.KioskItemInfo;
import com.devcoop.kiosk.domain.paylog.presentation.dto.KioskRequest;
import com.devcoop.kiosk.domain.paylog.presentation.dto.PayLogRequest;
import com.devcoop.kiosk.domain.paylog.presentation.dto.Payments;
import com.devcoop.kiosk.domain.paylog.repository.PayLogRepository;
import com.devcoop.kiosk.domain.receipt.KioskReceipt;
import com.devcoop.kiosk.domain.receipt.repository.KioskReceiptRepository;
import com.devcoop.kiosk.domain.receipt.types.SaleType;
import com.devcoop.kiosk.domain.user.User;
import com.devcoop.kiosk.domain.user.presentation.dto.UserPointRequest;
import com.devcoop.kiosk.domain.user.repository.UserRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelfCounterService {

    private final PayLogRepository payLogRepository;
    private final UserRepository userRepository;
    private final KioskReceiptRepository kioskReceiptRepository;
    private final ItemRepository itemRepository;

    public int deductPoints(UserPointRequest userPointRequest) throws GlobalException {
        String userCode = userPointRequest.userCode();
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        log.info("userName = {}", user.getUserName());

        log.info("포인트 차감 전: {}", user.getUserPoint());
        log.info("차감할 포인트: {}", userPointRequest.totalPrice());


        if (user.getUserPoint() < userPointRequest.totalPrice()) {
            throw new RuntimeException("결제하는 것에 실패했습니다.");
        }

        int newPoint = user.getUserPoint() - userPointRequest.totalPrice();
        user.setUserPoint(newPoint);
        userRepository.save(user);
        return newPoint;
    }

    public void savePayLog(int beforePoint, int afterPoint, PayLogRequest payLogRequest) {
        try {
            // PayLog 엔티티 생성 시, 정확한 beforePoint와 afterPoint를 사용
            PayLog payLog = payLogRequest.toEntity(beforePoint, afterPoint);
            payLogRepository.save(payLog);
        } catch (Exception e) {
            log.error("결제 로그 저장 중 오류가 발생하였습니다.", e);
            throw new RuntimeException("결제 로그 저장 중 오류가 발생하였습니다.", e);
        }
    }
    
    
    

    public void saveReceipt(KioskRequest kioskRequest) {
        try {
            List<KioskItemInfo> requestItems = kioskRequest.items();
            log.info("requestItemList = {}", requestItems);
            for (KioskItemInfo itemInfo : requestItems) {
                Item item = itemRepository.findByItemName(itemInfo.itemName());
                log.info("item = {}", item);

                if (item == null) {
                    throw new RuntimeException("없는 상품입니다.");
                }

                EventType eventType = EventType.NONE;

                if (item.getEvent() == EventType.ONE_PLUS_ONE) {
                    eventType = EventType.ONE_PLUS_ONE;
                }

                KioskReceipt kioskReceipt = KioskReceipt.builder()
                        .tradedPoint(itemInfo.dcmSaleAmt())  // 필드명 변경
                        .itemName(item.getItemName())
                        .saleQty((short) itemInfo.saleQty())
                        .userCode(kioskRequest.userId())  // 필드명 변경
                        .itemCode(String.valueOf(item.getItemId()))  // 필드명 변경
                        .saleType(SaleType.NORMAL)
                        .eventType(eventType)  // eventType을 String으로 처리
                        .build();

                kioskReceiptRepository.save(kioskReceipt);
            }
        } catch (Exception e) {
            log.error("영수증 저장 중 오류가 발생하였습니다.", e);
            throw new RuntimeException("영수증 저장 중 오류가 발생하였습니다.", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeAllTransactions(Payments payments) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 포인트 차감 전 beforePoint를 얻기 위해 User 상태 조회
            User userBeforePayment = userRepository.findByUserCode(payments.payLogRequest().userCode())
                    .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

            log.info("userBeforePayment = {}", userBeforePayment); // userBeforePayment.getUserCode() 로 userCode를 받아서 사용;
            int beforePoint = userBeforePayment.getUserPoint();

            // 포인트 차감
            int newPoints = deductPoints(payments.userPointRequest());
            response.put("remainingPoints", newPoints); // 새로운 포인트 반환

            // 결제 로그 저장 시 정확한 beforePoint와 afterPoint를 전달
            savePayLog(beforePoint, newPoints, payments.payLogRequest());

            // 영수증 저장
            saveReceipt(payments.kioskRequest());

            response.put("status", "success");
            response.put("message", "결제가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("트랜잭션 실패 및 롤백", e);
            throw new RuntimeException(e.getMessage(), e); // 예외를 다시 던져 트랜잭션을 롤백합니다.
        } catch (GlobalException e) {
            throw new RuntimeException(e);
        }

        return response;
    }
}
