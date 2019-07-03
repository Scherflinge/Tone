import javax.sound.sampled.AudioFormat;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javax.sound.midi.*;

import java.awt.Button;
import java.awt.Label;
import java.awt.TextField;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ToneGame {

	private static LinkedList<Integer> lastPlayed;

	public static void main(String[] args)
			throws LineUnavailableException, FileNotFoundException, MidiUnavailableException, InterruptedException {

		Scanner input = new Scanner(System.in);
		Synthesizer syn = MidiSystem.getSynthesizer();
		syn.open();
		final MidiChannel[] mc = syn.getChannels();
		Instrument[] instr = syn.getDefaultSoundbank().getInstruments();
		syn.loadInstrument(instr[0]);
		boolean exit = false;
		while (exit == false) {
			System.out.println(
					"\nType \"key\" for the Chord Key Game\nType \"chord\" for the Chord Guessing Game\nType \"library\" for the Chord Library\nType \"song FILE\" to play a song from a file");
			String myguess = input.next();
			if (myguess.toUpperCase().equals("CHORD")) {
				guessTheChord(input, mc);
			} else if (myguess.toUpperCase().equals("KEY")) {
				guessChordKey(input, mc);
			} else if (myguess.toUpperCase().equals("LIBRARY")) {
				LinkedList<Integer> currentChord = null;
				while (input.hasNext()) {
					String g = input.next();
					if (g.toUpperCase().equals("EXIT")) {
						break;
					}
					else if(g.toUpperCase().equals("NOTES")){
						System.out.println("The Notes in this chord are "+chordNotes(currentChord));
					}
					else{
					currentChord = makeChord(g);
					playNotes(currentChord, mc);
					}
				}
			} else if (myguess.toUpperCase().equals("SONG")) {
				String FileName = input.next();
				input = new Scanner(new File(FileName));
				while (input.hasNext()) {
					String g = input.next();
					playNotes(makeChord(g), mc);
					Thread.sleep(1400);
				}
			} else if (myguess.toUpperCase().equals("exit")) {
				exit = true;
			} else {
				System.out.println("\nI didn't understand.\n");
			}
		}

	}

	private static String chordNotes(LinkedList<Integer> currentChord) {
		String[] j = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
		String notes = "";
		LinkedList<String> usedNotes = new LinkedList<>();
		for(int i:currentChord){
			String currentNote=j[i%12];
			if(!usedNotes.contains(currentNote)){
				notes= notes+currentNote +" ";
				usedNotes.add(currentNote);
			}
		}
		return notes;
	}

	public static void guessChordKey(Scanner input, MidiChannel[] mc)
			throws LineUnavailableException, InterruptedException {

		Random rn = new Random();
		String ChordToGuess = "";
		int ctr = 0;
		int chord = 4;
		boolean hard = false;
		boolean down = false;
		boolean Sharp = true;

		int root = rn.nextInt(12);
		int[] ChordPlace = { 0, 0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17 };
		int[] NegativeChordPlace = { 0 + 12, -12 + 12, -10 + 12, -8 + 12, -7 + 12, -5 + 12, -3 + 12, -1 + 12, 0 + 12,
				2 + 12, 4 + 12, 5 + 12 };
		Note[] BaseNotes = { Note.A3, Note.A3$, Note.B3, Note.C3, Note.C3$, Note.D3, Note.D3$, Note.E3, Note.F3,
				Note.F3$, Note.G3, Note.G3$, Note.A4, Note.A4$, Note.B4, Note.C4, Note.C4$, Note.D4, Note.D4$, Note.E4,
				Note.F4, Note.F4$, Note.G4, Note.G4$, Note.A5, Note.A5$, Note.B5, Note.C5, Note.C5$, Note.D5, Note.D5$,
				Note.E5, Note.F5, Note.F5$, Note.G5, Note.G5$, Note.A6, Note.A6$, Note.B6, Note.C6, Note.C6$ };

		String[] ChordSharps = { "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" };
		// A#b Bb Cb C#b Db D#b Eb Fb F#b Gb G#b Ab
		// 0 1 2 3 4 5 6 7 8 9 10 11
		String[] MajorChords = { "", "MAJ", "MIN", "MIN", "MAJ", "MAJ", "MIN", "DIM", "MAJ" };
		String[] MinorChords = { "", "MIN", "DIM", "MAJ", "MAJ", "MIN", "MIN", "MAJ", "MAJ" };
		String[] ChordFlats = { "A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab" };

		System.out.println("Easy or Hard mode?");
		if (input.next().toUpperCase().equals("HARD")) {
			hard = true;
		}
		boolean end = false;

		while (end == false) {
			int k = root + ChordPlace[chord];
			if (k > 11) {
				k -= 12;
			}
			if (hard) {
				switch (root) {
				case 1:
				case 4:
				case 6:
				case 11:
					Sharp = false;
					break;
				default:
					Sharp = true;
				}
			}

			if (Sharp) {
				ChordToGuess = ChordSharps[k] + MajorChords[chord];
			} else {
				ChordToGuess = ChordFlats[k] + MajorChords[chord];
			}
			String RootName = "";
			if (Sharp) {
				RootName = ChordSharps[root] + "maj";
			} else {
				RootName = ChordFlats[root] + "maj";
			}

			if (hard) {
				System.out.println("Guess what the second chord is. The first chord was a " + RootName + ".");
			} else {
				System.out.println("Guess which chord in the key this is.");
			}

			playNotes(makeChord(RootName), mc);

			Thread.sleep(1000);
			if (down == true) {
				playNotes(makeChord("-" + ChordToGuess), mc);
			} else {
				playNotes(makeChord(ChordToGuess), mc);
			}
			// for(int i=0;i<6;i+=2){
			// playNotes(BaseNotes[root+chordplace[i+1]],mc);
			// }

			// int ct = 0;
			// for(int i=0;i<6;i+=2){
			// if(!down){
			// guess[ct] = BaseNotes[root+ChordPlace[chord+i]];
			// }
			// else{
			// guess[ct] = BaseNotes[root+NegativeChordPlace[chord+i]];
			// }
			// ct++;
			// }
			//
			// playNotes(guess,mc);
			String myguess = input.next();
			if (myguess.toUpperCase().equals("EXIT")) {
				return;
			} else if (myguess.equals("" + chord) && hard == false) {
				System.out.println("Correct.");
				root = rn.nextInt(12);
				chord = rn.nextInt(6) + 2;
				ctr = 0;
				down = false;
				if (rn.nextBoolean()) {
					down = true;
				}
			} else if (myguess.equals("" + chord) && hard == true) {
				System.out.println("But what is the chords name?");
				ctr++;
			} else if (myguess.toUpperCase().equals(ChordToGuess.toUpperCase()) && hard == true) {
				System.out.println("Correct.");
				root = rn.nextInt(12);
				chord = rn.nextInt(6) + 2;
				ctr = 0;
				down = false;
				if (rn.nextBoolean()) {
					down = true;
				}
			} else if (myguess.toUpperCase().equals("END")) {
				end = true;
			} else if (myguess.equals("r")) {

			} else {
				System.out.println("Incorrect.");
				ctr++;
			}
			if (ctr == 3 && hard == true) {
				System.out.println("Wrong, it was " + ChordToGuess);
				root = rn.nextInt(12);
				chord = rn.nextInt(6) + 2;
				down = false;
				if (rn.nextBoolean()) {
					down = true;
				}
				ctr = 0;
			} else if (ctr == 3) {
				System.out.println("Wrong, it was " + chord);
				root = rn.nextInt(12);
				chord = rn.nextInt(6) + 2;
				down = false;
				if (rn.nextBoolean()) {
					down = true;
				}
				ctr = 0;
			}
		}
	}

	public static void guessTheChord(Scanner input, MidiChannel[] mc)
			throws LineUnavailableException, InterruptedException {
		boolean end = false;
		String[] chords;
		String[] notes = { "A", "AB", "A#", "B", "BB", "C", "C#", "D", "DB", "D#", "E", "EB", "F", "F#", "G", "GB",
				"G#" };
		// String[] notes = {"C"};
		Random rn = new Random();
		String playChord = "MAJ";
		String playNote = "C";
		int ctr = 0;
		System.out.println("Enter difficulty, easy, medium, or hard");
		String response = input.next();
		if (response.toUpperCase().equals("MEDIUM")) {
			chords = new String[] { "MAJ", "MIN", "MAJ7", "MIN7", "7" };
		} else if (response.toUpperCase().equals("HARD")) {
			chords = new String[] { "MAJ", "MIN", "MAJ7", "MIN7", "7", "AUG", "DIM", "SUS4", "SUS2" };
		} else if (response.toUpperCase().equals("WICKEDHARD")) {
			chords = new String[] { "MAJ", "MIN", "MAJ7", "MIN7", "7", "AUG", "DIM", "SUS4", "SUS2", "6", "5", "MIN6" };
		} else {
			chords = new String[] { "MAJ", "MIN" };
		}
		while (end == false) {
			System.out.print("Guess this chord, the root is " + playNote + ". ");
			playNotes(makeChord(playNote + playChord), mc);

			response = input.next();
			response = response.toUpperCase();
			if (response.equals("EXIT")) {
				return;
			} else if (response.equals(playNote + playChord)) {
				System.out.println("Correct!");
				playNotes(Note.A4, mc);
				playNotes(Note.E4, mc);
				Thread.sleep(1000);
				playChord = chords[rn.nextInt(chords.length)];
				playNote = notes[rn.nextInt(notes.length)];
				ctr = 0;
			} else if (response.equals("R")) {

			} else if (ctr != 3) {
				System.out
						.println("Incorrect. \nHere is the chord you guessed compared to the chord you need to guess.");
				// playNotes(makeChord("WRONG"),mc);
				Thread.sleep(1000);
				playNotes(makeChord(response), mc);
				Thread.sleep(1000);
				ctr++;
			} else {

				System.out.println("Incorrect, the chord was " + playNote + playChord);
				// playNotes(makeChord("WRONG"),mc);
				Thread.sleep(1000);
				playNotes(makeChord(playNote + playChord), mc);
				playChord = chords[rn.nextInt(chords.length)];
				playNote = notes[rn.nextInt(notes.length)];
				ctr = 0;
				Thread.sleep(1000);
			}
		}
		input.close();
		System.exit(0);
	}

	public static void playNotes(Note j, MidiChannel[] mc) throws InterruptedException, LineUnavailableException {
		LinkedList<Integer> f = new LinkedList<>();
		f.add(j.getNote());
		playNotes(f, mc);
	}

	public static void playNotes(LinkedList<Integer> j, MidiChannel[] mc) throws LineUnavailableException, InterruptedException {
		for (int i = 0; i < 88; i++) {
			mc[0].noteOff(i);
		}
		if(lastPlayed != null){
			for (int i = 0; i < lastPlayed.size(); i++) {
				mc[0].noteOff(lastPlayed.get(i));
			}	
		}
		for (int i = 0; i < j.size(); i++) {
			mc[0].noteOn(j.get(i), 300);
		}
		lastPlayed = j;
		//Thread.sleep(300);
	}

	// public static void play(SourceDataLine line, Note note, int ms) {
	// ms = Math.min(ms, Note.SECONDS * 1000);
	// int length = Note.SAMPLE_RATE * ms / 1000;
	// line.write(note.data(), 0, length);
	// }

	public static LinkedList<Integer> makeChord(String ChordName) {
		boolean down = false;
		if (ChordName.charAt(0) == '-') {
			down = true;
			ChordName = ChordName.substring(1);
		}
		ChordName = ChordName.toUpperCase();

		// this grabs the first chord name, and separates the chord part from
		// the bass
		String StartNoteName = "" + ChordName.charAt(0);
		if (ChordName.length() != 0) {
			ChordName = ChordName.substring(1);
		} else {
			ChordName = "";
		}
		// this checks if theres a sharp or flat
		if (ChordName.length() != 0 && (ChordName.charAt(0) == '#' || ChordName.charAt(0) == 'B')) {
			StartNoteName += ChordName.charAt(0);
			ChordName = ChordName.substring(1);
		}
		// this checks is theres a bass change
		String Bass = StartNoteName;
		if (ChordName.contains("/")) {
			Bass = ChordName.substring(ChordName.indexOf('/') + 1, ChordName.length());
			if (Bass.length() != ChordName.length() - 1) {
				ChordName = ChordName.substring(0, ChordName.length() - (1 + Bass.length()));
			} else {
				ChordName = "";
			}
		}
		// this adds maj to the end of the chord if the chord is just the name
		if (ChordName.length() == 0) {
			ChordName += "MAJ";
		}
		//Middle C is 60
		int middleC = 60;
		if (down) {
			middleC-=12;
		}
		LinkedList<Integer> chord = new LinkedList<>();

		Map<String, Integer> NameToNumber = new HashMap<String, Integer>();

		String[] j = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
		// C#b Db D#b Eb Fb F#b Gb G#b Ab A#b Bb Cb 
		// 0    1  2  3   4  5   6  7  8   9 10  11
		int ctr = 0;
		for (String x : j) {
			NameToNumber.put(x, ctr);
			ctr++;
		}
		NameToNumber.put("AB", 8);
		NameToNumber.put("BB", 10);
		NameToNumber.put("CB", 11);
		NameToNumber.put("DB", 1);
		NameToNumber.put("EB", 3);
		NameToNumber.put("FB", 4);
		NameToNumber.put("GB", 6);

		int startNote;
		int bassNote;
		try {
			startNote = NameToNumber.get(StartNoteName);
			bassNote = NameToNumber.get(Bass);
			if(bassNote>startNote){
				bassNote-=12;
			}
		} catch (NullPointerException ex) {
			LinkedList<Integer> BadName = new LinkedList<>();
			BadName.add(60);
			BadName.add(61);
			return BadName;
		}
		bassNote+=middleC;
		startNote+=middleC;
		// System.out.println("My root is " + StartNoteName+ ", my bass is " +
		// Bass + " and my chord is "+ ChordName);
		chord.add(bassNote);
		chord.add(bassNote-12);
			
		switch (ChordName) {
		case "MAJ":
			chord.add(startNote + 4);
			chord.add(startNote + 7);
			chord.add(startNote + 12);
			break;
		case "MIN":
		case "M":
			chord.add(startNote + 3);
			chord.add(startNote + 7);
			chord.add(startNote + 12);
			break;
		case "MIN7":
			chord.add(startNote + 3);
			chord.add(startNote + 7);
			chord.add(startNote + 10);
			break;
		case "MAJ7":
			chord.add(startNote + 4);
			chord.add(startNote + 7);
			chord.add(startNote + 11);
			break;
		case "5":
			chord.add(startNote + 7);
			chord.add(startNote + 12);
			break;
		case "6":
			chord.add(startNote + 4);
			chord.add(startNote + 7);
			chord.add(startNote + 9);
			break;
		case "7":
			chord.add(startNote + 4);
			chord.add(startNote + 7);
			chord.add(startNote + 10);
			break;
		case "9":
			chord.add(startNote + 4);
			chord.add(startNote + 7);
			chord.add(startNote + 10);
			chord.add(startNote + 14);
			break;
		case "11":
			chord.add(startNote + 4);
			chord.add(startNote + 7);
			chord.add(startNote + 10);
			chord.add(startNote + 14);
			chord.add(startNote + 17);
			break;
		case "SUS":
		case "SUS4":
			chord.add(startNote + 5);
			chord.add(startNote + 7);
			chord.add(startNote + 12);
			break;
		case "SUS2":
			chord.add(startNote + 2);
			chord.add(startNote + 7);
			chord.add(startNote + 12);
			break;
		case "AUGMENTED":
		case "AUG":
			chord.add(startNote + 4);
			chord.add(startNote + 8);
			chord.add(startNote + 12);
			break;
		case "DIM":
		case "DIMINISHED":
			chord.add(startNote + 3);
			chord.add(startNote + 6);
			chord.add(startNote + 12);
			break;
		case "DIM7":
		case "DIMINISHED7":
			chord.add(startNote + 3);
			chord.add(startNote + 6);
			chord.add(startNote + 9);
			break;
		case "HALFDIM7":
		case "HALFDIMINISHED7":
			chord.add(startNote + 3);
			chord.add(startNote + 6);
			chord.add(startNote + 10);
			break;
		case "MIN6":
			chord.add(startNote + 3);
			chord.add(startNote + 7);
			chord.add(startNote + 9);
			break;
		case "MIN9":
			chord.add(startNote + 3);
			chord.add(startNote + 7);
			chord.add(startNote + 14);
			break;
		default:
			System.out.println("I don't recognize " + ChordName + " as a chord");
			chord.clear();
			chord.add(60);
			chord.add(61);
		}
		return chord;
	}

	enum Note {

		REST, A3, A3$, B3, C3, C3$, D3, D3$, E3, F3, F3$, G3, G3$, A4, A4$, B4, C4, C4$, D4, D4$, E4, F4, F4$, G4, G4$, A5, A5$, B5, C5, C5$, D5, D5$, E5, F5, F5$, G5, G5$, A6, A6$, B6, C6, C6$;

		private int Notenum;

		Note() {
			int n = this.ordinal();
			if (n > 0) {
				Notenum = this.ordinal() + 44;
			}
		}

		public int getNote() {
			return Notenum;
		}

		public void changePitch(int pitchesToChange) {
			Notenum += pitchesToChange;
		}

	}
}