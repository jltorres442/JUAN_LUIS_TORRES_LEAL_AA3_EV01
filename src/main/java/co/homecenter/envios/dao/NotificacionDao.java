package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.Notificacion;
import co.homecenter.envios.modelo.Transportista;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Notificacion} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class NotificacionDao extends BaseDao<Notificacion> {

    private static final String HQL_LISTAR =
            "select n from Notificacion n join fetch n.transportista t "
            + "where n.mensaje like :patron escape '!' or t.nombre like :patron escape '!' "
            + "order by n.fechaHora desc, n.idNotificacion desc";

    public NotificacionDao() {
        super(Notificacion.class);
    }

    @Override
    public List<Notificacion> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    @Override
    protected void prepararParaGuardar(Session sesion, Notificacion notificacion, Notificacion existente)
            throws DaoException {
        notificacion.setTransportista(resolverReferencia(sesion, Transportista.class,
                notificacion.getIdTransportista(), "El transportista seleccionado ya no existe."));
    }
}
