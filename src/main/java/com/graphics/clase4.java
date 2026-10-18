package com.graphics; // Reúne la versión final con las otras tres lecciones.

import static org.lwjgl.glfw.GLFW.*; // Permite consultar la tecla que controla el minimapa.
import static org.lwjgl.opengl.GL33.*; // Permite cambiar viewport, recorte y buffers de dibujo.

/**
 * CLASE 4: CIUDAD TERMINADA, ENTREGAS Y MINIMAPA.
 * Orden de lectura: estado, controles, entregas, decoración y segundo pase de dibujo.
 * Hereda clase3, que hereda clase2, que a su vez hereda clase1.
 * Los semáforos son decorativos; el ejemplo no incluye tráfico autónomo.
 */
public class clase4 extends clase3 {

    // ==================== 1. ESTADO DEL JUEGO ====================
    protected boolean mostrarAyuda = false;
    protected String ultimoSector = "";
    protected float tiempoCartel = 4f;
    private final InterfazCiudad interfaz = new InterfazCiudad();
    protected final AutoTrafico[] trafico = crearTrafico();
    private boolean mostrarMapa = true; // Muestra el minimapa desde el inicio.
    protected int entregas = 0; // Cuenta las entregas completadas; también identifica el siguiente destino.
    protected float tiempo = 0; // Acumula los segundos de la partida hasta completar el recorrido.
    protected static final float[][] DESTINOS = { // Cada fila contiene X y Z de una parada sobre la calle.
            {centro(10), centro(0)}, // Primera entrega: esquina noreste.
            {centro(0), centro(0)}, // Segunda entrega: esquina noroeste.
            {centro(0), centro(10)} // Tercera entrega: regreso al punto inicial.
    };

    protected float tiempoAnimacion = 0; // Sigue avanzando incluso al terminar las entregas.

    // ==================== 2. CONTROLES Y REINICIO ====================

    /**
     * Añade el interruptor del minimapa a los controles anteriores.
     */
    @Override // Amplía las teclas definidas por clase3.
    protected void tecla(int key) {
        super.tecla(key); // Conserva salida, cámara, reinicio y luces.
        if (key == GLFW_KEY_H) mostrarAyuda = !mostrarAyuda;
        if (key == GLFW_KEY_M) { // Comprueba si se pulsó la tecla del mapa.
            mostrarMapa = !mostrarMapa; // Alterna entre mostrar y ocultar la vista superior.
        }
    }

    /**
     * Reinicia el vehículo y el progreso de las entregas.
     */
    @Override // Amplía el reinicio definido en clase2.
    protected void reiniciar() {
        super.reiniciar(); // Restaura posición, velocidad y orientación del vehículo.
        entregas = 0; // Vuelve a seleccionar la primera parada.
        tiempoAnimacion = 0;
        ultimoSector = "";
        tiempoCartel = 4f;
        for (AutoTrafico a : trafico) a.reiniciar();
        tiempo = 0; // Reinicia el cronómetro de la partida.

    }

    // ==================== 3. REGLAS DE LAS ENTREGAS ====================

    /**
     * Actualiza el vehículo y comprueba si llegó y frenó en el destino activo.
     */
    @Override // Añade el objetivo del juego al movimiento heredado.
    protected void actualizar(float deltaTime) {
        tiempoAnimacion += deltaTime;
        tiempoCartel += deltaTime;
        super.actualizar(deltaTime);
        actualizarTrafico(deltaTime);
        String sector = sectorActual();
        if (!sector.equals(ultimoSector)) {
            ultimoSector = sector;
            tiempoCartel = 0f;
        } // Procesa aceleración, giro, colisiones y título de la ventana.
        if (entregas >= DESTINOS.length) { // Comprueba si ya se completaron todas las paradas.
            return; // Conserva el tiempo final y evita leer fuera del arreglo.
        }
        tiempo += deltaTime; // Suma los segundos de este cuadro al cronómetro.
        float distanciaX = autoX - DESTINOS[entregas][0]; // Calcula la separación horizontal al destino activo.
        float distanciaZ = autoZ - DESTINOS[entregas][1]; // Calcula la separación en profundidad al destino.
        float distanciaCuadrada = distanciaX * distanciaX + distanciaZ * distanciaZ; // Mide cercanía sin calcular raíz cuadrada.
        boolean estaCerca = distanciaCuadrada < 3 * 3; // Acepta un radio de llegada de tres unidades.
        boolean estaFrenando = Math.abs(velocidad) < 1; // Exige circular a menos de una unidad por segundo.
        if (estaCerca && estaFrenando) { // Solo completa la entrega si ambas condiciones se cumplen.
            entregas++; // Selecciona la siguiente parada o completa el juego.
        }
    }

