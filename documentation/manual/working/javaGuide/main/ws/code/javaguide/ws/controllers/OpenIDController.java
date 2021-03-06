/*
 * Copyright (C) 2009-2016 Typesafe Inc. <http://www.typesafe.com>
 */
package javaguide.ws.controllers;

import play.twirl.api.Html;

//#ws-openid-controller
import java.util.*;
import java.util.concurrent.CompletionStage;

import play.data.*;
import play.libs.openid.*;
import play.mvc.*;

import javax.inject.Inject;

public class OpenIDController extends Controller {

    @Inject
    OpenIdClient openIdClient;

    public Result login() {
        return ok(views.html.login.render(""));
    }

    public CompletionStage<Result> loginPost() {

        // Form data
        DynamicForm requestData = Form.form().bindFromRequest();
        String openID = requestData.get("openID");

        CompletionStage<String> redirectUrlPromise =
                openIdClient.redirectURL(openID, routes.OpenIDController.openIDCallback().absoluteURL(request()));

        return redirectUrlPromise
                .thenApply(Controller::redirect)
                .exceptionally(throwable ->
                                badRequest(views.html.login.render(throwable.getMessage()))
                );
    }

    public CompletionStage<Result> openIDCallback() {

        CompletionStage<UserInfo> userInfoPromise = openIdClient.verifiedId();

        CompletionStage<Result> resultPromise = userInfoPromise.thenApply(userInfo ->
                        ok(userInfo.id() + "\n" + userInfo.attributes())
        ).exceptionally(throwable ->
                        badRequest(views.html.login.render(throwable.getMessage()))
        );

        return resultPromise;
    }

    public static class views {
        public static class html {
            public static class login {
                public static Html render(String msg) {
                    return javaguide.ws.html.login.render(msg);
                }
            }
        }
    }

}
//#ws-openid-controller

class OpenIDSamples extends Controller {

    static OpenIdClient openIdClient;

    public static void extendedAttributes() {

        String openID = "";

        //#ws-openid-extended-attributes
        Map<String, String> attributes = new HashMap<>();
        attributes.put("email", "http://schema.openid.net/contact/email");

        CompletionStage<String> redirectUrlPromise = openIdClient.redirectURL(
                openID,
                routes.OpenIDController.openIDCallback().absoluteURL(request()),
                attributes
        );
        //#ws-openid-extended-attributes
    }

}