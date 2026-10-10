package co.homecenter.envios.configuracion;

import co.homecenter.envios.modelo.Cliente;
import co.homecenter.envios.modelo.EnvioPedido;
import co.homecenter.envios.modelo.Factura;
import co.homecenter.envios.modelo.Incidencia;
import co.homecenter.envios.modelo.Logistica;
import co.homecenter.envios.modelo.Notificacion;
import co.homecenter.envios.modelo.Pedido;
import co.homecenter.envios.modelo.Transportista;
import co.homecenter.envios.modelo.Vendedor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

/**
 * Configura Hibernate y administra la única {@link SessionFactory} de la aplicación.
 *
 * <h2>¿Qué es la SessionFactory?</h2>
 * <p>Es el objeto central de Hibernate: conoce el mapeo de todas las
 * entidades y el pool de conexiones. Crearla es costoso, por eso existe una
 * sola instancia durante toda la vida de la aplicación (patrón
 * <em>Singleton</em>). A partir de ella cada operación abre una
 * {@link org.hibernate.Session} liviana y de corta duración.</p>
 *
 * <h2>Configuración</h2>
 * <p>Los parámetros se leen de {@code db.properties} (carpeta
 * {@code src/main/resources}); cada uno puede sobrescribirse con las
 * variables de entorno {@code HC_DB_URL}, {@code HC_DB_USUARIO} y
 * {@code HC_DB_CONTRASENA}.</p>
 *
 * <h2>Ciclo de vida</h2>
 * <p>{@link InicializadorAplicacion} llama a {@link #iniciar()} cuando Tomcat
 * despliega la aplicación y a {@link #cerrar()} cuando la detiene, para
 * liberar las conexiones del pool.</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public final class GestorHibernate {

    /** Archivo de configuración dentro del classpath. */
    private static final String ARCHIVO_PROPIEDADES = "db.properties";

    /** Clases anotadas con {@code @Entity} que Hibernate debe administrar. */
    private static final Class<?>[] ENTIDADES = {
        Cliente.class, Vendedor.class, Logistica.class, Transportista.class, Pedido.class,
        Factura.class, EnvioPedido.class, Incidencia.class, Notificacion.class
    };

    /**
     * Instancia única. {@code volatile} garantiza que todos los hilos de
     * Tomcat vean el valor actualizado tan pronto se asigna.
     */
    private static volatile SessionFactory fabricaSesiones;

    private GestorHibernate() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Construye la {@link SessionFactory} si todavía no existe.
     *
     * <p>Está sincronizado para que, si dos peticiones llegan al mismo tiempo
     * al arrancar, no se creen dos fábricas.</p>
     */
    public static synchronized void iniciar() {
        if (fabricaSesiones != null) {
            return;
        }
        StandardServiceRegistry registro = new StandardServiceRegistryBuilder()
                .applySettings(construirConfiguracion())
                .build();
        try {
            MetadataSources fuentes = new MetadataSources(registro);
            for (Class<?> entidad : ENTIDADES) {
                fuentes.addAnnotatedClass(entidad);
            }
            fabricaSesiones = fuentes.buildMetadata().buildSessionFactory();
        } catch (RuntimeException excepcion) {
            // Si el mapeo es inválido se libera el registro para no dejar hilos abiertos.
            StandardServiceRegistryBuilder.destroy(registro);
            throw excepcion;
        }
    }

    /**
     * Devuelve la fábrica de sesiones, creándola si es necesario.
     *
     * @return fábrica de sesiones lista para usar
     */
    public static SessionFactory obtenerFabrica() {
        SessionFactory fabrica = fabricaSesiones;
        if (fabrica == null) {
            iniciar();
            fabrica = fabricaSesiones;
        }
        return fabrica;
    }

    /**
     * Cierra la fábrica y el pool de conexiones. Se invoca al detener la
     * aplicación para evitar fugas de memoria en Tomcat.
     */
    public static synchronized void cerrar() {
        if (fabricaSesiones != null) {
            fabricaSesiones.close();
            fabricaSesiones = null;
        }
    }

    /**
     * Arma el mapa de propiedades de Hibernate a partir de {@code db.properties}.
     *
     * @return configuración para el {@link StandardServiceRegistryBuilder}
     */
    private static Map<String, Object> construirConfiguracion() {
        Properties propiedades = cargarPropiedades();
        Map<String, Object> configuracion = new HashMap<>();

        // --- Conexión JDBC ---
        configuracion.put(AvailableSettings.JAKARTA_JDBC_DRIVER, propiedades.getProperty("db.driver"));
        configuracion.put(AvailableSettings.JAKARTA_JDBC_URL, leer(propiedades, "HC_DB_URL", "db.url"));
        configuracion.put(AvailableSettings.JAKARTA_JDBC_USER, leer(propiedades, "HC_DB_USUARIO", "db.usuario"));
        configuracion.put(AvailableSettings.JAKARTA_JDBC_PASSWORD,
                leer(propiedades, "HC_DB_CONTRASENA", "db.contrasena"));

        // --- Dialecto: le indica a Hibernate qué variante de SQL generar ---
        configuracion.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect");

        // Permite que la aplicación arranque aunque MySQL esté apagado en ese momento;
        // el error se mostrará de forma amigable en la primera consulta.
        configuracion.put(AvailableSettings.ALLOW_METADATA_ON_BOOT, false);

        // --- Pool de conexiones HikariCP ---
        configuracion.put(AvailableSettings.CONNECTION_PROVIDER,
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        configuracion.put("hibernate.hikari.maximumPoolSize", propiedades.getProperty("db.pool.maximo", "10"));
        configuracion.put("hibernate.hikari.minimumIdle", "1");
        configuracion.put("hibernate.hikari.poolName", "HomecenterPool");
        // -1 = no fallar al arrancar si la base de datos aún no responde.
        configuracion.put("hibernate.hikari.initializationFailTimeout", "-1");

        // --- Rendimiento: carga en lotes de 16 las relaciones que no vienen con "join fetch" ---
        configuracion.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, 16);

        // --- Esquema: el script SQL crea las tablas; Hibernate no las modifica ---
        configuracion.put(AvailableSettings.HBM2DDL_AUTO, propiedades.getProperty("hibernate.esquema", "none"));

        // --- Depuración: true imprime en la consola de Tomcat cada SQL generado ---
        String mostrarSql = propiedades.getProperty("hibernate.mostrar_sql", "false");
        configuracion.put(AvailableSettings.SHOW_SQL, mostrarSql);
        configuracion.put(AvailableSettings.FORMAT_SQL, mostrarSql);

        return configuracion;
    }

    /**
     * Carga {@code db.properties} desde el classpath.
     *
     * @return propiedades leídas
     */
    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        ClassLoader cargador = Thread.currentThread().getContextClassLoader();
        try (InputStream entrada = cargador.getResourceAsStream(ARCHIVO_PROPIEDADES)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO_PROPIEDADES + " en el classpath");
            }
            propiedades.load(entrada);
        } catch (IOException excepcion) {
            throw new IllegalStateException("No fue posible leer " + ARCHIVO_PROPIEDADES, excepcion);
        }
        return propiedades;
    }

    /**
     * Devuelve un parámetro dando prioridad a la variable de entorno.
     *
     * @param propiedades     propiedades del archivo
     * @param variableEntorno nombre de la variable de entorno
     * @param clavePropiedad  clave dentro de {@code db.properties}
     * @return valor configurado (cadena vacía si no existe)
     */
    private static String leer(Properties propiedades, String variableEntorno, String clavePropiedad) {
        String valorEntorno = System.getenv(variableEntorno);
        return valorEntorno != null ? valorEntorno : propiedades.getProperty(clavePropiedad, "");
    }
}
