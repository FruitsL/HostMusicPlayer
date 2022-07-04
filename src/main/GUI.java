package main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class GUI {
	static GlobalHooker gh;
	static MusicSystem ms;
	static String saveFile_Path = System.getProperty("user.dir") + "/setting.ini";
	
	private int language;
	private String support_language[] = {"en", "ko"};
	private String labelText[][] = {{"Host's Music Player", "Music name", "Drag and drop music file (support format : wav, mp3)", "Output ", "Start delay", "Volume", "Start (F11)", "Stop (F12)"},
									{"방장의 음악 재생", "음악 이름", "음악 파일을 드래그하여 불러오세요 (지원 포맷 : wav, mp3)", "출력 ", "시작 딜레이", "볼륨", "재생 (F11)", "정지 (F12)"}};
	private String filePath, fileType, fileName;
	private ArrayList<String> musicType = new ArrayList<>(List.of("wav", "mp3"));
	private ArrayList<String> audio;
	private String fontName[] = {"Consolas", "굴림"};
	static File settingFile;
	
	private JFrame frame;
	static JTextField startDelay_TextField[] = new JTextField[2];
	static JTextField volume_TextField[] = new JTextField[2];
	private Font boldFont;
	private Font plainFont;
	
	JPanel panel;
	JLabel fileName_Label, file_Label;
	JLabel output_Label[] = new JLabel[2];
	JLabel startDelay_Label[] = new JLabel[2];
	JLabel ms_Label[] = new JLabel[2];
	JLabel volume_Label[] = new JLabel[2];
	JLabel percent_Label[] = new JLabel[2];
	JComboBox output_ComboBox[] = new JComboBox[2];
	JButton startButton, stopButton;
	
	public static int getStartDelay(int n) {
		try {
			return Integer.parseInt(startDelay_TextField[n].getText().toString());
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static int getVolume(int n) {
		try {
			return Integer.parseInt(volume_TextField[n].getText().toString());
		} catch (Exception e) {
			return 100;
		}
	}
	
	public int getOutputIndex(int n) {
		return output_ComboBox[n].getSelectedIndex();
	}
	
	public String getOutputString(int n) {
		return output_ComboBox[n].getSelectedItem().toString();
	}
	
	public String getTextFieldData() {
		return output_ComboBox[0].getSelectedIndex() + "#" +
				output_ComboBox[1].getSelectedIndex() + "#" +
				startDelay_TextField[0].getText() + "#" +
				startDelay_TextField[1].getText() + "#" +
				volume_TextField[0].getText() + "#" +
				volume_TextField[1].getText();
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// 필요한 객체 불러오기
		gh = new GlobalHooker();
		ms = new MusicSystem();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		// 언어 확인
		language = Arrays.asList(support_language).indexOf(System.getProperty("user.language"));
		language = language < 0 ? 0 : language;
		
		audio = ms.getinfoResult();
		boldFont = new Font(fontName[language], Font.BOLD, 20);
		plainFont = new Font(fontName[language], Font.PLAIN, 20);
		
		initialize();
		getSetting();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(){
		frame = new JFrame(labelText[language][0]);
		frame.getContentPane().setFont(plainFont);
		frame.setBounds(100, 100, 900, 490);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);
		
		WindowAdapter adapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				FileWriter file;
				
				try {
					file = new FileWriter(saveFile_Path, false);
					StringBuilder sb = new StringBuilder(getTextFieldData());
					file.write(sb.toString());
					file.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		};
		frame.addWindowListener(adapter);
		
		panel = new JPanel();
		panel.setBounds(0, 0, 884, 451);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		new FileDrop(panel, new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				try {
					filePath = files[0].getCanonicalFile().toString().replace("\\", "/");
				} catch (IOException e) {
					e.printStackTrace();
				}
				fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
				fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
				if(musicType.contains(fileType)) {
					if(fileType.equals("mp3"))
						ms.setFile(ms.convertToMP3(files[0]));
					else
						ms.setFile(files[0]);
					file_Label.setText(fileName);
				} else {
					file_Label.setText(labelText[language][2]);
				}
			}
		});
		
		fileName_Label = new JLabel(labelText[language][1]);
		fileName_Label.setBounds(0, 30, 130, 40);
		fileName_Label.setHorizontalAlignment(SwingConstants.RIGHT);
		fileName_Label.setFont(boldFont);
		panel.add(fileName_Label);
		
		file_Label = new JLabel(labelText[language][2]);
		file_Label.setBounds(150, 30, 700, 40);
		file_Label.setOpaque(true);
		file_Label.setBackground(Color.WHITE);
		file_Label.setFont(plainFont);
		panel.add(file_Label);
		
		for(int i = 0; i < 2; i++) {
			output_Label[i] = new JLabel(labelText[language][3] + (i+1));
			output_Label[i].setFont(boldFont);
			output_Label[i].setHorizontalAlignment(SwingConstants.RIGHT);
			output_Label[i].setBounds(0, i == 0 ? 100 : 240, 130, 40);
			panel.add(output_Label[i]);
			
			output_ComboBox[i] = new JComboBox();
			output_ComboBox[i].setBounds(150, i == 0 ? 100 : 240, 700, 40);
			output_ComboBox[i].setFont(plainFont);
			panel.add(output_ComboBox[i]);
			// 지원되는 사운드 장치 List로 추가
			for(String s : audio)
				output_ComboBox[i].addItem(s);
			
			startDelay_Label[i] = new JLabel(labelText[language][4]);
			startDelay_Label[i].setFont(boldFont);
			startDelay_Label[i].setHorizontalAlignment(SwingConstants.RIGHT);
			startDelay_Label[i].setBounds(0, i == 0 ? 170 : 310, 130, 40);
			panel.add(startDelay_Label[i]);
			
			startDelay_TextField[i] = new JTextField();
			startDelay_TextField[i].setText("1000");
			startDelay_TextField[i].setHorizontalAlignment(SwingConstants.RIGHT);
			startDelay_TextField[i].setFont(boldFont);
			startDelay_TextField[i].setBounds(150, i == 0 ? 170 : 310, 200, 40);
			panel.add(startDelay_TextField[i]);
			startDelay_TextField[i].setColumns(10);
			
			startDelay_TextField[i].addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					char c = e.getKeyChar();
					if( ((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
						e.consume();
					}
				}
			});
			
			ms_Label[i] = new JLabel("ms");
			ms_Label[i].setFont(plainFont);
			ms_Label[i].setBounds(360, i == 0 ? 170 : 310, 100, 40);
			panel.add(ms_Label[i]);
			
			volume_Label[i] = new JLabel(labelText[language][5]);
			volume_Label[i].setFont(boldFont);
			volume_Label[i].setHorizontalAlignment(SwingConstants.RIGHT);
			volume_Label[i].setBounds(400, i == 0 ? 170 : 310, 130, 40);
			panel.add(volume_Label[i]);
			
			volume_TextField[i] = new JTextField();
			volume_TextField[i].setText("100");
			volume_TextField[i].setHorizontalAlignment(SwingConstants.RIGHT);
			volume_TextField[i].setFont(plainFont);
			volume_TextField[i].setBounds(550, i == 0 ? 170 : 310, 200, 40);
			panel.add(volume_TextField[i]);
			volume_TextField[i].setColumns(10);
			
			volume_TextField[i].addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					char c = e.getKeyChar();
					if( ((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
						e.consume();
					}
				}
			});
			
			percent_Label[i] = new JLabel("%");
			percent_Label[i].setFont(plainFont);
			percent_Label[i].setHorizontalAlignment(SwingConstants.LEFT);
			percent_Label[i].setBounds(760, i == 0 ? 170 : 310, 100, 40);
			panel.add(percent_Label[i]);
		}
		
		startButton = new JButton(labelText[language][6]);
		startButton.setBounds(250, 380, 150, 40);
		startButton.setFont(boldFont);
		panel.add(startButton);
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ms.play();
			}
		});
		
		stopButton = new JButton(labelText[language][7]);
		stopButton.setBounds(500, 380, 150, 40);
		stopButton.setFont(boldFont);
		panel.add(stopButton);
		
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ms.stop();
			}
		});
		
		// 선택한 ComboBox index 확인
		output_ComboBox[0].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ms.setLine(0, output_ComboBox[0].getSelectedIndex());
			}
		});
		output_ComboBox[1].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ms.setLine(1, output_ComboBox[1].getSelectedIndex());
			}
		});
	}

	private void getSetting() {
		BufferedReader br = null;
		int[] setting = {-1, -1, 1000, 2420, 100, 100};
		settingFile = new File(saveFile_Path);
		
		try {
			if(!settingFile.createNewFile()) { // 파일 존재
				try {
					br = new BufferedReader(new FileReader(settingFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				String s;
				for(int line=0; (s = br.readLine()) != null; line++)
					setting = Arrays.stream(s.split("#")).mapToInt(Integer::parseInt).toArray();
			}
		} catch(IOException e) {}

		/* 불러온 데이터 반영 */
		for(int i = 0; i < 2; i++) {
			if(setting[i] >= 0) {
				ms.setLine(i, setting[i]);
				output_ComboBox[i].setSelectedIndex(setting[i]);
			}
			startDelay_TextField[i].setText(Integer.toString(setting[i + 2]));
			volume_TextField[i].setText(Integer.toString(setting[i + 4]));
		}
	}
}
