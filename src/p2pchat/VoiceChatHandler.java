/*
 * P2PChat - Peer-to-Peer Chat Application
 *
 * Copyright (c) 2014 Ahmed Samy  <f.fallen45@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package p2pchat;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class VoiceChatHandler implements Runnable {
	private TargetDataLine input;
	private SourceDataLine output;

	private final AtomicInteger running = new AtomicInteger();

	public VoiceChatHandler()
	{
		input  = null;
		output = null;
	}

	public boolean isSatisified()
	{
		return input != null && output != null;
	}

	public static Map getSourcesAvailable()
	{
		Map ret = new HashMap();

		Mixer.Info[] mixerInformation = AudioSystem.getMixerInfo();
		for (Mixer.Info info : mixerInformation) {
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info[] sourceLinesInfo = mixer.getSourceLineInfo();

			for (Line.Info sourceLineInfo : sourceLinesInfo) {
				if (sourceLineInfo instanceof DataLine.Info) {
					try (Line line = AudioSystem.getLine(sourceLineInfo)) {
						ret.put(info.getDescription(), line);
					} catch (LineUnavailableException e) {
						;
					}
				}
			}
		}

		return ret;
	}

	public static Map getTargetsAvailable()
	{
		Map ret = new HashMap();

		Mixer.Info[] mixerInformation = AudioSystem.getMixerInfo();
		for (Mixer.Info info : mixerInformation) {
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info[] targetLinesInfo = mixer.getSourceLineInfo();

			for (Line.Info targetLineInfo : targetLinesInfo) {
				if (targetLineInfo instanceof DataLine.Info) {
					try (Line line = AudioSystem.getLine(targetLineInfo)) {
						ret.put(info.getDescription(), line);
					} catch (LineUnavailableException e) {
						;
					}
				}
			}
		}

		return ret;
	}

	public void setInput(Line line)
	{
		input = (TargetDataLine) line;
		if (input != null) {
			try {
				input.open(new AudioFormat(8000.0f, 16, 1, true, true));
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

	public void setOutput(Line line)
	{
		output = (SourceDataLine) line;
		if (output != null) {
			try {
				output.open(new AudioFormat(8000.0f, 16, 1, true, true));
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

	public void toggleCapture()
	{
		if (running.get() == 0)
			running.set(1);
		else
			running.set(0);
	}

	public void startCapture()
	{
		if (running.get() == 0)
			running.set(1);
	}

	public void stopCapture()
	{
		if (running.get() == 1)
			running.set(0);
	}

	public boolean isCapturing()
	{
		return running.get() == 1;
	}

	public void feedData(byte[] data, int count)
	{
		if (output != null)
			output.write(data, 0, count);
	}

	@Override
	public void run()
	{
		while (true) {
			if (running.get() == 0) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ex) {
					;
				}
			}

			// Signaled, retrieve data if any.
			int available = input.available();
			if (available > 0) {
				byte data[] = new byte[available];
				int count = input.read(data, 0, available);

				P2PChat.get().transmitVoice(data, count);
			}
		}
	}
}
