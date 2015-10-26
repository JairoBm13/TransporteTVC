package controllers;

import java.util.Date;
import com.avaje.ebean.LikeType;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import actions.CorsComposition;
import actions.ForceHttps;
import security.*;
import utils.*;
import java.util.List;

@With(SecuredActionAdmin.class)
@CorsComposition.Cors
//@ForceHttps.Https
public class EstacionController  extends Controller{
	
    public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final String AUTH_TOKEN = "authToken";
    
	@BodyParser.Of(BodyParser.Json.class)
    public Result crearEstacion() {
        JsonNode j = Controller.request().body().asJson();
        Estacion estacion = Estacion.bind(j);

        String authToken = estacion.createToken();
        ObjectNode authTokenJson = Json.newObject();
        authTokenJson.put(AUTH_TOKEN, authToken);
        response().setCookie(AUTH_TOKEN, authToken);

        response().setHeader("Access-Control-Allow-Origin", "*");
        return ok(authTokenJson);
    }

    public Result darEstaciones() {
        List<Estacion> estaciones = new Model.Finder(Long.class, Estacion.class).all();
        
        response().setHeader("Access-Control-Allow-Origin", "*");
        return ok(Json.toJson(estaciones));
    }

    public Result darEstacion(Long id){
      Estacion estacion = (Estacion) new Model.Finder(Long.class, Estacion.class).byId(id);
      
      response().setHeader("Access-Control-Allow-Origin", "*");
      return ok(Json.toJson(estacion));  
    }

    public Result darEstacionesDesocupadas(){
        List<Estacion> estaciones = new Model.Finder(Long.class, Estacion.class).where().lt("ocupacion", 0.10).findList();
        
        response().setHeader("Access-Control-Allow-Origin", "*");
        return ok(Json.toJson(estaciones));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result llenarEstacion(){
        JsonNode j = Controller.request().body().asJson();
        Estacion estacion = (Estacion) new Model.Finder(Long.class, Estacion.class).byId(new Long(j.findPath("estacionId").asInt()));
        int cap = estacion.getCapacidad();
        List<Vcub> vcubs = new Model.Finder(Long.class, Vcub.class).where().isNull("estacion_id").findList();
        int i = 0;
        while(estacion.getVcubs().size() < cap/2 && i < vcubs.size()){
            Vcub vcubtemp = vcubs.get(i);
            
            vcubtemp.agregarPosicion(new Posicion(estacion.getLatitud(), estacion.getLongitud(), new Date()));
            vcubtemp.update();
            estacion.agregarVcub(vcubtemp);
            i++;
        }
        estacion.update();
        
        response().setHeader("Access-Control-Allow-Origin", "*");
        return ok(Json.toJson(estacion));  
    }

    public Result eliminarEstaciones(){
        List<Estacion> estaciones = new Model.Finder(Long.class, Estacion.class).all();
        for(int i = 0; i<estaciones.size();i++){
            estaciones.get(i).delete();
        }

        response().setHeader("Access-Control-Allow-Origin", "*");
        return ok(Json.toJson("")); 
    }

    public Result eliminarEstacion(Long id){
      Estacion estacion = (Estacion) new Model.Finder(Long.class, Estacion.class).byId(id);
      estacion.delete();
      response().setHeader("Access-Control-Allow-Origin", "*");
      return ok(Json.toJson(""));  
    }

}