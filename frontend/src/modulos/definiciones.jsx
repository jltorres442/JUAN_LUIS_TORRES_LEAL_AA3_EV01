/**
 * Definición declarativa de los 9 módulos CRUD (una entrada por tabla).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * IDEA PRINCIPAL
 * En vez de escribir 18 pantallas casi iguales (lista + formulario por tabla),
 * cada módulo se DESCRIBE aquí con datos: qué columnas muestra la tabla y qué
 * campos tiene el formulario. Los componentes genéricos PaginaListado y
 * PaginaFormulario leen esta descripción y construyen la interfaz.
 *
 * ESTRUCTURA DE UN MÓDULO
 *   clave           ruta de React y de la API (/api/{clave})
 *   titulo          título del módulo (plural)
 *   singular        nombre en singular para botones y mensajes
 *   descripcion     subtítulo de la banda azul
 *   icono, seccion  cómo aparece en el menú lateral
 *   campoId         nombre de la llave primaria en el JSON
 *   ayudaBusqueda   texto de ejemplo del buscador
 *   columnas        [{ campo, titulo, tipo?, secundario?, clase? }]
 *                   tipo: id | fecha | fechaHora | moneda | estado | booleano | texto
 *   campos          [{ nombre, etiqueta, tipo, requerido?, ... }]
 *                   tipo: text | email | tel | password | number | date |
 *                         datetime-local | textarea | checkbox | select
 *   describir(r)    texto para el cuadro "¿Seguro que desea eliminar ...?"
 */

/**
 * Campos comunes a Cliente, Vendedor, Logística y Transportista
 * (equivale a la clase abstracta Usuario.java y al fragmento campos-usuario.jspf).
 * `soloAlCrear` indica que el campo es obligatorio solo en el formulario de creación.
 */
const CAMPOS_USUARIO = [
  { nombre: 'numeroIdentificacion', etiqueta: 'Número de identificación', tipo: 'text', requerido: true, maximo: 20, ejemplo: 'Ej. 1020304050' },
  { nombre: 'nombre', etiqueta: 'Nombre completo', tipo: 'text', requerido: true, maximo: 100, ejemplo: 'Nombres y apellidos' },
  { nombre: 'email', etiqueta: 'Correo electrónico', tipo: 'email', requerido: true, maximo: 150, ejemplo: 'nombre@dominio.com' },
  { nombre: 'telefono', etiqueta: 'Teléfono', tipo: 'tel', requerido: true, maximo: 20, ejemplo: 'Ej. 3104567890' },
  {
    nombre: 'contrasena', etiqueta: 'Contraseña', tipo: 'password', soloAlCrear: true, maximo: 72,
    ayuda: 'Mínimo 8 caracteres con letras y números.',
    ayudaEdicion: 'Déjela vacía para conservar la actual.',
  },
  { nombre: 'confirmarContrasena', etiqueta: 'Confirmar contraseña', tipo: 'password', maximo: 72 },
];

/** Columnas comunes de los listados de usuarios. */
const COLUMNAS_USUARIO = [
  { campo: 'nombre', titulo: 'Nombre', clase: 'negrita' },
  { campo: 'numeroIdentificacion', titulo: 'Identificación' },
  { campo: 'email', titulo: 'Contacto', secundario: 'telefono' },
];

