package co.homecenter.envios.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 * Convierte objetos Java a texto JSON con la librería Jackson.
 *
 * <p>Jackson recorre los métodos <em>getter</em> de cada objeto: por ejemplo,
 * {@code getNombreCliente()} produce la propiedad {@code "nombreCliente"}.
 * Los getters marcados con {@code @JsonIgnore} (como el hash de la
 * contraseña) se omiten.</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public final class ConversorJson {

    /**
     * {@link ObjectMapper} es seguro para usarse desde varios hilos una vez
     * configurado, por eso se comparte una sola instancia.
     */
    private static final ObjectMapper MAPEADOR = new ObjectMapper()
            // Soporte para LocalDate y LocalDateTime.
            .registerModule(new JavaTimeModule())
            // Las fechas se escriben como texto ISO ("2026-09-20") y no como arreglos de números.
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ConversorJson() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Escribe un objeto como JSON en el flujo de salida indicado.
     *
     * @param objeto  objeto a convertir (Map, List, entidad, etc.)
     * @param destino flujo de escritura, normalmente la respuesta HTTP
     * @throws IOException si no se puede escribir
     */
    public static void escribir(Object objeto, Writer destino) throws IOException {
        MAPEADOR.writeValue(destino, objeto);
    }

    /**
     * Envía una respuesta HTTP completa en formato JSON.
     *
     * @param respuesta    respuesta HTTP
     * @param codigoEstado código HTTP (200 éxito, 401 sin sesión, 422 datos inválidos, etc.)
     * @param cuerpo       objeto que se convertirá a JSON
     * @throws IOException si no se puede escribir
     */
    public static void responder(HttpServletResponse respuesta, int codigoEstado, Object cuerpo) throws IOException {
        respuesta.setStatus(codigoEstado);
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Los datos de la API cambian constantemente: el navegador no debe guardarlos en caché.
        respuesta.setHeader("Cache-Control", "no-store");
        escribir(cuerpo, respuesta.getWriter());
    }

    /**
     * Atajo para responder un mensaje de error con la forma {@code {"mensaje": "..."}}.
     *
     * @param respuesta    respuesta HTTP
     * @param codigoEstado código HTTP de error
     * @param mensaje      texto para el usuario
     * @throws IOException si no se puede escribir
     */
    public static void responderError(HttpServletResponse respuesta, int codigoEstado, String mensaje)
            throws IOException {
        responder(respuesta, codigoEstado, Map.of("mensaje", mensaje));
    }
}
