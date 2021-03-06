package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Usuario;
import models.Cons;
import play.data.validation.Constraints;
import play.libs.Json;
import play.Logger;
import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import actions.CorsComposition;
import actions.ForceHttps;
import com.avaje.ebean.Model;
import security.*;

import static play.libs.Json.toJson;
import static play.mvc.Controller.request;
import static play.mvc.Controller.response;

@CorsComposition.Cors
@ForceHttps.Https
public class SecureUsuarioController extends Controller {

    public static final String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final String AUTH_TOKEN = "authToken";


    public static Usuario getUser() {
        return (Usuario)Http.Context.current().args.get("user");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result crearUsuario() {
        JsonNode j = Controller.request().body().asJson();
        Usuario usuario = Usuario.bind(j);
        usuario.save();

        response().setHeader(Cons.CORS, "*");
        return ok(Json.toJson(usuario));
    }

    // returns an authToken
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {

        response().setHeader(Cons.CORS, "*");
        JsonNode j = Controller.request().body().asJson();
        String user = j.findPath("user").asText();
        String pass = j.findPath("pass").asText();
        Usuario usuario = (Usuario) new Model.Finder(String.class, Usuario.class).where().eq("usuario",user).eq("password",pass).findUnique();
        if (usuario == null) {
            return unauthorized();
        }
        else {
            String authToken = usuario.createToken();
            ObjectNode authTokenJson = Json.newObject();
            authTokenJson.put(AUTH_TOKEN, authToken);
            response().setCookie(AUTH_TOKEN, authToken);
            return ok(Json.toJson(usuario));
        }
    }

    @With(SecuredActionUsuario.class)
    public Result logout() {
        response().discardCookie(AUTH_TOKEN);
        JsonNode j = Controller.request().body().asJson();
        String id = j.findPath("numeroIdentificacion").asText();
        Usuario usuario = (Usuario) new Model.Finder(String.class, Usuario.class).where().eq("numero_identificacion",id).findUnique();
        usuario.deleteAuthToken();
        response().setHeader(Cons.CORS, "*");
        return ok("User logged out");
    }

    public static class Login {

        @Constraints.Required
        @Constraints.Email
        public String emailAddress;

        @Constraints.Required
        public String password;

    }
}