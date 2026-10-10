package co.homecenter.envios.filtro;

import co.homecenter.envios.controlador.ServletBase;
import co.homecenter.envios.modelo.UsuarioSesion;
import co.homecenter.envios.util.ConversorJson;
import java.io.IOException;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controla el acceso a las páginas protegidas.
 *
 * <ol>
 *   <li><strong>Autenticación</strong>: si no hay usuario en sesión, redirige a
 *       {@code /login} (JSP) o responde 401 en JSON (API de React).</li>
 *   <li><strong>Autorización</strong>: los clientes solo pueden ver su panel de inicio;
 *       los módulos de administración son exclusivos del personal interno
 *       (403 en HTML o en JSON según el caso).</li>
 * </ol>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class AutenticacionFiltro implements Filter {

    /**
     * Rutas accesibles sin iniciar sesión. {@code /api/sesion} es pública porque
     * React la usa precisamente para iniciar sesión; {@code /app} es la
     * aplicación React, que muestra su propio formulario de ingreso.
     */
    private static final Set<String> RUTAS_PUBLICAS = Set.of("/login", "/registro", "/api/sesion", "/app");

    /** Prefijos de recursos estáticos (estilos, scripts, imágenes y la aplicación React compilada). */
    private static final Set<String> PREFIJOS_ESTATICOS = Set.of("/css/", "/js/", "/img/", "/app/");

    /** Rutas que un cliente puede usar (versión JSP y versión API). */
    private static final Set<String> RUTAS_CLIENTE = Set.of("/inicio", "/cerrar-sesion", "/api/inicio");

    @Override
    public void init(FilterConfig configuracion) {
        // No requiere configuración inicial.
    }

    @Override
    public void destroy() {
        // No hay recursos que liberar.
    }

    @Override
    public void doFilter(ServletRequest solicitudServlet, ServletResponse respuestaServlet, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest solicitud = (HttpServletRequest) solicitudServlet;
        HttpServletResponse respuesta = (HttpServletResponse) respuestaServlet;
        String ruta = solicitud.getServletPath();

        if (esRutaPublica(ruta)) {
            cadena.doFilter(solicitud, respuesta);
            return;
        }

        HttpSession sesion = solicitud.getSession(false);
        UsuarioSesion usuario = sesion == null ? null
                : (UsuarioSesion) sesion.getAttribute(ServletBase.ATRIBUTO_USUARIO);

        boolean esApi = ServletBase.esPeticionApi(solicitud);

        if (usuario == null) {
            if (esApi) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                        "Debe iniciar sesión para continuar.");
            } else {
                respuesta.sendRedirect(solicitud.getContextPath() + "/login");
            }
            return;
        }

        if (!usuario.getRol().isPersonalInterno() && !RUTAS_CLIENTE.contains(ruta)) {
            if (esApi) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_FORBIDDEN,
                        "Su rol no tiene permiso para esta sección.");
            } else {
                respuesta.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        // Evita que el botón "Atrás" muestre páginas privadas después de cerrar sesión.
        respuesta.setHeader("Cache-Control", "no-store");
        cadena.doFilter(solicitud, respuesta);
    }

    /**
     * @param ruta ruta del servlet solicitada
     * @return {@code true} si la ruta no requiere autenticación
     */
    private boolean esRutaPublica(String ruta) {
        if (RUTAS_PUBLICAS.contains(ruta)) {
            return true;
        }
        for (String prefijo : PREFIJOS_ESTATICOS) {
            if (ruta.startsWith(prefijo)) {
                return true;
            }
        }
        return false;
    }
}
