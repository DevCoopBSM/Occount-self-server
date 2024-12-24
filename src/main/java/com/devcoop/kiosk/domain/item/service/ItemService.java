package com.devcoop.kiosk.domain.item.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.item.Item;
import com.devcoop.kiosk.domain.item.presentation.dto.ItemResponse;
import com.devcoop.kiosk.domain.item.repository.ItemRepository;
import com.devcoop.kiosk.global.exception.GlobalException;
import com.devcoop.kiosk.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<ItemResponse> get(List<String> itemCodes) throws GlobalException {
        List<ItemResponse> itemResponses = new ArrayList<>();

        for (String itemCode : itemCodes) {
            log.info("Service에서 itemCode = {}", itemCode);
            Item item = itemRepository.findByItemCode(itemCode);
            log.info("item = {}", item);

            if (item == null) {
                throw new GlobalException(ErrorCode.BARCODE_NOT_VALID);
            }

            ItemResponse itemResponse = ItemResponse.builder()
                    .itemId(item.getItemId())
                    .itemCode(item.getItemCode())
                    .itemName(item.getItemName())
                    .itemPrice(item.getItemPrice())
                    .eventStatus(item.getEvent())
                    .itemCategory(item.getItemCategory())
                    .build();

            itemResponses.add(itemResponse);
        }
        log.info("{}",itemResponses);
        return itemResponses;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        List<Item> items = itemRepository.findAll();
        
        return items.stream()
            .map(item -> ItemResponse.builder()
                    .itemId(item.getItemId())
                    .itemCode(item.getItemCode())
                    .itemName(item.getItemName())
                    .itemPrice(item.getItemPrice())
                    .eventStatus(item.getEvent())
                    .itemCategory(item.getItemCategory())
                    .build())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsWithoutBarcode() {
        List<Item> items = itemRepository.findAllByItemCodeIsNoneOrEmpty();
        
        return items.stream()
            .map(item -> ItemResponse.builder()
                    .itemId(item.getItemId())
                    .itemCode(item.getItemCode())
                    .itemName(item.getItemName())
                    .itemPrice(item.getItemPrice())
                    .eventStatus(item.getEvent())
                    .itemCategory(item.getItemCategory())
                    .build())
            .collect(Collectors.toList());
    }
}
