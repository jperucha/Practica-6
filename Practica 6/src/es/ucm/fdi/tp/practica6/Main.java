package es.ucm.fdi.tp.practica6;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import es.ucm.fdi.tp.basecode.bgame.control.ConsoleCtrl;
import es.ucm.fdi.tp.basecode.bgame.control.ConsoleCtrlMVC;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.AIAlgorithm;
import es.ucm.fdi.tp.basecode.bgame.model.Game;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.minmax.MinMax;
import es.ucm.fdi.tp.practica6.ataxx.AtaxxFactoryExt;
import es.ucm.fdi.tp.practica6.attt.AdvancedTTTFactoryExt;
import es.ucm.fdi.tp.practica6.connectn.ConnectNFactoryExt;
import es.ucm.fdi.tp.practica6.ttt.TicTacToeFactoryExt;
import es.ucm.fdi.tp.practica6.control.GameClient;
import es.ucm.fdi.tp.practica6.control.GameServer;

/**
 * This is the class with the main method for the board games application.
 * 
 * It uses the Commons-CLI library for parsing command-line arguments: the game
 * to play, the players list, etc.. More information is available at
 * {@link https://commons.apache.org/proper/commons-cli/}
 * 
 * <p>
 * Esta es la clase con el metodo main de inicio del programa. Se utiliza la
 * libreria Commons-CLI para leer argumentos de la linea de ordenes: el juego al
 * que se quiere jugar y la lista de jugadores. Puedes encontrar mas información
 * sobre esta libreria en {@link https://commons.apache.org/proper/commons-cli/}
 * .
 */
public class Main {

	/**
	 * The possible views.
	 * <p>
	 * Vistas disponibles.
	 */
	enum ViewInfo {
		WINDOW("window", "Swing"), CONSOLE("console", "Console");

		private String id;
		private String desc;

		ViewInfo(String id, String desc) {
			this.id = id;
			this.desc = desc;
		}

		public String getId() {
			return id;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return id;
		}
	}

	/**
	 * The available games.
	 * <p>
	 * Juegos disponibles.
	 */
	enum GameInfo {
		CONNECTN("cn", "ConnectN"), TicTacToe("ttt", "Tic-Tac-Toe"), AdvancedTicTacToe("attt", "Advanced Tic-Tac-Toe"), Ataxx("ataxx", "Ataxx");

		private String id;
		private String desc;

		GameInfo(String id, String desc) {
			this.id = id;
			this.desc = desc;
		}

		public String getId() {
			return id;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return id;
		}

	}

	/**
	 * Player modes (manual, random, etc.)
	 * <p>
	 * Modos de juego.
	 */
	public enum PlayerMode {
		MANUAL("m", "Manual"), RANDOM("r", "Random"), AI("a", "Intelligent");

		private String id;
		private String desc;

		PlayerMode(String id, String desc) {
			this.id = id;
			this.desc = desc;
		}

		public String getId() {
			return id;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return desc;
		}
	}
	
	enum ApplicationMode{
		 NORMAL("normal", "Normal"), CLIENT("client", "Client"), SERVER("server", "Server");
		 
		private String id;
		private String desc;

		ApplicationMode(String id, String desc) {
			this.id = id;
			this.desc = desc;
		}

		public String getId() {
			return id;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return desc;
		}
	}
	
	/**
	 * Algorithms for automatic players. The 'none' option means that the
	 * default behavior is used (i.e., a player that waits for some time and
	 * then generates a random move)
	 * 
	 */
	private enum AlgorithmForAIPlayer {
		NONE("none", "No AI Algorithm"), MINMAX("minmax", "MinMax"), MINMAXAB("minmaxab",
				"MinMax with Alhpa-Beta Prunning");

		private String id;
		private String desc;

		AlgorithmForAIPlayer(String id, String desc) {
			this.id = id;
			this.desc = desc;
		}

		public String getId() {
			return id;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString() {
			return desc;
		}
	}

	
	
	/**
	 * Default game to play.
	 * <p>
	 * Juego por defecto.
	 */
	final private static GameInfo DEFAULT_GAME = GameInfo.CONNECTN;

