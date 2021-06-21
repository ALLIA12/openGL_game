package org.yourorghere;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.GLUT;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;

public class TempProject implements GLEventListener, KeyListener {

    int frames = 0;
    float[] hitBox = new float[6];
    float Theta = 0;
    int yMover = 2;
    boolean stop = false;
    float playerPosition[] = {0, 1, 0, 1, 0, 1};
    int level = 0;
    int life = 5;
    float objectRotation = 0;
    float rotation_angle = 0;
    float cam_zoom_IO = 0;
    float cam_LR = 0;
    float cam_UD = 0;
    float[] enemyCPosition = {0, 0, 0, 0, 0, 1};
    float[] enemyCPosition2 = {0, 0, 0, 0, 0, 1};
    float[] enemyCPosition3 = {0, 0, 0, 0, 0, 1};
    boolean[] detec = {false, false, false};
    float newPosition[] = {0, 1, 0, 1, 0, 1};
    float newPosition2[] = {0, 1, 0, 1, 0, 1};
    float newPosition3[] = {0, 1, 0, 1, 0, 1};
    double stars[][] = new double[2][100];
    GLU glu;
    int width = 1280;
    int height = 720;
    boolean[] newPositioner = {false, false, false};

    public static void main(String[] args) {

        Frame frame = new Frame("Astro man");
        GLCanvas canvas = new GLCanvas();
        TempProject obj = new TempProject();
        canvas.addGLEventListener(obj);
        canvas.addKeyListener(obj);
        frame.add(canvas);
        frame.setSize(1280, 720);
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {

                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        // Center frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        // Use debug pipeline
        // drawable.setGL(new DebugGL(drawable.getGL()));
        GL gl = drawable.getGL();
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        // Enable VSync
        gl.setSwapInterval(1);
        // Setup the drawing area and shading mode
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        playerPosition[0] = -16;
        playerPosition[1] = -15;
        playerPosition[2] = 0;
        playerPosition[3] = 1;
        rotation_angle = 0;
        glu = new GLU();

    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        glu.gluLookAt(0, 0, 0, 0, 0, 0, x, y, y);
        if (height <= 0) { // avoid a divide by zero error!
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1.0, 50.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        startUp(gl);
        //this will be used as the refrence to all  hit boxes
        float[] hitBoxChecker = new float[6];
        if (stop) {
            return;
        }
        if (level == 0) {
            hitBoxChecker = DrawEnemy(10, 3, 0, 1, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, gl, enemyCPosition);
            enemyKiller(hitBoxChecker, gl, 0);
            //playerPosition[3] -= 0.01f;
            //playerPosition[2] -= 0.01f;
        } else if (level == 1) {
            Theta = (float) ((Theta + 0.01));
            gl.glColor3f(1.0f, 0f, 0f);
            hitBoxChecker = DrawMovingObject(0, 0, 5, 1, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(5, 0, 5, 2, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(-10, 0, 6, 3, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(-12, 0, 2, 1, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(10, 0, 2, 2, gl);
            onCollisionReset(hitBoxChecker);
            //playerPosition[3] -= 0.01f;
            //playerPosition[2] -= 0.01f;
        } else if (level == 2) {
            // Clear the drawing area
            // Reset the current matrix to the "identity"
            // Move the "drawing cursor" around
            DrawPlayer(gl);
            showLives(gl);
            gl.glTranslatef(-4, 0.0f, 0);
            Theta = (float) ((Theta + 0.01));
            gl.glColor3f(1.0f, 0f, 0f);
            hitBoxChecker = DrawMovingObject(0, 0, 2, 2, gl);
            onCollisionReset(hitBoxChecker);
        } else if (level == 3) {
            level = 0;
        }
        // Flush all drawing operations to the graphics card
        gl.glFlush();

    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void keyTyped(KeyEvent key) {
        char keyChar = key.getKeyChar();
        double modifier = 1;
        if (key.isShiftDown()) {
            modifier = 5;
        }
        if (keyChar == KeyEvent.VK_D || keyChar == 'd') {
            rotation_angle += .5f;
            System.out.println(rotation_angle / 180 + "    d");

        } else if (keyChar == KeyEvent.VK_A || keyChar == 'a') {
            rotation_angle -= .5f;
            System.out.println(rotation_angle / 180 + "    a");

        } else if (keyChar == KeyEvent.VK_W || keyChar == 'w') {
            playerPosition[0] += 0.1f * modifier * Math.sin(rotation_angle);
            playerPosition[1] += 0.1f * modifier * Math.sin(rotation_angle);
            playerPosition[3] += 0.1f * modifier * Math.cos(rotation_angle);
            playerPosition[2] += 0.1f * modifier * Math.cos(rotation_angle);
            System.out.println(rotation_angle + "    w");
        } else if (keyChar == KeyEvent.VK_S || keyChar == 's') {
            playerPosition[3] -= 0.1f * modifier;
            playerPosition[2] -= 0.1f * modifier;
        } else if (keyChar == KeyEvent.VK_L || keyChar == 'l') {
            level++;
            playerPosition[0] = -16;
            playerPosition[1] = -15;
            playerPosition[2] = 0;
            playerPosition[3] = 1;
            rotation_angle = 0;
        } else if (keyChar == '2') {
            cam_UD++;
            System.out.println(cam_UD);
        } else if (keyChar == '8') {
            cam_UD--;
            System.out.println(cam_UD);
        } else if (keyChar == '4') {
            cam_LR++;
            System.out.println(cam_LR);
        } else if (keyChar == '6') {
            cam_LR--;
            System.out.println(cam_LR);
        } else if (keyChar == '9') {
            cam_zoom_IO++;
            System.out.println(cam_zoom_IO);
        } else if (keyChar == '7') {
            cam_zoom_IO--;
            System.out.println(cam_zoom_IO);
        } else if (keyChar == '5') {
            cam_zoom_IO = 0;
            cam_UD = 0;
            cam_LR = 0;
            System.out.println("RESET ALL");
        }
    }

    public void keyPressed(KeyEvent ke) {
        char keyChar = ke.getKeyChar();
        if (keyChar == KeyEvent.VK_SPACE) {
            stop = !stop;
        }
    }

    public void keyReleased(KeyEvent ke) {
    }

    private void onCollisionReset(float[] hitBoxChecker) {
        boolean x1 = hitBoxChecker[0] <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1];
        boolean x2 = hitBoxChecker[0] <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1];
        boolean y1 = hitBoxChecker[2] <= playerPosition[2] && playerPosition[2] <= hitBoxChecker[3];
        boolean y2 = hitBoxChecker[2] <= playerPosition[3] && playerPosition[3] <= hitBoxChecker[3];
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            life--;
            if (life == 0) {
                System.out.println("You died, RIP");
                System.exit(0);
            }
            System.out.println("You have this many lifes remanining: " + life);
            playerPosition[0] = -16;
            playerPosition[1] = -15;
            playerPosition[2] = 0;
            playerPosition[3] = 1;
            rotation_angle = 0;
        }
    }

    private float[] DrawBorderT(GL gl) {
        int minX = -12;
        int maxX = 35;
        int maxY = 7;
        int minY = 6;
        int minZ = 0;
        int maxZ = 1;
        float[] objectPosition = {minX - 10, maxX - 10, minY + 6, maxY + 6, minZ, maxZ};
        gl.glPushMatrix();
        gl.glTranslatef(minX, minY, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 0f, 0f);
        // front       
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, minY, minZ);

        //back 
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // left side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(maxX, minY, maxZ);

        // right side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(minX, minY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // top 
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, minZ);

        // bottom 
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);
        gl.glVertex3f(minX, minY, minZ);
        gl.glEnd();
        gl.glPopMatrix();

        return objectPosition;
    }

    private float[] DrawBorderB(GL gl) {
        int minX = -12;
        int maxX = 35;
        int maxY = -6;
        int minY = -7;
        int minZ = 0;
        int maxZ = 1;
        float[] objectPosition = {minX - 10, maxX - 10, minY - 5.8f, maxY - 4.8f, minZ, maxZ};
        gl.glPushMatrix();
        gl.glTranslatef(minX, minY, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 0f, 0f);
        // front       
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, minY, minZ);

        //back 
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // left side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(maxX, minY, maxZ);

        // right side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(minX, minY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // top 
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, minZ);

