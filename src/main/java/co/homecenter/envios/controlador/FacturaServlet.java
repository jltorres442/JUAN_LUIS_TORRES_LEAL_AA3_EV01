package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.CrudDao;
import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.dao.FacturaDao;
import co.homecenter.envios.dao.PedidoDao;
import co.homecenter.envios.modelo.Factura;
import co.homecenter.envios.util.LectorFormulario;
import java.time.LocalDate;
import javax.servlet.annotation.WebServlet;
import java.util.List;
import java.util.Map;

/**
 * Módulo CRUD de facturas. Atiende la ruta {@code /facturas} (vistas JSP)
 * y {@code /api/facturas} (API JSON para React).
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebServlet(name = "FacturaServlet", urlPatterns = {"/facturas", "/api/facturas"})
public class FacturaServlet extends CrudServlet<Factura> {

    private static final long serialVersionUID = 1L;

    private final FacturaDao facturaDao = new FacturaDao();
    private final PedidoDao pedidoDao = new PedidoDao();

    @Override
    protected CrudDao<Factura> getDao() {
        return facturaDao;
    }

    @Override
    protected String getCarpetaVistas() {
        return "facturas";
    }

    @Override
    protected String getRutaModulo() {
        return "/facturas";
    }

    @Override
    protected String getNombreEntidad() {
        return "La factura";
    }

    @Override
    protected Factura crearEntidadVacia() {
        Factura factura = new Factura();
        factura.setFechaEmision(LocalDate.now());
        return factura;
    }

    @Override
    protected Factura leerFormulario(LectorFormulario formulario, Integer id) {
        Factura factura = new Factura();
        factura.setIdFactura(id);
        factura.setFechaEmision(formulario.fecha("fechaEmision", "La fecha de emisión", true));
        factura.setMontoTotal(formulario.decimal("montoTotal", "El monto total", true));
        factura.setIdPedido(formulario.entero("idPedido", "El pedido", true));
        return factura;
    }

    @Override
    protected Map<String, List<?>> cargarCatalogos() throws DaoException {
        return Map.of("pedidos", pedidoDao.listar(null));
    }
}
