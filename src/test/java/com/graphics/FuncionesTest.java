package com.graphics;
import junit.framework.TestCase;
import static org.lwjgl.glfw.GLFW.*;

public class FuncionesTest extends TestCase {
    private static class Juego extends clase4 {
        boolean acelerar;
        @Override protected boolean pulsada(int key) { return acelerar && key==GLFW_KEY_W; }
        void avanzarMotor(float dt) { super.actualizar(dt); }
    }
    public void testControlesYArranque() {
        Juego j=new Juego();assertFalse(j.noche);assertFalse(j.mostrarAyuda);
        j.tecla(GLFW_KEY_H);assertTrue(j.mostrarAyuda);
        j.tecla(GLFW_KEY_H);assertFalse(j.mostrarAyuda);
        for(int i=1;i<=3;i++){j.tecla(GLFW_KEY_C);assertEquals(i%3,j.modoCamara);}
        j.tecla(GLFW_KEY_N);assertTrue(j.noche);j.tecla(GLFW_KEY_N);assertFalse(j.noche);
        assertEquals(5,j.trafico.length);
        j.autoX=20;j.autoZ=20;assertEquals("SURESTE",j.sectorActual());
    }
    public void testCartelSector() {
        Juego j=new Juego();j.actualizar(.05f);
        assertEquals("SUR",j.ultimoSector);assertEquals(0f,j.opacidadCartel(),0f);
        j.tiempoCartel=.3f;assertEquals(.5f,j.opacidadCartel(),.001f);
        j.tiempoCartel=2f;assertEquals(1f,j.opacidadCartel(),0f);
        j.tiempoCartel=3.4f;assertEquals(.5f,j.opacidadCartel(),.001f);
        j.tiempoCartel=4f;assertEquals(0f,j.opacidadCartel(),0f);
        j.autoX=20;j.autoZ=20;j.actualizar(.05f);
        assertEquals("SURESTE",j.ultimoSector);assertEquals(0f,j.tiempoCartel,0f);
    }
    public void testVelocidadYBordes() {
        Juego j=new Juego();j.acelerar=true;
        // Mantener el auto en un tramo despejado permite medir el motor sin choques.
        for(int i=0;i<400;i++){j.autoX=-10;j.autoZ=0;j.avanzarMotor(.05f);}
        assertEquals(17f,j.velocidad,.01f);
        assertFalse(j.puedeCircular(54,0));
    }
    public void testTraficoRojoVerdeYJugador() {
        Juego j=new Juego();clase4.AutoTrafico a=j.trafico[0];
        a.x=-50;a.z=-26;a.tramo=1;j.tiempoAnimacion=1;
        j.actualizarTrafico(.05f);assertEquals(-26f,a.z,0f);
        j.tiempoAnimacion=6;j.actualizarTrafico(.05f);assertTrue(a.z < -26);
        a.x=-50;a.z=-20;a.tramo=1;j.autoX=-50;j.autoZ=-23;
        j.actualizarTrafico(.05f);assertEquals(-20f,a.z,0f);
        assertFalse(j.puedeCircular(a.x,a.z));
    }
    public void testCircuitosSinAtravesarNiSalir() {
        Juego j=new Juego();j.autoX=0;j.autoZ=0;
        float[] recorridos=new float[5];
        for(int paso=0;paso<4000;paso++) {
            j.tiempoAnimacion+=.05f;
            float[] xs=new float[5],zs=new float[5];
            for(int i=0;i<5;i++){xs[i]=j.trafico[i].x;zs[i]=j.trafico[i].z;}
            j.actualizarTrafico(.05f);
            for(int i=0;i<5;i++) {
                clase4.AutoTrafico a=j.trafico[i];
                recorridos[i]+=Math.hypot(a.x-xs[i],a.z-zs[i]);
                assertTrue(Math.abs(a.x)<=50 && Math.abs(a.z)<=50);
                assertTrue(Math.hypot(a.x-j.autoX,a.z-j.autoZ)>=3.3);
                for(int k=i+1;k<5;k++)assertTrue(Math.hypot(a.x-j.trafico[k].x,a.z-j.trafico[k].z)>=3.3);
            }
        }
        for(float distancia:recorridos) assertTrue(distancia>20);
    }
}
