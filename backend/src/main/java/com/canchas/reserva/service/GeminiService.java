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
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public ChatResponse procesarConsulta(ChatRequest request, Usuario usuarioAutenticado) {
        String mensaje = request.getMensaje() != null ? request.getMensaje().trim() : "";
        if (mensaje.isEmpty()) {
            return new ChatResponse(
                    "¡Hola! ¿Qué deporte te gustaría jugar hoy? Puedo buscar horarios libres y armar tu reserva en fútbol, tenis, pádel o baloncesto.",
                    null,
                    "Asistente Javeriana"
            );
        }

        // Si hay API Key real de Google AI Studio configurada, usar Gemini en vivo
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.contains("tu_clave")) {
            try {
                ChatResponse respuestaGemini = consultarGoogleGemini(mensaje, request.getHistorial(), usuarioAutenticado);
                if (respuestaGemini != null) {
                    return respuestaGemini;
                }
            } catch (Exception e) {
                log.warn("[GEMINI API] Error o timeout consultando Google AI Studio ({}). Usando motor conversacional local.", e.getMessage());
            }
        }

        return procesarConMotorConversacional(mensaje, request.getHistorial(), usuarioAutenticado);
    }

    private ChatResponse consultarGoogleGemini(String promptUsuario, List<ChatMessage> historial, Usuario usuario) throws Exception {
        String urlConKey = geminiApiUrl + "?key=" + geminiApiKey.trim();
        String contextoSistema = construirContextoParaGemini(usuario);

        StringBuilder conversacion = new StringBuilder(contextoSistema).append("\n\n");
        if (historial != null && !historial.isEmpty()) {
            int start = Math.max(0, historial.size() - 6);
            for (int i = start; i < historial.size(); i++) {
                ChatMessage msg = historial.get(i);
                conversacion.append("assistant".equalsIgnoreCase(msg.getRole()) ? "ASISTENTE: " : "USUARIO: ")
                            .append(msg.getContent()).append("\n");
            }
        }
        conversacion.append("USUARIO: ").append(promptUsuario);

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", List.of(Map.of("text", conversacion.toString())));
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
                ChatResponse localAnalysis = procesarConMotorConversacional(promptUsuario, historial, usuario);
                String respuestaLimpia = limpiarTextoRespuesta(rawText);
                return new ChatResponse(respuestaLimpia, localAnalysis.getPropuestaReserva(), "Google Gemini");
            }
        }
        return null;
    }

    private ChatResponse procesarConMotorConversacional(String mensaje, List<ChatMessage> historial, Usuario usuario) {
        String texto = normalizar(mensaje);
        String nombreUser = (usuario != null && usuario.getNombre() != null)
                ? " " + usuario.getNombre().split(" ")[0]
                : "";

        // 1. Detectar si el usuario mencionó un deporte, cancha u hora específica en este mensaje
        Disciplina disciplinaActual = extraerDisciplina(texto);
        Escenario escenarioPorNombre = extraerEscenarioPorNombre(texto);
        LocalTime horaActual = extraerHora(texto);
        LocalDate fechaExplicita = extraerFechaExplicita(texto);

        boolean tieneIntencionDeportivaDirecta = (disciplinaActual != null || escenarioPorNombre != null || horaActual != null);

        // 2. Saludos puros o conversación casual ("hola", "buenas", "como estas", "ayuda", "que haces")
        if (!tieneIntencionDeportivaDirecta && esSaludoOPreguntaInicial(texto)) {
            return new ChatResponse(
                    "¡Hola" + nombreUser + "! 👋 Soy el asistente del Centro Deportivo. ¿Qué te gustaría jugar hoy?\n\n" +
                    "Tenemos canchas disponibles de **fútbol**, **tenis**, **pádel** y **baloncesto/voleibol**. " +
                    "Solo dime qué deporte buscas y a qué hora te queda bien (por ejemplo: *\"Quiero jugar tenis hoy a las 4pm\"*).",
                    null,
                    "Asistente Javeriana"
            );
        }

        // 3. Despedidas o agradecimientos ("gracias", "listo", "perfecto", "chao", "vale")
        if (!tieneIntencionDeportivaDirecta && esAgradecimientoODespedida(texto)) {
            return new ChatResponse(
                    "¡Con mucho gusto" + nombreUser + "! Si necesitas consultar otro horario o reservar otra cancha, aquí estaré.",
                    null,
                    "Asistente Javeriana"
            );
        }

        // 4. Consultas sobre iluminación nocturna / luz LED sin pedir reserva específica aún
        if (disciplinaActual == null && escenarioPorNombre == null && horaActual == null &&
            (texto.contains("luz") || texto.contains("iluminacion") || texto.contains("nocturn") || texto.contains("led"))) {
            List<Escenario> conLuz = escenarioRepository.findByEnMantenimientoFalse().stream()
                    .filter(e -> Boolean.TRUE.equals(e.getIluminacion()))
                    .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder("Todas nuestras instalaciones principales cuentan con **iluminación LED nocturna**:\n\n");
            for (Escenario e : conLuz) {
                sb.append("• **").append(e.getNombre()).append("** (").append(formatearMoneda(e.getTarifaHora())).append("/h)\n");
            }
            sb.append("\n¿Cuál de estas canchas te gustaría reservar y a qué hora?");
            return new ChatResponse(sb.toString(), null, "Asistente Javeriana");
        }

        // 5. Consultas generales de catálogo o precios ("que canchas hay", "precios", "cuanto cuesta", "cuales canchas")
        if (disciplinaActual == null && escenarioPorNombre == null && horaActual == null &&
            (texto.contains("cancha") || texto.contains("escenario") || texto.contains("precio") ||
             texto.contains("tarifa") || texto.contains("cuanto") || texto.contains("cuales") ||
             texto.contains("disponible") || texto.contains("deporte"))) {
            List<Escenario> disponibles = escenarioRepository.findByEnMantenimientoFalse();
            StringBuilder sb = new StringBuilder("Actualmente tenemos **").append(disponibles.size()).append(" escenarios disponibles**:\n\n");
            for (Escenario e : disponibles) {
                sb.append("• **").append(e.getNombre()).append("** — ").append(e.getDisciplina())
                  .append(" (").append(formatearMoneda(e.getTarifaHora())).append("/hora)\n");
            }
            sb.append("\nDime qué deporte y a qué hora quieres jugar para prepararte la reserva en un clic.");
            return new ChatResponse(sb.toString(), null, "Asistente Javeriana");
        }

        // 6. Recuperar contexto de mensajes anteriores si el usuario solo respondió "a las 6pm" o "mañana"
        Disciplina disciplinaContexto = disciplinaActual;
        Escenario escenarioContexto = escenarioPorNombre;
        LocalDate fechaContexto = fechaExplicita;

        if (historial != null && !historial.isEmpty()) {
            for (int i = historial.size() - 1; i >= 0; i--) {
                String msgPrevio = normalizar(historial.get(i).getContent());
                if (disciplinaContexto == null) {
                    disciplinaContexto = extraerDisciplina(msgPrevio);
                }
                if (escenarioContexto == null) {
                    escenarioContexto = extraerEscenarioPorNombre(msgPrevio);
                }
                if (fechaContexto == null) {
                    fechaContexto = extraerFechaExplicita(msgPrevio);
                }
            }
        }

        // Si aun revisando el historial no hay ni deporte ni cancha ni hora, responder orientando al usuario
        if (disciplinaContexto == null && escenarioContexto == null && horaActual == null) {
            return new ChatResponse(
                    "Para ayudarte a reservar al instante, cuéntame **qué deporte** quieres practicar (fútbol, tenis, pádel o baloncesto) y **a qué hora** te gustaría jugar.",
                    null,
                    "Asistente Javeriana"
            );
        }

        LocalDate fechaObjetivo = (fechaContexto != null) ? fechaContexto : LocalDate.now();

        // Seleccionar los escenarios candidatos
        List<Escenario> candidatos = escenarioRepository.findByEnMantenimientoFalse();
        if (escenarioContexto != null) {
            final Long idEsc = escenarioContexto.getId();
            candidatos = candidatos.stream().filter(e -> e.getId().equals(idEsc)).collect(Collectors.toList());
        } else if (disciplinaContexto != null) {
            final Disciplina disc = disciplinaContexto;
            candidatos = candidatos.stream().filter(e -> e.getDisciplina() == disc).collect(Collectors.toList());
        }

        if (candidatos.isEmpty()) {
            candidatos = escenarioRepository.findByEnMantenimientoFalse();
        }

        // Buscar horarios libres reales en la fecha objetivo
        for (Escenario e : candidatos) {
            if (Boolean.TRUE.equals(e.getEnMantenimiento())) continue;

            List<FranjaHoraria> todasFranjas = franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(e.getId());
            List<Reserva> reservasDia = reservaRepository.findByEscenarioIdAndFechaAndEstado(e.getId(), fechaObjetivo, EstadoReserva.CONFIRMADA);
            Set<Long> ocupadasIds = reservasDia.stream().map(r -> r.getFranja().getId()).collect(Collectors.toSet());

            List<FranjaHoraria> libres = todasFranjas.stream()
                    .filter(f -> !ocupadasIds.contains(f.getId()))
                    .collect(Collectors.toList());

            if (libres.isEmpty()) continue;

            // Caso A: El usuario pidió una hora específica (ej. "a las 6pm", "10am")
            if (horaActual != null) {
                FranjaHoraria exacta = libres.stream()
                        .filter(f -> f.getHoraInicio().getHour() == horaActual.getHour())
                        .findFirst()
                        .orElse(null);

                if (exacta != null) {
                    PropuestaReserva prop = crearPropuesta(e, exacta, fechaObjetivo);
                    String resp = String.format(
                            "¡Listo! La **%s** está libre para el **%s** de **%s a %s** (%s). " +
                            "Puedes confirmarla directamente con el botón de abajo:",
                            e.getNombre(),
                            formatearFechaHumana(fechaObjetivo),
                            exacta.getHoraInicio(),
                            exacta.getHoraFin(),
                            formatearMoneda(e.getTarifaHora())
                    );
                    return new ChatResponse(resp, prop, "Asistente Javeriana");
                } else {
                    // Buscar la más cercana disponible
                    FranjaHoraria cercana = libres.stream()
                            .min(Comparator.comparingInt(f -> Math.abs(f.getHoraInicio().getHour() - horaActual.getHour())))
                            .orElse(libres.get(0));
                    PropuestaReserva prop = crearPropuesta(e, cercana, fechaObjetivo);
                    String resp = String.format(
                            "El horario de las %s en **%s** ya no está disponible para el **%s**, pero tengo libre el bloque cercano de **%s a %s** (%s). " +
                            "¿Te gustaría reservar ese horario?",
                            horaActual,
                            e.getNombre(),
                            formatearFechaHumana(fechaObjetivo),
                            cercana.getHoraInicio(),
                            cercana.getHoraFin(),
                            formatearMoneda(e.getTarifaHora())
                    );
                    return new ChatResponse(resp, prop, "Asistente Javeriana");
                }
            }

            // Caso B: El usuario preguntó por un deporte/cancha ("¿Qué canchas de fútbol hay hoy?", "Quiero jugar tenis") sin especificar hora
            FranjaHoraria sugerida = elegirFranjaRecomendada(libres);
            PropuestaReserva prop = crearPropuesta(e, sugerida, fechaObjetivo);

            String muestraHorarios = libres.stream()
                    .filter(f -> f.getHoraInicio().getHour() >= 8 && f.getHoraInicio().getHour() <= 20)
                    .limit(6)
                    .map(f -> f.getHoraInicio().toString())
                    .collect(Collectors.joining(", "));
            if (muestraHorarios.isEmpty()) {
                muestraHorarios = libres.stream().limit(5).map(f -> f.getHoraInicio().toString()).collect(Collectors.joining(", "));
            }

            String resp = String.format(
                    "Para **%s** tenemos disponible **%s** (%s/hora).\n\n" +
                    "Algunos horarios libres son: **%s**.\n" +
                    "Te dejé lista una reserva sugerida de **%s a %s** por si deseas confirmarla de una vez, o dime qué otra hora prefieres.",
                    formatearFechaHumana(fechaObjetivo),
                    e.getNombre(),
                    formatearMoneda(e.getTarifaHora()),
                    muestraHorarios,
                    sugerida.getHoraInicio(),
                    sugerida.getHoraFin()
            );
            return new ChatResponse(resp, prop, "Asistente Javeriana");
        }

        return new ChatResponse(
                "No encontré franjas disponibles para esa combinación en el día " + fechaObjetivo + ". ¿Te gustaría que revisemos para mañana o en otra cancha?",
                null,
                "Asistente Javeriana"
        );
    }

    private FranjaHoraria elegirFranjaRecomendada(List<FranjaHoraria> libres) {
        // Priorizar horarios de tarde/noche (16:00, 17:00, 18:00, 10:00) en lugar de las 06:00 AM
        int[] horasPreferidas = {17, 18, 16, 19, 10, 15, 11, 14, 9, 20, 8};
        for (int h : horasPreferidas) {
            for (FranjaHoraria f : libres) {
                if (f.getHoraInicio().getHour() == h) {
                    return f;
                }
            }
        }
        return libres.get(0);
    }

    private PropuestaReserva crearPropuesta(Escenario e, FranjaHoraria f, LocalDate fecha) {
        return new PropuestaReserva(
                e.getId(),
                e.getNombre(),
                e.getDisciplina().name(),
                f.getId(),
                f.getHoraInicio(),
                f.getHoraFin(),
                fecha,
                e.getTarifaHora(),
                e.getSuperficie().name()
        );
    }

    private boolean esSaludoOPreguntaInicial(String t) {
        String limpio = t.replaceAll("[^a-z0-9\\s]", "").trim();
        Set<String> saludosExactos = Set.of(
                "hola", "buenas", "buenos dias", "buenas tardes", "buenas noches",
                "hey", "ola", "saludos", "que tal", "como estas", "hola como estas",
                "ayuda", "menu", "inicio", "quien eres", "que haces", "que puedes hacer"
        );
        if (saludosExactos.contains(limpio)) return true;
        return limpio.startsWith("hola ") && limpio.split("\\s+").length <= 4;
    }

    private boolean esAgradecimientoODespedida(String t) {
        String limpio = t.replaceAll("[^a-z0-9\\s]", "").trim();
        return Set.of("gracias", "muchas gracias", "ok", "vale", "listo", "perfecto", "chao", "adios", "hasta luego", "super", "genial").contains(limpio);
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .replace('á', 'a')
                .replace('é', 'e')
                .replace('í', 'i')
                .replace('ó', 'o')
                .replace('ú', 'u');
    }

    private Escenario extraerEscenarioPorNombre(String texto) {
        List<Escenario> todos = escenarioRepository.findByEnMantenimientoFalse();
        if (texto.contains("campincito") || texto.contains("futbol 8") || texto.contains("sintetica 1")) {
            return todos.stream().filter(e -> normalizar(e.getNombre()).contains("campincito")).findFirst().orElse(null);
        }
        if (texto.contains("futbol 11") || texto.contains("natural")) {
            return todos.stream().filter(e -> normalizar(e.getNombre()).contains("11")).findFirst().orElse(null);
        }
        if (texto.contains("coliseo")) {
            return todos.stream().filter(e -> normalizar(e.getNombre()).contains("coliseo")).findFirst().orElse(null);
        }
        return null;
    }

    private LocalDate extraerFechaExplicita(String texto) {
        LocalDate hoy = LocalDate.now();
        if (texto.contains("pasado mañana") || texto.contains("pasado manana")) return hoy.plusDays(2);
        if (texto.contains("mañana") || texto.contains("manana")) return hoy.plusDays(1);
        if (texto.contains("hoy")) return hoy;
        if (texto.contains("lunes")) return proximoDia(hoy, java.time.DayOfWeek.MONDAY);
        if (texto.contains("martes")) return proximoDia(hoy, java.time.DayOfWeek.TUESDAY);
        if (texto.contains("miercoles")) return proximoDia(hoy, java.time.DayOfWeek.WEDNESDAY);
        if (texto.contains("jueves")) return proximoDia(hoy, java.time.DayOfWeek.THURSDAY);
        if (texto.contains("viernes")) return proximoDia(hoy, java.time.DayOfWeek.FRIDAY);
        if (texto.contains("sabado")) return proximoDia(hoy, java.time.DayOfWeek.SATURDAY);
        if (texto.contains("domingo")) return proximoDia(hoy, java.time.DayOfWeek.SUNDAY);

        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            try { return LocalDate.parse(matcher.group(1)); } catch (Exception ignored) {}
        }
        return null;
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
        // Buscar patrones explícitos como "6pm", "6 pm", "18:00", "a las 6", "a las 10"
        Pattern amPmPattern = Pattern.compile("\\b(1[0-2]|0?[1-9])\\s*(?::00)?\\s*(am|pm)\\b");
        Matcher mAmPm = amPmPattern.matcher(texto);
        if (mAmPm.find()) {
            int h = Integer.parseInt(mAmPm.group(1));
            String meridiano = mAmPm.group(2);
            if ("pm".equals(meridiano) && h < 12) h += 12;
            if ("am".equals(meridiano) && h == 12) h = 0;
            if (h >= 6 && h <= 21) return LocalTime.of(h, 0);
        }

        Pattern h24Pattern = Pattern.compile("\\b([01]?\\d|2[0-3]):00\\b");
        Matcher m24 = h24Pattern.matcher(texto);
        if (m24.find()) {
            int h = Integer.parseInt(m24.group(1));
            if (h >= 6 && h <= 21) return LocalTime.of(h, 0);
        }

        Pattern aLasPattern = Pattern.compile("a las\\s+(1\\d|2[0-2]|0?[1-9])\\b");
        Matcher mALas = aLasPattern.matcher(texto);
        if (mALas.find()) {
            int h = Integer.parseInt(mALas.group(1));
            if (h >= 1 && h <= 5) h += 12; // "a las 4" -> 16:00
            else if (h >= 6 && h <= 11 && (texto.contains("tarde") || texto.contains("noche"))) h += 12;
            if (h >= 6 && h <= 21) return LocalTime.of(h, 0);
        }

        if (texto.contains("en la tarde") || texto.contains("por la tarde")) return LocalTime.of(16, 0);
        if (texto.contains("en la noche") || texto.contains("por la noche")) return LocalTime.of(19, 0);
        if (texto.contains("en la manana") || texto.contains("por la manana")) return LocalTime.of(9, 0);
        return null;
    }

    private Disciplina extraerDisciplina(String texto) {
        if (texto.contains("futbol") || texto.contains("sintetica") || texto.contains("micro")) return Disciplina.FUTBOL;
        if (texto.contains("tenis") || texto.contains("tennis") || texto.contains("arcilla") || texto.contains("ladrillo")) return Disciplina.TENIS;
        if (texto.contains("padel")) return Disciplina.PADEL;
        if (texto.contains("baloncesto") || texto.contains("basket") || texto.contains("basquet") || texto.contains("voley") || texto.contains("voleibol")) return Disciplina.BALONCESTO;
        return null;
    }

    private String formatearMoneda(java.math.BigDecimal valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        nf.setMaximumFractionDigits(0);
        return nf.format(valor);
    }

    private String formatearFechaHumana(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha.equals(hoy)) return "hoy (" + fecha + ")";
        if (fecha.equals(hoy.plusDays(1))) return "mañana (" + fecha + ")";
        return fecha.toString();
    }

    private String construirContextoParaGemini(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres el asistente deportivo del Centro Deportivo de la Pontificia Universidad Javeriana.\n");
        sb.append("Hoy es ").append(LocalDate.now()).append(".\n");
        sb.append("Tu objetivo es conversar con naturalidad y ayudar a consultar horarios y reservar canchas.\n");
        sb.append("Si el usuario solo saluda (ej. 'Hola'), salúdalo cordialmente y pregúntale qué deporte desea jugar, sin inventar que ya buscó una cancha.\n");
        sb.append("Canchas activas:\n");
        for (Escenario e : escenarioRepository.findByEnMantenimientoFalse()) {
            sb.append("- ").append(e.getNombre()).append(" (").append(e.getDisciplina()).append(", $").append(e.getTarifaHora()).append("/h)\n");
        }
        return sb.toString();
    }

    private String limpiarTextoRespuesta(String raw) {
        return raw.replaceAll("```json[\\s\\S]*?```", "").trim();
    }
}
