package com.followinsider;

import com.followinsider.eb.ElasticBeanstalkProps;
import com.followinsider.eb.ElasticBeanstalkStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class FollowInsiderApp {

    public static void main(final String[] args) throws Exception {
        App app = new App();

        Environment env = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();

        StackProps stackProps = StackProps.builder().env(env).build();

        new ElasticBeanstalkStack(app, ElasticBeanstalkProps.builder()
                .stackProps(stackProps)
                .instanceType("t2.micro")
                .healthUrl("/actuator/health")
                .assetPath("../fi-core/build/libs/fi-core.jar")
                .stackName("64bit Amazon Linux 2023 v4.1.1 running Corretto 17")
                .appName("follow-insider")
                .pre("follow-insider")
                .port("5000")
                .https(false)
                .build());

        app.synth();
    }

}