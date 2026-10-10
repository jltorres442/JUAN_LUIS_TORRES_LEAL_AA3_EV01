package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.CrudDao;
import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.util.ConversorJson;
import co.homecenter.envios.util.LectorFormulario;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador genérico con el flujo completo de un módulo CRUD.
 *
 * <p>Aplica el patrón <em>Template Method</em>: esta clase define el
 * algoritmo (listar, mostrar formulario, guardar, actualizar, eliminar) y
 * cada subclase solo implementa lo que cambia entre tablas, como la lectura
 * del formulario o las listas desplegables.</p>
 *
 * <h2>Dos interfaces, una sola lógica</h2>
 * <p>Cada módulo se publica en dos rutas, por ejemplo {@code /clientes} y
 * {@code /api/clientes}. La lógica (validación y acceso a datos con
 * Hibernate) es la misma; solo cambia la forma de responder:</p>
 * <ul>
 *   <li>{@code /clientes} &rarr; páginas HTML generadas con JSP.</li>
 *   <li>{@code /api/clientes} &rarr; datos JSON que consume el frontend React.</li>
 * </ul>
 *
 * <h2>Rutas atendidas (ejemplo con el módulo de clientes)</h2>
 * <table>
 *   <caption>Operaciones del módulo</caption>
 *   <tr><th>Método</th><th>URL</th><th>Operación</th><th>Respuesta API</th></tr>
 *   <tr><td>GET</td><td>?q=texto</td><td>Consultar</td><td>{@code {registros}}</td></tr>
 *   <tr><td>GET</td><td>?accion=nuevo</td><td>Formulario nuevo</td><td>{@code {registro, catalogos}}</td></tr>
 *   <tr><td>GET</td><td>?accion=editar&amp;id=5</td><td>Editar</td><td>{@code {registro, catalogos}}</td></tr>
 *   <tr><td>POST</td><td>accion=guardar</td><td>Insertar</td><td>201 {@code {mensaje, id}}</td></tr>
 *   <tr><td>POST</td><td>accion=actualizar</td><td>Actualizar</td><td>200 {@code {mensaje, id}}</td></tr>
 *   <tr><td>POST</td><td>accion=eliminar</td><td>Eliminar</td><td>200 {@code {mensaje}}</td></tr>
 * </table>
 *
 * <p>Errores de la API: 404 (no existe), 409 (conflicto con la base de datos,
 * p. ej. correo duplicado) y 422 (campos inválidos, con {@code {errores}}).</p>
 *
 * @param <T> tipo de la entidad administrada
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public abstract class CrudServlet<T> extends ServletBase {

    private static final long serialVersionUID = 1L;

    private static final String ACCION_LISTAR = "listar";
    private static final String ACCION_NUEVO = "nuevo";
    private static final String ACCION_EDITAR = "editar";
    private static final String ACCION_GUARDAR = "guardar";
    private static final String ACCION_ACTUALIZAR = "actualizar";
    private static final String ACCION_ELIMINAR = "eliminar";

    /** Código HTTP 422: la petición es correcta pero los datos no pasan la validación. */
    private static final int SC_DATOS_INVALIDOS = 422;

    // ------------------------------------------------------------------
    // Métodos que cada módulo debe implementar
    // ------------------------------------------------------------------

    /** @return DAO (Hibernate) que ejecuta las operaciones del módulo */
    protected abstract CrudDao<T> getDao();

    /** @return carpeta de las vistas dentro de {@code /WEB-INF/vistas/}, p. ej. {@code "clientes"} */
    protected abstract String getCarpetaVistas();

    /** @return ruta URL del módulo JSP, p. ej. {@code "/clientes"} */
    protected abstract String getRutaModulo();

    /** @return nombre singular de la entidad para los mensajes, p. ej. {@code "El cliente"} */
    protected abstract String getNombreEntidad();

    /** @return instancia vacía para el formulario de creación */
    protected abstract T crearEntidadVacia();

    /**
     * Construye la entidad a partir de los datos del formulario.
     *
     * @param formulario lector que convierte y valida los parámetros
     * @param id         llave primaria (solo al actualizar; {@code null} al crear)
     * @return entidad con los datos digitados
     */
    protected abstract T leerFormulario(LectorFormulario formulario, Integer id);

    /**
     * Devuelve las listas que necesita el formulario (por ejemplo, clientes y
     * vendedores para el formulario de pedidos). Por defecto no hay ninguna.
     *
     * @return mapa nombre &rarr; lista; en JSP cada entrada se publica como
     *         atributo de la petición y en la API se envía como {@code catalogos}
     * @throws DaoException si falla alguna consulta
     */
    protected Map<String, List<?>> cargarCatalogos() throws DaoException {
        return Collections.emptyMap();
    }

    // ------------------------------------------------------------------
    // Atención de peticiones HTTP
    // ------------------------------------------------------------------

    /**
     * Atiende las peticiones GET: consultas y formularios.
     */
    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = leerAccion(solicitud, ACCION_LISTAR);
        switch (accion) {
            case ACCION_NUEVO:
                mostrarFormulario(solicitud, respuesta, crearEntidadVacia(), true);
                break;
            case ACCION_EDITAR:
                mostrarFormularioEdicion(solicitud, respuesta);
                break;
            default:
                listar(solicitud, respuesta);
                break;
        }
    }

    /**
     * Atiende las peticiones POST: inserción, actualización y eliminación.
     */
    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = leerAccion(solicitud, "");
        switch (accion) {
            case ACCION_GUARDAR:
                guardar(solicitud, respuesta, false);
                break;
            case ACCION_ACTUALIZAR:
                guardar(solicitud, respuesta, true);
                break;
            case ACCION_ELIMINAR:
                eliminar(solicitud, respuesta);
                break;
            default:
                responderSolicitudInvalida(solicitud, respuesta, "Acción no reconocida.");
                break;
        }
    }

    // ------------------------------------------------------------------
    // Operaciones CRUD
    // ------------------------------------------------------------------

    /**
     * Consulta los registros (filtrados por el parámetro {@code q}).
     */
    private void listar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String filtro = solicitud.getParameter("q");
        List<T> registros;
        try {
            registros = getDao().listar(filtro);
        } catch (DaoException excepcion) {
            if (esPeticionApi(solicitud)) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        excepcion.getMessage());
                return;
            }
            registros = Collections.emptyList();
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, excepcion.getMessage());
        }

        if (esPeticionApi(solicitud)) {
            ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, Map.of("registros", registros));
            return;
        }
        solicitud.setAttribute("registros", registros);
        solicitud.setAttribute("filtro", filtro);
        mostrarVista(solicitud, respuesta, getCarpetaVistas() + "/lista.jsp");
    }

    /**
     * Busca el registro indicado por {@code id} y muestra el formulario con sus datos.
     */
    private void mostrarFormularioEdicion(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        Integer id = leerIdentificador(solicitud);
        if (id == null) {
            responderSolicitudInvalida(solicitud, respuesta, "Falta el identificador del registro.");
            return;
        }
        try {
            Optional<T> registro = getDao().buscarPorId(id);
            if (registro.isPresent()) {
                mostrarFormulario(solicitud, respuesta, registro.get(), false);
            } else {
                responderError(solicitud, respuesta, HttpServletResponse.SC_NOT_FOUND,
                        getNombreEntidad() + " #" + id + " no existe.");
            }
        } catch (DaoException excepcion) {
            responderError(solicitud, respuesta, HttpServletResponse.SC_SERVICE_UNAVAILABLE, excepcion.getMessage());
        }
    }

    /**
     * Valida el formulario y ejecuta INSERT o UPDATE (vía Hibernate) según corresponda.
     *
     * @param esActualizacion {@code true} para UPDATE, {@code false} para INSERT
     */
    private void guardar(HttpServletRequest solicitud, HttpServletResponse respuesta, boolean esActualizacion)
            throws ServletException, IOException {
        Integer id = esActualizacion ? leerIdentificador(solicitud) : null;
        if (esActualizacion && id == null) {
            responderSolicitudInvalida(solicitud, respuesta, "Falta el identificador del registro.");
            return;
        }

        LectorFormulario formulario = new LectorFormulario(solicitud);
        T entidad = leerFormulario(formulario, id);

        // 1. Validación: si hay errores, se devuelven sin tocar la base de datos.
        if (formulario.tieneErrores()) {
            String mensaje = "Revise los campos marcados en rojo.";
            if (esPeticionApi(solicitud)) {
                Map<String, Object> cuerpo = new LinkedHashMap<>();
                cuerpo.put("mensaje", mensaje);
                cuerpo.put("errores", formulario.getErrores());
                ConversorJson.responder(respuesta, SC_DATOS_INVALIDOS, cuerpo);
                return;
            }
            solicitud.setAttribute("errores", formulario.getErrores());
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, mensaje);
            mostrarFormulario(solicitud, respuesta, entidad, !esActualizacion);
            return;
        }

        // 2. Persistencia con Hibernate.
        try {
            String mensaje;
            int idResultado;
            if (esActualizacion) {
                if (!getDao().actualizar(entidad)) {
                    responderError(solicitud, respuesta, HttpServletResponse.SC_NOT_FOUND,
                            getNombreEntidad() + " #" + id + " ya no existe.");
                    return;
                }
                idResultado = id;
                mensaje = getNombreEntidad() + " #" + id + " se actualizó correctamente.";
            } else {
                idResultado = getDao().insertar(entidad);
                mensaje = getNombreEntidad() + " #" + idResultado + " se registró correctamente.";
            }

            if (esPeticionApi(solicitud)) {
                int codigo = esActualizacion ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED;
                ConversorJson.responder(respuesta, codigo, Map.of("mensaje", mensaje, "id", idResultado));
            } else {
                guardarMensajeExito(solicitud, mensaje);
                redirigir(solicitud, respuesta, getRutaModulo());
            }
        } catch (DaoException excepcion) {
            if (esPeticionApi(solicitud)) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_CONFLICT, excepcion.getMessage());
                return;
            }
            // Se vuelve a mostrar el formulario con los datos digitados para no perderlos.
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, excepcion.getMessage());
            mostrarFormulario(solicitud, respuesta, entidad, !esActualizacion);
        }
    }

    /**
     * Elimina el registro indicado por {@code id}.
     */
    private void eliminar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        Integer id = leerIdentificador(solicitud);
        if (id == null) {
            responderSolicitudInvalida(solicitud, respuesta, "Falta el identificador del registro.");
            return;
        }
        try {
            if (getDao().eliminar(id)) {
                String mensaje = getNombreEntidad() + " #" + id + " se eliminó correctamente.";
                if (esPeticionApi(solicitud)) {
                    ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, Map.of("mensaje", mensaje));
                    return;
                }
                guardarMensajeExito(solicitud, mensaje);
            } else {
                responderError(solicitud, respuesta, HttpServletResponse.SC_NOT_FOUND,
                        getNombreEntidad() + " #" + id + " ya no existe.");
                return;
            }
        } catch (DaoException excepcion) {
            if (esPeticionApi(solicitud)) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_CONFLICT, excepcion.getMessage());
                return;
            }
            guardarMensajeError(solicitud, excepcion.getMessage());
        }
        redirigir(solicitud, respuesta, getRutaModulo());
    }

    /**
     * Prepara los datos del formulario y responde con la vista JSP o con JSON.
     *
     * @param registro entidad a mostrar
     * @param esNuevo  {@code true} si el formulario es de creación
     */
    private void mostrarFormulario(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                   T registro, boolean esNuevo) throws ServletException, IOException {
        Map<String, List<?>> catalogos = Collections.emptyMap();
        try {
            catalogos = cargarCatalogos();
        } catch (DaoException excepcion) {
            if (esPeticionApi(solicitud)) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        excepcion.getMessage());
                return;
            }
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, excepcion.getMessage());
        }

        if (esPeticionApi(solicitud)) {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("registro", registro);
            cuerpo.put("esNuevo", esNuevo);
            cuerpo.put("catalogos", catalogos);
            ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, cuerpo);
            return;
        }
        // En JSP cada catálogo queda disponible por su nombre, p. ej. ${clientes}.
        catalogos.forEach(solicitud::setAttribute);
        solicitud.setAttribute("registro", registro);
        solicitud.setAttribute("esNuevo", esNuevo);
        mostrarVista(solicitud, respuesta, getCarpetaVistas() + "/formulario.jsp");
    }

    // ------------------------------------------------------------------
    // Respuestas de error comunes a JSP y API
    // ------------------------------------------------------------------

    /**
     * Responde un error: JSON con el código indicado en la API, o mensaje
     * flash y redirección al listado en la versión JSP.
     */
    private void responderError(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                int codigo, String mensaje) throws IOException {
        if (esPeticionApi(solicitud)) {
            ConversorJson.responderError(respuesta, codigo, mensaje);
        } else {
            guardarMensajeError(solicitud, mensaje);
            redirigir(solicitud, respuesta, getRutaModulo());
        }
    }

    /**
     * Responde 400 (petición mal formada).
     */
    private void responderSolicitudInvalida(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                            String mensaje) throws IOException {
        if (esPeticionApi(solicitud)) {
            ConversorJson.responderError(respuesta, HttpServletResponse.SC_BAD_REQUEST, mensaje);
        } else {
            respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST, mensaje);
        }
    }
}