export const MODULOS = [
  // ------------------------------------------------------------------ OPERACIÓN
  {
    clave: 'pedidos',
    titulo: 'Pedidos',
    singular: 'pedido',
    descripcion: 'Pedidos registrados por los vendedores para cada cliente.',
    icono: '📦',
    seccion: 'Operación',
    campoId: 'idPedido',
    ayudaBusqueda: 'Buscar por número, estado, cliente o vendedor',
    columnas: [
      { campo: 'idPedido', titulo: 'N.º', tipo: 'id' },
      { campo: 'fecha', titulo: 'Fecha', tipo: 'fecha' },
      { campo: 'nombreCliente', titulo: 'Cliente' },
      { campo: 'nombreVendedor', titulo: 'Vendedor' },
      { campo: 'estado', titulo: 'Estado', tipo: 'estado' },
      { campo: 'total', titulo: 'Total', tipo: 'moneda' },
    ],
    campos: [
      {
        nombre: 'idCliente', etiqueta: 'Cliente', tipo: 'select', requerido: true, catalogo: 'clientes',
        valorOpcion: (c) => c.idCliente, textoOpcion: (c) => `${c.nombre} · ${c.numeroIdentificacion}`,
      },
      {
        nombre: 'idVendedor', etiqueta: 'Vendedor', tipo: 'select', requerido: true, catalogo: 'vendedores',
        valorOpcion: (v) => v.idVendedor, textoOpcion: (v) => v.nombre,
      },
      { nombre: 'fecha', etiqueta: 'Fecha', tipo: 'date', requerido: true },
      { nombre: 'total', etiqueta: 'Total (COP)', tipo: 'number', requerido: true, minimo: 0, paso: '0.01', ejemplo: 'Ej. 389900' },
      { nombre: 'estado', etiqueta: 'Estado', tipo: 'select', requerido: true, estados: 'estadosPedido' },
    ],
    describir: (r) => `el pedido #${r.idPedido} (también se eliminarán su factura, envío e incidencias)`,
  },
  {
    clave: 'facturas',
    titulo: 'Facturas',
    singular: 'factura',
    descripcion: 'Cada pedido puede tener una única factura.',
    icono: '🧾',
    seccion: 'Operación',
    campoId: 'idFactura',
    ayudaBusqueda: 'Buscar por número de factura, pedido o cliente',
    columnas: [
      { campo: 'idFactura', titulo: 'N.º factura', tipo: 'id', prefijo: 'FAC-' },
      { campo: 'fechaEmision', titulo: 'Fecha de emisión', tipo: 'fecha' },
      { campo: 'idPedido', titulo: 'Pedido', tipo: 'id' },
      { campo: 'nombreCliente', titulo: 'Cliente' },
      { campo: 'montoTotal', titulo: 'Monto total', tipo: 'moneda' },
    ],
    campos: [
      {
        nombre: 'idPedido', etiqueta: 'Pedido', tipo: 'select', requerido: true, catalogo: 'pedidos', completo: true,
        ayuda: 'Un pedido solo puede tener una factura.',
        valorOpcion: (p) => p.idPedido, textoOpcion: (p) => `#${p.idPedido} · ${p.nombreCliente}`,
      },
      { nombre: 'fechaEmision', etiqueta: 'Fecha de emisión', tipo: 'date', requerido: true },
      { nombre: 'montoTotal', etiqueta: 'Monto total (COP)', tipo: 'number', requerido: true, minimo: 0, paso: '0.01' },
    ],
    describir: (r) => `la factura FAC-${r.idFactura}`,
  },
  {
    clave: 'envios',
    titulo: 'Envíos',
    singular: 'envío',
    descripcion: 'Despachos gestionados por el personal de logística.',
    icono: '🚚',
    seccion: 'Operación',
    campoId: 'idEnvio',
    ayudaBusqueda: 'Buscar por número, dirección, estado o cliente',
    columnas: [
      { campo: 'idEnvio', titulo: 'N.º', tipo: 'id' },
      { campo: 'fechaEnvio', titulo: 'Fecha de envío', tipo: 'fecha' },
      { campo: 'nombreCliente', titulo: 'Cliente', secundarioTexto: (r) => `Pedido #${r.idPedido}` },
      { campo: 'direccionEntrega', titulo: 'Dirección de entrega' },
      { campo: 'nombreLogistica', titulo: 'Logística' },
      { campo: 'estado', titulo: 'Estado', tipo: 'estado' },
    ],
    campos: [
      {
        nombre: 'idPedido', etiqueta: 'Pedido', tipo: 'select', requerido: true, catalogo: 'pedidos',
        ayuda: 'Un pedido solo puede tener un envío.',
        valorOpcion: (p) => p.idPedido, textoOpcion: (p) => `#${p.idPedido} · ${p.nombreCliente}`,
      },
      {
        nombre: 'idLogistica', etiqueta: 'Responsable de logística', tipo: 'select', requerido: true,
        catalogo: 'responsablesLogistica', valorOpcion: (l) => l.idLogistica, textoOpcion: (l) => l.nombre,
      },
      { nombre: 'direccionEntrega', etiqueta: 'Dirección de entrega', tipo: 'text', requerido: true, maximo: 250, completo: true },
      { nombre: 'fechaEnvio', etiqueta: 'Fecha de envío', tipo: 'date', requerido: true },
      { nombre: 'estado', etiqueta: 'Estado', tipo: 'select', requerido: true, estados: 'estadosEnvio' },
    ],
    describir: (r) => `el envío #${r.idEnvio} (también se eliminarán sus incidencias)`,
  },
  {
    clave: 'incidencias',
    titulo: 'Incidencias',
    singular: 'incidencia',
    descripcion: 'Problemas reportados por los transportistas durante la entrega.',
    icono: '⚠️',
    seccion: 'Operación',
    campoId: 'idIncidencia',
    ayudaBusqueda: 'Buscar por descripción, estado o número de envío',
    columnas: [
      { campo: 'idIncidencia', titulo: 'N.º', tipo: 'id' },
      { campo: 'direccionEnvio', titulo: 'Envío', secundarioTexto: (r) => `Envío #${r.idEnvio}` },
      { campo: 'descripcion', titulo: 'Descripción' },
      { campo: 'estado', titulo: 'Estado', tipo: 'estado' },
    ],
    campos: [
      {
        nombre: 'idEnvio', etiqueta: 'Envío afectado', tipo: 'select', requerido: true, catalogo: 'envios',
        valorOpcion: (e) => e.idEnvio, textoOpcion: (e) => `#${e.idEnvio} · ${e.direccionEntrega}`,
      },
      { nombre: 'estado', etiqueta: 'Estado', tipo: 'select', requerido: true, estados: 'estadosIncidencia' },
      {
        nombre: 'descripcion', etiqueta: 'Descripción', tipo: 'textarea', requerido: true, maximo: 2000, completo: true,
        ejemplo: 'Ej. Cliente no presente, dirección incorrecta, paquete dañado...',
      },
    ],
    describir: (r) => `la incidencia #${r.idIncidencia}`,
  },
  {
    clave: 'notificaciones',
    titulo: 'Notificaciones',
    singular: 'notificación',
    descripcion: 'Mensajes enviados a los conductores sobre sus rutas y entregas.',
    icono: '🔔',
    seccion: 'Operación',
    campoId: 'idNotificacion',
    ayudaBusqueda: 'Buscar por mensaje o transportista',
    columnas: [
      { campo: 'idNotificacion', titulo: 'N.º', tipo: 'id' },
      { campo: 'fechaHora', titulo: 'Fecha y hora', tipo: 'fechaHora' },
      { campo: 'nombreTransportista', titulo: 'Transportista' },
      { campo: 'mensaje', titulo: 'Mensaje' },
    ],
    campos: [
      {
        nombre: 'idTransportista', etiqueta: 'Transportista', tipo: 'select', requerido: true, catalogo: 'transportistas',
        valorOpcion: (t) => t.idTransportista, textoOpcion: (t) => `${t.nombre} · ${t.licencia}`,
      },
      { nombre: 'fechaHora', etiqueta: 'Fecha y hora', tipo: 'datetime-local', requerido: true },
      { nombre: 'mensaje', etiqueta: 'Mensaje', tipo: 'textarea', requerido: true, maximo: 2000, completo: true },
    ],
    describir: (r) => `la notificación #${r.idNotificacion}`,
  },

  // ------------------------------------------------------------------ PERSONAS
  {
    clave: 'clientes',
    titulo: 'Clientes',
    singular: 'cliente',
    descripcion: 'Personas que realizan pedidos en Homecenter.',
    icono: '👥',
    seccion: 'Personas',
    campoId: 'idCliente',
    ayudaBusqueda: 'Buscar por nombre, correo, identificación o teléfono',
    columnas: [
      { campo: 'idCliente', titulo: 'ID' },
      ...COLUMNAS_USUARIO,
      { campo: 'direccion', titulo: 'Dirección' },
      { campo: 'fechaRegistro', titulo: 'Registro', tipo: 'fecha' },
    ],
    campos: [
      ...CAMPOS_USUARIO,
      { nombre: 'direccion', etiqueta: 'Dirección', tipo: 'text', requerido: true, maximo: 200, completo: true, ejemplo: 'Ej. Cra. 15 # 93-47, Bogotá' },
    ],
    describir: (r) => `el cliente ${r.nombre}`,
  },
  {
    clave: 'vendedores',
    titulo: 'Vendedores',
    singular: 'vendedor',
    descripcion: 'Asesores comerciales que registran los pedidos.',
    icono: '💼',
    seccion: 'Personas',
    campoId: 'idVendedor',
    ayudaBusqueda: 'Buscar por nombre, correo, identificación o teléfono',
    columnas: [
      { campo: 'idVendedor', titulo: 'ID' },
      ...COLUMNAS_USUARIO,
      { campo: 'registroPedido', titulo: 'Registra pedidos', tipo: 'booleano', textos: ['HABILITADO', 'INHABILITADO'] },
    ],
    campos: [
      ...CAMPOS_USUARIO,
      { nombre: 'registroPedido', etiqueta: 'Habilitado para registrar pedidos', tipo: 'checkbox', completo: true, valorInicial: true },
    ],
    describir: (r) => `al vendedor ${r.nombre}`,
  },
  {
    clave: 'logistica',
    titulo: 'Logística',
    singular: 'responsable de logística',
    descripcion: 'Responsables de preparar y despachar los envíos.',
    icono: '🏭',
    seccion: 'Personas',
    campoId: 'idLogistica',
    ayudaBusqueda: 'Buscar por nombre, correo, identificación o teléfono',
    columnas: [{ campo: 'idLogistica', titulo: 'ID' }, ...COLUMNAS_USUARIO],
    campos: [...CAMPOS_USUARIO],
    describir: (r) => `a ${r.nombre}`,
  },
  {
    clave: 'transportistas',
    titulo: 'Transportistas',
    singular: 'transportista',
    descripcion: 'Conductores que entregan los pedidos a los clientes.',
    icono: '🚛',
    seccion: 'Personas',
    campoId: 'idTransportista',
    ayudaBusqueda: 'Buscar por nombre, correo, identificación o licencia',
    columnas: [
      { campo: 'idTransportista', titulo: 'ID' },
      ...COLUMNAS_USUARIO,
      { campo: 'licencia', titulo: 'Licencia' },
      {
        campo: 'direccionEnvio', titulo: 'Envío asignado',
        // Render personalizado: si no tiene envío se muestra la etiqueta "DISPONIBLE".
        render: (r) =>
          r.idEnvio ? (
            <>
              <strong>#{r.idEnvio}</strong>
              <span className="secundario">{r.direccionEnvio}</span>
            </>
          ) : (
            <span className="etiqueta etiqueta--neutra">DISPONIBLE</span>
          ),
      },
    ],
    campos: [
      ...CAMPOS_USUARIO,
      { nombre: 'licencia', etiqueta: 'Licencia de conducción', tipo: 'text', requerido: true, maximo: 50, ejemplo: 'Ej. C2-1098765432' },
      {
        nombre: 'idEnvio', etiqueta: 'Envío asignado', tipo: 'select', catalogo: 'envios',
        opcionVacia: '— Sin asignar (disponible) —',
        valorOpcion: (e) => e.idEnvio, textoOpcion: (e) => `#${e.idEnvio} · ${e.direccionEntrega}`,
      },
    ],
    describir: (r) => `al transportista ${r.nombre}`,
  },
];

/**
 * Busca la definición de un módulo por su clave (la que aparece en la URL).
 * @param {string} clave p. ej. "pedidos"
 * @returns {object|undefined}
 */
export function buscarModulo(clave) {
  return MODULOS.find((modulo) => modulo.clave === clave);
}
