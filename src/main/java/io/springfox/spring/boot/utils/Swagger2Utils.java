/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.springfox.spring.boot.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import io.springfox.spring.boot.Swagger2WebMvcProperties;
import io.springfox.spring.boot.model.DocketInfo;
import io.springfox.spring.boot.model.GlobalOperationParameter;
import io.springfox.spring.boot.model.GlobalResponseMessage;
import io.springfox.spring.boot.model.GlobalResponseMessageBody;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.Response;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;

/*
 * TODO
 * @author 		： <a href="https://github.com/hiwepy">wandl</a>
 */
public class Swagger2Utils {

	public static ApiInfo apiInfo(Swagger2WebMvcProperties swaggerProperties) {
 		return new ApiInfoBuilder()
				.title(swaggerProperties.getTitle())
				.description(swaggerProperties.getDescription())
				.version(swaggerProperties.getVersion())
				.license(swaggerProperties.getLicense())
				.licenseUrl(swaggerProperties.getLicenseUrl())
				.contact(new Contact(swaggerProperties.getContact().getName(), swaggerProperties.getContact().getUrl(), swaggerProperties.getContact().getEmail()))
				.termsOfServiceUrl(swaggerProperties.getTermsOfServiceUrl())
				.build();
	}
	
	public static ApiInfo apiInfo(DocketInfo docketInfo, Swagger2WebMvcProperties swaggerProperties) {
 		return new ApiInfoBuilder()
				.title(StringUtils.hasText(docketInfo.getTitle()) ? swaggerProperties.getTitle() : docketInfo.getTitle())
				.description(StringUtils.hasText(docketInfo.getDescription()) ? swaggerProperties.getDescription() : docketInfo.getDescription())
				.version(StringUtils.hasText(docketInfo.getVersion()) ? swaggerProperties.getVersion() : docketInfo.getVersion())
				.license(StringUtils.hasText(docketInfo.getLicense()) ? swaggerProperties.getLicense() : docketInfo.getLicense())
				.licenseUrl(StringUtils.hasText(docketInfo.getLicenseUrl()) ? swaggerProperties.getLicenseUrl() : docketInfo.getLicenseUrl())
				.contact(new Contact(
						StringUtils.hasText(docketInfo.getContact().getName()) ? swaggerProperties.getContact().getName() : docketInfo.getContact().getName(),
						StringUtils.hasText(docketInfo.getContact().getUrl()) ? swaggerProperties.getContact().getUrl() : docketInfo.getContact().getUrl(),
						StringUtils.hasText(docketInfo.getContact().getEmail()) ? swaggerProperties.getContact().getEmail() : docketInfo.getContact().getEmail()))
				.termsOfServiceUrl(
						StringUtils.hasText(docketInfo.getTermsOfServiceUrl()) ? swaggerProperties.getTermsOfServiceUrl() : docketInfo.getTermsOfServiceUrl())
				.build();
	}
	
	public static Docket defaultDocket(Swagger2WebMvcProperties swaggerProperties) {
		
		Docket docketForBuilder = new Docket(DocumentationType.SWAGGER_2)
				.host(swaggerProperties.getHost())
				.apiInfo(apiInfo(swaggerProperties))
				.securityContexts(Collections.singletonList(securityContext(swaggerProperties)))
				.globalRequestParameters(buildGlobalOperationParametersFromSwagger2WebMvcProperties( swaggerProperties.getGlobalOperationParameters()));

		switch (swaggerProperties.getAuthorization().getType()) {
			case APIKEY:{
				docketForBuilder.securitySchemes(Collections.singletonList(apiKey(swaggerProperties)));
			};break;
			case BASICAUTH:{
				docketForBuilder.securitySchemes(Collections.singletonList(basicAuth(swaggerProperties)));
			};break;
			default:{
				
			};break;
		}

		// 全局响应消息
		if (!swaggerProperties.isApplyDefaultResponseMessages()) {
			buildGlobalResponseMessage(swaggerProperties, docketForBuilder);
		}
		
		// RequestHandlerSelectors.basePackage(basePackage)
		// PathSelectors.ant(antPattern)
		
		Docket docket = docketForBuilder.select()
				.apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
				.paths(StringUtils.hasText(swaggerProperties.getBasePathPattern()) ? PathSelectors.ant(swaggerProperties.getBasePathPattern()) : PathSelectors.any())
				.build();

		/* ignoredParameterTypes **/
		Class<?>[] array = new Class[swaggerProperties.getIgnoredParameterTypes().size()];
		Class<?>[] ignoredParameterTypes = swaggerProperties.getIgnoredParameterTypes().toArray(array);
		docket.ignoredParameterTypes(ignoredParameterTypes)
				.enableUrlTemplating(swaggerProperties.isEnableUrlTemplating())
				.forCodeGeneration(swaggerProperties.isForCodeGen());

		return docket;
		
	}
	
