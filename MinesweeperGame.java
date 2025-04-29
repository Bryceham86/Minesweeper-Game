/*Bryce Hamilton 
Final Assignment
 */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class MinesweeperGame {
    private JFrame frame;
    private JPanel boardPanel;
    private JLabel timerLabel;
    private javax.swing.Timer gameTimer;
    private Cell[][] cells;
    private int rows, cols, totalMines;
    private int timeLimit;
    private int secondsElapsed = 0;
    private boolean gameOver = false;

    private class Cell extends JButton {
        boolean isMine = false;
        boolean isRevealed = false;
        boolean isFlagged = false;
        int adjacentMines = 0;

        public Cell() {
            setPreferredSize(new Dimension(30, 30));
            setMargin(new Insets(0, 0, 0, 0));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (gameOver) return;
                    if (SwingUtilities.isRightMouseButton(e)) {
                        toggleFlag();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (!isFlagged) reveal();
                    }
                }
            });
        }

            // Toggle flag state for the cell
        void toggleFlag() {
            if (isRevealed) return;
            isFlagged = !isFlagged;
            setText(isFlagged ? "F" : "");
        }
            // Reveal cell and handle mines
        void reveal() {
            if (isRevealed || isFlagged) return;
            isRevealed = true;
            if (isMine) {
                explode();
                gameOver();
            } else {
                setEnabled(false);
                if (adjacentMines > 0) setText(String.valueOf(adjacentMines));
                else revealAdjacent();
            }
        }
         // cell  exploded "visually" and play sound
        void explode() {
            setBackground(Color.RED);
            setText("*");
            playExplosionSound();
        }
        //reveal adjacent non-mine cells
        void revealAdjacent() {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = getCellRow(this) + dr;
                    int nc = getCellCol(this) + dc;
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                        cells[nr][nc].reveal();
                    }
                }
            }
        }
    }
    //setup difficulty, GUI, and start timer
    public MinesweeperGame() {
        selectDifficulty();
        createAndShowGUI();
        startTimer();
    }
    //Setupping up the board for each difficulty 
    void selectDifficulty() {
        String[] options = {"Beginner", "Advanced", "Expert"};
        int choice = JOptionPane.showOptionDialog(null, "Select Difficulty", "Minesweeper",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0: rows = 6; cols = 9; totalMines = 11; timeLimit = 60; break;
            case 1: rows = 12; cols = 18; totalMines = 36; timeLimit = 180; break;
            case 2: rows = 21; cols = 26; totalMines = 92; timeLimit = 660; break;
            default: System.exit(0);
        }
    }
 // Setupoing the game window, initialize the grid and layout that was picked 
    void createAndShowGUI() {
        frame = new JFrame("Minesweeper");
        boardPanel = new JPanel(new GridLayout(rows, cols));
        timerLabel = new JLabel("Time: 0");
        frame.setLayout(new BorderLayout());
        frame.add(timerLabel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);

        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = new Cell();
                cells[r][c] = cell;
                boardPanel.add(cell);
            }
        }

        placeMines();
        calculateAdjacentMines();

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    //starts the timer 
    void startTimer() {
        gameTimer = new javax.swing.Timer(1000, e -> {
            secondsElapsed++;
            timerLabel.setText("Time: " + secondsElapsed);
            if (secondsElapsed >= timeLimit) {
                gameOver();
            }
        });
        gameTimer.start();
    }

    //Place Mines in random areas
    void placeMines() {
        int placed = 0;
        Random rand = new Random();
        while (placed < totalMines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!cells[r][c].isMine) {
                cells[r][c].isMine = true;
                placed++;
            }
        }
    }
       // Count adjacent mines for each cell 
    void calculateAdjacentMines() {
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine) continue;
                int count = 0;
                for (int i = 0; i < 8; i++) {
                    int nr = r + dr[i];
                    int nc = c + dc[i];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && cells[nr][nc].isMine) count++;
                }
                cells[r][c].adjacentMines = count;
            }
        }
    }

    int getCellRow(Cell cell) {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] == cell) return r;
        return -1;
    }

    int getCellCol(Cell cell) {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] == cell) return c;
        return -1;
    }
// Shows all bombs and mark incorrect flags
    void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine && !cell.isRevealed && !cell.isFlagged) {
                    cell.explode();
                } else if (!cell.isMine && cell.isFlagged) {
                    cell.setText("X");
                    cell.setForeground(Color.RED);
                }
            }
        }
    }
     // Handle end of game: stop timer, reveal mines, show message
    void gameOver() {
        if (!gameOver) {
            gameTimer.stop();
            gameOver = true;
            revealAllMines();
            JOptionPane.showMessageDialog(frame, "Game Over! Thank you for Playing! -created by Bryce Hamilton. ");
        }
    }
    // Play explosion sound from file
    void playExplosionSound() {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("resoucres\\explosion_x.wav")); //taken from a source on the internet not my own sound 
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Could not play sound.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MinesweeperGame::new);
    }
}
