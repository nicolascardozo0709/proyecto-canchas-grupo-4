package com.canchas.reserva.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gestor automático de Túnel Público Cloudflare.
 * Se activa automáticamente al iniciar el servidor en Visual Studio Code,
 * terminal o cualquier compilador, exponiendo la aplicación a internet sin localhost.
 */
@Component
public class PublicTunnelManager {

    private static final Logger log = LoggerFactory.getLogger(PublicTunnelManager.class);
    private static final Pattern TUNNEL_URL_PATTERN = Pattern.compile("https://[a-zA-Z0-9-]+\\.trycloudflare\\.com");

    @Value("${server.port:8080}")
    private int serverPort;

    private Process tunnelProcess;
    private static String publicUrl = null;

    @EventListener(ApplicationReadyEvent.class)
    public void startPublicTunnel() {
        // Buscar el ejecutable de cloudflared en las carpetas del proyecto
        Path[] searchPaths = new Path[]{
            Paths.get("tools", "cloudflared.exe"),
            Paths.get("backend", "tools", "cloudflared.exe"),
            Paths.get("..", "tools", "cloudflared.exe"),
            Paths.get("cloudflared.exe")
        };

        Path binaryPath = null;
        for (Path p : searchPaths) {
            if (Files.exists(p)) {
                binaryPath = p.toAbsolutePath().normalize();
                break;
            }
        }

        if (binaryPath == null) {
            log.info("cloudflared no encontrado. La aplicación está lista localmente en http://localhost:{}", serverPort);
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                binaryPath.toString(),
                "tunnel",
                "--protocol", "http2",
                "--url", "http://localhost:" + serverPort
            );
            pb.redirectErrorStream(true);
            tunnelProcess = pb.start();

            Thread readerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(tunnelProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = TUNNEL_URL_PATTERN.matcher(line);
                        if (matcher.find() && publicUrl == null) {
                            publicUrl = matcher.group();
                            printBanner(publicUrl);
                            saveUrlFile(publicUrl);
                        }
                    }
                } catch (IOException ignored) {}
            }, "cloudflared-tunnel-reader");

            readerThread.setDaemon(true);
            readerThread.start();

        } catch (Exception e) {
            log.warn("No se pudo iniciar el túnel automático de Cloudflare: {}", e.getMessage());
        }
    }

    private void printBanner(String url) {
        System.out.println("\n");
        System.out.println("==================================================================================================");
        System.out.println("   🚀 ¡ENLACE PÚBLICO EN INTERNET ACTIVO! (UNIVERSIDAD JAVERIANA)");
        System.out.println("   Cualquier persona en el mundo puede ingresar desde cualquier celular o PC a:");
        System.out.println("   👉 " + url);
        System.out.println("   (Guardado también en el archivo: URL_PUBLICA_ACTIVA.txt)");
        System.out.println("==================================================================================================\n");
    }

    private void saveUrlFile(String url) {
        try {
            Files.writeString(Paths.get("URL_PUBLICA_ACTIVA.txt"), "ENLACE PÚBLICO OFICIAL:\n" + url + "\n\n(Abre este enlace en tu celular o compártelo)");
        } catch (Exception ignored) {}
        try {
            Files.writeString(Paths.get("..", "URL_PUBLICA_ACTIVA.txt"), "ENLACE PÚBLICO OFICIAL:\n" + url + "\n\n(Abre este enlace en tu celular o compártelo)");
        } catch (Exception ignored) {}
    }

    public static String getPublicUrl() {
        return publicUrl;
    }

    @PreDestroy
    public void stopPublicTunnel() {
        if (tunnelProcess != null && tunnelProcess.isAlive()) {
            tunnelProcess.destroyForcibly();
            log.info("Túnel público cerrado correctamente.");
        }
    }
}
