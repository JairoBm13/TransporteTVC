package controllers;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class ReservaController extends Controller{

	public Result darReservas(){
		List<Reserva> reservas = new Model.Finder(Long.class, Reserva.class).all();
		return ok(Json.toJson(reservas));
	}

	public Result darReservasSinAsignar(){
		List<Reserva> reservas = new Model.Finder(Long.class, Reserva.class).
		where().eq("estado",  Cons.R_ESPERA).findList();
		return ok(Json.toJson(reservas));
	}

	public Result darReservasVencen(){
		List<Reserva> reservas = new Model.Finder(Long.class, Reserva.class).where().isNull("ruta").eq("fecha", Reserva.maniana()).findList();
		return ok(Json.toJson(reservas));
	}

    @BodyParser.Of(BodyParser.Json.class)
	public Result asignarConductor(){
        JsonNode j = Controller.request().body().asJson();
        Conductor conductor = (Conductor) new Model.Finder(Long.class, Conductor.class).byId(new Long(j.findPath("conductorId").asInt()));
        Reserva reserva = (Reserva) new Model.Finder(Long.class, Reserva.class).byId(new Long(j.findPath("reservaId").asInt()));
        Mobibus mobibus = (Mobibus) new Model.Finder(Long.class, Mobibus.class).byId(new Long(j.findPath("mobibusId").asInt()));
        reserva.getRuta().setConductor(conductor);
        reserva.getRuta().setBus(mobibus);
        reserva.setEstado(Cons.R_ASIGNADA);
        reserva.update();
        return ok(Json.toJson(reserva));
    }
}