package com.nutriplan.api;

import org.springframework.boot.SpringApplication;

public class TestNutriplanApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(NutriplanApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
