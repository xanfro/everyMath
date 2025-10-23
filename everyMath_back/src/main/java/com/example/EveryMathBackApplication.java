package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EveryMath后端应用启动类
 */
@SpringBootApplication
@MapperScan("com.example.mapper")
public class EveryMathBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(EveryMathBackApplication.class, args);
	}

}