	public static Docket groupDocket(DocketInfo docketInfo, Swagger2WebMvcProperties swaggerProperties) {

		Docket docketForBuilder = new Docket(DocumentationType.SWAGGER_2)
				.host(swaggerProperties.getHost())
				.apiInfo(apiInfo(docketInfo, swaggerProperties))
				.securityContexts(Collections.singletonList(securityContext(swaggerProperties)))
				.globalRequestParameters(assemblyGlobalOperationParameters(swaggerProperties.getGlobalOperationParameters(), docketInfo.getGlobalOperationParameters()));

		switch (swaggerProperties.getAuthorization().getType()) {
			case APIKEY:{
				docketForBuilder.securitySchemes(Collections.singletonList(apiKey(swaggerProperties)));
			};break;
			case BASICAUTH:{
				docketForBuilder.securitySchemes(Collections.singletonList(basicAuth(swaggerProperties)));
			};break;
			default:{
				
			};break;
		}

		// 全局响应消息
		if (!swaggerProperties.isApplyDefaultResponseMessages()) {
			buildGlobalResponseMessage(swaggerProperties, docketForBuilder);
		}
		
		Docket docket = docketForBuilder.groupName(docketInfo.getName()).select()
				.apis(RequestHandlerSelectors.basePackage(docketInfo.getBasePackage()))
				.paths(StringUtils.hasText(docketInfo.getBasePathPattern()) ? PathSelectors.ant(docketInfo.getBasePathPattern()) : PathSelectors.any())
				.build();

		/* ignoredParameterTypes **/
		Class<?>[] array = new Class[docketInfo.getIgnoredParameterTypes().size()];
		Class<?>[] ignoredParameterTypes = docketInfo.getIgnoredParameterTypes().toArray(array);
		docket.ignoredParameterTypes(ignoredParameterTypes)
				.enableUrlTemplating(docketInfo.isEnableUrlTemplating())
				.forCodeGeneration(docketInfo.isForCodeGen());
		
		return docket;
	}
	

	/*
	 * 配置基于 ApiKey 的鉴权对象
	 *
	 * @return ApiKey
	 */
	public static ApiKey apiKey(Swagger2WebMvcProperties swaggerProperties) {
		return new ApiKey(swaggerProperties.getAuthorization().getName(),
				swaggerProperties.getAuthorization().getKeyName(), ApiKeyVehicle.HEADER.getValue());
	}

	/*
	 * 配置基于 BasicAuth 的鉴权对象
	 *
	 * @return BasicAuth
	 */
	public static BasicAuth basicAuth(Swagger2WebMvcProperties swaggerProperties) {
		return new BasicAuth(swaggerProperties.getAuthorization().getName());
	}

	/*
	 * 配置默认的全局鉴权策略的开关，以及通过正则表达式进行匹配；默认 ^.*$ 匹配所有URL 其中 securityReferences 为配置启用的鉴权策略
	 *
	 * @return SecurityContext
	 */
	public static SecurityContext securityContext(Swagger2WebMvcProperties swaggerProperties) {
		Predicate<String> predicate = PathSelectors.regex(swaggerProperties.getAuthorization().getAuthRegex());
		return SecurityContext.builder().securityReferences(defaultAuth(swaggerProperties))
				.operationSelector((ctx) -> {
					return predicate.test(ctx.requestMappingPattern());
				}).build();
	}

	/*
	 * 配置默认的全局鉴权策略；其中返回的 SecurityReference 中，reference
	 * 即为ApiKey对象里面的name，保持一致才能开启全局鉴权
	 *
	 * @return List<SecurityReference>
	 */
	public static List<SecurityReference> defaultAuth(Swagger2WebMvcProperties swaggerProperties) {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Collections.singletonList(SecurityReference.builder()
				.reference(swaggerProperties.getAuthorization().getName()).scopes(authorizationScopes).build());
	}

	public static List<RequestParameter> buildGlobalOperationParametersFromSwagger2WebMvcProperties(
			List<GlobalOperationParameter> globalRequestParameters) {
		List<RequestParameter> parameters = new ArrayList<RequestParameter>();

		if (Objects.isNull(globalRequestParameters)) {
			return parameters;
		}
		for (GlobalOperationParameter globalOperationParameter : globalRequestParameters) {
			parameters.add(new RequestParameterBuilder().name(globalOperationParameter.getName())
					.description(globalOperationParameter.getDescription())
					//.modelRef(new ModelRef(globalOperationParameter.getModelRef()))
					//.parameterType(globalOperationParameter.getParameterType())
					.required(Boolean.parseBoolean(globalOperationParameter.getRequired())).build());
		}
		return parameters;
	}

