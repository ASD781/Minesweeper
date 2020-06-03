import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;

public class MineSweeper extends JPanel implements ActionListener, MouseListener {

	JFrame frame;
	JMenuBar menuBar;
	JMenu menu, ctrlMenu, iconMenu;
	JMenuItem[] difItems, set;
	JMenuItem rightClick, leftClick, faceClick;
	JToggleButton[][] toggles;
	JPanel togglePanel;
	JButton reset;
	JLabel time, minesLeft;


	Timer timer;
	int timePassed, numMinesLeft;

	int iconSet= 0;
	ImageIcon[][] icons= new ImageIcon[][]{new ImageIcon[]	{new ImageIcon("images/msEmpty.png"),new ImageIcon("images/ms1.png"),new ImageIcon("images/ms2.png"),new ImageIcon("images/ms3.png"),new ImageIcon("images/ms4.png"),
															new ImageIcon("images/ms5.png"),new ImageIcon("images/ms6.png"),new ImageIcon("images/ms7.png"),new ImageIcon("images/ms8.png"),
															new ImageIcon("images/mine.jpg"),new ImageIcon("images/flag.png")},
											new ImageIcon[]{new ImageIcon("images/num0.png"),new ImageIcon("images/num1.png"),new ImageIcon("images/num2.png"),new ImageIcon("images/num3.png"),new ImageIcon("images/num4.png"),
															new ImageIcon("images/num5.png"),new ImageIcon("images/num6.png"),new ImageIcon("images/num7.png"),new ImageIcon("images/num8.png"),
															new ImageIcon("images/mine2.png"),new ImageIcon("images/flag2.png")},
											new ImageIcon[]{new ImageIcon("images/box0.png"),new ImageIcon("images/box1.png"),new ImageIcon("images/box2.png"),new ImageIcon("images/box3.png"),new ImageIcon("images/box4.png"),
															new ImageIcon("images/box5.png"),new ImageIcon("images/box6.png"),new ImageIcon("images/box7.png"),new ImageIcon("images/box8.png"),
															new ImageIcon("images/mine3.png"),new ImageIcon("images/flag3.png")}};

	ImageIcon[][] resetIcons= new ImageIcon[][] {new ImageIcon[] {new ImageIcon("images/default.png"),new ImageIcon("images/click.png"),new ImageIcon("images/lose.png"),new ImageIcon("images/win.png")},
												new ImageIcon[] {new ImageIcon("images/default2.png"),new ImageIcon("images/click2.png"),new ImageIcon("images/lose2.png"),new ImageIcon("images/win2.png")},
												new ImageIcon[] {new ImageIcon("images/default3.png"),new ImageIcon("images/click3.png"),new ImageIcon("images/lose3.png"),new ImageIcon("images/win3.png")}};


	int[] dimArr= new int[] {8,16,16,8,16,30};
	int[] mineArr= new int[] {10,40,99};

	int dimR, dimC, numMines;
	char[][] grid= new char[dimR][dimC];
	boolean[][] checkSelected, checkFlagged;
	String difficulty;
	boolean firstClick, gameStopped;
	int numFlags;

	public MineSweeper() {

		frame= new JFrame("MineSweeper");
		frame.add(this);
		frame.setSize(1000,800);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			frame.setIconImage(ImageIO.read(new File("images/mine.jpg")));
		}
		catch (Exception e) {
			System.out.println(e.getStackTrace());
		}

		for (int i=0; i<resetIcons.length; i++)
			for (int j=0; j<resetIcons[i].length; j++)
				resetIcons[i][j]= new ImageIcon(resetIcons[i][j].getImage().getScaledInstance(50,50,Image.SCALE_SMOOTH));
		reset= new JButton();
		reset.addActionListener(this);

		dimR= 9;
		dimC= 9;
		numMines= 10;

		time= new JLabel(timePassed+"",JLabel.CENTER);
		time.setFont(time.getFont().deriveFont(1000));

