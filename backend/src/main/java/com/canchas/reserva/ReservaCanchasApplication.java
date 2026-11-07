package com.canchas.reserva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
public class ReservaCanchasApplication {

    private static final Logger log = LoggerFactory.getLogger(ReservaCanchasApplication.class);

    static {
        asegurarMotorMySQLActivo();
    }

    public static void main(String[] args) {
        asegurarMotorMySQLActivo();
        SpringApplication.run(ReservaCanchasApplication.class, args);
    }

    public static synchronized void asegurarMotorMySQLActivo() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 3306), 600);
            return;
        } catch (Exception ignored) {
            // El puerto 3306 no está escuchando aún; iniciar instancia local de MySQL 8.4 si está instalada
        }

        File mysqld = new File("C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqld.exe");
        if (!mysqld.exists()) {
            return;
        }

        try {
            File dataDir = new File("data/mysql_db").getAbsoluteFile();
            if (!new File(dataDir, "mysql").exists()) {
                dataDir.mkdirs();
                log.info("[MySQL 8.4] Inicializando directorio de datos en: {}", dataDir);
                new ProcessBuilder(mysqld.getAbsolutePath(), "--initialize-insecure", "--datadir=" + dataDir.getAbsolutePath())
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start()
                        .waitFor();
            }

            log.info("[MySQL 8.4] Iniciando motor MySQL en 127.0.0.1:3306...");
            new ProcessBuilder(mysqld.getAbsolutePath(), "--datadir=" + dataDir.getAbsolutePath(), "--port=3306", "--bind-address=127.0.0.1")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();

            // Esperar hasta que el puerto 3306 acepte conexiones
            for (int i = 0; i < 20; i++) {
                Thread.sleep(500);
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress("127.0.0.1", 3306), 400);
                    log.info("[MySQL 8.4] Motor activo y aceptando conexiones en 127.0.0.1:3306.");
                    break;
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("[MySQL 8.4] No se pudo auto-iniciar mysqld: {}", e.getMessage());
        }
    }
}
