package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.AutenticacionDao;
import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.modelo.UsuarioSesion;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Inicio de sesión con correo electrónico y contraseña (versión JSP). Atiende {@code /login}.
 *
 * <ul>
 *   <li><strong>GET</strong>: muestra el formulario.</li>
 *   <li><strong>POST</strong>: valida las credenciales y crea la sesión.</li>
 * </ul>
 *
 * <p>La versión React usa {@link SesionApiServlet} ({@code /api/sesion}).</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final AutenticacionDao autenticacionDao = new AutenticacionDao();

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        // Si ya inició sesión no tiene sentido volver a mostrar el formulario.
        if (obtenerUsuarioSesion(solicitud) != null) {
            redirigir(solicitud, respuesta, "/inicio");
            return;
        }
        mostrarVista(solicitud, respuesta, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String email = solicitud.getParameter("email");
        String contrasena = solicitud.getParameter("contrasena");
        solicitud.setAttribute("email", email);

        if (email == null || email.isBlank() || contrasena == null || contrasena.isEmpty()) {
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, "Ingrese su correo electrónico y su contraseña.");
            mostrarVista(solicitud, respuesta, "login.jsp");
            return;
        }

        try {
            Optional<UsuarioSesion> usuario = autenticacionDao.autenticar(email, contrasena);
            if (usuario.isEmpty()) {
                // Mensaje genérico: no se revela si el correo existe o no.
                solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, "Correo electrónico o contraseña incorrectos.");
                mostrarVista(solicitud, respuesta, "login.jsp");
                return;
            }
            crearSesionAutenticada(solicitud, usuario.get())
                    .setAttribute(ATRIBUTO_MENSAJE_EXITO, "¡Bienvenido(a), " + usuario.get().getNombre() + "!");
            redirigir(solicitud, respuesta, "/inicio");
        } catch (DaoException excepcion) {
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, excepcion.getMessage());
            mostrarVista(solicitud, respuesta, "login.jsp");
        }
    }
}
