package com.restman.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("unused")
@Path("/herest")
public class RestmanTestCaseManager {

	private static String elasticSearchServer = null;

	static {

		if (System.getProperty("elasticSearchServer") != null) {

			elasticSearchServer = System.getProperty("elasticSearchServer");
		} else {

			String esUrl = ConfigReader.getInstance().getProperty("es.url");

			if (esUrl != null) {
				elasticSearchServer = esUrl;
			}
		}
	}

	@Path("{projectName}/{type}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public javax.ws.rs.core.Response save(@PathParam("projectName") String projectName, @PathParam("type") String type,@QueryParam("userName") String userName, String body) throws ClientProtocolException, IOException {
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(body);
		jsonObject.addProperty("type",type);
		jsonObject.addProperty("userName", userName);

		return  saveData(projectName,type,jsonObject.get("id").getAsString(),userName,jsonObject);
	}

	@Path("{projectName}/{type}/{id}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public javax.ws.rs.core.Response saveById(@PathParam("projectName") String projectName, @PathParam("type") String type,
			@PathParam("id") String id,@QueryParam("userName") String userName, String body) throws ClientProtocolException, IOException {

		JsonObject jsonObject = (JsonObject) new JsonParser().parse(body);
		jsonObject.addProperty("type",type);
		jsonObject.addProperty("userName", userName);


		return  saveData(projectName,type,id,userName,jsonObject);


	}

