package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.CrudDao;
import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.dao.EnvioPedidoDao;
import co.homecenter.envios.dao.LogisticaDao;
import co.homecenter.envios.dao.PedidoDao;
import co.homecenter.envios.modelo.EnvioPedido;
import co.homecenter.envios.util.CatalogoEstados;
import co.homecenter.envios.util.LectorFormulario;
import java.time.LocalDate;
import javax.servlet.annotation.WebServlet;
import java.util.List;
import java.util.Map;

/**
 * Módulo CRUD de envíos de pedidos. Atiende la ruta {@code /envios} (vistas JSP)
 * y {@code /api/envios} (API JSON para React).
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebServlet(name = "EnvioPedidoServlet", urlPatterns = {"/envios", "/api/envios"})
public class EnvioPedidoServlet extends CrudServlet<EnvioPedido> {

    private static final long serialVersionUID = 1L;

    private final EnvioPedidoDao envioPedidoDao = new EnvioPedidoDao();
    private final PedidoDao pedidoDao = new PedidoDao();
    private final LogisticaDao logisticaDao = new LogisticaDao();

    @Override
    protected CrudDao<EnvioPedido> getDao() {
        return envioPedidoDao;
    }

    @Override
    protected String getCarpetaVistas() {
        return "envios";
    }

    @Override
    protected String getRutaModulo() {
        return "/envios";
    }

    @Override
    protected String getNombreEntidad() {
        return "El envío";
    }

    @Override
    protected EnvioPedido crearEntidadVacia() {
        EnvioPedido envio = new EnvioPedido();
        envio.setFechaEnvio(LocalDate.now());
        envio.setEstado(CatalogoEstados.ESTADOS_ENVIO.get(0));
        return envio;
    }

    @Override
    protected EnvioPedido leerFormulario(LectorFormulario formulario, Integer id) {
        EnvioPedido envio = new EnvioPedido();
        envio.setIdEnvio(id);
        envio.setFechaEnvio(formulario.fecha("fechaEnvio", "La fecha de envío", true));
        envio.setDireccionEntrega(formulario.texto("direccionEntrega", "La dirección de entrega", 250, true));
        envio.setEstado(formulario.opcion("estado", "El estado", CatalogoEstados.ESTADOS_ENVIO));
        envio.setIdPedido(formulario.entero("idPedido", "El pedido", true));
        envio.setIdLogistica(formulario.entero("idLogistica", "El responsable de logística", true));
        return envio;
    }

    @Override
    protected Map<String, List<?>> cargarCatalogos() throws DaoException {
        return Map.of("pedidos", pedidoDao.listar(null), "responsablesLogistica", logisticaDao.listar(null));
    }
}
