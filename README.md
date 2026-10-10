# Homecenter Envíos — Java + Hibernate + React

**Autores:** Juan Luis Torres Leal y Angela María Constain Escobar
**Programa:** SENA · Análisis y Desarrollo de Software (3186607)

Sistema de realización y seguimiento de pedidos de Homecenter. Implementa el
modelo relacional de las evidencias GA6 (Cliente, Vendedor, Pedido, Factura,
Logística, Envío de pedido, Transportista, Incidencia y Notificación) con
**inserción, consulta, actualización y eliminación** en las 9 tablas, a través
de **dos interfaces** que comparten el mismo backend:

| Interfaz | Dirección | Tecnología |
|---|---|---|
| Versión JSP | `http://localhost:8080/homecenter-envios/` | Servlets + JSP + JSTL |
| Versión React | `http://localhost:8080/homecenter-envios/app/` | React 19 + JSX (Vite) |

```
 Navegador ──► Versión JSP  (HTML) ─┐
                                    ├─► Servlets ─► DAO (Hibernate ORM 7) ─► MySQL
 Navegador ──► Versión React (JSON) ┘      ▲
                                           └ Filtros: UTF-8 · CSRF · sesión y rol
```

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java (JDK) | 17 o superior (probado con JDK 24 de NetBeans) |
| Web | Servlets / JSP / JSTL (`javax.servlet`) | 3.1 · 2.3 · 1.2 |
| Persistencia | **Hibernate ORM** (JPA / Jakarta Persistence 3.2) + HikariCP | 7.4 |
| JSON | Jackson | 2.22 |
| Frontend | **React + React Router**, compilado con **Vite** | 19 · 7 · 8 |
| Servidor | Apache Tomcat | 8.5 (XAMPP) o 9 (NetBeans) |
| Base de datos | MySQL 8+ o MariaDB 10.5+ | — |

---

## 1. Requisitos

- **JDK 17+** (NetBeans ya trae uno).
- **Apache Tomcat 8.5 o 9** (el de XAMPP o el configurado en NetBeans).
- **MySQL** con usuario y contraseña.
- **Node.js 20.19+** (LTS) — solo si va a modificar o recompilar el frontend React.
  Verifique la instalación con `node -v` y `npm -v`.

## 2. Pasos de ejecución

### Paso 1 · Crear la base de datos

Abra `database/homecenter_db.sql` en MySQL Workbench y ejecútelo completo (botón ⚡),
o desde la consola:

```bash
mysql -u root -p < database/homecenter_db.sql
```

> ⚠️ El script **borra y vuelve a crear** `homecenter_db`.

Todas las cuentas de ejemplo usan la contraseña **`Homecenter2026*`**:

| Rol | Correo |
|---|---|
| Vendedor | andres.castillo@homecenter.com |
| Logística | jorge.ramirez@homecenter.com |
| Transportista | diego.martinez@homecenter.com |
| Cliente | maria.rojas@correo.com |

### Paso 2 · Configurar la conexión

Edite `src/main/resources/db.properties` y escriba la contraseña de **su** MySQL:

```properties
db.usuario=root
db.contrasena=SuContraseñaDeMySQL
```

### Paso 3 · Compilar el frontend React (opcional)

El proyecto **ya incluye** la versión compilada en `src/main/webapp/app/`, así que
este paso solo es necesario si modifica algún archivo de la carpeta `frontend/`:

```bash
cd frontend
npm install
npm run build
```

`npm install` descarga las dependencias (solo la primera vez) y `npm run build`
compila el JSX y deja el resultado en `src/main/webapp/app/`.

> En PowerShell, si aparece *"la ejecución de scripts está deshabilitada"*, use
> `npm.cmd install` y `npm.cmd run build`, o trabaje desde la terminal **cmd**.

### Paso 4 · Compilar y desplegar el backend

**Con NetBeans:** *File → Open Project* → carpeta del proyecto → clic derecho → **Run**.

**Con Maven y XAMPP:**

```bash
mvn clean package
```

