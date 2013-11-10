/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc.mvc;

/**
 *
 * @author edmundas
 */
public class Results
{
    public static Result status(int statusCode) {

        return new Result(statusCode);

    }
    
    public static Result ok() {
        return status(Result.SC_200_OK);
    }

    public static Result notFound() {
        return status(Result.SC_404_NOT_FOUND);
    }

    public static Result forbidden() {
        return status(Result.SC_403_FORBIDDEN);
    }

    public static Result badRequest() {
        return status(Result.SC_400_BAD_REQUEST);
    }

//    public static Result noContent() {
//        return status(Result.SC_204_NO_CONTENT)
//                .render(new NoHttpBody());
//    }

    public static Result internalServerError() {
        return status(Result.SC_500_INTERNAL_SERVER_ERROR);
    }
    
    public static Result text() {
        return status(Result.SC_200_OK).text();
    }
    
    public static Result text(Object content) {
        return status(Result.SC_200_OK).text().content(content.toString());
    }
    
    public static Result html() {
        return status(Result.SC_200_OK).html();
    }

    public static Result json() {
        return status(Result.SC_200_OK).json();
    }

    public static Result jsonp() {
        return status(Result.SC_200_OK).jsonp();
    }

    public static Result xml() {       
        return status(Result.SC_200_OK).xml();
    }
}