		minesLeft= new JLabel(numMinesLeft+"",JLabel.CENTER);
		minesLeft.setFont(minesLeft.getFont().deriveFont(1000));

		setGrid();
		setMines();
		printGrid();

		menuBar= new JMenuBar();

		menu= new JMenu("Game");
		difItems= new JMenuItem[] {new JMenuItem("Easy"),new JMenuItem("Medium"),new JMenuItem("Hard")};
		for (int i=0; i<difItems.length; i++) {
			difItems[i].addActionListener(this);
			menu.add(difItems[i]);
		}

		iconMenu= new JMenu("Icons");
		set= new JMenuItem [3];
		for (int i=0; i<set.length; i++) {
			set[i]= new JMenuItem("Set "+i);
			set[i].addActionListener(this);
			iconMenu.add(set[i]);
		}

		ctrlMenu= new JMenu("Controls");
		rightClick= new JMenuItem("Right-click to flag a space");
		leftClick= new JMenuItem("Left-click to uncover a space");
		faceClick= new JMenuItem("Left-click the face to reset");
		ctrlMenu.add(rightClick);
		ctrlMenu.add(leftClick);
		ctrlMenu.add(faceClick);

		menuBar.add(menu);
		menuBar.add(iconMenu);
		menuBar.add(ctrlMenu);
		menuBar.add(Box.createGlue());
		menuBar.add(reset);
		menuBar.add(Box.createGlue());
		menuBar.add(time);
		menuBar.add(Box.createGlue());
		menuBar.add(minesLeft);
		menuBar.add(Box.createGlue());

		frame.add(menuBar,BorderLayout.NORTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		for (int i=0; i<difItems.length; i++) {
			if (e.getSource()==difItems[i]) {
				dimR= dimArr[i];
				dimC= dimArr[i+3];
				numMines= mineArr[i];
			}
			setGrid();
		}

		for (int i=0; i<set.length; i++) {
			if (e.getSource()==set[i]) {
				iconSet= i;
				setGrid();
			}
		}
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		if (!gameStopped)
			reset.setIcon(resetIcons[iconSet][1]);
	}

