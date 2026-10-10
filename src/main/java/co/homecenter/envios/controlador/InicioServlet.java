package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.dao.EstadisticaDao;
import co.homecenter.envios.dao.PedidoDao;
import co.homecenter.envios.modelo.Pedido;
import co.homecenter.envios.modelo.UsuarioSesion;
import co.homecenter.envios.util.ConversorJson;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Panel principal después de iniciar sesión. Atiende {@code /inicio} (JSP)
 * y {@code /api/inicio} (JSON para React).
 *
 * <p>El contenido depende del rol: el personal interno ve indicadores y los
 * pedidos recientes; el cliente ve el historial de sus propios pedidos.</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebServlet(name = "InicioServlet", urlPatterns = {"/inicio", "/api/inicio"})
public class InicioServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    /** Cantidad de pedidos recientes que se muestran en el panel. */
    private static final int CANTIDAD_PEDIDOS_RECIENTES = 5;

    private final EstadisticaDao estadisticaDao = new EstadisticaDao();
    private final PedidoDao pedidoDao = new PedidoDao();

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        UsuarioSesion usuario = obtenerUsuarioSesion(solicitud);
        Map<String, Long> indicadores = null;
        List<Pedido> pedidos = List.of();
        String mensajeError = null;

        try {
            if (usuario.getRol().isPersonalInterno()) {
                indicadores = estadisticaDao.contarIndicadores();
                pedidos = pedidoDao.listarRecientes(CANTIDAD_PEDIDOS_RECIENTES);
            } else {
                pedidos = pedidoDao.listarPorCliente(usuario.getId());
            }
        } catch (DaoException excepcion) {
            mensajeError = excepcion.getMessage();
        }

        if (esPeticionApi(solicitud)) {
            if (mensajeError != null) {
                ConversorJson.responderError(respuesta, HttpServletResponse.SC_SERVICE_UNAVAILABLE, mensajeError);
                return;
            }
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("indicadores", indicadores);
            cuerpo.put("pedidos", pedidos);
            ConversorJson.responder(respuesta, HttpServletResponse.SC_OK, cuerpo);
            return;
        }

        solicitud.setAttribute("indicadores", indicadores);
        solicitud.setAttribute("pedidos", pedidos);
        if (mensajeError != null) {
            solicitud.setAttribute(ATRIBUTO_MENSAJE_ERROR, mensajeError);
        }
        mostrarVista(solicitud, respuesta, "inicio.jsp");
    }
}