        // bottom 
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);
        gl.glVertex3f(minX, minY, minZ);
        gl.glEnd();
        gl.glPopMatrix();

        return objectPosition;
    }

    private float[] DrawBorderL(GL gl) {
        int minX = -12;
        int maxX = -11;
        int maxY = 19;
        int minY = -7;
        int minZ = 0;
        int maxZ = 1;
        float[] objectPosition = {minX - 12, maxX - 12, minY - 5, maxY + -5, minZ, maxZ};
        gl.glPushMatrix();
        gl.glTranslatef(minX, minY, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 0f, 0f);
        // front       
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, minY, minZ);
        //back 
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // left side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(maxX, minY, maxZ);

        // right side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(minX, minY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // top 
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, minZ);

        // bottom 
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);
        gl.glVertex3f(minX, minY, minZ);
        gl.glEnd();
        gl.glPopMatrix();

        return objectPosition;
    }

    private float[] DrawBorderR(GL gl) {
        int minX = 12;
        int maxX = 11;
        int maxY = 20;
        int minY = -7;
        int minZ = 0;
        int maxZ = 1;
        float[] objectPosition = {minX + 11, maxX + 13, minY - 5, maxY + -5, minZ, maxZ};
        gl.glPushMatrix();
        gl.glTranslatef(minX, minY, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 0f, 0f);
        // front       
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, minY, minZ);

        //back 
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // left side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(maxX, minY, maxZ);

        // right side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(minX, minY, minZ);
        gl.glVertex3f(minX, maxY, minZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);

        // top 
        gl.glVertex3f(maxX, maxY, minZ);
        gl.glVertex3f(maxX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, maxZ);
        gl.glVertex3f(minX, maxY, minZ);

        // bottom 
        gl.glVertex3f(maxX, minY, minZ);
        gl.glVertex3f(maxX, minY, maxZ);
        gl.glVertex3f(minX, minY, maxZ);
        gl.glVertex3f(minX, minY, minZ);
        gl.glEnd();
        gl.glPopMatrix();

        return objectPosition;
    }

    private float[] DrawMovingObject(int xPos, int yPos, float speed, float distance, GL gl) {
        float sinWave = (float) Math.sin(Theta * speed);
        float yMover = sinWave * this.yMover * distance;
        float[] objectPosition = {-1 + xPos, 0 + xPos, +yPos + yMover, +5 + yMover + yPos, 0, 1};

        gl.glColor3f(1.0f, 0f, 0f);

        gl.glPushMatrix();
        gl.glTranslatef(xPos, yPos, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 0f, 0f);

        // front       
        gl.glVertex3f(0, 0 + yMover, 0);
        gl.glVertex3f(0, 5 + yMover, 0);
        gl.glVertex3f(- 1, 5 + yMover, 0);
        gl.glVertex3f(- 1, 0 + yMover, 0);

        //back 
        gl.glVertex3f(0, 0 + yMover, 1);
        gl.glVertex3f(0, 5 + yMover, 1);
        gl.glVertex3f(- 1, 5 + yMover, 1);
        gl.glVertex3f(- 1, 0 + yMover, 1);

        // left side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(0, 0 + yMover, 0);
        gl.glVertex3f(0, 5 + yMover, 0);
        gl.glVertex3f(0, 5 + yMover, 1);
        gl.glVertex3f(0, 0 + yMover, 1);

        // right side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(-1, 0 + yMover, 0);
        gl.glVertex3f(-1, 5 + yMover, 0);
        gl.glVertex3f(-1, 5 + yMover, 1);
        gl.glVertex3f(-1, 0 + yMover, 1);

        // top 
        gl.glVertex3f(0, 5 + yMover, 0);
        gl.glVertex3f(0, 5 + yMover, 1);
        gl.glVertex3f(-1, 5 + yMover, 1);
        gl.glVertex3f(-1, 5 + yMover, 0);

        // bottom 
        gl.glVertex3f(0, 0 + yMover, 0);
        gl.glVertex3f(0, 0 + yMover, 1);
        gl.glVertex3f(-1, 0 + yMover, 1);
        gl.glVertex3f(-1, 0 + yMover, 0);
        gl.glEnd();
        gl.glPopMatrix();
        return objectPosition;
    }

    private void DrawPlayer(GL gl) {
        gl.glPushMatrix();
        gl.glTranslated(playerPosition[0] + ((playerPosition[1] - playerPosition[0]) / 2), playerPosition[2] + (playerPosition[3] - playerPosition[2]) / 2, 0);
        gl.glRotated(rotation_angle * 122, 0, 0, 1);
        gl.glTranslated(-(playerPosition[0] + ((playerPosition[1] - playerPosition[0]) / 2)), -(playerPosition[2] + ((playerPosition[3] - playerPosition[2]) / 2)), 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1, 1, 1);

        // FRONT
        gl.glVertex3d(playerPosition[0], playerPosition[2], 0);
        gl.glVertex3d(playerPosition[1], playerPosition[2], 0);
        gl.glVertex3d(playerPosition[1], playerPosition[3], 0);
        gl.glVertex3d(playerPosition[0], playerPosition[3], 0);

        // BACK
        gl.glVertex3f(playerPosition[1], playerPosition[2], 1);
        gl.glVertex3f(playerPosition[1], playerPosition[3], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[3], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[2], 1);

        // RIGHT side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(playerPosition[1], playerPosition[2], 0);
        gl.glVertex3f(playerPosition[1], playerPosition[3], 0);
        gl.glVertex3f(playerPosition[1], playerPosition[3], 1);
        gl.glVertex3f(playerPosition[1], playerPosition[2], 1);

        // LEFT side 
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(playerPosition[0], playerPosition[2], 0);
        gl.glVertex3f(playerPosition[0], playerPosition[3], 0);
        gl.glVertex3f(playerPosition[0], playerPosition[3], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[2], 1);

        // TOP 
        gl.glVertex3f(playerPosition[1], playerPosition[3], 0);
        gl.glVertex3f(playerPosition[1], playerPosition[3], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[3], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[3], 0);

        // BOTTOM
        gl.glVertex3f(playerPosition[1], playerPosition[2], 0);
        gl.glVertex3f(playerPosition[1], playerPosition[2], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[2], 1);
        gl.glVertex3f(playerPosition[0], playerPosition[2], 0);

        gl.glEnd();
        gl.glPopMatrix();
        //gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    private float[] DrawFinishObject(int xPos, int yPos, GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(xPos, yPos, 0);
        float[] objectPosition = {-3 + xPos, 0 + xPos, 2 + yPos, 4 + yPos, 0, 1};
        gl.glBegin(GL.GL_QUADS);
        // front       
        gl.glVertex3f(0, 2, 0);
        gl.glVertex3f(0, 4, 0);
        gl.glVertex3f(-2, 4, 0);
        gl.glVertex3f(-2, 2, 0);

        //back 
        gl.glVertex3f(0, 2, 1);
        gl.glVertex3f(0, 4, 1);
        gl.glVertex3f(-2, 4, 1);
        gl.glVertex3f(-2, 2, 1);

        // right side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(0, 2, 0);
        gl.glVertex3f(0, 4, 0);
        gl.glVertex3f(0, 4, 1);
        gl.glVertex3f(0, 2, 1);

        // left side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(-2, 2, 0);
        gl.glVertex3f(-2, 4, 0);
        gl.glVertex3f(-2, 4, 1);
        gl.glVertex3f(-2, 2, 1);

        // top 
        gl.glVertex3f(0, 4, 0);
        gl.glVertex3f(0, 4, 1);
        gl.glVertex3f(-2, 4, 1);
        gl.glVertex3f(-2, 4, 0);

        // bottom 
        gl.glVertex3f(0, 2, 0);
        gl.glVertex3f(0, 2, 1);
        gl.glVertex3f(-2, 2, 1);
        gl.glVertex3f(-2, 2, 0);
        gl.glEnd();
        gl.glPopMatrix();
        return objectPosition;
    }

    private float[] DrawEnemy(int xPos, int yPos, int index, int speed, float[] currentPosition, GL gl) {
        gl.glPushMatrix();
        gl.glTranslatef(xPos, yPos, 0);
        if (detec[index] == false) {
            if (newPositioner[index] == false) {
                currentPosition[0] = -3 + xPos;
                currentPosition[1] = 0 + xPos;
                currentPosition[2] = 2 + yPos;
                currentPosition[3] = 4 + yPos;
            } 

            gl.glBegin(GL.GL_QUADS);
            gl.glColor3f(102 / 255.0f, 153 / 255.0f, 255 / 255.0f);
             
            //front
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 0);

            //back 
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 1);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 1);

            // right side 
            gl.glRotated(90, 0, 1, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 1);

            // left side
            gl.glRotated(-180, 0, 1, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 1);

            // top 
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 0);

            // bottom 
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 0);
            gl.glEnd();
            gl.glPopMatrix();
            return currentPosition;
        } else {
            int tester = (int) (newPosition[0] * 100);
            int tester2 = (int) (newPosition[2] * 100);
            if (currentPosition[0] != (float) (tester / 100)) {
                if (currentPosition[0] < newPosition[0]) {
                    currentPosition[0] += 0.01f * speed*5;
                    currentPosition[1] += 0.01f * speed*5;
                } else {
                    currentPosition[0] -= 0.01f * speed*5;
                    currentPosition[1] -= 0.01f * speed*5;
                }
            }
            if (currentPosition[2] != (float) (tester2 / 100)) {
                if (currentPosition[2] < newPosition[2]) {
                    currentPosition[2] += 0.01f * speed*5;
                    currentPosition[3] += 0.01f * speed*5;
                } else {
                    currentPosition[2] -= 0.01f * speed*5;
                    currentPosition[3] -= 0.01f * speed*5;
                }
            }
            gl.glBegin(GL.GL_QUADS);
            gl.glColor3f(0.6f * speed, 0f, speed / 3f);

            // front       
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 0);

            //back 
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 1);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 1);

            // right side 
            gl.glRotated(90, 0, 1, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 1);

            // left side
            gl.glRotated(-180, 0, 1, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 1);

            // top 
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[3] - yPos, 0);

            // bottom 
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 0);
            gl.glVertex3f(currentPosition[1] - xPos, currentPosition[2] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 1);
            gl.glVertex3f(currentPosition[0] - xPos, currentPosition[2] - yPos, 0);
            gl.glEnd();
            gl.glPopMatrix();
            return currentPosition;
        }
    }

    private void enemyDetector(int index, float[] hitBoxChecker, GL gl, float[] currentPosition) {
        boolean x1 = (hitBoxChecker[0] - 6) <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1] + 6;
        boolean x2 = (hitBoxChecker[0] - 6) <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1] + 6;
        boolean y1 = (hitBoxChecker[2] - 6) <= playerPosition[2] && playerPosition[2] <= (hitBoxChecker[3] + 6);
        boolean y2 = (hitBoxChecker[2] - 6) <= playerPosition[3] && playerPosition[3] <= (hitBoxChecker[3] + 6);
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        int tester = (int) (newPosition[0] * 100);
        int tester2 = (int) (newPosition[2] * 100);
        int tester3 = (int) (currentPosition[0] * 100);
        int tester4= (int) (currentPosition[2] * 100);
        if ((x1 || x2) && (y1 || y2) && (z1 || z2) && detec[index] == false) // check for x1
        {
            newPosition[0] = playerPosition[0];
            newPosition[1] = playerPosition[1];
            newPosition[2] = playerPosition[2];
            newPosition[3] = playerPosition[3];
            newPositioner[index] = true;
            detec[index] = true;
        } else if (!((x1 || x2) && (y1 || y2) && (z1 || z2)) || (float) (tester3 / 100) == (float) (tester / 100) && (float) (tester4 / 100) == (float) (tester2 / 100)) {
            detec[index] = false;

        }
    }

    private void enemyKiller(float[] hitBoxChecker, GL gl, int index) {
        boolean x1 = hitBoxChecker[0] <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1];
        boolean x2 = hitBoxChecker[0] <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1];
        boolean y1 = hitBoxChecker[2] <= playerPosition[2] && playerPosition[2] <= hitBoxChecker[3];
        boolean y2 = hitBoxChecker[2] <= playerPosition[3] && playerPosition[3] <= hitBoxChecker[3];
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            life--;
            newPositioner[index] = false;
            if (life == 0) {
                System.out.println("You died, RIP");
                System.exit(0);
            }
            System.out.println("You have this many lifes remanining: " + life);
            playerPosition[0] = -16;
            playerPosition[1] = -15;
            playerPosition[2] = 0;
            playerPosition[3] = 1;
            rotation_angle = 0;
        }
    }

    private void onCollisionNext(float[] hitBoxChecker, GL gl) {
        boolean x1 = hitBoxChecker[0] <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1];
        boolean x2 = hitBoxChecker[0] <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1];
        boolean y1 = hitBoxChecker[2] <= playerPosition[2] && playerPosition[2] <= hitBoxChecker[3];
        boolean y2 = hitBoxChecker[2] <= playerPosition[3] && playerPosition[3] <= hitBoxChecker[3];
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            level++;
            playerPosition[0] = -16;
            playerPosition[1] = -15;
            playerPosition[2] = 0;
            playerPosition[3] = 1;
            rotation_angle = 0;
            cam_zoom_IO = 0;
            cam_UD = 0;
            cam_LR = 0;

        }
    }

    private void showLives(GL gl) {
        gl.glColor3f(0, 1, 0);
        gl.glRasterPos3f(-22, 11f, 0);
        String string = String.format("%d /5 lives ", life);
        GLUT glut = new GLUT();
        glut.glutBitmapString(GLUT.BITMAP_8_BY_13, string);

    }

    private void setCamera(GL gl, GLU glu, float distance) {
        // Change to projection matrix.
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        // Perspective.
        float widthHeightRatio = (float) width / (float) height;
        glu.gluPerspective(45, widthHeightRatio, 1, 1000);
        glu.gluLookAt(0, 0, distance, 0, 0, 0, 0, 1, 0);

        // Change back to model view matrix.
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void startUp(GL gl) {
        // set the view
        gl.glMatrixMode(GL.GL_MODELVIEW);
        // Reset the current matrix to the "identity"
        gl.glLoadIdentity();
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        // Prepare light parameters, for the finish object
        float[] lightColorSpecular = {1, 1f, 0f, 1f};
        // Set light parameters.
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightColorSpecular, 0);
        // Enable lighting in GL.
        gl.glEnable(GL.GL_LIGHT1);
        gl.glEnable(GL.GL_LIGHTING);
        // Set material properties.
        float[] rgba = {1f, 1f, 0f};
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 100);
        // Move the "drawing cursor" around
        gl.glTranslatef(cam_LR, cam_UD, -29 + cam_zoom_IO);
        float[] hitBoxChecker;
        hitBoxChecker = DrawFinishObject(13, -3, gl);
        onCollisionNext(hitBoxChecker, gl);
        // disable the light that shines on the finish object.
        gl.glDisable(GL.GL_LIGHT1);
        gl.glDisable(GL.GL_LIGHTING);
        // this is a counter for the fps, you reset the stars position after 10frames
        frames++;
        if (frames % 10 == 0) {
            for (int j = 0; j < 100; j++) {
                stars[0][j] = (Math.random() * 45) - 23;
                stars[1][j] = (Math.random() * 27) - 14;
            }
        }
        for (int i = 0; i < 100; i++) {
            gl.glBegin(GL.GL_POINTS);
            gl.glColor3f(1.0f, 1f, 1f);
            gl.glPointSize(2);
            gl.glVertex3d(stars[0][i], stars[1][i], -1);
            gl.glEnd();
        }
        // draw the borders, and check if the player hit them
        hitBoxChecker = DrawBorderL(gl);
        onCollisionReset(hitBoxChecker);
        hitBoxChecker = DrawBorderR(gl);
        onCollisionReset(hitBoxChecker);
        hitBoxChecker = DrawBorderT(gl);
        onCollisionReset(hitBoxChecker);
        hitBoxChecker = DrawBorderB(gl);
        onCollisionReset(hitBoxChecker);
        DrawPlayer(gl);
        showLives(gl);
        gl.glTranslatef(-23, -12, 0);
        gl.glScaled(7.66666, 1, 1);
        int i = 0;
        while (true) {
            drawSpikes(gl);
            gl.glTranslatef(1, 0, 0);
            i++;
            if (i > 5) {
                break;
            }
        }
        // ignore the trasnlation that happened above
        gl.glLoadIdentity();
        gl.glTranslatef(cam_LR, cam_UD, -29 + cam_zoom_IO);
        // Draw player + reamaining lives
    }

    private void drawSpikes(GL gl) {
        Texture tex;
        //activate texture mapping for 2D
        gl.glEnable(GL.GL_TEXTURE_2D);
        try {
            //load texture
            tex = TextureIO.newTexture(new File("spike.png"), true);
            tex.bind();
        } catch (IOException ex) {
            System.err.println(ex);
        }
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1, 1, 1);
        gl.glTexCoord2d(1, 1);
        gl.glVertex2d(0, 0);
        gl.glTexCoord2d(0, 1);
        gl.glVertex2d(1, 0);
        gl.glTexCoord2d(0, 0);
        gl.glVertex2d(1, 1);
        gl.glTexCoord2d(1, 0);
        gl.glVertex2d(0, 1);
        gl.glTranslatef(2, 0, 0);
        gl.glEnd();
        gl.glDisable(GL.GL_TEXTURE_2D);
    }
    // this one doesn't have a hit box, i don't know why its not working........    
