# Ciudad interactiva ampliada con OpenGL

## Ejecutar

Requisitos: JDK 17 o posterior (no solamente JRE), Maven 3 y controlador gráfico
compatible con OpenGL 3.3. Desde la carpeta que contiene pom.xml:

```sh
mvn clean compile exec:exec
```

Este comando ejecuta `com.graphics.clase4`, la versión final. El perfil de Maven
para macOS añade automáticamente `-XstartOnFirstThread`. Windows y Linux no lo necesitan.
La primera compilación requiere conexión para descargar LWJGL y los plugins de Maven.
Para ejecutar las pruebas de lógica: `mvn test`.

## Controles

| Tecla | Acción |
| --- | --- |
| W / flecha arriba | Acelerar |
| S / flecha abajo | Frenar con motor y retroceder |
| A, D / flechas izquierda, derecha | Girar mientras el automóvil se mueve |
| Espacio | Freno |
| C | Alternar cámara de seguimiento y cámara general |
| N | Alternar día y noche |
| F | Encender/apagar los dos faros delanteros |
| M | Mostrar/ocultar minimapa |
| R | Reiniciar posición, orientación, velocidad, entregas y cronómetros |
| Esc | Salir |

## Diseño y funciones

Ciudad de 11 x 11 celdas de 10 unidades: 110 x 110 unidades, límites en ±55.
Hay 21 manzanas con edificios de distintos colores y alturas, y cuatro parques.
Las avenidas horizontales y verticales se cruzan cada dos celdas; todas las calles
pertenecen a una única red. Inicio en (-50, 50), orientado al norte (-Z).
Las entregas están en (50, -50), (-50, -50) y (-50, 50), sobre calles accesibles.
Frenar a menos de 1 unidad/s dentro de un radio de 3 unidades completa cada entrega.
El título muestra velocidad, ambiente, faros, progreso, cronómetro y victoria.

Suelo, centros de celda, límites de colisión, cámara general y proyección del minimapa
se adaptan a las dimensiones de la matriz. La colisión usa un círculo de radio 1,65
contra aceras de edificios y parques y contra los bordes exteriores.
El movimiento usa el tiempo entre cuadros, limitado a 50 ms para evitar saltos.

La iluminación GLSL usa normales transformadas, luz ambiental y direccional.
Veinticinco farolas distribuidas sobre las aceras, incluidas las nuevas manzanas,
aportan luz difusa cálida con atenuación por distancia. Los dos focos delanteros
siguen al vehículo, tienen cono suavizado y su aporte llega a cero a 24 unidades.
La emisión visible de bombillas y ventanas se calcula por separado de la luz difusa.
Los parques tienen árboles y bancos; los edificios, ventanas que brillan de noche.
Hay pasos peatonales y semáforos con ciclo rojo (5 s), verde (5 s), amarillo (2 s).
La animación de los semáforos continúa después de finalizar las entregas.

El minimapa simultáneo tiene norte hacia arriba, automóvil orientado en cian con
punta blanca y destino dorado. Su tamaño se adapta al framebuffer. El segundo pase
usa viewport y scissor, borra únicamente su recuadro y restaura el estado de dibujo.
R inicia un nuevo recorrido de entregas; conserva las preferencias de cámara y luces.

## Organización del código

- `clase1`: matriz de ciudad, geometría con normales, cámara general y ciclo OpenGL.
- `clase2`: conducción, colisiones, cámara de seguimiento y modelo del automóvil.
- `clase3`: modos día/noche, farolas y focos, shader de iluminación.
- `clase4`: decoración, semáforos, entregas, reinicio y minimapa.
- `ConduccionTest`: distribución, conectividad, colisiones de calles nuevas, finalización y reinicio.

Las etapas previas pueden abrirse con `mvn compile exec:exec -DmainClass=com.graphics.clase1`
(cambiar el número por 2 o 3). La ciudad ampliada es compartida por las cuatro etapas.

## Demostración en clase

1. Conducir por las avenidas exteriores y sectores nuevos. Intentar entrar en una
   manzana y salir del mapa para comprobar las colisiones.
2. Pulsar C, N, F y M; observar luz sobre asfalto/fachadas y orientación del mapa.
3. Redimensionar la ventana y comprobar que la escena y el minimapa conservan su encuadre.
4. Frenar sucesivamente en las tres marcas doradas; verificar el título de victoria.
5. Esperar y observar que los semáforos siguen cambiando; pulsar R y repetir las entregas.

## Alcance, limitaciones y recursos

Se implementan los requisitos obligatorios. No se incluyen las mejoras opcionales de
tráfico autónomo ni panel gráfico dentro de la ventana. Los semáforos son visuales.
La iluminación no proyecta sombras ni simula oclusión; los edificios pueden recibir luz
calculada de una fuente situada detrás de otro objeto. El mundo utiliza cajas de estilo
simplificado. Las farolas están sobre aceras inaccesibles al vehículo.

Se conserva y adapta el código del proyecto CiudadConduccion proporcionado por el usuario.
La geometría se genera por código; no se incorporan modelos, texturas ni fragmentos externos.
Dependencias externas: LWJGL 3.3.3 (GLFW y OpenGL), JUnit y plugins Maven declarados en pom.xml.
Se excluyen resultados de compilación, dependencias descargadas y archivos temporales del ZIP.

## Validación de esta entrega

Las cuatro clases y las pruebas compilaron con nivel de lenguaje Java 17.
Pasaron tres pruebas automatizadas de distribución/conectividad, colisiones y
finalización/reinicio. Los shaders de la versión final compilaron y enlazaron en
un contexto OpenGL 3.3 de Mesa mediante EGL. La interacción de teclado y el
redimensionamiento de la ventana requieren la demostración manual descrita arriba;
no se ejecutó la ventana GLFW en el entorno de preparación.
