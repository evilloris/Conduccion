package com.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL33.*;
import org.lwjgl.system.MemoryUtil;

/** Panel de texto dibujado como textura; conserva el estado OpenGL de la escena. */
final class InterfazCiudad {
    private int programa, textura, vao;
    private BufferedImage imagen;
    private ByteBuffer pixeles;
    private String anterior="";
    private int w,h;
    private static final String[] CONTROLES={
        "CONTROLES", "W / S: avanzar / reversa", "A / D: girar", "ESPACIO: freno",
        "C: cambiar camara", "Q / E: girar panoramica", "N: dia / noche",
        "F: faros", "M: minimapa", "R: reiniciar", "H: ocultar ayuda", "ESC: salir"
    };
    private static final java.util.Map<String,BufferedImage> ROTULOS = new java.util.HashMap<>();
    static BufferedImage rotulo(String texto) {
        if (ROTULOS.containsKey(texto)) return ROTULOS.get(texto);
        BufferedImage im=new BufferedImage(78,15,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=im.createGraphics();
        g.setFont(new Font("SansSerif",Font.BOLD,12));g.setColor(Color.WHITE);
        int ancho=g.getFontMetrics().stringWidth(texto);
        g.drawString(texto,(78-ancho)/2,12);g.dispose();ROTULOS.put(texto,im);return im;
    }
    private int shader(int tipo,String codigo) {
        int s=glCreateShader(tipo);glShaderSource(s,codigo);glCompileShader(s);
        if(glGetShaderi(s,GL_COMPILE_STATUS)==0) throw new IllegalStateException(glGetShaderInfoLog(s));
        return s;
    }
    private void iniciar() {
        int vs=shader(GL_VERTEX_SHADER,"""
            #version 330 core
            out vec2 uv;
            void main(){
                vec2 puntos[6]=vec2[6](vec2(-1,-1),vec2(1,-1),vec2(1,1),
                                     vec2(-1,-1),vec2(1,1),vec2(-1,1));
                vec2 p=puntos[gl_VertexID];
                gl_Position=vec4(p,0,1);uv=vec2((p.x+1)/2,(1-p.y)/2);
            }
            """);
        int fs=shader(GL_FRAGMENT_SHADER,"""
            #version 330 core
            in vec2 uv;
            uniform sampler2D imagen;
            out vec4 color;
            void main(){color=texture(imagen,uv);}
            """);
        programa=glCreateProgram();glAttachShader(programa,vs);glAttachShader(programa,fs);glLinkProgram(programa);
        if(glGetProgrami(programa,GL_LINK_STATUS)==0) throw new IllegalStateException(glGetProgramInfoLog(programa));
        glDeleteShader(vs);glDeleteShader(fs);
        textura=glGenTextures();vao=glGenVertexArrays();
    }
    void dibujar(int ancho,int alto,boolean ayuda,int velocidad,boolean faros,boolean noche,
                 int entregas,int total,String sector,int camara,float opacidad) {
        int programaAnterior=glGetInteger(GL_CURRENT_PROGRAM),vaoAnterior=glGetInteger(GL_VERTEX_ARRAY_BINDING);
        int activa=glGetInteger(GL_ACTIVE_TEXTURE);glActiveTexture(GL_TEXTURE0);
        int texAnterior=glGetInteger(GL_TEXTURE_BINDING_2D);
        boolean depth=glIsEnabled(GL_DEPTH_TEST),blend=glIsEnabled(GL_BLEND);
        int sr=glGetInteger(GL_BLEND_SRC_RGB),dr=glGetInteger(GL_BLEND_DST_RGB);
        int sa=glGetInteger(GL_BLEND_SRC_ALPHA),da=glGetInteger(GL_BLEND_DST_ALPHA);
        try {
            if(programa==0) iniciar();
            if(w!=ancho||h!=alto) {
                w=ancho;h=alto;imagen=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
                if(pixeles!=null) MemoryUtil.memFree(pixeles);
                pixeles=MemoryUtil.memAlloc(w*h*4);anterior="";
            }
            String estado=ayuda+":"+velocidad+":"+faros+":"+noche+":"+entregas+":"+sector+":"+camara+":"+Math.round(opacidad*100);
            glBindTexture(GL_TEXTURE_2D,textura);
            if(!estado.equals(anterior)) {
                pintar(ayuda,velocidad,faros,noche,entregas,total,sector,camara,opacidad);
                pixeles.clear();
                int[] datos=imagen.getRGB(0,0,w,h,null,0,w);
                for(int c:datos) pixeles.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));
                pixeles.flip();
                glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,w,h,0,GL_RGBA,GL_UNSIGNED_BYTE,pixeles);
                glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
                anterior=estado;
            }
            glDisable(GL_DEPTH_TEST);glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
            glUseProgram(programa);glUniform1i(glGetUniformLocation(programa,"imagen"),0);
            glBindVertexArray(vao);glDrawArrays(GL_TRIANGLES,0,6);
        } finally {
            glUseProgram(programaAnterior);glBindVertexArray(vaoAnterior);
            glBindTexture(GL_TEXTURE_2D,texAnterior);glActiveTexture(activa);
            if(depth) glEnable(GL_DEPTH_TEST);else glDisable(GL_DEPTH_TEST);
            if(blend) glEnable(GL_BLEND);else glDisable(GL_BLEND);
            glBlendFuncSeparate(sr,dr,sa,da);
        }
    }
    private void pintar(boolean ayuda,int velocidad,boolean faros,boolean noche,int entregas,int total,String sector,int camara,float opacidad) {
        Graphics2D g=imagen.createGraphics();g.setComposite(AlphaComposite.Clear);g.fillRect(0,0,w,h);
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int margen=Math.max(4,Math.min(18,w/30));
        int tam=Math.max(9,Math.min(16,w/55));
        g.setFont(new Font("SansSerif",Font.BOLD,tam));
        int linea=tam+7;
        // Indicador mínimo de interfaz, siempre visible arriba a la izquierda.
        int diametro=36;
        g.setColor(new Color(12,22,35,220));
        g.fillOval(margen,margen,diametro,diametro);
        g.setColor(Color.WHITE);g.setStroke(new BasicStroke(2f));
        g.drawOval(margen,margen,diametro,diametro);
        for(int i=0;i<3;i++) {
            int ly=margen+12+i*6;
            g.drawLine(margen+10,ly,margen+26,ly);
        }
        g.drawString("H",margen+diametro+10,margen+25);
        // Aviso de sector independiente de la interfaz desplegable.
        if(opacidad>0f) {
            g.setComposite(AlphaComposite.SrcOver.derive(opacidad));
            int bw=Math.min(330,Math.max(110,w/3));
            int bx=(w-bw)/2,by=margen;
            g.setColor(new Color(12,22,35,230));
            g.fillRoundRect(bx,by,bw,48,16,16);
            g.setFont(new Font("SansSerif",Font.BOLD,tam+4));
            g.setColor(Color.WHITE);
            int tw=g.getFontMetrics().stringWidth(sector);
            if(tw>bw-20)g.setFont(g.getFont().deriveFont((tam+4f)*(bw-20)/tw));
            tw=g.getFontMetrics().stringWidth(sector);
            g.drawString(sector,bx+(bw-tw)/2,by+31);
            g.setComposite(AlphaComposite.SrcOver);
        }
        int inicioAyuda=margen+diametro+14;
        if(ayuda) {
            // El tamaño se adapta para dejar espacio al minimapa y al panel.
            int altoAyuda=Math.min(CONTROLES.length*linea+18,Math.max(20,h-105-inicioAyuda));
            int anchoAyuda=Math.min(260,Math.max(100,w/2-margen*2));
            g.setColor(new Color(12,22,35,220));g.fillRoundRect(margen,inicioAyuda,anchoAyuda,altoAyuda,12,12);
            g.setClip(margen,inicioAyuda,anchoAyuda,altoAyuda);
            for(int i=0;i<CONTROLES.length;i++) {
                g.setColor(i==0?new Color(100,215,255):Color.WHITE);
                dibujarAjustado(g,CONTROLES[i],margen+10,inicioAyuda+linea*(i+1),anchoAyuda-20,tam);
            }
            g.setClip(null);
        }
        if(ayuda) {
        int ph=Math.min(88,Math.max(40,h/3)),y=h-ph-margen;
        g.setColor(new Color(12,22,35,230));g.fillRoundRect(margen,y,w-margen*2,ph,12,12);
        g.setColor(Color.WHITE);
        String[] textos={"VELOCIDAD: "+velocidad+" km/h   |   FAROS: "+(faros?"ENCENDIDOS":"APAGADOS"),
            "SECTOR: "+sector+"   |   ENTREGAS: "+entregas+"/"+total+(entregas==total?" - COMPLETADAS":""),
            "AMBIENTE: "+(noche?"NOCHE":"DIA")+"   |   CAMARA: "+new String[]{"TERCERA PERSONA","AEREA","PANORAMICA"}[camara]};
        for(int i=0;i<3;i++) dibujarAjustado(g,textos[i],margen+10,y+(i+1)*(ph/4),w-margen*2-20,tam);
        }
        g.dispose();
    }
    private void dibujarAjustado(Graphics2D g,String texto,int x,int y,int disponible,int tam) {
        Font font=new Font("SansSerif",Font.BOLD,tam);g.setFont(font);
        int longitud=g.getFontMetrics().stringWidth(texto);
        if(longitud>disponible) g.setFont(font.deriveFont(Math.max(5f,tam*(float)disponible/longitud)));
        g.drawString(texto,x,y);
    }
    void cerrar() {
        if(programa!=0) glDeleteProgram(programa);
        if(textura!=0) glDeleteTextures(textura);
        if(vao!=0) glDeleteVertexArrays(vao);
        if(pixeles!=null) MemoryUtil.memFree(pixeles);
        programa=textura=vao=0;pixeles=null;
    }
}
