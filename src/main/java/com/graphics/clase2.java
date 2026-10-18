
package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class clase2 extends clase1 {

    // ==================== 1. VARIABLES DEL AUTO ====================
    protected float autoX = centro(0);
    protected float autoZ = centro(MAPA.length - 1) - 5.0f;
    protected float angulo = 0;
    protected float velocidad = 0;
    protected int modoCamara = 0;
    protected boolean camaraAerea = false;

    protected static final float RADIO_AUTO = 1.65f;
    protected static final float DISTANCIA_CARRETA = 4.0f;
    protected static final float ADELANTO_INICIAL = 5.0f;

    // ==================== 2. VARIABLES DE LA CARRETA ====================
    protected float carretaX = centro(0);
    protected float carretaZ =
            centro(MAPA.length - 1) - ADELANTO_INICIAL
                    + DISTANCIA_CARRETA;

    // ==================== 3. TECLADO Y REINICIO ====================
    @Override
    protected void tecla(int key) {
        super.tecla(key);

        if (key == GLFW_KEY_C) {
            modoCamara = (modoCamara + 1) % 3;
            camaraAerea = modoCamara != 0;
        }

        if (key == GLFW_KEY_R) {
            reiniciar();
        }
    }

    protected void reiniciar() {
        autoX = centro(0);
        autoZ = centro(MAPA.length - 1) - ADELANTO_INICIAL;
        angulo = 0;
        velocidad = 0;

        carretaX = autoX;
        carretaZ = autoZ + DISTANCIA_CARRETA;
    }

    // ==================== 4. MOVIMIENTO ====================
    @Override
    protected void actualizar(float deltaTime) {

        if (modoCamara == 2) {
            if (pulsada(GLFW_KEY_Q)) orbita -= deltaTime;
            if (pulsada(GLFW_KEY_E)) orbita += deltaTime;
        }

        float acelerador = 0;

        if (pulsada(GLFW_KEY_W) || pulsada(GLFW_KEY_UP)) {
            acelerador += 1;
        }

        if (pulsada(GLFW_KEY_S) || pulsada(GLFW_KEY_DOWN)) {
            acelerador -= 1;
        }

        velocidad += acelerador * 14 * deltaTime;

        float resistencia = pulsada(GLFW_KEY_SPACE) ? 7 : 0.7f;

        velocidad *= (float) Math.exp(-resistencia * deltaTime);
        velocidad = Math.max(-6, Math.min(17, velocidad));

        float direccion = 0;

        if (pulsada(GLFW_KEY_A) || pulsada(GLFW_KEY_LEFT)) {
            direccion += 1;
        }

        if (pulsada(GLFW_KEY_D) || pulsada(GLFW_KEY_RIGHT)) {
            direccion -= 1;
        }

        float nuevoAngulo =
                angulo + direccion * velocidad * 0.11f * deltaTime;

        float frenteX = -(float) Math.sin(nuevoAngulo);
        float frenteZ = -(float) Math.cos(nuevoAngulo);

        float siguienteX = autoX + frenteX * velocidad * deltaTime;
        float siguienteZ = autoZ + frenteZ * velocidad * deltaTime;

        float siguienteCarretaX =
                siguienteX
                        + (float) Math.sin(nuevoAngulo)
                        * DISTANCIA_CARRETA;

        float siguienteCarretaZ =
                siguienteZ
                        + (float) Math.cos(nuevoAngulo)
                        * DISTANCIA_CARRETA;

        boolean autoLibre =
                puedeCircular(siguienteX, siguienteZ);

        boolean carretaLibre =
                puedeCircularCarreta(
                        siguienteCarretaX,
                        siguienteCarretaZ,
                        nuevoAngulo);

        if (autoLibre && carretaLibre) {
            autoX = siguienteX;
            autoZ = siguienteZ;
            angulo = nuevoAngulo;

            carretaX = siguienteCarretaX;
            carretaZ = siguienteCarretaZ;
        } else {
            velocidad = 0;
        }

        actualizarTitulo();
    }

    // ==================== 5. COLISIONES DE LA CARRETA ====================
    protected boolean puedeCircularCarreta(
            float x, float z, float anguloCarreta) {

        float coseno = (float) Math.cos(anguloCarreta);
        float seno = (float) Math.sin(anguloCarreta);

        float[] puntosX = {-1.45f, 1.45f, -1.45f, 1.45f, 0, 0};
        float[] puntosZ = {-2.25f, -2.25f, 2.25f, 2.25f, -2.25f, 2.25f};

        for (int i = 0; i < puntosX.length; i++) {
            float puntoX =
                    x + coseno * puntosX[i] + seno * puntosZ[i];

            float puntoZ =
                    z - seno * puntosX[i] + coseno * puntosZ[i];

            if (!puntoCarretaLibre(puntoX, puntoZ)) {
                return false;
            }
        }

        return true;
    }

    protected boolean puntoCarretaLibre(float x, float z) {
        float radio = 0.45f;
        float limitePermitido = LIMITE - radio;

        if (Math.abs(x) > limitePermitido
                || Math.abs(z) > limitePermitido) {
            return false;
        }

        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0;
                 columna < MAPA[fila].length;
                 columna++) {

                if (MAPA[fila][columna] == 0) {
                    continue;
                }

                float centroX = centro(columna);
                float centroZ = centro(fila);

                float cercaX = Math.max(
                        centroX - 5,
                        Math.min(x, centroX + 5));

                float cercaZ = Math.max(
                        centroZ - 5,
                        Math.min(z, centroZ + 5));

                float dx = x - cercaX;
                float dz = z - cercaZ;

                if (dx * dx + dz * dz < radio * radio) {
                    return false;
                }
            }
        }

        return true;
    }

    // ==================== 6. TITULO DE LA VENTANA ====================
    private void actualizarTitulo() {
        int kilometrosPorHora =
                Math.round(Math.abs(velocidad) * 3.6f);

        String titulo = getClass().getSimpleName();
        titulo += " | WASD/flechas: conducir";
        titulo += " | Espacio: freno";
        titulo += " | C: camara | R: reiniciar";
        titulo += " | " + kilometrosPorHora + " km/h";
        titulo += estadoExtra();

        if (ventana != 0) {
            glfwSetWindowTitle(ventana, titulo);
        }
    }

    protected String estadoExtra() {
        return "";
    }

    // ==================== 7. COLISIONES DEL AUTO ====================
    protected boolean puedeCircular(float x, float z) {
        float limitePermitido = LIMITE - RADIO_AUTO;

        if (Math.abs(x) > limitePermitido
                || Math.abs(z) > limitePermitido) {
            return false;
        }

        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0;
                 columna < MAPA[fila].length;
                 columna++) {

                if (MAPA[fila][columna] == 0) {
                    continue;
                }

                float centroX = centro(columna);
                float centroZ = centro(fila);

                float cercaX = Math.max(
                        centroX - 5,
                        Math.min(x, centroX + 5));

                float cercaZ = Math.max(
                        centroZ - 5,
                        Math.min(z, centroZ + 5));

                float dx = x - cercaX;
                float dz = z - cercaZ;

                if (dx * dx + dz * dz
                        < RADIO_AUTO * RADIO_AUTO) {
                    return false;
                }
            }
        }

        return true;
    }

    // ==================== 8. CÁMARA ====================
    @Override
    protected void configurarCamara() {
        if (camaraAerea) {
            if (modoCamara == 1) {
                vector("uOjo", autoX, 35, autoZ + 0.01f);
                vector("uObjetivo", autoX, 0, autoZ);
                decimal("uAspecto", (float) ancho / alto);
            } else {
                super.configurarCamara();
            }
            return;
        }

        float camaraX =
                autoX + (float) Math.sin(angulo) * 12;

        float camaraZ =
                autoZ + (float) Math.cos(angulo) * 12;

        vector("uOjo", camaraX, 9, camaraZ);
        vector("uObjetivo", autoX, 0.8f, autoZ);
        decimal("uAspecto", (float) ancho / alto);
    }

    // ==================== 9. DIBUJO DE LA ESCENA ====================
    @Override
    protected void escena() {
        super.escena();

        dibujarAuto();
        dibujarCarreta();
    }

    // ==================== 10. DIBUJO DEL AUTO ====================
    protected void dibujarAuto() {
        pieza(0, 0.65f, 0,
                1.65f, 0.55f, 2.6f,
                0.95f, 0.24f, 0.12f);

        pieza(0, 1.12f, 0.12f,
                1.3f, 0.55f, 1.25f,
                0.22f, 0.65f, 0.78f);

        float[] ladosRuedas = {-0.88f, 0.88f};
        float[] ejesRuedas = {-0.82f, 0.82f};

        for (float x : ladosRuedas) {
            for (float z : ejesRuedas) {
                pieza(x, 0.38f, z,
                        0.24f, 0.58f, 0.6f,
                        0.055f, 0.065f, 0.08f);
            }
        }

        float[] ladosFaros = {-0.55f, 0.55f};

        for (float x : ladosFaros) {
            pieza(x, 0.68f, -1.32f,
                    0.38f, 0.2f, 0.07f,
                    1, 0.95f, 0.65f);

            pieza(x, 0.68f, 1.32f,
                    0.35f, 0.17f, 0.07f,
                    0.85f, 0.05f, 0.05f);
        }
    }

    // ==================== 11. DIBUJO DE LA CARRETA ====================
    protected void dibujarCarreta() {
        pieza(0, 0.48f, 2.65f,
                0.16f, 0.16f, 2.0f,
                0.18f, 0.18f, 0.18f);

        piezaCarreta(0, 0.62f, 0,
                1.65f, 0.28f, 2.45f,
                0.22f, 0.22f, 0.24f);

        piezaCarreta(0, 1.02f, 0,
                1.48f, 0.62f, 2.15f,
                0.72f, 0.28f, 0.12f);

        piezaCarreta(0, 1.38f, -0.98f,
                1.48f, 0.12f, 0.10f,
                0.86f, 0.42f, 0.18f);

        piezaCarreta(0, 1.38f, 0.98f,
                1.48f, 0.12f, 0.10f,
                0.86f, 0.42f, 0.18f);

        piezaCarreta(-0.69f, 1.38f, 0,
                0.10f, 0.12f, 1.95f,
                0.86f, 0.42f, 0.18f);

        piezaCarreta(0.69f, 1.38f, 0,
                0.10f, 0.12f, 1.95f,
                0.86f, 0.42f, 0.18f);

        float[] lados = {-0.88f, 0.88f};
        float[] ejes = {-0.72f, 0.72f};

        for (float x : lados) {
            for (float z : ejes) {
                piezaCarreta(x, 0.35f, z,
                        0.22f, 0.52f, 0.48f,
                        0.06f, 0.06f, 0.07f);
            }
        }
    }

    // ==================== 12. PIEZAS DE LA CARRETA ====================
    protected void piezaCarreta(
            float x, float y, float z,
            float sx, float sy, float sz,
            float r, float g, float b) {

        float coseno = (float) Math.cos(angulo);
        float seno = (float) Math.sin(angulo);

        float mundoX =
                carretaX + coseno * x + seno * z;

        float mundoZ =
                carretaZ - seno * x + coseno * z;

        cajaGirada(
                mundoX, y, mundoZ,
                sx, sy, sz,
                r, g, b, angulo);
    }

    // ==================== 13. PIEZAS DEL AUTO ====================
    protected void pieza(
            float x, float y, float z,
            float sx, float sy, float sz,
            float r, float g, float b) {

        float coseno = (float) Math.cos(angulo);
        float seno = (float) Math.sin(angulo);

        float mundoX =
                autoX + coseno * x + seno * z;

        float mundoZ =
                autoZ - seno * x + coseno * z;

        cajaGirada(
                mundoX, y, mundoZ,
                sx, sy, sz,
                r, g, b, angulo);
    }

    // ==================== 14. INICIO DEL PROGRAMA ====================
    public static void main(String[] args) {
        clase2 aplicacion = new clase2();
        aplicacion.run();
    }
}
