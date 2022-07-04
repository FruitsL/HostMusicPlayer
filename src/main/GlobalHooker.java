package main;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class GlobalHooker {
	GlobalHooker() {
		LogManager.getLogManager().reset();
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(new KeyHooker());
	}

	class KeyHooker implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			int keyCode = e.getKeyCode();

			if (keyCode == 87) { // F11
				GUI.ms.play();
			} else if (keyCode == 88) { // F12
				GUI.ms.stop();
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void nativeKeyTyped(NativeKeyEvent arg0) {
			// TODO Auto-generated method stub
		}
	}
}