	/**
	 * default view to use.
	 * <p>
	 * Vista por defecto.
	 */
	final private static ViewInfo DEFAULT_VIEW = ViewInfo.WINDOW;

	/**
	 * Default player mode to use.
	 * <p>
	 * Modo de juego por defecto.
	 */
	final private static PlayerMode DEFAULT_PLAYERMODE = PlayerMode.MANUAL;
	
	final private static ApplicationMode DEFAULT_APPLICATIONMODE = ApplicationMode.NORMAL;
	
	/**
	 * Used as string instead of int so that the {@link CommandLine.getOptionValue()} can use it as the 2nd argument
	 */
	final private static String DEFAULT_SERVERPORT = "2000";
	
	final private static String DEFAULT_SERVERHOST = "localhost";
	
	/**
	 * Default algorithm for automatic player.
	 */
	final private static AlgorithmForAIPlayer DEFAULT_AIALG = AlgorithmForAIPlayer.NONE;

	/**
	 * This field includes a game factory that is constructed after parsing the
	 * command-line arguments. Depending on the game selected with the -g option
	 * (by default {@link #DEFAULT_GAME}).
	 * 
	 * <p>
	 * Este atributo incluye una factoria de juego que se crea despues de
	 * extraer los argumentos de la linea de ordenes. Depende del juego
	 * seleccionado con la opcion -g (por defecto, {@link #DEFAULT_GAME}).
	 */
	private static GameFactory gameFactory;

	/**
	 * List of pieces provided with the -p option, or taken from
	 * {@link GameFactory#createDefaultPieces()} if this option was not
	 * provided.
	 * 
	 * <p>
	 * Lista de fichas proporcionadas con la opcion -p, u obtenidas de
	 * {@link GameFactory#createDefaultPieces()} si no hay opcion -p.
	 */
	private static List<Piece> pieces;

	/**
	 * A list of players. The i-th player corresponds to the i-th piece in the
	 * list {@link #pieces}. They correspond to what is provided in the -p
	 * option (or using the default value {@link #DEFAULT_PLAYERMODE}).
	 * 
	 * <p>
	 * Lista de jugadores. El jugador i-esimo corresponde con la ficha i-esima
	 * de la lista {@link #pieces}. Esta lista contiene lo que se proporciona en
	 * la opcion -p (o el valor por defecto {@link #DEFAULT_PLAYERMODE}).
	 */
	private static List<PlayerMode> playerModes;

	/**
	 * The view to use. Depending on the selected view using the -v option or
	 * the default value {@link #DEFAULT_VIEW} if this option was not provided.
	 * 
	 * <p>
	 * Vista a utilizar. Dependiendo de la vista seleccionada con la opcion -v o
	 * el valor por defecto {@link #DEFAULT_VIEW} si el argumento -v no se
	 * proporciona.
	 */
	private static ViewInfo view;

	/**
	 * {@code true} if the option -m was provided, to use a separate view for
	 * each piece, and {@code false} otherwise.
	 * 
	 * <p>
	 * {@code true} si se incluye la opcion -m, para utilizar una vista separada
	 * por cada ficha, o {@code false} en caso contrario.
	 */
	private static boolean multiviews;

	/**
	 * Number of rows provided with the option -d ({@code null} if not
	 * provided).
	 * 
	 * <p>
	 * Numero de filas proporcionadas con la opcion -d, o {@code null} si no se
	 * incluye la opcion -d.
	 */
	private static Integer dimRows;
	/**
	 * Number of columns provided with the option -d ({@code null} if not
	 * provided).
	 * 
	 * <p>
	 * Numero de columnas proporcionadas con la opcion -d, o {@code null} si no
	 * se incluye la opcion -d.
	 * 
	 */
	private static Integer dimCols;
	
	/**
	 * Number of obstacles provided with the option -o ({@code null} if not
	 * provided).
	 * 
	 * <p>
	 * Numero de obstaculos proporcionados con la opcion -o, o {@code null} si no se
	 * incluye la opcion -o.
	 */
	private static Integer obstacles;

