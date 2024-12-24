package com.devcoop.kiosk.domain.chargelog.presentation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devcoop.kiosk.domain.chargelog.ChargeLog;

@Repository
public interface ChargeLogRepository extends JpaRepository<ChargeLog, Long> {
}