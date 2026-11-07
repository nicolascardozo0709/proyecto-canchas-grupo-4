package com.canchas.reserva.service;

import com.canchas.reserva.entity.Reserva;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio de notificaciones por correo electrónico para reservas confirmadas y canceladas.
 * Genera comprobantes con formato HTML institucional y diseño responsive de alta calidad.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.sender.name:Complejo Deportivo Javeriana}")
    private String senderName;

    @Value("${app.mail.sender.email:reservas@canchas.javeriana.edu.co}")
    private String senderEmail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));

    /**
     * Envía de forma asíncrona el comprobante oficial de reserva por correo electrónico.
     */
    @Async
    public void enviarConfirmacionReserva(Reserva reserva) {
        if (reserva == null || reserva.getUsuario() == null || reserva.getUsuario().getEmail() == null) {
            log.warn("[EMAIL] No se puede enviar confirmación: datos de usuario o reserva nulos.");
            return;
        }

        String destinatario = reserva.getUsuario().getEmail();
        String asunto = "Confirmación de Reserva #" + reserva.getCodigoReserva() + " - Complejo Deportivo Javeriana";
        String contenidoHtml = construirPlantillaHtmlReserva(reserva);

        boolean enviadoReal = false;

        // Intentar envío real si mailSender está disponible y hay credenciales
        if (mailSender != null && mailUsername != null && !mailUsername.trim().isEmpty()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setText(contenidoHtml, true);
                helper.setTo(destinatario);
                helper.setSubject(asunto);
                helper.setFrom(senderEmail, senderName);
                mailSender.send(mimeMessage);
                enviadoReal = true;
                log.info("[EMAIL] Correo de confirmación enviado exitosamente vía SMTP a {}", destinatario);
            } catch (Exception e) {
                log.warn("[EMAIL] No se pudo enviar por SMTP ({}), registrando en bitácora de auditoría...", e.getMessage());
            }
        }

        if (!enviadoReal) {
            log.info("================================================================================");
            log.info("[EMAIL SIMULADO] COMPROBANTE OFICIAL DE RESERVA GENERADO");
            log.info("Para: {} ({})", destinatario, reserva.getUsuario().getNombre());
            log.info("Asunto: {}", asunto);
            log.info("Código: {} | Escenario: {} | Fecha: {} | Franja: {}-{}",
                    reserva.getCodigoReserva(),
                    reserva.getEscenario().getNombre(),
                    reserva.getFecha(),
                    reserva.getFranja().getHoraInicio(),
                    reserva.getFranja().getHoraFin());
            log.info("================================================================================");
        }
    }

    /**
     * Construye la plantilla HTML del correo con estética profesional y branding Javeriana.
     */
    private String construirPlantillaHtmlReserva(Reserva r) {
        String fechaFormateada = r.getFecha().format(FECHA_FORMAT);
        String fechaCapitalizada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);

        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='utf-8'>" +
            "<style>" +
            "  body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f7; margin: 0; padding: 24px; color: #1d1d1f; }" +
            "  .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }" +
            "  .header { background: #002f6c; color: #ffffff; padding: 32px 28px; text-align: center; border-bottom: 4px solid #eaaa00; }" +
            "  .header h1 { margin: 0; font-size: 22px; font-weight: 700; letter-spacing: -0.5px; }" +
            "  .header p { margin: 6px 0 0; font-size: 13px; color: #e2e8f0; text-transform: uppercase; letter-spacing: 1px; }" +
            "  .body-content { padding: 32px 28px; }" +
            "  .greeting { font-size: 16px; margin-bottom: 16px; }" +
            "  .badge-code { display: inline-block; background: #eef2ff; color: #002f6c; font-weight: 700; padding: 8px 16px; border-radius: 999px; font-size: 14px; margin-bottom: 24px; border: 1px solid #c7d2fe; }" +
            "  .details-box { background: #f8fafc; border-radius: 14px; padding: 20px; margin-bottom: 24px; border: 1px solid #e2e8f0; }" +
            "  .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #cbd5e1; font-size: 14px; }" +
            "  .detail-row:last-child { border-bottom: none; }" +
            "  .detail-label { color: #64748b; font-weight: 500; }" +
            "  .detail-val { color: #0f172a; font-weight: 600; text-align: right; }" +
            "  .guidelines { background: #fffbeb; border-left: 4px solid #eaaa00; padding: 14px 18px; border-radius: 0 10px 10px 0; margin-bottom: 24px; font-size: 13px; color: #92400e; }" +
            "  .footer { text-align: center; padding: 20px; font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "  <div class='container'>" +
            "    <div class='header'>" +
            "      <h1>PONTIFICIA UNIVERSIDAD JAVERIANA</h1>" +
            "      <p>Centro Deportivo Universitario - Comprobante Oficial</p>" +
            "    </div>" +
            "    <div class='body-content'>" +
            "      <div class='greeting'>Hola <b>" + escape(r.getUsuario().getNombre()) + "</b>,</div>" +
            "      <p style='margin-bottom: 20px; line-height: 1.5; color: #334155;'>Tu reserva ha sido procesada y confirmada en el sistema en tiempo real. Te esperamos en el campus deportivo.</p>" +
            "      <div style='text-align: center;'><span class='badge-code'>CÓDIGO: " + r.getCodigoReserva() + "</span></div>" +
            "      <div class='details-box'>" +
            "        <div class='detail-row'><span class='detail-label'>Escenario:</span><span class='detail-val'>" + escape(r.getEscenario().getNombre()) + "</span></div>" +
            "        <div class='detail-row'><span class='detail-label'>Disciplina:</span><span class='detail-val'>" + r.getEscenario().getDisciplina() + "</span></div>" +
            "        <div class='detail-row'><span class='detail-label'>Superficie:</span><span class='detail-val'>" + r.getEscenario().getSuperficie() + "</span></div>" +
            "        <div class='detail-row'><span class='detail-label'>Fecha:</span><span class='detail-val'>" + fechaCapitalizada + "</span></div>" +
            "        <div class='detail-row'><span class='detail-label'>Horario:</span><span class='detail-val'>" + r.getFranja().getHoraInicio() + " - " + r.getFranja().getHoraFin() + " (60 min)</span></div>" +
            "        <div class='detail-row'><span class='detail-label'>Tarifa abonada:</span><span class='detail-val'>$" + String.format(Locale.US, "%,.0f", r.getPrecioTotal()) + " COP</span></div>" +
            "        <div class='detail-row'><span class='detail-label'>Estado:</span><span class='detail-val' style='color:#16a34a;'>CONFIRMADA</span></div>" +
            "      </div>" +
            "      <div class='guidelines'>" +
            "        <b>Instrucciones de Ingreso:</b><br>" +
            "        • Presenta tu carné institucional físico o digital en portería deportiva.<br>" +
            "        • Llega con 10 minutos de antelación.<br>" +
            "        • El uso de calzado reglamentario para la superficie es obligatorio." +
            "      </div>" +
            "    </div>" +
            "    <div class='footer'>" +
            "      © 2026 Pontificia Universidad Javeriana · Carrera 7 No. 40 - 62, Bogotá D.C.<br>" +
            "      Sistema Automatizado de Reservas Deportivas" +
            "    </div>" +
            "  </div>" +
            "</body>" +
            "</html>";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
