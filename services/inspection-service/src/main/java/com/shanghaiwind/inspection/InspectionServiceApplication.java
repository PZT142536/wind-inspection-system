package com.shanghaiwind.inspection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shanghaiwind.inspection", "com.shanghaiwind.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.shanghaiwind.inspection.mapper")
@EnableAsync
@EnableScheduling
public class InspectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InspectionServiceApplication.class, args);
    }
}
