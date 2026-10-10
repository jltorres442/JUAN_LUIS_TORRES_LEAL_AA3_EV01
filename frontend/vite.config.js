/**
 * Configuración de Vite: la herramienta que compila el JSX de React a
 * JavaScript que entiende el navegador y que sirve la aplicación en desarrollo.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * DOS MODOS DE TRABAJO
 *   1. Desarrollo (`npm run dev`):
 *      Vite sirve React en http://localhost:5173/homecenter-envios/app/ con
 *      recarga instantánea. Las peticiones a /homecenter-envios/api se
 *      reenvían (proxy) al Tomcat que corre el backend Java, así el navegador
 *      cree que todo viene del mismo servidor y las cookies de sesión funcionan.
 *
 *   2. Producción (`npm run build`):
 *      El resultado se escribe en ../src/main/webapp/app, de modo que Maven lo
 *      empaqueta dentro del WAR y Tomcat lo publica en /homecenter-envios/app/.
 */
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/** Dirección del Tomcat donde corre el backend durante el desarrollo. */
const SERVIDOR_BACKEND = process.env.HC_BACKEND ?? 'http://localhost:8080';

/** Ruta de contexto con la que Tomcat publica la aplicación (nombre del WAR). */
const CONTEXTO = '/homecenter-envios';

export default defineConfig(({ command }) => ({
  // El plugin de React transforma el JSX y habilita la recarga rápida de componentes.
  plugins: [react()],

  // En producción se usan rutas relativas ("./") para que la aplicación funcione
  // sin importar la ruta de contexto; en desarrollo se imita la ruta de Tomcat.
  base: command === 'build' ? './' : `${CONTEXTO}/app/`,

  build: {
    outDir: '../src/main/webapp/app',
    // Vite pide confirmación explícita para limpiar una carpeta fuera del proyecto frontend.
    emptyOutDir: true,
  },

  server: {
    port: 5173,
    // Permite importar la hoja de estilos compartida que vive en src/main/webapp/css.
    fs: { allow: ['..'] },
    proxy: {
      [`${CONTEXTO}/api`]: { target: SERVIDOR_BACKEND, changeOrigin: false },
      // Enlaces a la versión JSP (login, registro) durante el desarrollo.
      [`${CONTEXTO}/registro`]: { target: SERVIDOR_BACKEND, changeOrigin: false },
    },
  },
}));
