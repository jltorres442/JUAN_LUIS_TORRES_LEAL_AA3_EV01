package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.Cliente;
import co.homecenter.envios.modelo.Pedido;
import co.homecenter.envios.modelo.Vendedor;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Pedido} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class PedidoDao extends BaseDao<Pedido> {

    /**
     * Parte común de las consultas. {@code join fetch} le indica a Hibernate
     * que traiga el cliente y el vendedor en el mismo SELECT (un solo viaje a
     * la base de datos en lugar de uno por pedido).
     */
    private static final String HQL_BASE =
            "select p from Pedido p join fetch p.cliente c join fetch p.vendedor v ";

    /** Busca por número de pedido, estado, cliente o vendedor. */
    private static final String HQL_LISTAR =
            HQL_BASE
            + "where cast(p.idPedido as String) like :patron escape '!' or p.estado like :patron escape '!' "
            + "or c.nombre like :patron escape '!' or v.nombre like :patron escape '!' "
            + "order by p.fecha desc, p.idPedido desc";

    private static final String HQL_LISTAR_POR_CLIENTE =
            HQL_BASE + "where c.idCliente = :idCliente order by p.fecha desc, p.idPedido desc";

    private static final String HQL_LISTAR_RECIENTES =
            HQL_BASE + "order by p.fecha desc, p.idPedido desc";

    public PedidoDao() {
        super(Pedido.class);
    }

    @Override
    public List<Pedido> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    /**
     * Consulta los pedidos de un cliente, para su portal personal.
     *
     * @param idCliente llave primaria del cliente
     * @return pedidos del cliente, del más reciente al más antiguo
     * @throws DaoException si falla la consulta
     */
    public List<Pedido> listarPorCliente(int idCliente) throws DaoException {
        return consultarLista(HQL_LISTAR_POR_CLIENTE, Map.of("idCliente", idCliente));
    }

    /**
     * Consulta los pedidos más recientes para el panel de inicio.
     *
     * @param cantidad número máximo de pedidos
     * @return pedidos recientes
     * @throws DaoException si falla la consulta
     */
    public List<Pedido> listarRecientes(int cantidad) throws DaoException {
        return consultarLista(HQL_LISTAR_RECIENTES, Map.of(), cantidad);
    }

    /** Reemplaza las referencias de cliente y vendedor (solo id) por las entidades reales. */
    @Override
    protected void prepararParaGuardar(Session sesion, Pedido pedido, Pedido existente) throws DaoException {
        pedido.setCliente(resolverReferencia(sesion, Cliente.class, pedido.getIdCliente(),
                "El cliente seleccionado ya no existe."));
        pedido.setVendedor(resolverReferencia(sesion, Vendedor.class, pedido.getIdVendedor(),
                "El vendedor seleccionado ya no existe."));
    }
}
