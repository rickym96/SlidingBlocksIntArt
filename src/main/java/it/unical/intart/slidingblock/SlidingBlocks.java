package it.unical.intart.slidingblock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SlidingBlocks extends JPanel{ //jpanel disegna la griglia in un pannello dedicato

	
	
	//Grandezza del nostro gioco sliding block
	private int size;

	//Numero di Tile usati
	private int nbTiles;

	//Dimensione della Griglia UI
	private int dimension ;

	//colore di foreground
	private static final Color FOREGROUND_COLOR = new Color(239,83,80); //colore arbitrario

	// Random Object to shuffle tiles
	private static final Random RANDOM = new Random();

	//Salviamo i tiles in un array 1D di interi
	private int[] tiles;

	//Dimensione dei tile nella UI
	private int tileSize;

	//Posizione del tile bianco
	private int blankPos;

	//Margine per la griglia sul frame
	private int margin;

	//Dimensione Griglia UI
	private int gridSize;
	private boolean gameOver; //Vero se il gioco finisce, falso altrimenti

	public SlidingBlocks (int size, int dim, int mar) {
		this.size = size;
		dimension = dim;
		margin= mar;

		//inizializzazione tiles
		nbTiles = size*size -1 ; //perchè non viene contato il tile bianco
		tiles = new int[size*size];

		//calcoliamo la grandezza della griglia e del tile
		gridSize = (dim-2*margin);
		tileSize = gridSize/ size;

		setPreferredSize(new Dimension(dimension, dimension +margin));
		setBackground(Color.WHITE);
		setForeground(FOREGROUND_COLOR);
		setFont(new Font("TimesRoman" ,Font.BOLD, 60));
		gameOver = true;

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				//listener per interagire con il mouse sulla griglia
				if(gameOver) {
					newGame();

				}else {
					int ex= e.getX() - margin;
					int ey= e.getY() -margin;

					//Clic sulla griglia
					if(ex<0 || ex>gridSize || ey<0 || ey> gridSize)
						return;

					//Ottieni posizione sulla griglia
					int c1 = ex/ tileSize;
					int r1 = ey/ tileSize; 

					// Ottieni posizione della cella bianca
					int c2 = blankPos% size;
					int r2 = blankPos /size;

					//Conversione in coordinate 1D
					int clickPos = r1*size+c1;

					int dir =0;

					//Ricerca della direzione per il movimento multiplo dei tile uno alla volta
					if(c1 ==c2 && Math.abs(r1-r2)>0)
						dir = (r1-r2)>0 ? size: -size;
						else if(r1 == r2 && Math.abs(c1-c2)>0)
							dir = (c1-c2) >0 ? 1: -1;

							if(dir!=0) {
								//Muovi i tiles in quella direzione
								do {
									int newBlankPos = blankPos+dir;
									tiles[blankPos] = tiles[newBlankPos];
									blankPos= newBlankPos;
								}while(blankPos!=clickPos);
								tiles[blankPos] =0;
							}

							//Check per controllare che il gioco sia finito
							gameOver= isSolved();
				}

				//Ricolorazione panel
				repaint();

			}

		});
		newGame();
	}

	private void newGame() {
		do {
			reset(); //resetta lo stato iniziale
			shuffle(); //shuffle
		}
		while(!isSolvable()); //Fa fino a quando la griglia è risolvibile
		gameOver=false;
	}

	private void reset() {
		for(int i= 0; i<tiles.length;i++) {
			tiles[i] = (i+1) % tiles.length;
		}
		//mettiamo la cella bianca ultima
		blankPos = tiles.length -1;
	}

	private void shuffle() {
		// non includiamo la cella bianca nello shuiffle, viene lasciata nella posizione di solve
		int n = nbTiles;

		while(n>1) {
			int r = RANDOM.nextInt(n--);
			int tmp = tiles[r];
			tiles[r] = tiles[n];
			tiles[n] = tmp;
		}
	}
	// Solo la metà delle permutazioni del puzzle sono risolvibili
	// Fino a quando un tile è preceduto da un tile con un valore più alto viene contato
	// come un inversion. Nel nostro case con il tile bianco nella solved position,
	// il numero di inversions must be even for the puzzle to be solvable

	private boolean isSolvable() {
		int countInversions=0;

		for(int i =0;i<nbTiles;i++) {
			for(int j =0; j<i;j++) {
				if(tiles[j] >tiles[i])
					countInversions++;
			}
		}
		return countInversions % 2==0;

	}

	private boolean isSolved() {
		if(tiles[tiles.length -1] !=0) // se il tile bianco non è nella solved position ==> non solved
			return false;

		for(int i = nbTiles-1;i>=0;i--) {
			if(tiles[i]!= i+1)
				return false;
		}
		return true;
	}



	private void drawGrid(Graphics2D g) {
		for(int i =0;i<tiles.length;i++) {

			// Convertiamo le coordinate 1D in coordinate 2D dando la size dell'array 2D
			int r = i/size;
			int c = i% size;

			//convertiamo in coord della UI
			int x = margin+c*tileSize;
			int y = margin+r*tileSize;

			//Controlla casi speciali per il tile BIANCO
			if(tiles[i] ==0) {
				if(gameOver) {
					g.setColor(FOREGROUND_COLOR);
					drawCenteredString(g, "V", x,y);
				}
				continue;
			}

			//per gli altri tile
			g.setColor(getForeground());
			g.fillRoundRect(x,y,tileSize, tileSize,25,25);
			g.setColor(Color.BLACK);
			g.drawRoundRect(x, y, tileSize, tileSize, 25, 25);
			g.setColor(Color.WHITE);

			drawCenteredString(g, String.valueOf(tiles[i]) , x , y);


		}

	}


	private void drawStartMessage(Graphics2D g) {
		if(gameOver) {
			g.setFont(getFont().deriveFont(Font.BOLD, 18));
			g.setColor(FOREGROUND_COLOR);
			String s = "Clicca per startare un nuovo gioco";
			g.drawString(s, (getWidth()-g.getFontMetrics().stringWidth(s))/2, 
					getHeight()- margin	);
		}
	}

	private void drawCenteredString(Graphics2D g, String s, int x, int y) {
		// centra la stringa s per il tile dato(x,y)

		FontMetrics fm= g.getFontMetrics();
		int asc = fm.getAscent();
		int desc = fm.getDescent();

		g.drawString(s, x+(tileSize - fm.stringWidth(s))/2,
				y+(asc+(tileSize-(asc+desc)) /2));

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawGrid(g2D);
		drawStartMessage(g2D);

	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			JFrame frame= new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setTitle("Sliding Blocks");
			frame.setResizable(false);
			frame.add(new SlidingBlocks(4, 550, 30), BorderLayout.CENTER);
			frame.pack();
			//centra sullo schermo
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

}
