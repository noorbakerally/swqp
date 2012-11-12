package edu.rpi.tw.escience.species;

import static edu.rpi.tw.escience.waterquality.query.Query.VAR_NS;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import edu.rpi.tw.escience.waterquality.Module;
import edu.rpi.tw.escience.waterquality.ModuleConfiguration;
import edu.rpi.tw.escience.waterquality.QueryMethod;
import edu.rpi.tw.escience.waterquality.Request;
import edu.rpi.tw.escience.waterquality.Resource;
import edu.rpi.tw.escience.waterquality.SemantAquaUI;
import edu.rpi.tw.escience.waterquality.query.NamedGraphComponent;
import edu.rpi.tw.escience.waterquality.query.Query;
import edu.rpi.tw.escience.waterquality.query.QueryResource;
import edu.rpi.tw.escience.waterquality.query.Variable;
import edu.rpi.tw.escience.waterquality.query.Query.Type;

public class SpeciesDataProviderModule implements Module {
	
	private static final String POL_NS = "http://escience.rpi.edu/ontology/semanteco/2/0/pollution.owl#";
	private static final String WATER_NS = "http://escience.rpi.edu/ontology/semanteco/2/0/water.owl#";
	private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String UNIT_NS = "http://sweet.jpl.nasa.gov/2.1/reprSciUnits.owl#";
	//private static final String TIME_NS = "http://www.w3.org/2006/time#";
	private static final String GEOSPECIES_NS = "http://rdf.geospecies.org/ont/geospecies.owl#";
	public static final String  TXN_NS = "http://lod.taxonconcept.org/ontology/txn.owl#";
	private static final String WILDLIFE_NS = "http://www.semanticweb.org/ontologies/2012/2/wildlife.owl#";
	private static final String HEALTHEFFECT_NS = "http://escience.rpi.edu/ontology/semanteco/2/0/healtheffect.owl";
	private static final String PROV_NS = "http://www.w3.org/ns/prov#";
	private static final String DC_NS = "http://purl.org/dc/terms/";
	private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
	private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
	private static final String WGS_NS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	private static final String e1_NS = "http://was.tw.rpi.edu/source/bird-data/dataset/ebird-data/version/2012-Nov-4/params/enhancement/1/";
	public static final String e2_NS = "http://was.tw.rpi.edu/source/bird-data/dataset/ebird-data/vocab/enhancement/1/";
	public static final String QUERY_NS = "http://aquarius.tw.rpi.edu/projects/semantaqua/data-source/query-variable/";
	private static final String FAILURE = "{\"success\":false}";
	//private ModuleConfiguration config = null;
	private static final String BINDINGS = "bindings";
	private Logger log = Logger.getLogger(SpeciesDataProviderModule.class);
	private static final String SOURCE_VAR = "source";
	private static final String LABEL_VAR = "label";
	private static final String VALUE = "value";
	private static final String SITE_VAR = "site";
	private static final String parent = "parent";
	private static final String child = "child";
	private static final String LAT = "lat";
	private static final String LONG = "long";

	private ModuleConfiguration config = null;
	
	@Override
	public void visit(final Model model, final Request request) {
		// TODO populate data model
	}

	@Override
	public void visit(final OntModel model, final Request request) {
		// TODO populate ontology model
	}

	@Override
	public void visit(final Query query, final Request request) {
		// TODO modify queries
	}
	
