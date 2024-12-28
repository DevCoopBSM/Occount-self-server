package com.devcoop.kiosk.domain.item.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devcoop.kiosk.domain.item.Item;
import com.devcoop.kiosk.domain.item.presentation.dto.EventItemResponse;
import com.devcoop.kiosk.domain.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EventItemService {
    private final ItemRepository itemRepository;

    @Value("${db.imageUrl}")
    private String defaultUrl;

    @Transactional(readOnly = true)
    public List<EventItemResponse> read() {
        List<Item> items = itemRepository.findAllByEvent();
        return items.stream()
                .map(item -> EventItemResponse.builder()
                        .itemCode(item.getItemCode()) // barcode를 itemCode로 변경
                        .itemName(item.getItemName())
                        .itemPrice(item.getItemPrice())
                        .event(item.getEvent().name())
                        .image(item.getItemImage() != null ? item.getItemImage() : defaultUrl)
                        .build())
                .collect(Collectors.toList());
    }
}
