package com.devcoop.kiosk.domain.item.presentation;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.item.presentation.dto.NonBarcodeItemResponse;
import com.devcoop.kiosk.domain.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NonBarcodeItemService {
    private final ItemRepository itemRepository;

    @Transactional(
            readOnly = true,
            rollbackFor = Exception.class
    )
    public List<NonBarcodeItemResponse> getNonBarcodeItems() {

        return itemRepository.findAllByItemCode();
    }
}
