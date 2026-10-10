package com.graphics;

import java.util.ArrayDeque;
import junit.framework.TestCase;

/** Comprueba el escenario y las reglas sin requerir una ventana. */
public class ConduccionTest extends TestCase {
    private static class Juego extends clase4 {
        @Override protected boolean pulsada(int key) { return false; }
    }

    public void testDistribucionYAcceso() {
        assertEquals(11, clase1.MAPA.length);
        assertEquals(10f, clase1.CELDA, 0f);
        int edificios = 0, parques = 0;
        boolean[][] visitado = new boolean[11][11];
        ArrayDeque<int[]> pendientes = new ArrayDeque<>();
        pendientes.add(new int[]{10, 0});
        visitado[10][0] = true;
        while (!pendientes.isEmpty()) {
            int[] celda = pendientes.remove();
            for (int[] paso : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int f = celda[0] + paso[0], c = celda[1] + paso[1];
                if (f >= 0 && f < 11 && c >= 0 && c < 11 && !visitado[f][c] && clase1.MAPA[f][c] == 0) {
                    visitado[f][c] = true;
                    pendientes.add(new int[]{f,c});
                }
            }
        }
        Juego juego = new Juego();
        for (int f = 0; f < 11; f++) {
            assertEquals(11, clase1.MAPA[f].length);
            for (int c = 0; c < 11; c++) {
                int tipo = clase1.MAPA[f][c];
                if (tipo == 1) edificios++;
                if (tipo == 2) parques++;
                if (tipo == 0) {
                    assertTrue(visitado[f][c]);
                    assertTrue(juego.puedeCircular(clase1.centro(c), clase1.centro(f)));
                } else assertFalse(juego.puedeCircular(clase1.centro(c), clase1.centro(f)));
            }
        }
        assertEquals(21, edificios);
        assertEquals(4, parques);
        for (float[] destino : clase4.DESTINOS) assertTrue(juego.puedeCircular(destino[0], destino[1]));
        assertEquals(25, clase3.LUCES.length);
    }

    public void testCallesNuevasYColisiones() {
        Juego juego = new Juego();
        for (float t = -50; t <= 50; t += 0.25f) {
            assertTrue(juego.puedeCircular(-50, t));
            assertTrue(juego.puedeCircular(t, -50));
            assertTrue(juego.puedeCircular(50, t));
            assertTrue(juego.puedeCircular(t, 50));
        }
        assertFalse(juego.puedeCircular(54, 0));
        assertFalse(juego.puedeCircular(0, -56));
        assertFalse(juego.puedeCircular(-45.5f, -40));
        assertTrue(juego.puedeCircular(-47, -40));
    }

    public void testEntregasAnimacionYReinicio() {
        Juego juego = new Juego();
        for (float[] destino : clase4.DESTINOS) {
            juego.autoX = destino[0]; juego.autoZ = destino[1]; juego.velocidad = 0;
            juego.actualizar(0.05f);
        }
        assertEquals(3, juego.entregas);
        float tiempoFinal = juego.tiempo, animacion = juego.tiempoAnimacion;
        juego.actualizar(0.05f);
        assertEquals(tiempoFinal, juego.tiempo, 0f);
        assertTrue(juego.tiempoAnimacion > animacion);
        juego.velocidad = 10; juego.angulo = 2;
        juego.reiniciar();
        assertEquals(-50f, juego.autoX, 0f);
        assertEquals(50f, juego.autoZ, 0f);
        assertEquals(0f, juego.velocidad, 0f);
        assertEquals(0f, juego.angulo, 0f);
        assertEquals(0, juego.entregas);
        assertEquals(0f, juego.tiempo, 0f);
        juego.autoX = clase4.DESTINOS[0][0]; juego.autoZ = clase4.DESTINOS[0][1];
        juego.actualizar(0.05f);
        assertEquals(1, juego.entregas);
    }
}