Copie `target/homecenter-envios.war` en `C:\xampp\tomcat\webapps\` e inicie Tomcat.

> Use **un solo Tomcat a la vez**: si NetBeans ya inició uno en el puerto 8080,
> el de XAMPP no podrá arrancar (y viceversa).

### Paso 5 · Abrir la aplicación

- Versión JSP: <http://localhost:8080/homecenter-envios/>
- Versión React: <http://localhost:8080/homecenter-envios/app/>

Cada versión tiene un enlace para cambiar a la otra (en el login y en el menú lateral).

### Modo desarrollo de React (recarga instantánea)

Con Tomcat corriendo en el puerto 8080:

```bash
cd frontend
npm run dev
```

Abra <http://localhost:5173/homecenter-envios/app/>. Vite recarga la página al guardar
cualquier archivo `.jsx` y reenvía las peticiones `/api` a Tomcat.

---

## 3. Estructura del proyecto

```
homecenter-envios/
├── pom.xml                         Dependencias Maven (Hibernate, Jackson, MySQL, JSTL)
├── database/homecenter_db.sql      Script de la base de datos con datos de prueba
├── frontend/                       ── Aplicación React (JSX) ──
│   ├── package.json                Dependencias npm y scripts (dev, build)
│   ├── vite.config.js              Compilación hacia src/main/webapp/app + proxy de desarrollo
│   ├── index.html                  Página base de la SPA
│   └── src/
│       ├── main.jsx                Punto de entrada (monta <App />)
│       ├── App.jsx                 Rutas (HashRouter)
│       ├── api/clienteApi.js       fetch + token CSRF + manejo de errores
│       ├── contexto/SesionContexto.jsx   Usuario autenticado (React Context)
│       ├── modulos/definiciones.jsx      Columnas y campos de las 9 tablas
│       ├── componentes/            Diseño, campo de formulario, alertas, etc.
│       ├── paginas/                Login, Inicio, Listado, Formulario, 404
│       └── utilidades/formato.js   Moneda COP, fechas y estados
└── src/main/
    ├── resources/db.properties     Conexión a MySQL y opciones de Hibernate
    ├── java/co/homecenter/envios/
    │   ├── configuracion/          GestorHibernate (SessionFactory) · InicializadorAplicacion
    │   ├── modelo/                 Entidades JPA (@Entity) · Usuario (@MappedSuperclass)
    │   ├── dao/                    BaseDao<T> genérico con Hibernate · un DAO por tabla
    │   ├── controlador/            Servlets: CrudServlet<T> · SesionApiServlet · Login · Inicio…
    │   ├── filtro/                 CodificacionFiltro · CsrfFiltro · AutenticacionFiltro
    │   └── util/                   LectorFormulario · SeguridadContrasena · ConversorJson
    └── webapp/
        ├── app/                    React compilado (generado por "npm run build")
        ├── css/estilos.css         Paleta y tipografía (compartida por JSP y React)
        └── WEB-INF/                web.xml · fragmentos JSP · vistas JSP
