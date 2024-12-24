package com.devcoop.kiosk.global.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND("찾을 수 없는 사용자입니다", 404),

    // Server
    INTERNAL_SERVER_ERROR("서버 에러가 발생하였습니다", 500),

    // Auth
    BARCODE_NOT_VALID("존재하지 않는 바코드입니다", 404),

    // Receipt
    RECEIPT_SAVE_FAILED("영수증 저장 중 오류가 발생하였습니다", 500),

    // Item
    ITEM_NOT_FOUND("상품을 찾을 수 없습니다", 404),

    // Payment
    PAYMENT_FAILED("결제 처리에 실패했습니다", 400),
    TRANSACTION_IN_PROGRESS("이미 진행 중인 거래가 있습니다", 409),
    INSUFFICIENT_POINTS("포인트가 부족합니다", 400),
    CARD_PAYMENT_FAILED("카드 결제에 실패했습니다", 400),
    PAYMENT_TIMEOUT("결제 시간이 초과되었습니다. 다시 시도해주세요.", 408),
    INVALID_PAYMENT_REQUEST("잘못된 결제 요청입니다.", 400),
    PAYMENT_TYPE_INVALID("지원하지 않는 결제 유형입니다.", 400),

    // Point & Charge
    POINT_CHARGE_FAILED("포인트 충전에 실패했습니다", 400),
    POINT_DEDUCTION_FAILED("포인트 차감에 실패했습니다", 400),
    INVALID_CHARGE_AMOUNT("유효하지 않은 충전 금액입니다", 400),
    CHARGE_ITEM_NOT_FOUND("충전 상품을 찾을 수 없습니다", 404),
    CHARGE_LOG_SAVE_FAILED("충전 기록 저장에 실패했습니다", 500),
    INVALID_ITEM_ID_FORMAT("잘못된 상품 ID 형식입니다.", 400);

    private final String message;
    private final int status;


    public String getMessage() {
        return message;
    }
}
