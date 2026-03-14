package com.nutriplan.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = { 
    org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiChatAutoConfiguration.class 
})
public class NutriplanApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NutriplanApiApplication.class, args);
	}

}