    /**
     * Compone el progreso que se añade al título de la ventana.
     */
    @Override // Amplía los indicadores de día/noche y faros de clase3.
    protected String estadoExtra() {
        String mensaje = super.estadoExtra(); // Recupera los indicadores de iluminación.
        mensaje += " | M: mapa | "; // Muestra la tecla que alterna el minimapa.
        if (entregas == DESTINOS.length) { // Selecciona el texto de victoria al completar las tres paradas.
            mensaje += "GANASTE en " + (int) tiempo + " s! R: jugar otra vez"; // Muestra tiempo final y opción de reinicio.
        } else { // Durante el recorrido muestra progreso e instrucciones.
            mensaje += "Entregas " + entregas + "/3"; // Indica cuántas paradas se completaron.
            mensaje += " | Frena en la marca dorada | " + (int) tiempo + " s"; // Explica la condición de entrega y el tiempo.
        }
        return mensaje; // Entrega el texto a actualizarTitulo() de clase2.
    }

    // ==================== 4. ESCENA FINAL Y DESTINO ====================

    /**
     * Añade decoración y señal del destino a la escena iluminada.
     */
    @Override // Amplía el dibujo acumulado de las tres etapas anteriores.
    protected void escena() {
        super.escena();
        dibujarExtras();
        dibujarTrafico(); // Dibuja la ciudad, el auto y las farolas.
        if (!vistaMapa) { // Los detalles pequeños solo son necesarios en la vista principal.
            decorarCiudad(); // Añade árboles, bancos, ventanas y señalización urbana.
        }
        if (entregas < DESTINOS.length) { // Dibuja un objetivo únicamente mientras queden entregas.
            dibujarDestino(); // Coloca la marca dorada en la parada activa.
        }
    }

    /**
     * Marca la próxima parada con una plataforma y una baliza flotante.
     */
    private void dibujarDestino() {
        float x = DESTINOS[entregas][0]; // Lee el X de la próxima entrega.
        float z = DESTINOS[entregas][1]; // Lee el Z de la próxima entrega.
        entero("uEmision", 1); // Hace que el objetivo sea visible incluso de noche.
        caja(x, 0.06f, z, 5, 0.08f, 5, 1, 0.72f, 0.12f); // Dibuja una marca dorada sobre el asfalto.
        if (!vistaMapa) { // Evita añadir una baliza tridimensional al mapa pequeño.
            float alturaBaliza = 3.5f + (float) Math.sin(tiempo * 2) * 0.3f; // Hace oscilar la baliza suavemente.
            cajaGirada(x, alturaBaliza, z, 0.8f, 0.8f, 0.8f, 1, 0.8f, 0.15f, tiempo); // Dibuja el cubo giratorio del objetivo.
        }
        entero("uEmision", 0); // Devuelve a los siguientes objetos su iluminación normal.
    }

    // ==================== 5. DECORACIÓN DE LAS MANZANAS ====================

    /**
     * Decide qué decoración corresponde a cada tipo de parcela.
     */
    private void decorarCiudad() {
        for (int fila = 0; fila < MAPA.length; fila++) { // Recorre las filas del mapa.
            for (int columna = 0; columna < MAPA[fila].length; columna++) { // Recorre las columnas de esa fila.
                float x = centro(columna); // Obtiene el centro horizontal de la parcela.
                float z = centro(fila); // Obtiene el centro de la parcela en profundidad.
                int tipo = MAPA[fila][columna]; // Lee el contenido de la celda.
                if (tipo == 2) { // Detecta una parcela de parque.
                    dibujarParque(x, z); // Añade árboles y un banco.
                }
                if (tipo == 1) { // Detecta una parcela con edificio.
                    float altura = 5 + (fila * 3 + columna * 7) % 9; // Recupera la misma altura calculada en clase1.
                    dibujarVentanas(x, z, altura); // Coloca ventanas en sus cuatro fachadas.
                }
                if (tipo != 0) { // La señalización se coloca junto a las manzanas, no en celdas de calle.
                    dibujarPasoPeatonal(x, z); // Añade el cruce pintado sobre la calle contigua.
                    dibujarSemaforo(x + 4, z - 4); // Coloca el semáforo dentro de la acera.
                }
            }
        }
    }

