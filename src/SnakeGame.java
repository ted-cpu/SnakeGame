package src;
import java.awt.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.io.File;

public class SnakeGame extends JPanel implements ActionListener{
	private final int TILE_SIZE = 20;
	private ArrayList<Point> snake;
	private Point food;
	private char direction = 'R';
	private Timer timer;
	private Clip bgmClip;
	private Clip eatClip;
	private int score = 0;
	private int foodX = 10;
	private int foodY = 10;
	private boolean canChangeDirection = true;
	public SnakeGame(){
		
		setPreferredSize(new Dimension(400, 400));
		setBackground(Color.BLACK);
		setFocusable(true); // 讓JPanel接收鍵盤事件
		//音效準備
		prepareSound();
		
		//播放BGM
		playBGM();
		
		//初始化蛇
		snake = new ArrayList<>();
		snake.add(new Point(5, 5));
		
		//初始化食物
		food = new Point(foodX, foodY);
		
		//啟動timer
		timer = new Timer(90, this);
		timer.start();
		
		//鍵盤監聽
		addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				
				if(canChangeDirection){
					switch(e.getKeyCode()){
						case KeyEvent.VK_UP:
							if(direction != 'D')
								direction = 'U';
							break;
						case KeyEvent.VK_DOWN:
							if(direction != 'U')
								direction = 'D';
							break;
						case KeyEvent.VK_LEFT:
							if(direction != 'R')
								direction = 'L';
							break;
						case KeyEvent.VK_RIGHT:
							if(direction != 'L')
								direction = 'R';
							break;
					}
				}
				canChangeDirection = false; //直到下個actionPerfomred被執行之後，才允許更改方向，避免回頭撞自己。
			}
		});
	}
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		//畫食物
		g.setColor(Color.RED);
		g.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		
		//畫蛇
		g.setColor(Color.WHITE);
		g.fillRect(snake.get(0).x * TILE_SIZE, snake.get(0).y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		g.setColor(Color.GREEN);
		for(int i = 1;i < snake.size();i++){
			g.fillRect(snake.get(i).x * TILE_SIZE, snake.get(i).y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		}
		
		//繪製分數文字
		g.setColor(Color.WHITE); 
		g.setFont(new Font("Arial", Font.BOLD, 16)); 
		
		g.drawString("Score: " + score, 10, 25);
		
		}
	
	@Override 
	public void actionPerformed(ActionEvent e){
		
		repaint();
		
		//更新座標
		Point head = snake.get(0);
		Point newHead = new Point(head.x, head.y);
		
		if(direction == 'U') newHead.y--;
		if(direction == 'D') newHead.y++;
		if(direction == 'R') newHead.x++;
		if(direction == 'L') newHead.x--;
		
		snake.add(0, newHead);
		
		//食物判斷
        if (newHead.equals(food)){
			playEatSound();
			score++;
			
			createFood();
        }else{
            snake.remove(snake.size() - 1);
        }
		
		//碰撞檢測
		//1. 是否碰到牆壁
		if(newHead.x < 0 || newHead.x >= 20 || newHead.y < 0 || newHead.y >= 20){
			gameOver();
			return;
		}
		//2. 是否碰動自己身體
		for(int i = 1;i < snake.size();i++){
			Point body = snake.get(i);
			if(newHead.equals(body)){
				gameOver();
				return;
			}
		}
		
		canChangeDirection = true; //當資訊更新完，開放方向變更
	}
	private void gameOver(){
		timer.stop();
		bgmClip.stop();
		JOptionPane.showMessageDialog(this, "Game Over! Your score: " + (snake.size() - 1),
									  "Game over", JOptionPane.INFORMATION_MESSAGE);
									  
		createNewGame();
	}
	private void prepareSound(){
		try{
			//背景音樂
			File soundFile = new File("res/bgm.wav");
			AudioInputStream bgmAis = AudioSystem.getAudioInputStream(soundFile);
			
			bgmClip = AudioSystem.getClip();
			bgmClip.open(bgmAis);
			
			//吃食物音效
			soundFile = new File("res/eat.wav");
			AudioInputStream eatAis = AudioSystem.getAudioInputStream(soundFile);
			
			eatClip = AudioSystem.getClip();
			eatClip.open(eatAis);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void playBGM(){
		if (bgmClip != null){
			bgmClip.loop(Clip.LOOP_CONTINUOUSLY); 
			bgmClip.start();
		}
	}
	private void playEatSound(){
		if(eatClip != null){
			if(eatClip.isRunning()){
				eatClip.stop();
			}
			eatClip.setFramePosition(0);
			eatClip.start();
		}
	}
	private void createFood(){
		
		boolean onSnake = true;
		while(onSnake){
			
			foodX = (int)(Math.random()*20);
			foodY = (int)(Math.random()*20);
			
			food = new Point(foodX, foodY);
			
			onSnake = false;
			for(Point p : snake){
				if(p.equals(food)){
					onSnake = true;
					break;
				}
			}
		}
	}
	
	private void createNewGame(){
		
		score = 0;
		direction = 'R';
		canChangeDirection = true;
		
		snake.clear();
		snake.add(new Point(5, 5));
		
		createFood();
		
		if (bgmClip != null) {
			bgmClip.setFramePosition(0);
			bgmClip.start();
			bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
		}
		timer.start();
		
		repaint();
	}
	
	public static void main(String[] args){
		
		UIManager.put("Button.focus", new Color(0, 0, 0, 0));
		
		JFrame frame = new JFrame("Java 貪食蛇");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
        frame.setVisible(true);
		
		game.requestFocusInWindow();
	}
}