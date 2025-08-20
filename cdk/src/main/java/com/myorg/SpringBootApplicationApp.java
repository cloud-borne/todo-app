package com.myorg;

import java.util.Objects;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import dev.stratospheric.cdk.SpringBootApplicationStack;

public class SpringBootApplicationApp {
    public static void main(final String[] args) {
        App app = new App();

        String accountId = (String) app.getNode().tryGetContext("accountId");
        Objects.requireNonNull(accountId, "accountId must not be null");
        String region = (String) app.getNode().tryGetContext("region");
        Objects.requireNonNull(region, "context variable 'region' must not be null");


        new SpringBootApplicationStack( app,
                                        "SpringBootApplication",
                                        makeEnv(accountId, region),
                                        "docker.io/avijitchatterjee/todo-application-v1:latest");
                                        app.synth();
        }
        static Environment makeEnv(String account, String region) {
                 return Environment.builder()
                 .account(account)
                 .region(region)
                 .build();
        }
}

