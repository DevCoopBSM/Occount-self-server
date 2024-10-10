package com.devcoop.kiosk.domain.item.service;

import java.util.List;
import java.util.Random;

import com.devcoop.kiosk.domain.item.repository.ItemRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemSuggestService {
  private final ItemRepository itemRepository;

  @Transactional(readOnly = true)
  public String read() {
    Random random = new Random();
    List<String> items = itemRepository.findNameAll(); // 모든 아이템을 가져옴

    int randomIndex = random.nextInt(items.size()); // 무작위 인덱스 생성

    return items.get(randomIndex); // 무작위 아이템 반환
  }
}
