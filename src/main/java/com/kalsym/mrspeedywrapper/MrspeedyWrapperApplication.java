package com.kalsym.mrspeedywrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.Entity;

@SpringBootApplication
@EntityScan(basePackages = {"com.kalsym.mrspeedywrapper", "com.kalsym.parentwrapper.models"})
public class MrspeedyWrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(MrspeedyWrapperApplication.class, args);
	}

}
