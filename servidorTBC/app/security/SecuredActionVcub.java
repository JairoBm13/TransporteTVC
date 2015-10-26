package security;


import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import com.avaje.ebean.Model;

import models.*;

public class SecuredActionVcub extends Action.Simple {
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        String tokenCifrado = getTokenFromHeader(ctx);
        String tokenFimrado = getSignFronHeader(ctx);
        if (tokenCifrado != null && tokenFimrado != null) {

            String token = Crypto.decryptAES(tokenCifrado);
            Vcub vcub = (Vcub) new Model.Finder(Long.class, Vcub.class).where().eq("authToken", token).findUnique();
            if (vcub != null) {
                String llave = vcub.id;
                String verifica = Crypto.sign(token, llave.getBytes());
                if(verifica.equals(tokenFimrado)){
                    ctx.request().setUsername(vcub.getId()+"");
                    return delegate.call(ctx);
                }
            }
        }
        Result unauthorized = Results.unauthorized("unauthorized");
        return F.Promise.pure(unauthorized);
    }


    private String getTokenFromHeader(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            return authTokenHeaderValues[0];
        }
        return null;
    }

    private String getSignFronHeader(Http.Context ctx){
        String[]  authTokenHeaderValues = ctx.request().headers().get("X-SIGN-TOKEN");
        if((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)){
            return authTokenHeaderValues[0];
        }
        return null;
    }
}