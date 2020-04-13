package de.sakpaas.backend;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresqlContainer extends PostgreSQLContainer<PostgresqlContainer> {
  private static final String IMAGE_VERSION = "postgres:latest";
  private static PostgresqlContainer container;

  private PostgresqlContainer() {
    super(IMAGE_VERSION);
  }

  public static PostgresqlContainer getInstance() {
    if (container == null) {
      container = new PostgresqlContainer();
      container.start();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("DB_URL", container.getJdbcUrl());
    System.setProperty("DB_USERNAME", container.getUsername());
    System.setProperty("DB_PASSWORD", container.getPassword());
  }

  @Override
  public void stop() {
    //do nothing, JVM handles shut down
  }
}
