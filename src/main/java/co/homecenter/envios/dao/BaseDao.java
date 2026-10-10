package co.homecenter.envios.dao;

import co.homecenter.envios.configuracion.GestorHibernate;
import co.homecenter.envios.modelo.Usuario;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;

/**
 * Implementación genérica de las operaciones CRUD con Hibernate.
 *
 * <h2>Cómo trabaja Hibernate en esta clase</h2>
 * <ol>
 *   <li>Cada operación abre una {@link Session} (unidad de trabajo) a partir de la
 *       {@link SessionFactory} y la cierra al terminar (<em>try-with-resources</em>).</li>
 *   <li>Las operaciones que modifican datos se ejecutan dentro de una
 *       {@link Transaction}: si algo falla se hace <em>rollback</em> y la base de
 *       datos queda como estaba.</li>
 *   <li>Hibernate genera el SQL: {@code persist} &rarr; INSERT, {@code merge} &rarr;
 *       UPDATE, {@code remove} &rarr; DELETE y {@code find} &rarr; SELECT por llave.</li>
 *   <li>Las búsquedas usan HQL (Hibernate Query Language), que consulta
 *       <em>entidades y atributos de Java</em> en lugar de tablas y columnas.
 *       Los valores van como parámetros con nombre ({@code :patron}), lo que
 *       previene la inyección SQL.</li>
 * </ol>
 *
 * <p>Las subclases solo implementan {@link #listar(String)} (su consulta HQL)
 * y, si tienen relaciones, {@link #prepararParaGuardar(Session, Object, Object)}.</p>
 *
 * @param <T> entidad JPA administrada
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public abstract class BaseDao<T> implements CrudDao<T> {

    private static final Logger REGISTRO = Logger.getLogger(BaseDao.class.getName());

    /** Carácter de escape para los comodines de {@code LIKE} (ver {@link #patronBusqueda(String)}). */
    protected static final char CARACTER_ESCAPE = '!';

    /** Código MySQL: valor duplicado en una llave única. */
    private static final int ERROR_DUPLICADO = 1062;
    /** Código MySQL: no se puede borrar una fila padre con hijos. */
    private static final int ERROR_FILA_REFERENCIADA = 1451;
    /** Código MySQL: la llave foránea apunta a una fila inexistente. */
    private static final int ERROR_REFERENCIA_INEXISTENTE = 1452;
    /** Códigos MySQL (3819) y MariaDB (4025): se violó una restricción CHECK. */
    private static final int ERROR_CHECK_MYSQL = 3819;
    private static final int ERROR_CHECK_MARIADB = 4025;

    /**
     * Operación que se ejecuta con una sesión abierta.
     *
     * @param <R> tipo del resultado
     */
    @FunctionalInterface
    protected interface OperacionSesion<R> {
        R ejecutar(Session sesion) throws DaoException;
    }

    /** Clase de la entidad, necesaria para {@code find} y para las consultas tipadas. */
    private final Class<T> claseEntidad;

    /**
     * @param claseEntidad clase anotada con {@code @Entity}
     */
    protected BaseDao(Class<T> claseEntidad) {
        this.claseEntidad = claseEntidad;
    }

    // ------------------------------------------------------------------
    // Operaciones CRUD
    // ------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>{@code persist} programa el INSERT y {@code flush} lo ejecuta de
     * inmediato para obtener la llave generada por {@code AUTO_INCREMENT}.</p>
     */
    @Override
    public int insertar(T entidad) throws DaoException {
        return enTransaccion("guardar el registro", sesion -> {
            prepararParaGuardar(sesion, entidad, null);
            sesion.persist(entidad);
            sesion.flush();
            return (Integer) obtenerIdentificador(entidad);
        });
    }

    @Override
    public Optional<T> buscarPorId(int id) throws DaoException {
        return enSesion("consultar la información",
                sesion -> Optional.ofNullable(sesion.find(claseEntidad, id)));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Primero se carga el registro actual con {@code find}; si no existe se
     * devuelve {@code false}. Luego {@code merge} copia los datos nuevos sobre
     * el registro cargado y Hibernate genera el UPDATE solo con lo necesario.</p>
     */
    @Override
    public boolean actualizar(T entidad) throws DaoException {
        return enTransaccion("modificar el registro", sesion -> {
            Object id = obtenerIdentificador(entidad);
            T existente = sesion.find(claseEntidad, id);
            if (existente == null) {
                return false;
            }
            prepararParaGuardar(sesion, entidad, existente);
            sesion.merge(entidad);
            sesion.flush();
            return true;
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Las reglas {@code ON DELETE CASCADE / RESTRICT / SET NULL} definidas en
     * el script SQL se siguen aplicando: si el registro tiene dependencias
     * protegidas, MySQL rechaza el DELETE y se muestra un mensaje amigable.</p>
     */
    @Override
    public boolean eliminar(int id) throws DaoException {
        return enTransaccion("eliminar el registro", sesion -> {
            T existente = sesion.find(claseEntidad, id);
            if (existente == null) {
                return false;
            }
            sesion.remove(existente);
            sesion.flush();
            return true;
        });
    }

    // ------------------------------------------------------------------
    // Puntos de extensión para las subclases
    // ------------------------------------------------------------------

    /**
     * Se ejecuta antes de un INSERT o UPDATE, dentro de la transacción.
     * Las subclases lo usan para reemplazar las referencias por id (creadas
     * en el formulario) por las entidades reales, o para conservar datos.
     *
     * @param sesion    sesión abierta
     * @param entidad   datos a guardar
     * @param existente registro actual (solo al actualizar; {@code null} al insertar)
     * @throws DaoException si una referencia no existe
     */
    protected void prepararParaGuardar(Session sesion, T entidad, T existente) throws DaoException {
        // Por defecto no hay nada que preparar.
    }

    // ------------------------------------------------------------------
    // Utilidades para las subclases
    // ------------------------------------------------------------------

    /**
     * Ejecuta una consulta HQL de selección y devuelve la lista de entidades.
     *
     * @param hql        consulta HQL con parámetros con nombre
     * @param parametros valores de los parámetros
     * @return entidades encontradas
     * @throws DaoException si falla la consulta
     */
    protected List<T> consultarLista(String hql, Map<String, Object> parametros) throws DaoException {
        return consultarLista(hql, parametros, 0);
    }

    /**
     * Igual que {@link #consultarLista(String, Map)} pero limitando el número de resultados.
     *
     * @param hql           consulta HQL
     * @param parametros    valores de los parámetros
     * @param maximoFilas   máximo de resultados (0 = sin límite)
     * @return entidades encontradas
     * @throws DaoException si falla la consulta
     */
    protected List<T> consultarLista(String hql, Map<String, Object> parametros, int maximoFilas)
            throws DaoException {
        return enSesion("consultar la información", sesion -> {
            SelectionQuery<T> consulta = sesion.createSelectionQuery(hql, claseEntidad);
            parametros.forEach(consulta::setParameter);
            if (maximoFilas > 0) {
                consulta.setMaxResults(maximoFilas);
            }
            return consulta.getResultList();
        });
    }

    /**
     * Busca la entidad real que corresponde a una referencia por id.
     *
     * @param sesion        sesión abierta
     * @param clase         clase de la entidad referenciada
     * @param id            llave primaria (puede ser {@code null} en relaciones opcionales)
     * @param mensajeError  mensaje si el registro ya no existe
     * @param <E>           tipo de la entidad referenciada
     * @return entidad encontrada, o {@code null} si {@code id} es {@code null}
     * @throws DaoException si el id no existe en la base de datos
     */
    protected static <E> E resolverReferencia(Session sesion, Class<E> clase, Integer id, String mensajeError)
            throws DaoException {
        if (id == null) {
            return null;
        }
        E entidad = sesion.find(clase, id);
        if (entidad == null) {
            throw new DaoException(mensajeError, null);
        }
        return entidad;
    }

    /**
     * Al editar un usuario, si el formulario dejó la contraseña vacía
     * ({@code null}), se conserva el hash que ya estaba guardado.
     *
     * @param usuario   datos nuevos
     * @param existente registro actual ({@code null} al insertar)
     */
    protected static void conservarContrasena(Usuario usuario, Usuario existente) {
        if (existente != null && usuario.getContrasena() == null) {
            usuario.setContrasena(existente.getContrasena());
        }
    }

    /**
     * Construye el patrón para una búsqueda con {@code LIKE}.
     * Los comodines que escriba el usuario ({@code %} y {@code _}) se escapan
     * para que se busquen de forma literal.
     *
     * @param filtro texto digitado por el usuario (puede ser {@code null})
     * @return {@code "%texto%"}, o {@code "%"} si no hay filtro
     */
    protected static String patronBusqueda(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return "%";
        }
        String escape = String.valueOf(CARACTER_ESCAPE);
        String textoEscapado = filtro.trim()
                .replace(escape, escape + escape)
                .replace("%", escape + "%")
                .replace("_", escape + "_");
        return "%" + textoEscapado + "%";
    }

    /**
     * Abre una sesión de solo lectura, ejecuta la operación y la cierra.
     *
     * @param accion    descripción de la acción para los mensajes de error
     * @param operacion trabajo a realizar
     * @param <R>       tipo del resultado
     * @return resultado de la operación
     * @throws DaoException si ocurre un error
     */
    protected static <R> R enSesion(String accion, OperacionSesion<R> operacion) throws DaoException {
        try (Session sesion = fabrica().openSession()) {
            return operacion.ejecutar(sesion);
        } catch (RuntimeException excepcion) {
            throw traducirError(excepcion, accion);
        }
    }

    /**
     * Abre una sesión, inicia una transacción, ejecuta la operación y confirma
     * ({@code commit}). Si algo falla, deshace los cambios ({@code rollback}).
     *
     * @param accion    descripción de la acción para los mensajes de error
     * @param operacion trabajo a realizar
     * @param <R>       tipo del resultado
     * @return resultado de la operación
     * @throws DaoException si ocurre un error
     */
    protected static <R> R enTransaccion(String accion, OperacionSesion<R> operacion) throws DaoException {
        try (Session sesion = fabrica().openSession()) {
            Transaction transaccion = sesion.beginTransaction();
            try {
                R resultado = operacion.ejecutar(sesion);
                transaccion.commit();
                return resultado;
            } catch (DaoException | RuntimeException excepcion) {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
                throw excepcion;
            }
        } catch (RuntimeException excepcion) {
            throw traducirError(excepcion, accion);
        }
    }

    // ------------------------------------------------------------------
    // Métodos internos
    // ------------------------------------------------------------------

    /** @return fábrica de sesiones de la aplicación */
    private static SessionFactory fabrica() {
        return GestorHibernate.obtenerFabrica();
    }

    /**
     * Lee la llave primaria de una entidad usando los metadatos de JPA.
     *
     * @param entidad entidad con id asignado
     * @return valor de la llave primaria
     */
    private static Object obtenerIdentificador(Object entidad) {
        return fabrica().getPersistenceUnitUtil().getIdentifier(entidad);
    }

    /**
     * Convierte una excepción de Hibernate en una {@link DaoException} con un
     * mensaje entendible para el usuario.
     *
     * <p>Hibernate envuelve la {@link SQLException} original de MySQL; se
     * recorre la cadena de causas para encontrarla y leer su código de error.</p>
     *
     * @param excepcion error lanzado por Hibernate
     * @param accion    descripción de lo que se intentaba hacer
     * @return excepción lista para lanzar
     */
    private static DaoException traducirError(RuntimeException excepcion, String accion) {
        for (Throwable causa = excepcion; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException) {
                return traducirErrorSql((SQLException) causa, excepcion, accion);
            }
        }
        REGISTRO.log(Level.SEVERE, "Error de Hibernate al " + accion, excepcion);
        return new DaoException("No fue posible " + accion
                + ". Verifique la conexión con la base de datos e intente de nuevo.", excepcion);
    }

    /**
     * Traduce los códigos de error de MySQL a mensajes para el usuario.
     *
     * @param errorSql  excepción SQL encontrada en la cadena de causas
     * @param original  excepción completa (para el registro)
     * @param accion    descripción de lo que se intentaba hacer
     * @return excepción con el mensaje adecuado
     */
    private static DaoException traducirErrorSql(SQLException errorSql, RuntimeException original, String accion) {
        String mensajeTecnico = String.valueOf(errorSql.getMessage()).toLowerCase();
        String mensajeUsuario;

        switch (errorSql.getErrorCode()) {
            case ERROR_DUPLICADO:
                if (mensajeTecnico.contains("email")) {
                    mensajeUsuario = "Ya existe un registro con ese correo electrónico.";
                } else if (mensajeTecnico.contains("identificacion")) {
                    mensajeUsuario = "Ya existe un registro con ese número de identificación.";
                } else if (mensajeTecnico.contains("pedido")) {
                    mensajeUsuario = "El pedido seleccionado ya tiene un registro asociado.";
                } else {
                    mensajeUsuario = "El registro ya existe.";
                }
                break;
            case ERROR_FILA_REFERENCIADA:
                mensajeUsuario = "No se puede eliminar: hay otros registros que dependen de este.";
                break;
            case ERROR_REFERENCIA_INEXISTENTE:
                mensajeUsuario = "El registro relacionado que seleccionó ya no existe.";
                break;
            case ERROR_CHECK_MYSQL:
            case ERROR_CHECK_MARIADB:
                mensajeUsuario = "Alguno de los valores no cumple las reglas de la base de datos.";
                break;
            default:
                REGISTRO.log(Level.SEVERE, "Error SQL al " + accion, original);
                mensajeUsuario = "No fue posible " + accion
                        + ". Verifique la conexión con la base de datos e intente de nuevo.";
                break;
        }
        return new DaoException(mensajeUsuario, original);
    }
}