	/**
	 * The algorithm to be used by the automatic player. Not used so far, it is
	 * always {@code null}.
	 * 
	 * <p>
	 * Algoritmo a utilizar por el jugador automatico. Actualmente no se
	 * utiliza, por lo que siempre es {@code null}.
	 */
	private static AIAlgorithm aiPlayerAlg;

	
	private static ApplicationMode applicationMode;

	private static int serverPort;
	
	private static String serverHost;
	

	
	/**
	 * The depth of the maximum depth in the MinMax Algorithm.
	 * 
	 * <p>
	 * La profundidad máxima del árbol MinMax
	 */
	private static Integer minmaxTreeDepth;

	/**
	 * Processes the command-line arguments and modify the fields of this
	 * class with corresponding values. E.g., the factory, the pieces, etc.
	 *
	 * <p>
	 * Procesa la linea de ordenes del programa y crea los objetos necesarios
	 * para los atributos de esta clase. Por ejemplo, la factoria, las fichas,
	 * etc.
	 * 
	 * 
	 * @param args
	 *            Command line arguments.
	 * 
	 *            <p>
	 *            Lista de argumentos de la linea de ordenes.
	 * 
	 * 
	 */
	private static void parseArgs(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = new Options();
		cmdLineOptions.addOption(constructHelpOption()); // -h or --help
		cmdLineOptions.addOption(constructGameOption()); // -g or --game
		cmdLineOptions.addOption(constructObstaclesOption()); // -o or --obstacles
		cmdLineOptions.addOption(constructViewOption()); // -v or --view
		cmdLineOptions.addOption(constructMutiViewOption()); // -m or
																// --multiviews
		cmdLineOptions.addOption(constructPlayersOption()); // -p or --players
		cmdLineOptions.addOption(constructDimensionOption()); // -d or --dim
		cmdLineOptions.addOption(constructApplicationOption()); // -am or --app-mode
		cmdLineOptions.addOption(constructServerPortOption()); // -sp or --server-port
		cmdLineOptions.addOption(constructServerHostOption()); // -sh or --server-host
		cmdLineOptions.addOption(constructMinMaxDepathOption()); // --minmax-depth or -md
		cmdLineOptions.addOption(constructAIAlgOption()); // --ai-algorithm or -aialg
		
		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parseHelpOption(line, cmdLineOptions);
			parseApplicationOption(line);
			parseServerPortOption(line);
			parseServerHostOption(line);
			parseDimOptionn(line);
			parseObstaclesOption(line);
			parseGameOption(line);
			parseViewOption(line);
			parseMultiViewOption(line);
			parsePlayersOptions(line);
			parseMixMaxDepthOption(line);
			parseAIAlgOption(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException | GameError e) {
			// new Piece(...) might throw GameError exception
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	/**
	 * Builds the multiview (-m or --multiviews) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -m.
	 * 
	 * @return CLI {@link {@link Option} for the multiview option.
	 */

	private static Option constructMutiViewOption() {
		return new Option("m", "multiviews", false,
				"Create a separate view for each player (valid only when using the " + ViewInfo.WINDOW + " view)");
	}

	/**
	 * Parses the multiview option (-m or --multiview). It sets the value of
	 * {@link #multiviews} accordingly.
	 * 
	 * <p>
	 * Extrae la opcion multiview (-m) y asigna el valor de {@link #multiviews}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 */
	private static void parseMultiViewOption(CommandLine line) {
		multiviews = line.hasOption("m");
	}

	/**
	 * Builds the view (-v or --view) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -v.
	 * 
	 * @return CLI {@link Option} for the view option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */
	private static Option constructViewOption() {
		String optionInfo = "The view to use ( ";
		for (ViewInfo i : ViewInfo.values()) {
			optionInfo += i.getId() + " [for " + i.getDesc() + "] ";
		}
		optionInfo += "). By defualt, " + DEFAULT_VIEW.getId() + ".";
		Option opt = new Option("v", "view", true, optionInfo);
		opt.setArgName("view identifier");
		return opt;
	}

	/**
	 * Parses the view option (-v or --view). It sets the value of {@link #view}
	 * accordingly.
	 * 
	 * <p>
	 * Extrae la opcion view (-v) y asigna el valor de {@link #view}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided (the valid values are those
	 *             of {@link ViewInfo}.
	 */
	private static void parseViewOption(CommandLine line) throws ParseException {
		String viewVal = line.getOptionValue("v", DEFAULT_VIEW.getId());
		// view type
		for (ViewInfo v : ViewInfo.values()) {
			if (viewVal.equals(v.getId())) {
				view = v;
			}
		}
		if (view == null) {
			throw new ParseException("Uknown view '" + viewVal + "'");
		}
	}
	
	/**
	 * Builds the Application (--app-mode or -am) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -am.
	 * 
	 * @return CLI {@link Option} for the Application option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */
	private static Option constructApplicationOption() {
		String optionInfo = "The application to use ( ";
		for (ApplicationMode i : ApplicationMode.values()) {
			optionInfo += i.getId() + " [for " + i.getDesc() + "] ";
		}
		optionInfo += "). By defualt, " + DEFAULT_APPLICATIONMODE.getId() + ".";
		Option opt = new Option("am", "app-mode", true, optionInfo);
		opt.setArgName("Application identifier");
		return opt;
	}

	/**
	 * Parses the Application option (--app-mode o -am). It sets the value of {@link #Application}
	 * accordingly.
	 * 
	 * <p>
	 * Extrae la opcion Application (-am) y asigna el valor de {@link #Application}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided (the valid values are those
	 *             of {@link ApplicationMode}.
	 */
	private static void parseApplicationOption(CommandLine line) throws ParseException {
		String applicationVal = line.getOptionValue("am", DEFAULT_APPLICATIONMODE.getId());
		for (ApplicationMode am : ApplicationMode.values()) {
			if (applicationVal.equals(am.getId())) {
				applicationMode = am;
			}
		}
		if (applicationMode == null) {
			throw new ParseException("Uknown application mode '" + applicationVal + "'");
		}
	}
	
	/**
	 * Builds the server-port (--server-port or -sp) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -sp.
	 * 
	 * @return CLI {@link {@link Option} for the server port option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */

	private static Option constructServerPortOption() {
		return new Option("sp", "server-port", true,
			"The server port to which the server has to be initialized or the port the server is listening");
	}
	
	/**
	 * Parses the Server Port option (--server-port or -sp). It sets the value of
	 * {@link #serverPort} accordingly. 
	 * 
	 * <p>
	 * Extrae la opcion de puerto del servidor (-sp). Asigna el valor del atributo
	 * {@link #serverPort}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided 
	 *             <p>
	 *             Si se proporciona un valor invalido 
	 */
	private static void parseServerPortOption(CommandLine line) throws ParseException {
		String portVal = line.getOptionValue("sp", DEFAULT_SERVERPORT);
		if (portVal != null) {
			try {
				serverPort = Integer.parseInt(portVal);
			} catch (NumberFormatException e) {
				throw new ParseException("Invalid port: " + portVal);
			}
		}
	}
	
	
	/**
	 * Builds the server-port (--server-port or -sp) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -sp.
	 * 
	 * @return CLI {@link {@link Option} for the server port option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */
	
	private static Option constructServerHostOption() {
		return new Option("sh", "server-host", true,
				"The name of the machine that is executing the server");
	}

	/**
	 * Parses the Server Port option (--server-port or -sp). It sets the value of
	 * {@link #serverPort} accordingly. 
	 * 
	 * <p>
	 * Extrae la opcion de puerto del servidor (-sp). Asigna el valor del atributo
	 * {@link #serverPort}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided 
	 *             <p>
	 *             Si se proporciona un valor invalido 
	 */
	private static void parseServerHostOption(CommandLine line) throws ParseException {
		String hostVal = line.getOptionValue("sh", DEFAULT_SERVERHOST);
		if (hostVal != null) {
			serverHost = hostVal;
		}
	}
	
	/**
	 * Builds the MinMax tree depth (-md or --minmax-depth) CLI option.
	 * 
	 * @return CLI {@link {@link Option} for the MinMax tree depth option.
	 */
	private static Option constructMinMaxDepathOption() {
		Option opt = new Option("md", "minmax-depth", true, "The maximum depth of the MinMax tree");
		opt.setArgName("number");
		return opt;
	}

	/**
	 * Parses the MinMax tree depth option (-md or --minmax-depth). It sets the
	 * value of {@link #minmaxTreeDepth} accordingly.
	 * 
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 */
	private static void parseMixMaxDepthOption(CommandLine line) throws ParseException {
		String depthVal = line.getOptionValue("md");
		minmaxTreeDepth = null;

		if (depthVal != null) {
			try {
				minmaxTreeDepth = Integer.parseInt(depthVal);
			} catch (NumberFormatException e) {
				throw new ParseException("Invalid value for the MinMax depth '" + depthVal + "'");
			}
		}
	}

	/**
	 * Builds the ai-algorithm (-aialg or --ai-algorithm) CLI option.
	 * 
	 * @return CLI {@link {@link Option} for the ai-algorithm option.
	 */
	private static Option constructAIAlgOption() {
		String optionInfo = "The AI algorithm to use ( ";
		for (AlgorithmForAIPlayer alg : AlgorithmForAIPlayer.values()) {
			optionInfo += alg.getId() + " [for " + alg.getDesc() + "] ";
		}
		optionInfo += "). By defualt, no algorithm is used.";
		Option opt = new Option("aialg", "ai-algorithm", true, optionInfo);
		opt.setArgName("algorithm for ai player");
		return opt;
	}

	/**
	 * Parses the ai-algorithm option (-aialg or --ai-algorithm). It sets the
	 * value of {@link #minmaxTreeDepth} accordingly.
	 * 
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 */
	private static void parseAIAlgOption(CommandLine line) throws ParseException {
		String aialg = line.getOptionValue("aialg", DEFAULT_AIALG.getId());

		AlgorithmForAIPlayer selectedAlg = null;
		for (AlgorithmForAIPlayer a : AlgorithmForAIPlayer.values()) {
			if (a.getId().equals(aialg)) {
				selectedAlg = a;
				break;
			}	
		}

		if (selectedAlg == null) {
			throw new ParseException("Uknown AI algorithms '" + aialg + "'");
		}

		switch (selectedAlg) {
		case MINMAX:
			aiPlayerAlg = minmaxTreeDepth == null ? new MinMax(false) : new MinMax(minmaxTreeDepth, false);
			break;
		case MINMAXAB:
			aiPlayerAlg = minmaxTreeDepth == null ? new MinMax() : new MinMax(minmaxTreeDepth);
			break;
		case NONE:
			aiPlayerAlg = null;
			break;
		}
		
	}
		
	

	/**
	 * Builds the players (-p or --player) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -p.
	 * 
	 * @return CLI {@link Option} for the list of pieces/players.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */
	private static Option constructPlayersOption() {
		String optionInfo = "A player has the form A:B (or A), where A is sequence of characters (without any whitespace) to be used for the piece identifier, and B is the player mode (";
		for (PlayerMode i : PlayerMode.values()) {
			optionInfo += i.getId() + " [for " + i.getDesc() + "] ";
		}
		optionInfo += "). If B is not given, the default mode '" + DEFAULT_PLAYERMODE.getId()
				+ "' is used. If this option is not given a default list of pieces from the corresponding game is used, each assigmed the mode '"
				+ DEFAULT_PLAYERMODE.getId() + "'.";

		Option opt = new Option("p", "players", true, optionInfo);
		opt.setArgName("list of players");
		return opt;
	}

	/**
	 * Parses the players/pieces option (-p or --players). It sets the value of
	 * {@link #pieces} and {@link #playerModes} accordingly.
	 *
	 * <p>
	 * Extrae la opcion players (-p) y asigna el valor de {@link #pieces} y
	 * {@link #playerModes}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided (@see
	 *             {@link #constructPlayersOption()}).
	 *             <p>
	 *             Si se proporciona un valor invalido (@see
	 *             {@link #constructPlayersOption()}).
	 */
	private static void parsePlayersOptions(CommandLine line) throws ParseException {

		String playersVal = line.getOptionValue("p");

		if (playersVal == null) {
			// if no -p option, we take the default pieces from the
			// corresponding
			// factory, and for each one we use the default player mode.
			pieces = gameFactory.createDefaultPieces();
			playerModes = new ArrayList<PlayerMode>();
			for (int i = 0; i < pieces.size(); i++) {
				playerModes.add(DEFAULT_PLAYERMODE);
			}
		} else {
			pieces = new ArrayList<Piece>();
			playerModes = new ArrayList<PlayerMode>();
			String[] players = playersVal.split(",");
			for (String player : players) {
				String[] playerInfo = player.split(":");
				if (playerInfo.length == 1) { // only the piece name is provided
					pieces.add(new Piece(playerInfo[0]));
					playerModes.add(DEFAULT_PLAYERMODE);
				} else if (playerInfo.length == 2) { // piece name and mode are
														// provided
					pieces.add(new Piece(playerInfo[0]));
					PlayerMode selectedMode = null;
					for (PlayerMode mode : PlayerMode.values()) {
						if (mode.getId().equals(playerInfo[1])) {
							selectedMode = mode;
						}
					}
					if (selectedMode != null) {
						playerModes.add(selectedMode);
					} else {
						throw new ParseException("Invalid player mode in '" + player + "'");
					}
				} else {
					throw new ParseException("Invalid player information '" + player + "'");
				}
			}
		}
	}

	/**
	 * Builds the game (-g or --game) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -g.
	 * 
	 * @return CLI {@link {@link Option} for the game option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */

	private static Option constructGameOption() {
		String optionInfo = "The game to play ( ";
		for (GameInfo i : GameInfo.values()) {
			optionInfo += i.getId() + " [for " + i.getDesc() + "] ";
		}
		optionInfo += "). By defualt, " + DEFAULT_GAME.getId() + ".";
		Option opt = new Option("g", "game", true, optionInfo);
		opt.setArgName("game identifier");
		return opt;
	}

	/**
	 * Parses the game option (-g or --game). It sets the value of
	 * {@link #gameFactory} accordingly. Usually it requires that
	 * {@link #parseDimOptionn(CommandLine)} has been called already to parse
	 * the dimension option.
	 * 
	 * <p>
	 * Extrae la opcion de juego (-g). Asigna el valor del atributo
	 * {@link #gameFactory}. Normalmente necesita que se haya llamado antes a
	 * {@link #parseDimOptionn(CommandLine)} para extraer la dimension del
	 * tablero.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided (the valid values are those
	 *             of {@link GameInfo}).
	 *             <p>
	 *             Si se proporciona un valor invalido (Los valores validos son
	 *             los de {@link GameInfo}).
	 */
	private static void parseGameOption(CommandLine line) throws ParseException {
		String gameVal = line.getOptionValue("g", DEFAULT_GAME.getId());
		GameInfo selectedGame = null;

		for( GameInfo g : GameInfo.values() ) {
			if ( g.getId().equals(gameVal) ) {
				selectedGame = g;
				break;
			}
		}

		if ( selectedGame == null ) {
			throw new ParseException("Uknown game '" + gameVal + "'");
		}
	
		switch ( selectedGame ) {
		case AdvancedTicTacToe:
			gameFactory = new AdvancedTTTFactoryExt();
			break;
		case CONNECTN:
			if (dimRows != null && dimCols != null && dimRows == dimCols) {
				gameFactory = new ConnectNFactoryExt(dimRows);
			} else {
				gameFactory = new ConnectNFactoryExt();
			}
			break;
		case TicTacToe:
			gameFactory = new TicTacToeFactoryExt();
			break;
		case Ataxx:
			if (obstacles == null){
				if (dimRows != null && dimCols != null && dimRows == dimCols && dimRows % 2 != 0){
					gameFactory = new AtaxxFactoryExt(dimRows);
				} else {
					gameFactory = new AtaxxFactoryExt();
				}
			}
			else {
				if (dimRows != null && dimCols != null && dimRows == dimCols && dimRows % 2 != 0){
					gameFactory = new AtaxxFactoryExt(dimRows, obstacles);
				} else {
					gameFactory = new AtaxxFactoryExt(obstacles, false);
				}
			}
			break;
		default:
			throw new UnsupportedOperationException("Something went wrong! This program point should be unreachable!");
		}
	
	}
	
	/**
	 * Builds the obstacles (-o or --obstacles) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -o.
	 * 
	 * @return CLI {@link {@link Option} for the obstacles option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */

	private static Option constructObstaclesOption() {
		return new Option("o", "obstacles", true,
				"The number of obstacles in the board.");
	}

	/**
	 * Parses the obstacles option (-o or --obstacles). It sets the value of
	 * {@link #obstacles} accordingly. 
	 * 
	 * <p>
	 * Extrae la opcion de obstaculos (-o). Asigna el valor del atributo
	 * {@link #obstacles}.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided (the valid values are those
	 *             of {@link GameInfo}).
	 *             <p>
	 *             Si se proporciona un valor invalido (Los valores validos son
	 *             los de {@link GameInfo}).
	 */
	private static void parseObstaclesOption(CommandLine line) throws ParseException {
		String dimVal = line.getOptionValue("o");
		if (dimVal != null) {
			try {
				obstacles = Integer.parseInt(dimVal);
			} catch (NumberFormatException e) {
				throw new ParseException("Invalid dimension: " + dimVal);
			}
		}
	}

	/**
	 * Builds the dimension (-d or --dim) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -d.
	 * 
	 * @return CLI {@link {@link Option} for the dimension.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */
	private static Option constructDimensionOption() {
		return new Option("d", "dim", true,
				"The board size (if allowed by the selected game). It must has the form ROWSxCOLS.");
	}

	/**
	 * Parses the dimension option (-d or --dim). It sets the value of
	 * {@link #dimRows} and {@link #dimCols} accordingly. The dimension is
	 * ROWSxCOLS.
	 * 
	 * <p>
	 * Extrae la opcion dimension (-d). Asigna el valor de los atributos
	 * {@link #dimRows} and {@link #dimCols}. La dimension es de la forma
	 * ROWSxCOLS.
	 * 
	 * @param line
	 *            CLI {@link CommandLine} object.
	 * @throws ParseException
	 *             If an invalid value is provided.
	 *             <p>
	 *             Si se proporciona un valor invalido.
	 */
	private static void parseDimOptionn(CommandLine line) throws ParseException {
		String dimVal = line.getOptionValue("d");
		if (dimVal != null) {
			try {
				String[] dim = dimVal.split("x");
				if (dim.length == 2) {
					dimRows = Integer.parseInt(dim[0]);
					dimCols = Integer.parseInt(dim[1]);
				} else {
					throw new ParseException("Invalid dimension: " + dimVal);
				}
			} catch (NumberFormatException e) {
				throw new ParseException("Invalid dimension: " + dimVal);
			}
		}

	}

	/**
	 * Builds the help (-h or --help) CLI option.
	 * 
	 * <p>
	 * Construye la opcion CLI -h.
	 * 
	 * @return CLI {@link {@link Option} for the help option.
	 *         <p>
	 *         Objeto {@link Option} de esta opcion.
	 */

	private static Option constructHelpOption() {
		return new Option("h", "help", false, "Print this message");
	}

	/**
	 * Parses the help option (-h or --help). It print the usage information on
	 * the standard output.
	 * 
	 * <p>
	 * Extrae la opcion help (-h) que imprime informacion de uso del programa en
	 * la salida estandar.
	 * 
	 * @param line
	 *            * CLI {@link CommandLine} object.
	 * @param cmdLineOptions
	 *            CLI {@link Options} object to print the usage information.
	 * 
	 */
	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	/**
	 * Starts a game using a {@link ConsoleCtrl} which is not based on MVC. Is
	 * used only for teaching the difference from the MVC one.
	 * 
	 * <p>
	 * Método para iniciar un juego con el controlador {@link ConsoleCtrl}, no
	 * basado en MVC. Solo se utiliza para mostrar las diferencias con el
	 * controlador MVC.
	 * 
	 */
	public static void startGameNoMVC() {
		Game g = new Game(gameFactory.gameRules());
		Controller c = null;

		switch (view) {
		case CONSOLE:
			ArrayList<Player> players = new ArrayList<Player>();
			for (int i = 0; i < pieces.size(); i++) {
				switch (playerModes.get(i)) {
				case AI:
					players.add(gameFactory.createAIPlayer(aiPlayerAlg));
					break;
				case MANUAL:
					players.add(gameFactory.createConsolePlayer());
					break;
				case RANDOM:
					players.add(gameFactory.createRandomPlayer());
					break;
				default:
					throw new UnsupportedOperationException(
							"Something went wrong! This program point should be unreachable!");
				}
			}
			c = new ConsoleCtrl(g, pieces, players, new Scanner(System.in));
			break;
		case WINDOW:
			throw new UnsupportedOperationException(
					"Swing Views are not supported in startGameNoMVC!! Please use startGameMVC instead.");
		default:
			throw new UnsupportedOperationException("Something went wrong! This program point should be unreachable!");
		}

		c.start();
	}

	/**
	 * Starts a game. Should be called after {@link #parseArgs(String[])} so
	 * some fields are set to their appropriate values.
	 * 
	 * <p>
	 * Inicia un juego. Debe llamarse despues de {@link #parseArgs(String[])}
	 * para que los atributos tengan los valores correctos.
	 * 
	 */
	public static void startGame() {
		Game g = new Game(gameFactory.gameRules());
		Controller c = null;

		switch (view) {
		case CONSOLE:
			ArrayList<Player> players = new ArrayList<Player>();
			for (int i = 0; i < pieces.size(); i++) {
				switch (playerModes.get(i)) {
				case AI:
					players.add(gameFactory.createAIPlayer(aiPlayerAlg));
					break;
				case MANUAL:
					players.add(gameFactory.createConsolePlayer());
					break;
				case RANDOM:
					players.add(gameFactory.createRandomPlayer());
					break;
				default:
					throw new UnsupportedOperationException(
							"Something went wrong! This program point should be unreachable!");
				}
			}
			c = new ConsoleCtrlMVC(g, pieces, players, new Scanner(System.in));
			gameFactory.createConsoleView(g, c);
			break;
		case WINDOW:
			//Creates a view for each piece, else creates the general view
			c = new Controller(g, pieces);
			if (multiviews){
				for (Piece p : pieces) {
					gameFactory.createSwingView(g, c, p, gameFactory.createRandomPlayer(), gameFactory.createAIPlayer(aiPlayerAlg));
					}
			}
			else{
				gameFactory.createSwingView(g, c, null, gameFactory.createRandomPlayer(), gameFactory.createAIPlayer(aiPlayerAlg));
			}
			//Para esto se ha creado la clase swingControler
			break;
		default:
			throw new UnsupportedOperationException("Something went wrong! This program point should be unreachable!");
		}
		//c
		c.start();
	}

	/**
	 * The main method. It calls {@link #parseArgs(String[])} and then
	 * {@link #startGame()}.
	 * 
	 * <p>
	 * Metodo main. Llama a {@link #parseArgs(String[])} y a continuacion inicia
	 * un juego con {@link #startGame()}.
	 * 
	 * @param args
	 *            Command-line arguments.
	 * 
	 */
	public static void main(String[] args) {
		parseArgs(args);
		switch (applicationMode) {
		case NORMAL:
			startGame(); break;
		case CLIENT:
			startClient(); break;
		case SERVER:
			startServer(); break;
		}
	}
	
	private static void startServer() {
		GameServer c = new GameServer(gameFactory, pieces, serverPort);
			c.start();
		}
	
	private static void startClient() {
		try {
			GameClient c = new GameClient(serverHost, serverPort);
			gameFactory = c.getGameFactory();
			System.out.println(c.getPlayerPiece());
			gameFactory.createSwingView(c, c, c.getPlayerPiece(), gameFactory.createRandomPlayer(), gameFactory.createAIPlayer(aiPlayerAlg));
			c.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
