package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;

public class MusicSystem {
	ArrayList<Mixer.Info> infoResult = new ArrayList<>();
	File file;
	int[] line = {0, 0};
	private boolean play = false;
	
	MusicSystem() {
		file = new File(System.getProperty("user.dir") + "/music.wav");
		getEnableAudio();
	}
	
	private void getEnableAudio() {
		ArrayList<String> results = new ArrayList<>();
		AudioInputStream stream = null;
		try {
			stream = AudioSystem.getAudioInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		AudioFormat format = stream.getFormat();
		DataLine.Info dlinfo = new DataLine.Info(SourceDataLine.class, format, 2048);
		Mixer.Info[] infoList = AudioSystem.getMixerInfo();
		Mixer mixer;
		
		for(int i = 0; i < infoList.length; i++) {
			mixer = (Mixer)AudioSystem.getMixer(infoList[i]);
			try {
				if(mixer.isLineSupported(dlinfo)) {
					infoResult.add(infoList[i]);
					results.add(infoList[i].getName());
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setLine(int num, int type) {
		line[num] = type;
	}
	
	public ArrayList<String> getinfoResult() {
		ArrayList<String> result = new ArrayList<>();
		for (Mixer.Info i : infoResult)
			result.add(i.getName());
		return result;
	}
	
	public File convertToMP3(File file) {
		File outputfile = new File(System.getProperty("user.dir") + "/music.wav");
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("pcm_s16le");
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("wav");
		attrs.setAudioAttributes(audio);
		Encoder encoder = new Encoder();
		try {
			encoder.encode(file, outputfile, attrs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputfile;
	}
	
	public void play() {
		if (play) return;
		play = true;
		long startTime = System.currentTimeMillis();
		
		for(int i = 0; i < 2; i++)
			new Timer().schedule(new MusicPlay(i, GUI.getVolume(i)), (startTime - System.currentTimeMillis()) + GUI.getStartDelay(i));
	}
	
	public void stop() {
		if (!play) return;
		play = false;
	}
	
	class MusicPlay extends TimerTask {
		int type = 0;
		float volume = 100.0f;
		Mixer.Info mInfo;
		SourceDataLine speaker;
		FloatControl gain;
		
		public MusicPlay(int type, int volume) {
			this.type = type;
			this.volume = volume > 100 ? 100.0f : (float)volume;
		}
		
		@Override
		public void run() {
			/* 해당 부분은 스레드 별로 선언하지 않으면 실행 시 문제 생김 */
			AudioInputStream stream = null;
			try {
				stream = AudioSystem.getAudioInputStream(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			AudioFormat format = stream.getFormat();
			DataLine.Info dlinfo = new DataLine.Info(SourceDataLine.class, format, 2048);
			
			mInfo = infoResult.get(line[type]);
			Mixer output = AudioSystem.getMixer(mInfo);
			speaker = null;
			try {
				speaker = (SourceDataLine)output.getLine(dlinfo);
				speaker.open(format, 8192);
				gain = (FloatControl)speaker.getControl(FloatControl.Type.MASTER_GAIN);
				gain.setValue(20.0f * (float)Math.log10(volume / 100.0f));
				speaker.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			byte[] buffer = new byte[2048];
			int bytesRead = 0;
			while(bytesRead != -1 && GUI.ms.play) {
				try {
					bytesRead = stream.read(buffer, 0, buffer.length);
					if(bytesRead >= 0)
						speaker.write(buffer, 0, bytesRead);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			GUI.ms.play = false;
		}
	}
}