    /**
     * Construye cuatro árboles y un banco utilizando cajas.
     */
    private void dibujarParque(float x, float z) {
        float[] posiciones = {-2.5f, 2.5f}; // Define desplazamientos respecto al centro de la parcela.
        for (float desplazamientoX : posiciones) { // Selecciona el lado izquierdo o derecho del parque.
            for (float desplazamientoZ : posiciones) { // Selecciona el lado delantero o trasero.
                float arbolX = x + desplazamientoX; // Convierte el desplazamiento local en coordenada X mundial.
                float arbolZ = z + desplazamientoZ; // Convierte el desplazamiento local en coordenada Z mundial.
                caja(arbolX, 1.2f, arbolZ, 0.35f, 2, 0.35f, 0.38f, 0.22f, 0.12f); // Dibuja el tronco marrón.
                caja(arbolX, 2.7f, arbolZ, 2, 2.3f, 2, 0.12f, 0.42f, 0.23f); // Dibuja la copa verde del árbol.
            }
        }
        dibujarFuente(x, z + 1.4f);
        caja(x, 0.65f, z, 3, 0.25f, 0.8f, 0.55f, 0.30f, 0.13f); // Dibuja el asiento de madera del banco.
        caja(x, 0.4f, z, 2, 0.6f, 0.35f, 0.22f, 0.24f, 0.24f); // Dibuja el soporte oscuro del banco.
        caja(x, 1, z + 0.35f, 3, 0.7f, 0.15f, 0.55f, 0.30f, 0.13f); // Dibuja el respaldo detrás del asiento.
    }

