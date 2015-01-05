/**
 * Meinsweeper.java
 * 
 * Original version of a traditional minesweeper-style game (Minesweeper, KMines)
 *  
 * @author elsapet
 * @version 1.0
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Meinsweeper extends JFrame 
{		
	private int row;
	private int col;
	private int numMines;
	private int mineCount;
	private int flagCount;	
	
	private boolean gameOver;
		
	private MeinCell cells [][] = null;	// grid of individual cells
	private JPanel grid = null;	
	private JLabel displayMinesRemaining = new JLabel();
	
	private JRadioButton easy = new JRadioButton("easy");
	private JRadioButton medium = new JRadioButton("medium");
	private JRadioButton hard = new JRadioButton("hard");

	private ImageIcon coal = new ImageIcon("assets/coal.gif");
	private ImageIcon sweep = new ImageIcon("assets/chimney_sweep.gif");
	private ImageIcon chimneySweep = new ImageIcon("assets/chimneysweep_silhouette.gif");
	private ImageIcon chimneyClean = new ImageIcon("assets/sweep_clean.gif");
	
	private ImageIcon num1 = new ImageIcon("assets/num1.gif");
	private ImageIcon num2 = new ImageIcon("assets/num2.gif");
	private ImageIcon num3 = new ImageIcon("assets/num3.gif");
	private ImageIcon num4 = new ImageIcon("assets/num4.gif");
	private ImageIcon num5 = new ImageIcon("assets/num5.gif");
	private ImageIcon num6 = new ImageIcon("assets/num6.gif");
	private ImageIcon num7 = new ImageIcon("assets/num7.gif");
	private ImageIcon num8 = new ImageIcon("assets/num8.gif");
	
	/**
	 * Listener class for mouse clicks (left, right, middle)
	 */
	private class Click implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e) 
		{ }

		/**
		 * abstract method from MouseListener class
		 * precondition: mouse button (left, right, or middle) has been pressed
		 * @param MouseEvent e (mouse click)
		 */
		@Override
		public void mousePressed(MouseEvent e) 
		{
			if (!gameOver)	// game has not ended
			{
				for(int i = 0; i < row; i++)
					for(int j = 0; j < col; j++)
						if (e.getSource() == cells[i][j].ms)	// locating which cell has been pressed
						{
							if (SwingUtilities.isLeftMouseButton(e))
							{										
								cells[i][j].ms.setBackground(Color.GRAY);
							}
							else if (SwingUtilities.isRightMouseButton(e))
							{
								cells[i][j].color = cells[i][j].ms.getBackground();	// store original color in Color array
								cells[i][j].ms.setBackground(Color.GRAY);
							}							
							else if (SwingUtilities.isMiddleMouseButton(e))
							{
								halo(i,j,Color.GRAY);	// highlight surrounding cells
							}
							break;
						}
			}
		}

		/**
		 * abstract method from MouseListener class
		 * precondition: mouse button (left, right, or middle) has been released
		 * @param MouseEvent e (mouse click)
		 */
		@Override
		public void mouseReleased(MouseEvent e) 
		{
			if (!gameOver)
			{
				for(int i = 0; i < row; i++)
					for(int j = 0; j < col; j++)
						if (e.getSource() == cells[i][j].ms)	// locating which cell has been pressed
						{
							if (SwingUtilities.isLeftMouseButton(e))
							{
								if (isAMine(i,j))
								{
									cells[i][j].ms.setBackground(Color.LIGHT_GRAY);
									allMines();		// show all "mines" 
								}
								else 
								{
									cells[i][j].clicked = true;	// mark cell as "clicked"
									cells[i][j].ms.setBackground(Color.LIGHT_GRAY);									
									if (numCount(i,j) > 0)	// one or more "mines" in vicinity of cell
										cells[i][j].ms.setIcon(numIcon(i,j));	// show number of "mines" in vicinity of cell
									else	
									{
										cells[i][j].ms.setIcon(null);
										clearBlocks(i,j);	// recursively clear cells
									}									
								}						
							}
							else if (SwingUtilities.isRightMouseButton(e))
							{
								cells[i][j].ms.setBackground(cells[i][j].color);
								if (!cells[i][j].clicked)
								{
									cells[i][j].ms.setBackground(Color.LIGHT_GRAY);
									if (cells[i][j].flag)	// cell has a flag already
									{
										cells[i][j].flag = false;	
										cells[i][j].ms.setIcon(null);	// take away flag display										
										flagCount--;	
										
										mineCount++;
										displayMinesRemaining.setText("Mines remaining: "+mineCount);										
									}
									else
									{
										cells[i][j].flag = true; // mark cell as flagged
										cells[i][j].ms.setIcon(sweep);
										flagCount++;										
										if (mineCount > 0)
										{
											mineCount--;
											displayMinesRemaining.setText("Mines remaining: "+mineCount);
										}
										if (flagCount == numMines) // number of flags on grid same as number of "mines"
											if (checkIfWin())
												JOptionPane.showMessageDialog(Meinsweeper.this,
														"Congratulations you win!\nReset to play again!",
														"Win!", JOptionPane.PLAIN_MESSAGE);
									}	
								}
							}							
							else if (SwingUtilities.isMiddleMouseButton(e))
							{
								if (haloFlags(i,j) == numCount(i,j)) // if right amount of flags in vicinity of cell
								{	if (numCount(i,j) == 0)	// no mines in vicinity of cell
										restoreHalo(i,j);
									else
									{
										restoreHalo(i,j);
										haloSolution(i,j);	// solve for other cells in vicinity of cell
									}
								}
								else
									restoreHalo(i,j);
							}
							break;
						}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) { }

		@Override
		public void mouseExited(MouseEvent e) {	}
	}
	
	/**
	 * Listener class for Reset button
	 */
	private class ResetButton implements ActionListener
	{
		/**
		 * Abstract method from ActionListener class
		 * Resets game to easy, medium, or hard, using initialiseGame(rows, columns, number of "mines")
		 * precondition: reset button has been clicked
		 * @param ActionEvent e (reset button clicked)
		 */
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			initialiseGame(1,1,1);	// default initialisation
			
			if (easy.isSelected())
				initialiseGame(8,8,10);
			else if (medium.isSelected())
				initialiseGame(12,12,15);
			else if (hard.isSelected())
				initialiseGame(18,18,25);			
		}		
	}
	
	/**
	 * Listener class for Help button
	 */
	private class HelpButton implements ActionListener
	{
		/**
		 * Abstract method from ActionListener class
		 * Shows JOptionPane containing directions for gameplay
		 * precondition: help button has been clicked
		 * @param ActionEvent e (help button clicked)
		 */
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			JPanel help = new JPanel();
			help.setLayout(new BoxLayout(help, BoxLayout.Y_AXIS));
			JLabel leftClick = new JLabel("LEFT CLICK:");
			JLabel leftClick2 = new JLabel("Reveals number of coals surrounding block (dark grey if none)");
			JLabel leftClick3 = new JLabel("or GAME OVER if block is a coal");
			JLabel rightClick = new JLabel("RIGHT CLICK:");
			JLabel rightClick2 = new JLabel("Places or removes chimney brush");
			JLabel middleClick = new JLabel("MIDDLE CLICK:");
			JLabel middleClick2 = new JLabel("Highlights surrounding blocks and reveals their numbers (or coals)");
			
			help.add(leftClick);
			help.add(leftClick2);
			help.add(leftClick3);
			help.add(rightClick);
			help.add(rightClick2);
			help.add(middleClick);
			help.add(middleClick2);
			
			JOptionPane.showMessageDialog(Meinsweeper.this,help,
					"Help", JOptionPane.PLAIN_MESSAGE);			
		}		
	}

	/**
	 * Implementation of WindowListener class for exit
	 * Prompts user if close button ("X") clicked "Do you really want to exit?"
	 */
	private class ExitCheck extends JFrame implements WindowListener
	{
		@Override
		public void windowActivated(WindowEvent e) 
		{ }

		@Override
		public void windowClosed(WindowEvent e) 
		{ }

		/**
		 * Confirmation window to manage game exit
		 * @param WindowEvent e (user has selected close "X")
		 */
		@Override
		public void windowClosing(WindowEvent e) 
		{			
			Object[] options = {"Yes", "No"};
			int reply = JOptionPane.showOptionDialog(
				    Meinsweeper.this,
				    "Do you really want to exit?",
					"Exit?",
				    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, chimneySweep, options, options[0]);
			if (reply == JOptionPane.YES_OPTION) // if user clicks yes to exit
				System.exit(0);				
		}

		@Override
		public void windowDeactivated(WindowEvent e) 
		{ }

		@Override
		public void windowDeiconified(WindowEvent e) 
		{ }

		@Override
		public void windowIconified(WindowEvent e) 
		{ }

		@Override
		public void windowOpened(WindowEvent e) 
		{ }		
	}
	
	/**
	 * Constructor for Meinsweeper class
	 * Default game initialised to "easy" (8 x 8 grid with 10 mines)
	 */
	public Meinsweeper()
	{
		super();
		msLayout();
		initialiseGame(8,8,10);	
	}
	
	/**
	 * Method to initialise game grid: resets counters, re-makes cell grid, calls functions
	 * to place "mines", create arrays, and place new game grid
	 * @param row Row number
	 * @param col Column number
	 * @param numMines Number of "mines" 
	 */
	private void initialiseGame(int row, int col, int numMines)
	{	
		this.gameOver = false;
		this.flagCount = 0;
		
		this.row = row;
		this.col = col;
		
		this.numMines = numMines;
		this.mineCount = numMines;
		
		displayMinesRemaining.setText("Mines remaining: "+mineCount);
		
		cells = new MeinCell[row][col];	// create new cells for new game
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				cells[i][j] = new MeinCell();
		
		mineGen(row,col, numMines);
		msArray();
		placeGrid();		
	}
	
	/**
	 * Method to generate "mine" positions randomly 
	 * Work with random numbers in range [0 - target), where target = size of grid (row * column)
	 * Every time a "mine" is placed, decrement target (to avoid repeat placing)
	 * postcondition: if "mine" is placed in cell, boolean mine for that cell is "true"
	 * @param row Row number
	 * @param col Column number
	 * @param numMines Number of mines 
	 */
	private void mineGen(int row, int col, int numMines)
	{	
		for(int m = 0; m < numMines; m++)
		{
			int size = row*col;
			int target = (int)(Math.random()*(size-m));
				
			int count = 0;
			for(int i = 0; i < row; i++)
				for(int j = 0; j < col; j++) 			
					if (!cells[i][j].mine) 
					{
						if (count == target)
							cells[i][j].mine = true;	// place mine
						count ++;
					}
		}
	}
	
	/**
	 * Method to set display of each cell button (color) 
	 * and add MouseListener "click"
	 */
	private void msArray()
	{		
		for(int i=0; i < row; i++)
			for(int j=0; j < col; j++)
			{				
				cells[i][j].ms = new JButton("");	
				cells[i][j].ms.setBackground(Color.LIGHT_GRAY);
				Click click = new Click();
				cells[i][j].ms.addMouseListener(click);
			}
	}	
	
	/**
	 * Method to layout game grid and add WindowListener
	 */
	private void msLayout()	
	{
		setLayout(new BorderLayout());
		setSize(600, 600);
		setResizable(false);
		setTitle("MeinSweeper");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new ExitCheck());
		
		getContentPane().setBackground(Color.GRAY);
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		
		JLabel heading = new JLabel("Welcome to Mein Sweeper!");
		heading.setBackground(Color.GRAY);
		JLabel objective = new JLabel("You are a hard-working chimney sweep. It is your mission to navigate ");
		JLabel objective2 = new JLabel("a path through the chimney. Use logic and guess work to avoid the coals.");
		
		top.add(heading);
		top.add(objective);
		top.add(objective2);

		add(top, BorderLayout.NORTH);		
		
		JPanel east = selectDifficulty();
		add(east, BorderLayout.EAST);			
	}
	
	/**
	 * Method to make grid and add to layout
	 */
	private void placeGrid()
	{	
		if (grid != null)	// clears grid from previous game (if any)
			remove(grid);
		grid = new JPanel();
		grid.setLayout(new GridLayout(row,col));
		for(int i = 0; i < row; i++)
			for(int j=0; j < col; j++)
				grid.add(cells[i][j].ms);
		add(grid, BorderLayout.CENTER);
	}
	
	/**
	 * Method to create JPanel (for layout) with difficulty settings, and reset and help options. 
	 * Also adds ActionListeners for Reset and Help buttons.
	 * @return Returns JPanel with RadioButtons (for difficulty), Reset, and Help buttons
	 */
	private JPanel selectDifficulty()
	{
		JPanel east = new JPanel();
		east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
		
		JLabel line = new JLabel("---------------------------");
		JLabel line2 = new JLabel("---------------------------");
		JLabel line3 = new JLabel("---------------------------");
		
		JLabel select = new JLabel("Select Difficulty:");
		easy.setSelected(true);
		
		JButton reset = new JButton("reset");		
		JButton help = new JButton("help");
		
		reset.addActionListener(new ResetButton());
		help.addActionListener(new HelpButton());
		
		ButtonGroup difficulty = new ButtonGroup();
		difficulty.add(easy);
		difficulty.add(medium);
		difficulty.add(hard);
		
		JLabel picLabel = new JLabel(chimneyClean);		
		
		east.add(line);
		east.add(select);
		east.add(easy);
		east.add(medium);
		east.add(hard);		
		east.add(reset);
		east.add(help);
		east.add(line2);
		east.add(displayMinesRemaining);
		east.add(line3);
		east.add(picLabel);
		
		return east;
	}
	
	/**
	 * Method that sets the color of cells surrounding cell[i][j] to the color specified
	 * @param i Row number
	 * @param j Column number
	 * @param c Color to which cell must change
	 */
	private void halo(int i, int j, Color c) 
	{
		for(int ii = i-1; ii <= i+1; ii++)
			for(int jj = j-1; jj <= j+1; jj++)
				if(ii >= 0 && jj >= 0 && ii < row && jj < col)
				{
					Color original = cells[ii][jj].ms.getBackground();
					cells[ii][jj].color = original;
					cells[ii][jj].ms.setBackground(c);
				}
	}
	
	/**
	 * Method that restores original color of cells surrounding cell[i][j]
	 * @param i Row number
	 * @param j Column number
	 */
	private void restoreHalo(int i, int j)
	{
		for(int ii = i-1; ii <= i+1; ii++)
			for(int jj = j-1; jj <= j+1; jj++)
				if(ii >= 0 && jj >= 0 && ii < row && jj < col)
					cells[ii][jj].ms.setBackground(cells[ii][jj].color);
	}

	/**
	 * Method that shows number of "mines" for cells surrounding cell[i][j], provided
	 * flag count in vicinity of cell[i][j] matches number of "mines" in vicinity of cell[i][j]
	 * @param i Row number
	 * @param j Column number
	 */
	private void haloSolution(int i, int j) 
	{
		int count = numCount(i,j);
		if (cells[i][j].flag)
			cells[i][j].ms.setBackground(cells[i][j].color); // if cell is flagged, restore original color
		else if (count > 0)
		{			
			for(int ii = i-1; ii <= i+1; ii++)
				for(int jj = j-1; jj <= j+1; jj++)
					if(ii >= 0 && jj >= 0 && ii < row && jj < row)
					{						
						int num = numCount(ii,jj);
						if (cells[ii][jj].flag)
							cells[ii][jj].ms.setBackground(cells[ii][jj].color);
						else if (cells[ii][jj].mine)
						{
							allMines();	// show all mines (game over)
							cells[ii][jj].ms.setBackground(cells[ii][jj].color);
						}
						else if (num > 0)
						{
							cells[ii][jj].ms.setIcon(numIcon(ii,jj));	// show numbers of surrounding cells
							cells[ii][jj].ms.setBackground(cells[ii][jj].color);
						}
						else if (num == 0)	
							clearBlocks(ii, jj);	// clear cells recursively
					}
		}
	}
	
	/**
	 * Method that returns correct number image for cell in question
	 * @param i Row number
	 * @param j Column number
	 * @return Returns appropriate image for cell
	 */
	private ImageIcon numIcon(int i, int j)
	{
		int num = numCount(i,j);
		switch (num)
		{
			case 1:
				return num1;
			case 2:
				return num2;
			case 3:
				return num3;
			case 4:
				return num4;
			case 5:
				return num5;
			case 6:
				return num6;
			case 7:
				return num7;
			case 8:
				return num8;
			default:
				return null;
		}
	}
	
	/**
	 * Method to check if game has been won or lost
	 * @return true if won, false if lost
	 */
	private boolean checkIfWin()
	{
			boolean win = true;
			for(int i = 0; i < row; i++)
				for(int j = 0; j < col; j++)
					if (cells[i][j].flag && !cells[i][j].mine)
						win = false;
			if (win)
				gameOver = true;
			return win;
	}
	
	/**
	 * Method to check if cell[i][j] is a "mine"
	 * @param i Row number
	 * @param j Column number
	 * @return true if is a mine, false if not
	 */
	private boolean isAMine(int i, int j)
	{
		if (cells[i][j].mine)	
			return true;
		else 					
			return false;
	}

	/**
	 * Method to show all "mines" and thus end game
	 * precondition: a "mine" is left-clicked
	 * postcondition all "mine" images are displayed, "Game over!" message
	 */
	private void allMines()
	{
		displayMinesRemaining.setText("Mines remaining: --");
		this.gameOver = true;	// game is over (current MouseListener and ActionListeners deactivated)
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				if (isAMine(i,j) && ! cells[i][j].flag)
				{
					cells[i][j].ms.setIcon(coal);
					cells[i][j].ms.setBackground(Color.GRAY);
				}
		JOptionPane.showMessageDialog(Meinsweeper.this,"Sorry, you lose!\nReset to try again.", 
				"Game Over!", JOptionPane.PLAIN_MESSAGE);
	}
	
	/**
	 * Method to return number of "mines" in 8-cell radius of cell[i][j]
	 * @param i Row number
	 * @param j Column number
	 * @return Return number of "mines" in vicinity of cell[i][j]
	 */
	private int numCount(int i, int j)	
	{
		int count = 0;		
		for(int ii = i-1; ii <= i+1; ii++)
			for(int jj = j-1; jj <= j+1; jj++)
				if(ii >= 0 && jj >= 0 && ii < row && jj < row)
					if(cells[ii][jj].mine)
						count ++;
		return count;
	}
	
	/**
	 * Method to count the number of flags in 8-cell radius of cell[i][j]
	 * @param i Row number
	 * @param j Column number
	 * @return Return number of flags in vicinity of cell[i][j]
	 */
	private int haloFlags(int i, int j)
	{
		int count = 0;
		for(int ii = i-1; ii <= i+1; ii++)
			for(int jj = j-1; jj <= j+1; jj++)
				if(ii >= 0 && jj >= 0 && ii < row && jj < row)
					if(cells[ii][jj].flag)
						count ++;
		return count;		
	}
	
	/**
	 * Method to clear cells (recursively) in 8-cell radius of cell[i][j] that have 
	 * a "mine" count of zero
	 * @param i Row number
	 * @param j Column number
	 */
	private void clearBlocks(int i, int j)
	{
		cells[i][j].clicked = true;
		cells[i][j].ms.setIcon(null);		
		cells[i][j].ms.setBackground(Color.DARK_GRAY);
		
		for (int ii = i-1; ii <= i+1; ii++)
			for (int jj = j-1; jj <= j+1; jj++)
			{
				if(ii >= 0 && jj >= 0 && ii < row && jj < row)
				{
					if (numCount(ii,jj) == 0)
					{
						if (!cells[ii][jj].clicked)					
							clearBlocks(ii, jj);	// recursive call
					}
					else
					{
						cells[ii][jj].ms.setIcon(numIcon(ii,jj));
						cells[ii][jj].clicked = true;
					}
				}		
			}
	}
}