	@Override
	public void visit(SemantAquaUI ui, Request request) {
		Resource res = null;
		res = config.getResource("speciesHierarchy.js");
		Resource res2 = config.getResource("speciesHierarchy.jsp");

		ui.addScript(res);
		ui.addFacet(res2);
	}

	
	@QueryMethod
	public String queryForNearbySpeciesCounts(Request request){
		//this works for eBird only
		final Query query = config.getQueryFactory().newQuery(Type.SELECT);	
		//Variables
		final Variable site = query.getVariable(VAR_NS+SITE_VAR);
		String countyCode = (String) request.getParam("county");
		String stateAbbr = (String) request.getParam("state");	
		
		assert(countyCode != null);
		assert(stateAbbr != null);
		
		//URIs
		//final QueryResource dataSet = query.getResource("http://sparql.tw.rpi.edu/source/akn/dataset/GBBC_CSV/version/2012-Oct-19");
		final QueryResource dataSet = query.getResource("http://was.tw.rpi.edu/source/bird-data/dataset/ebird-data/version/2012-Nov-4");
		//<http://was.tw.rpi.edu/source/bird-data/dataset/ebird-data/version/2012-Nov-4>
		final QueryResource inDataSet = query.getResource("http://rdfs.org/ns/void#inDataset");
		                                             //http://rdfs.org/ns/void#inDataset	
		final QueryResource countyCoded = query.getResource(e1_NS + "countyCoded");
		final QueryResource stateAbbrev = query.getResource(e1_NS + "stateCoded");
		final Variable measurement = query.getVariable(VAR_NS+"measurement");
		final Variable count = query.getVariable(QUERY_NS+"count");
		final Variable date = query.getVariable(QUERY_NS+"date");
		final Variable commonName = query.getVariable(VAR_NS+"commonName");


		final Variable lat = query.getVariable(QUERY_NS+LAT);
		final Variable lng = query.getVariable(QUERY_NS+LONG);
		final QueryResource wgsLat = query.getResource(WGS_NS+LAT);
		final QueryResource wgsLong = query.getResource(WGS_NS+LONG);
		final QueryResource birdCount = query.getResource(e2_NS+"observation_count");
		final QueryResource obsDate = query.getResource(e2_NS+"observation_date");
		final QueryResource hasCommonName = query.getResource(TXN_NS+"CommonNameID");

		//build query
		Set<Variable> vars = new LinkedHashSet<Variable>();
		vars.add(measurement);
		vars.add(count);
		vars.add(date);
		vars.add(commonName);
		vars.add(lat);
		vars.add(lng);

		
		//vars.add(measurement);

		query.setVariables(vars);
		//query pattern
		final NamedGraphComponent graph = query.getNamedGraph("http://was.tw.rpi.edu/ebird-data");
		graph.addPattern(measurement, inDataSet, dataSet);
		graph.addPattern(measurement, countyCoded, countyCode,null);
		graph.addPattern(measurement, stateAbbrev, stateAbbr,null);
		graph.addPattern(measurement, birdCount, count);
		graph.addPattern(measurement, obsDate, date);
		graph.addPattern(measurement, hasCommonName, commonName);
		graph.addPattern(measurement, wgsLat, lat);
		graph.addPattern(measurement, wgsLong, lng);
		
		//we want to get their lat, long, category, commonName, observation date/time observation started, observationCount, 
		//http://lod.taxonconcept.org/ontology/txn.owl#CommonNameID
		//http://was.tw.rpi.edu/source/bird-data/dataset/ebird-data/vocab/enhancement/1/observation_count
		//http://was.tw.rpi.edu/source/bird-data/dataset/ebird-data/vocab/enhancement/1/observation_date
		
		//graph.addPattern(measurement, lat, lat,null);
		//graph.addPattern(measurement, long, long,null);
		//graph.addPattern(measurement, long, long,null);

//http://sparql.tw.rpi.edu/source/epa-air/id/aqs-site/08-001-3001

		
		// ?m void:inDataSet <http://sparql.tw.rpi.edu/source/akn/dataset/GBBC_CSV/version/2012-Oct-19>
		return config.getQueryExecutor(request).accept("application/json").execute(query);		
	}


	
	
