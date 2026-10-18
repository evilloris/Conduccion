# Ciudad interactiva completa

## Ejecutar en IntelliJ
1. Descomprime el ZIP.
2. Abre esta carpeta, que contiene pom.xml, como proyecto Maven.
3. Selecciona un JDK 17 o superior y permite descargar las dependencias.
4. Ejecuta el main de src/main/java/com/graphics/clase4.java.

Desde consola: `mvn clean test compile exec:exec`.

## Funciones
- Ciudad de 11 por 11 celdas: 21 edificios, 4 parques, 25 farolas.
- Arranque diurno, automóvil rojo, interfaz oculta al iniciar; icono circular de tres líneas y H arriba a la izquierda y minimapa superior derecho.
- W/S o flechas arriba/abajo: acelerar y reversa; A/D o flechas laterales: girar.
- Espacio: freno fuerte. Máximo hacia delante: 17 unidades/s, unos 61 km/h.
- H: mostrar/ocultar ayuda y panel. M: minimapa. N: día/noche manual. F: faros. R: reiniciar entregas y tráfico. ESC: salir.
- C: tercera persona, aérea sobre el auto y panorámica de toda la ciudad.
- Q/E: girar en modo panorámico.
- Sectores ESTE, SUR y SURESTE: aviso arriba centrado con fade in de 0.6 s, permanencia de 2.2 s y fade out de 1.2 s al cambiar de sector. Se quitaron los carteles físicos.
- Barrera gris exterior, colisiones con manzanas y vehículos.
- Parques con cuatro árboles, banco y fuente animada.
- Pasos peatonales y semáforos sincronizados: rojo 5 s, verde 5 s y amarillo 2 s.
- Cinco autos de colores siguen un circuito perimetral de un solo sentido, giran en sus esquinas, esperan antes de los cruces con rojo/amarillo y mantienen separación del jugador y otros autos.
- El minimapa muestra el norte arriba, jugador orientado, destino y tráfico.
- Farolas cálidas con atenuación, dos faros delanteros con alcance limitado y ventanas nocturnas.

## Organización
clase1: ciudad, ventana y dibujo básico.
clase2: jugador, controles, colisiones y cámaras.
clase3: iluminación y cambio manual de ambiente.
clase4: decoración, entregas, tráfico, minimapa y panel.
InterfazCiudad: dibuja la ayuda y el panel como textura OpenGL con texto generado por Java.

## Validación
Compilación Java 17 con LWJGL 3.3.3. Ocho pruebas JUnit aprobadas.
Ventana OpenGL probada con Mesa/Xvfb: día/noche, cambios de cámara, ayuda, minimapa y redimensionamiento.
En el equipo de destino conviene recorrer la lista de controles para comprobar el teclado y el rendimiento de su tarjeta gráfica.

Esta versión conserva N como interruptor manual para cumplir la lista de comprobación; no incorpora el ejercicio separado del ciclo automático de 12 minutos.
