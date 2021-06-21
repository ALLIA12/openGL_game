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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class TempProject implements GLEventListener, KeyListener {

    int frames = 0;
    float Theta = 0;
    int yMover = 2;
    boolean stop = false;
    boolean isRunning = false;
    int level = 0;
    int lives = 5;
    float objectRotation = 0;
    float rotation_angle = 0;
    float cam_zoom_IO = 0;
    float cam_LR = 0;
    float cam_UD = 0;
    float rotate_LR = 0;// not used
    float rotate_UD = 0;// not used
    int speedModifier = 1;
    float playerPosition[] = {0, 1, 0, 1, 0, 1};
    float bullets[][] = new float[200][6];
    boolean[] isBulletFired = new boolean[200];
    float[] enemyCPosition = {0, 0, 0, 0, 0, 1};
    float[] enemyCPosition2 = {0, 0, 0, 0, 0, 1};
    float[] enemyCPosition3 = {0, 0, 0, 0, 0, 1};
    boolean[] detec = new boolean[3];
    float newPosition[] = {0, 1, 0, 1, 0, 1};
    float newPosition2[] = {0, 1, 0, 1, 0, 1};
    float newPosition3[] = {0, 1, 0, 1, 0, 1};
    double stars[][] = new double[2][100];
    boolean[] newPositioner = new boolean[3];
    GLU glu;// not used
    int width = 1280;// not used
    int height = 720;// not used
    boolean tutorial = true;

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
        playMusic("BGMusic.wav");
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
        this.width = width;
        this.height = height;
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        startUp(gl);
        //this will be used as the refrence to all  hit boxes
        float[] hitBoxChecker = new float[6];
        if (stop) {
            return;
        }
        if (tutorial) {
            tutorial(gl);
            hitBoxChecker = DrawEnemy(10, 3, 0, 1, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, gl, enemyCPosition);
            enemyKiller(hitBoxChecker, 0);
            lives = 5;
        } else if (level == 1) {
            gl.glColor3f(1.0f, 0f, 0f);

            level1Text(gl);
            hitBoxChecker = DrawMovingObject(0, 0, 5, 1, gl);
            onCollisionReset(hitBoxChecker);

        } else if (level == 2) {
            gl.glColor3f(1.0f, 0f, 0f);

            // draw moving objects 
            hitBoxChecker = DrawMovingObject(-5, 0, 2, 2, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(0, 0, 5, 3, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(+5, 0, 6, 5, gl);
            onCollisionReset(hitBoxChecker);
            hitBoxChecker = DrawMovingObject(9, 0, 2, 2, gl);
            onCollisionReset(hitBoxChecker);
            newPositioner[0] = false;

            // draw enemy
            hitBoxChecker = DrawEnemy(-16, -8, 0, 1, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, gl, enemyCPosition);
            enemyKiller(hitBoxChecker, 0);
            newPositioner[1] = false;

        } else if (level == 3) {
            for (int i = 0; i < 20; i++) {
                hitBoxChecker = DrawBullet(2, gl, i);
                onCollisionResetBullet(hitBoxChecker);
            }
        } else if (level == 4) {
            // draw moving objects 
            hitBoxChecker = DrawMovingObject(-5, 0, 5, 2, gl);
            onCollisionReset(hitBoxChecker);

            // draw moving objects 
            hitBoxChecker = DrawMovingObject(10, 0, 10, 2, gl);
            onCollisionReset(hitBoxChecker);

            newPositioner[0] = false;
            hitBoxChecker = DrawEnemy(-16, -8, 0, 2, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, gl, enemyCPosition);
            enemyKiller(hitBoxChecker, 0);

            newPositioner[1] = false;
            hitBoxChecker = DrawEnemy(1, -3, 1, 2, enemyCPosition2, gl);
            enemyDetector(1, hitBoxChecker, gl, enemyCPosition2);
            enemyKiller(hitBoxChecker, 1);

        } else if (level == 5) {
            for (int i = 0; i < 50; i++) {
                hitBoxChecker = DrawBullet(2, gl, i);
                onCollisionResetBullet(hitBoxChecker);
            }
            newPositioner[0] = false;
            hitBoxChecker = DrawEnemy(-16, -8, 0, 1, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, gl, enemyCPosition);
            enemyKiller(hitBoxChecker, 0);

        } else if (level == 6) {
            
            // this is a joke of a level, you don't die
            hitBoxChecker = DrawMovingObject(-5, 0, 10, 2, gl);

            hitBoxChecker = DrawMovingObject(5, 0, 8, 2, gl);

            hitBoxChecker = DrawMovingObject(0, 0, 5, 4, gl);

            hitBoxChecker = DrawMovingObject(10, 2, 5, 2, gl);

            hitBoxChecker = DrawMovingObject(15, 0, 7, 2, gl);

            for (int i = 0; i < 200; i++) {
                hitBoxChecker = DrawBullet(2, gl, i);
            }

            newPositioner[0] = false;
            hitBoxChecker = DrawEnemy(15, 0, 0, 2, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, gl, enemyCPosition);
            //enemyKiller(hitBoxChecker, 0);

            newPositioner[1] = false;
            hitBoxChecker = DrawEnemy(-16, -8, 1, 2, enemyCPosition2, gl);
            enemyDetector(1, hitBoxChecker, gl, enemyCPosition2);

            newPositioner[2] = false;
            hitBoxChecker = DrawEnemy(5, 3, 2, 3, enemyCPosition3, gl);
            enemyDetector(2, hitBoxChecker, gl, enemyCPosition3);
        } else if (level == 7) {
            level = 1;
        }

        // Flush all drawing operations to the graphics card
        gl.glFlush();
        // gravity 
        //playerPosition[3] -= 0.01f;
        //playerPosition[2] -= 0.01f;
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    // when a key is pressed
    public void keyTyped(KeyEvent key) {
        char keyChar = key.getKeyChar();
        if (keyChar == KeyEvent.VK_C || keyChar == 'c') {
            isRunning = !isRunning;
        } else if (keyChar == KeyEvent.VK_D || keyChar
                == 'd') {
            rotation_angle += .5f;

        } else if (keyChar == KeyEvent.VK_A || keyChar
                == 'a') {
            rotation_angle -= .5f;

        } else if (keyChar == KeyEvent.VK_W || keyChar
                == 'w') {
            playerPosition[0] += 0.1f * speedModifier * Math.sin(rotation_angle);
            playerPosition[1] += 0.1f * speedModifier * Math.sin(rotation_angle);
            playerPosition[3] += 0.1f * speedModifier * Math.cos(rotation_angle);
            playerPosition[2] += 0.1f * speedModifier * Math.cos(rotation_angle);
        } else if (keyChar == KeyEvent.VK_S || keyChar
                == 's') {
            playerPosition[3] -= 0.1f * speedModifier;
            playerPosition[2] -= 0.1f * speedModifier;
        } else if (keyChar == KeyEvent.VK_L || keyChar
                == 'l') {
            if (tutorial) {    //
                tutorial = false;
                level++;
            } else {
                level++;
            }
            playMusic("bleep.wav");
            resetEverything();
        } else if (keyChar == KeyEvent.VK_T || keyChar
                == 't') {
            tutorial = true;
            playerPosition[0] = -18;
            playerPosition[1] = -17;
            playerPosition[2] = 0;
            playerPosition[3] = 1;
        } else if (keyChar
                == '2') {
            cam_UD++;
        } else if (keyChar
                == '8') {
            cam_UD--;
        } else if (keyChar
                == '4') {
            cam_LR++;
        } else if (keyChar
                == '6') {
            cam_LR--;
        } else if (keyChar
                == '9') {
            cam_zoom_IO++;
        } else if (keyChar
                == '7') {
            cam_zoom_IO--;
        } else if (keyChar
                == '5') {
            cam_zoom_IO = 0;
            cam_UD = 0;
            cam_LR = 0;
            System.out.println("RESET ALL");
        }
    }

// when a key is pressed
    public void keyPressed(KeyEvent ke) {
        char keyChar = ke.getKeyChar();
        if (keyChar == KeyEvent.VK_SPACE) {
            stop = !stop;
        }
    }

    // when a key is released
    public void keyReleased(KeyEvent ke) {
        if (ke.isShiftDown()) {
            speedModifier = 5;
        } else {
            speedModifier = 1;
        }

    }

    // when hit the player gets resset to the original point
    private void onCollisionReset(float[] hitBoxChecker) {
        boolean x1 = hitBoxChecker[0] <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1];
        boolean x2 = hitBoxChecker[0] <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1];
        boolean y1 = hitBoxChecker[2] <= playerPosition[2] && playerPosition[2] <= hitBoxChecker[3];
        boolean y2 = hitBoxChecker[2] <= playerPosition[3] && playerPosition[3] <= hitBoxChecker[3];
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            lives--;
            if (lives == 0) {
                System.out.println("You died, RIP");
                playMusic("Explosion.wav");
                try {
                    Thread.sleep(5200);
                } catch (InterruptedException ex) {
                }
                System.exit(0);
            } else {
                playMusic("bleep.wav");
            }
            System.out.println("You have this many lifes remanining: " + lives);
            resetEverything();
        }
    }

    // draw top border
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

    // draw bottom border
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

    // draw left border
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

    // draw right border
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

    // this is used to draw an object that moves up and down
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

    // this is used to darw the bullets, they are all spawned in random locations
    // and then they move until they exist line of sight, before respwaning
    private float[] DrawBullet(int speed, GL gl, int index) {
        if (!isBulletFired[index]) {
            bullets[index][0] = (float) ((Math.random() * 20) + 30);
            bullets[index][1] = bullets[index][0] + 1;
            bullets[index][2] = (float) ((Math.random() * 21) - 10);
            bullets[index][3] = bullets[index][2] + 0.25f;
            bullets[index][4] = 0;
            bullets[index][5] = 1;
            isBulletFired[index] = true;
        } else {
            bullets[index][0] -= 0.1f * speed;
            bullets[index][1] -= 0.1f * speed;
            if (bullets[index][0] < -23) {
                isBulletFired[index] = false;
            }
        }
        float x[] = bullets[index];
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1, 0, 0);
        gl.glVertex3f(x[0], x[2], 0);
        gl.glVertex3f(x[1], x[2], 0);
        gl.glVertex3f(x[1], x[3], 0);
        gl.glVertex3f(x[0], x[3], 0);
        gl.glEnd();
        return x;
    }

    // this is used to draw the player character
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

    // this is used to draw the finish object, when you enter it, you skip to 
    // the next level
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

    // this is used to draw the enemey that follows you
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
                    currentPosition[0] += 0.01f * speed * 3;
                    currentPosition[1] += 0.01f * speed * 3;
                } else {
                    currentPosition[0] -= 0.01f * speed * 3;
                    currentPosition[1] -= 0.01f * speed * 3;
                }
            }
            if (currentPosition[2] != (float) (tester2 / 100)) {
                if (currentPosition[2] < newPosition[2]) {
                    currentPosition[2] += 0.01f * speed * 3;
                    currentPosition[3] += 0.01f * speed * 3;
                } else {
                    currentPosition[2] -= 0.01f * speed * 3;
                    currentPosition[3] -= 0.01f * speed * 3;
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

    // this is for the enemies radius checker, if you enter said radius
    // they will start following you around the map, until they lose sight of you,
    // and then they will begin their search all over again
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
        int tester4 = (int) (currentPosition[2] * 100);
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

    // this is the hitbox checker for enemies that follow you
    private void enemyKiller(float[] hitBoxChecker, int index) {
        boolean x1 = hitBoxChecker[0] <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1];
        boolean x2 = hitBoxChecker[0] <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1];
        boolean y1 = hitBoxChecker[2] <= playerPosition[2] && playerPosition[2] <= hitBoxChecker[3];
        boolean y2 = hitBoxChecker[2] <= playerPosition[3] && playerPosition[3] <= hitBoxChecker[3];
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            newPositioner[index] = false;
            lives--;
            if (lives == 0) {
                System.out.println("You died, RIP");
                playMusic("Explosion.wav");
                try {
                    Thread.sleep(5200);
                } catch (InterruptedException ex) {
                }
                System.exit(0);
            }
            System.out.println("You have this many lifes remanining: " + lives);
            resetEverything();
            float[] enemyCPosition = {0, 0, 0, 0, 0, 1};
            float[] enemyCPosition2 = {0, 0, 0, 0, 0, 1};
            float[] enemyCPosition3 = {0, 0, 0, 0, 0, 1};
            this.enemyCPosition = enemyCPosition;
            this.enemyCPosition2 = enemyCPosition2;
            this.enemyCPosition3 = enemyCPosition3;
        }
    }

    // this is the normal hitbox checker for the finish object
    private void onCollisionNext(float[] hitBoxChecker, GL gl) {
        boolean x1 = hitBoxChecker[0] <= playerPosition[0] && playerPosition[0] <= hitBoxChecker[1];
        boolean x2 = hitBoxChecker[0] <= playerPosition[1] && playerPosition[1] <= hitBoxChecker[1];
        boolean y1 = hitBoxChecker[2] <= playerPosition[2] && playerPosition[2] <= hitBoxChecker[3];
        boolean y2 = hitBoxChecker[2] <= playerPosition[3] && playerPosition[3] <= hitBoxChecker[3];
        boolean z1 = hitBoxChecker[4] <= playerPosition[4] && playerPosition[4] <= hitBoxChecker[4];
        boolean z2 = hitBoxChecker[5] <= playerPosition[5] && playerPosition[5] <= hitBoxChecker[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            if (tutorial) {  // 
                tutorial = false;
                level++;
            } else {
                level++;
            }
            playMusic("bleep.wav");
            playerPosition[0] = -16;
            playerPosition[1] = -15;
            playerPosition[2] = 0;
            playerPosition[3] = 1;
            rotation_angle = 0;
        }
    }

    // this just shows how many lives the player has left
    private void showLives(GL gl) {
        gl.glColor3f(0, 1, 0);
        gl.glRasterPos3f(-22, 11f, 0);
        String string = String.format("%d /5 lives ", lives);
        GLUT glut = new GLUT();
        glut.glutBitmapString(GLUT.BITMAP_8_BY_13, string);

    }

    // this method is called every time display gets called, it has the shared
    // objects drawn in it
    private void startUp(GL gl) {
        // set the view
        // setCamera(gl, glu); not used
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
        // draw the spikes
        gl.glTranslatef(-23, -12, 0);
        gl.glScaled(7.66666, 1, 1);
        gl.glTexParameteri(height, frames, frames);
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
        DrawPlayer(gl);
        showLives(gl);
        // add to the movement angle, of objects
        Theta = (float) ((Theta + 0.01));
        if (isRunning) {
            speedModifier = 5;
        } else {
            speedModifier = 1;
        }
    }

    // draw the worst spikes you have ever seen
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

    // this is hitbox checker for bullets
    private void onCollisionResetBullet(float[] hitBoxChecker) {
        boolean x1 = playerPosition[0] <= hitBoxChecker[0] && hitBoxChecker[0] <= playerPosition[1];
        boolean x2 = playerPosition[0] <= hitBoxChecker[1] && hitBoxChecker[1] <= playerPosition[1];
        boolean y1 = playerPosition[2] <= hitBoxChecker[2] && hitBoxChecker[2] <= playerPosition[3];
        boolean y2 = playerPosition[2] <= hitBoxChecker[3] && hitBoxChecker[3] <= playerPosition[3];
        boolean z1 = playerPosition[4] <= hitBoxChecker[4] && hitBoxChecker[4] <= playerPosition[4];
        boolean z2 = playerPosition[5] <= hitBoxChecker[5] && hitBoxChecker[5] <= playerPosition[5];
        if ((x1 || x2) && (y1 || y2) && (z1 || z2)) // check for x1
        {
            lives--;
            if (lives == 0) {
                System.out.println("You died, RIP");
                playMusic("Explosion.wav");
                try {
                    Thread.sleep(5200);
                } catch (InterruptedException ex) {
                }
                System.exit(0);
            }
            System.out.println("You have this many lifes remanining: " + lives);
            resetEverything();

        }
    }

    // this displays the text on the tutorial
    private void tutorial(GL gl) {
        GLUT glut = new GLUT();
        gl.glColor3f(1, 1, 1);
        gl.glRasterPos3f(-7f, 10f, 0);
        String string = "This is the tutorial, please learn how to play here";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);

        gl.glRasterPos3f(-7f, 9f, 0);
        String string5 = "Go to the Yellow square to win!!";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string5);

        gl.glRasterPos3f(-16f, -5f, 0);
        String string2 = "press D to rotate downward                                                                                 press A to rotate upward";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string2);

        gl.glRasterPos3f(-16f, -7f, 0);
        String string3 = "press W to move forward                      press C to toggle speed mode            press S to move downward";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string3);

        gl.glRasterPos3f(-16f, -9f, 0);
        String string4 = "press L to go to next level                                                                             press T to enter this tutorial map again";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string4);

        gl.glRasterPos3f(4f, 4f, 0);
        String string6 = "Blue cubes are enemies!! don't let them catch you";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string6);

    }

    // this displays the text on level 1
    private void level1Text(GL gl) {
        GLUT glut = new GLUT();
        gl.glColor3f(1, 1, 1);
        gl.glRasterPos3f(-7f, 10f, 0);
        String string = "You only have 5 lives!! Be aware to not run out of lives";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);

        gl.glRasterPos3f(-3f, -9f, 0);
        String string1 = "Dodge the red objects";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string1);
    }

    // this plays the back ground music
    private void playMusic(String musicLocation) {
        try {
            File musicPath = new File(musicLocation);

            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                if (musicLocation.equals("BGMusic.wav")) {
                    clip.loop(clip.LOOP_CONTINUOUSLY);
                }

            } else {
                System.out.println("can't find");
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    // reset everything that might have changec, except lives
    private void resetEverything() {
        playerPosition[0] = -16;
        playerPosition[1] = -15;
        playerPosition[2] = 0;
        playerPosition[3] = 1;
        rotation_angle = 0;
        bullets = new float[200][6];
        isBulletFired = new boolean[200];

    }

    // this one doesn't have a hit box, i don't know why its not working........    
    // so I just skipped it
    private float[] DrawRotatingObject(GL gl) {
        float[] hitBox = new float[6];
        gl.glPushMatrix();
        // pos after roration:
        hitBox[0] = (float) (hitBox[0] * Math.cos(objectRotation) + hitBox[4] * Math.sin(objectRotation));
        hitBox[1] = (float) (hitBox[1] * Math.cos(objectRotation) + hitBox[5] * Math.sin(objectRotation));
        hitBox[4] = (float) (hitBox[4] * Math.cos(objectRotation) - hitBox[0] * Math.sin(objectRotation));
        hitBox[5] = (float) (hitBox[5] * Math.cos(objectRotation) - hitBox[1] * Math.sin(objectRotation));

        gl.glPushMatrix();
        gl.glTranslatef(hitBox[0] + ((hitBox[1] - hitBox[0]) / 2), hitBox[2] + ((hitBox[3] - hitBox[2]) / 2), 0.0f);
        gl.glRotatef(objectRotation, 0f, 0f, 1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glColor3f(1, 0, 0);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(0, 5, 0);
        gl.glVertex3f(- 1, 5, 0);
        gl.glVertex3f(- 1, 0, 0);
        gl.glEnd();
        gl.glPopMatrix();
        objectRotation += 0.5f;
        return hitBox;
    }

    // not used, aka its useless
    private void setCamera(GL gl, GLU glu) {
        //System.out.println(x + " " + y + " " + width + " " + height);
        glu.gluLookAt(0, rotate_UD, rotate_LR, 0, 0, 0, 0, 0, 0);
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

}
