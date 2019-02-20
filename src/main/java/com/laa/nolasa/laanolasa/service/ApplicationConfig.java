package com.laa.nolasa.laanolasa.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfig {

  @Value("${client.default-uri}")
  private String defaultUri;

  @Bean
  @Primary
  public AWSCredentialsProvider awsCredentialsProvider() {
    return new DefaultAWSCredentialsProviderChain();
  }

  @Bean
  Jaxb2Marshaller jaxb2Marshaller() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setContextPath("uk.gov.justice._2013._11.magistrates");
    NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
      public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if ("http://www.justice.gov.uk/2013/11/magistrates".equals(namespaceUri) && !requirePrefix)
          return "";
        return "ns";
      }
    };
    Map<String, Object> propertiesMap = new HashMap<>();
    propertiesMap.put("jaxb.formatted.output", true);
    propertiesMap.put("com.sun.xml.bind.namespacePrefixMapper", mapper);
    jaxb2Marshaller.setMarshallerProperties(propertiesMap);

    return jaxb2Marshaller;
  }

  @Bean
  public WebServiceTemplate webServiceTemplate() {
    WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
    webServiceTemplate.setMarshaller(jaxb2Marshaller());
    webServiceTemplate.setUnmarshaller(jaxb2Marshaller());
    webServiceTemplate.setDefaultUri(defaultUri);
    webServiceTemplate.setMessageFactory(soapMessageFactory());
    return webServiceTemplate;
  }

  @Bean
  public SaajSoapMessageFactory soapMessageFactory() {
      SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory();
      saajSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_12);
      return saajSoapMessageFactory;
  }

  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}