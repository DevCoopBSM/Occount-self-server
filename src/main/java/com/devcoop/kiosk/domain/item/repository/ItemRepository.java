package com.devcoop.kiosk.domain.item.repository;

import com.devcoop.kiosk.domain.item.Item;
import com.devcoop.kiosk.domain.item.presentation.dto.NonBarcodeItemResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    // 상품 이름으로 조회
    Item findByItemName(String itemName);

    // 상품 코드로 조회 (기존 barcode를 itemCode로 변경)
    Item findByItemCode(String itemCode);

    // 모든 상품 이름 조회
    @Query("select i.itemName from Item i")
    List<String> findNameAll();

    // 이벤트가 존재하는 모든 상품 조회
    @Query("SELECT i FROM Item i WHERE i.event <> 'NONE'")
    List<Item> findAllByEvent();

    @Query("SELECT new com.devcoop.kiosk.domain.item.presentation.dto.NonBarcodeItemResponse(i.itemName, i.itemPrice) " +
            "FROM Item i " +
            "WHERE i.itemCode = 'None' or i.itemCode = ''")
    List<NonBarcodeItemResponse> findAllByItemCode();
}
