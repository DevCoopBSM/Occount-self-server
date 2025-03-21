package com.devcoop.kiosk.domain.user;

import com.devcoop.kiosk.domain.user.types.Role;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "common_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    @Column(name = "userNumber")
    private String userNumber; // Primary key, 조합원 번호

    @Column(name = "userCode")
    private String userCode; // 학생증 바코드

    @Column(name = "userName")
    private String userName; // 유저 이름

    @Column(name = "userEmail")
    private String userEmail; // 유저 이메일

    @Column(name = "userPassword")
    private String userPassword; // 유저 비밀번호

    @Column(name = "userPin")
    private String userPin; // 유저 셀프 계산대 비밀번호

    @Setter @Column(name = "userPoint")
    private int userPoint; // 유저 포인트

    @Column(name = "userFingerPrint")
    private String userFingerPrint; // 유저 지문 정보 (추가된 필드)

    @Enumerated(EnumType.STRING)
    private Role roles; // 사용자 인증용 Role, == userType

    // 생성자
    @Builder
    public User(
            String userNumber, String userCode, String userName,
            String userEmail, String userPassword, String userPin,
            int userPoint, String userFingerPrint, Role roles
    ) {
        this.userNumber = userNumber;
        this.userCode = userCode;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userPin = userPin;
        this.userPoint = userPoint;
        this.userFingerPrint = userFingerPrint;
        this.roles = roles;
    }

    public void changePassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void changePin(String userPin) {
        this.userPin = userPin;
    }

    public void chargePoint(int chargedPoint) {
        this.userPoint += chargedPoint;
    }

    public void payPoint(int payedPoint) {
        this.userPoint -= payedPoint;
    }
}
