package org.uni.santo.tomas.helper.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    private final DataSource dataSource;

    public DatabaseHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            // Si la conexión es exitosa, el estado es UP
            return HealthCheckResponse.up("Database connection UP");
        } catch (SQLException e) {
            // Si falla, el estado es DOWN
            return HealthCheckResponse.down("Database connection DOWN");
        }
    }
}
