package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.EnvioPedido;
import co.homecenter.envios.modelo.Incidencia;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Incidencia} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class IncidenciaDao extends BaseDao<Incidencia> {

    private static final String HQL_LISTAR =
            "select i from Incidencia i join fetch i.envio e "
            + "where i.descripcion like :patron escape '!' or i.estado like :patron escape '!' "
            + "or cast(e.idEnvio as String) like :patron escape '!' "
            + "order by i.idIncidencia desc";

    public IncidenciaDao() {
        super(Incidencia.class);
    }

    @Override
    public List<Incidencia> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    @Override
    protected void prepararParaGuardar(Session sesion, Incidencia incidencia, Incidencia existente)
            throws DaoException {
        incidencia.setEnvio(resolverReferencia(sesion, EnvioPedido.class, incidencia.getIdEnvio(),
                "El envío seleccionado ya no existe."));
    }
}
