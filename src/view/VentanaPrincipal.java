package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import core.ParsedMethod;
import handler.file.FiltroDirectorio;
import handler.file.FiltroJava;
import handler.parser.ParserHandler;

@SuppressWarnings("serial")
public class VentanaPrincipal extends JFrame {
	private FiltroJava filtroJava;
	private FiltroDirectorio filtroDir;
	private JPanel contentPane;
	private JTextField textFieldLOCT;
	private JTextField textFieldLOCC;
	private JTextField textFieldPLOCC;
	private JTextField textFieldCC;
	private JTextField textFieldFanIn;
	private JTextField textFieldFanOut;
	private JTextField textFieldHL;
	private JTextField textFieldHV;
	private JList<String> listClases;
	private JList<String> listMetodos;
	private JList<File> listFiles;
	private ParserHandler handler;
	private JLabel lblHalsteadVolumen;
	private JLabel lblHalsteadLongitud;
	final private Color green = new Color(0, 128, 0);
	final private Color yellow = new Color(128, 128, 0);
	final private Color red = new Color(128, 0, 0);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VentanaPrincipal frame = new VentanaPrincipal();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VentanaPrincipal() {		
		this.filtroJava = new FiltroJava("java");
		this.filtroDir = new FiltroDirectorio();
		setTitle("Herramienta de Testing");
		setMinimumSize(new Dimension(800, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 410);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnAnalisis = new JMenu("Analisis");
		menuBar.add(mnAnalisis);

		JMenuItem mntmSeleccionar = new JMenuItem("Seleccionar");
		mntmSeleccionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final JFileChooser fc = new JFileChooser("C:/Users/Flor/workspace/Dummy/src/main");
				// fc.setMultiSelectionEnabled(true); //habilitar seleccionar
				// mas de 1 archivo a la vez.
				fc.setFileFilter(filtroDir);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setAcceptAllFileFilterUsed(false);
				int resultado = fc.showOpenDialog(fc);
				if (resultado == JFileChooser.APPROVE_OPTION) {
					// obtengo archivos .java en el nivel de directorio elegido.
					File[] archivos = fc.getSelectedFile().listFiles(filtroJava);

					if (archivos.length == 0) {
						JOptionPane.showMessageDialog(null, "El directorio indicado, no contiene archivos .java");
					} else {
						listFiles.setListData(archivos);
					}
				}
				
			}
		});
		mnAnalisis.add(mntmSeleccionar);

		JMenuItem mntmSalir = new JMenuItem("Salir");
		mntmSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		mnAnalisis.add(mntmSalir);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblCodigoDelMetodo = new JLabel("Codigo del Metodo Seleccionado");
		lblCodigoDelMetodo.setForeground(green);
		lblCodigoDelMetodo.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblCodigoDelMetodo.setBounds(28, 161, 259, 24);
		contentPane.add(lblCodigoDelMetodo);

		TextArea textAreaCodigo = new TextArea();
		textAreaCodigo.setBounds(28, 190, 398, 302);
		contentPane.add(textAreaCodigo);
		textAreaCodigo.setBackground(Color.WHITE);
		textAreaCodigo.setEditable(false);
		
		JScrollPane scrollPaneMetodos = new JScrollPane();
		scrollPaneMetodos.setBounds(579, 33, 140, 107);
		contentPane.add(scrollPaneMetodos);

		listMetodos = new JList<String>();
		listMetodos.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		    	handler.setMethodName(listMetodos.getSelectedValue().toString());
		    	ParsedMethod method = handler.getMethod();
		    	textAreaCodigo.setText(handler.getSource(method.getStart(), method.getEnd()));
		    	textFieldLOCT.setText(String.valueOf(method.getTotalLines()));
		    	textFieldLOCC.setText(String.valueOf(method.getTotalComments()));
		    	double percentage = method.getCommentsPercentage();
		    	textFieldPLOCC.setText(String.format("%.2f", percentage)+"%");
		    	if(percentage >= 30)
		    		textFieldPLOCC.setForeground(green);
		    	else if(percentage <= 14)
		    		textFieldPLOCC.setForeground(red);
		    	else
		    		textFieldPLOCC.setForeground(yellow);
		    	int cc = method.getCyclomaticComplexity();
		    	textFieldCC.setText(String.valueOf(cc));
		    	if(cc <= 10)
		    		textFieldCC.setForeground(green);
		    	else if(cc >= 21)
		    		textFieldCC.setForeground(red);
		    	else
		    		textFieldCC.setForeground(yellow);
		    	textFieldFanIn.setText(String.valueOf(method.getFanIn()));
		    	textFieldFanOut.setText(String.valueOf(method.getFanOut()));
		    	textFieldHL.setText(String.valueOf(method.getHalsteadLength()));
		    	textFieldHV.setText(String.format("%.2f", method.getHalsteadVolume()));
		    	String halsteadInfo = method.getHalsteadInfo();
		    	lblHalsteadLongitud.setToolTipText(halsteadInfo);
		    	lblHalsteadVolumen.setToolTipText(halsteadInfo);
		    }
		});
		scrollPaneMetodos.setViewportView(listMetodos);
		
		JLabel lblSeleccioneUn = new JLabel("Seleccione Metodo de la Lista");
		lblSeleccioneUn.setForeground(green);
		lblSeleccioneUn.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblSeleccioneUn.setBounds(579, 16, 203, 14);
		contentPane.add(lblSeleccioneUn);
		
		JScrollPane scrollPaneClases = new JScrollPane();
		scrollPaneClases.setBounds(353, 33, 140, 107);
		contentPane.add(scrollPaneClases);
		
		JLabel lblClases = new JLabel("Seleccione Clase de la Lista");
		lblClases.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblClases.setForeground(green);
		lblClases.setBounds(353, 11, 226, 24);
		contentPane.add(lblClases);

		listClases = new JList<String>();
		listClases.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		    	handler.setClassName(listClases.getSelectedValue().toString());
		    	listMetodos.setListData(handler.getMethods());
				scrollPaneMetodos.setViewportView(listMetodos);
		    }
		});
		scrollPaneClases.setViewportView(listClases);
		
		JScrollPane scrollPaneArchivos = new JScrollPane();
		scrollPaneArchivos.setBounds(28, 33, 271, 107);
		contentPane.add(scrollPaneArchivos);

		JLabel lblNewLabel_1 = new JLabel("Seleccione un Archivo de la Lista");
		lblNewLabel_1.setForeground(green);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblNewLabel_1.setBounds(28, 17, 226, 14);
		contentPane.add(lblNewLabel_1);	
		
		listFiles = new JList<File>();
		listFiles.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        try {
		        	handler = new ParserHandler();
		        	handler.loadSource(listFiles.getSelectedValue().toString());
		        	handler.parse();
					listClases.setListData(handler.getClasses());
					scrollPaneClases.setViewportView(listClases);
				} catch (IOException e1) {
					// TODO Handle this better.
					JOptionPane.showMessageDialog(null, "Error al cargar el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
				}
		    }
		});
		scrollPaneArchivos.setViewportView(listFiles);

		JLabel lblAnalisis = new JLabel("Analisis del Metodo");
		lblAnalisis.setForeground(green);
		lblAnalisis.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAnalisis.setBounds(457, 166, 259, 14);
		contentPane.add(lblAnalisis);

		JLabel lblLinCodT = new JLabel("Lineas de Codigo Totales:");
		lblLinCodT.setBounds(457, 191, 193, 14);
		contentPane.add(lblLinCodT);

		JLabel labelLinCodComent = new JLabel("Lineas de Codigo Comentadas:");
		labelLinCodComent.setBounds(457, 216, 193, 14);
		contentPane.add(labelLinCodComent);

		JLabel lblPLOCC = new JLabel("<html>Porcentaje de Lineas de <br>Codigo Comentadas:</html>");
		lblPLOCC.setBounds(457, 241, 193, 41);
		lblPLOCC.setToolTipText("<html>Para que sea:<br>"
				+ "<b>Bueno:</b> deberá ser mayor o igual a 30%.<br>"
				+ "<b>Regular:</b> deberá estar entre 30% y 14%.<br>"
				+ "<b>Malo:</b> menor o igual a 14%.</html>");
		contentPane.add(lblPLOCC);

		JLabel lblComplejidadCiclomatica = new JLabel("Complejidad Ciclomatica:");
		lblComplejidadCiclomatica.setBounds(457, 293, 193, 14);
		lblComplejidadCiclomatica.setToolTipText("<html>Para que sea:<br>"
				+ "<b>Buena:</b> deberá ser menor o igual a 10.<br>"
				+ "<b>Regular:</b> deberá estar entre 10 y 21.<br>"
				+ "<b>Mala:</b> mayor o igual a 21.</html>");
		contentPane.add(lblComplejidadCiclomatica);

		JLabel lblFanIn = new JLabel("Fan In:");
		lblFanIn.setBounds(457, 318, 193, 14);
		lblFanIn.setToolTipText("Las invocaciones al metodo que se tienen en cuenta son solo las declaradas en este archivo.");
		contentPane.add(lblFanIn);

		JLabel lblFanOut = new JLabel("Fan Out:");
		lblFanOut.setBounds(457, 343, 193, 14);
		lblFanOut.setToolTipText("Los metodos que se tienen en cuenta son solo los declarados en este archivo.");
		contentPane.add(lblFanOut);

		lblHalsteadLongitud = new JLabel("Halstead Longitud:");
		lblHalsteadLongitud.setBounds(457, 368, 193, 14);
		contentPane.add(lblHalsteadLongitud);

		lblHalsteadVolumen = new JLabel("Halstead Volumen:");
		lblHalsteadVolumen.setBounds(457, 393, 193, 14);
		contentPane.add(lblHalsteadVolumen);

		textFieldLOCT = new JTextField();
		textFieldLOCT.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldLOCT.setText("0");
		textFieldLOCT.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldLOCT.setBorder(null);
		textFieldLOCT.setForeground(new Color(0, 100, 0));
		textFieldLOCT.setEditable(false);
		textFieldLOCT.setBackground(null);
		textFieldLOCT.setBounds(660, 191, 57, 20);
		contentPane.add(textFieldLOCT);
		textFieldLOCT.setColumns(10);

		textFieldLOCC = new JTextField();
		textFieldLOCC.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldLOCC.setText("0");
		textFieldLOCC.setForeground(new Color(0, 100, 0));
		textFieldLOCC.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldLOCC.setEditable(false);
		textFieldLOCC.setColumns(10);
		textFieldLOCC.setBorder(null);
		textFieldLOCC.setBackground((Color) null);
		textFieldLOCC.setBounds(660, 216, 56, 20);
		contentPane.add(textFieldLOCC);

		textFieldPLOCC = new JTextField();
		textFieldPLOCC.setText("0%");
		textFieldPLOCC.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldPLOCC.setForeground(new Color(0, 100, 0));
		textFieldPLOCC.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldPLOCC.setEditable(false);
		textFieldPLOCC.setColumns(10);
		textFieldPLOCC.setBorder(null);
		textFieldPLOCC.setBackground((Color) null);
		textFieldPLOCC.setBounds(660, 247, 56, 20);
		contentPane.add(textFieldPLOCC);

		textFieldCC = new JTextField();
		textFieldCC.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldCC.setText("0");
		textFieldCC.setForeground(new Color(0, 100, 0));
		textFieldCC.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldCC.setEditable(false);
		textFieldCC.setColumns(10);
		textFieldCC.setBorder(null);
		textFieldCC.setBackground((Color) null);
		textFieldCC.setBounds(660, 290, 56, 20);
		contentPane.add(textFieldCC);

		textFieldFanIn = new JTextField();
		textFieldFanIn.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldFanIn.setText("0");
		textFieldFanIn.setForeground(new Color(0, 100, 0));
		textFieldFanIn.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldFanIn.setEditable(false);
		textFieldFanIn.setColumns(10);
		textFieldFanIn.setBorder(null);
		textFieldFanIn.setBackground((Color) null);
		textFieldFanIn.setBounds(660, 318, 56, 20);
		contentPane.add(textFieldFanIn);

		textFieldFanOut = new JTextField();
		textFieldFanOut.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldFanOut.setText("0");
		textFieldFanOut.setForeground(new Color(0, 100, 0));
		textFieldFanOut.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldFanOut.setEditable(false);
		textFieldFanOut.setColumns(10);
		textFieldFanOut.setBorder(null);
		textFieldFanOut.setBackground((Color) null);
		textFieldFanOut.setBounds(660, 343, 56, 20);
		contentPane.add(textFieldFanOut);

		textFieldHL = new JTextField();
		textFieldHL.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldHL.setText("0");
		textFieldHL.setForeground(new Color(0, 100, 0));
		textFieldHL.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldHL.setEditable(false);
		textFieldHL.setColumns(10);
		textFieldHL.setBorder(null);
		textFieldHL.setBackground((Color) null);
		textFieldHL.setBounds(660, 365, 56, 20);
		contentPane.add(textFieldHL);

		textFieldHV = new JTextField();
		textFieldHV.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textFieldHV.setText("0");
		textFieldHV.setForeground(new Color(0, 100, 0));
		textFieldHV.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldHV.setEditable(false);
		textFieldHV.setColumns(10);
		textFieldHV.setBorder(null);
		textFieldHV.setBackground((Color) null);
		textFieldHV.setBounds(660, 390, 56, 20);
		contentPane.add(textFieldHV);
	

		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(filtroDir);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setSelectedFile(new File("C:/Users/Flor/workspace/Dummy/src/main"));
		listFiles.setListData(fc.getSelectedFile().listFiles(filtroJava));
	}
}
