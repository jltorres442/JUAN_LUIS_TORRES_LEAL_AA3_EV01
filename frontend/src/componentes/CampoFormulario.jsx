/**
 * Campo de formulario genérico: dibuja la etiqueta, el control adecuado según
 * el tipo (texto, lista, fecha, casilla...) y el mensaje de error.
 * Usa las mismas clases CSS que los formularios JSP (campo, campo--error...).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */
import { useState } from 'react';
import { formatearEstado } from '../utilidades/formato.js';

/**
 * @param {object}   props
 * @param {object}   props.campo       definición del campo (ver modulos/definiciones.jsx)
 * @param {any}      props.valor       valor actual (estado controlado por el formulario padre)
 * @param {Function} props.alCambiar   función (nombre, nuevoValor) para actualizar el valor
 * @param {string}   [props.error]     mensaje de validación devuelto por el servidor
 * @param {boolean}  props.esNuevo     true en el formulario de creación
 * @param {object}   props.catalogos   listas para los <select> (clientes, pedidos, ...)
 * @param {object}   props.estados     listas de estados (estadosPedido, ...)
 */
export default function CampoFormulario({ campo, valor, alCambiar, error, esNuevo, catalogos, estados }) {
  // Estado local solo para el botón MOSTRAR/OCULTAR de las contraseñas.
  const [contrasenaVisible, setContrasenaVisible] = useState(false);

  const requerido = Boolean(campo.requerido || (campo.soloAlCrear && esNuevo));
  const idControl = `campo-${campo.nombre}`;
  const idError = `error-${campo.nombre}`;

  // Atributos comunes a todos los controles. aria-invalid y aria-describedby
  // conectan el control con su mensaje de error para los lectores de pantalla.
  const atributos = {
    id: idControl,
    name: campo.nombre,
    required: requerido,
    'aria-invalid': error ? 'true' : undefined,
    'aria-describedby': error ? idError : undefined,
  };

  /** Notifica al formulario padre el nuevo valor del campo. */
  const cambiar = (evento) => alCambiar(campo.nombre, evento.target.value);

  // ---- Casilla de verificación: la etiqueta va a la derecha del control ----
  if (campo.tipo === 'checkbox') {
    return (
      <div className={`campo ${campo.completo ? 'campo--completo' : ''}`}>
        <label className="casilla">
          <input
            type="checkbox"
            {...atributos}
            checked={Boolean(valor)}
            onChange={(evento) => alCambiar(campo.nombre, evento.target.checked)}
          />
          <span>{campo.etiqueta}</span>
        </label>
      </div>
    );
  }

  return (
    <div className={`campo ${campo.completo ? 'campo--completo' : ''} ${error ? 'campo--error' : ''}`}>
      <label htmlFor={idControl}>
        {campo.etiqueta} {requerido && <span className="obligatorio">*</span>}
      </label>

      {dibujarControl()}

      {campo.ayuda && <p className="campo__ayuda">{campo.ayuda} {!esNuevo && campo.ayudaEdicion}</p>}
      {error && <p className="campo__error" id={idError}>{error}</p>}
    </div>
  );

  /**
   * Devuelve el control HTML que corresponde al tipo del campo.
   * (Declaración de función: puede usarse antes de su definición dentro del componente.)
   */
  function dibujarControl() {
    switch (campo.tipo) {
      case 'textarea':
        return (
          <textarea {...atributos} value={valor ?? ''} onChange={cambiar}
                    maxLength={campo.maximo} placeholder={campo.ejemplo} />
        );

      case 'select': {
        // Las opciones salen de un catálogo (lista de entidades) o de una lista de estados.
        const opciones = campo.estados
          ? (estados[campo.estados] ?? []).map((estado) => ({ valor: estado, texto: formatearEstado(estado) }))
          : (catalogos[campo.catalogo] ?? []).map((elemento) => ({
              valor: campo.valorOpcion(elemento),
              texto: campo.textoOpcion(elemento),
            }));
        return (
          <select {...atributos} value={valor ?? ''} onChange={cambiar}>
            {!campo.estados && <option value="">{campo.opcionVacia ?? '— Seleccione —'}</option>}
            {opciones.map((opcion) => (
              // "key" permite a React identificar cada opción al actualizar la lista.
              <option key={opcion.valor} value={opcion.valor}>{opcion.texto}</option>
            ))}
          </select>
        );
      }

      case 'password':
        return (
          <div className="contrasena-envoltura">
            <input {...atributos} type={contrasenaVisible ? 'text' : 'password'} value={valor ?? ''}
                   onChange={cambiar} maxLength={campo.maximo} autoComplete="new-password"
                   placeholder="••••••••" />
            <button type="button" className="mostrar-contrasena"
                    onClick={() => setContrasenaVisible((visible) => !visible)}>
              {contrasenaVisible ? 'OCULTAR' : 'MOSTRAR'}
            </button>
          </div>
        );

      default:
        // text, email, tel, number, date, datetime-local
        return (
          <input {...atributos} type={campo.tipo} value={valor ?? ''} onChange={cambiar}
                 maxLength={campo.maximo} min={campo.minimo} step={campo.paso}
                 placeholder={campo.ejemplo} />
        );
    }
  }
}
