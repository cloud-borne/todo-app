package com.myorg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.stratospheric.cdk.ApplicationEnvironment;
import dev.stratospheric.cdk.Network;
import dev.stratospheric.cdk.Service;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import static com.myorg.Validations.requireNonEmpty;

public class ServiceApp {

  public static void main(final String[] args) {
    App app = new App();

    String environmentName = (String) app.getNode().tryGetContext("environmentName");
    requireNonEmpty(environmentName, "context variable 'environmentName' must not be null");

    String applicationName = (String) app.getNode().tryGetContext("applicationName");
    requireNonEmpty(applicationName, "context variable 'applicationName' must not be null");

    String accountId = (String) app.getNode().tryGetContext("accountId");
    requireNonEmpty(accountId, "context variable 'accountId' must not be null");

    String springProfile = (String) app.getNode().tryGetContext("springProfile");
    requireNonEmpty(springProfile, "context variable 'springProfile' must not be null");

    String dockerRepositoryName = (String) app.getNode().tryGetContext("dockerRepositoryName");
    requireNonEmpty(dockerRepositoryName, "context variable 'dockerRepositoryName' must not be null");

    String dockerImageTag = (String) app.getNode().tryGetContext("dockerImageTag");
    requireNonEmpty(dockerImageTag, "context variable 'dockerImageTag' must not be null");

    String region = (String) app.getNode().tryGetContext("region");
    requireNonEmpty(region, "context variable 'region' must not be null");

    Environment awsEnvironment = makeEnv(accountId, region);

    ApplicationEnvironment applicationEnvironment = new ApplicationEnvironment(
      applicationName,
      environmentName
    );

    Stack serviceStack = new Stack(app, "ServiceStack", StackProps.builder()
      .stackName(applicationEnvironment.prefix("Service"))
      .env(awsEnvironment)
      .build());

    Service service = new Service(
      serviceStack,
      "Service",
      awsEnvironment,
      applicationEnvironment,
      new Service.ServiceInputParameters(
        new Service.DockerImageSource(dockerRepositoryName, dockerImageTag),
        environmentVariables(
          serviceStack,
          springProfile))
        .withStickySessionsEnabled(true)
        .withHealthCheckIntervalSeconds(30), // needs to be long enough to allow for slow start up with low-end computing instances

      Network.getOutputParametersFromParameterStore(serviceStack, applicationEnvironment.getEnvironmentName()));

    app.synth();
  }

  static Map<String, String> environmentVariables(String springProfile) {
    Map<String, String> vars = new HashMap<>();
    vars.put("SPRING_PROFILES_ACTIVE", springProfile);
    return vars;
  }

  static Environment makeEnv(String account, String region) {
    return Environment.builder()
      .account(account)
      .region(region)
      .build();
  }
}
