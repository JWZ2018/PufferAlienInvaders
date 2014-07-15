//PufferAlien Invaders
//Wuhan Zhou
//Similar to the arcade game Space Invaders. Differs in that it has only one round of many enemies and the goal is to defeat them using the least
//number of bullets. They will move faster and shoot faster as their numbers decrease.
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

public class SpaceInvader extends JFrame implements KeyListener,MouseListener,MouseMotionListener{
	private int rx,vrx;//position and speed of the space ship
	private static boolean[]keys= new boolean[2000];//a list to keep track of which keys are being pressed
	private BufferedImage dbImage;//the buffer image
	private Graphics dbg;
	private Image ship=new ImageIcon("img//rocket.gif").getImage(); //space ship picture
	private Image alien1=new ImageIcon("img//invaders.jpg").getImage(),alien2=new ImageIcon("img//invaders2.jpg").getImage();//aliens pictures
	private Image frontpage=new ImageIcon("img//frontpage.png").getImage();//front page picture
	private Image background=new ImageIcon("img//background.png").getImage();//background picture
	private Image lifepic=new ImageIcon("img//star.gif").getImage();//life symbols picture
	private ArrayList<Integer> bullets;//list to store bullets shot
	private int ready,spritecount;//timer for if ready to shoot another bullet and if should blit skinny or fat alien
	private int ix,iy,vix,viy;//x,y, position of aliens and speed of aliens
	private boolean[][] live;//list to keep track of aliens dead or alive
	private boolean movex,movey;//flags controlling whether aliens moving sideways or downwards
	private ArrayList<Integer> takeout;//list of squares on shield hit by bullets or missiles
	private int countDown,killed;//timer to see if ready to shoot another missile, counter for number of aliens killed
	private ArrayList<Integer> missiles;//list of missiles shot by aliens
	private int life,score,bulletcount,wasted;//counters for life, the score, # of bullets used, and # of bullets wasted
	private boolean startGame,playGame,endGame;//flags for which mode of the game is being run
	private static boolean updateFile=false;//flag to input data into the txt file
	private static ArrayList<String>highscoresnames=new ArrayList<String>();//list for names of players
	private static ArrayList<Integer>highscoresnums=new ArrayList<Integer>();//list for their scores
	private String name;//name being inputed by user at the end
	private static boolean enterName=false;//flag to see if ready to enter the name
	private int left,right,down;//counters to see how many rows and columns are empty on the sides
	private int missileSpeed;//speed of the missiles
    public SpaceInvader() {//constucting the space invaders project
    	super("Space Invader");//caption at top of screen
    	addMouseListener(this);
    	addKeyListener(this);
    	setSize(1200,800);//screen size
    	//Initializing all fields of the object for the purposes stated in the comments above
    	this.bullets=new ArrayList<Integer>();
    	this.missiles=new ArrayList<Integer>();
    	this.rx=6;
    	this.vrx=5;
    	this.ix=150;
    	this.iy=50;
    	this.vix=1;
    	this.viy=5;
    	this.movex=true;
    	this.movey=false;
    	this.takeout=new ArrayList<Integer>();
    	this.live=new boolean[700/44+1][351/35+1];
    	for (int i=0;i<700/44+1;i++){//making an alien at each spot
    		for (int j=0;j<351/35+1;j++){
    			live[i][j]=true;
    		}	
    	}
    
    	this.life=3;
    	this.killed=0;
    	this.score=0;
    	this.ready=0;
    	this.spritecount=0;
    	this.countDown=5;
    	this.bulletcount=0;
    	this.wasted=0;
    	this.missileSpeed=3;
    	this.name="";
    	this.left=0;
    	this.right=0;
    	this.down=0;
    	this.startGame=true;
    	this.playGame=false;
    	this.endGame=false;
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setVisible(true);
    	
    }
    