```

---

## 4. Hibernate (persistencia)

Las clases del paquete `modelo` son **entidades JPA**: Hibernate las relaciona con las
tablas y genera el SQL automáticamente.

| Concepto | Dónde se aplica |
|---|---|
| `@Entity`, `@Table`, `@Column`, `@Id` | Todas las entidades |
| `@GeneratedValue(IDENTITY)` | Llaves `AUTO_INCREMENT` |
| `@MappedSuperclass` | `Usuario`: columnas comunes de Cliente, Vendedor, Logística y Transportista |
| `@ManyToOne` | Pedido → Cliente / Vendedor · Envío → Logística · Incidencia → Envío · Notificación → Transportista · Transportista → Envío (opcional) |
| `@OneToOne` | Factura → Pedido · Envío → Pedido (columna `UNIQUE`) |
| `SessionFactory` (singleton) | `GestorHibernate`, creada al desplegar y cerrada al detener Tomcat |
| `Session` + `Transaction` | `BaseDao.enSesion` / `enTransaccion` (commit y rollback) |
| `persist` / `find` / `merge` / `remove` | Insertar / consultar / actualizar / eliminar en `BaseDao` |
| HQL con `join fetch` y parámetros `:patron` | Búsquedas de cada DAO (sin inyección SQL) |
| Pool HikariCP | Configurado en `GestorHibernate` (`db.pool.maximo`) |

Las reglas `ON DELETE CASCADE / RESTRICT / SET NULL` del script SQL se mantienen; si
MySQL rechaza una operación, `BaseDao` traduce el código de error a un mensaje claro.

## 5. API JSON (usada por React)

Cada módulo responde en dos rutas: `/clientes` (HTML) y `/api/clientes` (JSON).

| Método | Ruta | Operación | Respuesta |
|---|---|---|---|
| GET | `/api/sesion` | Estado de la sesión y token CSRF | 200 |
| POST | `/api/sesion` `accion=iniciar` | Iniciar sesión | 200 · 401 |
| POST | `/api/sesion` `accion=cerrar` | Cerrar sesión | 200 |
| GET | `/api/inicio` | Indicadores y pedidos recientes | 200 |
| GET | `/api/{modulo}?q=texto` | **Consultar** | 200 `{registros}` |
| GET | `/api/{modulo}?accion=nuevo` · `accion=editar&id=N` | Datos del formulario | 200 · 404 |
| POST | `/api/{modulo}` `accion=guardar` | **Insertar** | 201 · 422 · 409 |
| POST | `/api/{modulo}` `accion=actualizar&id=N` | **Actualizar** | 200 · 404 · 422 · 409 |
| POST | `/api/{modulo}` `accion=eliminar&id=N` | **Eliminar** | 200 · 404 · 409 |

Módulos: `clientes`, `vendedores`, `logistica`, `transportistas`, `pedidos`, `facturas`,
`envios`, `incidencias`, `notificaciones`. Códigos de error: **401** sin sesión,
**403** sin permiso o token CSRF inválido, **422** datos inválidos (con `errores` por
campo), **409** conflicto con la base de datos (p. ej. correo duplicado).

## 6. React (frontend)

| Concepto | Dónde se aplica |
|---|---|
| Componentes funcionales y JSX | Todos los archivos `.jsx` |
| `useState`, `useEffect`, `useCallback`, `useMemo` | Páginas y `SesionContexto` |
| Context API + hook propio `useSesion()` | `contexto/SesionContexto.jsx` |
| Formularios controlados | `PaginaLogin`, `PaginaFormulario`, `CampoFormulario` |
| Enrutamiento, rutas anidadas y protegidas | `App.jsx`, `RutaProtegida.jsx`, `DisenoAplicacion.jsx` |
| Componentes genéricos guiados por datos | `definiciones.jsx` + `PaginaListado` / `PaginaFormulario` |
| `fetch` con cookies de sesión y token CSRF | `api/clienteApi.js` |

## 7. Elementos JSP (versión JSP)

| Elemento | Dónde |
|---|---|
| Directivas `page`, `taglib`, `include` | Todas las vistas; `cabecera.jspf`, `campos-usuario.jspf` |
| `<jsp:include>` + `<jsp:param>` | `mensajes.jsp`, `error-campo.jsp`, `acciones-fila.jsp`, `barra-busqueda.jsp` |
| `<jsp:useBean>` | `registro.jsp` |
| Expression Language y JSTL (`c`, `fmt`, `fn`) | Listados y formularios |

## 8. Estándares de codificación

- **Paquetes** en minúscula con dominio invertido: `co.homecenter.envios.dao`.
- **Clases y componentes React** en *PascalCase*: `ClienteDao`, `PaginaListado`.
- **Métodos, funciones y variables** en *camelCase* con verbos: `buscarPorId`, `cargarCatalogos`.
- **Constantes** en MAYÚSCULAS: `HQL_LISTAR`, `URL_BASE_API`.
- **Columnas SQL** en *snake_case* (`id_cliente`); **atributos Java/JSON** en *camelCase* (`idCliente`).
- **Comentarios:** Javadoc con `@author` en todas las clases Java; JSDoc en el código React;
  encabezado con autores en JSP, CSS, SQL y archivos de configuración.
- **Formato:** 4 espacios en Java/JSP y 2 en JavaScript/JSX; líneas de Java de hasta 120 caracteres.

## 9. Seguridad

- Contraseñas con **PBKDF2-HMAC-SHA256** + sal aleatoria; el hash nunca sale en el JSON (`@JsonIgnore`).
- HQL con parámetros con nombre (sin concatenar datos del usuario).
- Salida escapada (`c:out`, `fn:escapeXml` en JSP; React escapa por defecto).
- Token **CSRF** en todos los POST (formularios JSP y peticiones de React).
- Sesión regenerada al iniciar sesión; cookie `HttpOnly`.
- Autorización por rol en el servidor (`AutenticacionFiltro`): los clientes solo ven su panel.