	public void mouseReleased(MouseEvent e) {
		if (!gameStopped)
			reset.setIcon(resetIcons[iconSet][0]);

		for (int i=0; i<dimR; i++) {
			for (int j=0; j<dimC; j++) {
				if (e.getSource()==toggles[i][j]) {
					if (gameStopped) {
						if (e.getButton()==MouseEvent.BUTTON1 && !checkSelected[i][j])
							deselect(i,j);
					}
					else {
						if (toggles[i][j].isSelected()) {
							if (e.getButton()==MouseEvent.BUTTON1) {
								if (checkFlagged[i][j])
									flag(i,j);
								else if (!checkSelected[i][j]) {
									if (firstClick) {
										while (grid[i][j]!='0')
											setMines();
										printGrid();

										timer= new Timer();
										timer.schedule(new UpdateTimer(),0,1000);

										firstClick= false;
									}
									select(i,j);
								}
							}

							else if (e.getButton()==MouseEvent.BUTTON3) {
								if (checkFlagged[i][j])
									deflag(i,j);
								else
									flag(i,j);
							}

						}
						else if (!toggles[i][j].isSelected()) {
							if (e.getButton()==MouseEvent.BUTTON1 && checkSelected[i][j])
								select(i,j);
							else if (e.getButton()==MouseEvent.BUTTON3) {
								if (checkFlagged[i][j])
									deflag(i,j);
								else
									flag(i,j);
							}

						}
						if (checkFlagged[i][j]) {
							flag(i,j);
						}

						if (checkWin())
							stopGame();
					}
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void printGrid() {
		System.out.println();
		for (char[] charArr: grid) {
			for (char c: charArr)
				System.out.print(c+" ");
			System.out.println();
		}
		System.out.println();
	}

	public void defaultIconSize() {
		icons= new ImageIcon[][]{new ImageIcon[]{new ImageIcon("images/msEmpty.png"),new ImageIcon("images/ms1.png"),new ImageIcon("images/ms2.png"),new ImageIcon("images/ms3.png"),new ImageIcon("images/ms4.png"),
												new ImageIcon("images/ms5.png"),new ImageIcon("images/ms6.png"),new ImageIcon("images/ms7.png"),new ImageIcon("images/ms8.png"),
												new ImageIcon("images/mine.jpg"),new ImageIcon("images/flag.png")},
											new ImageIcon[]{new ImageIcon("images/num0.png"),new ImageIcon("images/num1.png"),new ImageIcon("images/num2.png"),new ImageIcon("images/num3.png"),new ImageIcon("images/num4.png"),
												new ImageIcon("images/num5.png"),new ImageIcon("images/num6.png"),new ImageIcon("images/num7.png"),new ImageIcon("images/num8.png"),
												new ImageIcon("images/mine2.png"),new ImageIcon("images/flag2.png")},
											new ImageIcon[]{new ImageIcon("images/box0.png"),new ImageIcon("images/box1.png"),new ImageIcon("images/box2.png"),new ImageIcon("images/images/box3.png"),new ImageIcon("images/box4.png"),
												new ImageIcon("images/box5.png"),new ImageIcon("images/box6.png"),new ImageIcon("images/box7.png"),new ImageIcon("images/box8.png"),
												new ImageIcon("images/mine3.png"),new ImageIcon("images/flag3.png")}};
	}

	public void setGrid() {
		if (timer!=null)
			timer.cancel();

		reset.setIcon(resetIcons[iconSet][0]);

		if (togglePanel!=null)
			frame.remove(togglePanel);

		grid= new char[dimR][dimC];
		checkSelected= new boolean[dimR][dimC];
		checkFlagged= new boolean[dimR][dimC];
		difficulty= "easy";
		firstClick= true;
		gameStopped= false;
		numMinesLeft= numMines;
		minesLeft.setText("Mines Left: "+numMinesLeft);
		timePassed= 0;
		time.setText("Time Passed: 0"+timePassed);

		defaultIconSize();
		for (int i=0; i<icons.length; i++)
			for (int j=0; j<icons[i].length; j++) {
				icons[i][j]= new ImageIcon(icons[i][j].getImage().getScaledInstance(frame.getWidth()/dimC,frame.getHeight()/dimR,Image.SCALE_SMOOTH));
			}

		togglePanel= new JPanel();
		togglePanel.setLayout(new GridLayout(dimR,dimC));
		toggles= new JToggleButton[dimR][dimC];
		for (int i=0; i<dimR; i++) {
			for (int j=0; j<dimC; j++) {
				toggles[i][j]= new JToggleButton();
				toggles[i][j].addMouseListener(this);
				togglePanel.add(toggles[i][j]);
			}
		}

		frame.add(togglePanel,BorderLayout.CENTER);
		frame.revalidate();

	}


	public void setMines() {
		for (int i=0; i<grid.length; i++) {
			for (int j=0; j<grid[i].length; j++) {
				grid[i][j]= '0';
				checkSelected[i][j]= false;
				checkFlagged[i][j]= false;
			}
		}
		//place mines
		if (difficulty=="easy") {
			int minesLeft= numMines;
			while (minesLeft>0) {
				int randRow= (int)(Math.random()*grid.length);
				int randCol= (int)(Math.random()*grid[0].length);
				if (grid[randRow][randCol]!='X') {
					grid[randRow][randCol]= 'X';
					minesLeft--;
				}
			}
		}

		//set numbers
		for (int i=0; i<grid.length; i++)
			for (int j=0; j<grid[i].length; j++)
				if (grid[i][j]!='X') {
					int mineCount= 0;
					for (int i2=i-1; i2<=i+1; i2++)
						for (int j2=j-1; j2<=j+1; j2++)
							if (i2>=0 && i2<dimR && j2>=0 && j2<dimC)
								if (grid[i2][j2]=='X')
									mineCount++;
					grid[i][j]= Character.forDigit(mineCount,10);
				}

	}

	public void select(int row, int col) {
		checkSelected[row][col]= true;
		toggles[row][col].setSelected(true);

		String gridStr= String.valueOf(grid[row][col]);
		if (gridStr.matches("[0-9]+"))
			toggles[row][col].setIcon(icons[iconSet][Integer.parseInt(gridStr)]);
		else if (grid[row][col]=='X') {
			toggles[row][col].setIcon(icons[iconSet][9]);
			if (!gameStopped)
				stopGame();
		}

		if (grid[row][col]=='0')
			destroy(row,col);

		if (checkFlagged[row][col])
			flag(row,col);

	}

	public void deselect(int row, int col) {
		toggles[row][col].setSelected(false);
	}

	public void flag(int row, int col) {
		if (!checkFlagged[row][col])
			numMinesLeft--;
		checkFlagged[row][col]= true;
		toggles[row][col].setSelected(false);
		toggles[row][col].setIcon(icons[iconSet][10]);
		minesLeft.setText("Mines Left: "+numMinesLeft);
	}

	public void deflag(int row, int col) {
		if (checkFlagged[row][col])
			numMinesLeft++;
		checkFlagged[row][col]= false;
		toggles[row][col].setSelected(false);
		toggles[row][col].setIcon(null);
		minesLeft.setText("Mines Left: "+numMinesLeft);
	}

	//destroy all adacent zeroes
	public void destroy(int row, int col) {
		for (int i=row-1; i<=row+1; i++) {
			for (int j=col-1; j<=col+1; j++) {
				if (i>=0 && j>=0 && i<dimR && j<dimC) {
					if (!checkSelected[i][j]) {
						select(i,j);
						if (grid[i][j]=='0') {
							destroy(i,j);
						}
					}
				}
			}
		}
		int flagCount= 0;
		for (int i=0; i<checkFlagged.length; i++)
			for (int j=0; j<checkFlagged[i].length; j++)
				if (checkFlagged[i][j])
					flagCount++;
		minesLeft.setText("Mines Left: "+(numMines-flagCount));
	}

	//stop the game
	public void stopGame() {
		if (checkWin())
			reset.setIcon(resetIcons[iconSet][3]);
		else
			reset.setIcon(resetIcons[iconSet][2]);

		try {
			Thread.sleep(200);
		}
		catch (InterruptedException e) {
			System.out.println("Error");
		}

		gameStopped= true;
		for (int i=0; i<grid.length; i++) {
			for (int j=0; j<grid[i].length; j++) {
				if (grid[i][j]=='X') {
					select(i,j);
				}

			}
		}
	}

	//check that all non-mine spaces are clicked
	public boolean checkWin() {
		for (int i=0; i<grid.length; i++)
			for (int j=0; j<grid[i].length; j++)
				if (grid[i][j]!='X' && !checkSelected[i][j])
					return false;
		return true;
	}

	public void printGrid(boolean[][] mat) {
		for (int i=0; i<mat.length; i++) {
			for (int j=0; j<mat[i].length; j++) {
				System.out.print(mat[i][j]+"\t");
			}
			System.out.println();
		}
	}

	public void printGrid(char[][] mat) {
		for (int i=0; i<mat.length; i++) {
			for (int j=0; j<mat[i].length; j++) {
				System.out.print(mat[i][j]+" ");
			}
			System.out.println();
		}
	}

	public class UpdateTimer extends TimerTask {
		public void run() {
			if(!gameStopped){
				timePassed++;
				if (timePassed<10)
					time.setText("Time Passed: 0"+timePassed);
				else
					time.setText("Time Passed: "+timePassed);
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MineSweeper app= new MineSweeper();
			}
		});
	}
}