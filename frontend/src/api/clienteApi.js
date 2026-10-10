/**
 * Cliente HTTP para la API JSON del backend Java (servlets + Hibernate).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * RESPONSABILIDADES
 *  - Construir la URL de cada recurso a partir de la ubicación de la página.
 *  - Enviar los datos como formulario (application/x-www-form-urlencoded), el
 *    mismo formato que usan las vistas JSP; así los servlets reutilizan
 *    exactamente la misma validación (LectorFormulario.java).
 *  - Adjuntar el token CSRF en cada petición POST.
 *  - Convertir las respuestas de error en objetos ErrorApi fáciles de mostrar.
 */

/**
 * URL base de la API. La aplicación React se publica en
 * /homecenter-envios/app/, por lo que "../api/" apunta a /homecenter-envios/api/.
 * Usar una ruta relativa evita escribir a mano el nombre del contexto de Tomcat.
 */
const URL_BASE_API = new URL('../api/', window.location.href);

/** Token CSRF vigente; lo actualiza SesionContexto cada vez que cambia la sesión. */
let tokenCsrf = null;

/** Función que se ejecuta si el servidor responde 401 (la sesión expiró). */
let alExpirarSesion = () => {};

/**
 * Error devuelto por la API con información útil para la interfaz.
 */
export class ErrorApi extends Error {
  /**
   * @param {number} estado   código HTTP (401, 403, 404, 409, 422, 503...)
   * @param {string} mensaje  texto para el usuario
   * @param {Object<string,string>} [errores] errores por campo (solo en 422)
   */
  constructor(estado, mensaje, errores = {}) {
    super(mensaje);
    this.name = 'ErrorApi';
    this.estado = estado;
    this.errores = errores;
  }
}

/**
 * Guarda el token CSRF que se enviará en los POST.
 * @param {string|null} nuevoToken
 */
export function establecerTokenCsrf(nuevoToken) {
  tokenCsrf = nuevoToken;
}

/**
 * Registra la función que se ejecuta cuando la sesión expira.
 * @param {() => void} funcion
 */
export function configurarSesionExpirada(funcion) {
  alExpirarSesion = funcion;
}

/**
 * Hace una petición GET y devuelve el JSON de la respuesta.
 *
 * @param {string} recurso   ruta relativa a la API, p. ej. "clientes"
 * @param {Object<string, string|number|undefined>} [parametros] parámetros de consulta (?q=...)
 * @returns {Promise<any>} cuerpo JSON
 */
export async function obtener(recurso, parametros = {}) {
  const url = new URL(recurso, URL_BASE_API);
  Object.entries(parametros).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') {
      url.searchParams.set(clave, valor);
    }
  });
  const respuesta = await fetch(url, {
    headers: { Accept: 'application/json' },
    // Incluye la cookie JSESSIONID para que el servidor reconozca la sesión.
    credentials: 'same-origin',
  });
  return procesarRespuesta(respuesta);
}

/**
 * Hace una petición POST con los datos codificados como formulario.
 *
 * @param {string} recurso ruta relativa a la API
 * @param {Object<string, any>} datos campos a enviar; los valores null/undefined se omiten
 * @returns {Promise<any>} cuerpo JSON
 */
export async function enviar(recurso, datos) {
  const cuerpo = new URLSearchParams();
  Object.entries(datos).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null) {
      cuerpo.append(clave, String(valor));
    }
  });
  if (tokenCsrf) {
    cuerpo.append('tokenCsrf', tokenCsrf);
  }
  const respuesta = await fetch(new URL(recurso, URL_BASE_API), {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
    },
    body: cuerpo,
    credentials: 'same-origin',
  });
  return procesarRespuesta(respuesta);
}

/**
 * Convierte la respuesta HTTP en datos o en un ErrorApi.
 *
 * @param {Response} respuesta respuesta de fetch
 * @returns {Promise<any>} JSON si el código es 2xx
 * @throws {ErrorApi} si el código indica error
 */
async function procesarRespuesta(respuesta) {
  // Si el servidor no respondió JSON (p. ej. una página de error de Tomcat), se usa un objeto vacío.
  const tipo = respuesta.headers.get('Content-Type') ?? '';
  const cuerpo = tipo.includes('application/json') ? await respuesta.json() : {};

  if (respuesta.ok) {
    return cuerpo;
  }
  if (respuesta.status === 401) {
    alExpirarSesion();
  }
  const mensaje = cuerpo.mensaje ?? `El servidor respondió con el código ${respuesta.status}.`;
  throw new ErrorApi(respuesta.status, mensaje, cuerpo.errores ?? {});
}

/** Atajos con nombre para cada operación de la API. */
export const api = {
  /** GET /api/sesion → { autenticado, usuario, tokenCsrf, estados } */
  consultarSesion: () => obtener('sesion'),

  /** POST /api/sesion (accion=iniciar) */
  iniciarSesion: (email, contrasena) => enviar('sesion', { accion: 'iniciar', email, contrasena }),

  /** POST /api/sesion (accion=cerrar) */
  cerrarSesion: () => enviar('sesion', { accion: 'cerrar' }),

  /** GET /api/inicio → { indicadores, pedidos } */
  consultarInicio: () => obtener('inicio'),

  /** GET /api/{modulo}?q= → { registros } */
  listar: (modulo, filtro) => obtener(modulo, { q: filtro }),

  /** GET /api/{modulo}?accion=nuevo|editar → { registro, esNuevo, catalogos } */
  consultarFormulario: (modulo, id) =>
    obtener(modulo, id ? { accion: 'editar', id } : { accion: 'nuevo' }),

  /** POST guardar (sin id) o actualizar (con id) → { mensaje, id } */
  guardar: (modulo, datos, id) =>
    enviar(modulo, id ? { ...datos, accion: 'actualizar', id } : { ...datos, accion: 'guardar' }),

  /** POST eliminar → { mensaje } */
  eliminar: (modulo, id) => enviar(modulo, { accion: 'eliminar', id }),
};
