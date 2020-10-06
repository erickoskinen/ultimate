package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.PluginFactory;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.SettingsManager;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.ToolchainManager;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.services.ToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.RunDefinition;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.ToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchain;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.IUltimatePlugin;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILoggingService;
import de.uni_freiburg.informatik.ultimate.util.CoreUtil;


public class UltimateAPIServlet extends HttpServlet implements ICore<RunDefinition>, IUltimatePlugin {
	
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = !false;
	private final ServletLogger mLogger;
	private ToolchainManager mToolchainManager;
	private ToolchainStorage mCoreStorage;
	private SettingsManager mSettingsManager;
	private PluginFactory mPluginFactory;
	private ILoggingService mLoggingService;
	private String mUltimateVersion;
	
	/**
	 * Constructor.
	 *
	 * @see HttpServlet#HttpServlet()
	 */
	public UltimateAPIServlet() {
		super();
		mLogger = new ServletLogger(this, "Servlet", DEBUG);
		mCoreStorage = new ToolchainStorage();
		mLoggingService = mCoreStorage.getLoggingService();
		mSettingsManager = new SettingsManager(mLogger);
		mSettingsManager.registerPlugin(this);
		mPluginFactory = new PluginFactory(mSettingsManager, mLogger);
	}
	
	/**
	 * Process GET requests
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		mLogger.logDebug("Connection from " + request.getRemoteAddr() + ", GET: " + request.getQueryString());
		
		processAPIGetRequest(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		mLogger.logDebug("Connection from " + request.getRemoteAddr() + ", POST: " + request.getRequestURI());
		final ServletLogger sessionLogger = new ServletLogger(this, request.getSession().getId(), DEBUG);
		final Request internalRequest = new Request(request, sessionLogger);
		
		processAPIPostRequest(internalRequest, response);
	}
	
	
	private void processAPIGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final PrintWriter responseWriter = prepareJSONResponse(response);
		JSONObject jsonResult = new JSONObject();
		// Get the URL parts. A request url might look like /api/job/job_id.
		// urlParts = {"", "job", "job_id"} in this example.
		String[] urlParts = (request.getPathInfo() != null) ? request.getPathInfo().split("/") : null;
		// resource = "job" in this example.
		String resource = (urlParts != null) ? urlParts[1] : "";  
		
		try {
			switch (resource) {
			case "version":
				jsonResult.put("ultimate_version", this.getUltimateVersionString());
				break;
			case "job":
				String jobAction = (urlParts.length >= 2) ? urlParts[2] : "";
				
				String jobId = urlParts[2];
				JobResult jobResult = new JobResult(jobId);
				jobResult.load();
				jsonResult = jobResult.getJson();
				break;
			case "jobs":
				Job[] jobs = getPendingToolchainJobs();
				for (int i = 0; i < jobs.length; i++) {
					WebBackendToolchainJob job = (WebBackendToolchainJob) jobs[i];
					String theID = job.getId();
				}
				break;
			default:
				jsonResult.put("error", "unknown request.");
				break;
			}
			jsonResult.write(responseWriter);
		} catch (Exception e) {
			final String message = "{\"error\" : \"Invalid request: " + e.getMessage() + " \"}";
			responseWriter.print(message);
			mLogger.logDebug(message);
			
			if (DEBUG) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Pass the Request to initiateUltimateRun.
	 * Write results to responseWriter.
	 * 
	 * @param internalRequest
	 * @param responseWriter
	 */
	private void processAPIPostRequest(Request internalRequest, HttpServletResponse response) throws IOException {
		final PrintWriter responseWriter = prepareJSONResponse(response);
		
		try {
			mLogger.logDebug("Process API request.");
			JSONObject jsonResult = new JSONObject();
			if (internalRequest.getParameterList().containsKey("action")) {
				mLogger.logDebug("Initiate ultimate run for request: " + internalRequest.toString());
				jsonResult = initiateUltimateRun(internalRequest);
			} else {
				jsonResult.put("error", "Invalid request: Missing `action` parameter.");
			}
			jsonResult.write(responseWriter);
		} catch (final JSONException e) {
			final String message = "{\"error\" : \"Invalid request: " + e.getMessage() + " \"}";
			responseWriter.print(message);
			internalRequest.getLogger().logDebug(message);
			
			if (DEBUG) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set response headers for JSON. Return writer.
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private PrintWriter prepareJSONResponse(HttpServletResponse response) throws IOException {
		// Set response type to JSON.
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		return response.getWriter();
	}

	/**
	 * Initiate ultimate run for the request.
	 * Return the results as a json object.
	 * 
	 * @param internalRequest
	 * @return
	 * @throws JSONException
	 */
	private JSONObject initiateUltimateRun(Request internalRequest) throws JSONException {
		try {
			final String action = internalRequest.getSingleParameter("action");
			if (action.equals("execute")) {
				final JSONObject json = new JSONObject();
				json.put("requestId", internalRequest.getRequestId());
				json.put("status", "creating");
				final UltimateAPIController controller = new UltimateAPIController(internalRequest, json);				
				int status = controller.init(this);
				mToolchainManager = new ToolchainManager(mLoggingService, mPluginFactory, controller);
				if (status == 0) {
					controller.run();
				}
				mToolchainManager.close();
				return json;
			} else {
				internalRequest.getLogger().logDebug("Don't know how to handle action: " + action);
				final JSONObject json = new JSONObject();
				json.put("error", "Invalid request: Unknown `action` parameter ( " + action + ").");

				return json;
			}
		} catch (IllegalArgumentException e) {
			final JSONObject json = new JSONObject();
			json.put("error", "Invalid request: " + e.getMessage());
			return json;
		}
	}
	
	private boolean cancelToolchainJob(String jobId) {
		Job[] jobs = getPendingToolchainJobs();
		for (int i = 0; i < jobs.length; i++) {
			Job job = jobs[i];
		}
		
		return true;
	}
	
	/**
	 * Toolchain jobs (by family "WebBackendToolchainJob") running or queued.
	 * @return
	 */
	private Job[] getPendingToolchainJobs() {
		IJobManager jobManager = Job.getJobManager();
		Job[] jobs = jobManager.find("WebBackendToolchainJob");

		return jobs;
	}

	/***************************** ICore Implementation *********************/
	
	@Override
	public IToolchainData<RunDefinition> createToolchainData(String filename)
			throws FileNotFoundException, JAXBException, SAXException {
		if (!new File(filename).exists()) {
			throw new FileNotFoundException("The specified toolchain file " + filename + " was not found");
		}

		final ToolchainStorage tcStorage = new ToolchainStorage();
		return new ToolchainData(filename, tcStorage, tcStorage);
	}

	@Override
	public IToolchainData<RunDefinition> createToolchainData() {
		return null;
	}

	@Override
	public IToolchain<RunDefinition> requestToolchain(File[] inputFiles) {
		return mToolchainManager.requestToolchain(inputFiles);
	}

	@Override
	public void releaseToolchain(IToolchain<RunDefinition> toolchain) {
		mToolchainManager.releaseToolchain(toolchain);
	}

	@Override
	public void savePreferences(String absolutePath) {
		
	}

	@Override
	public void loadPreferences(String absolutePath, boolean silent) {
		
	}

	@Override
	public void resetPreferences(boolean silent) {
		
	}

	@Override
	public IUltimatePlugin[] getRegisteredUltimatePlugins() {
		return null;
	}

	@Override
	public String[] getRegisteredUltimatePluginIDs() {
		return null;
	}

	@Override
	public ILoggingService getCoreLoggingService() {
		return null;
	}

	@Override
	public IPreferenceProvider getPreferenceProvider(String pluginId) {
		return null;
	}

	@Override
	public String getUltimateVersionString() {
		if (mUltimateVersion == null) {
			final Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			if (bundle == null) {
				return "UNKNOWN";
			}
			final Dictionary<String, String> headers = bundle.getHeaders();
			if (headers == null) {
				return "UNKNOWN";
			}

			final String major = headers.get("Bundle-Version");
			final String gitVersion = CoreUtil.readGitVersion(getClass().getClassLoader());
			if (gitVersion == null) {
				return major;
			}
			mUltimateVersion = major + "-" + gitVersion;
		}
		return mUltimateVersion;
	}

	/************************* End ICore Implementation *********************/
	
	/************************* IUltimatePlugin Implementation *********************/

	@Override
	public String getPluginName() {
		return Activator.PLUGIN_NAME;
	}

	@Override
	public String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public IPreferenceInitializer getPreferences() {
		return null;
	}
	
	/************************* End IUltimatePlugin Implementation *********************/
	
}
