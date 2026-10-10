package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.Factura;
import co.homecenter.envios.modelo.Pedido;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Factura} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class FacturaDao extends BaseDao<Factura> {

    /** Navega Factura &rarr; Pedido &rarr; Cliente para buscar por el nombre del cliente. */
    private static final String HQL_LISTAR =
            "select f from Factura f join fetch f.pedido p join fetch p.cliente c "
            + "where cast(f.idFactura as String) like :patron escape '!' "
            + "or cast(p.idPedido as String) like :patron escape '!' or c.nombre like :patron escape '!' "
            + "order by f.fechaEmision desc, f.idFactura desc";

    public FacturaDao() {
        super(Factura.class);
    }

    @Override
    public List<Factura> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    @Override
    protected void prepararParaGuardar(Session sesion, Factura factura, Factura existente) throws DaoException {
        factura.setPedido(resolverReferencia(sesion, Pedido.class, factura.getIdPedido(),
                "El pedido seleccionado ya no existe."));
    }
}
