/**
 * Funciones de formato para mostrar datos al usuario.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */

/**
 * Formateador de moneda colombiana. Intl.NumberFormat es parte del estándar
 * de JavaScript y respeta las convenciones locales (punto de miles).
 */
const FORMATO_PESOS = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

/**
 * @param {number|string|null} valor monto
 * @returns {string} p. ej. "$ 1.250.000"
 */
export function formatearMoneda(valor) {
  if (valor === null || valor === undefined || valor === '') {
    return '—';
  }
  return FORMATO_PESOS.format(Number(valor));
}

/**
 * @param {string|null} fechaHoraIso p. ej. "2026-09-26T07:30:00"
 * @returns {string} p. ej. "2026-09-26 07:30"
 */
export function formatearFechaHora(fechaHoraIso) {
  if (!fechaHoraIso) {
    return '—';
  }
  return fechaHoraIso.replace('T', ' ').slice(0, 16);
}

/**
 * Convierte un estado de la base de datos en texto legible.
 * @param {string} estado p. ej. "EN_PREPARACION"
 * @returns {string} p. ej. "EN PREPARACION"
 */
export function formatearEstado(estado) {
  return (estado ?? '').replaceAll('_', ' ');
}

/**
 * Convierte la fecha y hora de la API al formato que acepta
 * <input type="datetime-local"> (sin segundos).
 * @param {string|null} valor
 * @returns {string}
 */
export function aValorFechaHoraLocal(valor) {
  return valor ? valor.slice(0, 16) : '';
}
