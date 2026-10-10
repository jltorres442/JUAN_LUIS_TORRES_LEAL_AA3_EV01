package co.homecenter.envios.controlador;

import co.homecenter.envios.dao.CrudDao;
import co.homecenter.envios.dao.DaoException;
import co.homecenter.envios.dao.EnvioPedidoDao;
import co.homecenter.envios.dao.TransportistaDao;
import co.homecenter.envios.modelo.Transportista;
import co.homecenter.envios.util.LectorFormulario;
import javax.servlet.annotation.WebServlet;
import java.util.List;
import java.util.Map;

/**
 * Módulo CRUD de transportistas. Atiende la ruta {@code /transportistas} (vistas JSP)
 * y {@code /api/transportistas} (API JSON para React).
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebServlet(name = "TransportistaServlet", urlPatterns = {"/transportistas", "/api/transportistas"})
public class TransportistaServlet extends CrudServlet<Transportista> {

    private static final long serialVersionUID = 1L;

    private final TransportistaDao transportistaDao = new TransportistaDao();
    private final EnvioPedidoDao envioPedidoDao = new EnvioPedidoDao();

    @Override
    protected CrudDao<Transportista> getDao() {
        return transportistaDao;
    }

    @Override
    protected String getCarpetaVistas() {
        return "transportistas";
    }

    @Override
    protected String getRutaModulo() {
        return "/transportistas";
    }

    @Override
    protected String getNombreEntidad() {
        return "El transportista";
    }

    @Override
    protected Transportista crearEntidadVacia() {
        return new Transportista();
    }

    @Override
    protected Transportista leerFormulario(LectorFormulario formulario, Integer id) {
        Transportista transportista = new Transportista();
        transportista.setIdTransportista(id);
        leerDatosUsuario(formulario, transportista, id == null);
        transportista.setLicencia(formulario.texto("licencia", "La licencia", 50, true));
        // El envío es opcional: un transportista puede estar disponible sin asignación.
        transportista.setIdEnvio(formulario.entero("idEnvio", "El envío", false));
        return transportista;
    }

    /** Envíos disponibles para asignar al transportista. */
    @Override
    protected Map<String, List<?>> cargarCatalogos() throws DaoException {
        return Map.of("envios", envioPedidoDao.listar(null));
    }
}