	private javax.ws.rs.core.Response saveData(@PathParam("projectName") String projectName, @PathParam("type") String type,
											   @PathParam("id") String id,@QueryParam("userName") String userName,JsonObject jsonObject)  throws ClientProtocolException, IOException{
		String resourceURL = elasticSearchServer + (projectName + "_" + type).toLowerCase() + "/_doc/" + id;

		HttpResponse resp = Request.Post(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"))
				.body(new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON)).execute().returnResponse();

		if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 201)
			return javax.ws.rs.core.Response.ok().build();
		else
			return javax.ws.rs.core.Response.serverError().build();
	}


	@Path("{projectName}/{type}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String genericDomainRead(@PathParam("projectName") String projectName, @PathParam("type") String type,
			@QueryParam("executionId") String executionId,@QueryParam("limit") int limit,@QueryParam("offset") int offset) throws ClientProtocolException, IOException {

		if(limit==0) limit=5000;

		String resourceURL = elasticSearchServer + (projectName + "_" + type).toLowerCase()   + "/_search?size="+limit;

		if (executionId != null && !executionId.isEmpty()) {

			resourceURL += "&q=executionId:" + executionId;
		}

		Request request = Request.Get(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"));

		try {
			return request.execute().returnContent().asString();

		} catch (Exception e) {

			return "{}";
		}
	}

	@Path("{projectName}/{type}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String genericDomainReadById(@PathParam("projectName") String projectName, @PathParam("type") String type,
									@PathParam("id") String id) throws ClientProtocolException, IOException {


		String resourceURL = elasticSearchServer + (projectName + "_" + type).toLowerCase()   + "/_doc/"+id;


		Request request = Request.Get(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"));

		try {
			return request.execute().returnContent().asString();

		} catch (Exception e) {

			return "{}";
		}
	}

	@Path("executions/execution/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getExecution(@PathParam("id") String id) throws ClientProtocolException, IOException {


		String resourceURL = elasticSearchServer +"executions/execution/"+id;


		Request request = Request.Get(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"));

		try {
			return request.execute().returnContent().asString();

		} catch (Exception e) {

			return "{}";
		}
	}

	@Path("{projectName}/{type}/{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String delete(@PathParam("projectName") String projectName, @PathParam("type") String type,
										@PathParam("id") String id) throws ClientProtocolException, IOException {


		String resourceURL = elasticSearchServer + (projectName + "_" + type).toLowerCase()   + "/_doc/"+id;


		Request request = Request.Delete(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"));

		try {
			return request.execute().returnContent().asString();

		} catch (Exception e) {

			return "{}";
		}
	}



//	@Path("{projectName}/testCase")
//	@POST
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public javax.ws.rs.core.Response test(@PathParam("projectName") String projectName,
//			@QueryParam("userName") String userName, String body)
//			throws URISyntaxException, ClientProtocolException, IOException {
//		JsonObject jsonObject = (JsonObject) new JsonParser().parse(body);
//
////		jsonObject.get("steps").getAsJsonArray().forEach(ja->{
////			String input = ja.getAsJsonObject().get("input").toString();
////			ja.getAsJsonObject().addProperty("input", input);
////			String assertions = ja.getAsJsonObject().get("assertions").toString();
////			ja.getAsJsonObject().addProperty("assertions", assertions);
////		});
//		jsonObject.addProperty("userName", userName);
//
//		String resourceURL = elasticSearchServer + projectName + "/testCase/" + jsonObject.get("id").getAsString();
//
//		Request request = Request.Post(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"))
//				.body(new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON));
//
//		int statusCode = request.execute().returnResponse().getStatusLine().getStatusCode();
//		System.out.println("Resource URL :" + resourceURL);
//
//		if (statusCode == 200 || statusCode == 201) {
//
//			return javax.ws.rs.core.Response.created(new URI(resourceURL)).build();
//		} else {
//
//			return javax.ws.rs.core.Response.serverError().build();
//		}
//	}

	// http://localhost:9200/mytest/_search?q=steps.method:GET%20userName:dhaneesh%20tags:performance&pretty&default_operator=AND
	@POST
	@Path("execute")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String execute(@QueryParam("userName") String userName, @QueryParam("projectName") String projectName,
			@QueryParam("tags") String tags, @QueryParam("executionId") String executionId,
			@QueryParam("onlyMyTests") boolean onlyMyTests, @Context UriInfo ui) {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		String execId = UUID.randomUUID().toString();
		new Thread(() -> executeInternal(userName, projectName, tags, execId, onlyMyTests)).start();
		JSONObject jo = new JSONObject();
		jo.put("status", "SUCCESS");
		jo.put("executionId", execId);
		return jo.toString();
	}

	// http://localhost:9200/mytest/_search?q=steps.method:GET%20userName:dhaneesh%20tags:performance&pretty&default_operator=AND

	public String executeInternal(@QueryParam("userName") String userName,
			@QueryParam("projectName") String projectName, @QueryParam("tags") String tags,
			@QueryParam("executionId") String executionId, @QueryParam("onlyMyTests") boolean onlyMyTests
			) {



		String q = "";
		String app = "";

		if ("All".equalsIgnoreCase(tags)) {
			tags = "";
		} else if (tags != null && !tags.isEmpty()) {
			tags = tags.replaceAll(",", " OR ");
			tags = "(" + tags + ")";
		}

		if (onlyMyTests)
			q += (userName != null && !userName.isEmpty()) ? "userName:" + userName : "";

		if (!q.isEmpty())
			app = " ";

		q += app + ((tags != null && !tags.isEmpty()) ? "tags:" + tags : "");

		if (!q.isEmpty())
			q = "?q=" + URLEncoder.encode(q) + "&default_operator=AND";

		String resourceURL = elasticSearchServer + (projectName + "_testCase").toLowerCase()+"/_search" + q;

		System.out.println("resourceURL :" + resourceURL);

		Request request = Request.Get(resourceURL).addHeader(new BasicHeader("Content-Type", "application/json"));

		Map<String, String> qp =getVariables(projectName);
		String content;
		final String execId;

		if (executionId == null) {
			execId = UUID.randomUUID().toString();
		} else {
			execId = executionId;
		}
		try {
			content = request.execute().returnContent().asString();

			JsonObject jsonObject = (JsonObject) new JsonParser().parse(content);

			JsonArray ja = jsonObject.get("hits").getAsJsonObject().get("hits").getAsJsonArray();

			MultiStepParser multiStep = new MultiStepParser();

			StringBuffer sb = new StringBuffer();

			ExecutorService service = Executors.newFixedThreadPool(10);

			String executionStatusURL = elasticSearchServer + "executions/execution/" + execId;

			JsonObject jsonObjectExec = new JsonObject();

			jsonObjectExec.addProperty("status", "RUNNING");

			jsonObjectExec.addProperty("projectName", projectName);

			jsonObjectExec.addProperty("tags", tags == null || tags.isEmpty() ? "all" : tags);

			jsonObjectExec.addProperty("startTime", new Date().toString());

			jsonObjectExec.add("context", new Gson().toJsonTree(qp));

			Request.Post(executionStatusURL).addHeader(new BasicHeader("Content-Type", "application/json"))
					.body(new StringEntity(jsonObjectExec.toString(), ContentType.APPLICATION_JSON)).execute();

			System.out.println("Total Testcases Extracted : " + ja.size());
			for (int i = 0; i < ja.size(); i++) {

				final int cIndex = i;

				service.submit(new Runnable() {

					@Override
					public void run() {
						try {
							String resultURL = elasticSearchServer + projectName + "_summary/data";

							String resultURLFull = elasticSearchServer + projectName + "_result/data";

							String data = multiStep.parseInput(
									ja.get(cIndex).getAsJsonObject().get("_source").getAsJsonObject().toString(), qp);

							JsonObject jsonNew = new JsonParser().parse(data).getAsJsonObject();

							jsonNew.addProperty("executionId", execId);

							JsonObject jsonFull = new JsonParser().parse(data).getAsJsonObject();

							jsonFull.addProperty("executionId", execId);

							JsonArray jap = jsonNew.get("steps").getAsJsonArray();

							for (int j = 0; j < jap.size(); j++) {

								JsonObject jo = jap.get(j).getAsJsonObject();

								jo.remove("response");

							}

							Request req = Request.Post(resultURL)
									.addHeader(new BasicHeader("Content-Type", "application/json"))
									.body(new StringEntity(jsonNew.toString(), ContentType.APPLICATION_JSON));

							int status;

							status = req.execute().returnResponse().getStatusLine().getStatusCode();

							System.out.println("Result update status:::" + status);

							req = Request.Post(resultURLFull)
									.addHeader(new BasicHeader("Content-Type", "application/json"))
									.body(new StringEntity(jsonFull.toString(), ContentType.APPLICATION_JSON));

							status = req.execute().returnResponse().getStatusLine().getStatusCode();

							System.out.println("Result full update status:::" + status);

							sb.append(jsonNew.toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});

			}

			try {
				service.shutdown();
				System.out.println("Waiting for the tasks to finish");
				service.awaitTermination(600, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			jsonObjectExec.addProperty("status", "COMPLETED");
			jsonObjectExec.addProperty("endTime", new Date().toString());

			Request.Post(executionStatusURL).addHeader(new BasicHeader("Content-Type", "application/json"))
					.body(new StringEntity(jsonObjectExec.toString(), ContentType.APPLICATION_JSON)).execute();

			return sb.toString();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "FAILED EXECUTION";
	}


	private Map<String, String>  getVariables(String projectName){
		String resultURL = elasticSearchServer + projectName + "_variables/_search";
		Request request = Request.Get(resultURL).addHeader(new BasicHeader("Content-Type", "application/json"));
		Map<String, String> qp = new HashMap<>();

		try {
			String data= request.execute().returnContent().asString();
			JsonObject jsonNew = new JsonParser().parse(data).getAsJsonObject();

			jsonNew.getAsJsonObject("hits").getAsJsonArray("hits").forEach(f->{
				JsonObject jo=f.getAsJsonObject().getAsJsonObject("_source");
				qp.put(jo.get("name").getAsString(),jo.get("value").getAsString());

			});

		} catch (Exception e) {

		}
		return qp;
	}


}