    public int getGameMode(){//returns the current game mode
    	if (startGame){
    		return 1;
    	}
    	else if(playGame){
    		return 2;
    	}
    	else if(endGame){
    		return 3;
    	}
    	return 4;//in case none are true at any time
    }
    public static void delay(long len){//to prevent screen from flashing
    	try{
    		Thread.sleep(len);
    	}
    	catch(InterruptedException ex){
    		System.out.println(ex);
    	}
    }
    public void paint(Graphics g){
    	if (dbImage==null){//creating new image
    		dbImage=new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_ARGB);
    		dbg=dbImage.getGraphics();
    	}
    	if (startGame){
    		dbg.drawImage(frontpage,0,0,this);
    		if (keys[10]){//start game if enter key is pressed
    			startGame=false;
    			playGame=true;
    		}
    	}
    	
    
		if (playGame){
			if (life==0 || iy>620-35*(11-down)){//if out of lives or aliens are at the door
				playGame=false;//done playing
				endGame=true;//start ending
				enterName=true;//allow user to enter name
			}
			if (killed==11*16){//if killed all enemies
				playGame=false;//done playing
				endGame=true;//start ending
				score+=10000-bulletcount*10+life*1000;//bonus score for lives leftover and least bullets wasted
				enterName=true;//allow user to enter name
			}
		
	    	dbg.setColor(new Color(0,0,0));
	     	dbg.fillRect(0,0,1200,800);//drawing background
	     		
	    	
	    	dbg.drawImage(ship,rx,715,this);//drawing space ship
	    	dbg.setColor(new Color(0,0,255));
	    	spritecount++;//keeping track of the two modes of sprites
	    	if (spritecount==60){
	    		spritecount=0;//resetting
	    	}
	    	for (int i=ix;i<ix+700;i+=44){//drawing aliens
	    		for (int j=iy;j<iy+351;j+=35){
	    			if (live[(i-ix)/44][(j-iy)/35]){//checking if alien at this spot is still alive
	    				if (spritecount>30){//draw skinny alien
	    					dbg.drawImage(alien1,i,j,this);
	    				}
	    				else{//draw fat alien
	    					dbg.drawImage(alien2,i,j,this);
	    				}
	    				
	    			}
	    		}
	    	}
	    	for (int i=0;i<5;i++){//draw shields
	    		dbg.setColor(new Color(0,255,0));
	    		dbg.fillRect(50+i*200,622,100,50);
	    	}
	    	dbg.setColor(new Color(0,0,0));
	    	for(int i=0;i<takeout.size();i+=2){//drawing black squares over places hit by bullets or missiles
	    		dbg.fillRect(takeout.get(i),takeout.get(i+1),10,10);
	    	}
	    	dbg.setColor(new Color(0,0,255));
	    	for (int i=0;i<bullets.size();i+=2){//drawing bullets
	    		dbg.fillRect(bullets.get(i),bullets.get(i+1),3,15);
	    	}
	    	dbg.setColor(new Color(206,54, 234));
	    	for (int i=0;i<missiles.size();i+=2){//drawing missiles
	    		dbg.fillRect(missiles.get(i),missiles.get(i+1),3,15);
	    	}
	    	dbg.setColor(new Color(255,0,0));
	    	dbg.fillRect(1000,0,3,800);//drawing red divider line
	    	for (int i=0;i<life;i++){//drawing life symbols
	    		dbg.drawImage(lifepic,1005+i*50,50,this);
	    	}
	    	dbg.setFont(new Font("Arial",Font.BOLD,60));
	    	dbg.drawString("Stats",1030,200);//stats title
	    	dbg.drawString("Score:",1010,650);//showing score
	    	dbg.drawString(""+score,1010,750);//showing score
	    	dbg.setFont(new Font("Arial",Font.BOLD,20));
	    	dbg.setColor(new Color(0,0,255));
	    	dbg.drawString("Enemies Left:",1030,230);//titles of each stat
	    	dbg.drawString("Bullets Used:",1030,300);
	    	dbg.drawString("Bullets Wasted:",1020,370);
	    	dbg.drawString("Enemy Speed:",1030,440);
	    	dbg.drawString("Missile Speed:",1020,510);
	    	dbg.setColor(new Color(0,255,0));//change color
	    	dbg.drawString(""+(176-killed),1060,265);//drawing the actual numbers of each stat
	    	dbg.drawString(""+bulletcount,1060,335);
	    	dbg.drawString(""+wasted,1060,405);
	    	dbg.drawString(""+Math.abs(vix),1060,475);
	    	dbg.drawString(""+missileSpeed,1060,535);
	    	
		}
		if (endGame){
			dbg.setColor(new Color(255,0,0));
			dbg.drawImage(background,0,0,this);//draw background
			dbg.setColor(new Color(0,255,0));
			dbg.setFont(new Font("Arial",Font.BOLD,60));
			if (enterName){//if in the entering name mode
				dbg.drawString("Enter Your Name:",350,100);//title
				dbg.setColor(new Color(255,255,255));
				dbg.fillRect(325,250,450,100);//white box for name
				dbg.setColor(new Color(0,255,0));
				dbg.drawString(name,350,300);//allows user to type in their name
				dbg.drawString("Enter key to continue",300,500);
				if(keys[10]){//finished entering name
					enterName=false;
					addData();//adding the new data into the list
				}
			}
			else{
				
				for (int i=0;i<8;i++){//showing high scores
					dbg.drawString("#"+(i+1)+". "+highscoresnames.get(i)+":"+highscoresnums.get(i),350,200+i*75);//showing rank, name and score
				}
				dbg.setColor(new Color(255,0,0));
				dbg.drawString("Your Score:   "+score,50,100);//showing user's score
				dbg.setFont(new Font("Arial",Font.BOLD,40));
				dbg.drawString("Space key to play again",650,100);//instructions to continue
				
			}
			
			
		}
    	
