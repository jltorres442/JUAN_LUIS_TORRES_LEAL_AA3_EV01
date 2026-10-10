package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.AutenticacionDao;
import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.filtro.CsrfFiltro;
import co.homecenter.envios.modelo.UsuarioSesion;
import co.homecenter.envios.util.CatalogoEstados;
import co.homecenter.envios.util.ConversorJson;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * API JSON de sesión para el frontend React. Atiende {@code /api/sesion}.
 *
 * <table>
 *   <caption>Operaciones</caption>
 *   <tr><th>Método</th><th>Parámetros</th><th>Respuesta</th></tr>
 *   <tr><td>GET</td><td>—</td>
 *       <td>{@code {autenticado, usuario, tokenCsrf, estados}}: React lo llama al cargar
 *       para saber si hay sesión y obtener el token CSRF.</td></tr>
 *   <tr><td>POST</td><td>{@code accion=iniciar, email, contrasena, tokenCsrf}</td>
 *       <td>200 con el usuario y el token nuevo, o 401 si las credenciales fallan.</td></tr>
 *   <tr><td>POST</td><td>{@code accion=cerrar, tokenCsrf}</td><td>200 y la sesión se invalida.</td></tr>
 * </table>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebServlet(name = "SesionApiServlet", urlPatterns = "/api/sesion")
public class SesionApiServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final AutenticacionDao autenticacionDao = new AutenticacionDao();

    /**
     * Informa el estado de la sesión. {@code CsrfFiltro} ya garantizó que la
     * sesión tenga un token, que se entrega a React para sus formularios.
     */
    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, construirEstadoSesion(solicitud));
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        String accion = leerAccion(solicitud, "");
        if ("iniciar".equals(accion)) {
            iniciarSesion(solicitud, respuesta);
        } else if ("cerrar".equals(accion)) {
            cerrarSesion(solicitud, respuesta);
        } else {
            ConversorJson.responderError(respuesta, HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida.");
        }
    }

    /**
     * Valida las credenciales y crea la sesión.
     */
    private void iniciarSesion(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        String email = solicitud.getParameter("email");
        String contrasena = solicitud.getParameter("contrasena");
        if (email == null || email.isBlank() || contrasena == null || contrasena.isEmpty()) {
            ConversorJson.responderError(respuesta, 422, "Ingrese su correo electrónico y su contraseña.");
            return;
        }
        try {
            Optional<UsuarioSesion> usuario = autenticacionDao.autenticar(email, contrasena);
            if (usuario.isEmpty()) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                        "Correo electrónico o contraseña incorrectos.");
                return;
            }
            crearSesionAutenticada(solicitud, usuario.get());
            // La sesión cambió, por eso se devuelve el estado completo con el token CSRF nuevo.
            ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, construirEstadoSesion(solicitud));
        } catch (DaoException excepcion) {
            ConversorJson.responderError(respuesta, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    excepcion.getMessage());
        }
    }

    /**
     * Invalida la sesión y entrega una sesión anónima nueva con su propio token,
     * para que React pueda volver a iniciar sesión sin recargar la página.
     */
    private void cerrarSesion(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        CsrfFiltro.asegurarToken(solicitud.getSession(true));
        ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, construirEstadoSesion(solicitud));
    }

    /**
     * Arma la respuesta con el estado de la sesión.
     *
     * @param solicitud petición actual
     * @return mapa que Jackson convierte a JSON
     */
    private Map<String, Object> construirEstadoSesion(HttpServletRequest solicitud) {
        HttpSession sesion = solicitud.getSession(true);
        UsuarioSesion usuario = obtenerUsuarioSesion(solicitud);

        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("autenticado", usuario != null);
        estado.put("usuario", usuario == null ? null : describirUsuario(usuario));
        estado.put("tokenCsrf", sesion.getAttribute(CsrfFiltro.NOMBRE_TOKEN));
        // Listas de estados para las listas desplegables de React.
        estado.put("estados", Map.of(
                "estadosPedido", CatalogoEstados.ESTADOS_PEDIDO,
                "estadosEnvio", CatalogoEstados.ESTADOS_ENVIO,
                "estadosIncidencia", CatalogoEstados.ESTADOS_INCIDENCIA));
        return estado;
    }

    /**
     * Convierte el usuario de la sesión en un mapa con los datos que necesita la interfaz.
     *
     * @param usuario usuario autenticado
     * @return datos públicos del usuario (sin contraseña)
     */
    private static Map<String, Object> describirUsuario(UsuarioSesion usuario) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("id", usuario.getId());
        datos.put("nombre", usuario.getNombre());
        datos.put("email", usuario.getEmail());
        datos.put("iniciales", usuario.getIniciales());
        datos.put("rol", usuario.getRol().name());
        datos.put("rolEtiqueta", usuario.getRol().getEtiqueta());
        datos.put("personalInterno", usuario.getRol().isPersonalInterno());
        return datos;
    }
}
