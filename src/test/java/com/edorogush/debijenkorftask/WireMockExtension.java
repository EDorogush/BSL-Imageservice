package com.edorogush.debijenkorftask;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

  private WireMockServer server;

  @Override
  public void beforeAll(ExtensionContext context) {
    server = new WireMockServer(wireMockConfig().dynamicPort().notifier(new ConsoleNotifier(true)));
    server.start();
    WireMock.configureFor(server.port());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    server.stop();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    server.resetAll();
  }

  public int getPort() {
    return server.port();
  }
}
