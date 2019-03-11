package com.laa.nolasa.laanolasa.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.justice._2013._11.magistrates.LIBRAServicePortType;

import java.util.List;

@Configuration
public class ApplicationConfig {

    @Value("${client.default-uri}")
    private String defaultUri;

    @Autowired
    private Bus cxfBus;

    @Bean
    public Bus bus() {
        cxfBus.setFeatures(List.of(loggingFeature()));
        return cxfBus;
    }

    @Bean
    public LoggingFeature loggingFeature() {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        loggingFeature.setLimit(1024);

        return loggingFeature;
    }
    @Bean(name = "infoxProxy")
    public LIBRAServicePortType infoxProxy() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(LIBRAServicePortType.class);
        factory.setAddress(defaultUri);
        factory.setBindingId("http://www.w3.org/2003/05/soap/bindings/HTTP/");
        return (LIBRAServicePortType) factory.create();
    }

    @Bean
    @Primary
    public AWSCredentialsProvider awsCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

}