	/*
	 * 局部参数按照name覆盖局部参数
	 *
	 * @param globalRequestParameters  全局参数
	 * @param docketOperationParameters  当前分组参数
	 * @return List<RequestParameter> 
	 */
	public static List<RequestParameter> assemblyGlobalOperationParameters(List<GlobalOperationParameter> globalRequestParameters,
			List<GlobalOperationParameter> docketOperationParameters) {

		if (Objects.isNull(docketOperationParameters) || docketOperationParameters.isEmpty()) {
			return buildGlobalOperationParametersFromSwagger2WebMvcProperties(globalRequestParameters);
		}

		Set<String> docketNames = docketOperationParameters.stream().map(GlobalOperationParameter::getName)
				.collect(Collectors.toSet());

		List<GlobalOperationParameter> resultOperationParameters = new ArrayList<GlobalOperationParameter>();

		if (Objects.nonNull(globalRequestParameters)) {
			for (GlobalOperationParameter parameter : globalRequestParameters) {
				if (!docketNames.contains(parameter.getName())) {
					resultOperationParameters.add(parameter);
				}
			}
		}

		resultOperationParameters.addAll(docketOperationParameters);
		return buildGlobalOperationParametersFromSwagger2WebMvcProperties(resultOperationParameters);
	}

	/*
	 * 设置全局响应消息
	 *
	 * @param swaggerProperties swaggerProperties 支持
	 *                          POST,GET,PUT,PATCH,DELETE,HEAD,OPTIONS,TRACE
	 * @param docketForBuilder  swagger docket builder
	 */
	public static void buildGlobalResponseMessage(Swagger2WebMvcProperties swaggerProperties, Docket docketForBuilder) {

		GlobalResponseMessage globalResponseMessages = swaggerProperties.getGlobalResponseMessage();

		/* POST,GET,PUT,PATCH,DELETE,HEAD,OPTIONS,TRACE 响应消息体 **/
		List<Response> postResponseMessages = getResponseMessageList(globalResponseMessages.getPost());
		List<Response> getResponseMessages = getResponseMessageList(globalResponseMessages.getGet());
		List<Response> putResponseMessages = getResponseMessageList(globalResponseMessages.getPut());
		List<Response> patchResponseMessages = getResponseMessageList(globalResponseMessages.getPatch());
		List<Response> deleteResponseMessages = getResponseMessageList(globalResponseMessages.getDelete());
		List<Response> headResponseMessages = getResponseMessageList(globalResponseMessages.getHead());
		List<Response> optionsResponseMessages = getResponseMessageList(globalResponseMessages.getOptions());
		List<Response> trackResponseMessages = getResponseMessageList(globalResponseMessages.getTrace());

		docketForBuilder.useDefaultResponseMessages(swaggerProperties.isApplyDefaultResponseMessages())
				.globalResponses(HttpMethod.POST, postResponseMessages)
				.globalResponses(HttpMethod.GET, getResponseMessages)
				.globalResponses(HttpMethod.PUT, putResponseMessages)
				.globalResponses(HttpMethod.PATCH, patchResponseMessages)
				.globalResponses(HttpMethod.DELETE, deleteResponseMessages)
				.globalResponses(HttpMethod.HEAD, headResponseMessages)
				.globalResponses(HttpMethod.OPTIONS, optionsResponseMessages)
				.globalResponses(HttpMethod.TRACE, trackResponseMessages);
	}

	/**
	 * 获取返回消息体列表
	 * @param globalResponseMessageBodyList 全局Code消息返回集合
	 * @return
	 */
	private static List<Response> getResponseMessageList(
			List<GlobalResponseMessageBody> globalResponseMessageBodyList) {
		List<Response> responseMessages = new ArrayList<>();
		for (GlobalResponseMessageBody globalResponseMessageBody : globalResponseMessageBodyList) {
			ResponseBuilder responseMessageBuilder = new ResponseBuilder()
						.code(globalResponseMessageBody.getCode())
						.description(globalResponseMessageBody.getMessage());

			if (!StringUtils.isEmpty(globalResponseMessageBody.getModelRef())) {
				//responseMessageBuilder.responseModel(new ModelRef(globalResponseMessageBody.getModelRef()));
			}
			responseMessages.add(responseMessageBuilder.build());
		}

		return responseMessages;
	}
	
}
