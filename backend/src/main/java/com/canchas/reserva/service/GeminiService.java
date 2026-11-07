package com.canchas.reserva.service;

import com.canchas.reserva.dto.AiDtos.*;
import com.canchas.reserva.entity.*;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import com.canchas.reserva.repository.ReservaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio de Inteligencia Artificial para el Asistente Deportivo Universitario.
 * Se conecta con Google AI Studio (Gemini 1.5 Flash API) y cuenta con un motor semántico
 * de respaldo local para garantizar operación continua aún sin conectividad o API key.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    private final EscenarioRepository escenarioRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final ReservaRepository reservaRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiService(EscenarioRepository escenarioRepository,
                         FranjaHorariaRepository franjaRepository,
                         ReservaRepository reservaRepository,
                         ObjectMapper objectMapper) {
        this.escenarioRepository = escenarioRepository;
        this.franjaRepository = franjaRepository;
        this.reservaRepository = reservaRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    /**
     * Procesa la consulta del usuario en lenguaje natural y devuelve una respuesta enriquecida
     * con propuestas interactivas de reserva si detecta la intención.
     */
    public ChatResponse procesarConsulta(ChatRequest request, Usuario usuarioAutenticado) {
        String mensaje = request.getMensaje() != null ? request.getMensaje().trim() : "";
        if (mensaje.isEmpty()) {
            return new ChatResponse("¡Hola! Soy tu Asistente Deportivo Javeriana. ¿En qué disciplina o cancha te gustaría jugar hoy?", null, "Local");
        }

        // Si hay API Key de Google configurada, intentar llamada en vivo a Gemini 1.5 Flash
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.contains("tu_clave")) {
            try {
                ChatResponse respuestaGemini = consultarGoogleGemini(mensaje, request.getHistorial(), usuarioAutenticado);
                if (respuestaGemini != null) {
                    return respuestaGemini;
                }
            } catch (Exception e) {
                log.warn("[GEMINI API] Fallo o timeout al consultar Google AI Studio ({}). Usando motor deportivo local...", e.getMessage());
            }
        }

        // Motor deportivo local de respaldo de alto rendimiento
        return procesarConMotorLocal(mensaje, usuarioAutenticado);
    }

    /**
     * Conexión directa vía HTTP REST a Google AI Studio (Gemini 1.5 Flash).
     */
    private ChatResponse consultarGoogleGemini(String promptUsuario, List<ChatMessage> historial, Usuario usuario) throws Exception {
        String urlConKey = geminiApiUrl + "?key=" + geminiApiKey.trim();

        // Construir contexto en tiempo real con canchas y franjas
        String contextoSistema = construirContextoParaGemini(usuario);

        // Armar payload JSON para Gemini
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", contextoSistema + "\n\nUSUARIO (" + (usuario != null ? usuario.getNombre() : "Visitante") + "): " + promptUsuario));
        contents.put("parts", parts);
        payload.put("contents", List.of(contents));

        String requestJson = objectMapper.writeValueAsString(payload);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlConKey))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");
            if (!textNode.isMissingNode()) {
                String rawText = textNode.asText();

                // Intentar extraer propuesta de reserva estructurada en el texto de Gemini
                PropuestaReserva propuesta = extraerPropuestaDesdeTexto(rawText, promptUsuario);
                String respuestaLimpia = limpiarTextoRespuesta(rawText);

                return new ChatResponse(respuestaLimpia, propuesta, "Gemini 1.5 Flash (Google AI Studio)");
            }
        } else {
            log.warn("[GEMINI API HTTP {}] Respuesta: {}", response.statusCode(), response.body());
        }

        return null;
    }

    /**
     * Motor Semántico Deportivo Local: Comprende disciplinas, fechas relativas y horarios
     * cruzándolos con la base de datos real sin requerir internet.
     */
    private ChatResponse procesarConMotorLocal(String mensaje, Usuario usuario) {
        String texto = mensaje.toLowerCase(Locale.ROOT);
        LocalDate fechaObjetivo = extraerFecha(texto);
        LocalTime horaObjetivo = extraerHora(texto);
        Disciplina disciplina = extraerDisciplina(texto);

        List<Escenario> escenarios = escenarioRepository.findByEnMantenimientoFalse();
        if (disciplina != null) {
            escenarios = escenarios.stream()
                    .filter(e -> e.getDisciplina() == disciplina)
                    .collect(Collectors.toList());
        }

        // Si no encontró escenario específico por disciplina, pero menciona nombre de cancha
        if (escenarios.isEmpty()) {
            escenarios = escenarioRepository.findByEnMantenimientoFalse();
        }

        // Buscar una franja disponible que haga match
        for (Escenario e : escenarios) {
            if (Boolean.TRUE.equals(e.getEnMantenimiento())) continue;

            List<FranjaHoraria> franjas = franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(e.getId());
            List<Reserva> reservasFecha = reservaRepository.findByEscenarioIdAndFechaAndEstado(e.getId(), fechaObjetivo, EstadoReserva.CONFIRMADA);
            Set<Long> franjasOcupadas = reservasFecha.stream().map(r -> r.getFranja().getId()).collect(Collectors.toSet());

            for (FranjaHoraria f : franjas) {
                if (franjasOcupadas.contains(f.getId())) continue;

                // Si hay hora objetivo y coincide aproximadamente
                boolean coincideHora = (horaObjetivo == null) ||
                        (f.getHoraInicio().getHour() == horaObjetivo.getHour()) ||
                        (Math.abs(f.getHoraInicio().getHour() - horaObjetivo.getHour()) <= 1);

                if (coincideHora) {
                    PropuestaReserva propuesta = new PropuestaReserva(
                            e.getId(),
                            e.getNombre(),
                            e.getDisciplina().name(),
                            f.getId(),
                            f.getHoraInicio(),
                            f.getHoraFin(),
                            fechaObjetivo,
                            e.getTarifaHora(),
                            e.getSuperficie().name()
                    );

                    String nombreUser = (usuario != null) ? usuario.getNombre().split(" ")[0] : "deportista";
                    String respuesta = String.format(
                            "¡Excelente elección, %s! 🎾 He consultado la disponibilidad en tiempo real para el **%s**: Encontré libre la **%s** en el horario de **%s a %s** (Tarifa: $%,.0f COP). ¿Deseas que confirme esta reserva para ti?",
                            nombreUser,
                            fechaObjetivo.toString(),
                            e.getNombre(),
                            f.getHoraInicio(),
                            f.getHoraFin(),
                            e.getTarifaHora()
                    );

                    return new ChatResponse(respuesta, propuesta, "Motor Deportivo Javeriana");
                }
            }
        }

        // Mensaje conversacional si no hay franjas o es una pregunta general
        if (texto.contains("hola") || texto.contains("buenas") || texto.contains("que puedes hacer")) {
            return new ChatResponse(
                    "¡Hola! Soy tu **Asistente Deportivo Inteligente** de la Javeriana. Puedo ayudarte a encontrar horarios libres y reservar canchas automáticamente. Puedes decirme cosas como:\n\n" +
                    "• *\"Quiero jugar fútbol mañana a las 6pm\"*\n" +
                    "• *\"¿Qué canchas de pádel hay libres hoy?\"*\n" +
                    "• *\"Resérvame la cancha de tenis para el viernes a las 10am\"*",
                    null,
                    "Motor Deportivo Javeriana"
            );
        }

        return new ChatResponse(
                "Entiendo tu consulta sobre **" + mensaje + "**. Para el día " + fechaObjetivo + " te recomiendo revisar el catálogo principal o indicarme una hora específica (por ejemplo *a las 4pm* o *a las 18:00*) para ubicarte la mejor cancha disponible al instante.",
                null,
                "Motor Deportivo Javeriana"
        );
    }

    private String construirContextoParaGemini(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres 'SportBot Javeriana', el Asistente Deportivo Inteligente del Complejo Deportivo de la Pontificia Universidad Javeriana en Bogotá.\n");
        sb.append("Hoy es ").append(LocalDate.now()).append(".\n");
        sb.append("Tu misión es asesorar amablemente a los deportistas y ayudarles a encontrar y reservar canchas disponibles.\n");
        sb.append("Canchas activas en el campus:\n");

        List<Escenario> escenarios = escenarioRepository.findByEnMantenimientoFalse();
        for (Escenario e : escenarios) {
            sb.append("- ID: ").append(e.getId())
              .append(" | ").append(e.getNombre())
              .append(" | Disciplina: ").append(e.getDisciplina())
              .append(" | Superficie: ").append(e.getSuperficie())
              .append(" | Tarifa: $").append(e.getTarifaHora())
              .append(Boolean.TRUE.equals(e.getEnMantenimiento()) ? " (EN MANTENIMIENTO)" : " (DISPONIBLE)")
              .append("\n");
        }
        sb.append("\nReglas: Responde de forma concisa, cálida y en español. Si el usuario desea reservar, menciona la cancha, fecha y hora sugerida.");
        return sb.toString();
    }

    private LocalDate extraerFecha(String texto) {
        LocalDate hoy = LocalDate.now();
        if (texto.contains("mañana")) return hoy.plusDays(1);
        if (texto.contains("pasado mañana")) return hoy.plusDays(2);
        if (texto.contains("hoy")) return hoy;
        if (texto.contains("lunes")) return proximoDia(hoy, java.time.DayOfWeek.MONDAY);
        if (texto.contains("martes")) return proximoDia(hoy, java.time.DayOfWeek.TUESDAY);
        if (texto.contains("miercoles") || texto.contains("miércoles")) return proximoDia(hoy, java.time.DayOfWeek.WEDNESDAY);
        if (texto.contains("jueves")) return proximoDia(hoy, java.time.DayOfWeek.THURSDAY);
        if (texto.contains("viernes")) return proximoDia(hoy, java.time.DayOfWeek.FRIDAY);
        if (texto.contains("sabado") || texto.contains("sábado")) return proximoDia(hoy, java.time.DayOfWeek.SATURDAY);
        if (texto.contains("domingo")) return proximoDia(hoy, java.time.DayOfWeek.SUNDAY);

        // Regex para fechas YYYY-MM-DD
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            try { return LocalDate.parse(matcher.group(1)); } catch (Exception ignored) {}
        }
        return hoy.plusDays(1); // Por defecto sugiere para mañana si no especifica
    }

    private LocalDate proximoDia(LocalDate desde, java.time.DayOfWeek dia) {
        LocalDate fecha = desde;
        for (int i = 0; i < 7; i++) {
            fecha = fecha.plusDays(1);
            if (fecha.getDayOfWeek() == dia) return fecha;
        }
        return desde.plusDays(1);
    }

    private LocalTime extraerHora(String texto) {
        if (texto.contains("6pm") || texto.contains("6 pm") || texto.contains("18:00") || texto.contains("18")) return LocalTime.of(18, 0);
        if (texto.contains("7pm") || texto.contains("7 pm") || texto.contains("19:00") || texto.contains("19")) return LocalTime.of(19, 0);
        if (texto.contains("8pm") || texto.contains("8 pm") || texto.contains("20:00") || texto.contains("20")) return LocalTime.of(20, 0);
        if (texto.contains("5pm") || texto.contains("5 pm") || texto.contains("17:00") || texto.contains("17")) return LocalTime.of(17, 0);
        if (texto.contains("4pm") || texto.contains("4 pm") || texto.contains("16:00") || texto.contains("16")) return LocalTime.of(16, 0);
        if (texto.contains("10am") || texto.contains("10 am") || texto.contains("10:00")) return LocalTime.of(10, 0);
        if (texto.contains("9am") || texto.contains("9 am") || texto.contains("09:00")) return LocalTime.of(9, 0);
        if (texto.contains("8am") || texto.contains("8 am") || texto.contains("08:00")) return LocalTime.of(8, 0);
        if (texto.contains("manana") || texto.contains("mañana por la mañana")) return LocalTime.of(9, 0);
        if (texto.contains("tarde") || texto.contains("en la tarde")) return LocalTime.of(17, 0);
        if (texto.contains("noche") || texto.contains("en la noche")) return LocalTime.of(19, 0);
        return null;
    }

    private Disciplina extraerDisciplina(String texto) {
        if (texto.contains("futbol") || texto.contains("fútbol") || texto.contains("sintetica") || texto.contains("sintética")) return Disciplina.FUTBOL;
        if (texto.contains("tenis") || texto.contains("tennis") || texto.contains("raqueta")) return Disciplina.TENIS;
        if (texto.contains("padel") || texto.contains("pádel") || texto.contains("paleta")) return Disciplina.PADEL;
        if (texto.contains("baloncesto") || texto.contains("basket") || texto.contains("basquet") || texto.contains("voley") || texto.contains("voleibol")) return Disciplina.BALONCESTO;
        return null;
    }

    private PropuestaReserva extraerPropuestaDesdeTexto(String textoGemini, String promptOriginal) {
        // Analiza si Gemini sugirió una reserva o utiliza el motor local para asociar la franja exacta
        return procesarConMotorLocal(promptOriginal, null).getPropuestaReserva();
    }

    private String limpiarTextoRespuesta(String raw) {
        return raw.replaceAll("```json[\\s\\S]*?```", "").trim();
    }
}
