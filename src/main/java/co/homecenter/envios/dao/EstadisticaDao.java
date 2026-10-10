package co.homecenter.envios.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Consultas de resumen para el panel de inicio, escritas en HQL con la
 * función de agregación {@code count}.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class EstadisticaDao {

    /** Estados que se consideran "pedido en proceso". */
    private static final List<String> ESTADOS_EN_PROCESO = List.of("PENDIENTE", "EN_PREPARACION", "DESPACHADO");

    /**
     * Cuenta los registros más relevantes del sistema.
     *
     * @return mapa ordenado indicador &rarr; cantidad, listo para iterarse en la vista
     * @throws DaoException si falla alguna consulta
     */
    public Map<String, Long> contarIndicadores() throws DaoException {
        return BaseDao.enSesion("consultar los indicadores", sesion -> {
            Map<String, Long> indicadores = new LinkedHashMap<>();
            indicadores.put("pedidosActivos", contar(sesion,
                    "select count(p) from Pedido p where p.estado in :valores", ESTADOS_EN_PROCESO));
            indicadores.put("pedidosEntregados", contar(sesion,
                    "select count(p) from Pedido p where p.estado in :valores", List.of("ENTREGADO")));
            indicadores.put("enviosEnRuta", contar(sesion,
                    "select count(e) from EnvioPedido e where e.estado in :valores", List.of("EN_RUTA")));
            indicadores.put("incidenciasAbiertas", contar(sesion,
                    "select count(i) from Incidencia i where i.estado not in :valores", List.of("RESUELTA")));
            indicadores.put("clientes", sesion
                    .createSelectionQuery("select count(c) from Cliente c", Long.class)
                    .getSingleResult());
            return indicadores;
        });
    }

    /**
     * Ejecuta una consulta {@code count} con un parámetro de lista.
     *
     * @param sesion  sesión abierta
     * @param hql     consulta con el parámetro {@code :valores}
     * @param valores valores para la cláusula {@code in}
     * @return cantidad de registros
     */
    private static long contar(Session sesion, String hql, List<String> valores) {
        return sesion.createSelectionQuery(hql, Long.class)
                .setParameterList("valores", valores)
                .getSingleResult();
    }
}
