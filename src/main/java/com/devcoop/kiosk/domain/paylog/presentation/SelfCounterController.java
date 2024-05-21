package com.devcoop.kiosk.domain.paylog.presentation;

import com.devcoop.kiosk.domain.paylog.presentation.dto.PayLogRequest;
import com.devcoop.kiosk.domain.paylog.presentation.dto.Payments;
import com.devcoop.kiosk.domain.user.presentation.dto.UserPointRequest;
import com.devcoop.kiosk.domain.paylog.service.SelfCounterService;
import com.devcoop.kiosk.global.exception.GlobalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kiosk", description = "Kiosk API")
public class SelfCounterController {
  private final SelfCounterService selfCounterService;
  private final TransactionTemplate transactionTemplate;

  @PostMapping("/executePayments")
  @Operation(summary = "kiosk service", description = "키오스크 전반적인 API")
  public ResponseEntity<Object> executeTransactions(@RequestBody Payments payments) {
    log.info("paymentsDto = {}", payments);
    return transactionTemplate.execute(new TransactionCallback<ResponseEntity<Object>>() {
      @Override
      public ResponseEntity<Object> doInTransaction(TransactionStatus transactionStatus) {
        System.out.println("check");
        try {
          UserPointRequest userPointRequestDto = payments.userPointRequest();

          log.info("userPointRequestDto = {}", userPointRequestDto);
          Object result = selfCounterService.deductPoints(userPointRequestDto);
          log.info("result = {}", result);
          if (result instanceof ResponseEntity && ((ResponseEntity<?>) result).getStatusCode().isError()) {
            throw new RuntimeException("결제를 하는 동안 에러가 발생하였습니다");
          }

          PayLogRequest payLogRequestDto = payments.payLogRequest();
          log.info("payLogRequestDto = {}", payLogRequestDto);
          selfCounterService.savePayLog(payLogRequestDto);

          System.out.println("save kiosk check");
          selfCounterService.saveReceipt(payments.kioskRequest());

          // 모든 트랜잭션 성공 시 200 OK 응답 반환
          return ResponseEntity.ok().build();
        } catch (GlobalException | Exception e) {
          // 트랜잭션 롤백
          transactionStatus.setRollbackOnly();
          return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
    });
  }
}
