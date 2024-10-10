package com.devcoop.kiosk.domain.paylog.presentation.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record KioskRequest(
        List<KioskItemInfo> items,
        String userId // userCode -> userId
) {

}
