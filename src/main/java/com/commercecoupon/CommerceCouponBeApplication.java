package com.commercecoupon;

import com.commercecoupon.repository.UserRepository;
import com.commercecoupon.repository.CouponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CommerceCouponBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommerceCouponBeApplication.class, args);
    }

    /**
     * 🔍 데이터베이스 진단용 CommandLineRunner
     * 애플리케이션 시작 후 자동으로 실행되어 데이터베이스 상태를 확인합니다.
     */
    @Bean
    public CommandLineRunner diagnosticRunner(UserRepository userRepository,
                                              CouponRepository couponRepository) {
        return args -> {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🔍 데이터베이스 진단 시작");
            System.out.println("=".repeat(50));

            try {
                // 1. Users 테이블 확인
                long userCount = userRepository.count();
                System.out.println("✅ Users 테이블 접근 성공!");
                System.out.println("📊 현재 사용자 수: " + userCount);

                // 2. Coupons 테이블 확인
                long couponCount = couponRepository.count();
                System.out.println("✅ Coupons 테이블 접근 성공!");
                System.out.println("📊 현재 쿠폰 수: " + couponCount);

                System.out.println("=".repeat(50));
                System.out.println("🎯 데이터베이스 준비 완료!");
                System.out.println("💡 이제 H2 Console에서 데이터를 확인할 수 있습니다.");
                System.out.println("🌐 H2 Console: http://localhost:8080/h2-console");
                System.out.println("🔗 JDBC URL: jdbc:h2:mem:testdb");
                System.out.println("=".repeat(50) + "\n");

            } catch (Exception e) {
                System.out.println("❌ 데이터베이스 오류 발생:");
                System.out.println("📋 오류 메시지: " + e.getMessage());
                System.out.println("🔧 오류 타입: " + e.getClass().getSimpleName());
                e.printStackTrace();
                System.out.println("=".repeat(50));
            }
        };
    }
}