package br.edu.fatecsjc.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import br.edu.fatecsjc.controller.LearnController;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JSpinner;

public class DiagnosticView extends JFrame {

	private JPanel contentPane;
	public JLabel lblLogLabel = new JLabel("");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		LearnController controller = new LearnController();
	}

	/**
	 * Create the frame.
	 */
	public DiagnosticView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		lblLogLabel.setBounds(10, 48, 424, 49);
		lblLogLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblLogLabel);
	}
}
