package io.springfox.spring.boot;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.WebJarsResourceResolver;

public class Swagger2UiWebMvcConfigurer implements WebMvcConfigurer {

	private final String META_INF_RESOURCES = "classpath:/META-INF/resources/";
	private final String META_INF_WEBJAR_RESOURCES = META_INF_RESOURCES + "webjars/";
	private final String META_INF_SPRINGFOX_SWAGGER_UI_RESOURCES = META_INF_WEBJAR_RESOURCES + "springfox-swagger-ui/";
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui/**").addResourceLocations(META_INF_SPRINGFOX_SWAGGER_UI_RESOURCES).resourceChain(false);
		registry.addResourceHandler("/doc.html**").addResourceLocations(META_INF_RESOURCES).resourceChain(false);;
		if(!registry.hasMappingForPattern("/webjars/**")) {
			registry.addResourceHandler("/webjars/**").addResourceLocations(META_INF_WEBJAR_RESOURCES)
				.resourceChain(false).addResolver(new WebJarsResourceResolver());
		}
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/swagger-ui/").setViewName("forward:/swagger-ui/index.html");
	}
	
}