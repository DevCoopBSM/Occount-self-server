package com.devcoop.kiosk.domain.item.presentation;

import com.devcoop.kiosk.domain.item.presentation.dto.NonBarcodeItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NonBarcodeItemController {
    private final NonBarcodeItemService nonBarcodeItemService;

    @GetMapping("/non-barcode-item")
    public List<NonBarcodeItemResponse> getList() {
        return nonBarcodeItemService.getNonBarcodeItems();
    }
}
