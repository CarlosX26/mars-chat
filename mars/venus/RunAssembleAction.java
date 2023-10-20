package mars.venus;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import mars.ErrorList;
import mars.ErrorMessage;
import mars.Globals;
import mars.MIPSprogram;
import mars.ProcessingException;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.Memory;
import mars.mips.hardware.RegisterFile;
import mars.util.FilenameFinder;
import mars.util.SystemIO;

/*
 * Copyright (c) 2003-2010, Pete Sanderson and Kenneth Vollmar
 *
 * Developed by Pete Sanderson (psanderson@otterbein.edu) and Kenneth Vollmar
 * (kenvollmar@missouristate.edu)
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * (MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Action class for the Run -> Assemble menu item (and toolbar icon)
 */
public class RunAssembleAction extends GuiAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1219029937418143070L;
	private static ArrayList MIPSprogramsToAssemble;
	private static boolean extendedAssemblerEnabled;
	private static boolean warningsAreErrors;
	// Threshold for adding filename to printed message of files being assembled.
	private static final int LINE_LENGTH_LIMIT = 60;

	public RunAssembleAction(final String name, final Icon icon, final String descrip, final Integer mnemonic,
			final KeyStroke accel, final VenusUI gui) {
		super(name, icon, descrip, mnemonic, accel, gui);

	}

	// These are both used by RunResetAction to re-assemble under identical
	// conditions.
	static ArrayList getMIPSprogramsToAssemble() {
		return MIPSprogramsToAssemble;
	}

	static boolean getExtendedAssemblerEnabled() {
		return extendedAssemblerEnabled;
	}

	static boolean getWarningsAreErrors() {
		return warningsAreErrors;
	}

	// edited ('‿') @CJ@
	public void writeInFile(final String file, String str) {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuilder fileContents = new StringBuilder();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				fileContents.append(line);
				fileContents.append(System.lineSeparator());
			}
			bufferedReader.close();
			fileReader.close();

			fileContents.append(str);

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(fileContents.toString());
			bufferedWriter.close();
			fileWriter.close();

			mainUI.editor.close();
			mainUI.editor.open(file);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// edited ('‿') @CJ@
	public void readFile(final String file) {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				if (line.startsWith("#@chat")) {
					String input = line.split("-")[1];
					sendQuestionToGpt(input, file);
				}
			}
			bufferedReader.close();
			fileReader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// edited ('‿') @CJ@
	public void sendQuestionToGpt(String input, final String file) {
		try {
			String prompt = "Faça o seguinte codigo em assembly mips, retorne apenas o codigo, não quero explicações: "
					+ input;
			String url = "https://api.openai.com/v1/chat/completions";
			String apiKey = "";
			String model = "gpt-3.5-turbo";

			URL urlObj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "Bearer " + apiKey);
			connection.setRequestProperty("Content-Type", "application/json");

			// String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\":
			// \"user\", \"content\": \"" + prompt
			// + "\"}]}";
			String body = "{\n\t\"messages\": [\n\t\t{\n\t\t\t\"role\": \"user\",\n\t\t\t\"content\": \"" + prompt
					+ "\"\n\t\t}\n\t],\n\t\"temperature\": 1,\n\t\"max_tokens\": 256,\n\t\"top_p\": 1,\n\t\"frequency_penalty\": 0,\n\t\"presence_penalty\": 0,\n\t\"model\": \""
					+ model + "\",\n\t\"stream\": false\n}";

			connection.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();
			writer.close();

			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;

			StringBuffer response = new StringBuffer();

			while ((line = br.readLine()) != null) {
				response.append(line);
			}
			br.close();

			String extractedMessage = extractMessageFromJSONResponse(response.toString());
			System.out.println(extractedMessage);

			// writeInFile(file, "#batatinha");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// edited ('‿') @CJ@
	public static String extractMessageFromJSONResponse(String response) {
		int start = response.indexOf("content") + 11;

		int end = response.indexOf("\"", start);

		return response.substring(start, end);

	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final String name = getValue(Action.NAME).toString();
		mainUI.getMainPane().getEditPane();
		final ExecutePane executePane = mainUI.getMainPane().getExecutePane();
		final RegistersPane registersPane = mainUI.getRegistersPane();
		extendedAssemblerEnabled = Globals.getSettings().getExtendedAssemblerEnabled();
		warningsAreErrors = Globals.getSettings().getWarningsAreErrors();
		if (FileStatus.getFile() != null) {
			if (FileStatus.get() == FileStatus.EDITED) {
				mainUI.editor.save();
			}
			try {
				Globals.program = new MIPSprogram();
				ArrayList filesToAssemble;
				if (Globals.getSettings().getAssembleAllEnabled()) {// setting calls for multiple file assembly
					filesToAssemble = FilenameFinder.getFilenameList(new File(FileStatus.getName()).getParent(),
							Globals.fileExtensions);
				} else {
					filesToAssemble = new ArrayList();
					filesToAssemble.add(FileStatus.getName());
				}
				String exceptionHandler = null;

				if (Globals.getSettings().getExceptionHandlerEnabled() && Globals.getSettings()
						.getExceptionHandler() != null && Globals.getSettings().getExceptionHandler().length() > 0) {
					exceptionHandler = Globals.getSettings().getExceptionHandler();
				}

				MIPSprogramsToAssemble = Globals.program.prepareFilesForAssembly(filesToAssemble, FileStatus.getFile()
						.getPath(), exceptionHandler);

				// edited ('‿') @CJ@
				// System.out.println(filesToAssemble);
				String file = (String) filesToAssemble.get(0);
				readFile(file);

				mainUI.messagesPane.postMarsMessage(buildFileNameList(name + ": assembling ", MIPSprogramsToAssemble));
				// added logic to receive any warnings and output them.... DPS 11/28/06
				final ErrorList warnings = Globals.program.assemble(MIPSprogramsToAssemble, extendedAssemblerEnabled,
						warningsAreErrors);
				if (warnings.warningsOccurred()) {
					mainUI.messagesPane.postMarsMessage(warnings.generateWarningReport());
				}
				mainUI.messagesPane.postMarsMessage(name + ": operation completed successfully.\n\n");
				FileStatus.setAssembled(true);
				FileStatus.set(FileStatus.RUNNABLE);
				RegisterFile.resetRegisters();
				Coprocessor1.resetRegisters();
				Coprocessor0.resetRegisters();
				executePane.getTextSegmentWindow().setupTable();
				executePane.getDataSegmentWindow().setupTable();
				executePane.getDataSegmentWindow().highlightCellForAddress(Memory.dataBaseAddress);
				executePane.getDataSegmentWindow().clearHighlighting();
				executePane.getLabelsWindow().setupTable();
				executePane.getTextSegmentWindow().setCodeHighlighting(true);
				executePane.getTextSegmentWindow().highlightStepAtPC();
				registersPane.getRegistersWindow().clearWindow();
				registersPane.getCoprocessor1Window().clearWindow();
				registersPane.getCoprocessor0Window().clearWindow();
				VenusUI.setReset(true);
				VenusUI.setStarted(false);
				mainUI.getMainPane().setSelectedComponent(executePane);

				// Aug. 24, 2005 Ken Vollmar
				SystemIO.resetFiles(); // Ensure that I/O "file descriptors" are initialized for a new program run

			} catch (final ProcessingException pe) {
				final String errorReport = pe.errors().generateErrorAndWarningReport();
				mainUI.messagesPane.postMarsMessage(errorReport);
				mainUI.messagesPane.postMarsMessage(name + ": operation completed with errors.\n\n");
				// Select editor line containing first error, and corresponding error message.
				final ArrayList errorMessages = pe.errors().getErrorMessages();
				for (int i = 0; i < errorMessages.size(); i++) {
					final ErrorMessage em = (ErrorMessage) errorMessages.get(i);
					// No line or position may mean File Not Found (e.g. exception file). Don't try
					// to open. DPS 3-Oct-2010
					if (em.getLine() == 0 && em.getPosition() == 0) {
						continue;
					}
					if (!em.isWarning() || warningsAreErrors) {
						Globals.getGui().getMessagesPane().selectErrorMessage(em.getFilename(), em.getLine(), em
								.getPosition());
						// Bug workaround: Line selection does not work correctly for the JEditTextArea
						// editor
						// when the file is opened then automatically assembled (assemble-on-open
						// setting).
						// Automatic assemble happens in EditTabbedPane's openFile() method, by invoking
						// this method (actionPerformed) explicitly with null argument. Thus e!=null
						// test.
						// DPS 9-Aug-2010
						if (e != null) {
							Globals.getGui().getMessagesPane().selectEditorTextLine(em.getFilename(), em.getLine(), em
									.getPosition());
						}
						break;
					}
				}
				FileStatus.setAssembled(false);
				FileStatus.set(FileStatus.NOT_EDITED);
			}
		}
	}

	// Handy little utility for building comma-separated list of filenames
	// while not letting line length get out of hand.
	private String buildFileNameList(final String preamble, final ArrayList programList) {
		String result = preamble;
		int lineLength = result.length();
		for (int i = 0; i < programList.size(); i++) {
			final String filename = ((MIPSprogram) programList.get(i)).getFilename();
			result += filename + (i < programList.size() - 1 ? ", " : "");
			lineLength += filename.length();
			if (lineLength > LINE_LENGTH_LIMIT) {
				result += "\n";
				lineLength = 0;
			}
		}
		return result + (lineLength == 0 ? "" : "\n") + "\n";
	}
}
