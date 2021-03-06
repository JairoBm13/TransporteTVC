package security;


import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.libs.Crypto;
import com.avaje.ebean.Model;

import models.*;

/**
* Clase que representa una accion segura sobre una Vcub
*/
public class SecuredActionAdmin extends Action.Simple {

    /**
    * Metodo que que ejecuta un llamado sobre un contexto http.
    * Se encarga de extraer el token del header y comparar con el que se encuentra guardado
    * En caso de coincidir, retorna un llamado al contexto, en caso de que no, 
    * @param ctx - Http.Context
    * @return F.Promise<Result> resultado en una promesa
    */
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        String token = getTokenFromHeader(ctx);

        if (token != null) {

            
            Usuario user = (Usuario) new Model.Finder(Long.class, Usuario.class).where().eq("authToken", token).findUnique();
            if (user != null && user.getRol().equals(Cons.ROL_ADMIN)) {
                ctx.request().setUsername(user.getUsuario());
                return delegate.call(ctx);
            }

        }
        Result unauthorized = Results.unauthorized("unauthorized");
        return F.Promise.pure(unauthorized);
    }

    /**
    * Extrae el token del encabezado
    * @param ctx - Http .Context
    */
    private String getTokenFromHeader(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            return authTokenHeaderValues[0];
        }
        return null;
    }
}