//    private float[] DrawRotatingObject(GL gl) {
//        gl.glPushMatrix();
//        // pos after roration:
//        hitBox[0] = (float) (hitBox[0] * Math.cos(objectRotation) + hitBox[4] * Math.sin(objectRotation));
//        hitBox[1] = (float) (hitBox[1] * Math.cos(objectRotation) + hitBox[5] * Math.sin(objectRotation));
//        hitBox[4] = (float) (hitBox[4] * Math.cos(objectRotation) - hitBox[0] * Math.sin(objectRotation));
//        hitBox[5] = (float) (hitBox[5] * Math.cos(objectRotation) - hitBox[1] * Math.sin(objectRotation));
//
//        gl.glPushMatrix();
//        gl.glTranslatef(hitBox[0] + ((hitBox[1] - hitBox[0]) / 2), hitBox[2] + ((hitBox[3] - hitBox[2]) / 2), 0.0f);
//        gl.glRotatef(objectRotation, 0f, 0f, 1f);
//        gl.glBegin(GL.GL_QUADS);
//        gl.glColor3f(1.0f, 1.0f, 1.0f);
//        gl.glColor3f(1, 0, 0);
//        gl.glVertex3f(0, 0, 0);
//        gl.glVertex3f(0, 5, 0);
//        gl.glVertex3f(- 1, 5, 0);
//        gl.glVertex3f(- 1, 0, 0);
//        gl.glEnd();
//        gl.glPopMatrix();
//        objectRotation += 0.5f;
//        return hitBox;
//    }
}
