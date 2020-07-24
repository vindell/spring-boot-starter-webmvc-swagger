package io.springfox.spring.boot;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class Swagger2UiWebMvcConfigurer implements WebMvcConfigurer {
	  private final String baseUrl;

	  public Swagger2UiWebMvcConfigurer(String baseUrl) {
	    this.baseUrl = baseUrl;
	  }

	  @Override
	  public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    String baseUrl = StringUtils.trimTrailingCharacter(this.baseUrl, '/');
	    registry.
	        addResourceHandler(baseUrl + "/swagger-ui/**")
	        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
	        .resourceChain(false);
	  }

	  @Override
	  public void addViewControllers(ViewControllerRegistry registry) {
	    registry.addViewController(baseUrl + "/swagger-ui/")
	        .setViewName("forward:" + baseUrl + "/swagger-ui/index.html");
	  }
	}