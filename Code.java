package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.math.BigInteger;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Main {
    static Main t;
    static Game G;
    static String password = "password";
    public static void main(String[] args){
        t.G = Game.createInstance(password);
        
    }
}
class Game extends JPanel {
    ArrayList<Obstacle> obstacles = new ArrayList();
    final private JFrame frame;
    private int score = 0;
    int ticks = 0;
    Character player;
    final private static String password = "52822410380749795915022741273850040399899648446890016768";
    private boolean passwordCorrect = false;
    public void gameOver(){
        if(passwordCorrect){
            JOptionPane.showMessageDialog(frame, "You lose with a score of "+ score);
            try{
                score = 0;
                Main.G = new Game();
            } catch(Exception e){}
        }
    }
    @Override public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        obstacles.stream().forEach((obstacle) -> {
            obstacle.draw(g2d);
        });
        player.draw(g2d);
    }
    private Game() throws Exception {
        ticks = 0;
        double tickSpeed;
        player = new Character();
        passwordCorrect = true;
        Game g = this;
        obstacles.add(new Obstacle((int) Math.floor(50 + (200 * Math.random()))));
        frame = new JFrame("Game"){{
            setSize(500, 600);
            setFocusable(true);
            setUndecorated(true);
            add(g);
            setVisible(true);
            setDefaultCloseOperation(3);
            addKeyListener(new KeyListener(){
                @Override public void keyPressed(KeyEvent e){
                    player.keyPress(e.getKeyChar());
                }
                @Override public void keyReleased(KeyEvent e){
                    player.keyUp(e.getKeyChar());
                }
                @Override public void keyTyped(KeyEvent e){}
            });
        }};
        long startTime, endTime;
        while(true){
            startTime = System.currentTimeMillis();
            ticks++;
            tickSpeed = 10 / ((double)(score) / 15 + 1);
            repaint();
            revalidate();
            if(ticks % 300 == 0)
                obstacles.add(new Obstacle((int) Math.floor(50 + (200 * Math.random()))));
            if(ticks % 650 == 0){
                obstacles.remove(0);
                score++;
            }
            obstacles.stream().forEach((obstacle) -> {
                obstacle.move();
            });
            player.move(obstacles);
            endTime = System.currentTimeMillis();
            Thread.sleep((long)(tickSpeed - (endTime - startTime)));
        }
    }
    private static String encrypt(String in){
        return new BigInteger(in, 36).pow((int) Math.pow(Math.PI * 10, Math.E)).add(BigInteger.valueOf(Integer.MAX_VALUE)).remainder(new BigInteger("1000000000000", 36)).toString(36);
    }
    public static Game createInstance(String pass) {
        if(new BigInteger(password).mod(new BigInteger(encrypt(pass), 36)).compareTo(BigInteger.ZERO) == 0){
            try {
                return new Game();
            }catch(Exception e){}
        }
        return null;
    }
    private class Obstacle {
        private int gap;
        int direction = gap > 200 ? -1 : 1;
        public int height = -50;
        Polygon side1p1, side1p2, side2p1, side2p2;
        Area collisionArea;
        private void initialize(){
            side1p1 = new Polygon(){{
                addPoint(0, height);
                addPoint(gap, height + 25);
                addPoint(0, height + 25);
            }};
            side1p2 = new Polygon(){{
                addPoint(0, height + 25);
                addPoint(gap, height + 25);
                addPoint(0, height + 50);
            }};
            side2p1 = new Polygon(){{
                addPoint(500, height);
                addPoint(gap + 150, height + 25);
                addPoint(500, height + 25);
            }};
            side2p2 = new Polygon(){{
                addPoint(500, height + 25);
                addPoint(gap + 150, height + 25);
                addPoint(500, height + 50);
            }};
            collisionArea = new Area(){{
                add(new Area(side1p1));
                add(new Area(side1p2));
                add(new Area(side2p1));
                add(new Area(side2p2));
            }};
        }
        public void move(){
            gap += direction;
            height++;
            if(gap < 50 || gap > 250)
                direction *= -1;
            initialize();
        }
        public void draw(Graphics2D g2d){
            g2d.setColor(new Color(0x555555));
            g2d.fill(side1p1);
            g2d.fill(side2p1);
            g2d.setColor(new Color(0xAAAAAA));
            g2d.fill(side1p2);
            g2d.fill(side2p2);
        }
        public Obstacle(int gap){
            this.gap = gap;
            initialize();
        }
    }
    private class Character{
        double ymotion, xmotion;
        int x = 175;
        int y = 400;
        Polygon character;
        Area collisionArea;
        boolean aDown, dDown;
        private void initialize(){
            character = new Polygon(){{
                addPoint(35 + x, y);
                addPoint(65 + x, y);
                addPoint(65 + x, 35 + y);
                addPoint(100 + x, y + 35);
                addPoint(100 + x, 65 + y);
                addPoint(65 + x, 65 + y);
                addPoint(65 + x, y + 100);
                addPoint(35 + x, y + 100);
                addPoint(35 + x, 65 + y);
                addPoint(x, y + 65);
                addPoint(x, 35 + y);
                addPoint(x + 35, y + 35);
            }};
            collisionArea = new Area(character);
        }
        private boolean hitAnything(ArrayList<Obstacle> obstacles){
            for(Obstacle obstacle : obstacles){
                obstacle.collisionArea.intersect(collisionArea);
                if(!obstacle.collisionArea.isEmpty())
                    return true;
            }
            return false;
        }
        public void move(ArrayList<Obstacle> obstacles){
            ymotion += 0.1;
            if(aDown)
                xmotion = (xmotion - 2) / 2;
            if(dDown)
                xmotion = (xmotion + 4) / 2;
            if(!aDown && !dDown);
                xmotion = (xmotion) / 2;
            x += xmotion;
            y += ymotion;
            if(y + 100 > 600 || x > 400 || x < 0 || hitAnything(obstacles))
                gameOver();
            if(y < 0)
                ymotion = Math.abs(ymotion);
            initialize();
        }
        public void draw(Graphics2D g){
            g.setColor(Color.green);
            g.fill(character);

        }
        public void keyPress(char key){
            if(key == 'w' || key == ' ')
                ymotion = -5;
            if(key == 'a')
                aDown = true;
            if(key == 'd')
                dDown = true;
        }
        public void keyUp(char key){
            if(key == 'a')
                aDown = false;
            if(key == 'd')
                dDown = false;
        }
        public Character(){
            initialize();
        }
    }
}
