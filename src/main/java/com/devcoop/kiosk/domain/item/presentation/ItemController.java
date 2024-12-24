package com.devcoop.kiosk.domain.item.presentation;

import com.devcoop.kiosk.domain.item.presentation.dto.ItemResponse;
import com.devcoop.kiosk.domain.item.service.ItemService;
import com.devcoop.kiosk.global.exception.GlobalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j @Tag(name = "item", description = "Item API")
public class ItemController {
  private final ItemService itemSelectService;
  @GetMapping("/item")
  @Operation(summary = "get item info", description = "아이템 코드로 상품 정보 조회")
  public List<ItemResponse> getItemByCode(
          @Parameter(description = "상품 코드 목록")
          @RequestParam List<String> itemCode) throws GlobalException {
    log.info("itemCode = {}", itemCode);
    List<ItemResponse> itemResponses = itemSelectService.get(itemCode);
    return itemResponses;
  }

  @GetMapping("/items")
  @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다")
  public ResponseEntity<List<ItemResponse>> getItems() {
    List<ItemResponse> items = itemSelectService.getAllItems();
    return ResponseEntity.ok(items);
  }

  @GetMapping("/items/no-barcode")
  @Operation(summary = "코드 미등록 상품 조회", description = "상품 코드가 없는 상품 목록을 조회합니다")
  public ResponseEntity<List<ItemResponse>> getItemsWithoutBarcode() {
    List<ItemResponse> items = itemSelectService.getItemsWithoutBarcode();
    return ResponseEntity.ok(items);
  }
}