    	g.drawImage(dbImage,0,0,this);//drawing onto the screen
    	
    	
    }
    public void quit(){//allows user to exit at any time
    	if (keys[27]){//if hit escape key
    		System.exit(0);
    	}
    }
    public void mousePressed(MouseEvent e){
    	int mx=e.getX();//mouse pos x
    	int my=e.getY();//mouse pos y
    	int mb=e.getButton();
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    
    public void keyTyped(KeyEvent e){}
    public void keyPressed(KeyEvent e){
    	if (enterName){//if typing in name
    		if(e.getKeyCode()==8 && name.length()>0){//if backspace key is hit
    			name=name.substring(0,name.length()-1);
    		}
    		else if(e.getKeyCode()!=10){
    			name+=(char)e.getKeyCode();//adding typed letter into name
    		}
    		
    	}
    	
    	keys[e.getKeyCode()]=true;//setting the hit key to be pressed
    }
    public void keyReleased(KeyEvent e){
    	keys[e.getKeyCode()]=false;
    }
    public void mouseMoved(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    
    public void addData(){//adding current user's data into the list
    	for(int i=0;i<highscoresnums.size();i++){
    		if (score>highscoresnums.get(i)){//looping through to make sure adding to the correct position of highest to lowest
    			highscoresnums.add(i,score);
    			highscoresnames.add(i,name);
    			break;
    		}
    	}
    	updateFile=true;//allows file to update with the new info
    }
    public void moveRocket(){//moving the space ship at the bottom
    	if (keys[37]&& rx>-15){//moving space ship to the left
    		rx-=vrx;
    	}
    	if (keys[39] && rx<916){//moving space ship to the right
    		rx+=vrx;
    	}
    }
    public void addBullet(){//adding another bullet
    	if (keys[32] && ready<=0){//checking if space bar pressed and time interval is up
			bullets.add(rx+42);//adding x-coord of bullet
			bullets.add(700);//adding y-coord of bullet
			bulletcount+=1;//one more bullet used
			ready=15;//resetting time interval before next bullet
    	}
    }
    public void moveBullet(){//moves all the bullets each frame
    	if (ready>0){//
    		ready-=1;//counting down until next bullet
    	}
    	int j=0;//bullet list index
    	while(j<bullets.size()){
    		bullets.set(j+1,bullets.get(j+1)-10);//moving a specific bullet
    		if (bullets.get(j+1)<5){//if bullet is goes out of range, removing the bullet
    			bullets.remove(j);
    			bullets.remove(j);
    			wasted++;//another bullet wasted
    		}
    		else{//increase index to check next bullet
    			j+=2;
    		}
    	}

    }
    public void checkCollision(){//checking if bullets hit the enemies
    	int i=0;//index for bullet list
    	boolean valid=true;//flag for each bullet if hit enemy or not
		while(i<bullets.size()){
			int xspot=bullets.get(i)-ix;//position of bullet, minus the offset
			if(xspot/44>=0 && xspot/44<16){//if bullet within range of enemy columns
				valid=true;//not hit enemy yet
				for (int j=live[xspot/44].length-1;j>=0;j--){//only need to check the column in the path of hte bullet
					if (live[xspot/44][j]&& bullets.get(i+1)<j*35+iy+35){//if enemy at that spot is still alive and bullet is colliding that enemy
						if (spritecount>=30){//for skinny enemies
							if(xspot>(xspot/44)*44+8 && xspot<(xspot/44+1)*44-8){//checking to make sure bullet is not in the gap between 2 enemies
								score+=15;//higher score for skinny enemies
								live[xspot/44][j]=false;//enemy at that spot is dead
								bullets.remove(i);//removing that bullet
								bullets.remove(i);
								killed++;//one more enemy killed
							}
							else{
								i+=2;//if not killed, increase index to check next bullet
							}
							
						}
						else if(spritecount<30){//for fat enemies, no gaps to check
							score+=10;//lower score for fat enemies
							live[xspot/44][j]=false;//enemy at that spot is dead
							bullets.remove(i);//removing used bullet
							bullets.remove(i);
							killed++;
						}
						valid=false;//an enemy was hit
						break;
					}
				}
				if (valid){//increase index
					i+=2;
				}
			}
			else{//increase index
				i+=2;
			}
			
		}
    		
    	
    }
    public void moveAliens(){//moving enemies
    	vix=(killed/60+1)*(vix/Math.abs(vix));//speed increases as number of enemies decrease
    	if (movex){//moving horizontally
    		ix+=vix;
    	}
   		if(movey){//moving vertically
    		iy+=viy;
    		movey=false;
    	}
    	
    	if(ix>1000-(16-right)*44-1 || ix<5-left*44){//changing directions when we reach either end
    		vix*=-1;
    		ix+=vix;
    		movey=true;
    	}
    }
    public void alienShoot(int x,int y){//having aliens shoot
    	missiles.add(x*44+ix+30);//adding to the missiles list
    	missiles.add(y*35+iy+17);
    }
    public void addMissile(){//adding another bullet
    	if (countDown<0 && rx+43>ix && rx+43<ix+44*16){//if ready to shoot and space ship in range for shooting
    	
    		for (int i=live[(rx+43-ix)/44].length-1;i>=0;i--){//using only the column directly above the space ship to shoot
    			if (live[(rx+43-ix)/44][i]){//checking for the lowest guy in that column that is still alive
    				alienShoot((rx+43-ix)/44,i);//making that guy shoot
    				break;
    			}
    			
    		}
    		countDown=50;//resetting time for reloading
    	}	
    	else{
    		countDown-=1;//getting ready to shoot
    	}
    }
    public void moveMissile(){//moving missiles downwards
    	missileSpeed=4+killed/30;//speed changes depending on how many enemies left
    	int j=0;//index for missiles list
    	while (j<missiles.size()){
    		missiles.set(j+1,missiles.get(j+1)+missileSpeed);//set new position of missiles
    		if (missiles.get(j+1)>770){//if missiles out of range, then remove
    			missiles.remove(j);
    			missiles.remove(j);
    		}
    		else{
    			j+=2;//increase index to check next missile
    		}
    	}
    }
    public void hitShield(){// see if bullets or missiles hit the shield
    	int j=0;//index for bullets list
    	while(j<bullets.size()){
    		int c = dbImage.getRGB(bullets.get(j)+1,bullets.get(j+1)-1);//checking the colour at top of bullet to see if it collided a shield
    		int r,g,b;
    		r = (c >> 16) & 0xFF;
    		g = (c >> 8) & 0xFF;
    		b = c & 0xFF;
    		if(r==0 && g==255 && b==0){//shield is green
    			takeout.add(bullets.get(j)-5);//adding the spot in the shield that was
    			takeout.add(bullets.get(j+1)-8);//can cover up that spot with black square to show it was hit
    			bullets.remove(j);//taking out the bullet that was used
    			bullets.remove(j);
    			wasted++;//one more bullet wasted
    			
    		}
    		else{
    			j+=2;//increase index to check next bullet
    		}
    	}
    	j=0;//index for missiles list
    	while(j<missiles.size()){
    		boolean valid=false;//missile did not hit shield initially
    		for (int i=1;i<=10;i++){//checking each of the pixels that the missile travelled in the last frame
    			int c = dbImage.getRGB(missiles.get(j)+1,missiles.get(j+1)+5+i);//getting color to see if it's not black
	    		int r,g,b;
	    		r = (c >> 16) & 0xFF;
	    		g = (c >> 8) & 0xFF;
	    		b = c & 0xFF;
	    		if (r==0 && g==255 && b==0){//shielf is green
	    			takeout.add(missiles.get(j)-5);//covering up the spot that was hit
	    			takeout.add(missiles.get(j+1)+5+i);
	    			missiles.remove(j);//removing the used missile
	    			missiles.remove(j);
	    			valid=true;//shield was hit by missile now
	    			break;
	    		}
    		}
    		
    		if (!(valid)){
    			j+=2;// increase index if not hit to check next missile
    		}
    	}
    }
    public void loseLife(){//to see if missile hit space ship
    	int i=0;//index for missiles list
    	while(i<missiles.size()){
    		if (missiles.get(i)>rx && missiles.get(i)<rx+86){//if missile in range of space ship
    			if (missiles.get(i+1)>723){//if missile low enough to hit space ship
    				life-=1;//lose one life
    				rx=0;//return ship to original position
    				missiles.remove(i);//take out the used missile
    				missiles.remove(i);
    			}
    			else{
    				i+=2;//increase index to check next missile
    			}
    		}
    		else{
    			i+=2;//increase index to check next missile
    		}
    	}
    }
    public void checkBounds(){//checking the boundaries of the enemy troops to see if entire rows or columns have been wiped out
    	boolean valid=false;//assuming bottom row is empty
    	for(int i=0;i<16;i++){//checking each guy in bottom row
    		if (live[i][10-down]){//if a guy is alive in that row
    			valid=true;//that row is not empty
    			break;
    		}
    	}
    	if(!(valid)){//if empty
    		down++;//change bottom row to next highest row
    	}
    	valid=false;//assuming left column is empty
    	for(int i=0;i<11;i++){//checking each guy in left column
    		if(live[left][i]){//if a guy is alive in that column
    			valid=true;//that column is not empty
    			break;
    		}
    	}
    	if (!(valid)){//if empty
    		left++;//change left column to one column to the right
    	}
    	valid=false;//assuming right column is empty
    	for(int i=0;i<11;i++){//checking each guy in right column
    		if(live[15-right][i]){//if a guy is alive in that column
    			valid=true;//that column is not empty
    			break;
    		}
    	}
    	if (!(valid)){//if empty
    		right++;//change right column to one column to the left
    	}
    }
    public static void main(String[]args) throws IOException{//main method
    	
    	SpaceInvader game=new SpaceInvader();//making new game
    	Scanner inFile = new Scanner(new BufferedReader(new FileReader("scores.txt")));//opening the scores file
		while(inFile.hasNextLine()){//while there is a line to read
			String line=inFile.nextLine();//reading a line
			String[] line2=line.split(":");//splitting the line into name and score
			highscoresnames.add(line2[0]);//adding data in list in program
			highscoresnums.add(Integer.parseInt(line2[1]));
		}
    	
    	
    	while(true){
    		
    		if (game.getGameMode()==2){//if playgame mode
    			game.moveRocket();//run all the game functions
    			game.addBullet();
    			game.moveBullet();
	    		game.moveAliens();
	    		game.checkCollision();
	    		game.addMissile();
	    		game.moveMissile();
	    		game.loseLife();
	    		game.hitShield();
	    		game.checkBounds();
    		}
    		
			game.repaint();//updating screen
			game.quit();//seeing if user wishes to exit
    		delay(20);//stopping screen from flashing
    		if(updateFile){//if time to update file
    			PrintWriter outFile = new PrintWriter(new BufferedWriter (new FileWriter ("scores.txt")));//opening the txt file to write to
    			for (int i=0;i<highscoresnums.size();i++){//each set of data
    				String output=highscoresnames.get(i)+":"+highscoresnums.get(i);
    				outFile.println(output);//writing data into file
    			}
    			outFile.close();//closing file
    			updateFile=false;//only update file once
    		}
    		if (game.getGameMode()==3 && !(enterName) && keys[32]){//if player wants to play again, reset everything
    			game=new SpaceInvader();
    			updateFile=false;
    		}
    		
    	}
    	
    	
    }
    
}