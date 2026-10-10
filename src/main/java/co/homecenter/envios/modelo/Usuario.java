package co.homecenter.envios.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Datos comunes de los actores que pueden iniciar sesión en el sistema:
 * {@link Cliente}, {@link Vendedor}, {@link Logistica} y {@link Transportista}.
 *
 * <p><strong>Mapeo Hibernate:</strong> se anota con {@link MappedSuperclass}
 * porque en la base de datos no existe una tabla "Usuario". Hibernate copia
 * estas columnas en la tabla de cada subclase (Cliente, Vendedor, etc.), de
 * modo que la herencia existe solo en Java y evita repetir atributos.</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@MappedSuperclass
public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Cédula, NIT o pasaporte. Único por tabla. */
    @Column(name = "numero_identificacion", nullable = false, length = 20)
    private String numeroIdentificacion;

    /** Nombre completo. */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Correo electrónico, usado como nombre de usuario al iniciar sesión. */
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /**
     * Hash PBKDF2 de la contraseña, tal como está en la base de datos.
     * Nunca contiene la contraseña en texto plano y nunca se envía en el JSON
     * de la API (ver {@link #getContrasena()}).
     */
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    /** Número de teléfono o celular de contacto. */
    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * {@link JsonIgnore} impide que Jackson incluya el hash en las respuestas
     * JSON que consume React: el hash no debe salir nunca del servidor.
     *
     * @return hash de la contraseña
     */
    @JsonIgnore
    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
