package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.EnvioPedido;
import co.homecenter.envios.modelo.Logistica;
import co.homecenter.envios.modelo.Pedido;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link EnvioPedido} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class EnvioPedidoDao extends BaseDao<EnvioPedido> {

    private static final String HQL_LISTAR =
            "select e from EnvioPedido e join fetch e.pedido p join fetch p.cliente c join fetch e.logistica l "
            + "where cast(e.idEnvio as String) like :patron escape '!' "
            + "or e.direccionEntrega like :patron escape '!' or e.estado like :patron escape '!' "
            + "or c.nombre like :patron escape '!' "
            + "order by e.fechaEnvio desc, e.idEnvio desc";

    public EnvioPedidoDao() {
        super(EnvioPedido.class);
    }

    @Override
    public List<EnvioPedido> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    @Override
    protected void prepararParaGuardar(Session sesion, EnvioPedido envio, EnvioPedido existente)
            throws DaoException {
        envio.setPedido(resolverReferencia(sesion, Pedido.class, envio.getIdPedido(),
                "El pedido seleccionado ya no existe."));
        envio.setLogistica(resolverReferencia(sesion, Logistica.class, envio.getIdLogistica(),
                "El responsable de logística seleccionado ya no existe."));
    }
}