	@QueryMethod
	public String queryBirdTaxonomy(Request request) throws IOException, JSONException{	
		
		// * this was for testing, now using real sparql data
		JSONArray data = new JSONArray();
		data.put(new JSONObject().put("Acanthis", "Fringillidae"));
		data.put(new JSONObject().put("Fringillidae", "Passeriformes"));
		data.put(new JSONObject().put("Passeriformes", "Aves"));
		data.put(new JSONObject().put("Aves", "Chordata"));
		data.put(new JSONObject().put("Chordata", "Animalia"));
		
		data.put(new JSONObject().put("Accipiter", "Accipitridae"));
		data.put(new JSONObject().put("Accipitridae", "Falconiformes"));
		data.put(new JSONObject().put("Falconiformes", "Aves"));
		data.put(new JSONObject().put("Aves", "Chordata"));
		data.put(new JSONObject().put("Chordata", "Animalia"));
		data.put(new JSONObject().put("Animalia", ""));
		
		//in the visit which has query and request for characteristic module (use archtype)
		//first check ifg bbbq state is null for chemical
		//new Query().findGraphComponentsWithPattern(?measurement, pol:hasCharacteristic, null)
		//returns a list of graphComponent. should be a singleon list, check on what it returns when empty
	    //if not empty, graph.addpattern();
		
		
		
		return data.toString();
		
		/*
		final Query query = config.getQueryFactory().newQuery(Type.SELECT);	
		//Variables
		final Variable child = query.getVariable(VAR_NS+ "child");
		final Variable parent = query.getVariable(VAR_NS+ "parent");		
		//URIs
		final QueryResource subClassOf = query.getResource(RDFS_NS + "subClassOf");
		final QueryResource inDataSet = query.getResource("http://rdfs.org/ns/void#inDataset");
		                                                 //http://rdfs.org/ns/void#inDataset
		//build query
		Set<Variable> vars = new LinkedHashSet<Variable>();
		vars.add(child);
		vars.add(parent);
		query.setVariables(vars);
		String parentClass = null;
		//query pattern
		//query.addPattern(site, inDataSet, dataSet);
		final NamedGraphComponent graph = query.getNamedGraph("http://was.tw.rpi.edu/bird-taxonomy");
		graph.addPattern(child, subClassOf, parent);

		// ?m void:inDataSet <http://sparql.tw.rpi.edu/source/akn/dataset/GBBC_CSV/version/2012-Oct-19>	
		String responseStr = FAILURE;
		//config.getQueryExecutor(request).accept("application/json").execute(query);
		
		
		String resultStr = config.getQueryExecutor(request).accept("application/json").execute(query);
		////return config.getQueryExecutor(request).accept("application/json").execute(query);

		
		log.debug("Results: "+resultStr);
		if(resultStr == null) {
			return responseStr;
		}
		try {
			JSONObject results = new JSONObject(resultStr);
			JSONObject response = new JSONObject();
			JSONArray data = new JSONArray();
			response.put("success", true);
			response.put("data", data);
			results = results.getJSONObject("results");
			JSONArray bindings = results.getJSONArray(BINDINGS);
			for(int i=0;i<bindings.length();i++) {
				JSONObject binding = bindings.getJSONObject(i);
				String childClass = binding.getJSONObject("child").getString("value");
				try {
					parentClass = binding.getJSONObject("parent").getString("value");
				}
				catch(Exception e) { }
				//if(labelStr == null) {
				//	labelStr = sourceUri.substring(sourceUri.lastIndexOf('/')+1).replace('-', '.');
				//}
				JSONObject mapping = new JSONObject();
				mapping.put("child", childClass);
				mapping.put("parent", parentClass);
				data.put(mapping);
			}
			responseStr = response.toString();
		} catch (JSONException e) {
			log.error("Unable to parse JSON results", e);
		}
		return responseStr;
		
		*/
		//return config.getQueryExecutor(request).accept("application/json").execute(query);		
		

		/*
		String endpoint = "";
		final Query query = config.getQueryFactory().newQuery(Type.SELECT);
		
		String responseStr = FAILURE;
		config.getQueryExecutor(request).accept("application/json").execute(query);
		String resultStr = config.getQueryExecutor(request).execute(endpoint, query);
		log.debug("Results: "+resultStr);
		if(resultStr == null) {
			return responseStr;
		}
		try {
			JSONObject results = new JSONObject(resultStr);
			JSONObject response = new JSONObject();
			JSONArray data = new JSONArray();
			response.put("success", true);
			response.put("data", data);
			results = results.getJSONObject("results");
			JSONArray bindings = results.getJSONArray(BINDINGS);
			for(int i=0;i<bindings.length();i++) {
				JSONObject binding = bindings.getJSONObject(i);
				String sourceUri = binding.getJSONObject(SOURCE_VAR).getString(VALUE);
				String labelStr = null;
				try {
					labelStr = binding.getJSONObject(LABEL_VAR).getString(VALUE);
				}
				catch(Exception e) { }
				if(labelStr == null) {
					labelStr = sourceUri.substring(sourceUri.lastIndexOf('/')+1).replace('-', '.');
				}
				JSONObject mapping = new JSONObject();
				mapping.put("uri", sourceUri);
				mapping.put(LABEL_VAR, labelStr);
				data.put(mapping);
			}
			responseStr = response.toString();
		} catch (JSONException e) {
			log.error("Unable to parse JSON results", e);
		}
		return responseStr;
		*/
		
	


		
	}


	@Override
	public String getName() {
		return "SpeciesDataProvider";
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public String getExtraVersion() {
		return null;
	}

	@Override
	public void setModuleConfiguration(final ModuleConfiguration config) {
		this.config = config;
	}

}