    /**
     * Distribuye ventanas por pisos en las cuatro paredes del edificio.
     */
    private void dibujarVentanas(float x, float z, float altura) {
        if (noche) { // Las ventanas simulan habitaciones encendidas en el ambiente nocturno.
            entero("uEmision", 1); // Permite ver el color de las ventanas sin depender de farolas.
        }
        for (float y = 1.7f; y < altura; y += 2) { // Recorre los pisos separados por dos unidades de altura.
            for (float desplazamiento = -2; desplazamiento <= 2; desplazamiento += 2) { // Coloca tres ventanas por fachada.
                caja(x + desplazamiento, y, z - 3.51f, 0.8f, 0.9f, 0.04f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada norte.
                caja(x + desplazamiento, y, z + 3.51f, 0.8f, 0.9f, 0.04f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada sur.
                caja(x - 3.51f, y, z + desplazamiento, 0.04f, 0.9f, 0.8f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada oeste.
                caja(x + 3.51f, y, z + desplazamiento, 0.04f, 0.9f, 0.8f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada este.
            }
        }
        entero("uEmision", 0); // Restablece la iluminación normal de los demás elementos.
    }

    /**
     * Conecta con pasos de cebra las aceras enfrentadas en las esquinas de cada manzana.
     */
    private void dibujarPasoPeatonal(float x, float z) {
        // Cruce norte: une la esquina noreste con la esquina sureste de la manzana de enfrente.
        // Las franjas atraviesan los 10 metros de calle, de borde de acera a borde de acera.
        for (int i = 0; i < 8; i++) {
            caja(x + 3.3f, 0.045f, z - 14.35f + i * 1.25f,
                    2.7f, 0.04f, 0.68f, 0.85f, 0.87f, 0.83f);
        }
        // Cruce este: une las dos esquinas enfrentadas a ambos lados de la calle vertical.
        for (int i = 0; i < 8; i++) {
            caja(x + 5.6f + i * 1.25f, 0.045f, z + 3.3f,
                    0.68f, 0.04f, 2.7f, 0.85f, 0.87f, 0.83f);
        }
    }

    /**
     * Construye un semáforo decorativo que alterna rojo, verde y amarillo cada 12 segundos.
     */
    private void dibujarSemaforo(float x, float z) {
        caja(x, 1.7f, z, 0.18f, 2.8f, 0.18f, 0.18f, 0.20f, 0.22f); // Dibuja el poste sobre la acera.
        caja(x, 3.1f, z, 0.55f, 1.2f, 0.45f, 0.08f, 0.10f, 0.12f); // Dibuja la carcasa de las tres luces.
        int fase = faseSemaforo(); // Repite un ciclo de segundos comprendidos entre 0 y 11.
        for (int indice = 0; indice < 3; indice++) { // Recorre rojo arriba, amarillo al centro y verde abajo.
            boolean encendida = false; // Parte de una bombilla apagada.
            if (indice == 0) { // Selecciona la bombilla roja.
                encendida = fase < 5; // Mantiene rojo durante los primeros cinco segundos.
            } else if (indice == 1) { // Selecciona la bombilla amarilla.
                encendida = fase >= 10; // Mantiene amarillo en los últimos dos segundos del ciclo.
            } else { // Selecciona la bombilla verde.
                encendida = fase >= 5 && fase < 10; // Mantiene verde durante los cinco segundos intermedios.
            }
            float brillo = 0.15f; // Conserva un color tenue cuando la bombilla está apagada.
            entero("uEmision", 0); // Configura inicialmente una superficie sin emisión.
            if (encendida) { // Comprueba si esta bombilla corresponde a la fase activa.
                brillo = 1; // Usa intensidad completa para su color.
                entero("uEmision", 1); // Hace que la bombilla se vea encendida.
            }
            float rojo = 0; // Componente roja inicialmente ausente.
            float verde = 0; // Componente verde inicialmente ausente.
            if (indice < 2) { // Rojo y amarillo necesitan componente roja.
                rojo = brillo; // Añade rojo con la intensidad elegida.
            }
            if (indice > 0) { // Amarillo y verde necesitan componente verde.
                verde = brillo; // Añade verde; rojo más verde produce amarillo.
            }
            float altura = 3.45f - indice * 0.35f; // Separa verticalmente las tres bombillas.
            caja(x, altura, z - 0.24f, 0.28f, 0.25f, 0.06f, rojo, verde, 0.02f); // Dibuja la bombilla frente a la carcasa.
        }
        entero("uEmision", 0); // Evita que el siguiente objeto herede la emisión del semáforo.
    }

    // ==================== 6. MINIMAPA: SEGUNDO PASE DE DIBUJO ====================

    /**
     * Dibuja la escena principal y después la misma ciudad desde arriba, en un recuadro.
     */
    @Override // Amplía el cuadro completo definido en clase1.
    protected void dibujarFrame() {
        super.dibujarFrame(); // Dibuja primero la vista normal de la ciudad.
        if (!mostrarMapa) {
            dibujarInterfaz(); // Comprueba si el usuario ocultó el minimapa con M.
            return; // Conserva únicamente la imagen principal.
        }
        int dimensionMenor = Math.min(ancho, alto); // Busca la dimensión que limita el espacio disponible.
        int lado = Math.min(260, dimensionMenor / 3); // Limita el mapa a 260 píxeles y a un tercio de la ventana.
        int margen = Math.min(18, dimensionMenor / 20); // Calcula una separación adaptable respecto a los bordes.
        int x = ancho - lado - margen; // Ubica el recuadro cerca del borde derecho.
        int y = alto - lado - margen; // Ubica el recuadro arriba; OpenGL mide Y desde abajo.
        glEnable(GL_SCISSOR_TEST); // Activa el recorte para no borrar el resto de la escena.
        glScissor(x - 3, y - 3, lado + 6, lado + 6); // Selecciona el mapa más un borde de tres píxeles.
        glClearColor(0.8f, 0.87f, 0.94f, 1); // Define un color claro para el marco.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Borra solo el recuadro exterior gracias al scissor.
        glScissor(x, y, lado, lado); // Reduce el recorte al interior del minimapa.
        glClearColor(0.06f, 0.10f, 0.15f, 1); // Define el fondo oscuro del mapa.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpia color y profundidad dentro del mapa.
        glViewport(x, y, lado, lado); // Redirige la proyección al recuadro cuadrado.
        entero("uMapa", 1); // Selecciona la proyección ortográfica del shader de clase1.
        vistaMapa = true; // Indica a escena() que omita decoración pequeña y baliza flotante.
        try { // Asegura que el estado de dibujo se restaure incluso si el segundo pase falla.
            escena(); // Dibuja otra vez la misma ciudad, ahora vista desde arriba.
            dibujarIndicadorAuto(); // Resalta la posición y el frente del jugador en el mapa.
        } finally { // El siguiente cuadro debe volver a la configuración de pantalla completa.
            vistaMapa = false; // Reactiva los detalles de la escena principal.
            entero("uMapa", 0); // Recupera la proyección en perspectiva.
            glDisable(GL_SCISSOR_TEST); // Permite que la próxima limpieza abarque toda la pantalla.
            glViewport(0, 0, ancho, alto); // Recupera el área de dibujo de la ventana completa.
        }
        dibujarInterfaz();
        if (glGetError() != GL_NO_ERROR) { // Comprueba que el pase del mapa no haya generado errores OpenGL.
            throw new IllegalStateException("Error OpenGL en minimapa"); // Expone el error en la consola.
        }
    }

    /**
     * Dibuja una marca cian y una punta blanca por encima de los edificios del minimapa.
     */
    private void dibujarIndicadorAuto() {
        cajaGirada(autoX, 25, autoZ, 2.2f, 0.1f, 3.2f, 0.1f, 1, 1, angulo); // Marca la posición con un rectángulo cian orientado.
        float frenteX = -(float) Math.sin(angulo); // Calcula la dirección frontal en el eje X.
        float frenteZ = -(float) Math.cos(angulo); // Calcula la dirección frontal en el eje Z.
        float puntaX = autoX + frenteX * 2; // Desplaza la punta dos unidades hacia delante en X.
        float puntaZ = autoZ + frenteZ * 2; // Desplaza la punta dos unidades hacia delante en Z.
        caja(puntaX, 26, puntaZ, 0.9f, 0.1f, 0.9f, 1, 1, 1); // Dibuja la punta blanca encima del indicador cian.
    }

    // Tráfico: cada vehículo guarda ruta, progreso y color propios.
    protected static class AutoTrafico {
        final float[][] ruta;
        final float r, g, b;
        int tramo;
        float x, z, angulo;

        AutoTrafico(float[][] ruta, float r, float g, float b) {
            this.ruta = ruta;
            this.r = r;
            this.g = g;
            this.b = b;
            reiniciar();
        }

        void reiniciar() {
            tramo = 1;
            x = ruta[0][0];
            z = ruta[0][1];
            angulo = (float) Math.atan2(-(ruta[1][0] - x), -(ruta[1][1] - z));
        }
    }

    // Convierte intersecciones en curvas redondeadas para evitar giros de 90 grados.
    private static float[][] rutaSuave(float[][] esquinas) {
        java.util.ArrayList<float[]> puntos = new java.util.ArrayList<>();
        int n = esquinas.length;
        for (int i = 0; i < n; i++) {
            float[] anterior = esquinas[(i + n - 1) % n];
            float[] actual = esquinas[i];
            float[] siguiente = esquinas[(i + 1) % n];
            float largoEntrada = (float) Math.hypot(actual[0] - anterior[0], actual[1] - anterior[1]);
            float largoSalida = (float) Math.hypot(siguiente[0] - actual[0], siguiente[1] - actual[1]);
            float radio = Math.min(4f, Math.min(largoEntrada, largoSalida) * .25f);
            float ex = actual[0] + (anterior[0] - actual[0]) * radio / largoEntrada;
            float ez = actual[1] + (anterior[1] - actual[1]) * radio / largoEntrada;
            float sx = actual[0] + (siguiente[0] - actual[0]) * radio / largoSalida;
            float sz = actual[1] + (siguiente[1] - actual[1]) * radio / largoSalida;
            puntos.add(new float[]{ex, ez});
            for (int j = 1; j <= 16; j++) {
                float t = j / 16f, u = 1 - t;
                puntos.add(new float[]{u * u * ex + 2 * u * t * actual[0] + t * t * sx,
                        u * u * ez + 2 * u * t * actual[1] + t * t * sz});
            }
        }
        return puntos.toArray(new float[0][]);
    }

    private AutoTrafico[] crearTrafico() {
        // Cada circuito pasa por calles interiores y gira mediante curvas suaves.
        return new AutoTrafico[]{
                new AutoTrafico(rutaSuave(new float[][]{{-50, -50}, {-10, -50}, {-10, -10}, {30, -10}, {30, 30}, {-50, 30}}), .1f, .8f, .25f),
                new AutoTrafico(rutaSuave(new float[][]{{10, -50}, {50, -50}, {50, 10}, {10, 10}, {10, 50}, {-30, 50}, {-30, -50}}), .9f, .65f, .1f),
                new AutoTrafico(rutaSuave(new float[][]{{50, -30}, {30, -30}, {30, 10}, {-10, 10}, {-10, 50}, {50, 50}}), .25f, .3f, .95f),
                new AutoTrafico(rutaSuave(new float[][]{{-50, 10}, {-30, 10}, {-30, -30}, {10, -30}, {10, 30}, {-50, 30}}), .8f, .2f, .8f),
                new AutoTrafico(rutaSuave(new float[][]{{-50, -10}, {-10, -10}, {-10, 30}, {30, 30}, {30, -50}, {-50, -50}}), .1f, .8f, .9f)
        };
    }

    protected int faseSemaforo() {
        return (int) (tiempoAnimacion % 12);
    }

    protected boolean verdeTrafico() {
        int fase = faseSemaforo();
        return fase >= 5 && fase < 10;
    }

    // No se detiene dentro del cruce: espera en su aproximación.
    protected boolean debeEsperarSemaforo(float x, float z, float dx, float dz) {
        if (verdeTrafico()) return false;
        float coordenada = Math.abs(dx) > .5f ? x : z;
        float sentido = Math.abs(dx) > .5f ? dx : dz;
        for (int i = 2; i < MAPA.length - 1; i += 2) {
            float distancia = (centro(i) - coordenada) * sentido;
            if (distancia > 2f && distancia <= 4.5f) return true;
        }
        return false;
    }

    protected void actualizarTrafico(float dt) {
        for (AutoTrafico a : trafico) {
            float tx = a.ruta[a.tramo][0], tz = a.ruta[a.tramo][1];
            float dx = tx - a.x, dz = tz - a.z;
            float distancia = (float) Math.hypot(dx, dz);
            if (distancia < .001f) {
                a.tramo = (a.tramo + 1) % a.ruta.length;
                continue;
            }
            dx /= distancia;
            dz /= distancia;
            // Los NPC solo frenan cuando hay un obstaculo delante; no esperan semaforos.
            float paso = Math.min(6f * dt, distancia);
            float nx = a.x + dx * paso, nz = a.z + dz * paso;
            boolean libre = super.puedeCircular(nx, nz);
            // Solo frena por vehículos delante y dentro de su trayectoria.
            if (vehiculoDelante(a, dx, dz, paso, autoX, autoZ)) libre = false;
            for (AutoTrafico otro : trafico) {
                if (otro != a && vehiculoDelante(a, dx, dz, paso, otro.x, otro.z)) libre = false;
            }
            if (libre) {
                a.x = nx;
                a.z = nz;
                float objetivo = (float) Math.atan2(-dx, -dz);
                float diferencia = (float) Math.atan2(Math.sin(objetivo - a.angulo), Math.cos(objetivo - a.angulo));
                a.angulo += Math.max(-2.5f * dt, Math.min(2.5f * dt, diferencia));
                if (paso >= distancia - .001f) a.tramo = (a.tramo + 1) % a.ruta.length;
            }
        }
    }

    private static boolean vehiculoDelante(AutoTrafico a, float dx, float dz,
                                           float paso, float otroX, float otroZ) {
        float relativoX = otroX - a.x, relativoZ = otroZ - a.z;
        float distanciaFrontal = relativoX * dx + relativoZ * dz;
        float distanciaLateral = Math.abs(relativoX * dz - relativoZ * dx);
        // Conserva cuatro unidades de separación frontal; ignora los lados y la parte trasera.
        return distanciaFrontal > 0f && distanciaFrontal < 4f + paso
                && distanciaLateral < 2f;
    }

    @Override
    protected boolean puedeCircular(float x, float z) {
        if (!super.puedeCircular(x, z)) return false;
        if (trafico != null) for (AutoTrafico a : trafico) {
            if (Math.hypot(x - a.x, z - a.z) < RADIO_AUTO * 2) return false;
        }
        return true;
    }

    private void dibujarTrafico() {
        float px = autoX, pz = autoZ, pa = angulo;
        try {
            for (AutoTrafico a : trafico) {
                if (vistaMapa) {
                    cajaGirada(a.x, 24, a.z, 2.4f, .1f, 2.4f, a.r, a.g, a.b, a.angulo);
                } else {
                    autoX = a.x;
                    autoZ = a.z;
                    angulo = a.angulo;
                    pieza(0, .65f, 0, 1.65f, .55f, 2.6f, a.r, a.g, a.b);
                    pieza(0, 1.12f, .12f, 1.3f, .55f, 1.25f, .22f, .65f, .78f);
                    for (float x : new float[]{-.88f, .88f})
                        for (float z : new float[]{-.82f, .82f})
                            pieza(x, .38f, z, .24f, .58f, .6f, .055f, .065f, .08f);
                }
            }
        } finally {
            autoX = px;
            autoZ = pz;
            angulo = pa;
        }
    }

    protected String sectorActual() {
        if (autoX > 0 && autoZ > 0) return "SURESTE";
        if (autoX > 0) return "ESTE";
        if (autoZ > 0) return "SUR";
        return "CENTRO / NOROESTE";
    }

    private void dibujarExtras() {
        // La barrera coincide con el límite empleado por puedeCircular().
        caja(0, .7f, -LIMITE, LIMITE * 2, 1.4f, .3f, .5f, .53f, .56f);
        caja(0, .7f, LIMITE, LIMITE * 2, 1.4f, .3f, .5f, .53f, .56f);
        caja(-LIMITE, .7f, 0, .3f, 1.4f, LIMITE * 2, .5f, .53f, .56f);
        caja(LIMITE, .7f, 0, .3f, 1.4f, LIMITE * 2, .5f, .53f, .56f);
    }

    private void dibujarFuente(float x, float z) {
        caja(x, .45f, z, 1.5f, .3f, 1.5f, .55f, .6f, .65f);
        caja(x, .62f, z, 1.2f, .08f, 1.2f, .15f, .6f, .9f);
        caja(x, .9f, z, .18f, .6f, .18f, .6f, .65f, .7f);
        float altura = .6f + .15f * (float) Math.sin(tiempoAnimacion * 3);
        caja(x, 1.2f + altura / 2, z, .10f, altura, .10f, .3f, .75f, 1);
    }

    // 0.6 s de entrada, 2.2 s visible y 1.2 s de salida.
    protected float opacidadCartel() {
        float t = tiempoCartel;
        float valor = t < .6f ? t / .6f : t < 2.8f ? 1f : (4f - t) / 1.2f;
        valor = Math.max(0f, Math.min(1f, valor));
        return valor * valor * (3f - 2f * valor);
    }

    private void dibujarInterfaz() {
        interfaz.dibujar(ancho, alto, mostrarAyuda,
                Math.round(Math.abs(velocidad) * 3.6f), faros, noche,
                entregas, DESTINOS.length, sectorActual(), modoCamara, opacidadCartel());
    }

    @Override
    protected void limpiarExtras() {
        interfaz.cerrar();
    }

    /**
     * Punto de entrada del proyecto final.
     */
    public static void main(String[] args) {
        clase4 aplicacion = new clase4(); // Crea la versión con todas las etapas acumuladas.
        aplicacion.run(); // Inicia el ciclo de vida completo de la aplicación.
    }

}
