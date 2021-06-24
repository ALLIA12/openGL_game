package org.yourorghere;

import com.sun.opengl.util.Animator;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class TempProject implements GLEventListener, KeyListener {

    boolean tutorial = true;
    boolean finishedGame = false;
    boolean gravityToggle = true;
    boolean stop = false;
    boolean isRunning = false;
    int frames = 0; // control stars speed
    int yMover = 2;
    int level = 0;
    int lives = 5;
    int speedModifier = 1; // player speed modifier
    float Theta = 0;// controls object movement 
    float objectRotation = 0;
    float rotation_angle = 0;
    float cam_zoom_IO = 0;
    float cam_LR = 0;
    float cam_UD = 0;
    boolean[] newPositioner = new boolean[3];
    boolean[] isBulletFired = new boolean[500];
    boolean[] detec = new boolean[3]; // detection status by enemies
    float playerPosition[] = {0, 1, 0, 1, 0, 1};
    float bullets[][] = new float[500][6];
    float[] enemyCPosition = {0, 0, 0, 0, 0, 1}; // current enemy position 
    float[] enemyCPosition2 = {0, 0, 0, 0, 0, 1};
    float[] enemyCPosition3 = {0, 0, 0, 0, 0, 1};
    float newPosition[] = {0, 1, 0, 1, 0, 1}; // new enemy positions
    float newPosition2[] = {0, 1, 0, 1, 0, 1};
    float newPosition3[] = {0, 1, 0, 1, 0, 1};
    double stars[][] = new double[2][100];

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
        // reset levels if you go out of range
        if (level > 8) {
            level = 8;
        }
        // if the tuturial is true load it
        if (tutorial) {
            tutorial(gl);
            hitBoxChecker = DrawEnemy(10, 3, 0, 1, enemyCPosition, gl);
            enemyDetector(0, hitBoxChecker, enemyCPosition);
            enemyKiller(hitBoxChecker, 0);
        }
        // load a specific level 
        switch (level) {
            case 1:
                levelOne(gl, hitBoxChecker);
                break;
            case 2:
                levelTwo(gl, hitBoxChecker);
                break;
            case 3:
                levelThree(gl, hitBoxChecker);
                break;
            case 4:
                levelFour(gl, hitBoxChecker);
                break;
            case 5:
                levelFive(gl, hitBoxChecker);
                break;
            case 6:
                levelSix(gl, hitBoxChecker);
                break;
            case 7:
                // this next level is a joke :)
                levelSeven(gl, hitBoxChecker);
                break;
            case 8:
                levelEight(gl);
                break;
            default:
                // how did you get here ?
                // this is when level == 0
                break;
        }
        // Flush all drawing operations to the graphics card
        gl.glFlush();
        // check if gravity is on
        if (gravityToggle) {
            playerPosition[3] -= 0.01f;
            playerPosition[2] -= 0.01f;
        }

    }

    // useless method
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    // when a key is typed
    public void keyTyped(KeyEvent ke) {
    }

    // when a key is pressed
    public void keyPressed(KeyEvent ke) {
        // get the inputs from the user
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                stop = !stop;
                break;
            case KeyEvent.VK_C:
                isRunning = !isRunning;
                break;
            case KeyEvent.VK_D:
                rotation_angle += .5f;
                break;
            case KeyEvent.VK_A:
                rotation_angle -= .5f;
                break;
            case KeyEvent.VK_W:
                playerPosition[0] += 0.1f * speedModifier * Math.sin(rotation_angle);
                playerPosition[1] += 0.1f * speedModifier * Math.sin(rotation_angle);
                playerPosition[3] += 0.1f * speedModifier * Math.cos(rotation_angle);
                playerPosition[2] += 0.1f * speedModifier * Math.cos(rotation_angle);
                break;
            case KeyEvent.VK_S:
                playerPosition[3] -= 0.1f * speedModifier;
                playerPosition[2] -= 0.1f * speedModifier;
                break;
            case KeyEvent.VK_L:
                if (tutorial) {
                    tutorial = false;
                    level++;
                } else {
                    level++;
                }
                playMusic("bleep.wav");
                resetEverything();
                break;
            case KeyEvent.VK_T:
                tutorial = true;
                resetEverything();
                level = 0;
                break;
            case KeyEvent.VK_G:
                gravityToggle = !gravityToggle;
                break;
            case KeyEvent.VK_H:
                lives += 5;
                break;
            case KeyEvent.VK_DOWN:
                cam_UD += 0.1;
                break;
            case KeyEvent.VK_UP:
                cam_UD -= 0.1;
                break;
            case KeyEvent.VK_LEFT:
                cam_LR += 0.1;
                break;
            case KeyEvent.VK_RIGHT:
                cam_LR -= 0.1;
                break;
            case KeyEvent.VK_PAGE_UP:
                cam_zoom_IO += 0.1;
                break;
            case KeyEvent.VK_PAGE_DOWN:
                cam_zoom_IO -= 0.1;
                break;
            case KeyEvent.VK_HOME:
                cam_zoom_IO = 0;
                cam_UD = 0;
                cam_LR = 0;
                break;
            default:
                break;
        }
        // the next one only happens when the player has already beaten the game
        if (finishedGame) {
            switch (ke.getKeyCode()) {
                case KeyEvent.VK_F1:
                    level = 1;
                    resetEverything();
                    break;
                case KeyEvent.VK_F2:
                    level = 2;
                    resetEverything();
                    break;
                case KeyEvent.VK_F3:
                    level = 3;
                    resetEverything();
                    break;
                case KeyEvent.VK_F4:
                    level = 4;
                    resetEverything();
                    break;
                case KeyEvent.VK_F5:
                    level = 5;
                    resetEverything();
                    break;
                case KeyEvent.VK_F6:
                    level = 6;
                    resetEverything();
                    break;
                case KeyEvent.VK_F7:
                    level = 7;
                    resetEverything();
                    break;
                case KeyEvent.VK_F8:
                    level = 8;
                    resetEverything();
                    break;
                default:
                    break;
            }
        }
    }

    // when a key is released
    public void keyReleased(KeyEvent ke) {
    }

    /**
     * This method is used to check if the user hit a specific object, its
     * always used with moving objects and possibly enemies as well.
     *
     * @param hitBoxChecker
     */
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
                    Thread.sleep(5500);
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

    /**
     * this method is used to draw the top border
     *
     * @param gl
     * @return the border hitBox
     */
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

    /**
     * this method is used to draw the bottom border
     *
     * @param gl
     * @return the border hitBox
     */
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

    /**
     * this method is used to draw the left border
     *
     * @param gl
     * @return the border hitBox
     */
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

    /**
     * this method is used to draw the right border
     *
     * @param gl
     * @return the border hitBox
     */
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

    /**
     * this method is used to draw a top -> down moving object
     *
     * @param xPos position on x axis
     * @param yPos position on the y axis
     * @param speed the speed at which the object moves
     * @param distance the distance the object moves
     * @param gl do I need to explain this ?
     * @return hitBox
     */
    private float[] DrawMovingObject(int xPos, int yPos, float speed, float distance, GL gl) {
        float sinWave = (float) Math.sin(Theta * speed);
        float localYmover = sinWave * this.yMover * distance;
        float[] objectPosition = {-1 + xPos, 0 + xPos, +yPos + localYmover, +5 + localYmover + yPos, 0, 1};

        gl.glColor3f(1.0f, 0f, 0f);

        gl.glPushMatrix();
        gl.glTranslatef(xPos, yPos, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(1.0f, 0f, 0f);

        // front       
        gl.glVertex3f(0, 0 + localYmover, 0);
        gl.glVertex3f(0, 5 + localYmover, 0);
        gl.glVertex3f(- 1, 5 + localYmover, 0);
        gl.glVertex3f(- 1, 0 + localYmover, 0);

        //back 
        gl.glVertex3f(0, 0 + localYmover, 1);
        gl.glVertex3f(0, 5 + localYmover, 1);
        gl.glVertex3f(- 1, 5 + localYmover, 1);
        gl.glVertex3f(- 1, 0 + localYmover, 1);

        // left side 
        gl.glRotated(90, 0, 1, 0);
        gl.glVertex3f(0, 0 + localYmover, 0);
        gl.glVertex3f(0, 5 + localYmover, 0);
        gl.glVertex3f(0, 5 + localYmover, 1);
        gl.glVertex3f(0, 0 + localYmover, 1);

        // right side
        gl.glRotated(-180, 0, 1, 0);
        gl.glVertex3f(-1, 0 + localYmover, 0);
        gl.glVertex3f(-1, 5 + localYmover, 0);
        gl.glVertex3f(-1, 5 + localYmover, 1);
        gl.glVertex3f(-1, 0 + localYmover, 1);

        // top 
        gl.glVertex3f(0, 5 + localYmover, 0);
        gl.glVertex3f(0, 5 + localYmover, 1);
        gl.glVertex3f(-1, 5 + localYmover, 1);
        gl.glVertex3f(-1, 5 + localYmover, 0);

        // bottom 
        gl.glVertex3f(0, 0 + localYmover, 0);
        gl.glVertex3f(0, 0 + localYmover, 1);
        gl.glVertex3f(-1, 0 + localYmover, 1);
        gl.glVertex3f(-1, 0 + localYmover, 0);
        gl.glEnd();
        gl.glPopMatrix();
        return objectPosition;
    }

    /**
     * this is used to draw the bullets, they are all spawned in random
     * locations and then they move until they exist line of sight, before
     * reappearing
     *
     * @param speed the bullet speed
     * @param gl drawer object
     * @param index index in the array
     * @return hitBox
     */
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

    /**
     * this method is used to draw the player object
     *
     * @param gl drawer
     */
    private void DrawPlayer(GL gl) {
        gl.glPushMatrix();
        gl.glTranslated(playerPosition[0] + ((playerPosition[1] - playerPosition[0]) / 2), playerPosition[2] + (playerPosition[3] - playerPosition[2]) / 2, 0);
        gl.glRotated(rotation_angle * 122, 0, 0, 1);
        gl.glTranslated(-(playerPosition[0] + ((playerPosition[1] - playerPosition[0]) / 2)), -(playerPosition[2] + ((playerPosition[3] - playerPosition[2]) / 2)), 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3f(0, 1, 0);
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

    /**
     * this method is used to draw the finish object
     *
     * @param xPos position on the x axis
     * @param yPos position on the y axis
     * @param gl drawer
     * @return hitBox
     */
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

    /**
     * this is used to draw the enemy that follows you around
     *
     * @param xPos position on the x axis
     * @param yPos position on the y axis
     * @param index index in the array, {0,1,2}
     * @param speed speed of movement, {0,1,2}
     * @param currentPosition the array of its current position
     * @param gl drawer
     * @return hitBox
     */
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
            int speeder = 1;
            if (speedModifier == 5) {
                speeder = 2;
            }
            if (currentPosition[0] != (float) (tester / 100)) {
                if (currentPosition[0] < newPosition[0]) {
                    currentPosition[0] += 0.01f * speed * 3 * speeder;
                    currentPosition[1] += 0.01f * speed * 3 * speeder;
                } else {
                    currentPosition[0] -= 0.01f * speed * 3 * speeder;
                    currentPosition[1] -= 0.01f * speed * 3 * speeder;
                }
            }
            if (currentPosition[2] != (float) (tester2 / 100)) {
                if (currentPosition[2] < newPosition[2]) {
                    currentPosition[2] += 0.01f * speed * 3 * speeder;
                    currentPosition[3] += 0.01f * speed * 3 * speeder;
                } else {
                    currentPosition[2] -= 0.01f * speed * 3 * speeder;
                    currentPosition[3] -= 0.01f * speed * 3 * speeder;
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

    /**
     * this is for the enemies circumference checker, if you enter said
     * circumference they will start following you around the map, until they
     * lose sight of you, and then they will begin their search all over again
     *
     * @param index index in the array, {0,1,2}
     * @param hitBoxChecker its hitBox
     * @param currentPosition the current position of the enemy
     */
    private void enemyDetector(int index, float[] hitBoxChecker, float[] currentPosition) {
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

    /**
     * This is used to check if you the player hit an enemy and died
     *
     * @param hitBoxChecker the enemy hitBox
     * @param index its index in the array, {0,1,2}
     */
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
                    Thread.sleep(5500);
                } catch (InterruptedException ex) {
                }
                System.exit(0);
            }
            System.out.println("You have this many lifes remanining: " + lives);
            resetEverything();
            float[] temp1 = {0, 0, 0, 0, 0, 1};
            float[] temp2 = {0, 0, 0, 0, 0, 1};
            float[] temp3 = {0, 0, 0, 0, 0, 1};
            this.enemyCPosition = temp1;
            this.enemyCPosition2 = temp2;
            this.enemyCPosition3 = temp3;
        }
    }

    /**
     * This is used to go to the next level
     *
     * @param hitBoxChecker
     */
    private void onCollisionNext(float[] hitBoxChecker) {
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
            resetEverything();
        }
    }

    /**
     * this is used to draw the lives in the top left corner
     *
     * @param gl
     */
    private void showLives(GL gl) {
        gl.glColor3f(0, 1, 0);
        gl.glRasterPos3f(-22, 11f, 0);
        String string = String.format("%d lives ", lives);
        GLUT glut = new GLUT();
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);

    }

    /**
     * this method is called every frame to put the basic stuff
     *
     * @param gl
     */
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
        onCollisionNext(hitBoxChecker);
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
        // show current level
        GLUT glut = new GLUT();
        gl.glColor3f(0, 1, 0);
        gl.glRasterPos3f(19f, 11, 0);
        String string = String.format("Level: %d", level);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);
    }

    /**
     * This is used to draw the most scuffed spikes you could ever find anywhere
     * :)
     *
     * @param gl
     */
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

    /**
     * this is used to check if a bullet enters the player hitBox
     *
     * @param hitBoxChecker bullet hitBox
     */
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
                    Thread.sleep(5500);
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

    /**
     * This is used to draw the tutorial level
     *
     * @param gl
     */
    private void tutorial(GL gl) {
        GLUT glut = new GLUT();
        gl.glColor3f(1, 1, 0);

        gl.glRasterPos3f(8f, -2f, 0);
        String string5 = "Yellow leads to the escape route";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string5);

        gl.glColor3f(1, 1, 1);

        gl.glRasterPos3f(-22f, 9f, 0);
        String string2 = "A/D Rotation";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string2);

        gl.glRasterPos3f(-22f, 8f, 0);
        String string3 = "W Thrust, S descend";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string3);

        gl.glRasterPos3f(-22f, 7f, 0);
        String string4 = "L skip level";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string4);

        gl.glRasterPos3f(-22f, 6f, 0);
        String string7 = "G toggle gravity";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string7);

        gl.glRasterPos3f(-22f, 5f, 0);
        String string8 = "C toggle speed";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string8);

        gl.glRasterPos3f(-22f, 4f, 0);
        String string9 = "H add more lives";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string9);

        gl.glRasterPos3f(-22f, 3f, 0);
        String string10 = "ARROWS for camera control";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string10);

        gl.glColor3f(0, 0, 1);
        gl.glRasterPos3f(4f, 4f, 0);
        String string6 = "Blues are ailen UFOs, Dodge them";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string6);
    }

    /**
     * This si used to draw the last level
     *
     * @param gl
     */
    private void FinishScreen(GL gl) {
        GLUT glut = new GLUT();
        gl.glColor3f(1, 1, 0);
        gl.glRasterPos3f(-10f, 8, 0);
        String string = "Congratulations on escaping... You can try your luck again by using F1-F8";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);
    }

    /**
     * Level 1 texts
     *
     * @param gl
     */
    private void level1Text(GL gl) {
        GLUT glut = new GLUT();
        gl.glColor3f(1, 0, 0);
        gl.glRasterPos3f(-7f, 10f, 0);
        String string = "You only get 5 lives, make them count stranger";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);

        gl.glRasterPos3f(-4f, -9f, 0);
        String string1 = "Dodge the red asteroids";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string1);
    }

    /**
     * This plays the backGround music
     *
     * @param musicLocation the location of the music file you want to use,
     * relative to the project location
     */
    private void playMusic(String musicLocation) {
        try {
            File musicPath = new File(musicLocation);

            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                if (musicLocation.equals("BGMusic.wav")) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            } else {
                System.out.println("can't find");
            }
        } catch (IOException e) {
            System.exit(0);
        } catch (LineUnavailableException e) {
            System.exit(0);
        } catch (UnsupportedAudioFileException e) {
            System.exit(0);
        }
    }

    /**
     * reset everything that might have changed, except lives
     */
    private void resetEverything() {
        playerPosition[0] = -16;
        playerPosition[1] = -15;
        playerPosition[2] = 0;
        playerPosition[3] = 1;
        rotation_angle = 0;
        bullets = new float[500][6];
        isBulletFired = new boolean[500];
        newPositioner[0] = false;
        newPositioner[1] = false;
        newPositioner[2] = false;
    }

    /**
     * Draw the 1st level, one moving object
     *
     * @param gl
     * @param hitBoxChecker this one will be used at every level
     */
    private void levelOne(GL gl, float[] hitBoxChecker) {
        gl.glColor3f(1.0f, 0f, 0f);
        level1Text(gl);
        hitBoxChecker = DrawMovingObject(0, 0, 5, 1, gl);
        onCollisionReset(hitBoxChecker);
    }

    /**
     * Draw the 2nd level, 4 moving objects, 1 enemy
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelTwo(GL gl, float[] hitBoxChecker) {
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
        // draw enemy
        hitBoxChecker = DrawEnemy(-5, -8, 0, 1, enemyCPosition, gl);
        enemyDetector(0, hitBoxChecker, enemyCPosition);
        enemyKiller(hitBoxChecker, 0);
    }

    /**
     * Draw the 3rd level, 30 bullets
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelThree(GL gl, float[] hitBoxChecker) {
        GLUT glut = new GLUT();
        gl.glColor3f(1, 0, 0);
        gl.glRasterPos3f(-13f, 8, 0);
        String string = "GET READY TO DODGE";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);
        for (int i = 0; i < 30; i++) {
            hitBoxChecker = DrawBullet(2, gl, i);
            onCollisionResetBullet(hitBoxChecker);
        }
    }

    /**
     * Draw the 4th level, 2 moving objects, 3 enemies
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelFour(GL gl, float[] hitBoxChecker) {
        // draw moving objects
        hitBoxChecker = DrawMovingObject(-5, 0, 5, 2, gl);
        onCollisionReset(hitBoxChecker);
        // draw moving objects
        hitBoxChecker = DrawMovingObject(10, 0, 10, 2, gl);
        onCollisionReset(hitBoxChecker);
        hitBoxChecker = DrawEnemy(-5, -8, 0, 2, enemyCPosition, gl);
        enemyDetector(0, hitBoxChecker, enemyCPosition);
        enemyKiller(hitBoxChecker, 0);
        hitBoxChecker = DrawEnemy(1, -3, 1, 1, enemyCPosition2, gl);
        enemyDetector(1, hitBoxChecker, enemyCPosition2);
        enemyKiller(hitBoxChecker, 1);
        hitBoxChecker = DrawEnemy(1, 3, 2, 1, enemyCPosition3, gl);
        enemyDetector(2, hitBoxChecker, enemyCPosition3);
        enemyKiller(hitBoxChecker, 2);
    }

    /**
     * Draw the fifth level, 2 enemies, 40 bullets
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelFive(GL gl, float[] hitBoxChecker) {
        for (int i = 0; i < 40; i++) {
            hitBoxChecker = DrawBullet(2, gl, i);
            onCollisionResetBullet(hitBoxChecker);
        }
        hitBoxChecker = DrawEnemy(-5, -8, 0, 1, enemyCPosition, gl);
        enemyDetector(0, hitBoxChecker, enemyCPosition);
        enemyKiller(hitBoxChecker, 0);

        hitBoxChecker = DrawEnemy(0, 8, 1, 1, enemyCPosition2, gl);
        enemyDetector(1, hitBoxChecker, enemyCPosition2);
        enemyKiller(hitBoxChecker, 1);
    }

    /**
     * Draw the 6th level, 3 enemies(2 fast, 1 slow)
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelSix(GL gl, float[] hitBoxChecker) {
        hitBoxChecker = DrawEnemy(-5, -7, 0, 1, enemyCPosition, gl);
        enemyDetector(0, hitBoxChecker, enemyCPosition);
        enemyKiller(hitBoxChecker, 0);
        hitBoxChecker = DrawEnemy(0, 4, 1, 2, enemyCPosition2, gl);
        enemyDetector(1, hitBoxChecker, enemyCPosition2);
        enemyKiller(hitBoxChecker, 1);
        hitBoxChecker = DrawEnemy(10, -2, 2, 2, enemyCPosition3, gl);
        enemyDetector(2, hitBoxChecker, enemyCPosition3);
        enemyKiller(hitBoxChecker, 2);
    }

    /**
     * This level is just a joke of a level, you don't die Draw the 7th level, 5
     * moving objects 3 enemies, 500 bullets
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelSeven(GL gl, float[] hitBoxChecker) {
        // this is a joke of a level, you don't die
        // aka no hit box calling in this level
        DrawMovingObject(-5, 0, 10, 2, gl);
        DrawMovingObject(5, 0, 8, 2, gl);
        DrawMovingObject(0, 0, 5, 4, gl);
        DrawMovingObject(10, 2, 5, 2, gl);
        DrawMovingObject(15, 0, 7, 2, gl);
        for (int i = 0; i < 500; i++) {
            DrawBullet(2, gl, i);
        }
        hitBoxChecker = DrawEnemy(15, 0, 0, 3, enemyCPosition, gl);
        enemyDetector(0, hitBoxChecker, enemyCPosition);
        hitBoxChecker = DrawEnemy(-5, -8, 1, 3, enemyCPosition2, gl);
        enemyDetector(1, hitBoxChecker, enemyCPosition2);
        hitBoxChecker = DrawEnemy(5, 3, 2, 3, enemyCPosition3, gl);
        enemyDetector(2, hitBoxChecker, enemyCPosition3);
    }

    /**
     * this is the final screen
     *
     * @param gl
     * @param hitBoxChecker
     */
    private void levelEight(GL gl) {
        FinishScreen(gl);
        finishedGame = true;
    }

    /**
     * I tried to implement this one, I wanted to make an object that rotates in
     * its current position about the Z axis, but I just couldn't for the what
     * ever reason get it to work fine, so I just kept it as it is, without
     * changing anything, this version is probably bad, because I made like 20
     * of them and I lost most changes T_T
     *
     * @param gl
     * @return
     */
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

}
